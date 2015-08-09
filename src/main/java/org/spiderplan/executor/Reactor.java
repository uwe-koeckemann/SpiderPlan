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
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.Interval;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

public abstract class Reactor {
	public Statement target;
//	MetaCSPAdapter csp;
//	ConstraintDatabase execDB;
	
	protected long t;
	
	long t_init_start = -1;
	long t_init_finish = -1;
	
	public State s = State.NotStarted;
	
	AllenConstraint afterPast;
	AllenConstraint overlapsFuture;
	
	Collection<Constraint> activeConstraints = new ArrayList<Constraint>();
	
	public enum State { NotStarted, WaitingForStart, Started, WaitingForFinished, Finished, Done };
	
	public long EST,LST,EET,LET;
	
	public String name = "Executor";
	public int depth = 0;
	public boolean verbose = false;
	
	boolean firstUpdate = true;
	
	public Reactor( Statement target ) {
		this.target = target;
	}	
	
	public Collection<Constraint> update( long t, long EST, long LST, long EET, long LET, ConstraintDatabase execDB ) {
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
