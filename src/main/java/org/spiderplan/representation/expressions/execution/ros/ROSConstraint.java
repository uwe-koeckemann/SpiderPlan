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
package org.spiderplan.representation.expressions.execution.ros;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.ROSRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;


/**
 * Publish and subscribe to ROS topics.
 * 
 * @author Uwe Köckemann
 * 
 */
public class ROSConstraint extends Expression implements Substitutable, Mutable {
	
	ROSRelation relation;
	Term variable;
	Term value;
	Term topic;
	Term msg;
	
	/**
	 * Create a ROS constraint for publishing/subscribing the values
	 * of all statements with a specific state-variable.
	 * 
	 * @param relation select publish or subscribe
	 * @param variable the state-variable
	 * @param value used to determine what to send or how to subscribe
	 * @param topic ROS topic to publish/subscribe to
	 * @param msg ROS message in form of a (complex) term
	 */
	public ROSConstraint( ROSRelation relation, Term variable, Term value, Term topic, Term msg ) {
		super(ExpressionTypes.ROS);
		
		this.relation = relation;
		this.variable = variable;
		this.value = value;
		this.topic = topic;
		this.msg = msg;
	}
		
	/**
	 * Get the relation (publish or subscribe)
	 * @return the relation.
	 */
	public ROSRelation getRelation() { return relation; }
	/**
	 * Get the state-variable.
	 * @return the state-variable
	 */
	public Term getVariable() { return variable; }
	/**
	 * Get the value.
	 * @return the value
	 */
	public Term getValue() { return value; }
	/**
	 * Get the ROS topic.
	 * @return the ROS topic
	 */
	public Term getTopic() { return topic; }
	/**
	 * Get message description.
	 * @return message description as (complex) term
	 */
	public Term getMsg() { return msg; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.variable.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.value.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.topic.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.msg.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public ROSConstraint copy() {
		return new ROSConstraint(this.relation, this.variable, this.value, this.topic, this.msg); //, this.wrt);
	}

	@Override
	public Expression substitute(Substitution theta) {
		variable = variable.substitute(theta);
		value = value.substitute(theta);
		topic = topic.substitute(theta);
		msg = msg.substitute(theta);
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ROSConstraint ) {
			ROSConstraint c = (ROSConstraint)o;
			
			if ( !this.relation.equals(c.relation) ) {
				return false;
			} else if ( !this.variable.equals(c.variable) ) {
				return false;
			} else if ( !this.value.equals(c.value) ) {
				return false;
			} else if ( !this.msg.equals(c.msg) ) {
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
		if ( relation.equals(ROSRelation.Publish)) 
			rb.append("(publish\n\t");
		else 
			rb.append("(subscribe\n\t");
		rb.append(variable.toString());
		rb.append("\n\t");
		rb.append(value.toString());
		rb.append("\n\t");
		rb.append(topic.toString());
		rb.append("\n\t");
		rb.append(msg.toString());
		rb.append("\n)");
		return rb.toString();
	}
}
