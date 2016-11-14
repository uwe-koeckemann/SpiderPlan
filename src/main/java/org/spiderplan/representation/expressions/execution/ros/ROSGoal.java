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
 * Translates name of an operator to a goal for a ROS action.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class ROSGoal extends Expression implements Substitutable, Mutable {
	
	Atomic operatorName;
	Term serverID;
	Term actionName;
	Term goalMsg;
	
	Term resultMsg = null;
	
	/**
	 * Makes a state-variable a ROS goal. As a result the value of a statement 
	 * will be published as a goal during execution. 
	 * 
	 * @param variable state-variable to execute
	 * @param serverID ID of ROS action server
	 * @param actionName name of ROS action
	 * @param goalMsg ROS goal message
	 * @param resultMsg term to match result message
	 */
	public ROSGoal( Atomic variable, Term serverID, Term actionName, Term goalMsg, Term resultMsg ) {
		super(ExpressionTypes.ROS);
		
		this.operatorName = variable;
		this.serverID = serverID;
		this.actionName = actionName;
		this.goalMsg = goalMsg;
		this.resultMsg = resultMsg;
//		this.wrt = condition;
	}

//	public ROSGoal( ROSGoal c ) {	
//		super(ConstraintTypes.ROS);	
//		this.operatorName = c.operatorName;
//		this.serverID = c.serverID;
//		this.actionName = c.actionName;
//		this.goalMsg = c.goalMsg;
//		if ( c.resultMsg != null )
//			this.resultMsg = c.resultMsg;
//	}
	
	/**
	 * Get the state-variable representing the ROS goal
	 * @return state-variable
	 */
	public Atomic getVariable() { return operatorName; }
	/**
	 * Get the ID of the ROS action server.
	 * @return server ID
	 */
	public Term getServerID() { return serverID; }
	/**
	 * Get the name of the ROS acton.
	 * @return ROS action name
	 */
	public Term getActionName() { return actionName; }
	/**
	 * Get the ROS goal message.
	 * @return ROS goal message as a (complex) term
	 */
	public Term getGoalMsg() { return goalMsg; }
	/**
	 * Get the ROS goal result message that is sent after the action was completed.
	 * @return ROS message for result of action as a (complex) term
	 */
	public Term getResultMsg() { return resultMsg; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(operatorName.getVariableTerms());
		r.addAll(serverID.getVariables());
		r.addAll(goalMsg.getVariables());
		r.addAll(actionName.getVariables());
		return r;
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(operatorName.getGroundTerms());
		if ( serverID.isGround() )
			r.add(serverID);
		if ( actionName.isGround() )
			r.add(actionName);
		if ( goalMsg.isGround() )
			r.add(goalMsg);
		return r;
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		Collection<Atomic> r = new HashSet<Atomic>();
		r.add(this.operatorName);
		return r;
	}

	@Override
	public ROSGoal copy() {		
		return new ROSGoal(this.operatorName, this.serverID, this.actionName, this.goalMsg, this.resultMsg);
	}

	@Override
	public Expression substitute(Substitution theta) {
		operatorName = operatorName.substitute(theta);
		serverID = serverID.substitute(theta);
		actionName = actionName.substitute(theta);
		goalMsg = goalMsg.substitute(theta);
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ROSGoal ) {
			ROSGoal c = (ROSGoal)o;
			if ( !this.operatorName.equals(c.operatorName) ) {
				return false;
			} else if ( !this.serverID.equals(c.serverID) ) {
				return false;
			} else if ( !this.actionName.equals(c.actionName) ) {
				return false;
			} else if ( !this.goalMsg.equals(c.goalMsg) ) {
				return false;
			} else if ( !this.resultMsg.equals(c.resultMsg) ) {
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
		rb.append("(is-goal\n\t");
		rb.append(operatorName.toString());
		rb.append("\n\t");
		rb.append(serverID.toString());
		rb.append("\n\t");
		rb.append(actionName.toString());
		rb.append("\n\t");
		rb.append(goalMsg.toString());
		rb.append("\n)");
		return rb.toString();
	}
}
