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
package org.spiderplan.executor;

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.tools.logging.Logger;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Manages execution of statements. 
 * 
 * @author Uwe Köckemann
 *
 */
public abstract class Reactor {
	protected Statement target;
	
	protected long t;
	
	long t_init_start = -1;
	long t_init_finish = -1;
	
	protected State s = State.NotStarted;
	
	AllenConstraint afterPast;
	AllenConstraint overlapsFuture;
	
	Collection<Expression> activeConstraints = new ArrayList<Expression>();
	
	/**
	 * States of execution.
	 * 
	 * @author Uwe Köckemann
	 *
	 */
	public enum State { /**
	 * 
	 */
	NotStarted, /**
	 * 
	 */
	WaitingForStart, /**
	 * 
	 */
	Started, /**
	 * 
	 */
	WaitingForFinished, /**
	 * 
	 */
	Finished, /**
	 * 
	 */
	Done };
	
	private long EST,LST,EET,LET; // TODO: only used by toString()
	
	protected String name = "Executor"; // TODO: only used by logger. is there a better way to do this?
	protected int depth = 0;
	protected boolean verbose = false;
	
	boolean firstUpdate = true;
	
	/**
	 * Create a new reactor for target statement.
	 * 
	 * @param target statement to be executed
	 */
	public Reactor( Statement target ) {
		this.target = target;
	}	
	
	/**
	 * Get the statement executed by this reactor.
	 * 
	 * @return executed statement
	 */
	public Statement getTarget() { return target; }
	
	/**
	 * Get current state of execution.
	 * 
	 * @return the execution state
	 */
	public State getState() { return s; }
	
	/**
	 * Update state of execution
	 * 
	 * @param t current time 
	 * @param EST earliest start time of target statement
	 * @param LST latest start time of target statement
	 * @param EET earliest end time of target statement
	 * @param LET latest end time of target statement
	 * @param execDB execution context
 	 * @return constraints that need to be added to execution context to maintain execution timing
	 */
	public Collection<Expression> update( long t, long EST, long LST, long EET, long LET, ConstraintDatabase execDB ) {
		this.t = t;
		
		this.EST = EST;
		this.LST = LST;
		this.EET = EET;
		this.LET = LET;
		
		boolean change = true;
		
		while ( change ) {
			change = false;
		
			if ( s == State.WaitingForStart ) {
				if ( hasStarted(EST,LST) ) {
					print("Started at " + t, 2);
					s = State.Started;
					change = true;
				}
			} else if ( s == State.WaitingForFinished ) {
				if ( hasEnded(EET,LET) ) {
					print("Finished at " + t, 2);
					s = State.Finished;
					change = true;
				}
			} else if ( s == State.NotStarted ) {
				if ( t >= EST ) {
					print("Requesting start at " + t + "[EST LST] = [" + EST + " " + LST + "]", 2);
					this.initStart();
//					afterPast = new AllenConstraint(target.getKey(), new Term("past", false), TemporalRelation.MetBy);
					afterPast = new AllenConstraint(target.getKey(), Term.createConstant("past"), TemporalRelation.MetByOrAfter, new Interval(Term.createInteger(0), Term.createConstant("inf")));
//					afterPast = new AllenConstraint(target.getKey(), new Term("past", false), TemporalRelation.After, new Interval(new Term(1), new Term("inf",false)));
					execDB.add(afterPast);
					activeConstraints.add(afterPast);
					s = State.WaitingForStart;
					t_init_start = t;
					change = true;
				}
			} else if ( s == State.Started ) {
				print("Running at " + t, 2);
				AllenConstraint r = new AllenConstraint(target.getKey(), TemporalRelation.Release, new Interval(Term.createInteger(t), Term.createInteger(t)));
				execDB.add(r);
				activeConstraints.add(r);
				execDB.remove(afterPast);
				activeConstraints.remove(afterPast);
	//			overlapsFuture = new AllenConstraint(new Term("future",false),target.getKey(), TemporalRelation.ContainsOrStartedByOrOverlappedByOrMetBy, new Interval(new Term(0), new Term("inf",false)));
				overlapsFuture = new AllenConstraint(new Atomic("(met-by-or-overlapped-by future "+ target.getKey().toString() +" (interval 0 inf))"));
				execDB.add(overlapsFuture);
				activeConstraints.add(overlapsFuture);
				s = State.WaitingForFinished;
				t_init_finish = t;
				change = true;
			} else if ( s == State.Finished ) {
				print("Finished at " + t, 2);
				AllenConstraint r = new AllenConstraint(new Atomic("(deadline " + target.getKey().toString() + " (interval "+t+" "+t+"))"));
				execDB.remove(overlapsFuture);
				activeConstraints.remove(overlapsFuture);
				execDB.add(r);		
				activeConstraints.add(r);
						
				s = State.Done;
				change = true;
			}
		}
		
		return activeConstraints;
	}
		
	/**
	 * Initiate the start of an action.
	 * Overwrite for real execution.
	 */
	protected void initStart() {  
	}

	protected abstract boolean hasStarted( long EST, long LST );
	
	protected abstract boolean hasEnded( long EET, long LET );
	
	/**
	 * Setup for the print method
	 * @param name Name used for the {@link Logger}
	 * @param depth Message depth
	 * @param verbose true to send messages to {@link Logger}.
	 */
	public void printSetting( String name, int depth, boolean verbose ) {
		this.name = name;
		this.depth = depth;
		this.verbose = verbose;
	}
	
	/**
	 * Print message if {@link Module} is verbose and has given verbosity level
	 * @param s Message string
	 * @param level Required verbosity level to print this message
	 */
	public void print( String s, int level ) {
		Logger.msg(this.name, s, level);
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof Reactor ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.target.hashCode();
	}
	
	@Override 
	public String toString() {
		return target + " " + this.s + "[" + EST + " " + LST + "] [" + EET + " " + LET + "]";
	}
}
