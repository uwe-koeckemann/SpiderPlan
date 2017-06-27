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
package org.spiderplan.representation.expressions.execution.caresses;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.CaressesRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;


/**
 * Statements with variables matching this action will be executed.
 * 
 * @author Uwe Köckemann
 * 
 */
public class CaressesExpression extends Expression implements Substitutable {
	
	CaressesRelation relation;
	Term expression;
	
	/**
	 * Makes a state-variable a ROS goal. As a result the value of a statement 
	 * will be published as a goal during execution. 
	 * 
	 * @param expression name of ROS action
	 */
	public CaressesExpression( Term expression ) {
		super(ExpressionTypes.Caresses);
		this.relation = ExpressionTypes.CaressesExpressions.assertSupported(expression, CaressesExpression.class);
		this.expression = expression;
	}
	/**
	 * Get the name of the action.
	 * @return expression
	 */
	public Term getExpression() { return expression; }
	
	/**
	 * Get type of relation.
	 * @return the relation
	 */
	public CaressesRelation getRelation() {
		return relation;
	}
	
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.expression.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new CaressesExpression(this.expression.substitute(theta));
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof CaressesExpression ) {
			CaressesExpression c = (CaressesExpression)o;
			if ( !this.expression.equals(c.expression) ) {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder rb = new StringBuilder();
		rb.append("(action \n\t");
		rb.append(expression.toString());
		rb.append("\n)");
		return rb.toString();
	}
}
