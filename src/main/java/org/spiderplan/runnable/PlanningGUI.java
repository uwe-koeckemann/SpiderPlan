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
import org.spiderplan.modules.FlowModule;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.tools.stopWatch.StopWatch;

public class PlanningGUI {

	public static MainFrame mFrame;
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) throws ParseException {	
		Logger.init();
		mFrame = new MainFrame("data", "fastDownward.uddl", "ipc-match-cellar", "p01.uddl");

		while ( true ) {
			@SuppressWarnings("unused")
			String s = "" + mFrame.run;

			if ( mFrame.run ) { 
				run();
				mFrame.run = false;
			} else if ( mFrame.check ) {
				check();
				mFrame.check = false;
			}
			
			try {
				Thread.sleep(1000);
			} catch ( Exception e) {
				
			}
		}
	}

	public static void run() throws ParseException {

		Logger.init();
		Logger.keepAllLogs = true;
		
		Logger.registerSource("Times", 0);
				
		String domain = "" + mFrame.selectedDomainFileName;
		String problem = "" + mFrame.selectedProblemFileName;
		String planner = "" + mFrame.selectedPlannerFileName;
		
		System.out.println(domain);
		System.out.println(problem);
		System.out.println(planner);
		
		Compile.compile( domain, problem, planner);
		
		ConfigurationManager oM = Compile.getPlannerConfig();
		
		Module main;
		if ( oM.getString("main", "class").equals("FlowModule") ) {
			main = new FlowModule("main", oM);
		} else {
			System.err.println("Unsupported main module class: " + oM.getString("main", "class"));
			return;
		}
		
		Logger.draw("main,Plan,TDBCheckerSuggestion");
		Core initCore = new Core();
		
		StopWatch.start("[main] Running...");
//			StopWatch.on = false;
		Core r = main.run(initCore);
//			StopWatch.on = true;
		StopWatch.stop("[main] Running...");
		
//			System.out.println("fromScratch: " + APSPSolver.fromScratchCounter);
//			System.out.println("inc: " + APSPSolver.incCounter);
//			System.out.println("singleTPCreate: " + APSPSolver.singleTPcreation);
//			System.out.println("multiTOCreate: " + APSPSolver.multiTPcreation);
//			System.out.println("tPDelete: " + APSPSolver.tpDeleteCount);
//			System.out.println("cCreate: " + APSPSolver.cCreate);
//			System.out.println("constructorCalls: " + APSPSolver.constructorCalls);
//			System.out.println("propagations: " + APSPSolver.propagations);
		
//			System.out.println("sCvar: " + APSPSolver.singleCreateVarSub);
//			System.out.println("mCvar: " + APSPSolver.multiCreateVarSub);
		
	
		Plan p = r.getPlan();
		
//			TemporalDatabase tDB = (TemporalDatabase)c.read( "plan" );
		
//		MetaCSPAdapter csp = new MetaCSPAdapter(1000);
//			
		
//			csp.draw();
		p.draw();
		
//			TemporalDatabase tDB = new TemporalDatabase();
//			TemporalDatabase app = p.apply(tDB, 0);
		
//			csp.isConsistent(tDB, Core.dM.tM);
		
//			csp.draw();
		
		
		Logger.msg("Times", StopWatch.allSums2Str(), 0);
			
	}
	
	public static void check() {
		
	}
	
}
