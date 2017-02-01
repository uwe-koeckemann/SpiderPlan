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
package org.spiderplan.representation.types;

import java.util.ArrayList;

import org.spiderplan.representation.logic.Term;

 
/**
 * Double in a range between min and max.
 * @author Uwe Köckemann
 */
public class FloatType extends Type { 
	/**
	 * Minimum allowed value
	 */
	public double min = 0.0;
	/**
	 * Maximum allowed value
	 */
	public double max = 1.0;
	
	@Override
	public boolean contains(Term s) {
		try {
			double v = Double.valueOf(s.toString()).doubleValue();
			return v <= max && v >= min;
		} catch ( NumberFormatException e ) {
			return false;
		}
		
	}

	@Override
	public String toString() {
		return "float " + name + "["+min+":"+max+"]";
	}

	@Override
	public ArrayList<Term> getDomain() {
		return new ArrayList<Term>();
	}
}
