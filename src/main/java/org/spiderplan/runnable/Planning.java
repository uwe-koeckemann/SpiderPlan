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

import java.util.Arrays;

import org.spiderplan.modules.solvers.Module;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.Compile.DomainVersion;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.profiler.Profiler;
import org.spiderplan.tools.stopWatch.StopWatch;

public class Planning {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) throws ParseException {	

//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test01-open_door.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
		
//		String domain = "./data/domains/ecai-domain/domain-full.uddl";
//		String problem = "./data/domains/ecai-domain/experiments/p-u0-h1-g10-id01-a0117.uddl";
//		String problem = "./data/domains/ecai-domain/test-cases/01.uddl";
//		String planner = "./data/domains/ecai-domain/planners/NewGoals.uddl";
		
//		String domain = "./data/domains/jair-domain/domain-intersections-v2.uddl";
//		String problem = "/home/uwe/experiments/p-h3-g10-u00-id01-a0267.uddl";
//		String problem = "./data/domains/jair-domain/p-u7-h1-g30-id15-a0269.uddl";
//		String planner = "./data/domains/jair-domain/planners/BackjumpingBinSearch.uddl";
//		String planner = "./data/domains/jair-domain/planners/BackjumpingBinSearchLogicalInLoop.uddl";
		
//		Compile.verbose = true;
//		Compile.domainVersion = Compile.DomainVersion.v2;
		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test01-open_door.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
		
		
//		String domain = "./data/domains/aaai-domain/domain-intersections-v2.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test02.uddl";
		
//		String problem = "./data/domains/aaai-domain/experiment/p-h1-u0-g30-id11-a0112.uddl";
//		String problem = "./data/domains/aaai-domain/experiment/p-h2-u1-g20-id04-a0159.uddl";
		
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.uddl";
//		String planner = "./data/domains/aaai-domain/planners/WithExecution.uddl";
		
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test01.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
		
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/domains/ipc-elevators-resources/planners/CausalGraph.uddl";

//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test02-door-will-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
		
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/planners/CreateNewOperators.uddl";
		
//		String domain = "./data/domains/ickeps-petrobras/domain-v5.uddl";
//		String problem = "./data/domains/ickeps-petrobras/experiment-v2/15c.uddl";
		
//		String domain = "./data/domains/ickeps-petrobras/domain-v3.uddl";
//		String problem = "./data/domains/ickeps-petrobras/experiment/15c.uddl";
//		String planner = "./data/domains/ickeps-petrobras/planners/CausalGraph.spider";
		
//		String problem = "./data/domains/ickeps-petrobras/test-cases/test01.uddl";
//		String problem = "./data/domains/ickeps-petrobras/test-cases/test02.uddl";
//		String problem = "./data/domains/ickeps-petrobras/test-cases/test03.uddl";
//		String problem = "./data/domains/ickeps-petrobras/problemFull.uddl";

//		String problem = "./data/domains/ickeps-petrobras/problemSmallAllLocations.uddl";

//		String planner = "./data/domains/ickeps-petrobras/planners/PartialOrderPlanning.uddl";
		
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/test-cases/p01.uddl";
//		String planner = "./data/planners/CreateNewOperators.uddl";

//		String domain = "./data/domains/truck-world/domain.uddl";
//		String problem = "./data/domains/truck-world/test-cases/01.uddl";
//		String planner = "./data/domains/truck-world/planners/POCL.uddl";
//		String planner = "./data/domains/truck-world/planners/ForwardPOP.uddl";
		
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/test-cases/p01.uddl";
//		String planner = "./data/planners/CreateNewOperators.uddl";
		
//		String domain = "./data/domains/construction/domain.uddl";
//		String problem = "./data/domains/construction/test02.uddl";
//		String problem = "./data/domains/construction/test01.uddl";
//		String planner = "./data/domains/construction/planner.uddl";

		
//		Compile.verbose = true;
		
//		String domain = "./data/domains/human-aware planning/domain-v5.uddl";
//		String domain = "./data/domains/human-aware planning/domain-v5-comp.uddl";
//		String problem = "./data/domains/human-aware planning/test-cases/test01just_move.uddl";
//		String problem = "./data/domains/human-aware planning/test-cases/test02with_ics.uddl";
//		String problem = "./data/domains/human-aware planning/test-cases/test03simulate.uddl";
//		String problem = "./data/domains/human-aware planning/test-cases/test06simulate.uddl";
//		String problem = "./data/domains/human-aware planning/test-cases/test08overeager.uddl";
//		String problem = "./data/domains/human-aware planning/icaps-2015/out_bak/problem128.uddl";
//		String problem = "./data/domains/human-aware planning/ijcai-2015-exp/problem-base-all-sim.uddl";
//		String problem = "./data/domains/human-aware planning/ijcai-2015-exp/problem-base-2.uddl";
//		String problem = "./data/domains/human-aware planning/ijcai-2015-exp/problem-scheduling-conflict.uddl";
//		String problem = "./data/domains/human-aware planning/ijcai-2015-exp/problem-base.uddl";
//		String problem = "./data/domains/human-aware planning/icaps-2015-exp/problem256.uddl";
		
//		String problem = "/home/uwe/icaps-2015-exp/problem001.uddl";
//		String problem = "/home/uwe/icaps-2015-exp/problem050.uddl";
//		String problem = "/home/uwe/icaps-2015-exp/problem255.uddl"; 
		
//		String problem = "/home/uwe/icaps-2015-exp/problem500.uddl";
		
//		String planner = "./data/domains/human-aware planning/planner-old.uddl";
		
//		String planner = "./data/domains/human-aware planning/planner-ic-first-experiment.spider";
//		String planner = "./data/domains/human-aware planning/planner-ic-first-no-exec-new-sched.spider";
//		String planner = "./data/domains/human-aware planning/planner-ic-first-pocl.uddl";
		
//		String problem = "./data/domains/human-aware planning/icaps-2015/out/problem007.uddl";
		
//		
//		String planner = "./data/domains/human-aware planning/preIC.uddl";
		
//		Compile.verbose = true;
		
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/experiment/p03.uddl";
//		String problem = "./data/domains/ipc-match-cellar/experiment/p03.uddl";
//		String planner = "./data/domains/ipc-match-cellar/planners/LiftedPruning.spider";
//		String planner = "./data/domains/ipc-match-cellar/planners/CausalGraph.spider";
//		String planner = "./data/domains/ipc-match-cellar/planners/CreateNewOperators.spider";	
		
//		String domain = "./data/domains/transportation/domain.uddl";
//		String problem = "./data/domains/transportation/test-cases/test01.uddl";
//		String planner = "./data/domains/transportation/planners/CausalGraph.spider";
		
//		MetaCSPLogging.setLevel(Level.ALL);
		
//		String domain = "./data/domains/benchmark/social-acceptablility/domain-3vars-10res.uddl";
		
//		String domain = "./data/domains/benchmark/social-acceptablility/domain-vars-scale.uddl";
		
//		String problem = "./data/domains/benchmark/social-acceptablility/bad-2-ints/test002.uddl";
//		String problem = "./data/domains/benchmark/social-acceptablility/SA-FF-3INT/test002.uddl";
//		String problem = "./data/domains/benchmark/social-acceptablility/SA-NC-3INT/test050.uddl";
//		String problem = "./data/domains/benchmark/social-acceptablility/var-scale-test/test003.l";
//		String problem = "./data/domains/benchmark/social-acceptablility-class-3/test.uddl";
		
//		String domain = "./data/domains/benchmark/context-awareness/domain-4-ints.uddl";
//		String problem = "./data/domains/benchmark/context-awareness/4-ints/test002.uddl";
//		String planner = "./data/domains/benchmark/planner-debug-bruteforce.spider";
//		String planner = "./data/domains/benchmark/planner-debug.spider";
//		String planner = "./data/domains/benchmark/planner-experiment.spider";
//		String planner = "./data/domains/benchmark/planner-experiment-bruteforce.spider";	
		
//		String domain = "./data/domains/assisted-living/domain.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-ros-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-robotC-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-clean-kitchen.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-puts-trashcan.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test06.uddl";
//		String planner = "./data/domains/assisted-living/planner.spider";
		
		
		String domain =  "./domains/household/domain.uddl";
		String problem = "./domains/household/test-cases/test01.uddl";
		String planner = "./domains/household/planner.spider";
		
		if ( args.length == 3 ) {
			domain = args[0];
			problem = args[1];
			planner = args[2];
		}
		
		Compile.domainVersion = DomainVersion.v4;
		RunPlanner.run(domain, problem, planner, true);
		
		System.out.println("===========================================================");
		System.out.println("= Statistics");
		System.out.println("===========================================================");
		System.out.println(Module.getStats());
		
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

