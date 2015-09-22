/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.spiderplan.causal.ApplyPlanIterator;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.DiscardedPlan;
import org.spiderplan.representation.constraints.PlanningInterval;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Checks if lifting variable in the last action of a {@link Plan}
 * causes an inconsistency and will add a {@link DiscardedPlan} 
 * constraint if this is the case. This allows to generalize failure.
 *
 * @author Uwe Köckemann
 *
 */
public class LiftedPruning extends Module {

	Module consistencyChecker;
	String consistencyCheckerName;
	
	List<String> methods;
	
	long t0 = 0;
	long temporalHorizon = Global.MaxTemporalHorizon;	
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public LiftedPruning(String name, ConfigurationManager cM) {
		super(name, cM);
	
		super.parameterDesc.add( new ParameterDescription("", "String", "", "Name of the module that verifies consistency of constraint databases.") );
		
		super.parameterDesc.add( new ParameterDescription("methods", "string", "Complete", "Comma-seperated list of approaches to find generalizations (Complete,SingleVariable,AllVariables).") );
				
		super.parameterDesc.add( new ParameterDescription("consistencyChecker", "String", "", "Name of the module that verifies consistency of constraint databases.") );
				
		this.consistencyCheckerName = cM.getString(this.getName(), "consistencyChecker" );
		
		consistencyChecker = ModuleFactory.initModule( this.consistencyCheckerName, cM );
		
		if ( cM.hasAttribute(name, "methods" )) {
			methods = cM.getStringList(name, "methods");
		} else {
			methods = new ArrayList<String>();
			methods.add("Complete");
		}
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		Collection<DiscardedPlan> prunedPlans = new HashSet<DiscardedPlan>();

		if ( methods.contains("AllVariables") ) {
			prunedPlans.addAll(singleShotAllVariables(core.getPlan(), core));
		}
		
		if ( prunedPlans.isEmpty() && methods.contains("Complete") ) {	
			prunedPlans.addAll(singleVariablePruning(core));
		}

		if ( !methods.contains("Complete") && methods.contains("SingleVariable") ) {
			prunedPlans.addAll(greedyPruning(core.getPlan(), core));
		}
		
		if ( keepStats ) Module.stats.addLong(this.msg("#Discarded plans"), Long.valueOf(prunedPlans.size()));
		
		for ( DiscardedPlan p : prunedPlans ) {
			if ( keepStats ) Module.stats.increment(this.msg("#Discarded plans"));
			if ( verbose ) Logger.msg(this.getName(), "Found " + prunedPlans.size() + " generalizations to discard.", 4);
			if ( verbose ) Logger.msg(this.getName(), "Pruning plan:\n" + p, 4);
			core.getContext().add(p);
		}
		
		if ( verbose ) Logger.depth--;
		return core;
	}
	
	private boolean satisfiableOnlyPlan( Plan plan, Core core ) {
		if ( keepStats ) Module.stats.increment(msg("Satisfiability test (just plan)")); 
		
		ConstraintDatabase context = new ConstraintDatabase();
		context = plan.apply(context);
		context.add(core.getContext());
		
//		System.out.println(context);
		
		Core testCore = new Core();
		testCore.setContext(context);
		testCore.setOperators(core.getOperators());
		testCore.setTypeManager(core.getTypeManager());
		testCore.setPlan(plan);
		testCore = consistencyChecker.run(testCore, false, 0);				
		return testCore.getResultingState(consistencyCheckerName).equals(Core.State.Consistent);
	}
	
	private boolean satisfiable( Plan plan, Core core ) {
		if ( keepStats ) Module.stats.increment(msg("Satisfiability test")); 
		ApplyPlanIterator aI = new ApplyPlanIterator(core.getContext(), plan, this.getName(), this.cM, false, core.getTypeManager());
		boolean atLeastOneSatisfiable = false;
		Resolver r = aI.next();
		while ( r != null ) {
			ConstraintDatabase context = core.getContext().copy();
			r.apply(context);
			
			Core testCore = new Core();
			testCore.setContext(context);
			testCore.setOperators(core.getOperators());
			testCore.setTypeManager(core.getTypeManager());
			testCore.setPlan(plan);
			
			testCore = consistencyChecker.run(testCore, false, 0);				
			
			if ( testCore.getResultingState(consistencyCheckerName).equals(Core.State.Consistent) ) {
				atLeastOneSatisfiable = true;
				break;
			}
			
			r = aI.next();
		}
		return atLeastOneSatisfiable;
	}
	
