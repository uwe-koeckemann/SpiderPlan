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
package org.spiderplan.causal.forwardPlanning;

import java.util.Collection;

import org.spiderplan.causal.forwardPlanning.causalGraph.CausalGraphHeuristic;
import org.spiderplan.causal.forwardPlanning.fastForward.FastForwardHeuristic;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.hadd.HAdd;
import org.spiderplan.representation.types.TypeManager;


/**
 * Used to construct heuristics given their name and using the {@link Heuristic} interface.
 * 
 * @author Uwe Köckemann
 */
public class HeuristicFactory {
	/**
	 * Create and initialize a heuristic.
	 * @param name Name of the heuristic
	 * @param g goals
	 * @param A actions
	 * @param tM type manager
	 * @return the create heuristic
	 */
	public static Heuristic createHeuristic( String name, Collection<Goal> g, 
												Collection<StateVariableOperator> A, TypeManager tM ) {
		Heuristic h = null;
		
		if ( name.equals("CausalGraph") || name.equals("CG") || name.equals("CausalGraphHeuristic") ) {
			h = new CausalGraphHeuristic();
			h.initializeHeuristic(g, A, tM);
		} else if  ( name.equals("FastForward") || name.equals("FF") || name.equals("FastForwardHeuristic") ) {
			h = new FastForwardHeuristic();
			h.initializeHeuristic(g, A, tM);
		} else if  ( name.equals("HAdd") || name.equals("hAdd") || name.equals("YAHSPHeuristic") ) {
			h = new HAdd();
			h.initializeHeuristic(g, A, tM);
		} 
		
		if ( h != null )
			return h;
		else
			throw new IllegalArgumentException("Unknown heuristic: " + name);
	}
}
