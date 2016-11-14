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
package org.spiderplan.representation.expressions.execution.ros;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Cost constraints either add or subtract a value from a cost variable
 * or pose inequalities on a cost variable.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class ROSRegisterAction extends Expression implements Substitutable, Mutable {

	Term serverID;
	Term actionName;
	
	/**
	 * @param serverID
	 * @param actionName
	 */
	public ROSRegisterAction( Term serverID, Term actionName ) {
		super(ExpressionTypes.ROS);
		
		this.serverID = serverID;
		this.actionName = actionName;
	}

	/**
	 * Get ROS action server ID.
	 * @return ROS action server ID
	 */
	public Term getServerID() { return serverID; }
	/**
	 * Get ROS action name.
	 * @return ROS action name
	 */
	public Term getActionName() { return actionName; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(actionName.getVariables());
		r.addAll(serverID.getVariables());
		return r;
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		Collection<Term> r = new ArrayList<Term>();
		if ( actionName.isGround() )
			r.add(actionName);
		if ( serverID.isGround() )
			r.add(serverID);
		return r;
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		Collection<Atomic> r = new HashSet<Atomic>();
		return r;
	}

	@Override
	public ROSRegisterAction copy() {
		return new ROSRegisterAction(this.serverID, this.actionName);
	}

	@Override
	public Expression substitute(Substitution theta) {
		serverID = serverID.substitute(theta);
		actionName = actionName.substitute(theta);
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ROSRegisterAction ) {
			ROSRegisterAction c = (ROSRegisterAction)o;
			if ( !this.serverID.equals(c.serverID) ) {
				return false;
			} else if ( !this.actionName.equals(c.actionName) ) {
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
		
		rb.append("(register-action ");
		rb.append(serverID.toString());
		rb.append(" ");
		rb.append(actionName.toString());
		rb.append(")");
		return rb.toString();
	}
}
