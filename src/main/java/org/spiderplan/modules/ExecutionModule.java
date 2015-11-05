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
package org.spiderplan.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.spiderplan.executor.ROSProxy;
import org.spiderplan.executor.Reactor;
import org.spiderplan.executor.ReactorObservation;
import org.spiderplan.executor.ReactorPerfectSimulation;
import org.spiderplan.executor.ReactorROS;
import org.spiderplan.executor.ReactorRandomSimulation;
import org.spiderplan.executor.ReactorSoundPlaySpeech;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.ROSRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.execution.Simulation;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.execution.ros.ROSRegisterAction;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.Loop;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;

/**
 * Executes a plan.
 * 
 * @author Uwe Köckemann
 * 
 */
public class ExecutionModule  extends Module {

	long t = 0;	
	long tMax = 1000;
	
	long t0;
	
	boolean useRealTime = false; //TODO: Real time does not work yet
	boolean useForgetting = true;
	boolean perfectSim = true;
	
	ConstraintDatabase execDB;	
	ConstraintDatabase simDB;	
	
	ConstraintDatabase initialContext;

	ArrayList<Reactor> reactors = new ArrayList<Reactor>();
	
	ArrayList<Statement> hasReactorList = new ArrayList<Statement>();
	ArrayList<Statement> doneList = new ArrayList<Statement>();
	ArrayList<Statement> execList = new ArrayList<Statement>();
	ArrayList<Statement> startedList = new ArrayList<Statement>();
	ArrayList<Statement> simList = new ArrayList<Statement>();
	
	ArrayList<Operator> executedActions = new ArrayList<Operator>();
	ArrayList<Expression> executedLinks = new ArrayList<Expression>();
	ArrayList<Expression> reachedGoals = new ArrayList<Expression>();
	ArrayList<Statement> removedStatements = new ArrayList<Statement>();
	
	TypeManager tM;
	
	IncrementalSTPSolver execCSP;
	IncrementalSTPSolver simCSP;
	
	Atomic tHorizon = new Atomic("tHorizon");
	
	Statement past = new Statement(Term.createConstant("past"), tHorizon, Term.createConstant("past") );
	Statement future = new Statement(Term.createConstant("future"), tHorizon, Term.createConstant("future") );
	
