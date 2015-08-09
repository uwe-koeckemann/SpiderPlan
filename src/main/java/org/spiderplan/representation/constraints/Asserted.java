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

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.constraintInterfaces.Assertable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Just marks another constraint as asserted.
 * 
 * @author Uwe Köckemann
 */
public class Asserted extends Constraint implements Mutable, Assertable {

	final private static Term ConstraintType = Term.createConstant("assertion");
	
	final private String classString;
	final private int constraintHash;
	private boolean isAsserted = false;
	
//	private Constraint c;
	
	private Asserted( String classString, int constraintHash ) {
		super(ConstraintType);
		this.classString = classString;
		this.constraintHash = constraintHash;	
	}
	
	/**
	 * Set a constraint to asserted.
	 * @param c The constraint that is asserted.
	 */
	public Asserted( Constraint c ) {
		super(ConstraintType);
		classString = c.getClass().getSimpleName();
		constraintHash = c.hashCode();
		
//		this.c = c.copy();
	}
	
	/**
	 * Apply assertion by finding its attached constraint 
	 * and setting it to asserted. 
	 * @param cDB The {@link ConstraintDatabase} to which to apply
	 * the assertion.
	 */
//	public void apply( ConstraintDatabase cDB ) {
//		for ( Constraint c : cDB.getConstraints() ) {
//	if ( c instanceof Assertable ) {
//	if ( c.getClass().getSimpleName().equals(classString) && c.hashCode() == constraintHash ) {
//		c.setAsserted(true);
//	} 
//}
//		}
//	}
	
	public boolean appliesTo( Constraint c ) {
		if ( c instanceof Assertable ) {
			if ( c.getClass().getSimpleName().equals(classString) && c.hashCode() == constraintHash ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		return new ArrayList<Term>();
	}

	@Override
	public Collection<Term> getGroundTerms() {
		return new ArrayList<Term>();
	}

	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();
	}

	@Override
	public Constraint copy() {
		return new Asserted( classString, constraintHash );
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Asserted ) {
			Asserted a = (Asserted)o;
			return a.classString.equals(this.classString) && a.constraintHash == this.constraintHash;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return classString.hashCode() + 3*constraintHash;
	}

	@Override
	public String toString() {
		return "(assert " + classString + " " + constraintHash + ")";
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
