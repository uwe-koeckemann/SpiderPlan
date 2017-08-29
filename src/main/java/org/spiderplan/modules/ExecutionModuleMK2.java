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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.execution.Observation;
import org.spiderplan.representation.expressions.execution.Simulation;
import org.spiderplan.representation.expressions.execution.ros.ROSConstraint;
import org.spiderplan.representation.expressions.execution.ros.ROSGoal;
import org.spiderplan.representation.expressions.execution.ros.ROSRegisterAction;
import org.spiderplan.representation.expressions.execution.sockets.SocketExpression;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.misc.Assertion;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.DateTimeReference;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
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

	// Planner's time
	private long t = 0;	
	private long tPrev;
	// Real time (milliseconds)
	private long tReal;
	private long tPrevReal;
	private long tDiffReal;
	private long tPreferredUpdateInterval = 1000;
	// Updates are missed when flaw resolution takes longer than preferred update interval 
	private int missedUpdates = 0;
	
	long tMax = 1000;
	
	long t0;
	
	boolean useRealTime = false; //TODO: Real time does not work yet
//	boolean useForgetting = true;
	
	ConstraintDatabase execDB;	
	
	ConstraintDatabase initialContext;

	ArrayList<Reactor> reactors = new ArrayList<Reactor>();

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
	
	DateTimeReference dtRef = null;
	
	Runtime runtime = Runtime.getRuntime();
	
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
		
		this.dtRef = core.getContext().getUnique(DateTimeReference.class);
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
		/************************************************************************************************
		 * Update times
		 ************************************************************************************************/
		tPrev = t;
		tPrevReal = tReal;
		tReal = System.currentTimeMillis();
		tDiffReal = tReal - tPrevReal;
		if ( !useRealTime ) {
			t++;
		} else {
			if ( dtRef != null ) { 
				t = dtRef.externalDateTime2internal(tReal);
			} else {
				t = tReal;
			}
		}				
		if ( dtRef != null ) { 
			t = dtRef.externalDateTime2internal(tReal);
		} else {
			t = tReal;
		}
		if ( verbose ) {
			Logger.landmarkMsg(this.getName() + "@t=" + t);
			Logger.msg(this.getName(), String.format("t=%d (%d since last)", t, (t-tPrev)), 1);
			Logger.msg(this.getName(), String.format("tReal=%d (%d since last)", tReal, tDiffReal), 1);
//			long maxMem = runtime.maxMemory() /1024;
//			long alocMem = runtime.totalMemory() /1024;
//			long freeMem = runtime.freeMemory() /1024;
			
//			Logger.msg(this.getName(), String.format("Free memory: %dkb", freeMem), 0);
//			Logger.msg(this.getName(), String.format("Allocated memory: %dkb", alocMem), 0);
//			Logger.msg(this.getName(), String.format("Max memory: %dkb", maxMem), 0);
//			Logger.msg(this.getName(), String.format("Total free memory: %dkb", (freeMem + (maxMem - alocMem))), 0);
			Logger.msg(this.getName(), String.format("Missed updates: %d", this.missedUpdates), 0);
		}
						
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
//		execCore.getContext().add(new PlanningInterval(Term.createInteger(t), Term.createConstant("inf")));
		execCore = repairSolver.run(execCore);
		
		execDB = execCore.getContext();
		
		
		/**
		 * TODO: This is a bit of a hack to ensure that actions are related to the current time even
		 * if planner is unaware of it. This should be included in planning to begin with though to find temporal
		 * Inconsistencies when backtracking is possible and easy
		 */
		for ( Operator o : execDB.getUnique(Plan.class).getActions() ) {
			AllenConstraint ac = new AllenConstraint(o.getLabel(), TemporalRelation.Release, new Interval(Term.createInteger(t), Term.createConstant("inf")));
			execDB.add(ac);
		}
		IncrementalSTPSolver stpSolver = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		stpSolver.isConsistent(execDB);		
		stpSolver.getPropagatedTemporalIntervals(execDB.getUnique(ValueLookup.class));
		
				
		/************************************************************************************************
		 * TODO: Re-plan on failure
		 ************************************************************************************************/
