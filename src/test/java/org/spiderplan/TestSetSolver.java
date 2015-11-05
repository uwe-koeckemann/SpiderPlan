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

import org.spiderplan.modules.GraphSolver;
import org.spiderplan.modules.SetSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.set.SetConstraint;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

/**
 * Test the {@link GraphSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestSetSolver extends TestCase {

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
		cDB.add(new SetConstraint("(add set1 1)"));
		cDB.add(new SetConstraint("(in set1 1)"));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("SetSolver");
				
		SetSolver sS = new SetSolver("SetSolver", cM);
		
		sS.run(c);
				
		assertTrue(c.getResultingState("SetSolver").equals(State.Consistent));
	}
	
	/**
	 * Test with simple satisfiable operations
	 */
	public void test2() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new SetConstraint("(add set1 1)"));
		cDB.add(new SetConstraint("(in set1 2)"));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("SetSolver");
				
		SetSolver sS = new SetSolver("SetSolver", cM);
		
		sS.run(c);
				
		assertFalse(c.getResultingState("SetSolver").equals(State.Consistent));
	}
	
	/**
	 * Test subsets
	 */
	public void test3() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new SetConstraint("(add S1 1)"));
		cDB.add(new SetConstraint("(add S1 2)"));
		cDB.add(new SetConstraint("(add S1 3)"));
		
		cDB.add(new SetConstraint("(add S2 1)"));
		cDB.add(new SetConstraint("(add S2 2)"));		
		
		cDB.add(new SetConstraint("(subset S2 S1)"));
		cDB.add(new SetConstraint("(subset S1 S1)"));
		cDB.add(new SetConstraint("(subset S2 S2)"));
		
		cDB.add(new SetConstraint("(proper-subset S2 S1)"));
		
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("SetSolver");
				
		SetSolver sS = new SetSolver("SetSolver", cM);
		
		sS.run(c);
				
		assertTrue(c.getResultingState("SetSolver").equals(State.Consistent));
	}
	
	/**
	 * Test subsets
	 */
	public void test4() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new SetConstraint("(add S1 1)"));
		cDB.add(new SetConstraint("(add S1 2)"));
		cDB.add(new SetConstraint("(add S1 3)"));
		
		cDB.add(new SetConstraint("(add S2 1)"));
		cDB.add(new SetConstraint("(add S2 2)"));		
		cDB.add(new SetConstraint("(add S2 3)"));
		
		cDB.add(new SetConstraint("(proper-subset S1 S2)"));
				
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("SetSolver");
				
		SetSolver sS = new SetSolver("SetSolver", cM);
		
		sS.run(c);
				
		assertFalse(c.getResultingState("SetSolver").equals(State.Consistent));
	}
	
	/**
	 * Test subsets
	 */
	public void test5() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new SetConstraint("(add S1 1)"));
		cDB.add(new SetConstraint("(add S1 2)"));
		cDB.add(new SetConstraint("(add S1 3)"));
		
		cDB.add(new SetConstraint("(is-domain S2 type)"));
		
		cDB.add(new SetConstraint("(equals S1 S2)"));

		Core c = new Core();
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("type", "1,2,3");
		c.setTypeManager(tM);
		
//		System.out.println(tM);
		
		c.setContext(cDB);
		
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.set("setSolver", "class", "SetSolver");
//		cM.set("setSolver", "verbose", "true");
//		cM.set("setSolver", "verbosity", "10");
		
//		Logger.addPrintStream("setSolver", System.out);	

		Module sS = ModuleFactory.initModule("setSolver", cM);

		sS.run(c);
				
		assertTrue(c.getResultingState("setSolver").equals(State.Consistent));
	}
}

