/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan;

import org.spiderplan.modules.MathSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.math.MathConstraint;
import org.spiderplan.representation.logic.Term;
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
	 * Test evaluating math expressions
	 */
	public void test1() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new MathConstraint(Term.parse("(eval-int x (* 100 (+ (* (/ 100 10) (- 7 3)) (% 5 3))))")));
		cDB.add(new MathConstraint(Term.parse("(eval-int (a q) (/ x 100))")));
		cDB.add(new MathConstraint(Term.parse("(eval-int (q a) (% (a q) 7))")));
		cDB.add(new MathConstraint(Term.parse("(eval-float y (* 0.5 3.0))")));

		cDB.add(new MathConstraint(Term.parse("(greater-than y 1)")));
		cDB.add(new MathConstraint(Term.parse("(greater-than (a q) 41)	")));
		cDB.add(new MathConstraint(Term.parse("(less-than (a q) 42.5)")));
				
		Core c = new Core();
		c.setContext(cDB);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("MathSolver");
				
		MathSolver mS = new MathSolver("MathSolver", cM);
		
		SolverResult r = mS.testAndResolve(c);
		
		ValueLookup vl = r.getResolverIterator().next().getConstraintDatabase().get(ValueLookup.class).get(0);
			
		assertTrue(vl.getInt(Term.createConstant("x")) == 4200);
		assertTrue(vl.getInt(Term.parse("(a q)")) == 42);
		assertTrue(vl.getFloat(Term.parse("y")) == 1.5);
	}
}

