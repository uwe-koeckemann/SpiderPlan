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
 * Integer in a range between min and max.
 * @author Uwe Köckemann
 */
public class IntegerType extends Type { 
	/**
	 * Minimum allowed value
	 */
	public long min = 0;
	/**
	 * Maximum allowed value
	 */
	public long max = 1;
	
	@Override
	public boolean contains(Term s) {
		try {
			long v = Long.valueOf(s.toString()).longValue();
			return v <= max && v >= min;
		} catch ( NumberFormatException e ) {
			return false;
		}
		
	}

	@Override
	public String toString() {
		return "int " + name + "[" + min + ":" + max + "]";
	}

//	@Override
//	public ArrayList<String> getStringDomain() {
//		ArrayList<String> D = new ArrayList<String>((int) (this.max-this.min));
//		for ( long v = this.min ; v <=  this.max ; v++ ) {
//			D.add("" + v);
//		}
//		return D;
//	}

	@Override
	public ArrayList<Term> getDomain() {
		ArrayList<Term> D = new ArrayList<Term>((int) (this.max-this.min));
		for ( long v = this.min ; v <=  this.max ; v++ ) {
			D.add(Term.createInteger(v) ) ;
		}
		return D;
	}
}
