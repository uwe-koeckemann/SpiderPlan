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
package org.spiderplan.representation.expressions.causal;

import java.util.Collection;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.misc.Assertion;
import org.spiderplan.representation.logic.Term;

/**
 * Contains a {@link Statement} as a goal.
 * 
 * @author Uwe Köckemann
 */
public class OpenGoal extends Expression implements Substitutable, Mutable, Assertable {
	
	private Statement goal;
	private boolean isAsserted = false;
		
	/**
	 * Create new open goal by providing a {@link Statement} that represents 
	 * what has to be achieved.
	 * @param g the statement representing the goal
	 */
	public OpenGoal( Statement g ) {
		super(ExpressionTypes.Goal);
		this.goal = g;
	}	
	
	/**
	 * Get the goal statement.
	 * @return the statement representing the goal
	 */
	public Statement getStatement() { return goal; };
	
	/**
	 * Get assertion that marks this goal as reached
	 * @return the assertion
	 */
	public Assertion getAssertion() {
		Term achieved = Term.createComplex("achieved", this.goal.getKey());
		Assertion assertion = new Assertion(super.getType(), achieved);
		return assertion;
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		return goal.getVariableTerms();		
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		return goal.getGroundTerms();
//	}
//	@Override
//	public Collection<Term> getComplexTerms() {
//		return new ArrayList<Term>();		
//	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.goal.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public OpenGoal copy() {
		OpenGoal c = new OpenGoal(goal);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof OpenGoal ) {
			OpenGoal piC = (OpenGoal)o;
			return piC.goal.equals(this.goal);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return goal.toString();
	}

	@Override
	public OpenGoal substitute(Substitution theta) {
		goal = goal.substitute(theta);
		return this;
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
	public boolean appliesTo( Assertion assertion ) {
		// (:goal (I x v))
		// (:assertion (assert task (achieved I)))
		if ( assertion.getExpressionType().equals(super.getType())) {
			if ( assertion.getParameter(0).getArg(0).equals(this.goal.getKey()) ) {
				return true;
			}
		}
		return false;
	};
}
