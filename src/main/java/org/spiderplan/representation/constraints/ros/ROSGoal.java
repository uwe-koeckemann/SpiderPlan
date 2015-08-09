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
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Translates name of an operator to a goal for a ROS action.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class ROSGoal extends Constraint implements Substitutable, Mutable {
	
	Atomic operatorName;
	Term serverID;
	Term actionName;
	Term goalMsg;
	
	Term resultMsg = null;
	
	ConstraintDatabase wrt = null;
	
	public ROSGoal( Atomic operatorName, Term serverID, Term actionName, Term goalMsg ) {
		super(ConstraintTypes.ROS);
		
		this.operatorName = operatorName;
		this.serverID = serverID;
		this.actionName = actionName;
		this.goalMsg = goalMsg;
	}
	
	public ROSGoal(Atomic operatorName, Term serverID, Term actionName, Term goalMsg, ConstraintDatabase condition ) {
		super(ConstraintTypes.ROS);
		
		this.operatorName = operatorName;
		this.serverID = serverID;
		this.actionName = actionName;
		this.goalMsg = goalMsg;
		this.wrt = condition;
	}
	
	public ROSGoal( Atomic operatorName, Term serverID, Term actionName, Term goalMsg, Term resultMsg, ConstraintDatabase condition ) {
		super(ConstraintTypes.ROS);
		
		this.operatorName = operatorName;
		this.serverID = serverID;
		this.actionName = actionName;
		this.goalMsg = goalMsg;
		this.resultMsg = resultMsg;
		this.wrt = condition;
	}

	public ROSGoal( ROSGoal c ) {	
		super(ConstraintTypes.ROS);	
		this.operatorName = c.operatorName;
		this.serverID = c.serverID;
		this.actionName = c.actionName;
		this.goalMsg = c.goalMsg;
		if ( c.resultMsg != null )
			this.resultMsg = c.resultMsg;
		if ( c.wrt != null )
			this.wrt = c.wrt.copy();
	}
	
	public Atomic getOperatorName() { return operatorName; }
	public Term getServerID() { return serverID; }
	public Term getActionName() { return actionName; }
	public Term getGoalMsg() { return goalMsg; }
	public Term getResultMsg() { return resultMsg; }
	public ConstraintDatabase getCDB() { return wrt; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Collection<Term> r = new ArrayList<Term>();
		r.addAll(operatorName.getVariableTerms());
		r.addAll(serverID.getVariables());
		r.addAll(goalMsg.getVariables());
		r.addAll(actionName.getVariables());
		r.addAll(wrt.getVariableTerms());
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
		r.addAll(wrt.getGroundTerms());
		return r;
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		Collection<Atomic> r = new HashSet<Atomic>();
		r.add(this.operatorName);
		r.addAll(wrt.getAtomics());
		return r;
	}

	@Override
	public ROSGoal copy() {
		ROSGoal c = new ROSGoal(this);
		return c;
	}

	@Override
	public Constraint substitute(Substitution theta) {
		operatorName = operatorName.substitute(theta);
		serverID = serverID.substitute(theta);
		actionName = actionName.substitute(theta);
		goalMsg = goalMsg.substitute(theta);
		if ( wrt != null ) {
			wrt.substitute(theta);
		}
		return this;
	}	
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof ROSGoal ) {
			ROSGoal c = (ROSGoal)o;
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
		rb.append("(is-goal\n\t");
		rb.append(operatorName.toString());
		rb.append("\n\t");
		rb.append(serverID.toString());
		rb.append("\n\t");
		rb.append(actionName.toString());
		rb.append("\n\t");
		rb.append(goalMsg.toString());
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