	private Collection<DiscardedPlan> singleShotAllVariables( Plan plan, Core core ) {
		
		Set<Term> allGroundTerms = new HashSet<Term>();
		
		for ( Operator a : plan.getActions() ) {
			allGroundTerms.addAll(a.getName().getGroundTerms());
		}
			
		if ( verbose ) Logger.msg(this.getName(), "Trying to lift: " + allGroundTerms, 1);
		Plan pCopy = plan.copy();
		for ( Term t : allGroundTerms ) {
			pCopy.liftTerm(t);
		}
		
//			boolean satisfiable = satisfiable(plan, core);
		Collection<DiscardedPlan> pruningList = new HashSet<DiscardedPlan>();
		boolean satisfiable = satisfiableOnlyPlan(plan, core);
		if ( satisfiable ) {
			if ( verbose ) Logger.msg(this.getName(), "Plan by itself is satisfiable..." + allGroundTerms, 1);
			satisfiable = satisfiable(plan, core);
		} else{
			if ( verbose ) Logger.msg(this.getName(), "Plan by itself is unsatisfiable..." + allGroundTerms, 1);
			pruningList.add(new DiscardedPlan(pCopy, true));
		}
					
		if ( satisfiable ) {
			if ( verbose ) Logger.msg(this.getName(), "Result satisfiable..." + allGroundTerms, 1);
		} else {
			if ( verbose ) Logger.msg(this.getName(), "Result unsatisfiable: This can be pruned", 1);
			pruningList.add(new DiscardedPlan(pCopy, false));
		}
						
		return pruningList;
	}

	private Collection<DiscardedPlan> greedyPruning( Plan plan, Core core ) {
	
		Stack<Set<Term>> queue = new Stack<Set<Term>>();
		Set<Set<Term>> processed = new HashSet<Set<Term>>();
		Set<Set<Term>> prunable = new HashSet<Set<Term>>();
		Set<Set<Term>> prunableWithUnsatPlan = new HashSet<Set<Term>>();
		Set<Term> allGroundTerms = new HashSet<Term>();
		
		for ( Operator a : plan.getActions() ) {
			allGroundTerms.addAll(a.getName().getGroundTerms());
		}
		
		for ( Term t : allGroundTerms ) {
			Set<Term> s = new HashSet<Term>();
			s.add(t);
			queue.add(s);
		}
		
		int numQueueElementsProcessed = 0;
		
		while ( !queue.isEmpty() ) {
			if ( keepStats ) Module.stats.increment(this.msg("Candidates evaluated"));
			numQueueElementsProcessed++;
			
			Set<Term> current = queue.pop();
			
			if ( verbose ) Logger.msg(this.getName(), "Trying to lift: " + current, 1);
			Plan pCopy = plan.copy();
			for ( Term t : current ) {
				pCopy.liftTerm(t);
			}
			
//			boolean satisfiable = satisfiable(plan, core);
			
			boolean satisfiable = satisfiableOnlyPlan(plan, core);
			if ( satisfiable ) {
				if ( verbose ) Logger.msg(this.getName(), "Plan by itself is satisfiable..." + current, 1);
				satisfiable = satisfiable(plan, core);
			} else{
				if ( verbose ) Logger.msg(this.getName(), "Plan by itself is unsatisfiable..." + current, 1);
				prunableWithUnsatPlan.add(current);
			}
						
			if ( satisfiable ) {
				if ( verbose ) Logger.msg(this.getName(), "Result satisfiable..." + current, 1);
				processed.add(current);
			} else {
				if ( verbose ) Logger.msg(this.getName(), "Result unsatisfiable: This can be pruned", 1);
				prunable.add(current);
				
				Set<Set<Term>> remList = new HashSet<Set<Term>>();
				for ( Set<Term> a : queue ) {
					if ( current.containsAll(a) ) {
						remList.add(a);
					}
				}
				queue.removeAll(remList);				
				
				for ( Term t : allGroundTerms ) {
					if ( !current.contains(t) ) {
						Set<Term> candidate = new HashSet<Term>();
						candidate.addAll(current);
						candidate.add(t);
						if ( !(queue.contains(candidate) || processed.contains(candidate)) ) {
							if ( verbose ) Logger.msg(this.getName(), "Adding candidate to queue..." + candidate, 3);
							queue.push(candidate);
						} else {
							if ( verbose ) Logger.msg(this.getName(), "Not adding candidate to queue..." + candidate, 3);
						}
					}
				}
			}
		}
		
		Set<Set<Term>> remList = new HashSet<Set<Term>>();
		for ( Set<Term> a : prunable ) {
			for ( Set<Term> b : prunable ) {
//				System.out.println(a + " VS " + v);
				if ( !a.equals(b) && a.containsAll(b) ) {
					remList.add(b);
				}
			}			
		}
		prunable.removeAll(remList);
		prunableWithUnsatPlan.removeAll(remList);
		
		Collection<DiscardedPlan> pruningList = new HashSet<DiscardedPlan>();
		for ( Set<Term> candidate : prunable ) {
			Plan pCopy = plan.copy();
			for ( Term t : candidate ) {
				pCopy.liftTerm(t);
			}
			if ( prunableWithUnsatPlan.contains(candidate) ) {
				pruningList.add(new DiscardedPlan(pCopy, true));
			} else {
				pruningList.add(new DiscardedPlan(pCopy));
			}
		}
		
		return pruningList;
	}
	
