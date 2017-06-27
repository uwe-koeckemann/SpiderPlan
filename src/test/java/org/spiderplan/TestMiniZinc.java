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

import java.util.Collection;

import org.spiderplan.minizinc.MiniZincAdapter;
import org.spiderplan.modules.GraphSolver;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.tools.ExecuteSystemCommand;

import junit.framework.TestCase;

/**
 * Test the {@link GraphSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestMiniZinc extends TestCase {
	
	boolean minizincExists = true;

	@Override
	public void setUp() throws Exception {
		minizincExists = (ExecuteSystemCommand.testIfCommandExists("minizinc"));
		if ( !minizincExists ) {  
			System.out.println("[Warning] Skipping this because minizinc binary not in $PATH. To run this test install minizinc (http://www.minizinc.org/) and make sure the binary is in $PATH. When using solvers that require minizinc it is also possible to set the path to the binary as part of the solver configuration.");
		}
	}  

	@Override
	public void tearDown() throws Exception { 
	}
	
	/**
	 * Test with simple satisfiable flow
	 */
	public void test() {
		if ( minizincExists ) {
			String program = "var 0..600: FuelNeeded_43;\n";
			program += "var 0..600: X_01;\n";
			program += "var 0..600: X_49;\n";
			program += "var 0..600: F1;\n";
			program += "var 0..600: X_34;\n";
			program += "var 0..600: X_35;\n";
			program += "var 0..600: FuelNeeded_35;\n";
			program += "var 0..600: X_39;\n";
			program += "var 0..600: X_53;\n";
			program += "var 0..600: X_42;\n";
			program += "var 0..600: X_56;\n";
			program += "var 0..600: X_60;\n";
			program += "var 0..600: X_63;\n";
			program += "var 0..600: X_66;\n";
			program += "constraint FuelNeeded_43 + X_01 ==   X_49 ;\n";
			program += "constraint F1 ==   X_34 ;\n";
			program += "constraint X_35 + FuelNeeded_35 ==  X_39 ;\n";
			program += "constraint X_34 ==   X_35 + 4 ;\n";
			program += "constraint X_49 ==   X_53 + 6 ;\n";
			program += "constraint X_39 ==   X_42 + 10 ;\n";
			program += "constraint X_53 ==   X_56 + 17 ;\n";
			program += "constraint X_42 ==   X_01 + 12 ;\n";
			program += "constraint X_56 ==   X_60 + 16 ;\n";
			program += "constraint X_60 ==   8 + X_63 ;\n";
			program += "constraint X_63 ==   X_66 + 19 ;\n";
			program += "constraint X_66 ==   2 ;\n\n";
		
			program += "solve satisfy;\n\n";
	
			program += "output [\"{FuelNeeded_43/\", show(FuelNeeded_43),\n";
			program += "\",X_01/\", show(X_01),\n";
			program += "\",X_49/\", show(X_49),\n";
			program += "\",F1/\", show(F1),\n";
			program += "\",X_34/\", show(X_34),\n";
			program += "\",FuelNeeded_35/\", show(FuelNeeded_35),\n";
			program += "\",X_39/\", show(X_39),\n";
			program += "\",X_53/\", show(X_53),\n";
			program += "\",X_42/\", show(X_42),\n";
			program += "\",X_56/\", show(X_56),\n";
			program += "\",X_60/\", show(X_60),\n";
			program += "\",X_63/\", show(X_63),\n";
			program += "\",X_66/\", show(X_66),\"}\"];\n";
			
	//		System.out.println(program);
			
			Collection<Substitution> r = MiniZincAdapter.runMiniZinc("minizinc", program,true);
			
	//		for ( Substitution theta : r ) {
	//			System.out.println(theta);
	//		}
			
			assertTrue( r.size() > 0 );
			
	//		System.out.println(mza.stopwatch.allSums2Str());
	//		System.out.println(r.size());
		}
	}		
}

