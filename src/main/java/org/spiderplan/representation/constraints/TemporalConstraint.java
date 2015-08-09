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

import java.util.Collection;
import java.util.HashSet;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Miscellaneous temporal constraints.
 * 
 * @author Uwe Köckemann
 * 
 */
public class TemporalConstraint extends Constraint implements Substitutable, Mutable {
			
	private Atomic a;
	public Relation relation;
	
	/**
	 * Allowed relations of {@link TemporalConstraint}s
	 * 
	 * @author Uwe Köckemann
	 *
	 */
	public enum Relation { Draw, Makespan, PossibleIntersection };

	public TemporalConstraint( Atomic a ) {
		super(ConstraintTypes.Temporal);
		this.a = a;
		this.relation = Relation.valueOf(a.name());
	}
	
	public TemporalConstraint( TemporalConstraint c ) {	
		super(ConstraintTypes.Temporal);
		this.a = c.a;
		this.relation = c.relation;
	}
	
	public Atomic getAtomic() {
		return a;
	}
	
	public Relation getRelation() {
		return relation;
	}

	
	@Override
	public Collection<Term> getVariableTerms() {
		return a.getVariableTerms();
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		return this.a.getGroundTerms();
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		return new HashSet<Atomic>();
	}

	@Override
	public TemporalConstraint copy() {
		TemporalConstraint c = new TemporalConstraint(this);
		return c;
	}

	@Override
	public Constraint substitute(Substitution theta) {
		this.a = this.a.substitute(theta);
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof TemporalConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return a.toString();
	}
}
