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
package org.spiderplan.causal.forwardPlanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set; 
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.causal.forwardPlanning.ForwardPlanningNode.EqualityCriteria;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.causal.DiscardedPlan;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.VariableDomainRestriction;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.plans.SequentialPlan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.UniqueID;

/**
 * Iterator over solutions to reaching {@link OpenGoal}s by using
 * a variation of heuristic forward planning.
 * 
 * @author Uwe Köckemann
 */
public class ForwardPlanningIterator extends ResolverIterator {
	
	private ConstraintDatabase originalContext;
	private Resolver lastProposedResolver;
	private Plan lastProposedPlan;
	
	Collection<OpenGoal> G;
	Collection<Operator> O;
	TypeManager tM;
	
	ForwardPlanningSearch planner;

	private long planID;
	
	long t0 = 0;
	long temporalHorizon = Global.MaxTemporalHorizon;

	boolean plannerInitialized = false;
		
	boolean deferredHeuristic = false;
	boolean updateOnlyWhenCausalPredecessorChanges = false;
	
	boolean multiEffectSupport = true;

	Map<Atomic,List<Term>> s0;
	ArrayList<Goal> g;

	int prunedByExternalModules = 0;
	int prunedThisTime = 0;
	int prunedNoGoods = 0;
	int lookAhead = 0;
	boolean yahspLookahead = false;
	
	Collection<DiscardedPlan> prunablePlans = new HashSet<DiscardedPlan>();
	
	boolean uniqueInitialState = false;
	
	String queueStyle = "Lexicographic";
	List<String> heuristicNames = new ArrayList<String>();
	boolean[] useHelpful;
	
	ArrayList<Operator> transitionOperators = new ArrayList<Operator>();
	
	Set<String> usedVars = new HashSet<String>();
	
	private boolean incremental = false;
	private String consistencyCheckerName;
	private Module consistencyChecker = null;
	
	private boolean advancedPruningEnabled = false;
	private String pruningModuleName;
	private Module pruningModule = null;
	
	
	private ApplyPlanIterator applyPlan = null;
	private boolean firstTime = true;
	
	private final static Term False = Term.createConstant("false");
//	private final static Term Executing = Term.createConstant("executing");
//	private final static Term TransitionOperatorLabel = Term.createConstant("transOp");
	
	/**
	 * Construct heuristic forward planning iterator by providing all necessary information. 
	 * @param cDB initial context
	 * @param G goals
	 * @param O available actions
	 * @param tM type manager 
	 * @param cManager configuration manager
	 * @param name name used by logging
	 */
	public ForwardPlanningIterator( ConstraintDatabase cDB, Collection<OpenGoal> G, Collection<Operator> O, TypeManager tM, ConfigurationManager cManager, String name ) {
		super(name, cManager);
		
		super.parameterDesc.add( new ParameterDescription("lookAhead", "int", "0", "Number of steps taken by one iteration of the causal reasoner. This can help to overcome plateaus in the heuristic function or to solve problems with required concurrency.") );
		super.parameterDesc.add( new ParameterDescription("symbolicValueScheduling", "boolean", "true", "Determines if this modules uses symbolic value scheduling.") );		
		super.parameterDesc.add( new ParameterDescription("multiEffectSupport", "boolean", "true", "Determines if this module uses multiple effects in case an operator uses the same state variable twice.") );
		super.parameterDesc.add( new ParameterDescription("yahspLookahead", "boolean", "false", "If true the lookahead function from YAHSP 2.0 is used.") );
		super.parameterDesc.add( new ParameterDescription("uniqueInitialState", "boolean", "false", "If true initial state will be created based on latest statements.") );		
		super.parameterDesc.add( new ParameterDescription("queueStyle", "string", "Lexicographic", "Decides if multiple heuristics are sorted lexicographicals (\"Lexicographic\") or a queue is created for each heuristic (\"MultiQueue\").") );
		super.parameterDesc.add( new ParameterDescription("heuristics", "string", "CausalGraph", "Comma-seperated list of heuristics (supported: FastDownward).") );
		super.parameterDesc.add( new ParameterDescription("useHelpfulTransitions", "string", "", "Comma-seperated list of true/false indicating whether heuristics use helpful actions.") );
		super.parameterDesc.add( new ParameterDescription("nodeEquality", "string", "PlanBased", "Determines when two nodes in the planner's search space are equal. In (\"PlanBased\") mode the sequence of actions has to be the same. In (\"StateBased\") mode state and actions have to be the same. In (\"ResultingStateBased\") result of applying action to state is taken for equality. While the second option will detect more loops it may throw away solutions in cases where the planner is under informed (i.e. states look the same to the causal reasoner, but are different in the constraint database).") );	
			
		ForwardPlanningNode.eqCriteria = EqualityCriteria.PlanBased;
		
		if ( cManager.hasAttribute(name, "lookAhead") ) {
			lookAhead = cManager.getInt(name, "lookAhead");
		}

		if ( cManager.hasAttribute(name, "yahspLookahead") ) {
			yahspLookahead = cManager.getBoolean(name, "yahspLookahead");
		}
		
//		if ( cManager.hasAttribute(name, "checkFutureEvents") ) {
//			findConflictsWithFutureEvents = cManager.getBoolean(name, "checkFutureEvents");
//		}
				
		if ( cManager.hasAttribute(name, "queueStyle" )) {
			queueStyle = cManager.getString(name, "queueStyle");
		}
		
		if ( cManager.hasAttribute(name, "nodeEquality" )) {
			String nodeEqStr = cManager.getString(name, "nodeEquality");
			ForwardPlanningNode.eqCriteria = ForwardPlanningNode.EqualityCriteria.valueOf(nodeEqStr);
		}
		
		if ( cManager.hasAttribute(name, "heuristics" )) {
			heuristicNames = cManager.getStringList(name, "heuristics");
		} else {
			heuristicNames = new ArrayList<String>();
			heuristicNames.add("CausalGraph");
		}
		
		if ( cManager.hasAttribute(name, "useHelpfulTransitions" )) {
			List<String> useStrs = cManager.getStringList(name, "useHelpfulTransitions");
			useHelpful = new boolean[useStrs.size()];
			for ( int i = 0 ; i < useStrs.size() ; i++ ) {
				if ( useStrs.get(i).equals("true") ) {
					useHelpful[i] = true;
				} else {
					useHelpful[i] = false;
				}
			}
		} else {
			useHelpful = new boolean[heuristicNames.size()];
			for ( int i = 0 ; i < useHelpful.length ; i++ ) {
				useHelpful[i] = false;
			}
		}
		
		if ( cManager.hasAttribute(name, "uniqueInitialState") ) {
			this.uniqueInitialState = cManager.getBoolean(name, "uniqueInitialState");
		}
		
		if ( cManager.hasAttribute(name, "multiEffectSupport") ) {
			this.multiEffectSupport = cManager.getBoolean(name, "multiEffectSupport");
		}
		
		if ( cManager.hasAttribute(name, "consistencyChecker") ) {			
			this.incremental = true;
			this.consistencyCheckerName = cManager.getString(this.getName(), "consistencyChecker" );
			this.consistencyChecker = ModuleFactory.initModule( this.consistencyCheckerName, cManager );
		}
		
		if ( cManager.hasAttribute(name, "pruningModule") ) {
			this.advancedPruningEnabled = true;
			this.pruningModuleName = cManager.getString(this.getName(), "pruningModule" );
			this.pruningModule = ModuleFactory.initModule( this.pruningModuleName, cManager );
		}
				
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(cDB);
		if ( pI != null ) {
			temporalHorizon = pI.getHorizonValue();
		}		
		
		planID = UniqueID.getID();
		
		this.originalContext = cDB.copy();
		this.O = O; 
		this.G = G;
		
		plannerInitialized = true;
		if ( keepTimes ) StopWatch.start(msg("Initializing"));
		initialize( cDB, G, O, tM );
		if ( keepTimes ) StopWatch.stop(msg("Initializing"));
	}
		
