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
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


public class ReusableResourceCapacity extends Constraint implements Mutable {
		
	private Atomic variable;
	private int capacity;
	
	public ReusableResourceCapacity( Atomic variable, int capacity ) {
		super(ConstraintTypes.Resource);
		this.variable = variable;
		this.capacity = capacity;
	}
	
	public boolean applicable( Atomic a ) {
		return variable.match(a) != null;
	}
	
	public Atomic getVariable() { return variable; }
	public int getCapacity()    { return capacity; }

	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(variable.getVariableTerms());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(variable.getGroundTerms());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		r.add(this.variable);
		return r;		
	}
	
	@Override
	public Constraint copy() {
		Constraint c = new ReusableResourceCapacity(variable, capacity);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof ReusableResourceCapacity ) {
			ReusableResourceCapacity rrCap = (ReusableResourceCapacity)o;
			return this.variable.equals(rrCap.variable) && this.capacity == rrCap.capacity;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "( reusable " + variable + "  " + capacity + " )";
	}
	
}
