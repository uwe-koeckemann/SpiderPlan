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
package org.spiderplan.search;

/**
 * Node for {@link MultiHeuristicSearch}
 * 
 * @author Uwe Köckemann
 *
 */
public abstract class MultiHeuristicNode extends AbstractNode { 

	private long[] h;
	private boolean overrideHeuristicValue = false;;
	
	/**
	 * Construct new node by providing the number of heuristics that it uses
	 * @param numHeuristics the number of heuristics
	 */
	public MultiHeuristicNode( int numHeuristics ) {
		h = new long[numHeuristics];
	}
		
	@Override
	public abstract int depth();

	@Override
	public abstract String toString();
	
	/**
	 * Assign a value to one of the heuristics
	 * @param i index of heuristic that is assigned
	 * @param v heuristic value
	 */
	public void setHeuristicValue( int i, long v ) {
		h[i] = v;
	}
	
	/**
	 * Get specific heuristic value.
	 * @param i index of heuristic
	 * @return value assigned to heuristic <code>i</code>
	 */
	public long getHeuristicValue( int i ) {
		if ( overrideHeuristicValue ) 
			return 0;
		else
			return h[i];
	}
	/**
	 * Get all heuristic values.
	 * @return array of heuristic values
	 */
	public long[] getHeuristicValues() {
		return h;
	}
}
