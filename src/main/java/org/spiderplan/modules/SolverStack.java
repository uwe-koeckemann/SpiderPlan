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
import org.spiderplan.representation.constraints.AppliedPlan;
import org.spiderplan.tools.logging.Logger;
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
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SolverStack(String name, ConfigurationManager cM) {
		super(name, cM);
		
		super.parameterDesc.add(  new ParameterDescription("solvers", "String", "", "List of modules implementing the SolverInterface that are used to find a solution.") );
		
		
		if ( cM.hasAttribute(name, "solvers")  ) {
			this.solverNames = cM.getStringList(name, "solvers");
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
		
		Stack<ResolverIterator> backtrackStack = new Stack<ResolverIterator>();
		Stack<Core> coreStack = new Stack<Core>();

		if ( keepTimes ) StopWatch.start(msg("Copy"));
		Core currentCore = new Core();
		currentCore.setContext(core.getContext());
		currentCore.setPlan(core.getPlan());
		currentCore.setOperators(core.getOperators());
		currentCore.setTypeManager(core.getTypeManager());
		if ( keepTimes ) StopWatch.stop(msg("Copy"));
		
	
		SolverResult result;
		int i = 0;
		while ( i < solvers.size() ) {
			if ( verbose ) Logger.msg(getName(), "Running " + ((Module)solvers.get(i)).getName(), 0);

			if ( keepStats ) {
				Module.stats.increment(msg("Calling solver " + i + " " +((Module)solvers.get(i)).getName()));
				Module.stats.addLong(msg("Solver Sequence"), Long.valueOf(i));
			}
						
			if ( verbose ) Logger.depth++;
			if ( keepTimes ) StopWatch.start(msg("test: " + solverNames.get(i)));
			result = solvers.get(i).testAndResolve(currentCore);
			if ( keepTimes ) StopWatch.stop(msg("test: " + solverNames.get(i)));
			if ( verbose ) Logger.depth--;
					
			if ( verbose ) Logger.msg(getName(), ((Module)solvers.get(i)).getName() + " says -> " + result.getState().toString(), 0);
			
			if ( !result.getState().equals(State.Inconsistent) && result.getResolverIterator() == null ) {
				i++;
			} else {
				/**
				 * Push new resolver iterator (only when Searching)
				 */
				if ( result.getResolverIterator() != null ) {
					if ( verbose ) Logger.msg(getName(), "Pushing resolvers on stack level "+(backtrackStack.size()+1)+" ("+result.getResolverIterator().getName()+")", 1);
					if ( keepStats ) {
						Module.stats.increment(msg("Level " +(backtrackStack.size()+1) + " pushing"));
						Module.stats.addLong(msg("Pushing resolvers sequence"), Long.valueOf(i));
					}
					
					if ( keepTimes ) StopWatch.start(msg("Copy"));
					Core stackedCore = new Core();
					stackedCore.setContext(currentCore.getContext());
					stackedCore.setPlan(currentCore.getPlan().copy());
					stackedCore.setOperators(currentCore.getOperators());
					stackedCore.setTypeManager(core.getTypeManager());
					if ( keepTimes ) StopWatch.stop(msg("Copy"));
					
					if ( keepTimes ) StopWatch.start(msg("Pushing resolver"));
					coreStack.push(stackedCore);
					backtrackStack.push(result.getResolverIterator());
					if ( keepTimes ) StopWatch.stop(msg("Pushing resolver"));
				}
				/**
				 * Find next resolver (backtrack if needed)
				 */				
				if ( verbose ) Logger.msg(getName(), "Choosing next resolver...", 1);
				if ( keepTimes ) StopWatch.start(msg("Choosing resolver"));
				boolean usedBacktracking = false;
				Resolver r = null;
				while ( backtrackStack.size() > 0 && r == null ) {
					if ( verbose ) Logger.msg(getName(), "... trying stack level " + backtrackStack.size() + ": "+ backtrackStack.peek().getName(), 1);
					if ( keepStats ) Module.stats.increment(msg("Level " +(backtrackStack.size()) + " peeking"));
					if ( keepTimes ) StopWatch.start(msg("next(): " + backtrackStack.peek().getName()));
					r = backtrackStack.peek().next();
					if ( keepTimes ) StopWatch.stop(msg("next(): " + backtrackStack.peek().getName()));
					if ( r == null ) {
						usedBacktracking = true;
						if ( verbose ) Logger.msg(getName(), "Backtracking: No resolver found on stack level " + backtrackStack.size() + ": "+ backtrackStack.peek().getName(), 1);
						if ( keepStats ) {
							Module.stats.increment(msg("#Backtracking"));
							Module.stats.increment(msg("Level " +(backtrackStack.size()) + " popping"));
						}
						
						if ( keepTimes ) StopWatch.start(msg("Popping resolver"));
						backtrackStack.pop();
						coreStack.pop();
						if ( keepTimes ) StopWatch.stop(msg("Popping resolver"));
					}
				}
				if ( keepTimes ) StopWatch.stop(msg("Choosing resolver"));
				/**
				 * Apply next resolver (if one exists)
				 */	
				if ( r != null ) {							// found resolver to try next
					if ( keepTimes ) StopWatch.start(msg("Applying resolver"));
					if ( verbose ) {
						Logger.msg(getName(), "Applying resolver", 1);
//						Logger.msg(getName(), "Resolver: " + r, 3);
					}
					if ( keepStats ) {
						Module.stats.increment(msg("Applied resolvers"));
					}
					
					if ( keepTimes ) StopWatch.start(msg("Copy"));
					currentCore = new Core();
					currentCore.setContext(coreStack.peek().getContext().copy());
					currentCore.setPlan(coreStack.peek().getPlan());
					currentCore.setTypeManager(coreStack.peek().getTypeManager());
					currentCore.setOperators(coreStack.peek().getOperators());
					r.apply(currentCore.getContext());// this should be the only place in which currentCore.getContext() actually changes...
					if ( keepTimes ) StopWatch.stop(msg("Copy"));
					
					// TODO: this is a hack
					Collection<AppliedPlan> plans = r.getConstraintDatabase().getConstraints().get(AppliedPlan.class);
					if ( !plans.isEmpty() ) {
						currentCore.setPlan(plans.iterator().next().getPlan());
					}
					if ( keepTimes ) StopWatch.stop(msg("Applying resolver"));
					
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
		
		if ( isConsistent ) {
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
