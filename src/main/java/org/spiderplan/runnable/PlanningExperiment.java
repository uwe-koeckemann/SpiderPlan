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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.prolog.YapPrologAdapter;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.ConsistencyChecker;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.stopWatch.StopWatch;

public class PlanningExperiment {

	public static MainFrame mFrame;
	
	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) throws ParseException {
		/**
		 * Few problems to start up (results not kept)
		 */
		String planner = 		"./data/domains/benchmark/planner-experiment.spider";
		String domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
		String problemDirName = "./data/domains/benchmark/social-acceptablility/burn-in";
		run("burn-in", domain, problemDirName, planner);
		Module.getStats().reset();  
		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-2INT";		
//		run("SA-NC-2INT", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-3INT";
//		run("SA-NC-3INT", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-4vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-4INT";
//		run("SA-NC-4INT", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
//		run("SA-FF-3INT", domain, problemDirName, planner);
//		Module.getStats().reset();	
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-02res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-02RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-03res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-03RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-04res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-04RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-05res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-05RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-06res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-06RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-07res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-07RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-08res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-08RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-09res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-09RES", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-10res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-10RES", domain, problemDirName, planner);
//		Module.getStats().reset();
				
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-02res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-02RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-03res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-03RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-04res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-04RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-05res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-05RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-06res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-06RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-07res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-07RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-08res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-08RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-09res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-09RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars-10res.uddl";
		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
		run("SA-FF-3INT-10RES", domain, problemDirName, planner);
		Module.getStats().reset();
		
		
		
		
//		
//		domain = 		"./data/domains/benchmark/context-awareness/domain-2-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/2-ints";
//		run("CA-2INT", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/context-awareness/domain-3-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/3-ints";
//		run("CA-3INT", domain, problemDirName, planner);	
//		Module.getStats().reset();
//				
//		domain = 		"./data/domains/benchmark/context-awareness/domain-4-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/4-ints";
//		run("CA-4INT", domain, problemDirName, planner);	
//		Module.getStats().reset();
//		
		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-vars-scale.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/var-scale-test";
//		run("SA-VS", domain, problemDirName, planner);
//		Module.getStats().reset();	
		
		
//		/**
//		 * Change planner and run again!
//		 */
//		planner = 		"./data/domains/benchmark/planner-experiment-bruteforce.spider";
////		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-2INT";		
//		run("SA-NC-2INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-3INT";
//		run("SA-NC-3INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-3vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT";
//		run("SA-FF-3INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();	
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-02res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-02RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-03res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-03RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-04res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-04RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-05res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-05RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-06res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-06RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-07res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-07RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-08res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-08RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-09res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-09RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-2vars-10res.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-FF-2INT";
//		run("SA-FF-2INT-10RES-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/context-awareness/domain-2-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/2-ints";
//		run("CA-2INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		
//		domain = 		"./data/domains/benchmark/context-awareness/domain-3-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/3-ints";
//		run("CA-3INT-BF", domain, problemDirName, planner);	
//		Module.getStats().reset();
		
		
		//Skipping for now
		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-4vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-NC-4INT";
//		run("SA-NC-4INT-BF", domain, problemDirName, planner);
//		Module.getStats().reset();
//		domain = 		"./data/domains/benchmark/context-awareness/domain-4-ints.uddl";
//		problemDirName = "./data/domains/benchmark/context-awareness/4-ints";
//		run("CA-4INT-BF", domain, problemDirName, planner);	
//		Module.getStats().reset();		
		
		//Continue here

		
//		domain = 		"./data/domains/benchmark/social-acceptablility/domain-vars-scale.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/var-scale-test";
//		run("SA-VS-BF", domain, problemDirName, planner);
//		Module.getStats().reset();	
//
//		
//		domain = 		 "./data/domains/benchmark/social-acceptablility/domain-2vars.uddl";
//		problemDirName = "./data/domains/benchmark/social-acceptablility/SA-BC-2INT";
//		run("SA-BC-2INT", domain, problemDirName, planner);
//		Module.getStats().reset();

		
		
		/**
		 * Offline proactivity tests
		 */
