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
package org.spiderplan.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows to input a list of a list of choices (instances of class T) and returns a list of 
 * all possible combinations of these choices.
 * 
 * @author Uwe Köckemann
 *
 * @param <T>
 */
public class GenericComboBuilder<T> {
	private int [] slotSizes;
	private int [] slots; 
	private boolean carry = false;
	private boolean done = false;
	
	/**
	 * Returns all possible combinations when choosing one value from each provided list.
	 * @param in a list of lists to be combined
	 * @return list of combinations
	 */
	public List<List<T>> getCombos(List<List<T>> in) {
		List<List<T>> out = new ArrayList<List<T>>();
		
		/**
		 * Return when missing choices
		 */
		if ( in.isEmpty() ) {
			return out;
		}
		for ( List<T> choices : in ) {
			if ( choices.isEmpty() ) {
				return out;
			}
		}
		
		this.slots = new int[in.size()];
		this.slotSizes = new int[in.size()];
		for ( int i = 0 ; i < in.size() ; i++ ) {
			slotSizes[i] = in.get(i).size();
		}
		
		while ( ! this.done ) {
			List<T> combo = new ArrayList<T>(slots.length);
			
			for ( int i = 0 ; i < slots.length ; i++ ) {
				combo.add(in.get(i).get(slots[i]));
			}
			out.add(combo);
			this.iterate();
		}
		
		return out;
	}

//	public List<List<T>> getCombosSingleList(List<T> in, int n,  boolean allowDuplicates ) {
//		List<List<T>> out = new ArrayList<List<T>>();
//		
//		this.slots = new int[n];
//		this.slotSizes = new int[n];
//		for ( int i = 0 ; i < n ; i++ ) {
//			slotSizes[i] = in.size();
//		}
//		
//		while ( ! this.done ) {	
//			if ( allowDuplicates  ||  !containsDuplicates(slots, in.size()) ) {
//			
//				ArrayList<T> combo = new ArrayList<T>(slots.length);
//			
//				for ( int i = 0 ; i < slots.length ; i++ ) {
//					combo.add(in.get(slots[i]));
//				}
//				out.add(combo);
//			}
//			this.iterate();
//			
//		}
//		
//		return out;
//	}
	
	private void iterate() {
		
		if ( done ) {	// reinit
			done = false;
			carry = false;
			for  ( int i = 0 ; i < slots.length; i++ ) {
				slots[i] = 0;
			}
		}
		
		carry = false;
		
		slots[0]++;
		
		for ( int i = 0 ; i < slots.length; i++ ) {
			if ( carry ) {
				slots[i]++;
				carry = false;
			}
			
			if ( slots[i] == slotSizes[i] ) {
				slots[i] = 0;
				carry = true;
			} 
		}
		
		if ( carry ) {
			done = true;
		} 
		
	}
	
//	private boolean containsDuplicates( int[] slots, int n ) {
//		int[] check = new int[n];
//		
//		for ( int s : slots ) {
//			check[s]++;
//			if ( check[s] > 1 ) {
//				return true;
//			}
//		}
//		return false;
//	}
}
