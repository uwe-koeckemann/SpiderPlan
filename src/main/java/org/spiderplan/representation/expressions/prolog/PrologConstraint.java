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
package org.spiderplan.representation.expressions.prolog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Prolog constraint that needs to be satisfied by a Prolog program with the corresponding ID.
 * Program IDs are used to allow multiple separate Prolog programs.
 * 
 * @author Uwe Köckemann
 */
public class PrologConstraint extends Expression implements Matchable, Substitutable, Mutable, Assertable {
	
	private Atomic r;
	private Term programID;
	private boolean isAsserted = false;
		
	/**
	 * Create a new Prolog constraint for a given relation and a 
	 * program ID
	 * @param l the relation
	 * @param programID the program under which the relation is evaluated
	 */
	public PrologConstraint( Atomic l, Term programID ) {
		super(ExpressionTypes.Prolog);
		this.programID = programID;
		this.r = l;
	}
	
	/**
	 * Get the relation-
	 * @return the relation
	 */
	public Atomic getRelation() {
		return r;
	}
	
	/**
	 * Get the program ID.
	 * @return the program ID
	 */
	public Term getProgramID() {
		return programID;
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
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
		r.add(this.r);
		return r;		
	}
	
	@Override
	public String toString() {
		return r.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		programID = programID.substitute(theta);
		return this;
	}
	
	@Override
	public PrologConstraint copy() {
		PrologConstraint c = new PrologConstraint(this.r, this.programID);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof PrologConstraint ) {
			PrologConstraint rC = (PrologConstraint)c;
			return this.getRelation().match(rC.getRelation());
		}
		return null;
	}
	
	@Override
	public boolean isAsserted() {
		return isAsserted;
	}
	@Override
	public Expression setAsserted(boolean asserted) {
		isAsserted = asserted;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof PrologConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.r.hashCode();
	}	
}
