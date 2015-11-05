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

import org.spiderplan.modules.MathSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.resources.ReusableResourceCapacity;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

import junit.framework.TestCase;

/**
 * Test the {@link MathSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestScheduling extends TestCase {
	
	Term INF = Term.createConstant("inf");
	Term L0 = Term.createInteger(0);
	Term L1 = Term.createInteger(1);
			
	
	Term I1 = Term.createConstant("I1");
	Term I2 = Term.createConstant("I2");
	Term I3 = Term.createConstant("I3");
	
	Term I4 = Term.createConstant("I4");
	Term I5 = Term.createConstant("I5");
	Term I6 = Term.createConstant("I6");
	
	Statement s1 = new Statement(I1, new Atomic("x"), Term.createInteger(1));
	Statement s2 = new Statement(I2, new Atomic("x"), Term.createInteger(1));
	Statement s3 = new Statement(I3, new Atomic("x"), Term.createInteger(1));
	
	Statement s4 = new Statement(I4, new Atomic("y"), Term.createInteger(1));
	Statement s5 = new Statement(I5, new Atomic("y"), Term.createInteger(1));
	Statement s6 = new Statement(I6, new Atomic("y"), Term.createInteger(1));
	
	ConfigurationManager cM = new ConfigurationManager();		
	Module solver;


	@Override
	public void setUp() throws Exception {
		cM.set("solver","class","SolverStack");
		cM.set("solver","solvers","stpSolver,scheduler");
		cM.set("stpSolver","class","STPSolver");
		
		cM.set("solver","verbose","true");
		cM.set("solver","verbosity","4");
		
//		cM.set("stpSolver","verbose","true");
//		cM.set("stpSolver","verbosity","0");
		cM.set("scheduler","class","SchedulingSolver");
//		cM.set("scheduler","verbose","true");
//		cM.set("scheduler","verbosity","1");
		
//		Logger.addPrintStream("solver", System.out);
//		Logger.addPrintStream("stpSolver", System.out);
//		Logger.addPrintStream("scheduler", System.out);	
				
		solver =  ModuleFactory.initModule("solver",cM);
	}

	@Override
	public void tearDown() throws Exception {
		Logger.reset();				
	}

	public void test1() {
		ConstraintDatabase cDB = new ConstraintDatabase();

		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(5), Term.createInteger(5))));
		cDB.add(new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(5), Term.createInteger(5))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(10)),
				new Interval(Term.createInteger(20), Term.createInteger(20))));
		
		cDB.add(new ReusableResourceCapacity(new Atomic("x"), 2));
		
		Core testCore = new Core();
		testCore.setContext( cDB );
	
		testCore = solver.run(testCore);
		
		assertTrue(testCore.getResultingState("solver").equals(State.Consistent));
		assertTrue(testCore.getContext().size() == 9);
	}
	
	public void test2() {
		ConstraintDatabase cDB = new ConstraintDatabase();

		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(5), Term.createInteger(5))));
		cDB.add(new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(5), Term.createInteger(5))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(20), Term.createInteger(20))));
		
		cDB.add(new ReusableResourceCapacity(new Atomic("x"), 2));

		Core testCore = new Core();
		testCore.setContext( cDB );

		testCore = solver.run(testCore);
		
		assertFalse(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void test3() {
		ConstraintDatabase cDB = new ConstraintDatabase();

		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(600), Term.createInteger(600))));
		cDB.add(new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(600)),
				new Interval(Term.createInteger(0), Term.createInteger(600))));
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createInteger(600))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createInteger(600))));
		
		cDB.add(new ReusableResourceCapacity(new Atomic("(x)"), 1));

		Core testCore = new Core();
		testCore.setContext( cDB );

		testCore = solver.run(testCore);
		
		assertFalse(testCore.getResultingState("solver").equals(State.Consistent));
	}
	
	public void test4_needs_matching() {
		ConstraintDatabase cDB = new ConstraintDatabase();

		Statement sv1 = new Statement(I1, new Atomic("(x a)"), Term.createInteger(1));
		Statement sv2 = new Statement(I2, new Atomic("(x a)"), Term.createInteger(1));
		
		cDB.add(sv1);
		cDB.add(sv2);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(0)),
				new Interval(Term.createInteger(600), Term.createInteger(600))));
		cDB.add(new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0), Term.createInteger(600)),
				new Interval(Term.createInteger(0), Term.createInteger(600))));
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createInteger(600))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createInteger(600))));
		
		cDB.add(new ReusableResourceCapacity(new Atomic("(x ?V)"), 1));

		Core testCore = new Core();
		testCore.setContext( cDB );

		testCore = solver.run(testCore);
		
		assertFalse(testCore.getResultingState("solver").equals(State.Consistent));
	}
}

