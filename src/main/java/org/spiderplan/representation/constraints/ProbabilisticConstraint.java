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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Cost constraints either add or subtract a value from a cost variable
 * or pose inequalities on a cost variable.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class ProbabilisticConstraint extends Constraint implements Substitutable, Mutable {
	
	Atomic rel;
		
	public ProbabilisticConstraint( Atomic probabilisticConstraint ) {
		super(ConstraintTypes.Cost);
		ConstraintTypes.ProbabilisticConstraints.assertSupported(probabilisticConstraint, this.getClass().getSimpleName());
		this.rel = probabilisticConstraint;
	}

	public ProbabilisticConstraint( ProbabilisticConstraint c ) {	
		super(ConstraintTypes.Cost);	
		this.rel = c.rel;
	}
	
	public Atomic getRelation() {
		return rel;
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(rel.getVariableTerms());
		return r;
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(rel.getGroundTerms());
		return r;
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		return new HashSet<Atomic>();
	}

	@Override
	public ProbabilisticConstraint copy() {
		ProbabilisticConstraint c = new ProbabilisticConstraint(this);
		return c;
	}

	@Override
	public Constraint substitute(Substitution theta) {
		rel = rel.substitute(theta);
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ProbabilisticConstraint ) {	
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
		return rel.toString();
	}
}
