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

public abstract class MultiHeuristicNode extends Node implements Comparable<MultiHeuristicNode> {

	private long[] h;
	private boolean forceExploration = false;
	private boolean overrideHeuristicValue = false;;
	
	public MultiHeuristicNode( int numHeuristics ) {
		h = new long[numHeuristics];
	}
	
	public enum CompareMethod { Index, Lexicographic };
	public CompareMethod compareMethod = CompareMethod.Index;
	
	private static int compareToIdx = 0; 
	
	@Override
	public abstract int depth();

	@Override
	public abstract String toString();
	
	public long setHeuristicValue( int i, long v ) {
		return h[i] = v;
	}
	
	public long getHeuristicValue( int i ) {
		if ( overrideHeuristicValue ) 
			return 0;
		else
			return h[i];
	}
	public long[] getHeuristicValues() {
		return h;
	}
	
	public void setOverrideHeuristicValue( boolean val ) {
		this.overrideHeuristicValue = val;
	}
	
	
	
	/**
	 * Whether or not visited nodes are
	 * considered before exploring this node.
	 * @return
	 */
	public boolean forceExploration() {
		return forceExploration;
	}
	
	/**
	 * True will fore node to be explored 
	 * even if it is considered equal to a 
	 * visited node.
	 * @param val 
	 */
	public void setForceExploration( boolean val ) {
		this.forceExploration = val;
	}
	
	public long getCompareValue( ) {
		return h[compareToIdx];
	}
	
//	public void setCompareIdx( int i ) {
//		if ( i >= 0 && i < h.length ) {
//			compareToIdx = i;	
//		} else {
//			throw new IllegalArgumentException("Compare index out bounds (index: "+i+" max: "+h.length+").");
//		}
//	}
	
	public static void setCompareIdx( int i ) {
		compareToIdx = i;
	}

//	@Override
//	public int compareTo(MultiHeuristicNode arg0) {	
//		if ( compareMethod.equals(CompareMethod.Index)) {
//			return (int)((this.getCompareValue()+this.depth() - arg0.getCompareValue()+arg0.depth() ));
//		} else {
//			for ( int i = 0 ; i < h.length ; i++ ) {
//				if ( this.h[i]+this.depth() < arg0.h[i]+arg0.depth() ) {
//					return -1;
//				}
//				if ( this.h[i]+this.depth() > arg0.h[i]+arg0.depth() ) {
//					return 1;
//				}
//			}
//			return 0;
//		}
//	}
	
	@Override
	public int compareTo(MultiHeuristicNode arg0) {	
		if ( compareMethod.equals(CompareMethod.Index)) {
			return (int)((this.getCompareValue() - arg0.getCompareValue()));
		} else {
			for ( int i = 0 ; i < h.length ; i++ ) {
				if ( this.h[i] < arg0.h[i] ) {
					return -1;
				}
				if ( this.h[i] > arg0.h[i] ) {
					return 1;
				}
			}
			return 0;
		}
	}
}
