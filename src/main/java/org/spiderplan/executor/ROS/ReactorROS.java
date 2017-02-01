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
package org.spiderplan.executor.ROS;

import java.util.Collection;

import org.spiderplan.executor.Reactor;
import org.spiderplan.executor.Reactor.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.logic.Term;

/**
 * Execute a ROS goal. Requires ROSproxy to be running.
 * 
 * @author Uwe Koeckemann
 */
public class ReactorROS extends Reactor {
	
	ROSGoal goal;
	
	Substitution goalResultSub;
	
	private int ID;
	
	/**
	 * Default constructor.
	 * 
	 * @param target statement to be executed
	 * @param goal associated ROS goal
	 */
	public ReactorROS( Statement target, ROSGoal goal ) {
		super(target);
		this.goal = goal;
	}
	
	@Override
	public Collection<Expression> update(long t, long EST, long LST, long EET, long LET, ConstraintDatabase execDB) {
		 super.update(t, EST, LST, EET, LET, execDB);
		 
		 if ( s == State.Done ) {
			 if ( goalResultSub != null ) {
				 execDB.substitute(goalResultSub);
				 goalResultSub = null;
			 }
		 }
		 
		 return super.activeConstraints;
	}
	
	@Override
	public void initStart( ) {
		ID = ROSProxy.send_goal(goal.getServerID(), goal.getActionName(), goal.getGoalMsg());
	}
	
	@Override
	public boolean hasStarted( long EST, long LST ) {
		/**
		 * Start if actual start time reached
		 */
		if ( ROSProxy.has_started(ID) ) {
			print("Starting at " + t  + " [EST LST] = [" + EST + " " + LST + "]", 2);
			return true;
		} 
		return false;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET ) {	
		/**
		 * End when actual end time reached
		 */
		if ( ROSProxy.has_finished(ID, goal.getResultMsg()) ) {
			goalResultSub = ROSProxy.getROSGoalResultSubstitution(ID);
			print("Finishing at " + t  + " [EET LET] = [" + EET + " " + LET + "]", 2);
			return true;
		} 
		return false;
	}
}
