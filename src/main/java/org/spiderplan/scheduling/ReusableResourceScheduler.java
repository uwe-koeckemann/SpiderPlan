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
package org.spiderplan.scheduling;

import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.logic.Term;


/**
 * Implements a scheduler for reusable resources.
 * @author Uwe Köckemann
 */
public class ReusableResourceScheduler extends Scheduler {
	
	private int capacity;
	
	/**
	 * 
	 * @param resourceVariable
	 * @param capacity
	 */
	public ReusableResourceScheduler( Term resourceVariable, int capacity ) {
		super(resourceVariable);
		this.capacity = capacity;		
		
		if ( capacity > 1 ) {
			super.strategy = PeakCollectionStrategy.SamplingPeakCollection;
		} else {
			super.strategy = PeakCollectionStrategy.BinaryPeakCollection;
		}
	}

	@Override
	protected boolean isConflicting( Statement[] peak ) {
		int sum = 0;
		for ( Statement val : peak ) {
			sum += Integer.parseInt(val.getValue().toString());
			if (sum > capacity) return true;
		}
		return false;
	}
	
	
}
