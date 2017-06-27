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
package org.spiderplan.representation.expressions.configurationPlanning;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.ConfigurationPlanningRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.misc.Assertion;
import org.spiderplan.representation.logic.Term;


/**
 * Defines information goals, links, and costs of providing information. 
 *  
 */
public class ConfigurationPlanningConstraint extends Expression implements Matchable, Substitutable, Assertable {
	
	private Term con;
	private ConfigurationPlanningRelation relation;
	
	private boolean isAsserted = false;
	
	/**
	 * Create a new {@link ConfigurationPlanningConstraint} based on {@link Term} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Term}
	 */
	public ConfigurationPlanningConstraint( Term l ) {
		super(ExpressionTypes.ConfigurationPlanning);
		relation = ExpressionTypes.ConfigurationPlanningConstraints.assertSupported(l, this.getClass());
		this.con = l;
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
	public ConfigurationPlanningRelation getRelation() {
		return relation;
	}
	
	/**
	 * Get assertion that marks this goal as reached
	 * @return the assertion
	 */
	public Assertion getAssertion() {
		if ( this.relation.equals(ConfigurationPlanningRelation.Goal )) {
			Term achieved = Term.createComplex("achieved", this.con.getArg(0));
			Assertion assertion = new Assertion(super.getType(), achieved);
			return assertion;
		} 
		throw new IllegalAccessError("Cannot generate assertion for non-goal expersion: " + this);
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public String toString() {
		return con.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new ConfigurationPlanningConstraint(con.substitute(theta));
	}
	
	@Override
	public boolean isGround() {
		return con.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof ConfigurationPlanningConstraint ) {
			ConfigurationPlanningConstraint rC = (ConfigurationPlanningConstraint)c;
			return this.getConstraint().match(rC.getConstraint());
		}
		return null;
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
		return assertion.getType().equals(super.getType())
				&& this.relation.equals(ConfigurationPlanningRelation.Goal)
				&& this.con.getArg(0).equals(assertion.getParameter(0).getArg(0));
	}

	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		con.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof ConfigurationPlanningConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
