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
package org.spiderplan.modules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.executor.Reactor;
import org.spiderplan.executor.ROS.ROSExecutionManager;
import org.spiderplan.executor.observation.ObservationExecutionManager;
import org.spiderplan.executor.simulation.SimulationExecutionManager;
import org.spiderplan.executor.sockets.SocketExecutionManager;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.execution.Observation;
import org.spiderplan.representation.expressions.execution.Simulation;
import org.spiderplan.representation.expressions.execution.caresses.CaressesExpression;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.execution.ros.ROSRegisterAction;
import org.spiderplan.representation.expressions.execution.sockets.SocketExpression;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.Loop;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;

/**
 * Executes a plan.
 * 
 * TODO: simulation constraints on release not firing?	
 * TODO: constraints to enable forgetting for statements
 * 
 * @author Uwe Köckemann
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

	Term tHorizon = Term.createConstant("time");
	
	Statement past = new Statement(Term.createConstant("past"), tHorizon, Term.createConstant("past") );
	Statement future = new Statement(Term.createConstant("future"), tHorizon, Term.createConstant("future") );
	
	AllenConstraint rPast = new AllenConstraint( Term.parse("(release past (interval 0 0))"));
	AllenConstraint mPastFuture = new AllenConstraint( Term.parse("(meets past future)"));
	AllenConstraint dFuture = new AllenConstraint( Term.parse("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
	AllenConstraint rFuture = new AllenConstraint( Term.parse("(deadline past (interval 1 1)"));
	AllenConstraint mFuture;
	
//	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
//	private ConstraintDatabase addedSimDBs = new ConstraintDatabase();
//	private ConstraintDatabase addedOnReleaseDB = new ConstraintDatabase();
//	private ConstraintDatabase addedByROS = new ConstraintDatabase();
	
	boolean drawTimeLines = false;
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
	
	List<String> execModuleNames = new ArrayList<String>();
	
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
		
		if ( cM.hasAttribute(name, "modules") ) {
			try {
				this.execModuleNames = cM.getStringList(name, "modules");
				
				for ( String moduleClassStr : this.execModuleNames ) {
					Class sClass = Class.forName("java.lang.String");
					Class cdbClass = Class.forName("org.spiderplan.representation.ConstraintDatabase");
					
					Class moduleClass = null;
					boolean foundClass = false;
					
		//			// Try default location of modules
		//			try {
		//				moduleClass = Class.forName("org.spiderplan.modules."+moduleClassStr);
		//				foundClass = true;
		//			} catch ( ClassNotFoundException e ) { }	// We still got options:
		//			// Try external module 
		//			if ( !foundClass ) {
					moduleClass = Class.forName(moduleClassStr);
		//			}
					@SuppressWarnings("unchecked")
					Constructor c = moduleClass.getConstructor(sClass);
					ExecutionManager m = (ExecutionManager)c.newInstance(name);
				
					this.managerList.add(m);
				}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Loop.start();
		} catch (SecurityException e) {
			e.printStackTrace();
			Loop.start();	
		} catch (InstantiationException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Loop.start();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
			e.printStackTrace();
			Loop.start();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Loop.start();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			Loop.start();
		} 
		}
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
//		DateTimeReference timeRef = core.getContext().getUnique(DateTimeReference.class);
		
		this.t0 = 0;
		
		this.t = pI.getStartTimeValue();
		this.tMax = pI.getHorizonValue();
		
		this.tM = core.getTypeManager();
		this.O = core.getOperators();
		
		rPast = new AllenConstraint(Term.parse("(release past (interval 0 0))"));
		mPastFuture = new AllenConstraint(Term.parse("(meets past future)"));
		dFuture = new AllenConstraint(Term.parse("(deadline future (interval "+(tMax-1)+" "+(tMax-1)+"))"));
		rFuture = new AllenConstraint(Term.parse("(deadline past (interval 1 1)"));
				
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
		
		// Initialize externally specified managers
		for ( ExecutionManager eM : this.managerList ) {
			eM.setVerbose(verbose);
			eM.setVerbosity(verbosity);
			eM.initialize(execDB);
		}
		
		// Check if default managers are needed
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
		if ( !execDB.get(SocketExpression.class).isEmpty() ) {
			if ( verbose ) Logger.msg(getName(), "Found socket expression. Initializing SocketExecutionManager.", 1);
			ExecutionManager socketManager = new SocketExecutionManager(this.getName());
			socketManager.setVerbose(this.verbose);
			socketManager.setVerbosity(verbosity);
			socketManager.initialize(execDB);
			managerList.add(socketManager);
		}
		if ( !execDB.get(Observation.class).isEmpty() ) {
			if ( verbose ) Logger.msg(getName(), "Found observation expression. Initializing ObservationExecutionManager.", 1);
			ExecutionManager observationManager = new ObservationExecutionManager(this.getName());
			observationManager.setVerbose(this.verbose);
			observationManager.setVerbosity(verbosity);
			observationManager.initialize(execDB);
			managerList.add(observationManager);
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
		rFuture = new AllenConstraint(Term.parse("(deadline past (interval "+(t)+" "+(t)+"))"));
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
				//TODO: Statements can be added that are used later but without having been propagated...
				// Might be best to only consider current information for all exec. managers?
				// + The order is arbitrary... so they should ignore each others additions... but how to make sure?
			}			
		}
		
						
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
					
					if ( execDB.getUnique(ValueLookup.class).hasInterval(id) ) {
						long[] bounds = execDB.getUnique(ValueLookup.class).getBoundsArray(id);
						
						if ( ! timeLineViewer.hasTrack(tName) ) {
							timeLineViewer.createTrack(tName);
						}
						if ( ! timeLineViewer.hasValue(id.toString()) ) {
							timeLineViewer.createValue(tName, value, id.toString(), (int)bounds[0], (int)bounds[2]);
						} else {
							timeLineViewer.updateValue(id.toString(), (int)bounds[0], (int)bounds[2]);
						}		
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

