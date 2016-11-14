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
import java.util.List;
import java.util.Stack;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes.OptimizationRelation;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.cost.Cost;
import org.spiderplan.representation.expressions.optimization.OptimizationTarget;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Calls a list of {@link Module} that implement the {@link SolverInterface}
 * and builds a backtracking stack based on their results.
 * 
 * @author Uwe Köckemann
 *
 */
public class SolverStack extends Module {
	
	List<String> solverNames = new ArrayList<String>();
	List<SolverInterface> solvers = new ArrayList<SolverInterface>();
	
	long timeout = -1;
	
	private boolean optimize = false;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SolverStack(String name, ConfigurationManager cM) {
		super(name, cM);
		
		super.parameterDesc.add(  new ParameterDescription("solvers", "String", "", "List of modules implementing the SolverInterface that are used to find a solution.") );
		super.parameterDesc.add(  new ParameterDescription("timeout", "integer", "-1", "Time in minutes that this module will keep searching after first solution was found. Use -1 to disable timeout and exhaust search space. Implementation lets current solver finish even if timeout was reached so it may actually take longer than timeout.") );
		super.parameterDesc.add(  new ParameterDescription("optimize", "boolean", "false", "Decides if optimization is attempted after first solution was found.") );
			
		
		if ( cM.hasAttribute(name, "solvers")  ) {
			this.solverNames = cM.getStringList(name, "solvers");
		} 		
		
		if ( cM.hasAttribute(name, "timeout")  ) {
			this.timeout = cM.getLong(name, "timeout") * 1000 * 60;
		} 	
		
		if ( cM.hasAttribute(name, "optimize")  ) {
			this.optimize = cM.getBoolean(name, "optimize");
		} 	

		for ( String mName : this.solverNames ) {
			Module m = ModuleFactory.initModule(mName, cM);
			if ( m instanceof SolverInterface ) {
				solvers.add((SolverInterface)m);
			} else {
				throw new IllegalArgumentException("Module \"" + mName + "\" does not implement SolverInterface." );
			}
		}
	}
	
	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed );
			return core;
		}
		if ( verbose ) Logger.depth++;
				
		boolean isConsistent = true;
		
		long startTime = 0;
		boolean startedTimer = false;
		
		Stack<ResolverIterator> backtrackStack = new Stack<ResolverIterator>();
		Stack<Core> coreStack = new Stack<Core>();

		Core currentCore = new Core();
		currentCore.setContext(core.getContext());
		currentCore.setPlan(core.getPlan());
		currentCore.setOperators(core.getOperators());
		currentCore.setTypeManager(core.getTypeManager());
		currentCore.setPredCore(core.getPredCore());
		
		Core bestSolution = null;
		
		if ( keepStats ) Statistics.creatCounter(msg("Backtracking"));
		
		List<Expression> bestSolutionCostConstraints = new ArrayList<Expression>();
		List<OptimizationTarget> optimizeTargets = core.getContext().get(OptimizationTarget.class);
		
		if ( optimizeTargets.size() > 1 ) {
			throw new IllegalStateException("Found more than one optimization target. This module only supports a single target at the moment. It is possible to use math constraints to combine all targets into a single value that can be optimized.");
		}
		
		if ( optimize ) {
			optimize = !optimizeTargets.isEmpty();
		} else {
			if ( !optimizeTargets.isEmpty() ) {
				System.out.println("[Warning] Found optimization target but optimization is turned off.");
			}
		}
		
		while ( true ) { // optimization loop
//			System.out.println(currentCore.getContext().get(Statement.class));
			
			SolverResult result;
			int i = 0;
			while ( i < solvers.size() ) {
				if ( startedTimer && (System.currentTimeMillis() > startTime+timeout) ) {
					isConsistent = false;
					break; // leads to breaking outer while loop
				}
				
				
				if ( verbose ) Logger.msg(getName(), "Running " + ((Module)solvers.get(i)).getName(), 0);
	
				if ( keepStats ) {
					Statistics.increment(msg("Calling solver " + i + " " +((Module)solvers.get(i)).getName()));
	//				Statistics.addLong(msg("Solver Sequence"), Long.valueOf(i));
				}
				
				if ( optimize ) {
					for ( Expression e : bestSolutionCostConstraints ) {
						if ( !currentCore.getContext().contains(e)) {
							currentCore.getContext().add(e);		
						}
					}
				}
				
				//TODO: search backtracks and replaces value lookup but the internal state of STP solver is still the same so value lookup is not restored at next call 
				
//				ValueLookup vl = currentCore.getContext().getUnique(ValueLookup.class);
//				if ( vl != null ) {
//					System.out.println("A: " + this.getName() + " " + solverNames.get(i) + " " + vl.hasInterval(Term.createConstant("E1_5379")));
//				}				
				
				if ( verbose ) Logger.depth++;
				if ( keepTimes ) StopWatch.start(msg("Test and find flaws " + solverNames.get(i)));
				result = solvers.get(i).testAndResolve(currentCore);
				if ( keepTimes ) StopWatch.stop(msg("Test and find flaws " + solverNames.get(i)));
				if ( verbose ) Logger.depth--;
				
//				currentCore.getContext().getUnique(ValueLookup.class);
//				if ( vl != null ) {
//					System.out.println("B: " + this.getName() + " " + solverNames.get(i) + " " + vl.hasInterval(Term.createConstant("E1_5379")));
//				}	
				
				if ( verbose ) Logger.msg(getName(), "    -> " + result.getState().toString(), 0);
				
				if ( !result.getState().equals(State.Inconsistent) && result.getResolverIterator() == null ) {
					i++;
				} else {
					/**
					 * Push new resolver iterator (only when Searching)
					 */
					if ( result.getResolverIterator() != null ) {
						if ( verbose ) Logger.msg(getName(), "Pushing resolvers on stack level "+(backtrackStack.size()+1)+" ("+result.getResolverIterator().getName()+")", 1);
	//					if ( keepStats ) {
	//						Statistics.increment(msg("Level " +(backtrackStack.size()+1) + " pushing"));
	//						Statistics.addLong(msg("Pushing resolvers sequence"), Long.valueOf(i));
	//					}
						
	//					if ( keepTimes ) StopWatch.start(msg("Copy"));
						Core stackedCore = new Core();
						stackedCore.setContext(currentCore.getContext());
						stackedCore.setPlan(currentCore.getPlan().copy());
						stackedCore.setOperators(currentCore.getOperators());
						stackedCore.setTypeManager(core.getTypeManager());
						stackedCore.setPredCore(currentCore.getPredCore());
	//					if ( keepTimes ) StopWatch.stop(msg("Copy"));
						
	//					if ( keepTimes ) StopWatch.start(msg("Pushing resolver"));
						coreStack.push(stackedCore);
						backtrackStack.push(result.getResolverIterator());
	//					if ( keepTimes ) StopWatch.stop(msg("Pushing resolver"));
					}
					/**
					 * Find next resolver (backtrack if needed)
					 */				
					if ( verbose ) Logger.msg(getName(), "Choosing next resolver...", 1);
	//				if ( keepTimes ) StopWatch.start(msg("Choosing resolver"));
					boolean usedBacktracking = false;
					Resolver r = null;
					while ( backtrackStack.size() > 0 && r == null ) {
						if ( verbose ) Logger.msg(getName(), "... trying stack level " + backtrackStack.size() + ": "+ backtrackStack.peek().getName(), 1);
	//					if ( keepStats ) Statistics.increment(msg("Level " +(backtrackStack.size()) + " peeking"));
						if ( keepTimes ) StopWatch.start(msg("Getting resolver " + backtrackStack.peek().getName()));
						r = backtrackStack.peek().next();
						if ( keepTimes ) StopWatch.stop(msg("Getting resolver " + backtrackStack.peek().getName()));
						if ( r == null ) {
							usedBacktracking = true;
							if ( verbose ) Logger.msg(getName(), "Backtracking: No resolver found on stack level " + backtrackStack.size() + ": "+ backtrackStack.peek().getName(), 1);
							if ( keepStats ) {
								Statistics.increment(msg("Backtracking"));
	//							Statistics.increment(msg("Level " +(backtrackStack.size()) + " popping"));
							}
							
							/**
							 * TODO: Add optional back jumping
							 * 
							 * 1) Get failed solver (or constraint? or resolvers?)
							 * 2) While type/constraint/resolvers fail: 
							 * 		a) Remove added resolvers and test if failed resolvers work.
							 * 3) If it works after n times we pop n times and take next resolver
							 *
							 * TODO: Add optional dynamic re-ordering:
							 * 	1) Type x constraint fails
							 * 	2) Estimate search space for all types y that have resolvers on stack
							 *  3) Swap ordering to minimize expected backtracking
							 *
							 */
							
	//						if ( keepTimes ) StopWatch.start(msg("Popping resolver"));
							backtrackStack.pop();
							coreStack.pop();
	//						if ( keepTimes ) StopWatch.stop(msg("Popping resolver"));
						}
					}
	//				if ( keepTimes ) StopWatch.stop(msg("Choosing resolver"));
					/**
					 * Apply next resolver (if one exists)
					 */	
					if ( r != null ) {							// found resolver to try next
	//					if ( keepTimes ) StopWatch.start(msg("Applying resolver"));
						if ( verbose ) {
							Logger.msg(getName(), "Applying resolver", 1);
							Logger.msg(getName(), r.toString(), 4);
						}
						if ( keepStats ) {
							Statistics.increment(msg("Applied resolvers"));
						}
						
	//					if ( keepTimes ) StopWatch.start(msg("Copy"));
						currentCore = new Core();
						currentCore.setContext(coreStack.peek().getContext().copy());
						currentCore.setPlan(coreStack.peek().getPlan());
						currentCore.setTypeManager(coreStack.peek().getTypeManager());
						currentCore.setOperators(coreStack.peek().getOperators());
						r.apply(currentCore.getContext());// this should be the only place in which currentCore.getContext() actually changes...
						currentCore.setPredCore(coreStack.peek());
						
	//					if ( keepTimes ) StopWatch.stop(msg("Copy"));
						
//						Collection<AppliedPlan> plans = r.getConstraintDatabase().get(AppliedPlan.class);
//						if ( !plans.isEmpty() ) {
//							currentCore.setPlan(plans.iterator().next().getPlan());
//						}
						
						Plan plan = r.getConstraintDatabase().getUnique(Plan.class);
						if ( plan != null ) {
							currentCore.setPlan(plan);
						}
						
	//					if ( keepTimes ) StopWatch.stop(msg("Applying resolver"));
						
						// Consistent with resolver does not require resetting i to 0
						// (we assume the resolver just added some information but
						// no constraints that need to be confirmed)
						if ( !usedBacktracking && result.getState().equals(State.Consistent) ) {
							i++;
						}
					} else { 									// ran out of choices -> inconsistent
						if ( verbose ) Logger.msg(getName(), "Empty backtrack stack: The chosen solvers cannot solve this problem", 1);
						isConsistent = false;
						break;
					}
					
					if ( !(!usedBacktracking && result.getState().equals(State.Consistent)) ) {
						i = 0;				
					}
				} 
			}
			if ( !optimize ) {
				break;
			} else {
				if ( !isConsistent ) {
					break; // no more solutions exist
				} else {
					i = 0;
					
	//				System.out.println("Solution:\n"+currentCore.getContext());
					bestSolution = new Core();
					bestSolution.setContext(currentCore.getContext().copy());
					bestSolution.setOperators(currentCore.getOperators());
					bestSolution.setTypeManager(currentCore.getTypeManager());
					bestSolution.setPlan(currentCore.getPlan());
					
					/**
					 * Create cost constraints that will ensure any new solution is better than or equal to the current one
					 * for all optimization targets.
					 */
					bestSolutionCostConstraints = new ArrayList<Expression>();
					
					ValueLookup computedValues = currentCore.getContext().get(ValueLookup.class).get(0);
					for ( OptimizationTarget ot : optimizeTargets ) {
//						System.out.println(computedValues);
//						System.out.println(ot);
						
						long value = computedValues.getInt(ot.getTargetTerm() );
						
						if ( verbose ) {
							Logger.msg(this.getName(), ot.getTargetTerm() + " = " + value, 1);
						}
						
						// TODO: strictly less than and greater than only really work for one target
						// for multiple targets at least one should be strictly less than. 
						// otherwise current solution is not rejected
						// -> disjunction if x_i < b_i would be easy to check in cost constraints
						//
						// (or (< x 10) (< y 10) (< z 5))
						if ( ot.getRelation().equals(OptimizationRelation.Minimize) ) {
							bestSolutionCostConstraints.add( new Cost(new Atomic("less-than", ot.getTargetTerm(), Term.createInteger(value) )) );
						} else {
							bestSolutionCostConstraints.add( new Cost(new Atomic("greater-than", ot.getTargetTerm(), Term.createInteger(value) )) );
						}
					}
					if ( this.timeout != -1 ) {
						startedTimer = true;
						startTime = System.currentTimeMillis();
					}
				}
			}
		}
				
		if ( optimize && bestSolution != null ) {
			if ( verbose ) Logger.msg(getName(),"Consistent", 0);
			bestSolution.setResultingState(getName(), State.Consistent);
			core = bestSolution;
		} else if ( !optimize && isConsistent ) {
			if ( verbose ) Logger.msg(getName(),"Consistent", 0);
			currentCore.setResultingState(getName(), State.Consistent);
			core = currentCore;
		} else {
			if ( verbose ) Logger.msg(getName(),"Inconsistent", 0);
			core.setResultingState(getName(), State.Inconsistent);
		}
		
		if ( verbose ) Logger.depth--;
		return core;
	}
}
