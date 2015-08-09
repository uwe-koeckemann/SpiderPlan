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

import org.spiderplan.tools.logging.Logger;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.PlanningInterval;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.tools.visulization.TemporalNetworkVisualizer;
  
public class Run {
 
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) throws ParseException {	
		String domain = args[0];
		String problem = args[1];
		String planner = null;
		
		boolean drawStuff = false;
		
		if ( args.length > 2 ) {
			planner = args[2];
		} 
		if ( args.length > 3 ) {
			drawStuff = true;
		}
	
		System.out.println(domain);
		System.out.println(problem);
		System.out.println(planner);
				 
		Compile.compile( domain, problem, planner );
		
		ConfigurationManager oM = Compile.getPlannerConfig();
		
		Module main = ModuleFactory.initModule("main", oM);
		
		if ( drawStuff ) {
			Logger.draw();
		}
		Core initCore = Compile.getCore();
		

				
		StopWatch.start("[main] Running...");
		Core result = main.run(initCore);
		StopWatch.stop("[main] Running...");
	
		System.out.println("Total time: " + (StopWatch.getAvg("[main] Running...")/1000.0) + "s");
	
		ConstraintDatabase res = result.getContext();
		
		if ( drawStuff ) {
			TemporalNetworkVisualizer tnVis = new TemporalNetworkVisualizer();
			tnVis.draw(res);
			
			long temporalHorizon = 200000;
			PlanningInterval pI = ConstraintRetrieval.getPlanningInterval(result);
			if ( pI != null ) {
				temporalHorizon = pI.getHorizonValue();
			}	
		}
	
		System.out.println("Output signal of main module: " + result.getResultingState("main"));
	
		System.out.println(res.toString());
	}
}
