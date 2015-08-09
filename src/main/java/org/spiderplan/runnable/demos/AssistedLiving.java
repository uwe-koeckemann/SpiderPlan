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
package org.spiderplan.runnable.demos;

import org.spiderplan.modules.solvers.Module;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.Compile.DomainVersion;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.runnable.RunPlanner;
import org.spiderplan.tools.profiler.Profiler;
import org.spiderplan.tools.stopWatch.StopWatch;

public class AssistedLiving {

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws UnknownThing 
	 * @throws NonGroundThing 
	 */
	public static void main(String[] args) throws ParseException {	

		String domain = "./data/domains/assisted-living/domain.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-ros-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-robot-move.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-clean-kitchen.uddl";
//		String problem = "./data/domains/assisted-living/test-cases/test-human-puts-trashcan.uddl";
		String problem = "./data/domains/assisted-living/test-cases/test01.uddl";
		String planner = "./data/domains/assisted-living/planner.spider";
		
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
		
		System.out.println("===========================================================");
		System.out.println("= Profile");
		System.out.println("===========================================================");
		System.out.println(Profiler.getString());
	}
}

