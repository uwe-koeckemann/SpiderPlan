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
package org.spiderplan.representation.expressions.misc;

import java.util.Collection;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.logic.Term;

/**
 * Contains a {@link ConstraintDatabase} that has to be applied to
 * a solution to an overall problem.
 * 
 * @author Uwe Köckemann
 */
public class Finally extends Expression implements Mutable, Assertable {
	
	final private static Term ConstraintType = Term.createConstant("finally");

	private Term id;
	private ConstraintDatabase cDB;
	private boolean isAsserted = false;
	
	/**
	 * Set a constraint to asserted.
	 * @param id Identifier of this finally constraint
	 * @param cDB The constraint that is asserted.
	 */
	public Finally( Term id, ConstraintDatabase cDB ) {
		super(ConstraintType);
		this.id = id;
		this.cDB = cDB;
	}
		
	/**
	 * Add the internal {@link ConstraintDatabase} to another one. 
	 * @param cDB The {@link ConstraintDatabase} to which to apply
	 * the {@link Finally} {@link ConstraintDatabase}.
	 */
	public void apply( ConstraintDatabase cDB ) {
		cDB.add(this.cDB);
	}
	
	/**
	 * Get assertion that marks this finally constraint as added
	 * @return the assertion
	 */
	public Assertion getAssertion() {
		Term added = Term.createComplex("added", this.id);
		Assertion assertion = new Assertion(super.getType(), added);
		return assertion;
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
		
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.id.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.cDB.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public Expression copy() {
		Finally c = new Finally(id, cDB.copy());
		c.setAsserted(this.isAsserted());
		return c;		
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Finally ) {
			return ((Finally)o).cDB.equals(this.cDB);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 5 + cDB.hashCode();
	}

	@Override
	public String toString() {
		return "(:finally " + id + " " + cDB.toString() + ")";
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
	public boolean appliesTo(Assertion assertion) {
		return assertion.getExpressionType().equals(super.getType())
				&& assertion.getParameter(0).getArg(0).equals(this.id);
	}

}
