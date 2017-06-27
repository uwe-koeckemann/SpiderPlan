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
package org.spiderplan.executor.ROS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.ROSRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

/**
 * @author Uwe Köckemann
 *
 */
public class ROSExecutionManager extends ExecutionManager {
	
	/**
	 * Initialize with name.
	 * @param name
	 */
	public ROSExecutionManager( String name ) {
		super(name);
	}


	private ConstraintDatabase addedByROS = new ConstraintDatabase();
	
	List<ROSConstraint> ROSsubs = new ArrayList<ROSConstraint>();
	List<ROSConstraint> ROSpubs = new ArrayList<ROSConstraint>();
	Term rosSubInterval = Term.createVariable("?I_ROS");
	int ROS_NumSameValuesRequired = 2;
	int ROS_SameValueCounter = 0;
	Term ROS_NewValue = null;
	
	private Set<Term> variablesObservedByROS = new HashSet<Term>();
	
	// TODO: this came from ExecutionModule before
	Map<Term,Statement> lastChangingStatement = new HashMap<Term, Statement>();
	Map<Term,Expression> lastAddedDeadline = new HashMap<Term, Expression>();
	

	
	
	@Override
	public void initialize( ConstraintDatabase cdb ) {
		/**
		 * ROS subscriptions
		 */
		for ( ROSConstraint rosCon : cdb.get(ROSConstraint.class) ) {
			if ( rosCon.getRelation().equals(ROSRelation.Publish) ) {
				if ( verbose ) Logger.msg(this.getName(),"Publishing to: " + rosCon, 1);
				ROSProxy.publishToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName());
				this.ROSpubs.add(rosCon);
			} else {
				if ( verbose ) Logger.msg(this.getName(),"Subsribing to: " + rosCon, 1);
				ROSProxy.subscribeToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName(), rosCon.getMsg().getArg(0).toString());
				this.ROSsubs.add(rosCon);
				variablesObservedByROS.add(rosCon.getVariable());
			}
		}
			
		/**
		 * Reactors ROS goals
		 */
		for ( ROSGoal rosGoal :  cdb.get(ROSGoal.class) ) {
			if ( verbose ) { 
				 Logger.msg(this.getName(),"Goal: " + rosGoal, 1);
				 Logger.msg(this.getName(),"Registering action '" + rosGoal.getActionName() + "' at '" + rosGoal.getServerID() + "'" , 1);
			} 
			
			// TODO: Might register the same action multiple times (should not be a problem)
			ROSProxy.register_action(rosGoal.getServerID(), rosGoal.getActionName()); 
			
			for ( Statement s : cdb.get(Statement.class) ) {
				
				Substitution subst = rosGoal.getVariable().match(s.getVariable());
				
				if ( subst != null ) {
					if ( verbose ) Logger.msg(this.getName(),"    Creating reactor for " + s, 1);
					if ( hasReactorList.contains(s) ) {
						throw new IllegalStateException("Statement " + s + " has multiple reactors... This cannot be good!");
					}
					if ( !execList.contains(s) ) { 
						execList.add(s);
					}
					hasReactorList.add(s);
					ROSGoal goalCopy = rosGoal.copy();
					goalCopy.substitute(subst);					
					ReactorROS reactor = new ReactorROS(s, goalCopy);
					this.reactors.add(reactor);
				}
			}
		}	
	}
	

	@Override
	public boolean update(long t, ConstraintDatabase cdb) {
		/**
		 * What this should do:
		 * 
		 * - Add/remove reactors where necessary
		 * - Manage ROS subscribers
		 * - Publish statements to ROS
		 * - Update reactor state via super.update(..)
		 */
		
		
		for ( ROSGoal rosGoal :  cdb.get(ROSGoal.class) ) {
			if ( verbose ) { 
				 Logger.msg(this.getName(),"Goal: " + rosGoal, 1);
				 Logger.msg(this.getName(),"Registering action '" + rosGoal.getActionName() + "' at '" + rosGoal.getServerID() + "'" , 1);
			} 
			
			// TODO: Might register the same action multiple times (should not be a problem)
			ROSProxy.register_action(rosGoal.getServerID(), rosGoal.getActionName()); 
			
			for ( Statement s : cdb.get(Statement.class) ) {
				if ( !super.hasReactorList.contains(s) ) {				
					Substitution subst = rosGoal.getVariable().match(s.getVariable());
					
					if ( subst != null ) {
						if ( verbose ) Logger.msg(this.getName(),"    Creating reactor for " + s, 1);
						if ( !execList.contains(s) ) { 
							execList.add(s);
						}
						hasReactorList.add(s);
						ROSGoal goalCopy = rosGoal.copy();
						goalCopy.substitute(subst);					
						ReactorROS reactor = new ReactorROS(s, goalCopy);
						this.reactors.add(reactor);
					}
				}
			}
		}	
		
		ValueLookup propagatedTimes = cdb.getUnique(ValueLookup.class);
		
		Term variable;
		Term value;
		Term rosMessage;
		/*
		 * 1) Read
		 */
		for ( ROSConstraint sub : ROSsubs ) {
			variable = sub.getVariable();
			rosMessage = ROSProxy.get_last_msg(sub.getTopic().toString());
			
			if ( rosMessage != null ) {
				Substitution theta = sub.getMsg().match(rosMessage);
				value = sub.getValue().substitute(theta);
			} else {
				value = null;
			}			
			
			Statement s = lastChangingStatement.get(variable);
			if ( value == null && s == null ) {
				// nothing to do
			} else if ( (s != null && value == null) || (s != null && s.getValue().equals(value)) ) {
				// same as before: just update deadline
				
				addedByROS.remove(lastAddedDeadline.get(variable));
				cdb.remove(lastAddedDeadline.get(variable));
				AllenConstraint deadline = new AllenConstraint(s.getKey(), TemporalRelation.Deadline, new Interval(Term.createInteger(t+1),Term.createConstant("inf")));
				addedByROS.add(deadline);
				cdb.add(deadline);
				
				lastAddedDeadline.put(variable, deadline);
				if ( verbose ) Logger.msg(this.getName(),"[ROS] Updated existing statement: " + s, 2);
			} else {
				// new value
				
				if ( ROS_NewValue == null || !ROS_NewValue.equals(value) ) {
					ROS_SameValueCounter = 1;
					ROS_NewValue = value;				
				} else {
					ROS_SameValueCounter++;
				}
				
				if ( ROS_SameValueCounter == ROS_NumSameValuesRequired ) {
					ROS_SameValueCounter = 0;
					
					if ( s != null ) {
						Statement oldAssignment = lastChangingStatement.get(variable);
						addedByROS.remove(lastAddedDeadline.get(variable));
						cdb.remove(lastAddedDeadline.get(variable));
						AllenConstraint finalDeadline = new AllenConstraint(oldAssignment.getKey(), TemporalRelation.Deadline, new Interval(t,t));
						addedByROS.add(finalDeadline);
						cdb.add(finalDeadline);
					}			

					Term interval = rosSubInterval.makeUnique(UniqueID.getID()).makeConstant();
					Statement newStatement = new Statement(interval,variable,value);
					AllenConstraint release = new AllenConstraint(interval, TemporalRelation.Release, new Interval(t,t));
					AllenConstraint deadline = new AllenConstraint(interval, TemporalRelation.Deadline, new Interval(Term.createInteger(t+1),Term.createConstant("inf")));
					
					lastChangingStatement.put(variable, newStatement);
					lastAddedDeadline.put(variable, deadline);
					
					addedByROS.add(newStatement);
					addedByROS.add(release);
					addedByROS.add(deadline);
					cdb.add(newStatement);
					cdb.add(release);
					cdb.add(deadline);
										
					if ( verbose ) Logger.msg(this.getName(),"[ROS] Added new Statement: " + newStatement, 2);
				}
			}
		}
		
		/*
		 * 3) Publish current value
		 */
		for ( ROSConstraint pub : ROSpubs ) {
			variable = pub.getVariable(); 
			Statement currentStatement = null;
			for ( Statement s : cdb.get(Statement.class) ) {
				if ( variable.equals(s.getVariable()) && propagatedTimes.getEST(s.getKey()) <= t && propagatedTimes.getEET(s.getKey()) >= t ) {
					currentStatement = s;
					break;
				}
			}
			if ( currentStatement != null ) {
				Term ourValue = currentStatement.getValue();
				Substitution theta = pub.getValue().match(ourValue);
				Term toSend = pub.getMsg().substitute(theta);
				
				ROSProxy.send_msg(pub.getTopic().toString(), toSend);
				if ( verbose ) Logger.msg(this.getName(),"[ROS] Send message "+toSend+" to topic /"+pub.getTopic(), 2);
			}
		}		
		
		super.update(t, cdb);
		
		return true;
	}

//	@Override
//	public Collection<Reactor> getReactors() {
//		// TODO Auto-generated method stub
//		return null;
//	}


}
