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
package org.spiderplan.causal.forwardPlanning.hadd;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.Heuristic;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.goals.DisjunctiveGoal;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;


/**
 * Implementation of hAdd heuristic.
 * <p>
 * Relevant papers:
 * <li> Vidal, V. YAHSP2: Keep It Simple, Stupid Proc.~of the Int'l Planning Competition (IPC-7), 2011
 * <p>
 * @author Uwe Köckemann
 *
 */
public class HAdd implements Heuristic {
	
	private Map<SingleGoal,Long> lastHeuristicValues;
	
	private Collection<StateVariableOperator> O;
	private Set<Entry<Term,Term>> A;	
	
	@SuppressWarnings("unused")
	private boolean keepTimes = false;
	
	@Override
	public void initializeHeuristic(Collection<Goal> g,
			Collection<StateVariableOperator> A, TypeManager tM) {
		this.O = A;
		this.A = new HashSet<Map.Entry<Term,Term>>();
		for ( Goal goal : g ) {
			this.A.addAll(goal.getEntries());
		}
		for ( StateVariableOperator a : O ) {
			this.A.addAll(a.getPreconditions().entrySet());
			this.A.addAll(a.getEffects().entrySet());
		}
	}
	
	@Override
	public long calculateHeuristicValue(Map<Term, Term> s, Collection<Goal> g, CommonDataStructures dStructs ) {
		Map<Object,Long> cost = dStructs.getActionCosts(s);
		lastHeuristicValues = new HashMap<SingleGoal, Long>();
		long h = 0;
		
		for ( Goal goal : g ) {
			
			if ( !goal.wasReached() && goal.requirementsSatisfied() ) {
				if ( goal instanceof SingleGoal ) {
					for ( Entry<Term,Term> goalPart : goal.getEntries() ) {
						long goalCost = cost.get(goalPart);
						h = smartAddition(h, goalCost);
						lastHeuristicValues.put((SingleGoal)goal,goalCost);
					}
					
				} else if ( goal instanceof DisjunctiveGoal ) {
					long min = Long.MAX_VALUE;
					for ( Entry<Term,Term> goalPart : goal.getEntries() ) {
						long c = cost.get(goalPart);
						if ( c < min ) {
							min = c;
						}
						h = smartAddition(h, min);
					}
				}
				if ( h == Long.MAX_VALUE ) {
					break;
				}
			}
		}
		
		return h;
	}
	
	private long smartAddition( long a, long b ) {
		if ( a == Long.MAX_VALUE || b == Long.MAX_VALUE ) {
			return Long.MAX_VALUE;
		}
		return a+b;
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