//		needFromScratch = execCore.getResultingState(repairSolverName).equals(Core.State.Inconsistent);

		/************************************************************************************************
		 * Update all ExecutionManagers
		 ************************************************************************************************/
		if ( !firstUpdate ) { 
			for ( ExecutionManager em : managerList ) {
				if ( verbose ) {
					Logger.msg(this.getName(), "Updating: " + em.getClass().getSimpleName(), 0);
					Logger.depth++;
				}
				em.update(t, execDB);
				if ( verbose ) Logger.depth--;
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
		} else if (firstUpdate ) {
			firstUpdate = false;
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
		 * Wait until next update or count missed updates.
		 ************************************************************************************************/
		long tUpdateReal = System.currentTimeMillis() - tReal;
		if ( tUpdateReal < tPreferredUpdateInterval ) {
			try {
				Thread.sleep(tPreferredUpdateInterval - tUpdateReal);
			} catch ( Exception e ) { }
		} else if ( tUpdateReal > tPreferredUpdateInterval ) {
			long missedUpdatesNow = tUpdateReal/tPreferredUpdateInterval;
			missedUpdates += missedUpdatesNow;
			Logger.msg(this.getName(), String.format("Missed %d updates (%d total).", missedUpdatesNow, missedUpdates), 0);
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
	
	/**
	 * Only used to create from scratch databases.
	 */
	boolean useForgetting = true;
	int fromScratchDBsCreated = 0;
	Set<Expression> remList = new HashSet<Expression>();
	Set<Statement> writtenInStoneStatements = new HashSet<Statement>();	
	Set<Term> writtenInStone = new HashSet<Term>();
	
//	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
	ArrayList<Expression> executedLinks = new ArrayList<Expression>();
	
	private ConstraintDatabase getFromScrathDB( ) {
		
		ArrayList<Expression> reachedGoals = new ArrayList<Expression>();

		fromScratchDBsCreated++;
		
		/**********************************************************************************************
		 * Get current value lookup to have propagated bounds for temporal intervals
		 **********************************************************************************************/
		for ( OpenGoal og : execDB.get(OpenGoal.class) ) {
			execDB.add(og.getStatement());
		}
		
		IncrementalSTPSolver execCSP = new IncrementalSTPSolver(0, this.tMax);
		if ( !execCSP.isConsistent(execDB) ) {
			IncrementalSTPSolver csp = new IncrementalSTPSolver(0, this.tMax);
			csp.debug = true;
			csp.isConsistent(execDB);
			
			for ( Statement s : execDB.get(Statement.class) ) {
				System.out.println("[S] " + s);
			}
			for ( AllenConstraint tc : execDB.get(AllenConstraint.class) ) {
				System.out.println("[T] " + tc);
			}	
			
			throw new IllegalStateException("This should not happen!");
		}
		ValueLookup propagatedTimes = new ValueLookup();
		execCSP.getPropagatedTemporalIntervals(propagatedTimes); 
		
		if ( verbose ) { 
			Logger.msg(getName(), "Building from scratch CDB:", 0);
			Logger.depth++;
			Logger.msg(getName(), "Checking which operators to keep...", 2);
			Logger.depth++;
		}
		/**********************************************************************************************
		 * Find actions that are or have been executed
		 **********************************************************************************************/
		Set<Term> actionIntervals = new HashSet<Term>();
		ArrayList<Operator> executedActions = new ArrayList<Operator>();
		Set<Term> nonExecutedActionIntervals = new HashSet<Term>();
		for ( Operator a : this.plan.getActions() ) {
			if ( !executedActions.contains(a) && (this.isCommittedStatement(a.getNameStateVariable())) ) {
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
		/**********************************************************************************************
		 * Find statements with intervals that have started or ended already
		 **********************************************************************************************/
		for ( Statement s : execDB.get(Statement.class) ) { 

			if ( !nonExecutedActionIntervals.contains(s.getKey()) &&  ((propagatedTimes.getLST(s.getKey()) < t || propagatedTimes.getLET(s.getKey()) < t)) ) {
				long EST = propagatedTimes.getEST(s.getKey());
				long LST = propagatedTimes.getLST(s.getKey());
				long EET = propagatedTimes.getEET(s.getKey());
				long LET = propagatedTimes.getLET(s.getKey());
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
		
		/**********************************************************************************************
		 * Create planning interval
		 **********************************************************************************************/
		PlanningInterval pI = fromScratchDB.getUnique(PlanningInterval.class);
		fromScratchDB.remove(pI);
		fromScratchDB.add(new PlanningInterval(Term.createInteger(t), Term.createInteger(tMax)));
		
		/**********************************************************************************************
		 * TODO: do this directly above? this seems out of place
		 **********************************************************************************************/
		for ( Statement s : fromScratchDB.get(Statement.class) ) {
			actionIntervals.add(s.getKey());
		}
		
		
		if ( verbose ) { 
			Logger.depth--;
			Logger.msg(getName(), "Checking causal links that have been executed...", 2);
			Logger.depth++;
		}
		
		/**********************************************************************************************
		 * Keep only connected AllenConstraints (TODO: What about simple distance constraints, etc.?)
		 **********************************************************************************************/
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
		/**********************************************************************************************
		 * Find goals that are achieved (and keep them asserted)
		 **********************************************************************************************/
		for ( OpenGoal og : execDB.get(OpenGoal.class)) {
			if ( execDB.hasKey(og.getStatement().getKey())) {
				if ( verbose ) Logger.msg(getName(), "Goal " + og + " with " + og.getStatement().getKey() 
																	+ " [" + propagatedTimes.getEST(og.getStatement().getKey()) 
																	+ " " + propagatedTimes.getLST(og.getStatement().getKey())
																	+ "] [" + propagatedTimes.getEET(og.getStatement().getKey())
																	+ " " + propagatedTimes.getLET(og.getStatement().getKey()) + "]", 2);
				
				
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
				
//				if ( execCSP.getEST(og.getStatement().getKey()) <= t || connectedToExecutedEffect ) {
					
				if ( connectedToExecutedEffect ) {
					if ( verbose ) Logger.msg(getName(), "... was already achieved.", 2);
//					fromScratchDB.remove(og);
					OpenGoal ogCopy = og.copy();
					ogCopy.setAsserted(true);
//					reachedGoals.add(new Asserted(ogCopy));
					reachedGoals.add(ogCopy.getAssertion());
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
		/**********************************************************************************************
		 * Add preconditions, effects and constraints of executed operators
		 **********************************************************************************************/
		Collection<Operator> oRemList = new HashSet<Operator>();
		for ( Operator a : executedActions ) {
			if ( execDB.hasKey(a.getNameStateVariable().getKey()) ) {
				if ( verbose ) Logger.msg(getName(), a.getNameStateVariable().toString(), 2);
				long bounds[] = propagatedTimes.getBoundsArray(a.getNameStateVariable().getKey());
				Interval[] intervals = new Interval[2];
				intervals[0] = new Interval(Term.createInteger(bounds[0]), Term.createInteger(bounds[1]));
				intervals[1] = new Interval(Term.createInteger(bounds[2]), Term.createInteger(bounds[3]));
				
				AllenConstraint aC = new AllenConstraint(a.getNameStateVariable().getKey(), TemporalRelation.At, intervals); 
						
				fromScratchDB.add(aC);
				
				fromScratchDB.add(a.getNameStateVariable());
				for ( Statement p : a.getPreconditions() ) {
					fromScratchDB.add(p);	
				}
				for ( Statement e : a.getEffects() ) {
					fromScratchDB.add(e);	
				}
				fromScratchDB.addAll(a.getConstraints());
				
				for ( ExecutionManager eM : this.managerList ) {
					fromScratchDB.addAll(eM.getAddedReactorExpressions(a.getNameStateVariable()));
				}
				
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
		/**********************************************************************************************
		 * Decide which IC resolvers to keep
		 **********************************************************************************************/
		for ( InteractionConstraint ic : execDB.get(InteractionConstraint.class) ) {
			
			if ( ic.isAsserted() ) {
				boolean allConditionStatementsInPast = true;
				
				for ( Statement s : ic.getCondition().get(Statement.class) ) {
					if ( !(execDB.hasKey(s.getKey()) && (propagatedTimes.getLST(s.getKey()) < t || propagatedTimes.getLET(s.getKey()) < t)) ) {
						allConditionStatementsInPast = false;
						break;
					}
				}
				
				boolean allResolverStatementsInPast = true;
				for ( Statement s : ic.getResolvers().get(ic.getResolverIndex()).get(Statement.class) ) {
					if ( !(execDB.hasKey(s.getKey()) && (propagatedTimes.getLST(s.getKey()) < t || propagatedTimes.getLET(s.getKey()) < t)) ) {
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
		
		/**********************************************************************************************
		 * Process goal assertions (TODO: why is this here and not earlier?!?)
		 **********************************************************************************************/
		for ( Expression c : reachedGoals ) {
			if ( c instanceof Assertion ) {
				fromScratchDB.processAssertedTerm((Assertion)c);
			}				
		}
				
		if ( verbose ) Logger.depth--;
		/**********************************************************************************************
		 * Add everything (TODO: make sure that things are added only after this for clarity?)
		 **********************************************************************************************/
		fromScratchDB.addAll(executedLinks);
		
		for ( ExecutionManager eM : this.managerList ) {
			fromScratchDB.addAll(eM.getAddedExpressions());
		}

		fromScratchDB.add(past);
		fromScratchDB.add(future);
		fromScratchDB.add(rPast);
		fromScratchDB.add(rFuture);
		fromScratchDB.add(mPastFuture);
		fromScratchDB.add(dFuture);
		
		for ( ExecutionManager eM : this.managerList ) {
			fromScratchDB.addAll(eM.getStartedOrDoneExpressions());
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
	
	private boolean isCommittedStatement( Statement s ) {
		for ( ExecutionManager eM : this.managerList ) {
			if ( eM.isCommittedStatement(s) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isOnExecutionList( Statement s ) {
		for ( ExecutionManager eM : this.managerList ) {
			if ( eM.isOnExecutionList(s) ) {
				return true;
			}
		}
		return false;
	}
	
	private String Interval2String( long EST, long LST, long EET, long LET) {
		return String.format("[%d %d] [%d %d]", EST, LST, EET, LET);
	}
		
	private void removeWrittenInStone( ConstraintDatabase cdb, Set<Term> doNotAdd ) {		
		ValueLookup propagatedTimes = execDB.getUnique(ValueLookup.class);
		
		if ( verbose ) { 
			Logger.msg(getName(),"Searching for fixed statements...", 2);
			Logger.depth++;
		}

		Set<Expression> writtenInStoneConstraints = new HashSet<Expression>();
		
		for ( Statement s : execDB.get(Statement.class) ) {
			if ( propagatedTimes.hasInterval(s.getKey()) && !doNotAdd.contains(s.getKey()) ) { //TODO: work-around 
				long EST = propagatedTimes.getEST(s.getKey());
				long LST = propagatedTimes.getLST(s.getKey());
				long EET = propagatedTimes.getEET(s.getKey());
				long LET = propagatedTimes.getLET(s.getKey());
				
				if ( LET < t || t > LST ) {
					if ( !s.getKey().toString().equals("past") && !s.getKey().toString().equals("future") && !isOnExecutionList(s) )  {
						
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
		
		List<OpenGoal> remGoalList = new ArrayList<OpenGoal>();
		for ( Statement s : cdb.get(Statement.class) ) {
			if ( writtenInStone.contains(s.getKey()) && !connectedToOutside.contains(s.getKey()) ) {
				if ( verbose ) Logger.msg(getName(), s.toString(), 2);
				remList.add(s);
				
				for ( OpenGoal og : cdb.get(OpenGoal.class) ) {
					if ( og.getStatement().equals(s)) {
						remGoalList.add(og);
					}
				}
				
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
		int beforeOpenGoals = (cdb.get(OpenGoal.class).size());
		if ( verbose ) Logger.msg(this.getName(), "Number of constraints (before removal) " + beforeAC, 2);		
		if ( verbose ) Logger.msg(this.getName(), "Number of statements (before removal) " + beforeStatements, 2);
		if ( verbose ) Logger.msg(this.getName(), "Number of open goals (before removal) " + beforeOpenGoals, 2);		
		cdb.removeAll(remList);
		cdb.removeAll(remGoalList);
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
			Logger.msg(this.getName(), "Final number of goals " + (cdb.get(OpenGoal.class).size()) , 2);
			Logger.msg(this.getName(), "Final number of constraints " + (long) (cdb.size()) , 2);
			Logger.depth--;
		}
	}
	
}

