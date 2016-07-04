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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Global;

/**
 * Planning starts at t0 and any solution is restricted to 
 * a temporal horizon.
 * 
 * @author Uwe Köckemann
 *
 */
public class PlanningInterval extends Expression implements Mutable {
	
	private Term t0;
	private Term tHorizon;
	
	/**
	 * Create new planning interval from a complex term with the form <code>(interval l u)</code> or
	 * <code>[l u]</code>
	 * with lower bound <code>l</code> and upper bound <code>u</code>.
	 * @param interval term representing an interval
	 */
	public PlanningInterval( Term interval ) {
		super(ExpressionTypes.Temporal);
		if ( !interval.getName().equals("interval")) {
			throw new IllegalArgumentException(interval + " not supported. Term in this constructor must be interval(min,max). (The compiler also allows [min,max].)");
		}
		this.t0 = interval.getArg(0);
		this.tHorizon = interval.getArg(1);
	}	
	/**
	 * Create new planning interval constraint by providing terms for lower and upper bound.
	 * @param t0 lower earliest time relevant to planner
	 * @param tHorizon latest time relevant to planner
	 */
	public PlanningInterval( Term t0, Term tHorizon ) {
		super(ExpressionTypes.Temporal);
		this.t0 = t0;
		this.tHorizon = tHorizon;
	}
	
	/**
	 * Get start time of planning interval.
	 * @return term representing start time
	 */
	public Term getStartTime() { return t0; };
	/**
	 * Get horizon of planning interval.
	 * @return term representing the horizon
	 */
	public Term getHorizon()   { return tHorizon; };
	/**
	 * Get long value of horizon.
	 * @return long value of horizon
	 */
	public long getStartTimeValue() { 
		return Long.valueOf(t0.toString()); 
	};
	/**
	 * Get long value of start time.
	 * @return long value of start time
	 */
	public long getHorizonValue()   { 
		if ( tHorizon.toString().equals("inf")) {
			return Global.MaxTemporalHorizon;
		}
		return Long.valueOf( tHorizon.toString() ); 
	};
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		if ( t0.isVariable() )
			r.add(t0);
		if ( tHorizon.isVariable() ) 
			r.add(tHorizon);
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>(); 
		if ( t0.isGround() )
			r.add(t0);
		if ( tHorizon.isGround() ) 
			r.add(tHorizon);
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		return r;		
	}
	
	@Override
	public Expression copy() {
		Expression c = new PlanningInterval(t0, tHorizon);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof PlanningInterval ) {
			PlanningInterval pI = (PlanningInterval)o;
			return this.toString().equals(pI.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "( planning-interval [" + t0 + " " + tHorizon + "] )"; 
	}
	
	

}
