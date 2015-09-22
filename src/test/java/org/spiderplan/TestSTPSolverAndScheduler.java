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

import java.util.ArrayList;
import junit.framework.TestCase;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.PossibleIntersection;
import org.spiderplan.representation.constraints.ReusableResourceCapacity;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.TemporalIntervalLookup;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.modules.solvers.Core.State;
//import org.spiderplan.temporal.metaCSP.MetaCSPAdapterWithHistory;

public class TestSTPSolverAndScheduler extends TestCase {
	TypeManager tM = new TypeManager();
	ConstraintDatabase context;
	
	ConfigurationManager cM = new ConfigurationManager();		
	Module solver;
	Core testCore;
	
	@Override
	public void setUp() throws Exception {
		tM = new TypeManager();
		context = new ConstraintDatabase();
		
		cM.set("solver","class","SolverStack");
		cM.set("solver","solvers","stpSolver,scheduler");
		cM.set("stpSolver","class","STPSolver");
		cM.set("stpSolver","horizon","50000");		
		cM.set("scheduler","class","SchedulingSolver");
		
		/**
		 * Uncomment following block to activate solver output
		 */
//		cM.set("solver","verbose","true");
//		cM.set("solver","verbosity","4");
//		cM.set("stpSolver","verbose","true");
//		cM.set("stpSolver","verbosity","0");
//		cM.set("scheduler","verbose","true");
//		cM.set("scheduler","verbosity","1");
//		
//		Logger.addPrintStream("solver", System.out);
//		Logger.addPrintStream("stpSolver", System.out);
//		Logger.addPrintStream("scheduler", System.out);	
				
		solver =  ModuleFactory.initModule("solver",cM);
		
		testCore = new Core();
		testCore.setContext(context);
		testCore.setTypeManager(tM);
	}

	@Override
	public void tearDown() throws Exception {
		Logger.stop = false;
	}


