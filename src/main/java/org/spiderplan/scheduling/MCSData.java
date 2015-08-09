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
package org.spiderplan.scheduling;

import org.spiderplan.representation.logic.Term;

/**
 * Note: Code from the metacsp project (http://metacsp.org/) 
 */
public class MCSData implements Comparable<MCSData> {

	/**
	 * The value of k for the MCS associated to this {@link MCSData} object. 
	 */
	public float mcsK;
	
	/**
	 * The maximum FLEX(i,j) in the MCS associated to this {@link MCSData} object.
	 */
	public float mcsPcMin;
	
	/**
	 * The source {@link Term} with which {@link #mcsPcMin} is obtained. 
	 */
	public Term mcsActFrom;
	
	/**
	 * The destination {@link Term} with which {@link #mcsPcMin} is obtained.
	 */
	public Term mcsActTo;

	/**
	 * Create a new {@link MCSData} object.
	 * @param pcmin The lowest FLEX(i,j) in the MCS associated to this {@link MCSData} object.
	 * @param actFrom The i-th {@link Activity}.
	 * @param actTo The j-th {@link Activity}.
	 * @param k The value of k of the MCS associated to this {@link MCSData} object.
	 */
	public MCSData(float pcmin, Term actFrom, Term actTo, float k) {
		mcsK = k;
		mcsActFrom = actFrom;
		mcsActTo = actTo;
		mcsPcMin = pcmin;
	}

	/**
	 * Compare this {@link MCSData} object with a reference {@link MCSData} object.
	 * @param o The reference {@link MCSData} object
	 * @return {@code -1} if the value of k for the MCS associated to this {@link MCSData} object is less than that of
	 * the MCS associated to the reference {@link MCSData} object;  {@code 0} if the value of k for the MCS associated to this
	 * {@link MCSData} object is equal to that of the MCS associated to the reference {@link MCSData} object; {@code 1}
	 * otherwise.
	 */
	@Override
	public int compareTo(MCSData o) {
		if (mcsK < o.mcsK)
			return 1;
		if (mcsK == o.mcsK)
			return 0;
		return -1;
	}

	/**
	 * Get a String representation of this {@link MCSData} object.
	 * @return A String describing this {@link MCSData} object.
	 */
	@Override
	public String toString() {
		String ret = "[K = " + mcsK + ", pcMin = " + mcsPcMin + ", ActFrom = " + mcsActFrom + ", ActTo = " + mcsActTo + "]";
		return ret;
	}

}
