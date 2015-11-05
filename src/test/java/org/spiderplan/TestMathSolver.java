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
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.math.MathConstraint;
import org.spiderplan.representation.logic.Atomic;

import junit.framework.TestCase;

/**
 * Test the {@link MathSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestMathSolver extends TestCase {

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
		cDB.add(new MathConstraint(new Atomic("(add 1 2 ?X1)")));
		cDB.add(new MathConstraint(new Atomic("(add ?X1 2 5)")));
		cDB.add(new MathConstraint(new Atomic("(mult 4 2 8)")));
		cDB.add(new MathConstraint(new Atomic("(div 10 3 3)")));
		cDB.add(new MathConstraint(new Atomic("(mod 10 7 3)")));
		cDB.add(new MathConstraint(new Atomic("(sub 5 1 ?X2)")));
		cDB.add(new MathConstraint(new Atomic("(sub 10 ?X2 6)")));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("MathSolver");
				
		MathSolver mS = new MathSolver("MathSolver", cM);
		
		mS.run(c);
				
		assertTrue(c.getResultingState("MathSolver").equals(State.Consistent));
	}
	
	/**
	 * Test with simple unsatisfiable operations
	 */
	public void test2() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new MathConstraint(new Atomic("(mult 4 2 7)")));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("MathSolver");
				
		MathSolver mS = new MathSolver("MathSolver", cM);
		
		mS.run(c);
				
		assertFalse(c.getResultingState("MathSolver").equals(State.Consistent));
	}
	
	/**
	 * Test with simple satisfiable float operations
	 */
	public void test3() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new MathConstraint(new Atomic("(mult 4.0 2.0 ?X)")));
		cDB.add(new MathConstraint(new Atomic("(add 3.0 5.0 ?X)")));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("MathSolver");
				
		MathSolver mS = new MathSolver("MathSolver", cM);
		
		mS.run(c);
						
		assertTrue(c.getResultingState("MathSolver").equals(State.Consistent));
	}
	
	/**
	 * Test with bad types
	 */
	public void test4() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new MathConstraint(new Atomic("(mod 4.0 2.0 ?X)")));
				
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("MathSolver");
				
		MathSolver mS = new MathSolver("MathSolver", cM);
		
		boolean caught = false;
		
		try {
			mS.run(c);
		} catch ( IllegalStateException e ) {
			caught = true;
		}
						
		assertTrue(caught);
	}
}

