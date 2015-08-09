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
package org.spiderplan.search;

/*
 * Extension of Search that tries to find the best goal.
 * 
 * Also used to get approximate solutions, where a search
 * for the global optimum would take too long.
 * 
 * Ways to stop:
 * 1) maxNodes
 * 2) maxNodesAfterLastMin
 * 3) epsilon -> min. necessary improvement to keep looking
 * 4) goodEnough -> heuristic value used for termination
 */
public abstract class SearchOptimum extends Search {
	public long maxNodes = Long.MAX_VALUE;
	public long maxNodesAfterLastMin = Long.MAX_VALUE;
	public double epsilon = 0.0;
	public double goodEnough = Double.NEGATIVE_INFINITY;
	
	public double min = Double.MAX_VALUE;
	public Node argMin;
	
	private long exploredNodesAtLastMin = Long.MAX_VALUE;
	
	@Override
	public void step( SearchType searchType ) {
		super.step(searchType);
		if ( (done && success) ) {
			double h = heuristic(solution);
			if ( h < min ) {
				argMin = solution;
				exploredNodesAtLastMin = super.exploredNodes;
				if ( Math.abs(min - h) >= epsilon && super.exploredNodes <= maxNodes && !(h <= goodEnough) ) {
					done = false;
					success = false;
				} 
				min = h;
			} else {
				done = false;
				success = false;
			}
		} else if ( (argMin != null && exploredNodes > maxNodes) 
				||  (argMin != null && exploredNodes-exploredNodesAtLastMin > maxNodesAfterLastMin)) {
			done = true;
		}
	}		
}
