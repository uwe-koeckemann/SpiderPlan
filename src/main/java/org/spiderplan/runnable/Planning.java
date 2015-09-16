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
		if ( args.length >= 2 && !args[0].contains("[]")) { 			// Single problem
			RunSingleProblem.run(args);		
		} else if ( args.length == 1 ) {	// Experiment
			RunExperiment.run(args[0]);	
		} else {								// Default demo problem
			String[] defaultArgs = { "./domains/household/planner.spider", "./domains/household/domain.uddl", "./domains/household/test-cases/test01.uddl" };
			RunSingleProblem.run(defaultArgs);
		}
	}
}

