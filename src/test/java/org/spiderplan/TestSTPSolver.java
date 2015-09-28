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
import org.spiderplan.modules.STPSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.TemporalIntervalQuery;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.types.TypeManager;

import junit.framework.TestCase;

/**
 * Test the {@link MathSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestSTPSolver extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test with simple satisfiable operations
	 */
	public void test1() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(greater-than (duration i) 5)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertTrue(r.getState().equals(State.Consistent));
	}
	
	public void test2() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(greater-than-or-equals (duration i) 10)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertTrue(r.getState().equals(State.Consistent));
	}
	
	public void test3() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(greater-than-or-equals (duration i) 11)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertFalse(r.getState().equals(State.Consistent));
	}
	
	public void test4() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(greater-than (duration i) 11)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertFalse(r.getState().equals(State.Consistent));
	}
	
	public void test5() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(less-than (duration i) 11)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertTrue(r.getState().equals(State.Consistent));
	}
	
	public void test6() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(less-than-or-equals (duration i) 10)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertTrue(r.getState().equals(State.Consistent));
	}
	
	public void test7() {
		ConstraintDatabase cDB = new ConstraintDatabase();
	
		cDB.add(new Statement("(i x v)"));
		
		cDB.add(new AllenConstraint(new Atomic("(at i (interval 10 10) (interval 20 20))")));
		cDB.add(new TemporalIntervalQuery(new Atomic("(less-than-or-equals (duration i) 5)")));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "v");
		tM.attachTypes("x=v");
		
		Core c = new Core();
		c.setContext(cDB);
		c.setTypeManager(tM);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("STPSolver");
				
		STPSolver mS = new STPSolver("STPSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
				
		assertFalse(r.getState().equals(State.Consistent));
	}
}