	public void testBeforeConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new AllenConstraint("a1 Duration [1,10]") );
		context.add( new AllenConstraint("a2 Duration [11,20]") );
		context.add( new AllenConstraint("a1 Before a2 [1,10]") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testDurationConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new AllenConstraint("a1 Duration [1,10]") );

		testCore = solver.run(testCore);	
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}

	public void testEqualsConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
		context.add( new AllenConstraint("a1 Duration [1,10]") );
		context.add( new AllenConstraint("a2 Duration [1,10]") );
		context.add( new Statement("(a1 variable1 a)") );
		context.add( new Statement("(a2 variable2 a)") );

		context.add( new AllenConstraint("a1 Equals a2") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	

	
	public void testTwoEqualsConsistent() throws Exception {
			tM.addSimpleEnumType("value", "a");
			tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
			tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
			context.add( new Statement("(a1 variable1 a)") );
			context.add( new Statement("(a2 variable2 b)") );
			
			context.add( new AllenConstraint("a1 Equals a2") );
			context.add( new AllenConstraint("a2 Equals a1") );
			
			testCore = solver.run(testCore);		
			assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testDuringAndContains() throws Exception {
			tM.addSimpleEnumType("value", "a,b");
			tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
			tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
			context.add( new Statement("(a1 variable1 a)") );
			context.add( new Statement("(a2 variable2 b)") );
			
			context.add( new AllenConstraint("a1 Duration [15,15]") );
			context.add( new AllenConstraint("a1 Release [0,0]") );
			context.add( new AllenConstraint("a2 Release [2,2]") );
			context.add( new AllenConstraint("a2 Duration [10,10]") );
			context.add( new AllenConstraint("a1 Contains a2 [1,inf] [1,inf]") );
			
			testCore = solver.run(testCore);
			assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testOverlapsMeetsConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
	
		context.add( new AllenConstraint("a2 Duration [30,30]") );
		context.add( new AllenConstraint("a1 Duration [20,20]") );
		context.add( new AllenConstraint("a3 Duration [20,20]") );
		context.add( new Statement("(a2 variable1 a)") );
		context.add( new Statement("(a1 variable2 b)") );
		context.add( new Statement("(a3 variable2 b)") );

		context.add( new AllenConstraint("a1 Overlaps a2 [1,inf]") );
		context.add( new AllenConstraint("a2 Overlaps a3 [1,inf]") );
		context.add( new AllenConstraint("a1 Meets a3") );
		
		testCore = solver.run(testCore);
		

		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testTwoOverlapsInconsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable1  a)") );
		context.add( new Statement("(a2 variable2  b)") );
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 OverlappedBy a2 [1,inf]") );
		context.add( new AllenConstraint("a2 OverlappedBy a1 [1,inf]") );

		testCore = solver.run(testCore);
				

		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testStartStartEndEndEqualsConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable1 a)") );
		context.add( new Statement("(a2 variable2 b)") );

		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 StartStart a2 [0,inf]") );
		context.add( new AllenConstraint("a1 EndEnd a2 [0,inf]") );
		context.add( new AllenConstraint("a1 Equals a2") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}

	public void testMeetsInconsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );

		context.add( new AllenConstraint("a2 Duration [1,5]") );
		context.add( new AllenConstraint("a1 Meets a2") );
		context.add( new AllenConstraint("a2 Meets a1") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testEqualsInconsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable1 a)") );
		context.add( new Statement("(a2 variable2 b)") );

		context.add( new AllenConstraint("a1 Duration [1,5]") );
		context.add( new AllenConstraint("a2 Duration [10,100]") );


		context.add( new AllenConstraint("a1 Equals a2") );
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}

	public void testEqualsInconsistent2() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
				
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );

		context.add( new AllenConstraint("a1 Duration [10,20]") );
		context.add( new AllenConstraint("a2 Duration [1,5]") );
		context.add( new AllenConstraint("a1 Equals a2") );
		testCore = solver.run(testCore);

		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testBeforeInconsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
					
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [10,10]") );
		context.add( new AllenConstraint("a2 Release [10,10]") );
		context.add( new AllenConstraint("a1 Before a2 [1,inf]"));
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testStateVariableSchedulingInconsistent1() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
					
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
	
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [0,0]") );
		
		testCore.setContext(context);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testStateVariableSchedulingConsistent1() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
				
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
	
		context.add( new AllenConstraint("a1 Release [0,20]") );
		context.add( new AllenConstraint("a2 Release [0,0]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testStateVariableSchedulingConsistent2() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new Statement("(a3 variable c)") );
					
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a3 Duration [10,10]") );
	
		context.add( new AllenConstraint("a1 Release [0,20]") );
		context.add( new AllenConstraint("a2 Release [0,30]") );
		context.add( new AllenConstraint("a2 Release [0,0]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testStateVariableSchedulingInconsistent2() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new Statement("(a3 variable c)") );		
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a3 Duration [10,10]") );
	
		context.add( new AllenConstraint("a1 Release [0,10]") );
		context.add( new AllenConstraint("a2 Release [0,10]") );
		context.add( new AllenConstraint("a3 Release [0,10]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testStateVariableSchedulingConsistent3() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );

		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Meets a2") );
				
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));	
	}
	
	public void testStateVariableSchedulingConsistent4() throws Exception {
		tM.addSimpleEnumType("value", "a");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		
		context.add( new AllenConstraint("a1 Duration [50,inf]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	/**
	 * Usage of Meets used to cause a problem because of bad intersect method.
	 * This checks that this problem is resolved.
	 */
	public void testStateVariableSchedulingConsistent5() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a2 variable a)") );
		context.add( new Statement("(a3 variable b)") );
		
		context.add( new AllenConstraint("a2 Duration [1,inf]") );
		context.add( new AllenConstraint("a3 Duration [1,inf]") );

		context.add( new AllenConstraint("a3 Meets a2") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));			
	}
	
	public void testStateVariableSchedulingConsistent6() throws Exception {
		tM.addSimpleEnumType("value", "a");
		tM.attachTypes(new Atomic("variable1"), Term.createConstant("value") );
		tM.attachTypes(new Atomic("variable2"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable1 a)") );
		context.add( new Statement("(a2 variable2 a)") );
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [10,10]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testStateVariableSchedulingConsistent7() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new Statement("(a3 variable a)") );
		
		context.add( new AllenConstraint("a1 Duration [1,inf]") );
		context.add( new AllenConstraint("a2 Duration [1,inf]") );
		context.add( new AllenConstraint("a3 Duration [1,inf]") );

		context.add( new AllenConstraint("a1 Before a2 [1,2]") );
		context.add( new AllenConstraint("a2 Before a3 [1,2]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testStateVariableSchedulingConsistent8() throws Exception {
		tM.addSimpleEnumType("value", "a");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
	
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable a)") );
		
		context.add( new AllenConstraint("a1 Overlaps a2 [10,inf]") );	
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testStateVariableSchedulingConsistent9() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c,d");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new Statement("(a3 variable c)") );
		context.add( new Statement("(a4 variable d)") );
		
		context.add( new AllenConstraint("a1 Meets a2") );	
		context.add( new AllenConstraint("a2 Before a3 [1,inf]") );
		context.add( new AllenConstraint("a3 Before a4 [1,inf]") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testReusableResourceProblemConsistent1() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );
		context.add( new Statement("(a3 rr1 1)") );
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [20,20]") );
		context.add( new AllenConstraint("a3 Duration [10,10]") );
		
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [50,50]") );
		context.add( new AllenConstraint("a3 Release [150,150]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testReusableResourceProblemConsistent2() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 2));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );

		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [0,0]") );
	
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
		
	public void testReusableResourceProblemConsistent4() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 2));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );
		context.add( new Statement("(a3 rr1 1)") );
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a3 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [2,2]") );
		context.add( new AllenConstraint("a3 Release [1,1]") );
			
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}

	public void testReusableResourceProblemInconsistent1() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );
		context.add( new Statement("(a3 rr1 1)") );
		context.add( new AllenConstraint("a1 Meets a2") );
		context.add( new AllenConstraint("a2 Meets a3") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testReusableResourceProblemInconsistent2() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));		
		
		context.add( new Statement("(a1 rr1 3)") );
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testReusableResourceProblemConsistent5() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
		context.add( new ReusableResourceCapacity(new Atomic("rr2"), 1));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr2 1)") );	
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Release [0,0]") );
		context.add( new AllenConstraint("a2 Release [0,0]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testReusableResourceProblemInconsistent3() {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
		
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new AllenConstraint("a1 Release [39,4799]") );
		context.add( new AllenConstraint("a1 Deadline [97,4857]") );

		context.add( new Statement("(a2 rr1 1)") );
		context.add( new AllenConstraint("a2 Release [39,4799]") );
		context.add( new AllenConstraint("a2 Deadline [5000,5000]") );

		context.add( new Statement("(a3 rr1 1)") );
		context.add( new AllenConstraint("a3 Release [137,4897]") );
		context.add( new AllenConstraint("a3 Deadline [191,4951]") );
	
		context.add( new Statement("(a4 rr1 1)") );
		context.add( new AllenConstraint("a4 Release [192,4952]") );
		context.add( new AllenConstraint("a4 Deadline [5000,5000]") );

		context.add( new Statement("(a5 rr1 1)") );
		context.add( new AllenConstraint("a5 Release [239,4999]") );
		context.add( new AllenConstraint("a5 Deadline [5000,5000]") );
		
		cM.set("stpSolver", "horizon", "10");
		solver =  ModuleFactory.initModule("solver",cM);
		testCore = solver.run(testCore);
		
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}

	public void testTwoConsistencyChecksConsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Before a2 [1,2]") );
		
		ConstraintDatabase context2 = context.copy();
		
		context.add( new Statement("(a3 variable b)") );
		context.add( new AllenConstraint("a3 Duration [10,10]") );
		context.add( new AllenConstraint("a1 Before a3 [1,2]") );
		
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));		 
		
		testCore.setContext(context2);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));	
	}
	
	public void testDuringInconsistent() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c,d");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		context.add( new Statement("(a3 variable c)") );
		context.add( new Statement("(a4 variable d)") );
		
		context.add( new AllenConstraint("a1 Release [153,153]") );
		context.add( new AllenConstraint("a1 Duration [30,30]") );
		context.add( new AllenConstraint("a2 Release [203,203]") );
		context.add( new AllenConstraint("a2 Duration [30,30]") );
		context.add( new AllenConstraint("a3 Release [102,102]") );
		context.add( new AllenConstraint("a3 Duration [50,50]") );
		context.add( new AllenConstraint("a4 Release [102,102]") );
		context.add( new AllenConstraint("a4 Duration [100,100]") );
		
		context.add( new AllenConstraint("a3 During a1 [1,inf] [1,inf]") );
		context.add( new AllenConstraint("a4 During a2 [1,inf] [1,inf]") );

		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testReusableResourceProblemBecomesSolvable() throws Exception {	
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
						
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );

		ConstraintDatabase old = context.copy();
		
