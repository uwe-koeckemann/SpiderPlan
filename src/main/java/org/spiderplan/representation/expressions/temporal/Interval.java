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
package org.spiderplan.representation.expressions.temporal;

import org.spiderplan.representation.logic.IntegerTerm;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Global;

/**
 * Represents interval bounds.
 *  
 * @author Uwe Köckemann
 */
public class Interval {
	private Term min;
	private Term max;
	
	private static Term infTerm = Term.createConstant("inf");
	
	/**
	 * Create new interval based on an interval term.
	 * <p>
	 * Example:
	 * <ul>
	 * <li> (interval 0 10)
	 * </ul>
	 * 
	 * 
	 * @param intTerm
	 */
	public Interval(Term intTerm ) {
		if ( !intTerm.getName().equals("interval")) {
			throw new IllegalArgumentException(intTerm + " not supported. Term in this constructor must be interval(min,max). (The compiler also allows [min,max].)");
		}
		this.min = intTerm.getArg(0);
		this.max = intTerm.getArg(1);
	}
	
	/**
	 * Create interval with terms for lower and upper bound.
	 * @param lowerBound the lower bound
	 * @param upperBound the upper bound
	 */
	public Interval(Term lowerBound, Term upperBound ) {
		this.min = lowerBound;
		this.max = upperBound;
	}
	
	/**
	 * Create term from long values.
	 * @param lowerBound the lower bound
	 * @param upperBound the upper bound
	 */
	public Interval(long lowerBound, long upperBound ) {
		this.min = Term.createInteger(lowerBound);
		this.max = Term.createInteger(upperBound);
	}
	
	
	/**
	 * Parse interval from string. 
	 * (Should be avoided.)
	 * <p>
	 * Example input string:
	 * <ul>
	 * <li> [0 10]
	 * </ul>
	 * 
	 * @param s string representation of interval
	 */
	public Interval(String s) {
		String tmp[] = s.trim().replace("[","").replace("]", "").split(",");
	
		min = Term.parse(tmp[0]);
		max = Term.parse(tmp[1]);	
	}
	
	/**
	 * Get lower bound.
	 * @return the lower bound
	 */
	public long getLower() {
		if ( min.equals(infTerm) )
			return Global.MaxTemporalHorizon;
		else if ( min.isVariable()  ) 
			return 0;
		else 
			return ((IntegerTerm)min).getValue();
	} 
	
	/**
	 * Get upper bound.
	 * @return the upper bound
	 */
	public long getUpper() {
		if ( max.equals(infTerm) )
			return Global.MaxTemporalHorizon;
		else if ( min.isVariable()  ) 
			return Global.MaxTemporalHorizon;
		else
			return ((IntegerTerm)max).getValue();
	}
	
	/**
	 * Get lower bound term.
	 * @return the lower bound
	 */
	public Term getLowerTerm() {
		return min;		
	}
	/**
	 * Get upper bound term.
	 * @return the upper bound
	 */
	public Term getUpperTerm() {
		return max;		
	}
	
	/**
	 * Set lower bound.
	 * @param lower lower bound term
	 */
	public void setLowerTerm( Term lower ) {
		this.min = lower;		
	}
	/**
	 * Set upper bound.
	 * @param upper upper bound term
	 */
	public void setUpperTerm( Term upper ) {
		this.max = upper;		
	}
	
	/**
	 * Test if upper and lower bound are ground terms.
	 * @return <code>true</code> if both upper and lower bound are ground, <code>false</code> otherwise
	 */
	public boolean isGround() {
		return min.isGround() && max.isGround();
	}
	
	@Override
	public String toString() {
		return "[" + this.min.toString() + " " + this.max.toString() + "]";
	}

	/**
	 * Perform substitution on bounds.
	 * @param theta the substitution
	 * @return result of applying the substitution
	 */
	public Interval substitute(Substitution theta) {
		return new Interval(min.substitute(theta), max.substitute(theta));
	}
		
	/**
	 * Match this interval to another interval.
	 * @param i interval to match this one to
	 * @return substitution that makes both intervals equal or <code>null</code> if no such substitution exists
	 */
	public Substitution match( Interval i ) {
		Substitution theta = this.min.match(i.getLowerTerm());
		if ( theta == null ) {
			return null;
		}
		
		if ( !theta.add(this.max.match(i.getUpperTerm()))) {
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
