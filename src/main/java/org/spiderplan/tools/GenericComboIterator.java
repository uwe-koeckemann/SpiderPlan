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
import java.util.Iterator;
import java.util.List;

/**
 * Provides an {@link Iterator} over a combination of choices of class T.
 * <p>
 * This is a memory friendly alternative to {@link GenericComboBuilder} which builds
 * all combinations at once.
 * 
 * @author Uwe Köckemann
 *
 * @param <T>
 */
public class GenericComboIterator<T> implements Iterator<List<T>>, Iterable<List<T>> {
	private int [] slotSizes;
	private int [] slots; 
	private boolean carry = false;
	private boolean done = false;
	
	List<List<T>> in;

	/**
	 * Create iterator by providing choices. 
	 * @param toCombine list of lists of choices
	 */
	public GenericComboIterator( List<List<T>> toCombine ) {
		this.in = toCombine;
		
		this.slots = new int[this.in.size()];
		this.slotSizes = new int[this.in.size()];
		for ( int i = 0 ; i < this.in.size() ; i++ ) {
			slotSizes[i] = this.in.get(i).size();
		}
		
		if ( this.in.isEmpty() ) {
			done = true;
			return;
		}
		for ( List<T> choices : this.in ) {
			if ( choices.isEmpty() ) {
				done = true;
				return;
			}
		}
		
		this.slots = new int[this.in.size()];
		this.slotSizes = new int[this.in.size()];
		for ( int i = 0 ; i < this.in.size() ; i++ ) {
			slotSizes[i] = this.in.get(i).size();
		}
	}
	
	/**
	 * Calculates and returns the number of combinations of the input choices.
	 * @return the number of combinations
	 */
	public int getNumCombos() {
		int n = 1;
		for ( int i = 0 ; i < slotSizes.length ; i++ ) {
			n *= slotSizes[i];
		}
		return n;
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public List<T> next() {
		List<T> combo = new ArrayList<T>(slots.length);
		for ( int i = 0 ; i < slots.length ; i++ ) {
			combo.add(in.get(i).get(slots[i]));
		}
		this.iterate();
		return combo;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Setup next combination
	 */
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

	@Override
	public Iterator<List<T>> iterator() {
		return this;
	}
}
