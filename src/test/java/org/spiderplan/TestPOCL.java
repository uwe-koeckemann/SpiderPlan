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

import java.util.Collection;
import java.util.HashSet;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.logic.*;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

public class TestPOCL extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testSimple() {	
		ConstraintDatabase init = new ConstraintDatabase();
		Collection<Operator> O = new HashSet<Operator>();
		TypeManager tM = new TypeManager();
		
		init.add(new Statement("(s0 x a)"));		
		init.add(new OpenGoal(new Statement("(g0 x b)")));
		
		Operator a = new Operator();
		a.setName(new Atomic("op"));
		a.addPrecondition(new Statement("(?P x a)"));
		a.addEffect(new Statement("(?E x b)"));
		
		O.add(a);
		
		ConfigurationManager cM = new ConfigurationManager();
		
		cM.add("Stack");
		cM.set("Stack", "class", "SolverStack");
		cM.set("Stack", "solvers", "OpenGoalResolverSingleFlaw");
//		cM.set("Stack", "verbose", "true");
//		cM.set("Stack", "verbosity", "10");
		cM.add("OpenGoalResolverSingleFlaw");
		cM.set("OpenGoalResolverSingleFlaw", "class", "OpenGoalResolverSingleFlaw");
//		cM.set("POCL", "verbose", "true");
//		cM.set("POCL", "verbosity", "10");
//		
//		Logger.addPrintStream("Stack", System.out);
//		Logger.addPrintStream("POCL", System.out);
		
		Module s = ModuleFactory.initModule("Stack", cM); 
		
		Core testCore = new Core();
		
		testCore.setContext(init);
		testCore.setOperators(O);
		testCore.setTypeManager(tM);
		
		testCore = s.run(testCore);
		
//		System.out.println(testCore.getOutSignalsString());
		
		assertTrue(testCore.getResultingState("Stack").equals(State.Consistent));
	}
	
	public void testReqCon() {	
		

		ConstraintDatabase init = new ConstraintDatabase();
		Collection<Operator> O = new HashSet<Operator>();
		TypeManager tM = new TypeManager();
		
		init.add(new Statement("(s0 x a)"));		
		init.add(new Statement("(s1 y a)"));
//		init.add(new Statement("(g0,x,b)"));
//		init.add(new Statement("(g1,y,b)"));
		
		init.add(new OpenGoal(new Statement("(g0 x b)")));
		init.add(new OpenGoal(new Statement("(g1 y b)")));
		
		Operator a = new Operator();
		a.setName(new Atomic("op1"));
		a.addPrecondition(new Statement("(?P1 x a)"));
		a.addPrecondition(new Statement("(?P2 y b)"));
		a.addEffect(new Statement("(?E x b)"));
		
		O.add(a);
		
		a = new Operator();
		a.setName(new Atomic("op2"));
		a.addPrecondition(new Statement("(?P1 y a)"));
		a.addPrecondition(new Statement("(?P2 x b)"));
		a.addEffect(new Statement("(?E y b)"));
		
		O.add(a);
		
		ConfigurationManager cM = new ConfigurationManager();
		
		cM.add("Stack");
		cM.set("Stack", "class", "SolverStack");
		cM.set("Stack", "solvers", "OpenGoalResolverSingleFlaw");
//		cM.set("Stack", "verbose", "true");
//		cM.set("Stack", "verbosity", "10");
		cM.add("OpenGoalResolverSingleFlaw");
		cM.set("OpenGoalResolverSingleFlaw", "class", "OpenGoalResolverSingleFlaw");
//		cM.set("POCL", "verbose", "true");
//		cM.set("POCL", "verbosity", "10");
		
//		Logger.addPrintStream("Stack", System.out);
//		Logger.addPrintStream("POCL", System.out);
		
		Module s = ModuleFactory.initModule("Stack", cM); 
		
		Core testCore = new Core();
		
		testCore.setContext(init);
		testCore.setOperators(O);
		testCore.setTypeManager(tM);
		
		testCore = s.run(testCore);
		
//		System.out.println(testCore.getOutSignalsString());
		
		assertTrue(testCore.getResultingState("Stack").equals(State.Consistent));
	}
	
//	public void testMove() {	
//		ConstraintDatabase init = new ConstraintDatabase();
//		Collection<Operator> O = new ArrayList<Operator>();
//		TypeManager tM = new TypeManager();
//		tM.addEnumType("loc", "a,b,c,d");
//		tM.attachTypes(new Atomic("at"), new Term("loc"));
//		
//		init.add(new Statement("(s0,at,a)"));		
//			
//		init.add(new OpenGoal(new Statement("(g0,at,d)")));
//		
//		
//		Operator a = new Operator();
//		a.setName(new Atomic("op-a-b"));
//		a.addPrecondition(new Statement("(P,at,a)"));
//		a.addEffect(new Statement("(E,at,b)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		a = new Operator();
//		a.setName(new Atomic("op-b-a"));
//		a.addPrecondition(new Statement("(P,at,b)"));
//		a.addEffect(new Statement("(E,at,a)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		a = new Operator();
//		a.setName(new Atomic("op-b-c"));
//		a.addPrecondition(new Statement("(P,at,b)"));
//		a.addEffect(new Statement("(E,at,c)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		a = new Operator();
//		a.setName(new Atomic("op-c-b"));
//		a.addPrecondition(new Statement("(P,at,c)"));
//		a.addEffect(new Statement("(E,at,b)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		a = new Operator();
//		a.setName(new Atomic("op-c-d"));
//		a.addPrecondition(new Statement("(P,at,c)"));
//		a.addEffect(new Statement("(E,at,d)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		a = new Operator();
//		a.setName(new Atomic("op-d-c"));
//		a.addPrecondition(new Statement("(P,at,d)"));
//		a.addEffect(new Statement("(E,at,c)"));
//		a.addConstraint(new AllenConstraint(new Term("E"), Type.Duration, new Interval(new Term("1"), new Term("inf"))));
//		a.addConstraint(new AllenConstraint(new Term("P"), new Term("E"), Type.Meets));
//		O.add(a);
//		
//		ConfigurationManager cM = new ConfigurationManager();
//		
//		cM.add("Stack");
//		cM.set("Stack", "class", "SolverStack");
//		cM.set("Stack", "solvers", "Time,POCL");
//		cM.set("Stack", "verbose", "true");
//		cM.set("Stack", "verbosity", "10");
//		cM.add("POCL");
//		cM.set("POCL", "class", "POCL");
//		cM.set("POCL", "verbose", "true");
//		cM.set("POCL", "verbosity", "10");
//		cM.add("Time");
//		cM.set("Time", "class", "STPSolver");
//		cM.set("Time", "verbose", "true");
//		cM.set("Time", "verbosity", "10");
//		
//		Logger.addPrintStream("Stack", System.out);
//		Logger.addPrintStream("POCL", System.out);
//		Logger.addPrintStream("Time", System.out);
//		
//		Module s = ModuleFactory.initModule("Stack", cM); 
//		
//		Core testCore = new Core();
//		
//		testCore.setContext(init);
//		testCore.setOperators(O);
//		testCore.setTypeManager(tM);
//		
//		testCore = s.run(testCore);
//		
//		System.out.println(testCore.getOutSignalsString());
//		
//		assertTrue(testCore.getResultingState("Stack").equals(State.Consistent));
//	}
}

