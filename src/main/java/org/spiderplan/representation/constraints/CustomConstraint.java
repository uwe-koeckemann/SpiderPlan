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


/**
 * For relations of custom classes with their own reasoning modules.
 * 
 * Examples: spatial relations are not part of the language but can be added
 * by defining custom variables, such as, "custom:spatial:rel overlapping(A,B)" in
 * the domain file. 
 * 
 * If custom relations are added as constraints (e.g. in operators) they will use 
 * CustomRelationalConstraint rather than RelationalConstraint.
 * 
 * Very similar to RelationalConstraint, except that it expects the name of the custom
 * class in the constructors.
 * 
 * Another difference is the ID that is used to keep all CustomRelationalConstraints
 * unique, because sometimes we have to accumulate them (e.g. in case of scores).
 * 
 */
public class CustomConstraint extends Constraint implements Matchable, Substitutable, Mutable, Assertable {
	
	final private static Term ConstraintType = Term.createConstant("custom");
	
	private Atomic r;
	private Atomic customClass;
	private boolean isAsserted = false;
	
	private static int nextID = 0;
	private int ID;

	public CustomConstraint( Atomic relation, Atomic customClass ) {
		super(ConstraintType);
		this.r = relation;
		this.customClass = customClass;
		ID = nextID++;
	}
	
	public CustomConstraint(CustomConstraint rC ) {		
		super(ConstraintType);
		this.r = rC.r;
		this.customClass = rC.customClass;
		this.setAsserted(rC.isAsserted());
		ID = nextID++;
	}
	
	public Atomic getAtomic() {
		return r;
	}
	
	public Atomic getCustomClass() {
		return customClass;
	}

	@Override
	public String toString() {
		return "["+this.getCustomClass()+"] " + r.toString();
	}

	@Override
	public Constraint substitute(Substitution theta) {
		r = r.substitute(theta);
		return this;
	}
	
	@Override
	public CustomConstraint copy() {
		CustomConstraint c = new CustomConstraint(this);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}

	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.r.getVariableTerms());
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
//		for ( Term t : this.r.getArgs() ) {
//			if ( t.isGround() ) {
//				r.add(t);
//			}
//		}
		r.addAll(this.r.getGroundTerms());
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		r.add(this.r);
		return r;
	}
	
	@Override
	public Substitution match( Constraint c ) {
		if ( c instanceof CustomConstraint ) {
			CustomConstraint rC = (CustomConstraint)c;
			return this.getAtomic().match(rC.getAtomic());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof CustomConstraint ) {
			return this.toString().equals(o.toString()) && this.ID == ((CustomConstraint)o).ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode() + ID;
	}

	@Override
	public boolean isAsserted() {
		return isAsserted;
	}

	@Override
	public Constraint setAsserted(boolean asserted) {
		this.isAsserted = asserted;
		return this;
	}	
}
