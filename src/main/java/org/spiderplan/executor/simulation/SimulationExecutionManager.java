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
package org.spiderplan.executor.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.executor.Reactor;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.execution.Simulation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.DateTimeReference;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;

/**
 * Execution manager for simulated events that can be predetermined
 * in the domain definition (e.g., future incoming goals, observations,
 * activities, ...).
 * 
 * @author Uwe Köckemann
 *
 */
public class SimulationExecutionManager extends ExecutionManager {

	/**
	 * Initialize with name.
	 * @param name
	 */
	public SimulationExecutionManager( String name ) {
		super(name);
	}

	ArrayList<Statement> simList = new ArrayList<Statement>();
		
	ConstraintDatabase simDB = new ConstraintDatabase();
	
	private Map<Long,ConstraintDatabase> dispatchedDBs = new HashMap<Long, ConstraintDatabase>();
	
	Term tHorizon = Term.createConstant("time");
	Statement past = new Statement(Term.createConstant("past"), tHorizon, Term.createConstant("past") );
	Statement future = new Statement(Term.createConstant("future"), tHorizon, Term.createConstant("future") );
	
	AllenConstraint rPast = new AllenConstraint(Term.parse("(release past (interval 0 0))"));
	AllenConstraint mPastFuture = new AllenConstraint(Term.parse("(meets past future)"));
	AllenConstraint dFuture = new AllenConstraint(Term.parse("(deadline future (interval inf inf))"));
	AllenConstraint rFuture = new AllenConstraint(Term.parse("(deadline past (interval 1 1)"));
	AllenConstraint mFuture;
	
	IncrementalSTPSolver simCSP;
	
	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
	private ConstraintDatabase addedSimDBs = new ConstraintDatabase();
	private ConstraintDatabase addedOnReleaseDB = new ConstraintDatabase();
	
	@Override
	public void initialize(ConstraintDatabase cdb) {
		simDB = new ConstraintDatabase();
		simDB.add(cdb.getUnique(DateTimeReference.class));
		simDB.add(cdb.getUnique(PlanningInterval.class));
		
		for ( Simulation simCon : cdb.get(Simulation.class) ) {
			if ( simCon.getDispatchTime().toString().equals("on-release")) {
				simDB.add(simCon.getExecutionTimeDB());
				for ( Statement s : simCon.getExecutionTimeDB().get(Statement.class) ) {
					simList.add(s);
				}
			} else {
				try {
					Long dispatchTime = Long.valueOf(simCon.getDispatchTime().toString());
					ConstraintDatabase dispatchedDB = dispatchedDBs.get(dispatchTime);
					if ( dispatchedDB == null ) {
						dispatchedDB = new ConstraintDatabase();
						dispatchedDBs.put(dispatchTime, dispatchedDB);
					}
					dispatchedDB.add(simCon.getExecutionTimeDB());
					
				} catch ( NumberFormatException e ) {
					if ( verbose ) Logger.msg(getName(), "Non-ground dispatch time ignored for simulation constraint:\n" + simCon, 1);
				}
			}
		}
		
		for ( Statement s : simList ) {
			reactors.add(new ReactorPerfectSimulation(s));
		}
		
		simCSP = new IncrementalSTPSolver(0,Global.MaxTemporalHorizon);
		
		simDB.add(past);
		simDB.add(future);
		simDB.add(rPast);
		simDB.add(rFuture);
		simDB.add(mPastFuture);
		simDB.add(dFuture);
		
		TemporalNetworkTools.ensurePlanningTimeIsUsed(simDB);
				
		if ( !simCSP.isConsistent(simDB) ) {
			throw new IllegalStateException("Execution failure: Temporal inconsistency in simulation CDB.");
		}
	}
	
	@Override
	public boolean update( long t, ConstraintDatabase cdb ) {
		boolean change = false;
		
		/************************************************************************************************
		 * Dispatch new information (from simulation)
		 ************************************************************************************************/
		if ( dispatchedDBs.get(t) != null ) {
//			System.out.println("Dispatching: " + t);
			if ( verbose ) Logger.msg(getName(), "Dispatching:\n" + dispatchedDBs.get(t), 1);
			cdb.add(dispatchedDBs.get(t));
			addedSimDBs.add(dispatchedDBs.get(t));
		}
				
		simDB.remove(rFuture);
		rFuture = new AllenConstraint(Term.parse("(deadline past (interval "+(t)+" "+(t)+"))"));
		simDB.add(rFuture);
		
		ArrayList<Reactor> remList = new ArrayList<Reactor>();		
		for ( Reactor r : reactors ) {
			
			r.printSetting(name, Logger.depth, verbose);
			if ( r.getState() == Reactor.State.Done ) {
				doneList.add(r.getTarget());
				remList.add(r);
				change = true;
				
			} else {
				Collection<Expression> addedCons;
				long EST, LST, EET, LET;
				

				EST = simCSP.getEST(r.getTarget().getKey());
				LST = simCSP.getLST(r.getTarget().getKey());
				EET = simCSP.getEET(r.getTarget().getKey());
				LET = simCSP.getLET(r.getTarget().getKey());
				addedCons = r.update(t, EST, LST, EET, LET, simDB);
		
				
				if ( !r.getState().equals(Reactor.State.NotStarted) && !startedList.contains(r.getTarget()) ) {
					startedList.add(r.getTarget());
				}
				
				/**
				 * Add statements and constraints from sim list.
				 * (only from simulation constraints with "on-release" dispatch)
				 */
				if ( simList.contains(r.getTarget()) ) {
					if ( !addedCons.isEmpty() && !addedCons.equals(addedConstraints.get(r.getTarget())) ) {
						if ( addedConstraints.get(r.getTarget()) != null ) {
							cdb.removeAll(addedConstraints.get(r.getTarget()));
							addedOnReleaseDB.removeAll(addedConstraints.get(r.getTarget()));
						}
						cdb.addAll(addedCons);
						
						if ( verbose ) {
							Logger.msg(getName(), "On release adding:\n" + addedCons, 1);
						}
						
						addedOnReleaseDB.addAll(addedCons);
						if ( !cdb.contains(r.getTarget()) ) {
							addedOnReleaseDB.add(r.getTarget());
							cdb.add(r.getTarget());
						}
						change = true;
					}
				}
				Collection<Expression> store = new ArrayList<Expression>();
				store.addAll(addedCons);
				addedConstraints.put(r.getTarget(), store);
				
			}
		}
		reactors.removeAll(remList);
		
		return change;
	}

}