	AllenConstraint rPast = new AllenConstraint(new Atomic("(release past (interval 0 0))"));
	AllenConstraint mPastFuture = new AllenConstraint(new Atomic("(meets past future)"));
	AllenConstraint dFuture = new AllenConstraint(new Atomic("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
	AllenConstraint rFuture = new AllenConstraint(new Atomic("(deadline past (interval 1 1)"));
	AllenConstraint mFuture;
	
	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
	private ConstraintDatabase addedSimDBs = new ConstraintDatabase();
	private ConstraintDatabase addedOnReleaseDB = new ConstraintDatabase();
	private ConstraintDatabase addedByROS = new ConstraintDatabase();
	
	private Set<Atomic> variablesObservedByROS = new HashSet<Atomic>();
	
	private Map<Long,ConstraintDatabase> dispatchedDBs = new HashMap<Long, ConstraintDatabase>();
	
	boolean drawTimeLines = true;
	TimeLineViewer timeLineViewer = null;
		
//	private String repairSolverName;
//	private Module repairSolver = null;
	
	private String fromScratchSolverName;
	private Module fromScratchSolver = null;
	
	private Core testCore = new Core();
	
	boolean firstUpdate = true;
	
	Plan plan;
	Collection<Operator> O;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public ExecutionModule(String name, ConfigurationManager cM ) {
		super(name, cM);
		
//		if ( cM.hasAttribute(name, "repairSolver") ) {
//			this.repairSolverName = cM.getString(this.getName(), "repairSolver" );
//			this.repairSolver = ModuleFactory.initModule( this.repairSolverName , cM );
//		}
		
		if ( cM.hasAttribute(name, "fromScratchSolver") ) {
			this.fromScratchSolverName = cM.getString(this.getName(), "fromScratchSolver" );
			this.fromScratchSolver = ModuleFactory.initModule( this.fromScratchSolverName, cM );
		}
	}
	
	@Override
	public Core run( Core core ) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;

		this.plan = core.getPlan();
		this.initialContext = Global.initialContext;
//		this.initialContext = core.getRootCore().getContext();
		
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(core);
		
		if ( useRealTime )
			this.t0 = System.currentTimeMillis();
		else
			this.t0 = 0;
		
		this.t = pI.getStartTimeValue();
		this.tMax = pI.getHorizonValue();
		
		this.tM = core.getTypeManager();
		this.O = core.getOperators();
		
		doneList = new ArrayList<Statement>();
		execList = new ArrayList<Statement>();
		simList = new ArrayList<Statement>();
		
		rPast = new AllenConstraint(new Atomic("(release past (interval 0 0))"));
		mPastFuture = new AllenConstraint(new Atomic("(meets past future)"));
		dFuture = new AllenConstraint(new Atomic("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
		rFuture = new AllenConstraint(new Atomic("(deadline past (interval 1 1)"));
		
		for ( Operator a : core.getPlan().getActions() ) {
			execList.add(a.getNameStateVariable());
		}
			 	
		execDB = core.getContext().copy();
		simDB = new ConstraintDatabase();
		
		for ( Simulation simCon : execDB.get(Simulation.class) ) {
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
		
		/**
		 * ROS subscriptions
		 */
		for ( ROSConstraint rosCon : execDB.get(ROSConstraint.class) ) {
			if ( rosCon.getRelation().equals(ROSRelation.PublishTo) ) {
				ROSProxy.publishToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName());
				this.ROSpubs.add(rosCon);
			} else {
				ROSProxy.subscribeToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName(), rosCon.getMsg().getArg(0).toString());
				this.ROSsubs.add(rosCon);
				variablesObservedByROS.add(rosCon.getVariable());
			}
		}
		
		for ( ROSRegisterAction regAction : execDB.get(ROSRegisterAction.class) ) {
			ROSProxy.register_action(regAction.getServerID(), regAction.getActionName());
		}
		
		/**
		 * Reactors for speech
		 */
		for ( Statement s : execDB.get(Statement.class) ) {
			if ( s.getVariable().getUniqueName().equals("say/1")) {
				String text = "";
				for ( IncludedProgram ip : execDB.get(IncludedProgram.class) ) {
					if ( s.getVariable().getArg(0).equals(ip.getName())) {
						text += ip.getCode();
					}
				}
				
				if ( text.isEmpty() ) {
					text = s.getValue().toString();
				}
								
				execList.add(s);
				hasReactorList.add(s);
				ReactorSoundPlaySpeech reactor = new ReactorSoundPlaySpeech(s,text);
				this.reactors.add(reactor);
			}
		}
		
		/**
		 * Reactors ROS goals
		 */
		for ( ROSGoal rosGoal :  execDB.get(ROSGoal.class) ) {
			for ( Statement s : execDB.get(Statement.class) ) {
				
				
				Substitution subst = rosGoal.getVariable().match(s.getVariable());
				if ( subst != null ) {
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
		
		/**
		 * Reactors for effects linked to observations
		 */
		for ( Operator a : core.getPlan().getActions() ) {
			for ( Statement e : a.getEffects() ) {
				if ( variablesObservedByROS.contains(e.getVariable()) ) {
					ReactorObservation r = new ReactorObservation(e, lastChangingStatement);
					execList.add(e);
					hasReactorList.add(e);
					this.reactors.add(r);
				}
			}
		}
				
		execCSP = new IncrementalSTPSolver(0, tMax);
//		execCSP = new MetaCSPAdapter(tMax);
		simCSP = new IncrementalSTPSolver(0,tMax);
		
		/*
		 * Add some new type, statements and constraints about progress of time
		 */
		tM.addSimpleEnumType("timeReference", "past,future");
		
		
		this.tM.attachTypes(tHorizon, Term.createConstant("timeReference") );
		
		
		execDB.add(past);
		execDB.add(future);
		execDB.add(rPast);
		execDB.add(rFuture);
		execDB.add(mPastFuture);
		execDB.add(dFuture);
		
		simDB.add(past);
		simDB.add(future);
		simDB.add(rPast);
		simDB.add(rFuture);
		simDB.add(mPastFuture);
		simDB.add(dFuture);
		
		if ( !execCSP.isConsistent(execDB, this.tM) ) {
			throw new IllegalStateException("Execution failure: Temporal inconsistency in execution CDB.");
		}
		
		if ( !simCSP.isConsistent(simDB, tM) ) {
			throw new IllegalStateException("Execution failure: Temporal inconsistency in simulation CDB.");
		}
		/*
		 * Create simulation reactors
		 */ 
		for ( Statement s : execList ) {
			if ( !hasReactorList.contains(s) ) {
				if ( perfectSim ) reactors.add(new ReactorPerfectSimulation(s));
				else reactors.add(new ReactorRandomSimulation(s));
			}
		}
		for ( Statement s : simList ) {
			reactors.add(new ReactorPerfectSimulation(s));
		}
		
		testCore.setTypeManager(tM);
		testCore.setOperators(core.getOperators());
		testCore.setPlan(core.getPlan().copy());
		
		while ( !this.isDone() ) {		
			long before = 0;
			try {
				before = StopWatch.getLast(msg("Update"));
			} catch ( NullPointerException e ) {
				before = 0;
			}
			if ( keepTimes ) StopWatch.start(msg("Update"));
			this.update();			
			if ( keepTimes ) StopWatch.stop(msg("Update"));
			if( keepTimes && keepStats ) Statistics.addLong(msg("Update"), (StopWatch.getLast(msg("Update"))-before));
			

		}
		
		
		core.setResultingState(this.getName(), State.Consistent);
		core.setContext(execDB.copy());
		core.setPlan(this.plan);
		if ( verbose ) Logger.depth--;		
		return core;		
	}
	
	private boolean isDone() {
		return (t+1) >= tMax;
	}
	
	boolean newInformationReleased = false;
	
	private void update( ) {
		/************************************************************************************************
		 * Update time
		 ************************************************************************************************/
		if ( useRealTime ) {
			t = System.currentTimeMillis() - t0;
		} else {
			t++;
		}

		if ( verbose ) Logger.landmarkMsg(this.getName());
		if ( verbose ) Logger.msg(getName(), "@t=" + t, 1);
		
		/************************************************************************************************
		 * Dispatch new information (from simulation)
		 ************************************************************************************************/
		boolean newInformationDispatched = newInformationReleased;
		newInformationReleased = false;
		if ( dispatchedDBs.get(t) != null ) {
			if ( verbose ) Logger.msg(getName(), "Dispatching:\n" + dispatchedDBs.get(t), 1);
			execDB.add(dispatchedDBs.get(t));
			addedSimDBs.add(dispatchedDBs.get(t));
			newInformationDispatched = true;
		}
		
		execDB.remove(rFuture);
		simDB.remove(rFuture);
		rFuture = new AllenConstraint(new Atomic("(deadline past (interval "+(t)+" "+(t)+"))"));
		execDB.add(rFuture);
		simDB.add(rFuture);
		
		/************************************************************************************************
		 * From scratch propagation to handle new information
		 ************************************************************************************************/
		if ( newInformationDispatched ) {
			long before = 0;
			try {
				before = StopWatch.getLast(msg("Replanning"));
			} catch ( NullPointerException e ) {
				before = 0;
			}
			if ( keepTimes ) StopWatch.start(msg("Replanning"));
//			testCore.setContext(execDB.copy());
//			testCore = repairSolver.run(testCore);
			
			boolean needFromScratch = true;
			
//			needFromScratch = testCore.getResultingState(repairSolverName).equals(Core.State.Inconsistent);
			
			if ( !needFromScratch ) {
				for ( OpenGoal og : testCore.getContext().get(OpenGoal.class) ) {
					if ( !og.isAsserted() ) {
						needFromScratch = true;
						break;
					}
				}
			}
			
			if ( needFromScratch ) {
				if ( verbose ) Logger.msg(getName(), "Re-planning from scratch...", 1);
										
				ConstraintDatabase fromScratch = this.getFromScrathDB();
				
				execCSP.isConsistent(fromScratch, tM);
								
				Core fromScratchCore = new Core();
				fromScratchCore.setTypeManager(tM);
				fromScratchCore.setOperators(this.O);
				fromScratchCore.setPlan(new Plan());
				fromScratchCore.setContext(fromScratch.copy());
								
				fromScratchCore = fromScratchSolver.run(fromScratchCore);
				
				
				ConstraintDatabase fromScratchSolution = fromScratchCore.getContext();
				
				
				
				if ( fromScratchCore.getResultingState(fromScratchSolverName).equals(Core.State.Inconsistent) ) {
					throw new IllegalStateException("Inconsistency when planning from scratch during execution.");
				}
				
				
				for ( Operator a : fromScratchCore.getPlan().getActions() ) {
					fromScratchSolution.add(new AllenConstraint(a.getNameStateVariable().getKey(), TemporalRelation.Release, new Interval(Term.createInteger(t), Term.createConstant("inf"))));
				}
		
				execDB = fromScratchSolution;

				this.plan = fromScratchCore.getPlan();
				List<Reactor> remList = new ArrayList<Reactor>();
				for ( Reactor r : reactors ) {
//					System.out.println("Considering: "  +r);
//					for ( Statement s : execList ) {
//						System.out.println(r.target + " == " + s + " ? " + r.target.equals(s));
//					}
//					System.out.println(execList.contains(r.target));
					
					if ( execList.contains(r.getTarget()) && !startedList.contains(r.getTarget()) ) {
						remList.add(r);
					}
				}
				reactors.removeAll(remList);
				execList.clear();
				
				execCSP = new IncrementalSTPSolver(0,this.tMax);
			}
			if ( keepTimes ) StopWatch.stop(msg("Replanning"));
			if( keepTimes && keepStats ) Statistics.addLong(msg("Replanning"), (StopWatch.getLast(msg("Replanning"))-before));
		}
		
		long before = 0;
		try {
			before = StopWatch.getLast(msg("Temporal propagation"));
		} catch ( NullPointerException e ) {
			before = 0;
		}
		
		/**
		 * TODO: set this up in a way that makes it work (keep checking ICs for contingencies and add new reactors)
		 */
		if ( !newInformationDispatched ) {
			int numConstraintsBefore = execDB.size();
			
			Core core = new Core();
			core.setTypeManager(tM);
			core.setOperators(this.O);
			core.setPlan(new Plan());
			core.setContext(execDB);
			core = fromScratchSolver.run(core);
			
			execDB = core.getContext();
			
			if ( execDB.size() != numConstraintsBefore ) {
				newInformationDispatched = true;
			}
		}
		
		/************************************************************************************************
		 * Temporal propagation
		 ************************************************************************************************/
		if ( keepTimes ) StopWatch.start(msg("Temporal propagation"));
		if ( !execCSP.isConsistent(execDB, this.tM) ) {
			if ( verbose ) Logger.msg(getName(), "Temporal inconsistency during execution propagation. Will try to re-plan from scratch. ", 1);
	
			IncrementalSTPSolver csp = new IncrementalSTPSolver(0, tMax);
			csp.debug = true;
			csp.isConsistent(execDB, tM);
			
			Loop.start();
			
			
			ConstraintDatabase fromScratch = this.getFromScrathDB();
			
			Core fromScratchCore = new Core();
			fromScratchCore.setTypeManager(tM);
			fromScratchCore.setOperators(this.O);
			fromScratchCore.setPlan(new Plan());
			fromScratchCore.setContext(fromScratch.copy());
			fromScratchCore = fromScratchSolver.run(fromScratchCore);
							
			ConstraintDatabase fromScratchSolution = fromScratchCore.getContext();
			
			if ( fromScratchCore.getResultingState(fromScratchSolverName).equals(Core.State.Inconsistent) ) {
				throw new IllegalStateException("Inconsistency when planning from scratch during execution.");
			}
			
			for ( Operator a : fromScratchCore.getPlan().getActions() ) {
				fromScratchSolution.add(new AllenConstraint(a.getNameStateVariable().getKey(), TemporalRelation.Release, new Interval(Term.createInteger(t), Term.createConstant("inf"))));
			}
	
			execDB = fromScratchSolution;

			this.plan = fromScratchCore.getPlan();

			List<Reactor> remList = new ArrayList<Reactor>();
//			System.out.println("Replanning after temporal inconsistency: ");
			for ( Reactor r : reactors ) {
				if ( execList.contains(r.getTarget()) && !startedList.contains(r.getTarget()) ) {
//					System.out.println("Removing: "  +r);
					remList.add(r);
				}
			}
			reactors.removeAll(remList);
			execList.clear();
			
			newInformationDispatched = true;
			
			if ( !execCSP.isConsistent(execDB, this.tM) ) {
				IncrementalSTPSolver csp1 = new IncrementalSTPSolver(0, this.tMax);
				csp1.debug = true;
				csp1.isConsistent(execDB, this.tM);
		
				throw new IllegalStateException("Execution failure: Temporal inconsistency in execution CDB.");
			}	
		} else {
//			System.out.println("================================");
//			System.out.println(execDB.getStatements().toString().replace(",", "\n"));
			TemporalNetworkTools.dumbTimeLineData(execDB, execCSP.getPropagatedTemporalIntervals(), "execution@"+t);
		}
		
		if ( !simDB.isEmpty() && !simCSP.isConsistent(simDB, tM) ) {
			throw new IllegalStateException("Execution failure: Temporal inconsistency in simulation CDB.");
		}
		if ( keepTimes ) StopWatch.stop(msg("Temporal propagation"));
		if( keepTimes && keepStats ) Statistics.addLong(msg("Temporal propagation"), (StopWatch.getLast(msg("Temporal propagation"))-before));
		
		/************************************************************************************************
		 * Add reactors if needed
		 ************************************************************************************************/
		if ( newInformationDispatched ) {
			hasReactorList.clear();
			execList.clear();
			
			for ( Reactor r : this.reactors ) {
				hasReactorList.add(r.getTarget());
				execList.add(r.getTarget());
			}
			
			/* ROS goals */
			for ( ROSGoal rosGoal :  execDB.get(ROSGoal.class) ) {
				for ( Statement s : execDB.get(Statement.class) ) {
					if ( !execList.contains(s) && !doneList.contains(s) && !hasReactorList.contains(s) ) {
						if ( hasReactorList.contains(s) ) {
							throw new IllegalStateException("Statement " + s + " has multiple reactors... This cannot be good!");
						}
						

						Substitution subst = rosGoal.getVariable().match(s.getVariable());
						if ( subst != null ) {
							ROSGoal goalCopy = rosGoal.copy();
							goalCopy.substitute(subst);
							hasReactorList.add(s);
							execList.add(s);
							ReactorROS reactor = new ReactorROS(s, goalCopy);
							this.reactors.add(reactor);
						}
					}
				}
			}
			
			Logger.msg(getName(), "execList:", 1);
			Logger.depth++;
			for ( Statement s : execList )
				Logger.msg(getName(), s.toString(), 1);
			Logger.depth--;
			Logger.msg(getName(), "doneList:", 1);
			Logger.depth++;
			for ( Statement s : doneList )
				Logger.msg(getName(), s.toString(), 1);
			Logger.depth--;
			Logger.msg(getName(), "hasReactorList:", 1);
			Logger.depth++;
			for ( Statement s : hasReactorList )
				Logger.msg(getName(), s.toString(), 1);
			Logger.depth--;
			
			Logger.msg(getName(), "startedList:", 1);
			Logger.depth++;
			for ( Statement s : startedList )
				Logger.msg(getName(), s.toString(), 1);
			Logger.depth--;
			
			/* Operator reactors */
			for ( Operator a : this.executedActions ) {
				Statement opName = a.getNameStateVariable();
				if ( !execList.contains(opName) && !doneList.contains(opName) && !hasReactorList.contains(opName) ) {
					
					if ( execCSP.getLST(opName.getKey()) < t ) {
						throw new IllegalStateException("Newly added operator " + opName + " has latest start time "+execCSP.getLST(opName.getKey())+" in the past (<"+t+")");
					}
					
					Reactor r;
					if ( perfectSim ) r = new ReactorPerfectSimulation(opName);
					else r = new ReactorRandomSimulation(opName);
					
					reactors.add(r);
					if ( verbose ) Logger.msg(getName(), "@t=" + t + ": Adding reactor " + r, 1);
					execList.add(opName);
				}
			}
			/* Operator reactors */
			for ( Operator a : this.plan.getActions() ) {
				Statement opName = a.getNameStateVariable();
				if ( !execList.contains(opName) && !doneList.contains(opName) && !hasReactorList.contains(opName) ) {
					
					if ( execCSP.getLST(opName.getKey()) < t ) {
						throw new IllegalStateException("Newly added operator " + opName + " has latest start time "+execCSP.getLST(opName.getKey())+" in the past (<"+t+")");
					}
					
					Reactor r;
					if ( perfectSim ) r = new ReactorPerfectSimulation(opName);
					else r = new ReactorRandomSimulation(opName);
					
					reactors.add(r);
					if ( verbose ) Logger.msg(getName(), "@t=" + t + ": Adding reactor " + r, 1);
					execList.add(opName);
				}
			}
			/* Observable effect reactors */
			for ( Operator a : this.plan.getActions() ) {
				for ( Statement e : a.getEffects() ) {
					if ( !execList.contains(e) && !doneList.contains(e) && !hasReactorList.contains(e) ) {
						if ( variablesObservedByROS.contains(e.getVariable()) ) {
							ReactorObservation r = new ReactorObservation(e, lastChangingStatement);
							execList.add(e);
							hasReactorList.add(e);
							this.reactors.add(r);
						}
					}
				}
			}
			/* Observable effect reactors */
			for ( Operator a : this.executedActions ) {
				for ( Statement e : a.getEffects() ) {
					if ( !execList.contains(e) && !doneList.contains(e) && !hasReactorList.contains(e) ) {
						if ( variablesObservedByROS.contains(e.getVariable()) ) {
							ReactorObservation r = new ReactorObservation(e, lastChangingStatement);
							execList.add(e);
							hasReactorList.add(e);
							this.reactors.add(r);
						}
					}
				}
			}
			/* Speech reactors */
			for ( Statement s : execDB.get(Statement.class) ) {
				if ( s.getVariable().getUniqueName().equals("say/1")) {
					if ( !execList.contains(s) && !doneList.contains(s) && !hasReactorList.contains(s) ) {
						String text = "";
						for ( IncludedProgram ip : execDB.get(IncludedProgram.class) ) {
							if ( s.getVariable().getArg(0).equals(ip.getName())) {
								text += ip.getCode();
							}
						}
						
						if ( text.isEmpty() ) {
							text = s.getValue().toString();
						}
						
						execList.add(s);
						hasReactorList.add(s);
						ReactorSoundPlaySpeech reactor = new ReactorSoundPlaySpeech(s,text);
						this.reactors.add(reactor);
					}
				}
			}
		}
		/************************************************************************************************
		 * Check ROS
		 ************************************************************************************************/
		if ( !firstUpdate ) 
			newInformationReleased = newInformationReleased || updateROS(execDB);
		
		/************************************************************************************************
		 * Update all reactors 
		 ************************************************************************************************/
		ArrayList<Reactor> remList = new ArrayList<Reactor>();
		boolean someoneDone = false;
		for ( Reactor r : reactors ) {
			
			r.printSetting(name, Logger.depth, verbose);
			if ( r.getState() == Reactor.State.Done ) {
				doneList.add(r.getTarget());
				remList.add(r);
				someoneDone = true;
				
			} else {
				Collection<Expression> addedCons;
				long EST, LST, EET, LET;
				
				if ( !simList.contains(r.getTarget()) ) {
					EST = execCSP.getEST(r.getTarget().getKey());
					LST = execCSP.getLST(r.getTarget().getKey());
					EET = execCSP.getEET(r.getTarget().getKey());
					LET = execCSP.getLET(r.getTarget().getKey());
					
					if ( verbose ) Logger.msg(getName(), "@t=" + t + " (BEFORE) >>> " + r, 1);
					addedCons = r.update(t, EST, LST, EET, LET, execDB);
					if ( verbose ) Logger.msg(getName(), "@t=" + t + " (AFTER)  >>> " + r, 1);

				} else {
					EST = simCSP.getEST(r.getTarget().getKey());
					LST = simCSP.getLST(r.getTarget().getKey());
					EET = simCSP.getEET(r.getTarget().getKey());
					LET = simCSP.getLET(r.getTarget().getKey());
					addedCons = r.update(t, EST, LST, EET, LET, simDB);
				}
				
				
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
							execDB.removeAll(addedConstraints.get(r.getTarget()));
							addedOnReleaseDB.removeAll(addedConstraints.get(r.getTarget()));
						}
						execDB.addAll(addedCons);
						addedOnReleaseDB.addAll(addedCons);
						if ( !execDB.contains(r.getTarget()) ) {
							addedOnReleaseDB.add(r.getTarget());
							execDB.add(r.getTarget());
						}
						newInformationReleased = true;
					}
				}
				Collection<Expression> store = new ArrayList<Expression>();
				store.addAll(addedCons);
				addedConstraints.put(r.getTarget(), store);
				
			}
		}
		reactors.removeAll(remList);
		
		/************************************************************************************************
		 * Forget past
		 ************************************************************************************************/
		if ( someoneDone && useForgetting ) {
			removeWrittenInStone( execDB, new HashSet<Term>() );
		}

		/************************************************************************************************
		 * Update visualization
		 ************************************************************************************************/	
		if ( firstUpdate && drawTimeLines ) {
//			execCSP.isConsistent(execDB, tM);
			firstUpdate = false;
			this.draw();
		}
		if ( timeLineViewer != null ) {
			
			for ( Statement s : execDB.get(Statement.class) ) {
				try {
					String tName = s.getVariable().toString();
					String value = s.getValue().toString(); 
					Term id = s.getKey();
					long[] bounds = execCSP.getBoundsArray(id);
					
					if ( ! timeLineViewer.hasTrack(tName) ) {
						timeLineViewer.createTrack(tName);
					}
					
					if ( ! timeLineViewer.hasValue(id.toString()) ) {
						timeLineViewer.createValue(tName, value, id.toString(), (int)bounds[0], (int)bounds[2]);
					} else {
						timeLineViewer.updateValue(id.toString(), (int)bounds[0], (int)bounds[2]);
					}		
				} catch ( NullPointerException e ) {}
	
			}
			timeLineViewer.update();
			timeLineViewer.snapshot();
		}
		
//		System.out.println(StopWatch.allLast2Str());
		
		/************************************************************************************************
		 * Force some delay (TODO: adapt when doing real time)
		 ************************************************************************************************/
		try {
			Thread.sleep(250);
		} catch ( Exception e ) {
			
		}
	}
	
	int fromScratchDBsCreated = 0;
	private ConstraintDatabase getFromScrathDB( ) {
		fromScratchDBsCreated++;
		
		for ( OpenGoal og : execDB.get(OpenGoal.class) ) {
			execDB.add(og.getStatement());
		}
		
		if ( !execCSP.isConsistent(execDB, tM) ) {
			IncrementalSTPSolver csp = new IncrementalSTPSolver(0, this.tMax);
			csp.debug = true;
			csp.isConsistent(execDB, tM);
			
			for ( Statement s : execDB.get(Statement.class) ) {
				System.out.println("[S] " + s);
			}
			for ( AllenConstraint tc : execDB.get(AllenConstraint.class) ) {
				System.out.println("[T] " + tc);
			}	
			
			throw new IllegalStateException("This should not happen!");
		}
				
		Set<Term> actionIntervals = new HashSet<Term>();
		Set<Term> nonExecutedActionIntervals = new HashSet<Term>();
		
		if ( verbose ) { 
			Logger.msg(getName(), "Building from scratch CDB:", 0);
			Logger.depth++;
			Logger.msg(getName(), "Checking which operators to keep...", 2);
			Logger.depth++;
		}
		for ( Operator a : this.plan.getActions() ) {
			if ( !executedActions.contains(a) && (this.startedList.contains(a.getNameStateVariable()) || this.doneList.contains(a.getNameStateVariable())) ) {
				if ( verbose ) Logger.msg(getName(), a.getNameStateVariable().toString(), 2);
				executedActions.add(a.copy());
				
				actionIntervals.add(a.getNameStateVariable().getKey());
				for ( Statement p : a.getPreconditions() ) {
					actionIntervals.add(p.getKey());
				}
				for ( Statement e : a.getEffects() ) {
					actionIntervals.add(e.getKey());
				}
			} else if ( !executedActions.contains(a) ) {
				if ( verbose ) Logger.msg(getName(), "Won't keep: " + a.getNameStateVariable().toString(), 2);
				nonExecutedActionIntervals.add(a.getNameStateVariable().getKey());
				for ( Statement p : a.getPreconditions() ) {
					nonExecutedActionIntervals.add(p.getKey());
				}
				for ( Statement e : a.getEffects() ) {
					nonExecutedActionIntervals.add(e.getKey());
				}
			}
		}
				
		ConstraintDatabase fromScratchDB = initialContext.copy();
		
		if ( verbose ) { 
			Logger.depth--;
			if ( verbose ) Logger.msg(getName(), "nonExecutedActionIntervals = " + nonExecutedActionIntervals.toString(), 2);
			Logger.msg(getName(), "Checking which statements to keep...", 2);
			Logger.depth++;
		}
		for ( Statement s : execDB.get(Statement.class) ) { 

			if ( !nonExecutedActionIntervals.contains(s.getKey()) &&  ((execCSP.getLST(s.getKey()) < t || execCSP.getLET(s.getKey()) < t)) ) {
				long EST = execCSP.getEST(s.getKey());
				long LST = execCSP.getLST(s.getKey());
				long EET = execCSP.getEET(s.getKey());
				long LET = execCSP.getLET(s.getKey());
				if ( LET < t || t > LST ) {
					if ( !s.getKey().toString().equals("past") )  {
						if ( verbose ) Logger.msg(getName(), s.toString() + " " + Interval2String(EST, LST, EET, LET) , 2);		
						Interval b1 = new Interval(EST,LST);
						Interval b2 = new Interval(EET,LET);
						AllenConstraint atCon = new AllenConstraint(s.getKey(), TemporalRelation.At, b1, b2);
						
						fromScratchDB.add(s);
						fromScratchDB.add(atCon);
					}
				} else {
					if ( verbose ) Logger.msg(getName(), "...not keeping it.", 2);
				}
			}
		}
		
		PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(fromScratchDB);
		fromScratchDB.remove(pI);
		fromScratchDB.add(new PlanningInterval(Term.createInteger(t), Term.createInteger(tMax)));
		
		for ( Statement s : fromScratchDB.get(Statement.class) ) {
			actionIntervals.add(s.getKey());
		}
		
		
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(), "Checking causal links that have been executed...", 2);
			Logger.depth++;
		}
		for ( AllenConstraint tc : this.plan.getConstraints().get(AllenConstraint.class) ) {
			if ( !executedLinks.contains(tc) && (actionIntervals.contains(tc.getFrom()) && actionIntervals.contains(tc.getTo())) ) {
				if ( execDB.hasKey(tc.getFrom()) && execDB.hasKey(tc.getTo())) {
					if ( verbose ) Logger.msg(getName(), tc.toString(), 2);
					executedLinks.add(tc);
				} else {
					if ( verbose ) Logger.msg(getName(), "Not keeping " + tc, 3);
				}
			} else {
				if ( verbose ) Logger.msg(getName(), "Not keeping " + tc, 3);
			}
			
		}
		
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(), "Checking open goals that have been achieved...", 2);
			Logger.depth++;
		}
		for ( OpenGoal og : execDB.get(OpenGoal.class)) {
			System.out.println(og);
			if ( execDB.hasKey(og.getStatement().getKey())) {
				if ( verbose ) Logger.msg(getName(), "Goal " + og + " with " + og.getStatement().getKey() 
																	+ " [" + execCSP.getEST(og.getStatement().getKey()) 
																	+ " " + execCSP.getLST(og.getStatement().getKey())
																	+ "] [" + execCSP.getEET(og.getStatement().getKey())
																	+ " " + execCSP.getLET(og.getStatement().getKey()) + "]", 2);
				
				
				boolean connectedToExecutedEffect = false;
				for ( AllenConstraint ac : this.plan.getConstraints().get(AllenConstraint.class ) ) {
					if ( ac.getFrom().equals(og.getStatement().getKey()) && actionIntervals.contains(ac.getTo()) ) {
						connectedToExecutedEffect = true; 
						executedLinks.add(ac);
						break;
					}
					
					if ( ac.getTo().equals(og.getStatement().getKey()) && actionIntervals.contains(ac.getFrom()) ) {
						connectedToExecutedEffect = true;
						executedLinks.add(ac);
						break;
					}
				}
				
				
				if ( execCSP.getEST(og.getStatement().getKey()) <= t || connectedToExecutedEffect ) {
					if ( verbose ) Logger.msg(getName(), "... was already achieved.", 2);
//					fromScratchDB.remove(og);
					OpenGoal ogCopy = og.copy();
					ogCopy.setAsserted(true);
					reachedGoals.add(new Asserted(ogCopy));
					reachedGoals.add(og.getStatement());
					reachedGoals.add(ogCopy);
				} else {
					if ( verbose ) Logger.msg(getName(), "... will be added again.", 2);
				}
			}
		}
		fromScratchDB.addAll(reachedGoals);
		

				
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(), "Adding operators...", 2);
			Logger.depth++;
		}		
		Collection<Operator> oRemList = new HashSet<Operator>();
		for ( Operator a : executedActions ) {
			if ( execDB.hasKey(a.getNameStateVariable().getKey()) ) {
				if ( verbose ) Logger.msg(getName(), a.getNameStateVariable().toString(), 2);
				long bounds[] = execCSP.getBoundsArray(a.getNameStateVariable().getKey());
				Interval[] intervals = new Interval[2];
				intervals[0] = new Interval(Term.createInteger(bounds[0]), Term.createInteger(bounds[1]));
				intervals[1] = new Interval(Term.createInteger(bounds[2]), Term.createInteger(bounds[3]));
				
				AllenConstraint aC = new AllenConstraint(a.getNameStateVariable().getKey(), TemporalRelation.At, intervals); 
						
				fromScratchDB.add(aC);
				
				fromScratchDB.add(a.getNameStateVariable());
				for ( Statement p : a.getPreconditions() ) {
					fromScratchDB.add(p);	
				}
//				fromScratchDB.addStatements(a.getPreconditions());
				for ( Statement e : a.getEffects() ) {
					fromScratchDB.add(e);	
				}
//				fromScratchDB.addStatements(a.getEffects());
				fromScratchDB.addAll(a.getConstraints());
				fromScratchDB.addAll(addedConstraints.get(a.getNameStateVariable()));
			} else {
				oRemList.add(a);
			}
		}
		executedActions.removeAll(oRemList);
		
		
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(), "Checking ICs that applied or have been resolved in the past", 2);
			Logger.depth++;
		}	
		for ( InteractionConstraint ic : execDB.get(InteractionConstraint.class) ) {
			
			/**
			 * TODO: This is a hack to fix bug where the index is lost at some point...
			 * If this is still a problem it has to be fixed in a different way now...
			 */
//			for ( Asserted c : execDB.get(Asserted.class) ) {
//				if ( c.appliesTo(ic) ) {
//					InteractionConstraint ic2 = (InteractionConstraint)c.getConstraint();
//					ic.setResolverIndex(ic2.getResolverIndex());
//				}
//			}
			
			if ( ic.isAsserted() ) {
				boolean allConditionStatementsInPast = true;
				
				for ( Statement s : ic.getCondition().get(Statement.class) ) {
					if ( !(execDB.hasKey(s.getKey()) && (execCSP.getLST(s.getKey()) < t || execCSP.getLET(s.getKey()) < t)) ) {
						allConditionStatementsInPast = false;
						break;
					}
				}
				
				boolean allResolverStatementsInPast = true;
				for ( Statement s : ic.getResolvers().get(ic.getResolverIndex()).get(Statement.class) ) {
					if ( !(execDB.hasKey(s.getKey()) && (execCSP.getLST(s.getKey()) < t || execCSP.getLET(s.getKey()) < t)) ) {
						allResolverStatementsInPast = false;
						break;
					}
				}
				
				if ( allConditionStatementsInPast || allResolverStatementsInPast ) {
					if ( verbose ) Logger.msg(getName(), ic.getName().toString(), 2);
					fromScratchDB.add(ic);
					fromScratchDB.add(ic.getResolvers().get(ic.getResolverIndex()));
				}
			}
		}
		
		for ( Expression c : reachedGoals ) {
			if ( c instanceof Asserted ) {
				fromScratchDB.processAsserted((Asserted)c);			
			}
		}
				
		if ( verbose ) Logger.depth--;
		
		fromScratchDB.addAll(executedLinks);
		fromScratchDB.add(addedSimDBs);
		fromScratchDB.add(addedOnReleaseDB);
		fromScratchDB.add(addedByROS);

		fromScratchDB.add(past);
		fromScratchDB.add(future);
		fromScratchDB.add(rPast);
		fromScratchDB.add(rFuture);
		fromScratchDB.add(mPastFuture);
		fromScratchDB.add(dFuture);
		
		for ( Statement s : this.startedList ) {
			fromScratchDB.addAll(addedConstraints.get(s));
		}
		for ( Statement s : this.doneList ) {
			fromScratchDB.addAll(addedConstraints.get(s));
		}
		
		for ( Term k : nonExecutedActionIntervals ) {
			if ( fromScratchDB.hasKey(k) ) {
				throw new IllegalStateException("Interval " + k  + " has no business here (this should never happen).");
			} 
		}
		
