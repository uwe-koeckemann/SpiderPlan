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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import org.spiderplan.tools.stopWatch.StopWatch;



public abstract class MultiQueueSearch extends AbstractSearch {

	private ArrayList<PriorityQueue<MultiHeuristicNode>> queues = new ArrayList<PriorityQueue<MultiHeuristicNode>>();
	
	/**
	 * Which heuristic is used by nth queue
	 */
	protected int[] queueToHeuristicMap ;
	
	public Set<MultiHeuristicNode> removeSet = new HashSet<MultiHeuristicNode>();
	ArrayList<LinkedList<MultiHeuristicNode>> addedLast = new ArrayList<LinkedList<MultiHeuristicNode>>();
	
	protected ArrayList<MultiHeuristicNode> visited = new ArrayList<MultiHeuristicNode>();
		
	public boolean expandAndSelect = true;
	
	public long proposedNodes = 0;
	public long expandedNodes = 0;
	public long prunedNodes = 0;
	int currentQueue = 0;
	
	public MultiHeuristicNode n;
	
	public abstract boolean isGoal(MultiHeuristicNode n);
	public abstract ArrayList<LinkedList<MultiHeuristicNode>> expand(MultiHeuristicNode n);
	
	public void init( MultiHeuristicNode initNode ) {
		this.n = initNode;
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

		if ( expandAndSelect ) {	
			if ( n != null ) {
				expandedNodes++;
				addedLast = expand(n);
				
				if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Heuristic expand");
				heuristicExpand();
				if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Heuristic expand");
			}
		}
		
		
		int electedQueueIdx;
		boolean atLeastOneNonEmptyQueue = false;
		
		for ( int i = 0 ; i < queues.size() ; i++ ) {
			electedQueueIdx = (currentQueue+i) % queues.size();
			
			PriorityQueue<MultiHeuristicNode> queue = queues.get(electedQueueIdx);
			
			if ( queue.isEmpty() ) {
				continue;
			} else {
				n = null;
				while ( n == null ) {
					n = queue.poll();
					boolean gotIt = false;
					
					if ( !n.forceExploration() ) {
						if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Known node?");
						gotIt = visited.contains(n);
						if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Known node?");
					}
					
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
					continue;
				}
				
				if ( !expandAndSelect ) {	// expand selection immediately 
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
				}
				
				atLeastOneNonEmptyQueue = true;
				break;
			}
		}
		
		if ( !atLeastOneNonEmptyQueue ) {
			if ( queueBackups.isEmpty() ) {			// No queue left
				n = null;
				done = true;
			} else {								// Another queue left -> continue there
				n = null;
				queues = queueBackups.get(0);
				queueBackups.remove(0);
				step();								// recursive call makes sure we select node or fail if no queue with usable node is left
			}
		} else {
			if ( isGoal(n) ) {
				success = true;
				done = true;
				solution = n;
			}
			proposedNodes++;
		}
		
		currentQueue = (currentQueue+1) % queues.size();
	}
	
	/**
	 * Add a {@link MultiHeuristicNode} to search queue 
	 * @param i index of queue to be added to
	 * @param n a {@link MultiHeuristicNode}
	 */
	public void addToQueue( int i, MultiHeuristicNode n ) {
		MultiHeuristicNode.setCompareIdx(queueToHeuristicMap[i]);
		this.queues.get(i).add(n);
	}
	
	/**
	 * Create a new queue
	 */
	public void addNewQueue() {
		this.queues.add( new PriorityQueue<MultiHeuristicNode>() );
	}
	
	/**
	 * Get number of {@link PriorityQueue}s in this search.
	 * @return Number of queues.
	 */
	public int getNumQueues() {
		return queues.size();
	}
	
	/**
	 * Get size of {@link PriorityQueue} i
	 * @param i index of queue in question.
	 * @return The size.
	 */
	public int getQueueSize( int i ) {
		return queues.get(i).size();
	}
	
	/**
	 * Get a new {@link Collection} containing the nodes of all search queues. 
	 * @return A {@link Collection} containing all unexplored {@link MultiHeuristicNode}s.
	 */
	public Collection<MultiHeuristicNode> getUnexploredNodes() {
		Set<MultiHeuristicNode> r = new HashSet<MultiHeuristicNode>();
		for ( PriorityQueue<MultiHeuristicNode> pQ : this.queues ) {
			r.addAll(pQ);
		}
		return r;
	}
	
