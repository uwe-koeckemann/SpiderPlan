/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.causal.forwardPlanning.fastForward;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.Heuristic;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Implementation of Fast Forward heuristic.
 * <p>
 * Relevant papers:
 * <li> Hoffmann, J. & Nebel, B. The FF planning system: Fast plan generation through heuristic search Journal of Artificial Intelligence Research, 2001, 14, 2001
 * <p>
 * @author Uwe Köckemann
 *
 */
public class FastForwardHeuristic implements Heuristic {
	Collection<StateVariableOperator> A_orig;
	TypeManager tM;
	
	private ArrayList<EarliestLayerOperator> A;
	private ArrayList<EarliestLayerSVA> g;
	private HashMap<Entry<Term,Term>, EarliestLayerSVA> sEarliestMap;
	
	private Map<SingleGoal,Long> lastHeuristicValues = new HashMap<SingleGoal,Long>();
	
	private boolean keepTimes = false;
		
	@Override
	public void initializeHeuristic( Collection<Goal> g,
			Collection<StateVariableOperator> A, TypeManager tM) {
		this.A_orig = A;
		this.tM = tM;
		
		this.A = new ArrayList<EarliestLayerOperator>();
		sEarliestMap = new HashMap<Map.Entry<Term,Term>, EarliestLayerSVA>();
		
		for ( StateVariableOperator a_orig : A_orig ) {
			EarliestLayerOperator a = new EarliestLayerOperator();
			
			a.name = a_orig.getName();
			
			for ( Entry<Term,Term> p : a_orig.getPreconditions().entrySet() ) {
				EarliestLayerSVA pEarliset = sEarliestMap.get(p);
				if ( pEarliset == null ) {
					pEarliset = new EarliestLayerSVA(p);
					sEarliestMap.put(p, pEarliset);
					
				} 
				a.preconditions.add(pEarliset);				
			}
			
			for ( Entry<Term,Term> e : a_orig.getEffects().entrySet() ) {
				EarliestLayerSVA eEarliset = sEarliestMap.get(e);
				if ( eEarliset == null ) {
					eEarliset = new EarliestLayerSVA(e);
					sEarliestMap.put(e, eEarliset);
					
				} 
				a.effects.add(eEarliset);				
			}
			this.A.add(a);
		}
	}
	
	
	/**
	 * Test if all goals were reached.
	 * TODO: This goal can be null due to problems with initial state (probably unreachable goal/missing initial state variable)
	 * @return <code>true</code> if all goals are reached, <code>false</code> otherwise.
	 */
	public boolean reachedAllGoals ( ) {
		for ( EarliestLayerSVA goal : this.g ) {
			if ( goal == null || goal.earliestLayer == -1 ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public long calculateHeuristicValue(Map<Term, Term> s, Collection<Goal> g, CommonDataStructures dStructs ) {
		this.g = new ArrayList<EarliestLayerSVA>();
		
		for( Goal goal : g ) {
			for ( Entry<Term,Term> entry : goal.getEntries() ) {
				if ( sEarliestMap.get(entry) == null ) {
					System.err.println("got null goal for " + entry);	
				}
				
				if ( !goal.wasReached() && goal.requirementsSatisfied() ) {
					this.g.add(sEarliestMap.get(entry));
				}
			}
		}
		
		HashMap<EarliestLayerSVA, ArrayList<EarliestLayerOperator>> achieverMap = new HashMap<EarliestLayerSVA, ArrayList<EarliestLayerOperator>>();
		
		int i = 0;
		boolean reachedAllGoals = false;
		boolean reachedFixedPoint = false;
		
		if ( keepTimes ) StopWatch.start("[FastForward] Resetting stuff");
		for ( EarliestLayerSVA sva : this.sEarliestMap.values() ) {
			sva.earliestLayer = -1;
		}
		
		for ( EarliestLayerOperator a : this.A ) {
			a.earliestLayer = -1;
		}
		
		for ( Entry<Term,Term> e : s.entrySet() ) {
			EarliestLayerSVA sva = sEarliestMap.get(e);
			if ( sva == null ) {
				sva = new EarliestLayerSVA(e);
				sEarliestMap.put(e, sva);
			}
			sEarliestMap.get(e).earliestLayer = 0;
		}
		if ( keepTimes ) StopWatch.stop("[FastForward] Resetting stuff");
		
		/**
		 * Each iteration extends stateLayer i into i+1 and actionLayer i-1 into i
		 */
		if ( keepTimes ) StopWatch.start("[FastForward] Create planning graph");
		while ( !reachedAllGoals && !reachedFixedPoint ) {
			reachedFixedPoint = true;
			
			/**
			 * Add new actions and their effects if they have not been used before
			 * goalToActionMap maintains map to the operators that added effects for
			 * easy lookup when solving relaxed graph plan problem.
			 */
			for ( EarliestLayerOperator a : A ) {			
				if ( keepTimes ) StopWatch.start("[FastForward] App.?");
				boolean applicable = a.isFirstTimeApplicable(i);
				if ( keepTimes ) StopWatch.stop("[FastForward] App.?");
				
				if ( applicable ) {
					reachedFixedPoint = false;
					if ( keepTimes ) StopWatch.start("[FastForward] Maintaining map");
					for ( EarliestLayerSVA effect : a.effects ) {
						if ( !achieverMap.containsKey(effect) ) {
							achieverMap.put(effect, new ArrayList<EarliestLayerOperator>());
						}
						effect.setLayer(i+1);
						achieverMap.get(effect).add(a);
					}
					if ( keepTimes ) StopWatch.stop("[FastForward] Maintaining map");
				}
			}
			
			if ( keepTimes ) StopWatch.start("[FastForward] Reached all?");
			reachedAllGoals = reachedAllGoals();
			if ( keepTimes ) StopWatch.stop("[FastForward] Reached all?");
			
			i++;
		}
		
		if ( keepTimes ) StopWatch.stop("[FastForward] Create planning graph");
		
		/**
		 * Goal not reachable -> return "inf"
		 */
		if ( !reachedAllGoals && reachedFixedPoint ) {
			for ( EarliestLayerSVA goal : this.g ) {
				if ( goal == null || goal.earliestLayer == -1 ) {
					SingleGoal sGoal = new SingleGoal(goal.sva.getKey(), goal.sva.getValue());
					lastHeuristicValues.put(sGoal, Long.MAX_VALUE);
				}
			}
			
			return Long.MAX_VALUE;
		}
		
		Set<EarliestLayerSVA> gCurrent = new HashSet<EarliestLayerSVA>();
		Set<EarliestLayerSVA> gPrev = new HashSet<EarliestLayerSVA>();
		gCurrent.addAll(this.g);
		
		long actionsSelected = 0;
		
		if ( keepTimes ) StopWatch.start("[FastForward] Solving problem");
		for ( int j = i ;j > 0 ; j-- ) {
			for ( EarliestLayerSVA goal : gCurrent ) {
				if ( goal.earliestLayer < j ) {
					gPrev.add(goal);
				} else {
					actionsSelected += 1;
					EarliestLayerOperator selectedAction = achieverMap.get(goal).get(0);
					gPrev.addAll(selectedAction.preconditions);
//					actionsSelected += achieverMap.get(goal).size();
//					for ( EarliestLayerOperator sel : achieverMap.get(goal) ) {
//						gPrev.addAll(sel.preconditions);
//					}
				}
			}
			gCurrent = gPrev;
			gPrev = new HashSet<EarliestLayerSVA>();
		}
		if ( keepTimes ) StopWatch.stop("[FastForward] Solving problem");
		
		return actionsSelected;
	}

	@Override
	public Map<SingleGoal, Long> getLastHeuristicValues() {
		return lastHeuristicValues;
	}


	@Override
	public void setKeepTimes( boolean keepTimes ) {
		this.keepTimes = keepTimes;
	}
}
