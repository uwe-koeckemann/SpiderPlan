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

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.executor.Reactor;
import org.spiderplan.executor.ReactorObservation;
import org.spiderplan.executor.ReactorRandomSimulation;
import org.spiderplan.executor.ReactorSoundPlaySpeech;
import org.spiderplan.executor.ROS.ROSExecutionManager;
import org.spiderplan.executor.ROS.ROSProxy;
import org.spiderplan.executor.ROS.ReactorROS;
//import org.spiderplan.executor.database.DatabaseExecutionManager;
import org.spiderplan.executor.simulation.ReactorPerfectSimulation;
import org.spiderplan.executor.simulation.SimulationExecutionManager;
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
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.ROSRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.Simulation;
//import org.spiderplan.representation.expressions.execution.database.DatabaseExecutionExpression;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.execution.ros.ROSRegisterAction;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.expressions.sampling.SamplingConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;

/**
 * Executes a plan.
 * 
 * TODO: simulation constraints on release not firing?	
 * TODO: constraints to enable forgetting for statements
 * TODO: interface for connecting to outside (ROS, PEIS, Simulation, Ecare)
 * 
 * @author Uwe Köckemann
 * 
 */
public class ExecutionModuleMK2  extends Module {

	long t = 0;	
	long tMax = 1000;
	
	long t0;
	
	boolean useRealTime = false; //TODO: Real time does not work yet
//	boolean useForgetting = true;
	
	ConstraintDatabase execDB;	
	
	ConstraintDatabase initialContext;

	ArrayList<Reactor> reactors = new ArrayList<Reactor>();

	ArrayList<Operator> executedActions = new ArrayList<Operator>();
	ArrayList<Expression> executedLinks = new ArrayList<Expression>();
	ArrayList<Expression> reachedGoals = new ArrayList<Expression>();
	ArrayList<Statement> removedStatements = new ArrayList<Statement>();
	
	TypeManager tM;

	Atomic tHorizon = new Atomic("time");
	
	Statement past = new Statement(Term.createConstant("past"), tHorizon, Term.createConstant("past") );
	Statement future = new Statement(Term.createConstant("future"), tHorizon, Term.createConstant("future") );
	
