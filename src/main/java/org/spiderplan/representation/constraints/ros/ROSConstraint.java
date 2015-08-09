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
package org.spiderplan.representation.constraints.ros;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintTypes;
import org.spiderplan.representation.constraints.ConstraintTypes.ROSRelation;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Cost constraints either add or subtract a value from a cost variable
 * or pose inequalities on a cost variable.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class ROSConstraint extends Constraint implements Substitutable, Mutable {
	
	ROSRelation relation;
	Atomic variable;
	Term value;
	Term topic;
	Term msg;
	ConstraintDatabase wrt = null;
	
	public ROSConstraint( ROSRelation relation, Atomic variable, Term value, Term topic, Term msg ) {
		super(ConstraintTypes.ROS);
		
		this.relation = relation;
		this.variable = variable;
		this.value = value;
		this.topic = topic;
		this.msg = msg;
	}
	
	public ROSConstraint( ROSRelation relation, Atomic variable, Term value, Term topic, Term msg, ConstraintDatabase condition ) {
		super(ConstraintTypes.ROS);
		
		this.relation = relation;
		this.variable = variable;
		this.value = value;
		this.topic = topic;
		this.msg = msg;
		this.wrt = condition;
	}

	public ROSConstraint( ROSConstraint c ) {	
		super(ConstraintTypes.ROS);	
		this.relation = c.relation;
		this.variable = c.variable;
		this.value = c.value;
		this.topic = c.topic;
		this.msg = c.msg;
		this.wrt = c.wrt;
	}
	
	public ROSRelation getRelation() { return relation; }
	public Atomic getVariable() { return variable; }
	public Term getValue() { return value; }
	public Term getTopic() { return topic; }
	public Term getMsg() { return msg; }
	public ConstraintDatabase getCDB() { return wrt; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(variable.getVariableTerms());
		r.addAll(value.getVariables());
		r.addAll(msg.getVariables());
		r.addAll(topic.getVariables());
		r.addAll(wrt.getVariableTerms());
		return r;
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(variable.getGroundTerms());
		if ( value.isGround() )
			r.add(value);
		if ( topic.isGround() )
			r.add(topic);
		if ( msg.isGround() )
			r.add(msg);
		r.addAll(wrt.getGroundTerms());
		return r;
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		Collection<Atomic> r = new HashSet<Atomic>();
		r.add(this.variable);
		r.addAll(wrt.getAtomics());
		return r;
	}

	@Override
	public ROSConstraint copy() {
		ROSConstraint c = new ROSConstraint(this);
		return c;
	}

	@Override
	public Constraint substitute(Substitution theta) {
		variable = variable.substitute(theta);
		value = value.substitute(theta);
		topic = topic.substitute(theta);
		msg = msg.substitute(theta);
		if ( wrt != null ) {
			wrt.substitute(theta);
		}
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ROSConstraint ) {
			ROSConstraint c = (ROSConstraint)o;
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
		StringBuilder rb = new StringBuilder();
		if ( relation.equals(ROSRelation.PublishTo)) 
			rb.append("(publish-to\n\t");
		else 
			rb.append("(subscribe-to\n\t");
		rb.append(variable.toString());
		rb.append("\n\t");
		rb.append(value.toString());
		rb.append("\n\t");
		rb.append(topic.toString());
		rb.append("\n\t");
		rb.append(msg.toString());
		if ( wrt != null ) {
			rb.append("\n\t(condition");
			rb.append("\n\t\t");
			String s = wrt.toString().replace("\n", "\n\t\t");  
			rb.append(s.substring(0, s.length()-2));
			rb.append("\t)");
		}
		rb.append("\n)");
		return rb.toString();
	}
}
