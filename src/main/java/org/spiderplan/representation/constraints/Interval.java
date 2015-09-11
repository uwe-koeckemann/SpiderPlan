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
package org.spiderplan.representation.constraints;

import org.spiderplan.representation.logic.IntegerTerm;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Global;

public class Interval {
	private Term min;
	private Term max;
	
	private static Term infTerm = Term.createConstant("inf");
	
	public Interval(Term intTerm ) {
		if ( !intTerm.getName().equals("interval")) {
			throw new IllegalArgumentException(intTerm + " not supported. Term in this constructor must be interval(min,max). (The compiler also allows [min,max].)");
		}
		this.min = intTerm.getArg(0);
		this.max = intTerm.getArg(1);
	}
	
	public Interval(Term lowerBound, Term upperBound ) {
		this.min = lowerBound;
		this.max = upperBound;
	}
	
	public Interval(long lowerBound, long upperBound ) {
		this.min = Term.createInteger(lowerBound);
		this.max = Term.createInteger(upperBound);
	}
	
	public Interval(String s) {
		String tmp[] = s.trim().replace("[","").replace("]", "").split(",");
	
		min = Term.parse(tmp[0]);
		max = Term.parse(tmp[1]);	
	}
	
	public Interval(Interval i) {
		this.min = i.min;
		this.max = i.max;
	}
	
	
	public long getMin() {
		if ( min.equals(infTerm) )
			return Global.MaxTemporalHorizon;
		else if ( min.isVariable()  ) 
			return 0;
		else
			return ((IntegerTerm)min).getValue();
	} 
	
	public long getMax() {
		if ( max.equals(infTerm) )
			return Global.MaxTemporalHorizon;
		else if ( min.isVariable()  ) 
			return Global.MaxTemporalHorizon;
		else
			return ((IntegerTerm)max).getValue();
	}
	
	public Term getMinTerm() {
		return min;		
	}
	public Term getMaxTerm() {
		return max;		
	}
	
	public void setMinTerm( Term min ) {
		this.min = min;		
	}
	public void setMaxTerm( Term max ) {
		this.max = max;		
	}
	
	public boolean isGround() {
		return min.isGround() && max.isGround();
	}
	
	@Override
	public String toString() {
		return "[" + this.min.toString() + " " + this.max.toString() + "]";
	}

	public Interval substitute(Substitution theta) {
		return new Interval(min.substitute(theta), max.substitute(theta));
	}
		
	public Substitution match( Interval i ) {
		Substitution theta = this.min.match(i.getMinTerm());
		if ( theta == null ) {
			return null;
		}
		
		if ( !theta.add(this.max.match(i.getMaxTerm()))) {
			return null;
		}
		return theta;
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof Interval ) {
			Interval i = (Interval)o;
			return i.min.equals(this.min)&& i.max.equals(this.max); 
		}
		return false;
	}
}
