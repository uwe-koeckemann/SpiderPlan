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

import org.spiderplan.modules.CostSolver;
import org.spiderplan.modules.DomainSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Cost;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

/**
 * Testing evaluation of domain constraints using the {@link DomainSolver}.
 *  
 * @author Uwe Köckemann
 */
public class TestCostSolver extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Basic test with a single constraint that becomes 
	 * inconsistent after adding to the cost.
	 */
	public void testCosts() {
		ConstraintDatabase context = new ConstraintDatabase();
		context.add(new Cost(new Atomic("less-than", Term.createConstant("c"), Term.createInteger(15))));
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.add("CostSolver");
		CostSolver cSolver = new CostSolver("CostSolver", cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( context );
		testCore.setTypeManager(new TypeManager());
		
		testCore = cSolver.run(testCore);
		assertTrue(testCore.getResultingState("CostSolver").equals(State.Consistent));
		
		context.add(new Cost(new Atomic("add", Term.createConstant("c"), Term.createInteger(10))));
		
		testCore = cSolver.run(testCore);
		assertTrue(testCore.getResultingState("CostSolver").equals(State.Consistent));
		
		context.add(new Cost(new Atomic("add", Term.createConstant("c"), Term.createInteger(10))));
		
		testCore = cSolver.run(testCore);
		assertTrue(testCore.getResultingState("CostSolver").equals(State.Inconsistent));
	}
	
	
}