	public void clearAllQueues() {
		for ( PriorityQueue<MultiHeuristicNode> pQ : this.queues ) {
			pQ.clear();
		}
	}
	
	/**
	 * Used to start a fresh sub-search.
	 * If this search runs out of nodes the search returns to latest backup.
	 */
	ArrayList<ArrayList<PriorityQueue<MultiHeuristicNode>>> queueBackups = new ArrayList<ArrayList<PriorityQueue<MultiHeuristicNode>>>();
	public void backupAndClearQueues() {
		ArrayList<PriorityQueue<MultiHeuristicNode>> newQueues = new ArrayList<PriorityQueue<MultiHeuristicNode>>();
		ArrayList<PriorityQueue<MultiHeuristicNode>> backup = new ArrayList<PriorityQueue<MultiHeuristicNode>>();
		for ( int i = 0 ; i < this.queues.size() ; i++ ) {
			PriorityQueue<MultiHeuristicNode> pQ = this.queues.get(i);
			MultiHeuristicNode.setCompareIdx(i);
			if ( n != null ) {
				pQ.add(n);		// put n back on queue since we change search space
			}
			backup.add(pQ);
			newQueues.add(new PriorityQueue<MultiHeuristicNode>());
		}
		this.queueBackups.add(backup);
		this.queues = newQueues;
		n = null;			// set n null so we select a node from the new space next time.
	}
	public ArrayList<ArrayList<PriorityQueue<MultiHeuristicNode>>> getBackUpQueues() {
		return queueBackups;
	}
			
	public void heuristicExpand() {
		PriorityQueue<MultiHeuristicNode> queue;
		for ( int i = 0 ; i < addedLast.size() ; i++ ) {
			queue = queues.get(i);
			MultiHeuristicNode.setCompareIdx(i);
			for ( MultiHeuristicNode nNext : addedLast.get(i) ) {
				queue.add(nNext);
			}			
		}
	}
	
	public void prune() {
		if ( !removeSet.isEmpty() ) {
			this.prunedNodes += removeSet.size();
			for ( PriorityQueue<MultiHeuristicNode> queue : queues ) {
				queue.removeAll(removeSet);
			}
			for ( ArrayList<PriorityQueue<MultiHeuristicNode>> bQueues : this.queueBackups ) {
				for ( PriorityQueue<MultiHeuristicNode> queue : bQueues ) {
					queue.removeAll(removeSet);
				}
			}
			removeSet.clear();
		}
	}
	
	/**
	 * If expandAndSelect is true: 
	 *   Set current node to null, so it will not be expanded.
	 *   Instead during next step only a new node will be selected.
	 * else:
	 *   Remove last expansion
	 * 
	 */
	public void setNoGood() {
		if ( expandAndSelect ) {
			n = null;
		} else {
			undoLastExpand();
		}
	}
	
	/**
	 * Remove all nodes that resulted from the last expansion
	 */
	private void undoLastExpand() {
		ArrayList<LinkedList<MultiHeuristicNode>> newAdded = new ArrayList<LinkedList<MultiHeuristicNode>>();
		for ( LinkedList<MultiHeuristicNode> added : addedLast ) {
			prunedNodes += added.size();
			removeSet.addAll(added);
			newAdded.add( new LinkedList<MultiHeuristicNode>() );
		}
		addedLast = newAdded;
	}	
	
	@Override
	public MultiHeuristicNode getCurrentNode() {
		return n;
	}
	
	protected void reset() {
		this.currentQueue = 0;
		this.queues = new ArrayList<PriorityQueue<MultiHeuristicNode>>();
		this.queueBackups = new ArrayList<ArrayList<PriorityQueue<MultiHeuristicNode>>>();
		this.removeSet = new HashSet<MultiHeuristicNode>();
		this.addedLast = new ArrayList<LinkedList<MultiHeuristicNode>>();
		this.visited = new ArrayList<MultiHeuristicNode>();
	}
}
