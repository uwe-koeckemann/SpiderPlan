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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import org.spiderplan.tools.stopWatch.StopWatch;


public abstract class HeuristicSearch extends AbstractSearch {

	protected PriorityQueue<HeuristicNode> queue = new PriorityQueue<HeuristicNode>();
	public Set<HeuristicNode> removeSet = new HashSet<HeuristicNode>();
	LinkedList<HeuristicNode> addedLast = new LinkedList<HeuristicNode>();
	
	Set<HeuristicNode> visited = new HashSet<HeuristicNode>();
	
	public boolean expandAndSelect = true;
	
	public long proposedNodes = 0;
	public long expandedNodes = 0;
	public long prunedNodes = 0;
	int currentQueue = 0;
	
	public abstract boolean isGoal(HeuristicNode n);
	public abstract LinkedList<HeuristicNode> expand(HeuristicNode n);
	public abstract double heuristic(HeuristicNode n);
	
	public HeuristicNode n;
	
	public void init( HeuristicNode initNode ) {
		this.n = initNode;
		queue = new PriorityQueue<HeuristicNode>();
	}
	
	public void run() {
		while ( !done ) {
			step();
		}
	}
	
	public void step() {
		
		if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Prune");
		prune();
		if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Prune");

		
		if ( n != null ) {
			expandedNodes++;
			addedLast = expand(n);
			if ( isGoal(n) ) {
				success = true;
				done = true;
				solution = n;
			}
			if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Heuristic expand");
			heuristicExpand();
			if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Heuristic expand");
		}

		n = null;
		while ( n == null ) {
			n = queue.poll();
			if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Known node?");
			boolean gotIt = visited.contains(n);
			if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Known node?");
			if ( gotIt ) {
				n = null;
			} else {
				visited.add(n);
			}
			if ( queue.isEmpty() && n == null ) {
				break;
			}
		}
		
		if ( n == null ) {
			done = true;
		} else {
			proposedNodes++;
		}
	}
	
	public PriorityQueue<HeuristicNode> getQueue() {
		return queue;
	}
	
	public void heuristicExpand() {
		for ( HeuristicNode nNext : addedLast ) {
			queue.add(nNext);
		}			
	}
	
	public void prune() {
		if ( !removeSet.isEmpty() ) {
			this.prunedNodes += removeSet.size();
			queue.removeAll(removeSet);
			removeSet.clear();
		}
	}
	
	/**
	 * Set current node to null, so it will not be expanded.
	 * Instead during next step only a new node will be selected.
	 */
	public void setNoGood() {
		if ( expandAndSelect ) {
			n = null;
		} else {
			undoLastExpand();
		}
	}
	
	public void undoLastExpand() {
		LinkedList<HeuristicNode> newAdded = new LinkedList<HeuristicNode>();
		prunedNodes += addedLast.size();
		addedLast = new LinkedList<HeuristicNode>();
	}	
	
	@Override
	public HeuristicNode getCurrentNode() {
		return n;
	}
}
