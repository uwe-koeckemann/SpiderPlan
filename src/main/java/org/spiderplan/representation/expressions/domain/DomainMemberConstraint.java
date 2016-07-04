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
package org.spiderplan.representation.expressions.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.DomainRelation;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;

/**
 * TODO: sort out domain constraint mess.
 * 
 * @author Uwe Köckemann
 */
public class DomainMemberConstraint extends Expression implements Matchable, Substitutable {
		
	private Atomic r;
	private DomainRelation relation;

	/**
	 * Create a new {@link DomainMemberConstraint} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 */
	public DomainMemberConstraint( Atomic l ) {
		super(ExpressionTypes.Domain);
		relation = ExpressionTypes.DomainConstraints.assertSupported(l, this.getClass());
		r = l;
	}
	
	/**
	 * Get constraint as an atomic.
	 * @return the constraint
	 */
	public Atomic getConstraint() {
		return r;
	}
	
	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public DomainRelation getRelation() {
		return relation;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(this.r.getVariableTerms());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.r.getGroundTerms());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		return r;		
	}
		
	@Override
	public String toString() {
		return r.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new DomainMemberConstraint(r.substitute(theta));
	}
	
	@Override
	public boolean isGround() {
		return r.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof DomainMemberConstraint ) {
			DomainMemberConstraint rC = (DomainMemberConstraint)c;
			return this.getConstraint().match(rC.getConstraint());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof DomainMemberConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
