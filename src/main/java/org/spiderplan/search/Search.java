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

import java.util.LinkedList;

import org.spiderplan.tools.stopWatch.StopWatch;

public abstract class Search extends AbstractSearch {
	
	public abstract boolean isGoal(Node n);
	public abstract LinkedList<Node> expand(Node n);
	public abstract double heuristic(Node n);
	
//	public boolean done = false;
//	public boolean success = false;
	
	public LinkedList<Node> queue;
	public LinkedList<Node> addedLast;
	public LinkedList<Node> removeList;

	public long exploredNodes = 0;
	public long prunedNodes = 0;
	
	public enum SearchType { DepthFirst, BreathFirst, Heuristic, HeuristicLayer };
	
	public Search(  ) {
		queue = new LinkedList<Node>();
		removeList = new LinkedList<Node>();
		addedLast = new LinkedList<Node>();
	}
	
	public boolean search( Node initNode, SearchType searchType ) {
		init(initNode);
		while ( !done ) 
			step(searchType);
		return success;
	}
	
	public void init(Node initNode) {
		queue.removeAll(queue);
		addedLast.removeAll(addedLast);
		queue.add(initNode);
		n = initNode;
		solution = null;
		done = false;
		success = false;
		exploredNodes = 0;
	}
	
	public void run( SearchType searchType ) {
		while ( !done ) {
			step(searchType);
		}
	}
	
	public void step( SearchType searchType ) {
//		if ( keepTimes ) StopWatch.start("[Search] Prune");
//		prune();
//		if ( keepTimes ) StopWatch.stop("[Search] Prune");
		
		if ( queue.isEmpty() ) {
			done = true;
		} else {
			if ( keepTimes ) StopWatch.start("[Search] Is goal?");
			exploredNodes++;
			n = queue.removeFirst();
				
			if ( isGoal(n) ) {
				success = true;
				done = true;
				solution = n;
			} 
			if ( keepTimes ) StopWatch.stop("[Search] Is goal?");
			
			if ( keepTimes ) StopWatch.start("[Search] Expand");
			addedLast = expand(n);
			if ( keepTimes ) StopWatch.stop("[Search] Expand");
			
			if ( keepTimes ) StopWatch.start("[Search] Add to queue");
			if ( searchType.equals(SearchType.DepthFirst ) ) {
				depthFirstExpand();
			} else if ( searchType.equals(SearchType.BreathFirst ) ) {
				breadthFirstExpand();
			} else if ( searchType.equals(SearchType.Heuristic ) ) {
				heuristicExpand();
			} else if ( searchType.equals(SearchType.HeuristicLayer ) ) {
				heuristicLayerExpand();
			}
			if ( keepTimes ) StopWatch.stop("[Search] Add to queue");
		}
	}
	
	public void prune() {
		this.prunedNodes += removeList.size();
		for ( Node n : removeList) {
			queue.remove(n);
		}
		removeList.removeAll(removeList);
	}
	
	public void undoLastExpand() {
		for ( Node n : addedLast ) {
			removeList.add(n);
		}
		addedLast.removeAll(addedLast);
	}
	
	public void depthFirstExpand() {
		for ( int i = addedLast.size()-1; i >= 0; i-- ) {
			queue.addFirst(addedLast.get(i));
		}
	}
	
	public void breadthFirstExpand() {
		for ( int i = 0; i < addedLast.size(); i++ ) {
			queue.add(addedLast.get(i));
		}
	}
	
	public void heuristicExpand() {
		for ( Node nNext : addedLast ) {
			if ( ! queue.isEmpty() ) {
				boolean added = false;
				for ( int k = 0 ; k < queue.size() ; k++ ) {
					if ( heuristic(queue.get(k)) > heuristic(nNext) ) {
						queue.add(k,nNext);
						added = true;
						break;
					}
				}			
				if ( ! added ) {
					queue.add(nNext);
				}
			} else {
				queue.add(nNext);
			}
		}
	}
	
	/**
	 * Heuristic only orders among same depth.
	 * Nodes with higher depth are always preferred over lower depth.
	 */
	public void heuristicLayerExpand() {
		for ( Node nNext : addedLast ) {
			if ( ! queue.isEmpty() ) {
				boolean added = false;
				for ( int k = 0 ; k < queue.size() ; k++ ) {
					if ( queue.get(k).depth() == nNext.depth() && heuristic(queue.get(k)) > heuristic(nNext) ) {
						queue.add(k,nNext);
						added = true;
						break;
					}
				}			
				if ( ! added ) {
					for ( int k = 0 ; k < queue.size() ; k++ ) {
						if ( queue.get(k).depth() < nNext.depth()  ) {
							queue.add(k,nNext);
							added = true;
							break;
						}
					}	
					if ( ! added ) {
						queue.add(nNext);
					}
				}
			} else {
				queue.add(nNext);
			}
		}
	}
	
	public String queueString() {
		if ( !queue.isEmpty() ) {
			String s = "["+queue.get(0).depth() + ":" + heuristic(queue.get(0));
			
			for ( int i = 1 ; i < queue.size() ; i++ ) {
				s += "," + queue.get(i).depth() + ":" + heuristic(queue.get(i));
			}
			return s +"]";
		} else {
			return "[-]";
		}
	}
}
