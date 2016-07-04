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

import java.util.Collection;
import java.util.Map;

import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;

/**
 * Interface for forward planning heuristics.
 * 
 * @author @author Uwe Köckemann
 *
 */
public interface Heuristic {
	
	/**
	 * Initialize heuristic.
	 * 
	 * @param g goals
	 * @param A available actions
	 * @param tM type manager
	 */
	public void initializeHeuristic( Collection<Goal> g, Collection<StateVariableOperator> A, TypeManager tM );
	/**
	 * Calculate heuristic value.
	 * 
	 * @param s current state 
	 * @param g goals to reach
	 * @param dStructs data structures that may be shared between heuristics (to avoid re-computation)
	 * @return heuristic value
	 */
	public long calculateHeuristicValue( Map<Atomic,Term> s, Collection<Goal> g, CommonDataStructures dStructs );
	
	/**
	 * Get the heuristic values for each goal.
	 * <br>
	 * <b>Note:</b> Should not compute heuristic values and requires calling calculateHeuristics(...) beforehand.
	 * 
	 * @return map from goals to individual heuristic values
	 */
	public Map<SingleGoal,Long> getLastHeuristicValues();
			
	/**
	 * Setting to activate recording of computation times.
	 * @param keepTimes <code>true</code> if times should be recorded, <code>false</code> otherwise.
	 */
	public void setKeepTimes( boolean keepTimes );
}
