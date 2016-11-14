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
import java.util.HashMap;
import java.util.Map;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.causal.StateVariableOverride;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * If future external events change variables of interest to the causal reasoner,
 * this module will decide when those events lead to a conflict and add constraints 
 * to change the state of the causal reasoner accordingly.
 * 
 * This can be used to detect state-variable scheduling conflicts with future events
 * and may allow to turn of the "symbolicValueScheduling" feature of the temporal
 * reasoner/scheduler.
 * 
 * This module causes inconsistencies and adds constraints that allow the causal 
 * reasoner to change its search space.
 * 
 * @author Uwe Köckemann
 *
 */
public class FindConflictsWithFutureEvents extends Module {
	
	IncrementalSTPSolver csp;
	boolean firstTime = true;
	
	long t0 = 0;
	
	ArrayList<Statement> initStatementSlice = new ArrayList<Statement>();
	Map<Atomic,Statement> initialStatements = new HashMap<Atomic, Statement>();
	Map<Atomic,ArrayList<Statement>> futureEvents = new HashMap<Atomic,ArrayList<Statement>>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public FindConflictsWithFutureEvents(String name, ConfigurationManager cM) {
		super(name, cM);
	
		csp = new IncrementalSTPSolver(0,Global.MaxTemporalHorizon);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		csp.isConsistent(core.getContext());	
		
		/**
		 * TODO: Switch to initial context should work as well and be more clear
		 * (since it happens only once)
		 */
		if ( firstTime ) {
			firstTime = false;
			
			for ( PlanningInterval pst : core.getContext().get(PlanningInterval.class)) {
				t0 = pst.getStartTimeValue();
			}
			
			initStatementSlice = csp.getTemporalSnapshotWithFuture(t0);
			
			for ( Statement s : initStatementSlice ) {
				initialStatements.put(s.getVariable(), s );					
			}

			for ( Statement sFuture : core.getContext().get(Statement.class) ) {
				if ( !futureEvents.containsKey(sFuture.getVariable()) ) {
					futureEvents.put(sFuture.getVariable(), new ArrayList<Statement>());
				}
				futureEvents.get(sFuture.getVariable()).add(sFuture);
			}

			// Remove initial Statements
			for ( Statement initStatement : initStatementSlice ) {
				futureEvents.get(initStatement.getVariable()).remove(initStatement);
			}

			// Remove effects that have already been added
			Plan p = core.getContext().getUnique(Plan.class);
			if ( p != null ) {
				for ( Operator a : core.getContext().getUnique(Plan.class).getActions() ) {
					for ( Statement e : a.getEffects() ) {
						if ( futureEvents.containsKey(e.getVariable()) ) {
							futureEvents.get(e.getVariable()).remove(e);
						}
					}
				}
			}
			// Remove events that originated from goals
			for ( OpenGoal og : core.getContext().get(OpenGoal.class) ) {
				if ( futureEvents.containsKey(og.getStatement().getVariable()) ) {
					futureEvents.get(og.getStatement().getVariable()).remove(og.getStatement());
				}
			}
			
			if ( verbose ) {
				Logger.msg(getName(),"Init. statement slice:", 3);
				for ( Statement initStatement : initStatementSlice ) {
					Logger.msg(getName(), initStatement.toString(), 3);
				}
				Logger.msg(getName(),"Future events:", 3);
				for ( Atomic var: futureEvents.keySet() ) {
					Logger.msg(getName(), var.toString() + " -> " + futureEvents.get(var), 3);
				}
			}
		}
						
		/**
		 * Apply all previous StateVariableOverrides so that we do not
		 * discover them again.
		 * 
		 * Also make list of all effects so that we can exclude them from
		 * future events.
		 */
		if ( keepTimes ) StopWatch.start("[" + this.getName() + "] Current statements and effect list");
		Map<Atomic,Statement> currentStatements = new HashMap<Atomic,Statement>();
		
		Map<Atomic,ArrayList<Statement>> varHistory = new HashMap<Atomic,ArrayList<Statement>>();
				
		for ( Statement s : initStatementSlice ) {
			if ( !varHistory.containsKey(s.getVariable()) ) {
				varHistory.put( s.getVariable(), new ArrayList<Statement>() );
			}
			varHistory.get(s.getVariable()).add(s);
		}
		
		currentStatements.putAll(initialStatements);
		ArrayList<Statement> effectsOfLastAction = new ArrayList<Statement>();
		ArrayList<Statement> effectList = new ArrayList<Statement>();
		ArrayList<Statement> checkList = new ArrayList<Statement>();
		
		Plan p = core.getContext().getUnique(Plan.class);
		if ( p != null  ) {
			for ( Operator a : core.getContext().getUnique(Plan.class).getActions() ) {
				effectsOfLastAction = a.getEffects();
				for ( Statement e : a.getEffects() ) {
					if ( !varHistory.containsKey(e.getVariable()) ) {
						varHistory.put( e.getVariable(), new ArrayList<Statement>() );
					}
					varHistory.get(e.getVariable()).add(e);
					
					currentStatements.put(e.getVariable(), e);
					effectList.add(e);
					checkList.add(e);
				}
			}
		}
		
		
		checkList.addAll(initStatementSlice);
	
		if ( keepTimes ) StopWatch.stop("[" + this.getName() + "] Current statements and effect list");
	
		
	
		boolean atLeastOneConflict = false;
		if ( keepTimes ) StopWatch.start("[" + this.getName() + "] Checking future events");

		for ( Statement sCurrent : checkList ) {
//			if ( verbose ) print("Statement: "  + sCurrent + " has future events: " + futureEvents.get(sCurrent.getVariable()), 2);
			Atomic var = sCurrent.getVariable();
			
			if ( futureEvents.containsKey(var) ) {				
				
				for ( Statement s : futureEvents.get(var) ) {
						
					if ( !s.equals(sCurrent) 
							&& !s.getValue().equals(sCurrent.getValue()) 
							&& !effectList.contains(s) 
							&& (!varHistory.containsKey(var) || !varHistory.get(var).contains(s))  ) {
						if ( verbose ) Logger.msg(getName() ,"Checking " + sCurrent + " (current) VS " + s + " (future)", 2);
//						if ( verbose ) Logger.msg(getName() ,csp.getBounds(sCurrent.getKey()).toString(), 2);
//						if ( verbose ) Logger.msg(getName() ,csp.getBounds(s.getKey()).toString(), 2);
						long initEET = csp.getBoundsArray(sCurrent.getKey())[2];
						long futureEST = csp.getBoundsArray(s.getKey())[0];
					
						if ( futureEST < initEET ) {
							if ( verbose ) Logger.msg(getName(), "Found conflict between " + sCurrent + " (EET="+initEET+") and " + s + "(EST="+futureEST+")", 2);
							
							StateVariableOverride svo = null;
							if ( !effectsOfLastAction.contains(sCurrent) ) { 
								 svo = new StateVariableOverride(sCurrent, s);
							} else {
								int historySize = varHistory.get(sCurrent.getVariable()).size();
								if ( historySize > 1 ) {
									svo = new StateVariableOverride( varHistory.get(sCurrent.getVariable()).get(historySize-2), s );
								} else {
									
								}
							}
							
							if ( svo != null ) {
								core.getContext().add(svo);
								if ( verbose ) Logger.msg(getName(),"Found conflict. Adding " + svo, 1);
							} else {
								if ( verbose ) Logger.msg(getName(),"Found conflict. No possible resolver.", 1);
							}
							atLeastOneConflict = true;
						} else {
							if ( verbose ) Logger.msg(getName() ,"No conflict.", 2);
						}
					}
				}
			}
		}
		if ( keepTimes ) StopWatch.stop("[" + this.getName() + "] Checking future events");
		
		if ( atLeastOneConflict ) {
			core.setResultingState(this.getName(), State.Inconsistent);
		} else {
			core.setResultingState(this.getName(), State.Consistent);
		}
	
		if ( verbose ) Logger.depth--;
		return core;
	}
}
