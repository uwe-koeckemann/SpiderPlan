/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.representation.expressions.domain;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;


/**
 * Associate a domain to a type.
 * <p>
 * Supported domain classes are:
 * <ul>
 * <li> enum
 * <li> int
 * <li> float
 * </ul>
 *
 * 
 * @author Uwe Köckemann
 */
public class TypeDomainConstraint extends Expression implements Matchable, Substitutable, Mutable {
		
	private Term r;
	
	/**
	 * Create copy of {@link TypeDomainConstraint} gC.
	 * @param gC a {@link TypeDomainConstraint}
	 */
	public TypeDomainConstraint( TypeDomainConstraint gC ) {		
		super(ExpressionTypes.Domain);
		r = gC.r;
	}
	/**
	 * Create a new {@link TypeDomainConstraint} based on {@link Term} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Term}
	 */
	public TypeDomainConstraint( Term l ) {
		super(ExpressionTypes.Domain);
		this.r = l;
	}
	
	/**
	 * Get the relation.
	 * @return the constraint
	 */
	public Term getRelation() {
		return r;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		Set<Term> r = new HashSet<Term>(); 
//		r.addAll(this.r.getVariableTerms());
//		return r;
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(this.r.getGroundTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Term> getComplexTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
////		r.add(this.r);
//		return r;		
//	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.r.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public String toString() {
		return r.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		return this;
	}
	
	@Override
	public TypeDomainConstraint copy() {
		TypeDomainConstraint c = new TypeDomainConstraint(this);
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}

//	@Override
//	public Collection<Term> getTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( Term t : this.r.getTerms() ) {
//			r.add(t);
//		}
//		return r;		
//	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof TypeDomainConstraint ) {
			TypeDomainConstraint rC = (TypeDomainConstraint)c;
			return this.getRelation().match(rC.getRelation());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof TypeDomainConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