	private void recordStats() {
		if ( keepStats ) Statistics.setLong("["+this.getName()+"] Pruned (by external)", Long.valueOf(prunedByExternalModules));
		if ( keepStats ) Statistics.setLong("["+this.getName()+"] Pruned (total)", Long.valueOf(prunedNoGoods));	
	}
	
	private void printIterationInfo() {
				
		Logger.landmarkMsg(getName());
		
		String queueSizes = "(" + planner.getQueueSize(0);
		for ( int i = 1 ; i < planner.getNumQueues() ; i++ ) {
			queueSizes += ","+planner.getQueueSize(i);
		}
		queueSizes += ")";
		
		if ( verbose ) super.print("Proposed/expanded nodes: " + +planner.getProposedNodeCount() +"/" + planner.getExpandedNodeCount() + ", Queue size: " + queueSizes, 1);
		if ( verbose ) super.print("Pruned "+prunedNoGoods+" no goods, " + prunedThisTime + "/" + prunedByExternalModules + " pruned by external modules now/overall.", 1);
	
		ForwardPlanningNode fdn = (ForwardPlanningNode)planner.getCurrentNode();
		
		
		if ( fdn != null ) {
		
			super.print("Heuristic values: ", 1);
			
			for ( int i = 0 ; i < this.heuristicNames.size() ; i++ ) {
				super.print("  -"+this.heuristicNames.get(i)+ ": " + fdn.getHeuristicValue(i), 1);
			}
			
			super.print("Plan:", 2); 

			SequentialPlan plan = fdn.getPlan();
			super.print("    " + plan.toString().replace("\n", "\n    "), 0);
								
			
			if ( verbosity >= 3 ) {
				HashMap<Atomic,List<Term>> resultingState = new HashMap<Atomic, List<Term>>(); 
				resultingState.putAll(fdn.s);
				if ( fdn.a != null ) {
					resultingState.putAll(fdn.a.getEffects());
				}
				
				super.print("Resulting state:", 3);
				for ( Atomic k : resultingState.keySet()) {
					super.print("    " + k + " -> " + resultingState.get(k) ,3);
				}
			}

			ArrayList<Goal> unsatGoals = new ArrayList<Goal>();
		
			for ( Goal goal : fdn.g ) {
				if ( !goal.wasReached() ) {
					unsatGoals.add(goal);
				}
			}
			
			super.print("Unsatisfied goals ("+unsatGoals.size()+")", 1);
			for ( Goal goal : unsatGoals ) {
				super.print("    " + goal, 1);
			}
		}	
	}
	
	