	AllenConstraint rPast = new AllenConstraint(new Atomic("(release past (interval 0 0))"));
	AllenConstraint mPastFuture = new AllenConstraint(new Atomic("(meets past future)"));
	AllenConstraint dFuture = new AllenConstraint(new Atomic("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
	AllenConstraint rFuture = new AllenConstraint(new Atomic("(deadline past (interval 1 1)"));
	AllenConstraint mFuture;
	
//	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
//	private ConstraintDatabase addedSimDBs = new ConstraintDatabase();
//	private ConstraintDatabase addedOnReleaseDB = new ConstraintDatabase();
//	private ConstraintDatabase addedByROS = new ConstraintDatabase();
	
	boolean drawTimeLines = true;
	TimeLineViewer timeLineViewer = null;
		
	private String repairSolverName;
	private Module repairSolver = null;
//	
//	private String fromScratchSolverName;
//	private Module fromScratchSolver = null;
	
//	private Core testCore = new Core();
	
	boolean firstUpdate = true;
	
	Plan plan;
	Collection<Operator> O;
	
	List<ExecutionManager> managerList = new ArrayList<ExecutionManager>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public ExecutionModuleMK2(String name, ConfigurationManager cM ) {
		super(name, cM);
		
		if ( cM.hasAttribute(name, "repairSolver") ) {
			this.repairSolverName = cM.getString(this.getName(), "repairSolver" );
			this.repairSolver = ModuleFactory.initModule( this.repairSolverName , cM );
		}
		
//		if ( cM.hasAttribute(name, "fromScratchSolver") ) {
//			this.fromScratchSolverName = cM.getString(this.getName(), "fromScratchSolver" );
//			this.fromScratchSolver = ModuleFactory.initModule( this.fromScratchSolverName, cM );
//		}
	}
	
	@Override
	public Core run( Core core ) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;

		this.initialContext = core.getContext().copy();
		
		PlanningInterval pI = core.getContext().getUnique(PlanningInterval.class);
		
		this.t0 = 0;
		
		this.t = pI.getStartTimeValue();
		this.tMax = pI.getHorizonValue();
		
		this.tM = core.getTypeManager();
		this.O = core.getOperators();
		
		rPast = new AllenConstraint(new Atomic("(release past (interval 0 0))"));
		mPastFuture = new AllenConstraint(new Atomic("(meets past future)"));
		dFuture = new AllenConstraint(new Atomic("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
		rFuture = new AllenConstraint(new Atomic("(deadline past (interval 1 1)"));
				
		/*
		 * Add some new type, statements and constraints about progress of time
		 */
		tM.addSimpleEnumType("timeReference", "past,future");
		
		
		this.tM.attachTypes(tHorizon, Term.createConstant("timeReference") );
		
		execDB = core.getContext();
		
		execDB.add(past);
		execDB.add(future);
		execDB.add(rPast);
		execDB.add(rFuture);
		execDB.add(mPastFuture);
		execDB.add(dFuture);
		
//		testCore.setTypeManager(tM);
//		testCore.setOperators(core.getOperators());
//		testCore.getContext().add(core.getContext().getUnique(Plan.class).copy());
		
		if ( !execDB.get(ROSConstraint.class).isEmpty()
				|| !execDB.get(ROSGoal.class).isEmpty()
				|| !execDB.get(ROSRegisterAction.class).isEmpty() ) {
			if ( verbose ) Logger.msg(getName(), "Found ROS constraints. Initializing ROSExecutionManager.", 1);
			ExecutionManager rosManager = new ROSExecutionManager(this.getName());
			rosManager.setVerbose(verbose);
			rosManager.setVerbosity(verbosity);
			rosManager.initialize(execDB);
			managerList.add(rosManager);
		} 
		if ( !execDB.get(Simulation.class).isEmpty() ) {
			if ( verbose ) Logger.msg(getName(), "Found simulation constraints. Initializing SimulationExecutionManager.", 1);
			ExecutionManager simManager = new SimulationExecutionManager(this.getName());
			simManager.setVerbose(this.verbose);
			simManager.setVerbosity(verbosity);
			simManager.initialize(execDB);
			managerList.add(simManager);
		}
		// Removed from master because feature is not ready
		/*if ( !execDB.get(DatabaseExecutionExpression.class).isEmpty() ) {
			if ( verbose ) Logger.msg(getName(), "Found database execution expressions. Initializing DatabaseExecutionManager.", 1);
			ExecutionManager dbExecManager = new DatabaseExecutionManager(this.getName());
			dbExecManager.setVerbose(this.verbose);
			dbExecManager.setVerbosity(verbosity);
			dbExecManager.initialize(execDB);
			managerList.add(dbExecManager);
		} */
		
		while ( !this.isDone() ) {	
			this.update();			
		}
		
		
		core.setResultingState(this.getName(), State.Consistent);
		core.setContext(execDB.copy());

		if ( verbose ) Logger.depth--;		
		return core;		
	}
	
	private boolean isDone() {
		return (t+1) >= tMax;
	}
	
	boolean newInformationReleased = false;
	
	private void update( ) {

//		boolean needFromScratch = true;
		/************************************************************************************************
		 * Update time
		 ************************************************************************************************/
		t++;
		if ( verbose ) Logger.landmarkMsg(this.getName());
		if ( verbose ) Logger.landmarkMsg(this.getName() + "@t=" + t);
						
		execDB.remove(rFuture);
		rFuture = new AllenConstraint(new Atomic("(deadline past (interval "+(t)+" "+(t)+"))"));
		execDB.add(rFuture);
		
		/************************************************************************************************
		 * Resolve Flaws
		 ************************************************************************************************/
		Core execCore = new Core();
		execCore.setTypeManager(tM);
		execCore.setOperators(this.O);
		execCore.setContext(execDB.copy());		
		execCore.getContext().add(new Plan());
		execCore = repairSolver.run(execCore);
		
		execDB = execCore.getContext();
		
		/************************************************************************************************
		 * TODO: Re-plan on failure
		 ************************************************************************************************/
		
//				
//		needFromScratch = execCore.getResultingState(repairSolverName).equals(Core.State.Inconsistent);

		/************************************************************************************************
		 * Update all ExecutionManagers
		 ************************************************************************************************/
		if ( !firstUpdate ) { 
			for ( ExecutionManager em : managerList ) {
				em.update(t, execDB);	
			}			
		}
		
		System.out.println(execDB.get(Statement.class));
						
		/************************************************************************************************
		 * Forget past
		 ************************************************************************************************/
//		if ( someoneDone && useForgetting ) {
//			removeWrittenInStone( execDB, new HashSet<Term>() );
//		}

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

					long[] bounds = execDB.getUnique(ValueLookup.class).getBoundsArray(id);
					
					if ( ! timeLineViewer.hasTrack(tName) ) {
						timeLineViewer.createTrack(tName);
					}
					if ( ! timeLineViewer.hasValue(id.toString()) ) {
						timeLineViewer.createValue(tName, value, id.toString(), (int)bounds[0], (int)bounds[2]);
					} else {
						timeLineViewer.updateValue(id.toString(), (int)bounds[0], (int)bounds[2]);
					}		
				} catch ( NullPointerException e ) {
					e.printStackTrace();
				}
	
			}
			timeLineViewer.update();
//			timeLineViewer.snapshot(); //TODO: There is a bug where this does not terminate 

		}
	
		/************************************************************************************************
		 * Force some delay - TODO: adapt when doing real time
		 ************************************************************************************************/
		try {
			Thread.sleep(1000);
		} catch ( Exception e ) {
			
		}
	}
		
	/**
	 * Draw time-lines produced by execution 
	 */
	public void draw() {
		ValueLookup propagatedTimes = execDB.getUnique(ValueLookup.class);
		
		timeLineViewer = new TimeLineViewer();
		for ( Statement s : execDB.get(Statement.class) ) {
			String tName = s.getVariable().toString();
			String value = s.getValue().toString(); 
			Term id = s.getKey();
			if ( propagatedTimes.hasInterval(id)) {
				long[] bounds = propagatedTimes.getBoundsArray(id);
				
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