//		System.out.println("==================================");
//		System.out.println("INIT DB: ");
//		System.out.println("==================================");
//		for ( Statement s : Global.initialContext.getStatements() ) {
//			System.out.println("[S] " + s);
//		}
//		for ( OpenGoal s : Global.initialContext.get(OpenGoal.class) ) {
//			System.out.println("[G] " + s);
//		}
//		for ( AllenConstraint tc : Global.initialContext.get(AllenConstraint.class) ) {
//			System.out.println("[T] " + tc);
//		}	
//		System.out.println("==================================");
//		System.out.println("BEFORE: ");
//		System.out.println("==================================");
//		for ( Statement s : fromScratchDB.getStatements() ) {
//			System.out.println("[S] " + s);
//		}
//		for ( OpenGoal s : fromScratchDB.get(OpenGoal.class) ) {
//			System.out.println("[G] " + s);
//		}
//		for ( AllenConstraint tc : fromScratchDB.get(AllenConstraint.class) ) {
//			System.out.println("[T] " + tc);
//		}

		if ( useForgetting ) {
			removeWrittenInStone( fromScratchDB, nonExecutedActionIntervals );
		}
		
//		System.out.println("==================================");
//		System.out.println("FINALLY: ");
//		System.out.println("==================================");
//		for ( Statement s : fromScratchDB.getStatements() ) {
//			System.out.println("[S] " + s);
//		}
//		for ( OpenGoal s : fromScratchDB.get(OpenGoal.class) ) {
//			System.out.println("[G] " + s + " " + s.isAsserted());
//		}
//		for ( AllenConstraint tc : fromScratchDB.get(AllenConstraint.class) ) {
//			System.out.println("[T] " + tc);
//		}

		if ( verbose ) Logger.depth--;
		
		return fromScratchDB;
	}
	
	private String Interval2String( long EST, long LST, long EET, long LET) {
		return String.format("[%d %d] [%d %d]", EST, LST, EET, LET);
	}
	
	Set<Expression> remList = new HashSet<Expression>();
	Set<Statement> writtenInStoneStatements = new HashSet<Statement>();	
	Set<Term> writtenInStone = new HashSet<Term>();
	
	
	private void removeWrittenInStone( ConstraintDatabase cdb, Set<Term> doNotAdd ) {
//		if ( !execCSP.isConsistent(execDB, tM) ) {
//			throw new IllegalStateException();
//		}
		
		if ( verbose ) { 
			Logger.msg(getName(),"Searching for fixed statements...", 2);
			Logger.depth++;
		}

		Set<Expression> writtenInStoneConstraints = new HashSet<Expression>();
		
		for ( Statement s : execDB.get(Statement.class) ) {
			if ( execCSP.hasInterval(s.getKey()) && !doNotAdd.contains(s.getKey()) ) { //TODO: work-around 
				long EST = execCSP.getEST(s.getKey());
				long LST = execCSP.getLST(s.getKey());
				long EET = execCSP.getEET(s.getKey());
				long LET = execCSP.getLET(s.getKey());
				
				if ( LET < t || t > LST ) {
					if ( !s.getKey().toString().equals("past") && !execList.contains(s) )  {
						
						cdb.add(s); 
						Interval b1 = new Interval(EST,LST);
						Interval b2 = new Interval(EET,LET);
						AllenConstraint atCon = new AllenConstraint(s.getKey(), TemporalRelation.At, b1, b2); 
						cdb.add(atCon);
						
						if ( EST == LST && EET == LET ) {
							if ( verbose ) Logger.msg(getName(), s + " " + Interval2String(EST, LST, EET, LET), 2);
							writtenInStone.add(s.getKey());
							writtenInStoneConstraints.add(atCon);
						}
					}
				} 
			}
		}
			
		/**
		 * Temporal constraints between two "written in stone" statements have already been 
		 * propagated and can be removed (since we add the (at ...) constraint above.
		 */

		Set<Term> connectedToOutside = new HashSet<Term>();
		for ( AllenConstraint ac : cdb.get(AllenConstraint.class)) {
			if ( ac.isBinary() && writtenInStone.contains(ac.getFrom()) && writtenInStone.contains(ac.getTo()) ) {
				remList.add(ac);
			} else if ( ac.isUnary() && writtenInStone.contains(ac.getFrom()) ) {
				remList.add(ac);
			} else {
				connectedToOutside.add(ac.getFrom());
				if ( ac.isBinary() ) {
					connectedToOutside.add(ac.getTo());
				}
			}
		}
		
		/**
		 * Remove all statements that are only connected to other "written in stone"
		 * statements. (They may be a problem for ICs that rely on past statements.) 
		 */
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(),"The following statements will be removed...", 2);
			Logger.depth++;
		}
		for ( Statement s : cdb.get(Statement.class) ) {
			if ( writtenInStone.contains(s.getKey()) && !connectedToOutside.contains(s.getKey()) ) {
				if ( verbose ) Logger.msg(getName(), s.toString(), 2);
				remList.add(s);
				
				for ( AllenConstraint ac : cdb.get(AllenConstraint.class)) {
					if ( ac.getFrom().equals(s.getKey()) || (ac.isBinary() && ac.getTo().equals(s.getKey()))) {
						remList.add(ac);
						writtenInStoneConstraints.remove(ac);
						executedLinks.remove(ac);
					}
				}				
			} 
		}
		
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(),"Statistics:", 2);
			Logger.depth++;
		}
		

		
		int beforeAC =  (cdb.get(AllenConstraint.class).size());
		int beforeStatements = (cdb.get(Statement.class).size());
		if ( verbose ) Logger.msg(this.getName(), "Number of constraints (before removal) " + beforeAC, 2);		
		if ( verbose ) Logger.msg(this.getName(), "Number of statements (before removal) " + beforeStatements, 2);
		cdb.removeAll(remList);
		cdb.addAll(writtenInStoneConstraints);
		
		remList.clear();
		for ( AllenConstraint ac : cdb.get(AllenConstraint.class)) {
			if ( !cdb.hasKey(ac.getFrom()) || (ac.isBinary() && !cdb.hasKey(ac.getTo())) ) {
				remList.add(ac);
			}
		}				
		cdb.removeAll(remList);
		
		int afterAC =  (cdb.get(AllenConstraint.class).size());
		int afterStatements = (cdb.get(Statement.class).size());		
				