	/**
	 * Last node was a no-good so there is no need to expand it
	 */
	public void prune() {
		prunedNoGoods++;
		planner.setNoGood();	// Meaning: there is no need to expand the current node...
	}
	
	
	@Override
	public Resolver next( ConstraintDatabase C ) {
		if ( ResolverIterator.killFlag ) {
			return null;
		}
		
		/**
		 * If we are applying a plan try the next possibility.
		 * If there are no more we set applyPlan back to null
		 * and continue to find the next plan.
		 */
		if ( applyPlan != null ) {
			Resolver r = applyPlan.next(C);
			if ( r != null ) {
				return r;
			} 
			applyPlan = null;
		}
		
		Logger.depth++;
//		planner.success = false;
		
//		/**
//		 * The following lines handle external constraints that
//		 * translate high-level knowledge to changes in the planning
//		 * problem and/or the search space of the planner. 
//		 */
//		this.handleStateVariableOverrides(  );
//		
		
		/**
		 * If we come back here after the first call 
		 * -> we assume something is wrong with previous solution
		 */
		if ( !firstTime ) {
			if ( verbose ) { 
				print("Last node was no-good...", 1);
				ForwardPlanningNode fpn = (ForwardPlanningNode) planner.getCurrentNode();
				if ( fpn != null ) { 
					if ( verbose ) print("Last plan length: " + fpn.depth(),1);
					if ( verbose ) print("Last plan: " + fpn.getPlanList(),1);
				}
			}
			
			if ( this.advancedPruningEnabled ) {
				ConstraintDatabase failedCDB = originalContext.copy();
				lastProposedResolver.apply(failedCDB);
				Core testCore = new Core();
				testCore.setContext(failedCDB);
				testCore.setTypeManager(this.tM);
				testCore.setOperators(O);
				testCore.setPlan(lastProposedPlan);
				
				if ( keepTimes ) StopWatch.start(msg("Running pruning module"));
				testCore = this.pruningModule.run(testCore);
				if ( keepTimes ) StopWatch.stop(msg("Running pruning module"));
				
				prunablePlans.addAll(testCore.getContext().get(DiscardedPlan.class));
//				this.handleDiscardedPlans();
			}
			
//			this.handleDiscardedPlans();
			if ( keepStats ) Statistics.increment("["+getName()+"] NoGoods");
			prune();
			planner.continueSearch();
//			planner.done = false;
//			planner.success = false;	
		} else {
			firstTime = false;
		}

		while ( !planner.isDone() ) {
			if ( keepTimes ) StopWatch.start(msg("Stepping"));
			planner.step();
			
			if ( keepTimes ) StopWatch.stop(msg("Stepping"));			
			if ( verbose ) printIterationInfo();
			if ( keepStats ) recordStats();
			
			
			if ( keepTimes ) StopWatch.start(msg("Plan pruned?"));
			boolean currentPlanPruned = this.isDiscarded((ForwardPlanningNode)planner.getCurrentNode());
			if ( keepTimes ) StopWatch.stop(msg("Plan pruned?"));
			
			if ( currentPlanPruned ) {
				this.prune();
			}

			if ( incremental && !currentPlanPruned && !(planner.getCurrentNode() == null) ) {
				if ( keepTimes ) StopWatch.start(msg("Incremental consistency check"));

				if ( verbose ) print("Testing partial plan...", 0);

				SequentialPlan pPlan = ((ForwardPlanningNode)planner.getCurrentNode()).getPlan();	
//				if ( resetUniqueIDs ) UniqueID.reset();

				ArrayList<Operator> allOps = new ArrayList<Operator>();
				allOps.addAll(O);
				allOps.addAll(this.transitionOperators);
			
				Plan p = new Plan(pPlan, allOps, planID);
				
//				System.out.println(p);

			
//				if ( resetUniqueIDs ) UniqueID.restore();
				
				
				ConstraintDatabase context = originalContext.copy();
				
				ApplyPlanIterator aI = new ApplyPlanIterator(context, p, this.getName(), this.cM, false, tM);
				
				
				Resolver applyResolver = aI.next(C); 
					
				boolean atLeastOneWorks = false;
				
				while ( applyResolver != null ) {
					Core checkCore = new Core();
					ConstraintDatabase app = context.copy();
//					for ( OpenGoal og : app.get(OpenGoal.class) ) {
//						app.add(og.getStatement());
//					}
					applyResolver.apply(app);
					
//					/**
//					 * All actions must be executed within planning horizon
//					 */
//					for ( Operator a : p.getActions() ) {
//						app.add(new AllenConstraint(a.getLabel(), TemporalRelation.Release, new Interval(new Term(t0),new Term("inf", false))));
//						app.add(new AllenConstraint(a.getLabel(), TemporalRelation.Deadline, new Interval(new Term(0),new Term(temporalHorizon))));
//					}
					
					checkCore.setContext(app);
					checkCore.setPlan(p);
					checkCore.setTypeManager(tM);
					checkCore = consistencyChecker.run(checkCore);

					if ( checkCore.getResultingState(consistencyCheckerName).equals(Core.State.Consistent) ) {
						atLeastOneWorks = true;
						break;
					} 
					applyResolver = aI.next(C);
				}
				
				if ( !atLeastOneWorks ) {
					if ( this.advancedPruningEnabled ) {
						ConstraintDatabase failedCDB = originalContext.copy();
						Core testCore = new Core();
						testCore.setContext(failedCDB);
						testCore.setTypeManager(this.tM);
						testCore.setOperators(O);
						testCore.setPlan(p);
						if ( keepTimes ) StopWatch.start(msg("Running pruning module"));
						testCore = this.pruningModule.run(testCore);
						if ( keepTimes ) StopWatch.stop(msg("Running pruning module"));
						this.prunablePlans.addAll( testCore.getContext().get(DiscardedPlan.class) );
//						this.handleDiscardedPlans();
					}
					
					this.prune();
					planner.continueSearch();
//					if ( planner.isDone() )  {
//						planner.done = false;
//					}
//					if ( planner.isSuccess() ) {
//						planner.success = false;
//					}
				}
				if ( keepTimes ) StopWatch.stop(msg("Incremental consistency check"));
			}
		}
		
		Plan p = null;
		Resolver r = null;
		if ( planner.isSuccess() ) {
			/**
			 * Setup ApplyPlanIterator to try different 
			 * possible applications of the solution plan.
			 */
			if ( verbose ) print("Success", 0);

			SequentialPlan pPlan = ((ForwardPlanningNode)planner.getGoalNode()).getPlan();	
//			if ( resetUniqueIDs ) UniqueID.reset();

			ArrayList<Operator> allOps = new ArrayList<Operator>();
			allOps.addAll(O);
			allOps.addAll(this.transitionOperators);
			
			p = new Plan(pPlan, allOps, planID);
						
			if ( keepStats ) Statistics.setLong(msg("|\\pi|"), Long.valueOf(p.getActions().size())); 
		
//			if ( resetUniqueIDs ) UniqueID.restore();
			ConstraintDatabase context = originalContext.copy();
			for ( OpenGoal og : context.get(OpenGoal.class) ) {
				og.setAsserted(true);
			}			
			applyPlan = new ApplyPlanIterator(context,p,this.name,this.cM, true, tM);		
			
			r = applyPlan.next(C);
			
			for ( OpenGoal og : this.G ) {
				r.getConstraintDatabase().add(new Asserted(og));
				r.getConstraintDatabase().add(og.getStatement());
			}
			
//			/**
//			 * All actions must be executed within planning horizon
//			 */
//			for ( Operator a : p.getActions() ) {
//				r.getConstraintDatabase().add(new AllenConstraint(a.getLabel(), TemporalRelation.Release, new Interval(new Term(t0),new Term(temporalHorizon))));
//				r.getConstraintDatabase().add(new AllenConstraint(a.getLabel(), TemporalRelation.Deadline, new Interval(new Term(0),new Term(temporalHorizon))));
//			}
			
//			planner.done = false;
			planner.continueSearch();
		} else {
			if ( verbose )  print("Fail", 0);
		}
		lastProposedResolver = r;
		lastProposedPlan = p;
				
		Logger.depth--;
		return r;
	}		
	