//		s.verbose = true;
//		s.verbosity = 10;
//		Logger.addPrintStream(s.name, System.out);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
		
		context.add( new AllenConstraint("a1 Release [10,10]") );
		context.add( new AllenConstraint("a2 Release [5,5]") );

		testCore.setContext(context);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
		
		testCore.setContext(old);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
		
		
		ConstraintDatabase backtrack2 = old.copy();
		backtrack2.add( new  AllenConstraint("a2 Before a1 [1,10]") );
	
		ConstraintDatabase backtrack1 = old.copy();
		backtrack1.add( new  AllenConstraint("a1 Before a2 [1,10]") );
		
		testCore.setContext(backtrack1);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
		
		testCore.setContext(backtrack2);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void testReusableResourceProblemBecomesSolvable1() throws Exception {				
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 1));
						
		context.add( new Statement("(a1 rr1 1)") );
		context.add( new Statement("(a2 rr1 1)") );
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
	
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
		
		context.add( new AllenConstraint("a1 Release [10,10]") );
		context.add( new AllenConstraint("a2 Release [5,5]") );

		testCore.setContext(context);
		testCore = solver.run(testCore);
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));		
	}
	
	public void testBeforeAndDuringInconsistent() throws Exception {
		tM.addSimpleEnumType("var", "x1,x2,x3,x4,x5,x6");
		tM.addSimpleEnumType("val", "y");
		tM.attachTypes(new Atomic("(p var)"), Term.createConstant("val") );
		
		context.add( new Statement( Term.createConstant("k1"), new Atomic("(p x1)"), Term.createConstant("y")));
		context.add( new Statement( Term.createConstant("k2"), new Atomic("(p x2)"), Term.createConstant("y")));
		context.add( new Statement( Term.createConstant("k3"), new Atomic("(p x3)"), Term.createConstant("y")));
		context.add( new Statement( Term.createConstant("k4"), new Atomic("(p x4)"), Term.createConstant("y")));
	
		context.add( new AllenConstraint("k1 Duration [7,7]") );
		context.add( new AllenConstraint("k2 Duration [2,2]") );
		context.add( new AllenConstraint("k3 Duration [2,2]") );
		context.add( new AllenConstraint("k4 Duration [2,2]") );
	
		context.add( new AllenConstraint("k2 During k1 [1,inf] [1,inf]") );
		context.add( new AllenConstraint("k3 During k1 [1,inf] [1,inf]") );
		context.add( new AllenConstraint("k3 Before k2 [1,inf]") );
		context.add( new AllenConstraint("k3 Before k4 [1,inf]") );
		context.add( new AllenConstraint("k4 Before k2 [1,inf]") );
	
		testCore = solver.run(testCore);		
				
		assertTrue(testCore.getResultingState("solver").equals(State.Inconsistent));
	}
	
	public void testReusableResourceProblemConsistent6() throws Exception {
		StopWatch.keepAllTimes = true;
		
		tM.addSimpleEnumType("value", "a,b");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add( new AllenConstraint("a1 Duration [10,10]") );
		context.add( new AllenConstraint("a2 Duration [10,10]") );
		context.add( new Statement("(a1 variable a)") );
		context.add( new Statement("(a2 variable b)") );
		
		cM.set("stpSolver", "horizon", "1000");
		solver = ModuleFactory.initModule("solver", cM);
			
		testCore = solver.run(testCore);
		assertTrue( testCore.getResultingState("solver").equals(State.Consistent) );
		
		testCore = solver.run(testCore);
		assertTrue( testCore.getResultingState("solver").equals(State.Consistent) );	
	}	
	
	public void testOrderMattersBug() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c,d");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		context.add(new Statement("(s0 variable a)"));
		context.add(new Statement("(s1 variable b)"));
		context.add(new Statement("(s2 variable d)"));
		context.add(new Statement("(e0 variable c)"));
		
		context.add( new AllenConstraint("s0 Release [0,0]"));
		context.add( new AllenConstraint("s0 Duration [133,inf]"));
		context.add( new AllenConstraint("s1 At [150,150] [300,300]"));
		context.add( new AllenConstraint("s0 Before e0 [1,inf]"));
		context.add( new AllenConstraint("s2 At [500,500] [700,700]"));
		context.add( new AllenConstraint("e0 Duration [100,inf]"));
		
		testCore = solver.run(testCore);
		
		assertTrue( testCore.getResultingState("solver").equals(State.Consistent) );	
		TemporalIntervalLookup ti = testCore.getContext().get(TemporalIntervalLookup.class).get(0);
		assertTrue( ti.getEST(Term.createConstant("e0")) == 300 );
		assertTrue( ti.getEET(Term.createConstant("e0")) == 400 );
		
	}
	
	public void testResourceProblemNeedsResolving() throws Exception {
		context.add( new ReusableResourceCapacity(new Atomic("rr1"), 2));
		
		context.add( new Statement("(a1 rr1 2)") );
		context.add( new Statement("(a2 rr1 2)") );
		context.add( new AllenConstraint("a1 At [0,0] [100,200]") );
		context.add( new AllenConstraint("a2 At [50,200] [300,300]") );
		
		testCore = solver.run(testCore);
		assertTrue( testCore.getResultingState("solver").equals(State.Consistent) );	
		
		 // added scheduling decision to the two existing temporal constraints
		assertTrue( testCore.getContext().get(AllenConstraint.class).size() == 3 );
	}
	
	public void testPossibleIntersection() throws Exception {
		tM.addSimpleEnumType("value", "a,b,c,d");
		tM.attachTypes(new Atomic("variable"), Term.createConstant("value") );
		
		ArrayList<Term> intervals = new ArrayList<Term>();
		intervals.add(Term.createConstant("s0"));
		intervals.add(Term.createConstant("s1"));
		intervals.add(Term.createConstant("s2"));
		intervals.add(Term.createConstant("s3"));
		
		PossibleIntersection piC = new PossibleIntersection(intervals);
		
		assertTrue(piC.getIntervals().size() == 4);
		
		context.add(new Statement("(s0 variable a)"));
		context.add(new Statement("(s1 variable b)"));
		context.add(new Statement("(s2 variable d)"));
		context.add(new Statement("(s3 variable c)"));
		context.add(new AllenConstraint("s0 Duration [1,inf]"));
		context.add(new AllenConstraint("s1 Duration [1,inf]"));
		context.add(new AllenConstraint("s2 Duration [1,inf]"));
		context.add(new AllenConstraint("s3 Duration [1,inf]"));
		
		testCore = solver.run(testCore);
				
		// all intervals can be separated by StateVariable scheduling
		assertTrue( testCore.getResultingState("solver").equals(State.Consistent) );
		// 6 temporal constraints added for scheduling
		assertTrue( testCore.getContext().get(AllenConstraint.class).size() == (4+6) );
				
		// after adding an intersection constraint the problem becomes inconsistent
		context.add(piC);
		testCore.setContext(context);
		testCore = solver.run(testCore);
		assertTrue( testCore.getResultingState("solver").equals(State.Inconsistent) );		
	}
}