	private Collection<DiscardedPlan> singleVariablePruning( Core core ) {
		Collection<DiscardedPlan> prunedPlans = new ArrayList<DiscardedPlan>();
		Collection<Operator> inOperators = core.getOperators();
		
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(core);
		if ( pI != null ) {
			t0 = pI.getStartTimeValue();
			temporalHorizon = pI.getHorizonValue();
		}			
		
		if ( keepTimes ) StopWatch.start("[LiftedPruning] lastDecision");
		Plan plan = core.getPlan();
		
		Operator lastDecision = plan.getActions().get(plan.getActions().size()-1);

		if ( verbose ) Logger.msg(getName(), "Last decision: " + lastDecision.getName(), 1);	
		Operator lastDecisionOperator = null;
		for ( Operator o : inOperators ) {
			if ( o.getName().name().equals(lastDecision.getName().name()) ) {
				lastDecisionOperator = o;
			}
		}
		if ( keepTimes ) StopWatch.start("[LiftedPruning] lastDecision");
		
		if ( verbose ) Logger.msg(getName(), "Last decision (operator): " + lastDecisionOperator.getName(), 2);
		
		if ( keepTimes ) StopWatch.start("[LiftedPruning] lifting");
		for ( int i = 0 ; i < lastDecision.getName().getNumArgs(); i++ ) {

			Operator liftedOp = lastDecision.liftVariable(i, lastDecisionOperator);
			liftedOp.setOpenVariablesToMostRelaxed( core.getContext() );
			
			/**
			 * In the original plan connections from effects to preconditions were established.
			 * These need to be disconnected for statements with lifted variables, since the 
			 * statements are not the same anymore.
			 */
			
			
			if ( keepTimes ) StopWatch.start("[LiftedPruning] needToDisconnect");
			HashSet<Term> needToDisconnect = new HashSet<Term>();
			for ( Statement p : liftedOp.getPreconditions() ) {
				if ( !p.getVariable().isGround() || !p.getValue().isGround() ) {	
					if ( !p.getKey().isGround() ) {
						Substitution theta = new Substitution();
						theta.add(p.getKey(), Term.createConstant("g"+p.getKey()));
						liftedOp.substitute(theta);
					}
					needToDisconnect.add(p.getKey());
					
				}
			}
			if ( keepTimes ) StopWatch.stop("[LiftedPruning] needToDisconnect");
			
			if ( verbose ) Logger.msg(getName(),"Need to disconnect: " + needToDisconnect, 2);
			
			if ( keepTimes ) StopWatch.start("[LiftedPruning] copy remove add");
			Plan pCopy = plan.copy();
			pCopy.removeAction(pCopy.getActions().size()-1);
			pCopy.addAction(liftedOp);
			if ( keepTimes ) StopWatch.stop("[LiftedPruning] copy remove add");
			
			HashSet<Constraint> remList = new HashSet<Constraint>();
			for ( AllenConstraint tC : pCopy.getConstraints().get(AllenConstraint.class) ) {
				if ( needToDisconnect.contains(tC.getFrom()) || needToDisconnect.contains(tC.getTo())) {
					remList.add(tC);
				}
			}				
			pCopy.getConstraints().removeAll(remList);
			
			boolean satisfiable = satisfiable( pCopy, core ); 
									
			if ( keepTimes ) StopWatch.start("[LiftedPruning] testing DB");
			if ( satisfiable ) {
				if ( verbose ) Logger.msg(getName(), "Not pruning: " + liftedOp.getName(), 0);
			} else {
				if ( verbose ) Logger.msg(getName(), "Pruning: " + liftedOp.getName(), 0);
				prunedPlans.add(new DiscardedPlan(pCopy));
			}				
			
			if ( keepTimes ) StopWatch.stop("[LiftedPruning] testing DB");	
		}
		if ( keepTimes ) StopWatch.stop("[LiftedPruning] lifting");
		
		return prunedPlans;
	}
}
