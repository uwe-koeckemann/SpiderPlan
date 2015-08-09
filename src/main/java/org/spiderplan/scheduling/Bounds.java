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

/**
 * Note: Code from the metacsp project (http://metacsp.org/) 
 */
public class Bounds {

	public long min;
	public long max;
	
	public Bounds(long min, long max) {
		this.min = min;
		this.max = max;
		
		if(min > max) {
			throw new IllegalArgumentException(String.format("Invalid arguments, min > max, : (%d > %d)", min, max));
		}
	}
				
	/**
	 * Get intersection with another {@link Bounds}.
	 * This version treats intervals [n,n] as empty, which is necessary for
	 * intersections in scheduling where "a meets b" should not create a conflict.
	 * @param b The {@link Bounds} object to intersect this with.
	 * @return <code>true</code> is there is a non-empty intersection, <code>null</code> otherwise.
	 */
	public final Bounds intersectStrict(Bounds b) {
		final long _min = Math.max(this.min, b.min);
		final long _max = Math.min(this.max, b.max);
		if(_min < _max) return new Bounds(_min, _max);
		return null;
	}
	
	@Override
	public String toString() {
		return "[" + min + " " + max + "]";
	}
}
