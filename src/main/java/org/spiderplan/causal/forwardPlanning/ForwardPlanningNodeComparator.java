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

import java.util.Comparator;

import org.spiderplan.search.MultiHeuristicNode;

/**
 * Single heuristic comparator for nodes with multiple heuristics.
 * 
 * @author Uwe Köckemann
 *
 */
public class ForwardPlanningNodeComparator implements Comparator<ForwardPlanningNode> {
	
	Comparator<MultiHeuristicNode> baseComparator;
	
	/**
	 * Create comparator for planning nodes that uses another comparator for multi-heuristic nodes.
	 * @param baseComparator comparator for multi-heuristic nodes
	 */
	public ForwardPlanningNodeComparator( Comparator<MultiHeuristicNode> baseComparator ) {
		this.baseComparator = baseComparator;
	}

	@Override
	public int compare(ForwardPlanningNode arg0, ForwardPlanningNode arg1) {
		boolean equalGoals = arg0.g.size() == arg1.g.size();
		int solvedThis = 0;
		int solvedArg0 = 0;
		
		if ( equalGoals ) {
			for ( int i = 0 ; i < arg0.g.size(); i++ ) {
				if ( !arg0.g.get(i).equals(arg1.g.get(i)) ) {
					equalGoals = false;
					break;
				}
				if ( arg0.g.get(i).wasReached() ) { 
					solvedThis++;
				}
				if ( arg1.g.get(i).wasReached() ) { 
					solvedArg0++;
				}
			}
		}
		
		if ( !equalGoals || (solvedThis - solvedArg0) == 0 ) {
			int hCompare = baseComparator.compare(arg0,arg1);

			if ( hCompare == 0 ) {
				if ( arg0.depth() < arg1.depth() ) {
					return -1;
				} else if ( arg0.depth() > arg1.depth() ) {
					return 1;
				}
				return 0;
			}
			return hCompare;
		} else {
			return solvedArg0 - solvedThis;
		}
	}
}
