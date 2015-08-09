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
package org.spiderplan.runnable;

import java.util.logging.Level;

import org.spiderplan.tools.logging.LogEntry;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.PlanningInterval;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.tools.visulization.TemporalNetworkVisualizer;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;



/**
 * Collection of static methods that run the planner on some files.
 * 
 * @author Uwe Köckemann
 *
 */
public class RunPlanner {
	
	public static Core run( String domainFileName, String problemFileName, String plannerFileName, boolean drawLogger ) {
		return run(domainFileName, problemFileName, plannerFileName, drawLogger, true);
	}

	public static Core run( String domainFileName, String problemFileName, String plannerFileName, boolean drawLogger, boolean verbose ) {
		
		Logger.reset();
		Logger.keepAllLogs = true;		
		ModuleFactory.forgetStaticModules();
		
		LogEntry.addID2String = true;
			
		if ( verbose ) {
			System.out.println(domainFileName);
			System.out.println(problemFileName);
			System.out.println(plannerFileName);	
		}
		
		
//		Compile.verbose = true;

		Compile.compile( domainFileName, problemFileName, plannerFileName );
		
		ConfigurationManager oM = Compile.getPlannerConfig();
		
		Module main = ModuleFactory.initModule("main", oM);
		
		if ( drawLogger ) {
			
			Logger.drawWithName("Planning Log");	
		}
		
		Core initCore = Compile.getCore();
		
		/**
		 * Propagate single operators and dum timeline info for plotting
		 * (use PlotTimelines.py in main.python on resulting 
		 * files stored to /tmp/ by default)
		 */
//		for ( Operator o : initCore.getOperators() ) {
//			ConstraintDatabase cdb = new ConstraintDatabase();
//			cdb.addStatements(o.getPreconditions());
//			cdb.addStatements(o.getEffects());
//			cdb.addConstraints(o.getConstraints());
//			cdb.add(o.getNameStateVariable());
//			IncrementalSTPSolver stp = new IncrementalSTPSolver(0,10);
//			System.out.println(cdb);
//			System.out.println(stp.isConsistent(cdb, null));
//			TemporalNetworkTools.dumbTimeLineData(cdb, stp.getPropagatedTemporalIntervals(), o.getName().getUniqueName().toString().replace("/", "-"));
//		}
		
		StopWatch.start("[main] Running...");
		Core result = main.run(initCore);
		StopWatch.stop("[main] Running...");
		
		if ( verbose ) {
			ConstraintDatabase res = result.getContext();

			System.out.println(StopWatch.getAvg("[main] Running...")/1000.0 + "s");
			System.out.println("Output signal of main module: " + result.getResultingState("main"));
						
			long temporalHorizon = 200000;
			PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(result);
			if ( pI != null ) {
				temporalHorizon = pI.getHorizonValue();
			}
	
			IncrementalSTPSolver csp = new IncrementalSTPSolver( 0, temporalHorizon );
			System.out.println( csp.isConsistent(res, result.getTypeManager()) );
			
			TemporalNetworkVisualizer tnv = new TemporalNetworkVisualizer();
			tnv.draw(res);
			
			TemporalNetworkTools.dumbTimeLineData(res, csp.getPropagatedTemporalIntervals(), "stp.txt");
					

//			System.out.println(StopWatch.allSums2Str());
//			System.out.println("makespan = " + csp.getMakespan(res, new ArrayList<Statement>()));
			
//			System.out.println(Module.getStats());
		}
		
		return result;
	}
	
}
