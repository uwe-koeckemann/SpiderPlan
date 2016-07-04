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
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;

/**
 * Representing simple distance constraints between start and end times of intervals.
 * These constraints can be propagated directly by an STP solver.
 * <p>
 * <b>Note:</b> Use this constraint in case something cannot be expressed by an {@link AllenConstraint}. 
 * 
 * @author Uwe Köckemann
 *
 */
public class SimpleDistanceConstraint extends Expression implements Matchable, Substitutable {	
	
	/**
	 * Indicates which timepoint of an interval is used
	 * 
	 * @author Uwe Köckemann
	 *
	 */
	public enum TimePoint { /**
	 * Use the start time of an interval.
	 */
	ST, /**
	 * Use the end time of an interval.
	 */
	ET }; 
	
	private Term from;
	private Term to;
	private TimePoint fromPoint;
	private TimePoint toPoint;
	private Interval bound;
	
	final private static Term Time0 = Term.createInteger(0);
	final private static Term TimeInf = Term.createConstant("inf");
	
	/**
	 * Create a {@link SimpleDistanceConstraint}.
	 * @param fromPoint 
	 * @param from First interval key {@link Term}
	 * @param toPoint 
	 * @param to Second interval key {@link Term}
	 * @param bound 
	 */
	public SimpleDistanceConstraint( TimePoint fromPoint, Term from, TimePoint toPoint, Term to, Interval bound ) {
		super(ExpressionTypes.Temporal);
		this.from = from;
		this.to = to;
		this.fromPoint = fromPoint;
		this.toPoint = toPoint;
		this.bound = bound;
	}
	
	/**
	 * Create simple distance constraint from atomic representation.
	 * <p>
	 * <b>Example atomic:</b> (distance (ST I1) (ET I2) [0 10])
	 * @param a the atomic
	 */
	public SimpleDistanceConstraint( Atomic a ) {
		super(ExpressionTypes.Temporal);
		this.from = a.getArg(0).getArg(0);
		this.to = a.getArg(1).getArg(0);
		this.fromPoint = TimePoint.valueOf(a.getArg(0).getName());
		this.toPoint = TimePoint.valueOf(a.getArg(1).getName());
		this.bound = new Interval(a.getArg(2));
	}
			
	/**
	 * Get first interval.
	 * @return term representing the interval
	 */
	public Term getFrom() { return from; };
	/**
	 * Get second interval.
	 * @return term representing the interval or <code>null</code> if constraint is unary
	 */
	public Term getTo() { return to; };
	
	/**
	 * Get time point of the first interval.
	 * @return whether start time or end time is used
	 */
	public TimePoint getFromPoint() { return fromPoint; };
	/**
	 * Get time point of the second interval.
	 * @return whether start time or end time is used
	 */
	public TimePoint getToPoint() { return toPoint; };
	
	/**
	 * Get the bound on the distance between the two timepoints. 
	 * 
	 * @return bound
	 */
	public Interval getBound() {
		return bound;
	}

	/**
	 * Variable terms in the bounds of this {@link SimpleDistanceConstraint}
	 * will be set to least constraining concrete values.
	 * Allows to perform reasoning on non-ground temporal constraints.
	 * This will be 0 or 1 for lower bounds (depending on the constraint)
	 * and inf for all upper bounds.
	 */
	public void setBoundToMostRelaxed() {
		bound.setLowerTerm(Time0);
		bound.setUpperTerm(TimeInf);
	}
		
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(from.getVariables());
		r.addAll(to.getVariables());
		r.addAll(bound.getLowerTerm().getVariables());
		r.addAll(bound.getUpperTerm().getVariables());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		if ( from.isGround() )
			r.add(from);
		if ( to.isGround() )
			r.add(to);
		if ( bound.getLowerTerm().isGround() )
			r.add(bound.getLowerTerm());
		if ( bound.getUpperTerm().isGround() )
			r.add(bound.getUpperTerm());
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		return r;		
	}
	
	@Override
	public boolean isGround() {
		if ( !bound.isGround() ) {
			return false;
		}
		return true;
	}

	@Override
	public Substitution match(Expression c) {
		if ( c instanceof SimpleDistanceConstraint ) {
			SimpleDistanceConstraint tc = (SimpleDistanceConstraint)c;
						
			Substitution theta = this.from.match(tc.from);
			
			if ( theta == null ) {
				return null; 
			}
			
			if ( !theta.add(this.to.match(tc.to)) ) {
				return null;
			}
			
			if ( !theta.add( this.bound.match(tc.bound) )) {
					return null;
			}
	
			return theta;
		}
		return null;
	}
	
	@Override
	public Expression substitute(Substitution theta) {
		return new SimpleDistanceConstraint(fromPoint, from.substitute(theta), toPoint, to.substitute(theta), this.bound.substitute(theta));
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof SimpleDistanceConstraint ) {
			SimpleDistanceConstraint tC = (SimpleDistanceConstraint)o;
			if ( !this.fromPoint.equals(tC.fromPoint) ) {
				return false;
			}
			if ( !this.toPoint.equals(tC.toPoint) ) {
				return false;
			}
			if ( !this.from.equals(tC.from) ) {
				return false;
			}
			if ( !this.to.equals(tC.to)) {
				return false;
			}
			if ( !this.bound.equals(tC.bound)) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
	
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		
		r.append("(distance (");
		r.append(fromPoint.toString());
		r.append(" ");
		r.append(from.toString());		
		r.append(") (");
		r.append(toPoint.toString());
		r.append(" ");
		r.append(to.toString());		
		r.append(") ");
		r.append(bound.toString());
		r.append(")");
		return r.toString();
	}
}
