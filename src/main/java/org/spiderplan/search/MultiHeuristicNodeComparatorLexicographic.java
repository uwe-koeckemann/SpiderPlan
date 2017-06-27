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

import java.util.Comparator;

/**
 * Lexicographic comparator for nodes with multiple heuristics. 
 *
 * @author Uwe Köckemann
 *
 */
public class MultiHeuristicNodeComparatorLexicographic implements Comparator<MultiHeuristicNode> {
	
	@Override
	public int compare(MultiHeuristicNode arg0, MultiHeuristicNode arg1) {
		long[] h0 = arg0.getHeuristicValues();
		long[] h1 = arg1.getHeuristicValues();
		for ( int i = 0 ; i < h0.length ; i++ ) {
			if ( h0[i] < h1[i] ) {
				return -1;
			}
			if ( h0[i] > h1[i] ) {
				return 1;
			}
		}
		return 0;
	}

	
	
//	@Override
//	public int compareTo(MultiHeuristicNodeComparatorIndex arg0) {	
//		if ( compareMethod.equals(CompareMethod.Index)) {
//			
//		} else {

//		}
//	}
}
