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
package org.spiderplan.representation.expressions.execution;

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
 * Contains a {@link Statement} that is expected to be observed during execution.
 * 
 * @author Uwe Köckemann
 *
 */
public class Observation extends Expression implements Substitutable, Mutable, Assertable {
	
	private Statement task;
	private boolean isAsserted = false;
		
	/**
	 * Create a task from a {@link Statement}.
	 * 
	 * @param task the task statement
	 */
	public Observation( Statement task ) {
		super(ExpressionTypes.Observation);
		this.task = task;
	}	
	
	/**
	 * Get the statement of this task.
	 * @return the statement
	 */
	public Statement getStatement() { return task; }; 
	
	/**
	 * Get assertion that marks this goal as reached
	 * @return the assertion
	 */
	public Assertion getAssertion() {
		Term achieved = Term.createComplex("achieved", this.task.getKey());
		Assertion assertion = new Assertion(super.getType(), achieved);
		return assertion;
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.task.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public Observation copy() {
		Observation c = new Observation(task);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Observation ) {
			Observation piC = (Observation)o;
			return piC.task.equals(this.task);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return task.toString();
	}

	@Override
	public Observation substitute(Substitution theta) {
		task = task.substitute(theta);
		return this;
	};
	
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
		if ( assertion.getExpressionType().equals(super.getType())) {
			if ( assertion.getParameter(0).getArg(0).equals(this.task.getKey()) ) {
				return true;
			}
		}
		return false;
	};
}
