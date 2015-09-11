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
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.GraphConstraint;
import org.spiderplan.tools.ExecuteSystemCommand;

import junit.framework.TestCase;

/**
 * Test the {@link GraphSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestGraphSolver extends TestCase {
	
	boolean minizincExists = true;

	@Override
	public void setUp() throws Exception {
		minizincExists = (ExecuteSystemCommand.testIfCommandExists("/minizinc"));
		if ( !minizincExists ) {  
			System.out.println("[Warning] Could not find minizinc binary in $PATH. Skipping some tests accordingly. To run these tests download minizinc from http://www.minizinc.org/ and make sure the binary is in $PATH. When using solvers that require minizinc it is also possible to set the path to the binary as part of the solver configuration.");
		}
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test with simple satisfiable flow
	 */
	public void testFlow1() {
		if ( minizincExists ) {
			ConstraintDatabase cDB = new ConstraintDatabase();
			cDB.add(new GraphConstraint("(directed g)"));
			cDB.add(new GraphConstraint("(flow g)"));
			cDB.add(new GraphConstraint("(edge g a l1 1)"));
			cDB.add(new GraphConstraint("(edge g b l1 1)"));
			cDB.add(new GraphConstraint("(edge g l1 l2 ?X)"));
			cDB.add(new GraphConstraint("(edge g l2 c 2)"));
			cDB.add(new GraphConstraint("(cap g ?X 4)"));
			
			Core c = new Core();
			c.setContext(cDB);
			
			ConfigurationManager cM = new ConfigurationManager();
			cM.add("GraphAnalyzer");
							
			GraphSolver gA = new GraphSolver("GraphAnalyzer", cM);
			
			gA.run(c);
					
			assertTrue(c.getResultingState("GraphAnalyzer").equals(State.Consistent));
		}
	}		
	
	/**
	 * Simple unsatisfiable flow.
	 */
	public void testFlow2() {
		if ( minizincExists ) {
			ConstraintDatabase cDB = new ConstraintDatabase();
			cDB.add(new GraphConstraint("(directed g)"));
			cDB.add(new GraphConstraint("(flow g)"));
			cDB.add(new GraphConstraint("(edge g a l1 1)"));
			cDB.add(new GraphConstraint("(edge g l1 l2 ?X)"));
			cDB.add(new GraphConstraint("(edge g l2 c 2)"));
			cDB.add(new GraphConstraint("(cap g ?X 5)"));		
			
			Core c = new Core();
			c.setContext(cDB);
			
			ConfigurationManager cM = new ConfigurationManager();
			cM.add("GraphAnalyzer");
			GraphSolver gA = new GraphSolver("GraphAnalyzer", cM);
			
			gA.run(c);
			
			assertTrue(c.getResultingState("GraphAnalyzer").equals(State.Inconsistent));
		}
	}	
	
	/**
	 * Test-case from Petrobras domain
	 */
	public void testFlow3() {
		if ( minizincExists ) {
			ConstraintDatabase cDB = new ConstraintDatabase();
			cDB.add(new GraphConstraint("(directed s1)"));
			cDB.add(new GraphConstraint("(flow s1)"));
			cDB.add(new GraphConstraint("(edge s1 fuelSource (s s1 0) 100)"));
			cDB.add(new GraphConstraint("(edge s1 (s s1 0) (s s1 1) ?X1)"));
			cDB.add(new GraphConstraint("(edge s1 (s s1 1) (sink s1 1) 40)"));
			cDB.add(new GraphConstraint("(edge s1 (s s1 1) (s s1 2) ?X2)"));
			cDB.add(new GraphConstraint("(edge s1 (source s1 2) (s s1 2) ?X3)"));
			cDB.add(new GraphConstraint("(edge s1 (s s1 2) (s s1 3) ?X4)"));
			cDB.add(new GraphConstraint("(edge s1 (s s1 3) (sink s1 3) 100)"));
			
			cDB.add(new GraphConstraint("(cap s1 ?X1 600)"));
			cDB.add(new GraphConstraint("(cap s1 ?X2 600)"));
			cDB.add(new GraphConstraint("(cap s1 ?X3 600)"));
			cDB.add(new GraphConstraint("(cap s1 ?X4 600)"));
	
			Core c = new Core();
			c.setContext(cDB);
			
			ConfigurationManager cM = new ConfigurationManager();
			cM.add("GraphAnalyzer");
							
			GraphSolver gA = new GraphSolver("GraphAnalyzer", cM);
			
			gA.run(c);
		
			assertTrue(c.getResultingState("GraphAnalyzer").equals(State.Consistent));
		}
	}			
}