//		if ( keepStats ) Statistics.addLong(msg("FromScratchDB #" +fromScratchDBsCreated+ " removed written in stone"), (long) (remList.size()-writtenInStoneConstraints.size()));
		if ( verbose ) {
			Logger.msg(this.getName(), "Removed " + (beforeAC-afterAC) + " temporal constraints whose intervals are fixed anyways." , 2);
			Logger.msg(this.getName(), "Removed " + (beforeStatements-afterStatements) + " statements whose intervals are fixed anyways." , 2);
			Logger.msg(this.getName(), "Final number of temporal constraints " + (long) (cdb.get(AllenConstraint.class).size())  , 2);
			Logger.msg(this.getName(), "Final number of statements " + (cdb.get(Statement.class).size()) , 2);
			Logger.msg(this.getName(), "Final number of constraints " + (long) (cdb.size()) , 2);
			Logger.depth--;
		}
	}
	
//	private ConstraintDatabase getRepairDB( ) {
//		ArrayList<Operator> executedActions = new ArrayList<Operator>();
//		ArrayList<Constraint> executedLinks = new ArrayList<Constraint>();
//		Set<Term> actionIntervals = new HashSet<Term>();
//		
//		if ( verbose ) Logger.msg(getName(), "Building initial context...", 3);
//		for ( Operator a : this.plan.getActions() ) {
//			if ( this.startedList.contains(a.getNameStateVariable()) || this.doneList.contains(a.getNameStateVariable()) ) {
//				if ( verbose ) Logger.msg(getName(), "    Adding " + a, 3);
//				executedActions.add(a);
//				
//				actionIntervals.add(a.getNameStateVariable().getKey());
//				for ( Statement p : a.getPreconditions() ) {
//					actionIntervals.add(p.getKey());
//				}
//				for ( Statement e : a.getEffects() ) {
//					actionIntervals.add(e.getKey());
//				}
//			} 
//		}
//		
//		for ( AllenConstraint tc : this.plan.getConstraints().get(AllenConstraint.class) ) {
//			if ( actionIntervals.contains(tc.getFrom()) && actionIntervals.contains(tc.getTo()) ) {
//				if ( verbose ) Logger.msg(getName(), "    Keeping " + tc, 3);
//				executedLinks.add(tc);
//			}
//		}
//		
//		ConstraintDatabase fromScratchDB = Global.initialContext.copy();
//		
//		if ( verbose ) Logger.msg(getName(), "Collecting achieved goals...", 3);
//		for ( OpenGoal og : execDB.get(OpenGoal.class)) {
//			if ( verbose ) Logger.msg(getName(), "Setting goal " + og + " as asserted.", 3);
//			fromScratchDB.add(new Asserted(og.copy()));
//			fromScratchDB.add(og.getStatement());
//		}
//		
//		for ( Operator a : executedActions ) {
//			fromScratchDB.add(a.getNameStateVariable());
//			for ( Statement p : a.getPreconditions() ) {
//				fromScratchDB.add(p);
//			}
////			fromScratchDB.addStatements(a.getPreconditions());
//			for ( Statement e : a.getEffects() ) {
//				fromScratchDB.add(e);
//			}
////			fromScratchDB.addStatements(a.getEffects());
//			fromScratchDB.addAll(a.getConstraints());
//		}
//		fromScratchDB.addAll(executedLinks);
//		fromScratchDB.add(addedSimDBs);
////		fromScratchDB.addConstraints(addedConstraints);
//		return fromScratchDB;
//	}
	
	Map<Atomic,Statement> lastChangingStatement = new HashMap<Atomic, Statement>();
	Map<Atomic,Expression> lastAddedDeadline = new HashMap<Atomic, Expression>();
	List<ROSConstraint> ROSsubs = new ArrayList<ROSConstraint>();
	List<ROSConstraint> ROSpubs = new ArrayList<ROSConstraint>();
	Term rosSubInterval = Term.createVariable("?I_ROS");
	int ROS_NumSameValuesRequired = 2;
	int ROS_SameValueCounter = 0;
	Term ROS_NewValue = null;
	
	private boolean updateROS( ConstraintDatabase execDB ) {
		boolean change = false;
		Atomic variable;
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
				execDB.remove(lastAddedDeadline.get(variable));
				AllenConstraint deadline = new AllenConstraint(s.getKey(), TemporalRelation.Deadline, new Interval(Term.createInteger(t+1),Term.createConstant("inf")));
				addedByROS.add(deadline);
				execDB.add(deadline);
				
				lastAddedDeadline.put(variable, deadline);
				change = false;
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
						execDB.remove(lastAddedDeadline.get(variable));
						AllenConstraint finalDeadline = new AllenConstraint(oldAssignment.getKey(), TemporalRelation.Deadline, new Interval(t,t));
						addedByROS.add(finalDeadline);
						execDB.add(finalDeadline);
					}			
					//TODO not unique
					Term interval = rosSubInterval.makeUnique(UniqueID.getID()).makeConstant();
					Statement newStatement = new Statement(interval,variable,value);
					AllenConstraint release = new AllenConstraint(interval, TemporalRelation.Release, new Interval(t,t));
					AllenConstraint deadline = new AllenConstraint(interval, TemporalRelation.Deadline, new Interval(Term.createInteger(t+1),Term.createConstant("inf")));
					
					lastChangingStatement.put(variable, newStatement);
					lastAddedDeadline.put(variable, deadline);
					
					addedByROS.add(newStatement);
					addedByROS.add(release);
					addedByROS.add(deadline);
					execDB.add(newStatement);
					execDB.add(release);
					execDB.add(deadline);
					
					change = true;
					
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
			for ( Statement s : execDB.get(Statement.class) ) {
				if ( variable.equals(s.getVariable()) && execCSP.getEST(s.getKey()) <= t && execCSP.getEET(s.getKey()) >= t ) {
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
		
		
		return change;
	}
	
	/**
	 * Draw time-lines produced by execution 
	 */
	public void draw() {
		timeLineViewer = new TimeLineViewer();
		for ( Statement s : execDB.get(Statement.class) ) {
			String tName = s.getVariable().toString();
			String value = s.getValue().toString(); 
			Term id = s.getKey();
			if ( execCSP.hasInterval(id)) {
				long[] bounds = execCSP.getBoundsArray(id);
				
				if ( ! timeLineViewer.hasTrack(tName) ) {
					timeLineViewer.createTrack(tName);
				}
				timeLineViewer.createValue(tName, value, id.toString(), (int)bounds[0], (int)bounds[2]);
			}
		}
		timeLineViewer.update();
		timeLineViewer.snapshot();
	}
}

