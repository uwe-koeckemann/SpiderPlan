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
package org.spiderplan.representation.expressions.sampling;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.SamplingRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;

/**
 * Sampling constraints are used to randomize constraint databases.
 * They specify variables that take random values as soon as sampling
 * constraints are evaluated. 
 * 
 * @author Uwe Köckemann
 * 
 */
public class SamplingConstraint extends Expression implements Substitutable {
	
	Term con;
	SamplingRelation relation;
		
	/**
	 * Create a new sampling constraint.
	 * @param con {@link Term} representation of constraint
	 */
	public SamplingConstraint( Term con ) {
		super(ExpressionTypes.Cost);
		relation = ExpressionTypes.SamplingConstraints.assertSupported(con, this.getClass());
		this.con = con;
	}
	
	/**
	 * Get relational representation of this math constraint.
	 * @return the relation
	 */
	public Term getConstraint() {
		return con;
	}
	
	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public SamplingRelation getRelation() {
		return relation;
	}

	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.con.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new SamplingConstraint(con.substitute(theta));
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof SamplingConstraint ) {	
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return con.toString();
	}
}