	private void initialize( ConstraintDatabase initDB, Collection<OpenGoal> G, Collection<Operator> O, TypeManager tM ) {
		this.tM = tM;
		this.originalContext = initDB.copy();
//		CSP = new MetaCSPAdapter( temporalHorizon );
		ArrayList<Term> ignoredKeys = new ArrayList<Term>();
				
		ArrayList<Statement> goalStatements = new ArrayList<Statement>();
		for ( OpenGoal g : G ) {
			if ( !g.isAsserted() ) {
				goalStatements.add(g.getStatement());
			}
		}
		
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(initDB);
		if ( pI != null ) {
			this.t0 = pI.getStartTimeValue();
			this.temporalHorizon = pI.getHorizonValue();
		}
		
		usedVars = CausalReasoningTools.getRelevantVariables(initDB, O);
				
//			for ( IgnoredByCausalReasoner ignoreC : goalDB.get(IgnoredByCausalReasoner.class) ) {
//				ignoredKeys.add(ignoreC.getKey());
//			}
//		for ( IgnoredByCausalReasoner ignoreC : initDB.get(IgnoredByCausalReasoner.class) ) {
//			ignoredKeys.add(ignoreC.getKey());
//		}
		
		if ( verbose ) {
			print("Goals:", 1);
			for ( OpenGoal g : G ) {
				print("    " + g, 1);
			}			
			print("The following variables will be used:", 1);
			for ( String uName : usedVars ) {
				print("    " + uName, 1);
			}
		}
		
		s0 = new HashMap<Atomic,List<Term>>(); 
		Map<Atomic,Long> sEST = new HashMap<Atomic,Long>();
		Map<Atomic,String> sESTargmax = new HashMap<Atomic,String>();
		Map<Atomic,Statement> argmax = new HashMap<Atomic,Statement>();
		
		ValueLookup tiLookup = initDB.getUnique(ValueLookup.class); 
		
		print("Getting latest changing statements for initial state...", 3);
		for ( Statement s : initDB.get(Statement.class) ) {
			if ( usedVars.contains( s.getVariable().getUniqueName() ) && !goalStatements.contains(s) ) {
				if ( !this.uniqueInitialState ) {
					if ( !s0.containsKey(s.getVariable())) {
						s0.put(s.getVariable(), new ArrayList<Term>());
					}
					s0.get(s.getVariable()).add(s.getValue());
				} else {
					if ( verbose ) {
						print("   " + s.getVariable() + " ---> " + s + " [" + tiLookup.getEST(s.getKey()) + " " + tiLookup.getLST(s.getKey()) + "]", 3);
						if ( argmax.get(s.getVariable()) != null ) print("   argmax:" + s.getVariable() + " ---> " + argmax.get(s.getVariable()) + " [" + tiLookup.getEST(argmax.get(s.getVariable()).getKey()) + " " + tiLookup.getLST(argmax.get(s.getVariable()).getKey()) + "]", 3);
					}
					if ( !sEST.containsKey(s.getVariable()) ) {
						sEST.put(s.getVariable(), tiLookup.getEST(s.getKey()));
						sESTargmax.put(s.getVariable(), s.getKey().toString());
						s0.put(s.getVariable(), new ArrayList<Term>());
						s0.get(s.getVariable()).add(s.getValue());
						argmax.put(s.getVariable(), s);
					} else if ( tiLookup.getEST(s.getKey()) > sEST.get(s.getVariable()) ) {
						sEST.put(s.getVariable(), tiLookup.getEST(s.getKey()));
						sESTargmax.put(s.getVariable(), s.getKey().toString());
						s0.put(s.getVariable(), new ArrayList<Term>());
						s0.get(s.getVariable()).add(s.getValue());
						argmax.put(s.getVariable(), s);
					}				
				}
			}
		}
		
		if ( verbose ) {
			print("Initial state:", 2);
			for ( Atomic k : s0.keySet() ) {
				super.print("    " + k + " <- " + s0.get(k).toString().replace(",", " -> "), 2);
			}
		}
		
		/**
		 * Create goal list and order goals that use the same variable based on their
		 * earliest start time (EST).
		 */
		
//			if ( ! CSP.isConsistent( goalDB, tM ) ) {
//				throw new IllegalStateException("Temporal inconsistency in goal context)");
//			}
		
		g = new ArrayList<Goal>();
		ArrayList<SingleGoal> singleGoals = new ArrayList<SingleGoal>();
//			HashMap<Atomic,ArrayList<Goal>> variableGoalMap = new HashMap<Atomic, ArrayList<Goal>>();
		HashMap<Goal,Statement> goalStatementMap = new HashMap<Goal,Statement>();
		HashMap<Term,SingleGoal> statementGoalMap = new HashMap<Term,SingleGoal>();
		
		/**
		 * Assume boolean SVs from goals are initially false.
		 */
		for ( Statement s : goalStatements ) {
			if ( !ignoredKeys.contains( s.getKey() )) { // && !(s instanceof CustomStatement) ) { //&& !s.getKey().isGround() ) { 
				if ( !tM.isResourceAssignment(s, initDB) ) {
					if ( tM.getPredicateTypes(s.getVariable().getUniqueName(), -1).getName().equals(Term.createConstant("boolean")) ) {
						
						if ( !s0.containsKey(s.getVariable() )) {
							s0.put(s.getVariable(), new ArrayList<Term>() );
							s0.get(s.getVariable()).add(False);
						}
					}
				}
				
				Atomic var = s.getVariable();
				SingleGoal goal = new SingleGoal( var, s.getValue());
				
				singleGoals.add( goal );
				goalStatementMap.put(goal, s);
				statementGoalMap.put(s.getKey(), goal);
			}
		}
		
		ArrayList<SingleGoal> partOfDisjunction = new ArrayList<SingleGoal>();
		for ( SingleGoal sG : singleGoals ) {
			if ( !partOfDisjunction.contains(sG) ) {
				this.g.add(sG);
			}
		}
		
		for ( Goal goals1 : this.g ) {
			for ( SingleGoal goal1 : goals1.getSingleGoals() ) {
				for ( Goal goals2 : this.g ) {
					for ( SingleGoal goal2 : goals2.getSingleGoals() ) {
						if ( !goal1.equals(goal2) ) {
							Statement s1 = goalStatementMap.get(goal1);
							Statement s2 = goalStatementMap.get(goal2);
							/**
							 * More than one goal for same variable
							 * -> enforce order based on propagated temporal network
							 * TODO: make possible ways of deciding the ordering an option for the planner 
							 */
							long goal1EST = tiLookup.getEST(s1.getKey());	
							long goal2EST = tiLookup.getEST(s2.getKey());
							long goal1LET = tiLookup.getLET(s1.getKey());	
							long goal2LET = tiLookup.getLET(s2.getKey());
							
							long goal1LST = tiLookup.getLST(s1.getKey());	
							long goal2LST = tiLookup.getLST(s2.getKey());
							long goal1EET = tiLookup.getEET(s1.getKey());	
							long goal2EET = tiLookup.getEET(s2.getKey());
						
//							if ( goal1EST < goal2EST ) {
//								if ( !goals2.getRequirements().contains(goals1) ) {
//									goals2.addRequirement(goals1);					
//								}
//							} else if ( goal1EST > goal2EST ) {
//								if ( !goals1.getRequirements().contains(goals2) ) {
//									goals1.addRequirement(goals2);
//								}
//							}		
							
//							if ( goal1EET < goal2EET ) {
//								if ( !goals2.getRequirements().contains(goals1) ) {
//									if ( verbose ) {
//										super.print("Making goal " + s1 + " a requirement of "+s2+" (EET("+s1.getKey()+") < EET("+s2.getKey()+"))", 2);
//										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
//										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
//									}
//									if ( verbose ) 
//									if ( verbose ) 
//									goals2.addRequirement(goals1);					
//								}
//							} else if ( goal1EET > goal2EET ) {
//								if ( !goals1.getRequirements().contains(goals2) ) {
//									if ( verbose ) { 
//										super.print("Making goal " + s2 + " a requirement of "+s1+" (EET("+s2.getKey()+") < EET("+s1.getKey()+"))", 2);
//										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
//										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
//									}
//									goals1.addRequirement(goals2);
//								}
//							}
							
							if ( goal1LET < goal2LET ) {
								if ( !goals2.getRequirements().contains(goals1) ) {
									if ( verbose ) {
										super.print("Making goal " + s1 + " a requirement of "+s2+" (LET("+s1.getKey()+") < LET("+s2.getKey()+"))", 2);
										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
									}
									goals2.addRequirement(goals1);					
								}
							} else if ( goal1LET > goal2LET ) {
								if ( !goals1.getRequirements().contains(goals2) ) {
									if ( verbose ) { 
										super.print("Making goal " + s2 + " a requirement of "+s1+" (LET("+s2.getKey()+") < LET("+s1.getKey()+"))", 2);
										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
									}
									goals1.addRequirement(goals2);
								}
							} //else 
								
//							if ( goal1LST > goal2LST ) {
//								if ( !goals1.getRequirements().contains(goals2) ) {
////									System.out.println(s2 + " requires " + s1);
//									if ( verbose ) { 
//										super.print("Making goal " + s2 + " a requirement of "+s1+" (LET("+s2.getKey()+") < LET("+s1.getKey()+"))", 2);
//										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
//										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
//									}
//									goals1.addRequirement(goals2);
//									if (goal2EET < goal1LST ) {
////										System.out.println(s2 + " is a landmark");
//										goal2.setLandmark(true);
//									}
//								}
//							} else if ( goal1LST < goal2LST ) {
//								if ( !goals2.getRequirements().contains(goals1) ) {
////									System.out.println(s1 + " requires " + s2);
//									if ( verbose ) {
//										super.print("Making goal " + s1 + " a requirement of "+s2+" (LET("+s1.getKey()+") < LET("+s2.getKey()+"))", 2);
//										super.print( "Start time in ["+goal1EST+" "+goal1LST+"] end time in ["+goal1EET+" "+goal1LET+"]] for " + s1, 2);
//										super.print( "Start time in ["+goal2EST+" "+goal2LST+"] end time in ["+goal2EET+" "+goal2LET+"]] for " + s2, 2);
//									}
//									goals2.addRequirement(goals1);		
//									if (goal1EET < goal2LST ) {
////										System.out.println(s1 + " is a landmark");
//										goal1.setLandmark(true);
//									}
//								}
//							} 
							
//							if ( goal1LET < goal2EST ) {
//								if ( verbose ) super.print("Making goal " + s1 + " a landmark (LET("+s1.getKey()+") < EST("+s2.getKey()+"))", 2);
//								goal1.setLandmark(true);
//							} else if ( goal2LET < goal1EST ) {
//								if ( verbose ) super.print("Making goal " + s2 + " a landmark (LET("+s2.getKey()+") < EST("+s1.getKey()+"))", 2);
//								goal2.setLandmark(true);
//							}	

						}	
					}
				}
			}
		}
										
		HashMap<Atomic,Collection<Term>> sReachableValues = new HashMap<Atomic, Collection<Term>>();
		
		for ( Statement s : initDB.get(Statement.class) ) {
			if ( !goalStatements.contains(s) ) {
				if ( !sReachableValues.containsKey(s.getVariable()) ) {
					sReachableValues.put(s.getVariable(), new HashSet<Term>());
				}
				if ( !tM.isResourceAssignment(s, initDB) ) {		
					sReachableValues.get(s.getVariable()).add(s.getValue());	
				}
			}
		}
					
		ArrayList<StateVariableOperatorMultiState> svOperators = new ArrayList<StateVariableOperatorMultiState>();
		HashMap<StateVariableOperatorMultiState,Operator> origOperatorLookUp = new HashMap<StateVariableOperatorMultiState, Operator>();

		for ( Operator o : O ) { 
			StateVariableOperatorMultiState svo;
			if ( multiEffectSupport ) {
				svo = o.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
			} else {
				svo = o.getStateVariableBasedOperatorWithSingleEffectValue(usedVars);
			}
			svOperators.add(svo);
			origOperatorLookUp.put(svo, o);
		}

		Collection<Operator> app = new HashSet<Operator>();
		HashSet<StateVariableOperatorMultiState> checkList = new HashSet<StateVariableOperatorMultiState>();
		
		int appSizeBefore = -1;
			
		while ( ! (appSizeBefore == app.size()) ) {
			appSizeBefore = app.size(); 
			
//				Collection<Operator> appNew = CausalReasoningTools.getApplicableToMultiState(sReachableValues, O, C, usedVars, tM);

			Collection<Operator> appNew = new HashSet<Operator>();
			
			for ( StateVariableOperatorMultiState svo : svOperators ) {
//					print("Working on:    " + svo.toString().replace("\n","\n    "), 4);
				
				/**
				 * 1) (Partial) grounding based on preconditions
				 */
				Collection<StateVariableOperatorMultiState> svoApp = svo.getApplicablePartialGroundFromMultiState(sReachableValues, tM);				
				Operator o = origOperatorLookUp.get(svo);
				
				for ( StateVariableOperatorMultiState svAction : svoApp ) {
					Operator oCopy = o.copy();
					oCopy.substitute(svAction.getSubstitution());
					appNew.add(oCopy);
//					if ( !appNew.contains(oCopy) ) {
//						appNew.add(oCopy);
//					}
				}
				
				
//					print( "Reachable:", 4);
//					for ( Atomic key : sReachableValues.keySet() ) {
//						print( "    " + key + " -> " + sReachableValues.get(key), 4);
//					}
				
				/**
				 * 2) (Partial) grounding based on RelationalConstraints
				 */
//					Operator o = origOperatorLookUp.get(svo);
//					for ( StateVariableOperatorMultiState svAction : svoApp ) {
//						if ( !checkList.contains(svAction) ) {
//							print("Trying: " + svAction.getName().toString().replace("\n", "\n    "), 5);
//							
//							Operator oCopy = o.copy();
//							oCopy.substitute( svAction.getSubstitution() );
//							Collection<Substitution> relConstraintSubst = PrologTools.getSubstitutionsSatisfyingRelationalConstraints(oCopy,C);
//							
//							if ( relConstraintSubst != null ) {
//								if ( verbose ) {
//									for ( Substitution theta : relConstraintSubst ) print("    Satisfies logical: " + theta,5);
//								}
//								
//								if ( relConstraintSubst.isEmpty() ) {	// No additional substitution needed...
//									appNew.add(oCopy);
//								} else {								// Add action for all substitutions
//									for ( Substitution theta : relConstraintSubst ) {
//		//								print("    " + theta.toString(), 4);
//		//								Substitution thetaCopy = svAction.getSubstitution().copy();
//		//								thetaCopy.add(theta);
//		//								StateVariableOperator svActionRelGround = svo.copy();
//		//								svActionRelGround.substitute(thetaCopy);
//										
//										Operator a = oCopy.copy();
//										a.substitute(theta);
//										appNew.add(a);
//		//								print("    Adding: " + a.getName(), 4);
//									}
//								}
//							} else {
//								if ( verbose ) {
//									print("    No substitution satisfies logical constraints.",5);
//								}
//							}
//						}
//					}
			}			
			
			/**
			 * 3) Ground every open variable (not constrained otherwise)
			 * 		It's important to do this last, because there may be
			 * 		a lot of combinations otherwise. This step only grounds
			 * 		variable Terms in the name of the operator, since all
			 * 		other terms should be ground by now.
			 */
			ArrayList<Operator> remList = new ArrayList<Operator>();
			ArrayList<Operator> addList = new ArrayList<Operator>();
			for ( Operator o : appNew ) {
				if ( !o.getName().isGround() ) {
					remList.add(o);
					addList.addAll(o.getAllGround(tM));
				}
			}
			appNew.removeAll(remList);
			appNew.addAll(addList);
//			for ( Operator a : addList ) {
//				if ( !appNew.contains(a) ) {
//					appNew.add(a);
//				}
//			}
			
			/**
			 * 4) Filter out operators violating VariableDomainRestriction
			 */			
			remList.clear();
						
			for ( Operator o : appNew ) {
				boolean violatesVarDomRestriction = false;
				for ( Statement s : o.getPreconditions() ) {
					if ( !tM.isConsistentVariableTermAssignment(s.getVariable(), s.getValue())) {
						violatesVarDomRestriction = true;
						break;
					}
				}
				if ( !violatesVarDomRestriction ) {
					for ( Statement s : o.getEffects() ) {
						if ( !tM.isConsistentVariableTermAssignment(s.getVariable(), s.getValue())) {
							violatesVarDomRestriction = true;
							break;
						}
					}
				}				
				if ( !violatesVarDomRestriction ) {
					for ( Expression c : o.getConstraints() ) {
						if ( c instanceof VariableDomainRestriction ) {
							VariableDomainRestriction vdr = (VariableDomainRestriction)c;
							if ( !vdr.isConsistent() ) {
								violatesVarDomRestriction = true;
								break;
							}
						}
					}
				}
				if ( violatesVarDomRestriction ) {
					remList.add(o);
				}
			}
			appNew.removeAll(remList);
						
			/**
			 * Add all effects
			 */
			for ( Operator o : appNew ) {
				StateVariableOperatorMultiState svo;
				if ( multiEffectSupport ) {
					svo = o.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
				} else {
					svo = o.getStateVariableBasedOperatorWithSingleEffectValue(usedVars);
				}
				checkList.add(svo);
				
				for ( Entry<Atomic,List<Term>> e : svo.getEffects().entrySet() ) {
					if ( !sReachableValues.containsKey(e.getKey()) ) {
						sReachableValues.put( e.getKey() , new HashSet<Term>() );
					}
					sReachableValues.get(e.getKey()).addAll(e.getValue());
				};
			}
			app.addAll(appNew);
//			for ( Operator a : appNew ) {
//				if ( !app.contains(a) ) {
//					app.add(a);
//				}
//			}
		}
		
		if ( verbose ) {
			print("Reachable state:", 2);
			for ( Atomic k : sReachableValues.keySet() ) {
				super.print("    " + k + " <- " + sReachableValues.get(k), 2);
			}
		}
		
		ArrayList<String> sortedList = new ArrayList<String>();
		Map<String,Operator> map = new HashMap<String, Operator>();
		for ( Operator a : app ) {
			sortedList.add(a.getName().toString());
			map.put(a.getName().toString(), a);
		}
//			Collections.reverse(sortedList);
//		Collections.shuffle(sortedList);
		
		List<StateVariableOperatorMultiState> A = new ArrayList<StateVariableOperatorMultiState>();
		if ( verbose ) print("Applicable actions:", 4);
		for ( String k : sortedList ) {
			Operator a = map.get(k);
//				if ( verbose ) print("\t" + a.toString().replace("\n", "\n\t"),4);
			if ( verbose ) print("\t" + a.getName().toString() ,4);
			StateVariableOperatorMultiState svo;
			if ( multiEffectSupport ) {
				svo = a.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
			} else {
				svo = a.getStateVariableBasedOperatorWithSingleEffectValue(usedVars);
			}
			A.add(svo);
		}
				
		if ( verbose ) super.print("Found "+ A.size() +" ground operators", 1);
		
		
		if ( verbose ) super.print("Removing operators that cannot contribute to goals...", 1);
		
		List<StateVariableOperatorMultiState> filteredA = new ArrayList<StateVariableOperatorMultiState>();
		Map<Atomic,List<Term>> possibleSubGoals = new HashMap<Atomic,List<Term>>();
		for ( Goal goal : g ) {
			for ( SingleGoal sg : goal.getSingleGoals() ) {
				Atomic var = sg.getVariable();
				Term val = sg.getValue();
				if ( !possibleSubGoals.keySet().contains(var) ) {
					possibleSubGoals.put(var, new ArrayList<Term>());
				}
				possibleSubGoals.get(var).add(val);
			}
		}
		boolean change = true;
		while ( change ) {
			change = false;
			List<StateVariableOperatorMultiState> remList = new ArrayList<StateVariableOperatorMultiState>();
			
			for ( StateVariableOperatorMultiState a : A ) {
				
				for ( Atomic e_key : a.getEffects().keySet() ) {
					List<Term> values = possibleSubGoals.get(e_key); 
					if ( values != null ) {
						for ( Term e_value : a.getEffects().get(e_key) ) {
							if ( values.contains(e_value) ) {
								remList.add(a);
								filteredA.add(a);
								change = true;
								
								for ( Atomic p_key : a.getPreconditions().keySet() ) {
									if ( !possibleSubGoals.keySet().contains(p_key) ) {
										possibleSubGoals.put(p_key, new ArrayList<Term>());
									}
									possibleSubGoals.get(p_key).add(a.getPreconditions().get(p_key));
								}
							}
						}
					}
				}
			}
			A.removeAll(remList);
		}
		
		A = filteredA;
		if ( verbose ) super.print("Left with "+ A.size() +" ground operators", 1);
					
		planner = new ForwardPlanningSearch();

		planner.setLookAhead(lookAhead);
		planner.setName(super.getName());
		planner.setVerbose(super.verbose, super.verbosity);
		planner.setKeepTimes(super.keepTimes);
		planner.setYAHSPLookahead(this.yahspLookahead);
		
		if ( !incremental ) {
			planner.setDelayedExpansion(false);
		}
		
		/**
		 * Use operators with grounded observations to init. heuristics
		 */
		if ( this.queueStyle.equals("Lexicographic")) {
			planner.initLexicographicHeuristicOrder(s0, g, A, tM, heuristicNames, useHelpful[0] );		
		} else {
			planner.initSingleHeuristicPerQueue(s0, g, A, tM, heuristicNames, useHelpful );
		}
	}
	
//	private void findConflictsWithFutureEvents( ConstraintDatabase cDB, Plan p, TypeManager tM ) {
//		long t0 = 0;
//		
//		ArrayList<Statement> initStatementSlice = new ArrayList<Statement>();
//		Map<Atomic,Statement> initialStatements = new HashMap<Atomic, Statement>();
//		Map<Atomic,ArrayList<Statement>> futureEvents = new HashMap<Atomic,ArrayList<Statement>>();
//		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
//		
//		csp.isConsistent(cDB, tM);	
//					
//		for ( PlanningInterval pst : cDB.get(PlanningInterval.class)) {
//			t0 = pst.getStartTimeValue();
//		}
//		
//		initStatementSlice = csp.getTemporalSnapshotWithFuture(t0);
//		
//		for ( Statement s : initStatementSlice ) {
//			initialStatements.put(s.getVariable(), s );					
//		}
//
//		for ( Statement sFuture : cDB.get(Statement.class) ) {
//			if ( !futureEvents.containsKey(sFuture.getVariable()) ) {
//				futureEvents.put(sFuture.getVariable(), new ArrayList<Statement>());
//			}
//			futureEvents.get(sFuture.getVariable()).add(sFuture);
//		}
//
//		// Remove initial Statements
//		for ( Statement initStatement : initStatementSlice ) {
//			futureEvents.get(initStatement.getVariable()).remove(initStatement);
//		}
//
//		// Remove effects that have already been added
//		for ( Operator a : p.getActions() ) {
//			for ( Statement e : a.getEffects() ) {
//				if ( futureEvents.containsKey(e.getVariable()) ) {
//					futureEvents.get(e.getVariable()).remove(e);
//				}
//			}
//		}
//		// Remove events that originated from goals
//		for ( OpenGoal og : cDB.get(OpenGoal.class) ) {
//			if ( futureEvents.containsKey(og.getStatement().getVariable()) ) {
//				futureEvents.get(og.getStatement().getVariable()).remove(og.getStatement());
//			}
//		}
//		
//		if ( verbose ) {
//			print("Init. statement slice:",3);
//			for ( Statement initStatement : initStatementSlice ) {
//				print(initStatement.toString(), 3);
//			}
//			print("Future events:",3);
//			for ( Atomic var: futureEvents.keySet() ) {
//				print(var.toString() + " -> " + futureEvents.get(var), 3);
//			}
//		}
//		
//						
//		/**
//		 * Apply all previous StateVariableOverrides so that we do not
//		 * discover them again.
//		 * 
//		 * Also make list of all effects so that we can exclude them from
//		 * future events.
//		 */
//		StopWatch.start("[" + this.getName() + "] Current statements and effect list");
//		Map<Atomic,Statement> currentStatements = new HashMap<Atomic,Statement>();
//		
//		Map<Atomic,ArrayList<Statement>> varHistory = new HashMap<Atomic,ArrayList<Statement>>();
//				
//		for ( Statement s : initStatementSlice ) {
//			if ( !varHistory.containsKey(s.getVariable()) ) {
//				varHistory.put( s.getVariable(), new ArrayList<Statement>() );
//			}
//			varHistory.get(s.getVariable()).add(s);
//		}
//		
//		currentStatements.putAll(initialStatements);
//		ArrayList<Statement> effectsOfLastAction = new ArrayList<Statement>();
//		ArrayList<Statement> effectList = new ArrayList<Statement>();
//		ArrayList<Statement> checkList = new ArrayList<Statement>();
//		for ( Operator a : p.getActions() ) {
//			effectsOfLastAction = a.getEffects();
//			for ( Statement e : a.getEffects() ) {
//				if ( !varHistory.containsKey(e.getVariable()) ) {
//					varHistory.put( e.getVariable(), new ArrayList<Statement>() );
//				}
//				varHistory.get(e.getVariable()).add(e);
//				
//				currentStatements.put(e.getVariable(), e);
//				effectList.add(e);
//				checkList.add(e);
//			}
//		}
//		
//		
//		checkList.addAll(initStatementSlice);
//	
//		StopWatch.stop("[" + this.getName() + "] Current statements and effect list");
//	
//		boolean atLeastOneConflict = false;
//		StopWatch.start("[" + this.getName() + "] Checking future events");
//
//		Collection<StateVariableOverride> returnedConstraints = new ArrayList<StateVariableOverride>();
//		
//		for ( Statement sCurrent : checkList ) {
//			Atomic var = sCurrent.getVariable();
//			
//			if ( futureEvents.containsKey(var) ) {				
//				
//				for ( Statement s : futureEvents.get(var) ) {
//						
//					if ( !s.equals(sCurrent) 
//							&& !s.getValue().equals(sCurrent.getValue()) 
//							&& !effectList.contains(s) 
//							&& (!varHistory.containsKey(var) || !varHistory.get(var).contains(s))  ) {
//						if ( verbose ) print("Checking " + sCurrent + " (current) VS " + s + " (future)" ,2);
////						if ( verbose ) print(csp.getBounds(sCurrent.getKey()).toString() ,2);
////						if ( verbose ) print(csp.getBounds(s.getKey()).toString() ,2);
//						long initEET = csp.getBoundsArray(sCurrent.getKey())[2];
//						long futureEST = csp.getBoundsArray(s.getKey())[0];
//					
//						if ( futureEST < initEET ) {
//							if ( verbose ) print("Found conflict between " + sCurrent + " (EET="+initEET+") and " + s + "(EST="+futureEST+")", 2);
//							
//							StateVariableOverride svo = null;
//							if ( !effectsOfLastAction.contains(sCurrent) ) { 
//								 svo = new StateVariableOverride(sCurrent, s);
//							} else {
//								int historySize = varHistory.get(sCurrent.getVariable()).size();
//								if ( historySize > 1 ) {
//									svo = new StateVariableOverride( varHistory.get(sCurrent.getVariable()).get(historySize-2), s );
//								} else {
//									
//								}
//							}
//							
//							if ( svo != null ) {
//								returnedConstraints.add(svo);
//								if ( verbose ) print("Found conflict. Adding " + svo,1);
//							} else {
//								if ( verbose ) print("Found conflict. No possible resolver.",1);
//							}
//							atLeastOneConflict = true;
//						} else {
//							if ( verbose ) print("No conflict." ,2);
//						}
//					}
//				}
//			}
//		}
//		StopWatch.stop("[" + this.getName() + "] Checking future events");
//		
//		handleStateVariableOverrides(returnedConstraints, tM);
//	}
	
