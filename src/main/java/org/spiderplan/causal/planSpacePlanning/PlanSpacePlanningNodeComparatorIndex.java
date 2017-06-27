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
package org.spiderplan.causal.planSpacePlanning;

import java.util.Comparator;

/**
 * Single heuristic comparator for nodes with multiple heuristics.
 * 
 * @author Uwe Köckemann
 *
 */
public class PlanSpacePlanningNodeComparatorIndex implements Comparator<PlanSpacePlanningNode> {
	
	private int compareToIndex;
	
	/**
	 * Create new comparator for a specific index.
	 * @param comparedIndex index of heuristic that is used by this comparator
	 */
	public PlanSpacePlanningNodeComparatorIndex( int comparedIndex ) {
		this.compareToIndex = comparedIndex;
	}

	@Override
	public int compare(PlanSpacePlanningNode arg0, PlanSpacePlanningNode arg1) {
		return (int)((arg0.getHeuristicValue(compareToIndex) - arg1.getHeuristicValue(compareToIndex)));
	}
}