//		planner = "./data/domains/human-aware planning/planner-ic-first-experiment.spider";
//		domain = "./data/domains/human-aware planning/domain-v5.uddl";
//		problemDirName = "/home/uwe/icaps-2015-exp/";
//		run("proactivity", domain, problemDirName, planner);
//		Module.getStats().reset();
		
		
	}
	
	public static void run( String expName, String domain, String problemDirName, String planner ) {
		ConsistencyChecker.ignoreWarnings = true;
		int expCount = 0;
		
		
//		Logger.reset();
//		Logger.keepAllLogs = true;
//		Logger.registerSource("Times", 0);
		
//		Logger.init();
		
//		Logger.stop = true;
		
		StopWatch.keepAllTimes = false;
		
		YapPrologAdapter.uniqueFileNamePart = "epx";
	
		long maxTimeMillis = 1000*60*10; 
//		long maxTimeMillis = 2000; //*60*30; 
		
//		class TimeOutThread extends Thread {
//			
//			long startTimeMillis;
//			long maxTimeMillis;
//			boolean stopped = false;
//			
//			public TimeOutThread( long maxTimeMillis ) {
//				this.maxTimeMillis = maxTimeMillis;
//			}
//			
//			public void stopThread() {
//				this.stopped = true;
//			}
//			
//			@Override
//			public void run() {
//				startTimeMillis = System.currentTimeMillis();
//				
//				while ( !stopped && (System.currentTimeMillis()-startTimeMillis) < maxTimeMillis ) {
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//				if ( !stopped ) {
//					Module.setKillFlag(true);
//				}
//			}
//		};
						
		File problemDir = new File(problemDirName);
		
		ArrayList<String> problemList = new ArrayList<String>();
	
		for ( File f : problemDir.listFiles() ) {
			if ( f.isFile() && f.getName().contains(".uddl") && !f.getName().contains("~")) {
				problemList.add(f.getAbsolutePath());
			}
		}
		
		Collections.sort(problemList);
		
		ArrayList<Long> times = new ArrayList<Long>();
				
		System.out.println("Running:");
		System.out.println(domain);
		System.out.println(problemDirName);
		System.out.println(planner);
		
//		int numFinishedProblems = 0;
		ArrayList<String> attOrder = new ArrayList<String>();
		attOrder.add("Problem File");		
		attOrder.add("Domain");
		attOrder.add("Result");
		attOrder.add("Time [Total]");
//		attOrder.add("Time [Condition Test]");
//		attOrder.add("[Interaction] Testing condition");
//		attOrder.add("[Interaction] Satisfied Condition");
//		attOrder.add("[main] Applied resolvers");		
//		Module.getStats().renameKey("Time [Total]", "Solving Time [s]"); // actually it's ms but will be converted to s and used as output by some other program
//		Module.getStats().renameKey("Time [Condition Test]", "Condition Testing Time [s]"); // actually it's ms but will be converted to s and used as output by some other program
//		Module.getStats().renameKey("[Interaction] Satisfied Condition", "#Satisfied Conditions"); // actually it's ms but will be converted to s and used as output by some other program
//		Module.getStats().renameKey("[Interaction] Testing condition", "#Tested Conditions"); // actually it's ms but will be converted to s and used as output by some other program
//		Module.getStats().renameKey("[main] Applied resolvers", "#Applied Resolvers"); // actually it's ms but will be converted to s and used as output by some other program
		
		
		for ( String problem : problemList ) {
			int lastIndex = problem.split("/").length-1;
			String pName = problem.split("/")[lastIndex].split("\\.")[0];
			int lastIndexDomain = domain.split("/").length-1;
			String dName = domain.split("/")[lastIndexDomain].split("\\.")[0];
			expCount++;
							
			Compile.verbose = false;
			
			ModuleFactory.forgetStaticModules();
			
			Compile.compile( domain, problem, planner);
				
			ConfigurationManager oM = Compile.getPlannerConfig();
			
			Module main = ModuleFactory.initModule("main", oM);
					
			Core initCore = Compile.getCore();
		
			Date d = new Date();
			System.out.print("Starting " +pName+ " at " + addZero(String.valueOf(d.getHours())) + ":" + addZero(String.valueOf(d.getMinutes())) + ":" + addZero(String.valueOf(d.getSeconds())) + "... ");

			StopWatch.start("[main] Running... " + pName );
//			StopWatch.on = false;
			
//			Logger.drawWithName("Planning Log");
			
//			TimeOutThread tOutThread = new TimeOutThread(maxTimeMillis);
//			tOutThread.start();
			Core r = main.run(initCore);
//			tOutThread.stopThread();
						
//			StopWatch.on = true;

			StopWatch.stop("[main] Running... " + pName );
			
			if ( Module.getKillFlag() ) {
				System.out.println("Time for "+pName+" ("+expCount+"): TIMEOUT");
				Module.getStats().setString("Time", "TIMEOUT");
			} else {			
				System.out.println("Time for "+pName+" ("+expCount+"): " +  String.format("%.3f", StopWatch.getAvg("[main] Running... " + pName)/1000.0) + " (" + r.getResultingState("main") + ")");
				Module.getStats().setDouble("Time", StopWatch.getAvg("[main] Running... " + pName)/1000.0);
			}
			Module.setKillFlag(false);
			
//			numFinishedProblems++;
			
			times.add( StopWatch.getLast("[main] Running... " + pName ));
			
			
//			Module.getStats().renameKey("[Interaction] Testing condition", "Tested Conditions");
//			Module.getStats().renameKey("[Interaction] Satisfied Condition", "Satisfied Conditions");
			
			
			Module.getStats().setString("Result", r.getResultingState("main").toString());
			Module.getStats().setString("Problem File", pName+".uddl");
			Module.getStats().setString("Domain", dName);
			Module.getStats().setLong("Time [Total]", StopWatch.getLast("[main] Running... " + pName ));
//			Module.getStats().setLong("Time [Condition Test]", StopWatch.getLast("[Interaction] Testing condition"));
//			Module.getStats().setLong("Time [Temporal Propagation]", StopWatch.getLast("[ICConditionTester] test: Temporal"));

			
			
//			Module.getStats().setString("Human Set", pName.split("-")[1]);
//			Module.getStats().setString("Uncertainty", pName.split("-")[3]);
			
			
//			Module.getStats().setLong("#Goals", Long.valueOf(pName.split("-")[2].replace("g", "")));
//			Module.getStats().setLong("#Activities", Long.valueOf(pName.split("-")[5].replace("a", "")));
			
			Module.getStats().store();
			Module.getStats().dumpCSV("/home/uwe/results/" + expName + ".csv", attOrder);
			
			StopWatch.reset();			
			Global.resetStatics();
			
			System.gc();
			
			try {
				Thread.sleep(2000);
			} catch ( Exception e) {
				
			}
		}
	}
	
	private static String addZero( String s ) {
		if ( s.length() < 2 ) 
			return "0" + s;
		return s;
	}
}