	/**
	 * Handles {@link DiscardedPlan} constraints that remove all plans
	 * that match the (possibly non-ground) plans in these constraints. 
	 * @param core
	 */
	private boolean isDiscarded( ForwardPlanningNode fpn ) {
		StopWatch.start("[ForwardPlanningModule] Testing if discarded");

		if ( this.prunablePlans.size() > 0 ) {
			if ( verbose ) print("Number of discarded plans: " + this.prunablePlans.size(), 2);  
		}
		for ( DiscardedPlan dP : this.prunablePlans ) {				
			Plan pruned = dP.getPlan();
			SequentialPlan pSeq;
			SequentialPlan pPrunedSeq = pruned.getSequentialPlan();
			boolean matches;
			
			if ( fpn != null ) {
				pSeq = fpn.getPlan();
				
				if ( dP.standaloneInconsistency() ) {					
					matches = pPrunedSeq.matchesEndOf(pSeq);
				} else {
					matches = pPrunedSeq.isMatchingSubPlan(pSeq);
				}
				
				if ( matches ) {
					if ( verbose ) print("Current plan prunable", 2);
					prunedByExternalModules ++;
					prunedThisTime ++;		
					return true;
				} else {
					if ( verbose ) print("Current not prunable", 2);
				}
			}
		}
		StopWatch.stop("[ForwardPlanningModule] Testing if discarded");
		return false;
	}
	
//	/**
//	 * Handles {@link DiscardedPlan} constraints that remove all plans
//	 * that match the (possibly non-ground) plans in these constraints. 
//	 * @param core
//	 */
//	private boolean handleDiscardedPlans( ) {
//		StopWatch.start("[ForwardPlanningModule] Pruning DiscardedPlans");
//		prunedThisTime = 0;
//		boolean currentPlanPrunable = false;
////		ArrayList<DiscardedPlan> dpList = new ArrayList<DiscardedPlan>();
////		dpList.addAll(C.get(DiscardedPlan.class));
//		if ( this.prunablePlans.size() > 0 ) {
//			if ( verbose ) print("Number of discarded plans: " + this.prunablePlans.size(), 2);  
//		}
//		for ( DiscardedPlan dP : this.prunablePlans ) {				
//			Plan pruned = dP.getPlan();
//			ForwardPlanningNode fdn;
//			SequentialPlan pSeq;
//			SequentialPlan pPrunedSeq = pruned.getSequentialPlan();
////			Plan queuedPlan;
//			boolean matches;
//			
//			if ( planner.getCurrentNode() != null ) {
//				fdn = (ForwardPlanningNode)planner.getCurrentNode();
//				pSeq = fdn.getPlan();
////				StopWatch.start("new Plan");
////				queuedPlan = new Plan(pSeq, O);
////				StopWatch.stop("new Plan");
////				matches = pruned.isMatchingSubPlan(queuedPlan);
//				matches = pSeq.isMatchingSubPlan(pPrunedSeq);
//				
//				if ( verbose ) print("Matching:\n" + pSeq + "\n\nwith:\n\n" + pruned.getSequentialPlan(), 2);
//								
//				if ( matches ) {
//					if ( verbose ) print("Current plan prunable", 2);
//					this.prune();
//					currentPlanPrunable = true;
//				} else {
//					if ( verbose ) print("Current not prunable" + pSeq, 2);
//				}
//			}
//							
////				SequentialPlan pPrunedSeq = pruned.getSequentialPlan();
//			for ( int i = 0 ; i < planner.getNumQueues() ; i++ ) {
//				for ( MultiHeuristicNode n : planner.getUnexploredNodes() ) { 
//					fdn = (ForwardPlanningNode)n;
//					pSeq = fdn.getPlan();
//					
////					StopWatch.start("new Plan");
////					queuedPlan = new Plan(pSeq, O);
////					StopWatch.stop("new Plan");
//
//					matches = pSeq.isMatchingSubPlan(pPrunedSeq);
//					
////					matches = pruned.isMatchingSubPlan(queuedPlan);
//						
//					if ( matches ) {
//						if ( verbose ) print("Pruning plan:\n" + pSeq, 2);
//						planner.removeSet.add(n);
//					}
//				}
//				for ( ArrayList<PriorityQueue<MultiHeuristicNode>> bQueue : planner.getBackUpQueues() ) {
//					for ( MultiHeuristicNode n : bQueue.get(i) ) {
//						fdn = (ForwardPlanningNode)n;
//						pSeq = fdn.getPlan();
//	
////						StopWatch.start("new Plan");
////						queuedPlan = new Plan(pSeq, O);
////						StopWatch.stop("new Plan");
//						matches = pSeq.isMatchingSubPlan(pPrunedSeq);
////						matches = pruned.isMatchingSubPlan(queuedPlan);
//						
//						if ( matches ) {
//							if ( verbose ) print("Pruning plan:\n" + pSeq, 2);
//							planner.removeSet.add(n);
//						}
//					}
//				}
//			}
//			prunedByExternalModules += planner.removeSet.size();
//			prunedThisTime += planner.removeSet.size();		
//			
//		}
//		StopWatch.stop("[ForwardPlanningModule] Pruning DiscardedPlans");
//		return currentPlanPrunable;
//	}
	
