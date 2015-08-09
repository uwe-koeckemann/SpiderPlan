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

import java.util.HashMap;
import java.util.Map;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.SingleResolver;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.PlanningInterval;
import org.spiderplan.representation.constraints.PossibleIntersection;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.TemporalIntervalQuery;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Decides if {@link ConstraintDatabase} is temporally and resource consistent.
 * 
 * @author Uwe Köckemann
 */
public class STPSolver extends Module implements SolverInterface {
	
	IncrementalSTPSolver stpSolver;
	int historySize = 100;
		
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public STPSolver( String name, ConfigurationManager cM ) {
		super(name, cM);
		
		super.parameterDesc.add(  new ParameterDescription("historySize", "int", "100", "Number of past propagations that are kept by internal stp solver.") );
		super.parameterDesc.add(  new ParameterDescription("useFrameConstraints", "boolean", "false", "Frame constraints are temporal constraints that assure that a state variable that does not change will keep its value until the temporal horizon (i.e. forever). Used to create inconsistencies as early as possible. Can lead to removal of solutions in some cases (e.g. required concurrency, pre-scheduled resource usages). Should be turned on/off on a domain-by-domain basis. Note: Using this setting will most likely make the make-span to be the temporal horizon and thus useless as a quality criteria.") );
		super.parameterDesc.add(  new ParameterDescription("calculateRigidity", "boolean", "false", "Switch on to add rigidity of STN (after scheduling) to Core's features in case of consistency.") );		
		
//		if ( cM.hasAttribute(name, "useBookmarks") ) {
//			useBookmarks = cM.getBoolean(name, "useBookmarks");
//		}
//		if ( cM.hasAttribute(name, "useFrameConstraints") ) {
//			useFrameConstraints = cM.getBoolean(name, "useFrameConstraints");
//		}		
//		symbValScheduling = true;
//		if ( cM.hasAttribute(name, "symbolicValueScheduling") ) {
//			symbValScheduling = cM.getBoolean(name, "symbolicValueScheduling");
//		}
//		if ( cM.hasAttribute(name, "initNumActivities") ) {
//			initNumActivities = cM.getInt(name, "initNumActivities");
//		}
//		if ( cM.hasAttribute(name, "numActivitiesInc") ) {
//			numActivitiesInc = cM.getInt(name, "numActivitiesInc");
//		}
		
		
		if ( cM.hasAttribute(name, "historySize") ) {
			historySize = cM.getInt(name, "historySize");
		}
		
		stpSolver = new IncrementalSTPSolver(0,Global.MaxTemporalHorizon);
		stpSolver.setKeepTimes(this.keepTimes);
		stpSolver.setKeepStatistics(this.keepStats);
		stpSolver.setName(this.name);
		stpSolver.setVerbose(this.verbose);
		stpSolver.setMaxHistorySize(historySize);
	}

	@Override
	public Core run( Core core ) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		SolverResult result = this.testAndResolve(core);
			
		if ( !result.getState().equals(State.Inconsistent) ) {
			if ( result.getResolverIterator() == null ) {
				core.setResultingState( getName(), State.Consistent );
			} else {
				Resolver r = result.getResolverIterator().next();
				ConstraintDatabase cDB = core.getContext().copy();
				r.apply(cDB);
				core.setContext(cDB);
				core.setResultingState(getName(), State.Consistent); // Not State.Searching since STPSolver resolver only adds information
			}
		} else {
			core.setResultingState( getName(), State.Inconsistent );
		} 	
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		boolean isConsistent = true;
		boolean isTemporalConsistent = true;
		boolean isIntersectionConsistent = true;
		
		PlanningInterval planningInterval = ConstraintRetrieval.getPlanningInterval(core.getContext());
		
		if ( planningInterval != null ) {
			long origin = planningInterval.getStartTimeValue();
			long horizon  = planningInterval.getHorizonValue();
			if ( origin != stpSolver.getOrigin() || horizon != stpSolver.getHorizon() ) {
				stpSolver = new IncrementalSTPSolver(origin,horizon);
				stpSolver.setKeepTimes(this.keepTimes);
				stpSolver.setKeepStatistics(this.keepStats);
				stpSolver.setName(this.name);
				stpSolver.setVerbose(this.verbose);
				stpSolver.setMaxHistorySize(historySize);
			}
		}
	
		if ( keepTimes ) StopWatch.start("["+this.getName()+"] Initializing");
		ConstraintDatabase cDB = core.getContext();
		Map<Class,Integer> cdbConCount = cDB.getConstraintCount();

		/**
		 * Add goals for the test
		 */
		for ( OpenGoal og : cDB.getConstraints().get(OpenGoal.class) ) {
			cDB.add(og.getStatement());
		}
		if ( keepTimes ) StopWatch.stop("["+this.getName()+"] Initializing");
						
		if ( keepStats ) {
			HashMap<String,Long> cCount = new HashMap<String, Long>();

			for( AllenConstraint c : core.getContext().getConstraints().get(AllenConstraint.class) ) {		
				if ( !cCount.containsKey(c.getRelation().toString())) {
					cCount.put(c.getRelation().toString(), new Long(0));
				}
				cCount.put(c.getRelation().toString(), new Long(cCount.get(c.getRelation().toString()).intValue() + 1));
			}
			for ( String k : cCount.keySet() ) {
				stats.setLong(msg("Last #" + k), cCount.get(k));
			}
			stats.setLong(msg("Last #Statements"), Long.valueOf(core.getContext().getStatements().size()));
		}
		
		
		/** 
		 * Run Temporal Reasoner
		 */		
		if ( keepTimes ) StopWatch.start("["+this.getName()+"] Running incremental STP solver");
		isTemporalConsistent = stpSolver.isConsistent( cDB, core.getTypeManager() );
		if ( keepTimes ) StopWatch.stop("["+this.getName()+"] Running incremental STP solver");
		
