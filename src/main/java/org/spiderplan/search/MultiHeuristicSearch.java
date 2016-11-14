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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Abstract class providing a search with multiple queues.
 * Each time the search advances a different queue is selected.
 * This is used to allow search with multiple heuristics without
 * the need to combine them to a single value.
 * 
 * <p>
 * Relevant papers:
 * <li> Helmert, M. The fast downward planning system Journal of Artificial Intelligence Research, 2006, 26, 191-246 
 * <p>
 * 
 * @author Uwe Köckemann
 * @param <T> 
 */
public abstract class MultiHeuristicSearch<T extends MultiHeuristicNode> extends AbstractSearch {
	/**
	 * Which heuristic is used by nth queue
	 */
	protected T n;
	protected int[] queueToHeuristicMap ;
	protected ArrayList<T> visited = new ArrayList<T>();
		
	private ArrayList<PriorityQueue<T>> queues = new ArrayList<PriorityQueue<T>>();
		
	private Set<T> removeSet = new HashSet<T>();
	private ArrayList<LinkedList<T>> addedLast = new ArrayList<LinkedList<T>>();
			
	private boolean delayedExpansion = true;
	
	private int currentQueue = 0;
	private long proposedNodes = 0;
	private long expandedNodes = 0;
	private long prunedNodes = 0;
	
	protected abstract boolean isGoal(T n);
	protected abstract ArrayList<LinkedList<T>> expand(T n);
	
	/**
	 * Initialize search by providing root node.
	 * @param initNode the root node
	 */
	public void init( T initNode ) {
		this.n = initNode;
	}
	
	/**
	 * Run search until it succeeds or fails. See <code>step()</code>
	 * for details on each search step.
	 */
	public void run() {
		while ( !done ) {
			step();
		}
	}
	
	/**
	 * Advance search by a single step. It starts by pruning (if necessary).
	 * After this there are two options.
	 * The first option (delayed expansion) 
	 * expands the previously selected node <code>n</code> and selects a new one
	 * from the all nodes in the next queue.
	 * The second option selects a node from the current queue and expands it immediately. 
	 * This option is set by <code>setDelayedExpansion()</code> (see below).
	 * The first option is preferable if expanding a node is costly and nodes
	 * may be rejected by some external class (by calling <code>setNoGood()</code>). 
	 * In such cases pruning with the first option simply needs to
	 * remove <code>n</code> since it has not yet been expanded, while option two 
	 * would have to remove the expansion of <code>n</code> from all queues.
	 */
	public void step() {
		if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Prune");
		prune();
		if ( keepTimes ) StopWatch.stop("[MultiQueueSearch] Prune");
		
		if ( delayedExpansion ) {	
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
			
			PriorityQueue<T> queue = queues.get(electedQueueIdx);
			
			if ( queue.isEmpty() ) {
				continue;
			} else {
				n = null;
				while ( n == null ) {
					n = queue.poll();
					boolean gotIt = false;
					
					if ( keepTimes ) StopWatch.start("[MultiQueueSearch] Known node?");
					gotIt = visited.contains(n);
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
					continue;
				}
				
				if ( !delayedExpansion ) {	// expand selection immediately 
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
				success = false;
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
	 * Returns the number of nodes that were expanded
	 * by this search.
	 * @return the number of nodes expanded by this search
	 */
	public long getExpandedNodeCount() {
		return this.expandedNodes;
	}
	/**
	 * Returns the number of nodes proposed by this search.
	 * @return the number of nodes proposed by this search
	 */
	public long getProposedNodeCount() {
		return this.proposedNodes;
	}
	/**
	 * Returns number of pruned nodes
	 * @return the number of nodes pruned during search
	 */
	public long getPrunedNodeCount() {
		return this.prunedNodes;
	}
		
	/**
	 * Select behavior of search. Use <code>true</code> if search step should delay expansion of nodes until next search step.
	 * See documentation of <code>step()</code> for details.
	 * @param useDelayedExpansion <code>true</code> to enable delayed expansion.
	 */
	public void setDelayedExpansion( boolean useDelayedExpansion ) {
		this.delayedExpansion = useDelayedExpansion;
	}
		
	ArrayList<ArrayList<PriorityQueue<T>>> queueBackups = new ArrayList<ArrayList<PriorityQueue<T>>>();
	
	/**
	 * Used to start a fresh sub-search.
	 * If this search runs out of nodes the search returns to latest backup.
	 */
	public void backupAndClearQueues() {
		ArrayList<PriorityQueue<T>> newQueues = new ArrayList<PriorityQueue<T>>();
		ArrayList<PriorityQueue<T>> backup = new ArrayList<PriorityQueue<T>>();
		for ( int i = 0 ; i < this.queues.size() ; i++ ) {
			PriorityQueue<T> pQ = this.queues.get(i);
//			MultiHeuristicNode.setCompareIdx(i);
			if ( n != null ) {
				pQ.add(n);		// put n back on queue since we change search space
			}
			backup.add(pQ);
			newQueues.add(new PriorityQueue<T>(11, this.queues.get(i).comparator())); 
		}
		this.queueBackups.add(backup);
		this.queues = newQueues;
		n = null;			// set n null so we select a node from the new space next time.
	}
			
	@Override
	public MultiHeuristicNode getCurrentNode() {
		return this.n;
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
		if ( delayedExpansion ) {
			n = null;
		} else {
			undoLastExpand();
		}
	}
		
	/**
	 * Add a {@link MultiHeuristicNode} to search queue 
	 * @param i index of queue to be added to
	 * @param n a {@link MultiHeuristicNode}
	 */
	protected void addToQueue( int i, T n ) {
//		MultiHeuristicNode.setCompareIdx(queueToHeuristicMap[i]);
		this.queues.get(i).add(n);
	}
	
	/**
	 * Create a new queue
	 */
	protected void addNewQueue( Comparator<T> comp ) {
		this.queues.add( new PriorityQueue<T>(11, comp) );
	}
	
	protected void reset() {
		this.currentQueue = 0;
		this.queues = new ArrayList<PriorityQueue<T>>();
		this.queueBackups = new ArrayList<ArrayList<PriorityQueue<T>>>();
		this.removeSet = new HashSet<T>();
		this.addedLast = new ArrayList<LinkedList<T>>();
		this.visited = new ArrayList<T>();
	}
	
	private void heuristicExpand() {
		PriorityQueue<T> queue;
		for ( int i = 0 ; i < addedLast.size() ; i++ ) {
			queue = queues.get(i);
			for ( T nNext : addedLast.get(i) ) {
				queue.add(nNext);
			}			
		}
	}
	
	/**
	 * Remove all nodes that resulted from the last expansion
	 */
	private void undoLastExpand() {
		ArrayList<LinkedList<T>> newAdded = new ArrayList<LinkedList<T>>();
		for ( LinkedList<T> added : addedLast ) {
			prunedNodes += added.size();
			removeSet.addAll(added);
			newAdded.add( new LinkedList<T>() );
		}
		addedLast = newAdded;
	}
	
	private void prune() {
		if ( !removeSet.isEmpty() ) {
			this.prunedNodes += removeSet.size();
			for ( PriorityQueue<T> queue : queues ) {
				queue.removeAll(removeSet);
			}
			for ( ArrayList<PriorityQueue<T>> bQueues : this.queueBackups ) {
				for ( PriorityQueue<T> queue : bQueues ) {
					queue.removeAll(removeSet);
				}
			}
			removeSet.clear();
		}
	}
}
