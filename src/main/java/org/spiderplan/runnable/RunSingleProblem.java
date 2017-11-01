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
package org.spiderplan.runnable;

import java.util.ArrayList;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.causal.Task;
import org.spiderplan.representation.expressions.domain.TypeDomainConstraint;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.tools.profiler.Profiler;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.tools.visulization.TemporalNetworkVisualizer;

import com.hp.hpl.jena.graph.query.Domain;

/**
 * Collection of static methods that run the planner on some files.
 * 
 * @author Uwe Köckemann
 *
 */
public class RunSingleProblem {
	
	/**
	 * Solve single problem by providing a planner (first element in <code>args</code>
	 * and a series of domain definition files (second and following elements in <code>args</code>.
	 * 
	 * @param args list of filenames
	 */
	public static void run( String[] args ) {
		
		String plannerFilename = args[0];
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		
		for ( int i = 1 ; i < args.length ; i++ ) {
			domainFiles.add(args[i]);
		}
		
		Logger.reset();
		ModuleFactory.forgetStaticModules();
		
	
		System.out.println("Planner:\n\t" + plannerFilename);
		
		System.out.println("Domain & Problem Files:");
		for ( String domainFilename : domainFiles ) {
			System.out.println("\t" + domainFilename);
		}	
		
		
		
//		Compile.verbose = true;

		Compile.compile( domainFiles, plannerFilename );
		
		ConfigurationManager oM = Compile.getPlannerConfig();	
		
		Module main = ModuleFactory.initModule("main", oM);
		
		Logger.drawWithName("Planning Log");	
		
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
		
//		System.out.println(initCore.getContext());
		
		System.out.println(String.format("<Before> Number of domain constraints: %d", initCore.getContext().get(TypeDomainConstraint.class).size()));
		System.out.println(String.format("<Before> Number of operators: %d", initCore.getOperators().size()));
	
		
		StopWatch.start("[main] Running...");
		Core result = main.run(initCore);
		StopWatch.stop("[main] Running...");
		
		if ( true ) { // verbose
			ConstraintDatabase res = result.getContext();

			System.out.println(StopWatch.getAvg("[main] Running...")/1000.0 + "s");
			System.out.println("Output signal of main module: " + result.getResultingState("main"));
						
//			long temporalHorizon = 200000;
//			PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(result);
//			if ( pI != null ) {
//				temporalHorizon = pI.getHorizonValue();
//			}
	
//			IncrementalSTPSolver stpSolver = new IncrementalSTPSolver( 0, temporalHorizon );
			for ( OpenGoal g : res.get(OpenGoal.class) ) {
				res.add(g.getStatement());
			}
			for ( Task g : res.get(Task.class) ) {
				res.add(g.getStatement());
			}
//			System.out.println( stpSolver.isConsistent(res, result.getTypeManager()) );
			
//			System.out.println(res);

			System.out.println(String.format("<After> Number of domain constraints: %d", res.get(TypeDomainConstraint.class).size()));
			System.out.println(String.format("<After> Number of operators: %d", result.getOperators().size()));
			
			
			
			TemporalNetworkVisualizer tnv = new TemporalNetworkVisualizer();
			tnv.draw(res);
			
			ValueLookup valueLookup = res.getUnique(ValueLookup.class);
//			stpSolver.getPropagatedTemporalIntervals(valueLookup);
			
//			TimelineCanvas tlc = new TimelineCanvas();
//			TimelineCanvas.setCDB(res);
//			TimelineCanvas.main(args);
			
//			System.out.println("===================================");
			
			
			
			TemporalNetworkTools.dumbTimeLineData(res, valueLookup, "stp.txt");

			res.export("solution.uddl");
			
			ValueLookup vLookUp = res.getUnique(ValueLookup.class);
			if ( vLookUp != null ) {
				System.out.println("===========================================================");
				System.out.println("= Calculated Values");
				System.out.println("===========================================================");
				System.out.println(valueLookup);
			}
		}
		
		String statsStr = Statistics.getString();
		if ( !statsStr.equals("") ) {
			System.out.println("===========================================================");
			System.out.println("= Statistics");
			System.out.println("===========================================================");
			System.out.println();
		}
		
		System.out.println("===========================================================");
		System.out.println("= Times");
		System.out.println("===========================================================");
		System.out.println(StopWatch.allSums2Str());
		
		String profileReport = Profiler.getString();
		
		if ( !profileReport.isEmpty() ) {
			System.out.println("===========================================================");
			System.out.println("= Profile");
			System.out.println("===========================================================");
			System.out.println(Profiler.getString());
		}
		
		System.out.println("===========================================================");
		System.out.println("= Note");
		System.out.println("===========================================================");
		System.out.println("To generate a timeline plot with python and matplotlib use:\n\tpython ./src/main/python/PlotTimelines.py " + Global.workingDir + "stp.txt");
	}
	
}