		isConsistent = isTemporalConsistent;
			
		if ( isConsistent ) {
			if ( verbose )  {
				Logger.msg(this.getName(), "Checking intersections...", 3);
				Logger.depth++;
			}
			if ( keepTimes ) StopWatch.start(msg("Intersection Tests"));
			for ( PossibleIntersection pi : cDB.getConstraints().get(PossibleIntersection.class)) {
				isIntersectionConsistent = stpSolver.possibleIntersection(pi);
				if ( !isIntersectionConsistent ) {
					if ( verbose )  Logger.msg(this.getName(), pi.toString() + " -> not satisfied", 3);
					isConsistent = false;
					if ( keepTimes ) StopWatch.stop(msg("Intersection Tests"));
					break;
				} else {
					if ( verbose )  Logger.msg(this.getName(), pi.toString() + " -> satisfied", 3);
				}
			}
			if ( keepTimes ) StopWatch.stop(msg("Intersection Tests"));
			if ( verbose )  {
				Logger.depth--;
			}
		}
		
		if ( isConsistent ) {
			if ( verbose )  {
				Logger.msg(this.getName(), "Checking TemporalIntervalQueries...", 1);
				Logger.depth++;
			}
			if ( keepTimes ) StopWatch.start(msg("Testing queries"));
			for ( TemporalIntervalQuery tiq : cDB.getConstraints().get(TemporalIntervalQuery.class)) {
				if ( verbose ) Logger.msg(this.getName() , tiq.toString(), 1);
				String relation = tiq.getRelation().getUniqueName();
				
				Term variable = tiq.getRelation().getArg(0);
				Term value = tiq.getRelation().getArg(1);
				
				String feature = variable.getUniqueName();
				Term targetInterval = variable.getArg(0);
				
				if ( value.isVariable() ) {
					continue;
				}
				
				long EST = stpSolver.getEST(targetInterval);
				long LST = stpSolver.getLST(targetInterval);
				long EET = stpSolver.getEET(targetInterval);
				long LET = stpSolver.getLET(targetInterval);
				
				long lower = -1, upper = -1;
			
				long queryValue = Long.valueOf(value.toString()).longValue();
				
				if ( feature.equals("start-time/1") ) {
					lower = EST;
					upper = LST;
				} else if ( feature.equals("end-time/1") ) {
					lower = EET;
					upper = LET;
				} else if ( feature.equals("duration/1") ) {
					lower = EET-EST;
					upper = LET-LST;
				} else {
					throw new IllegalStateException("Unknown feature " + feature + ". Use start-time, end-time or duration with exactly one argument (a temporal interval).");
				}
				
				if ( relation.equals("greater-than/2") ) {
					isConsistent = lower > queryValue;
				} else if ( relation.equals("greater-than-or-equals/2") ) {
					isConsistent = lower >= queryValue;
				} else if ( relation.equals("less-than/2") ) {
					isConsistent = upper < queryValue;
				} else if ( relation.equals("less-than-or-equals/2") ) {
					isConsistent = upper <= queryValue;
				} else {
					throw new IllegalStateException("Unknown relation " + relation + ".");	
				}
				
				if ( !isConsistent ) {
					if ( verbose ) Logger.msg(this.getName() , "Not satisfied.", 1);
					break;
				} else {
					if ( verbose ) Logger.msg(this.getName() , "Satisfied.", 1);
				}
			}
			if ( keepTimes ) StopWatch.stop(msg("Testing queries"));
			if ( verbose ) Logger.depth--;
		}
	
		if ( keepTimes ) StopWatch.start(msg("Finishing up"));
		State state;
		ResolverIterator resolverIterator = null;
		if ( isConsistent ) {
			ConstraintDatabase resCDB = new ConstraintDatabase();
			resCDB.add(stpSolver.getPropagatedTemporalIntervals());
			Resolver r = new Resolver(resCDB);
			resolverIterator = new SingleResolver(r, name, cM);
			if ( verbose ) Logger.msg(getName(), "Consistent (adding bound information resolver)", 0);
			
			state = State.Consistent;
		} else {
			
//			MetaCSPAdapter csp = new MetaCSPAdapter();		
//			System.out.println("=========================================");
//			boolean test = csp.isConsistent(cDB, core.getTypeManager());
//			System.out.println(test);
//			IncrementalSTPSolver stpNew = new IncrementalSTPSolver(0, 10000000);
//			System.out.println(stpNew.isConsistent( cDB, core.getTypeManager()));
//			System.out.println(stpSolver.isConsistent( cDB, core.getTypeManager()));
//			System.out.println("=========================================");
//
//			if ( test && !isTemporalConsistent ) {
//				System.out.println(cDB);
//				IncrementalSTPSolver stpNewDebug = new IncrementalSTPSolver(0, 10000000);
//				stpNewDebug.debug = true;
//				System.out.println(stpNewDebug.isConsistent( cDB, core.getTypeManager()));
//				
//				Loop.start();
//			}
			
			if ( verbose ) {
				String msg = "Inconsistent";
				if ( !isTemporalConsistent ) {
					msg += " (temporal)";
				} else if (!isIntersectionConsistent) {
					msg += " (impossible intersection(s)) ";
				} else {
					msg += " (unsatisfied queries) ";
				}
				if ( verbose ) Logger.msg(getName() , msg, 0);
			}
			state = State.Inconsistent;
		}
		cDB.setToConstraintCount(cdbConCount);
		if ( keepTimes ) StopWatch.stop(msg("Finishing up"));
		return new SolverResult(state,resolverIterator);
	}
}
