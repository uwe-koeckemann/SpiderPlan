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
import java.util.Set;

import org.spiderplan.representation.constraints.constraintInterfaces.Assertable;
import org.spiderplan.representation.constraints.constraintInterfaces.Matchable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


public class PrologConstraint extends Constraint implements Matchable, Substitutable, Mutable, Assertable {
	
	private Atomic r;
	private Term programID;
	private boolean isAsserted = false;
	
	public PrologConstraint( PrologConstraint rC ) {	
		super(ConstraintTypes.Prolog);
		this.r = rC.r;
		this.programID = rC.programID;
		this.setAsserted(rC.isAsserted());
	}
	
	public PrologConstraint( Atomic l, Term programID ) {
		super(ConstraintTypes.Prolog);
		this.programID = programID;
		this.r = l;
	}
	
	public Atomic getRelation() {
		return r;
	}
	
	public Term getProgramID() {
		return programID;
	}
	
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
	public Constraint substitute(Substitution theta) {
		r = r.substitute(theta);
		programID = programID.substitute(theta);
		return this;
	}
	
	@Override
	public PrologConstraint copy() {
		PrologConstraint c = new PrologConstraint(this);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}
	
	@Override
	public Substitution match( Constraint c ) {
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
	public Constraint setAsserted(boolean asserted) {
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
