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

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Lets causal reasoner know that a variable has changed its value (due to future event).
 * 
 */
public class StateVariableOverride extends Constraint implements Substitutable, Mutable {
	
	final private static Term ConstraintType = Term.createConstant("state-variable-override");
	
	private Statement from, to;
	
	public StateVariableOverride( Statement from, Statement to ) {
		super(ConstraintType);
		this.from = from;
		this.to = to;
	}
	
	public StateVariableOverride( StateVariableOverride g ) {		
		super(ConstraintType);
		this.from = g.from;
		this.to = g.to;
	}
	
	public Statement getFrom() {
		return from;
	}
	
	public Statement getTo() {
		return to;
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(from.getVariableTerms());
		r.addAll(to.getVariableTerms());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(from.getGroundTerms());
		r.addAll(to.getGroundTerms());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		r.add(from.getVariable());
		r.add(to.getVariable());
		return r;		
	}
		
	
	@Override
	public String toString() {
		return from + " -> " + to;
	}

	@Override
	public Constraint substitute(Substitution theta) {
		from = from.substitute(theta);
		to = to.substitute(theta);
		return this;
	}
	
	@Override
	public StateVariableOverride copy() {
		StateVariableOverride c = new StateVariableOverride(this);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof StateVariableOverride ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
