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
 * A given {@link Constraint} will be removed when this one is applied.
 * <p>
 * <b>Note:</b> This breaks an important assumptions and can lead to problems
 * that require backtracking over the order in which solvers are used or 
 * constraints are evaluated.
 * 
 * @author Uwe Köckemann
 */
public class Delete extends Constraint implements Mutable, Assertable {
	
	final private static Term ConstraintType = Term.createConstant("delete");

	private Constraint c;
	private boolean isAsserted = false;
	
	/**
	 * Set a constraint to asserted.
	 * @param c The constraint that is asserted.
	 */
	public Delete( Constraint c ) {
		super(ConstraintType);
		this.c = c;
	}
	
	/**
	 * Apply this constraint by removing a constraint.
	 * @param cDB The {@link ConstraintDatabase} to which to apply
	 * the assertion.
	 */
	public void apply( ConstraintDatabase cDB ) {
		ArrayList<Constraint> remList = new ArrayList<Constraint>();
		for ( Constraint c : cDB ) {
			if ( this.c.equals(c) ) {
				remList.add(c);
			}
		}
		this.setAsserted(true);
		cDB.removeAll(remList);
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		return c.getVariableTerms();
	}

	@Override
	public Collection<Term> getGroundTerms() {
		return c.getGroundTerms();
	}

	@Override
	public Collection<Atomic> getAtomics() {
		return c.getAtomics();
	}

	@Override
	public Constraint copy() {
		if ( c instanceof Mutable ) {
			return new Delete(((Mutable)c).copy());
		}
		return new Delete(c);
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Delete ) {
			return ((Delete)o).c.equals(this.c);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 3 + c.hashCode();
	}

	@Override
	public String toString() {
		return c.toString();
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
