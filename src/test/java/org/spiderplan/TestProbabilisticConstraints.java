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
package org.spiderplan;

import java.util.logging.Level;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.ProbabilisticConstraint;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

public class TestProbabilisticConstraints extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
		
	}
	
	public void testTestSampling() {
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("activities", "a1,a2,a3,a4,a5,a6,a7,a8,a9,a10");
		
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 (activity ?H) ?A)"));
		s.add(new AllenConstraint(new Atomic("(duration s1 (interval ?D ?D))")));
		s.add(new ProbabilisticConstraint(new Atomic("(random-variable ?H (list h1 h2 h3 h4 h5 h6 h7 h8 h9 h10))")));
		s.add(new ProbabilisticConstraint(new Atomic("(random-variable ?A activities)")));
		s.add(new ProbabilisticConstraint(new Atomic("(random-variable ?D (interval 1 100))")));
		s.add(new ProbabilisticConstraint(new Atomic("(sample ?H)")));
		s.add(new ProbabilisticConstraint(new Atomic("(sample ?A)")));
		s.add(new ProbabilisticConstraint(new Atomic("(sample ?D)")));
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("probSolver","class","SamplingSolver");
//		cM.set("probSolver","verbose","true");
//		cM.set("probSolver","verbosity","1");
//		Logger.addPrintStream("probSolver", System.out);
		
		Module probSolver = ModuleFactory.initModule("probSolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext(s);
		testCore.setTypeManager(tM);
		
		testCore = probSolver.run(testCore);
		
		assertTrue( testCore.getContext().getVariableTerms().isEmpty() );
	}
}