	/**
	 * Handle {@link StateVariableOverride} constraints
	 * These constraints are added when conflicts with future changes
	 * of statements are encountered. Transition actions are used
	 * to inject these future changes into the planner (overriding
	 * previous values) and thus allow to plan under the assumption
	 * of the new value.
	 * 
	 * Subsequences of plan length n are extended with transition actions: 
	 * These subsequences are a_i..a_j where a_i is the 
	 * last added transition action or a_0 if there is none.
	 * In the latter case also the empty plan is used to create
	 * a new plan containing only the transition action. 
	 * The last action j lies between i+1..n-1 where a_n is the last
	 * action that was added to the plan (and caused the conflict). 
	 * 
	 * 	Note: All transitions are added at once, so we do not allow
	 *  different transitions to be injected at different points in the plan.
	 * 
	 * @param core
	 */
//	private void handleStateVariableOverrides( Collection<StateVariableOverride> svoList, TypeManager tM ) {
//
//		if ( !svoList.isEmpty() ) {
//			if ( !tM.hasVariable("transitionAction")) {
//				tM.attachTypes(new Atomic("transitionAction()") , Executing );
//			}
//			
//			ForwardPlanningNode fpN = (ForwardPlanningNode)planner.getCurrentNode();
//			
//			ArrayList<ForwardPlanningNode> newNodes = new ArrayList<ForwardPlanningNode>();
//			
//			if ( verbose ) super.print("Got future event conflict (depth " + fpN.depth() + ")", 1);
//			
//			while ( fpN != null ) {
//				if ( fpN.a != null && fpN.a.getName().toString().startsWith("transitionAction") ) {
//					break;
//				}
//				fpN = fpN.prev;
//				if ( fpN != null ) {
//					ForwardPlanningNode fpnNew = new ForwardPlanningNode(fpN.getHeuristicValues().length);
//
//					fpnNew.a = fpN.a;
//					fpnNew.C = new ConstraintDatabase();
//					fpnNew.C.addAll(fpN.C);
//					fpnNew.g = fpN.g.copy();
//					fpnNew.prev = fpN.prev;
//					fpnNew.s = new HashMap<Atomic, List<Term>>();
//					fpnNew.s.putAll(fpN.s);	
//					
//					Operator transitionActionOp = new Operator();
//					long ID = UniqueID.getID();
//					transitionActionOp.setName(new Atomic("transitionAction"+ID+"()"));
//					Term label = TransitionOperatorLabel;
//					label.makeUnique(ID);
//					transitionActionOp.setLabel(label);
//					tM.attachTypes(new Atomic("transitionAction"+ID+"()") , Executing );
//					for ( StateVariableOverride svo : svoList ) {
//						
//						transitionActionOp.getPreconditions().add(svo.getFrom());
//						transitionActionOp.getEffects().add(svo.getTo());		
//						transitionActionOp.addConstraint(new AllenConstraint( svo.getFrom().getKey(), svo.getTo().getKey(),TemporalRelation.Meets ) );
//					}
//									
//					this.transitionOperators.add(transitionActionOp);
//					
////					StateVariableOperatorMultiState transitionSVO = transitionActionOp.getStateVariableBasedOperatorWithMultipleEffectValues();
//					
//					StateVariableOperatorMultiState transitionSVO;
//					if ( multiEffectSupport ) {
//						transitionSVO = transitionActionOp.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
//					} else {
//						transitionSVO = transitionActionOp.getStateVariableBasedOperatorWithSingleEffectValue(usedVars);
//					}
//					
//					ForwardPlanningNode fpnWithTransition = new ForwardPlanningNode(fpN.getHeuristicValues().length);
//					
//					fpnWithTransition.a = transitionSVO;
//					fpnWithTransition.C = new ConstraintDatabase();
//					fpnWithTransition.C.addAll(fpN.C);
//					fpnWithTransition.C.addAll(svoList);
//					fpnWithTransition.g = fpN.g.copy();
//					
//					fpnWithTransition.prev = fpnNew;
//					
//					fpnWithTransition.s = new HashMap<Atomic, List<Term>>();
//					fpnWithTransition.s.putAll(fpnNew.s);
//					fpnWithTransition.s.putAll(transitionSVO.getEffects());
//										
////					fpnWithTransition.setForceExploration(true);		// ignore visited list for this and child nodes
////					fpnWithTransition.setOverrideHeuristicValue(true); // always return 0 as heuristic value
//					newNodes.add(fpnWithTransition);
//				}
//			}
//			
//			// Create new queue(s) and keep old ones to return to if this does not lead anywhere
//			planner.backupAndClearQueues();
////			planner.clearVisited();
//			planner.addNewPlanningNodes(newNodes, true);
//			
//			for ( ForwardPlanningNode newNode : newNodes ) {
//				if ( verbose ) super.print("Created new node (depth "+newNode.depth()+")", 1);		
//				if ( verbose ) super.print("    h=" + Arrays.toString(newNode.getHeuristicValues()), 2);
//				if ( verbose ) super.print("    " + newNode.getPlan().toString().replace("\n", "\n    "), 2);
//			}
//		}
//	}

}
