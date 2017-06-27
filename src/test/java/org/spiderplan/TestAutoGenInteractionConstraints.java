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

import java.util.ArrayList;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.parser.Compile;

// contents from imports.java will end up after this comment
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
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;


import junit.framework.TestCase;

/**
 * Automatically generated. Any changes will be overwritten the 
 * next time you run gradle.
 */
public class TestAutoGenInteractionConstraints extends TestCase {
	
	@Override
	public void setUp() throws Exception {
	}  

	@Override
	public void tearDown() throws Exception { 
	}
	
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
	/**
	 * Tests a simple interaction constraint with three resolvers. The third
	 * one works.
	 */ 	
	public void testSchedulingThirdResolverWorks() {
		String plannerFilename = "./domains/test-cases/InteractionConstraints/SchedulingThirdResolverWorks/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/InteractionConstraints/SchedulingThirdResolverWorks/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(resultCDB.contains(new AllenConstraint(Term.createConstant("I1"), Term.createConstant("I2"), TemporalRelation.Equals)));
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 30);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 50);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 80);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 30);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 40);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 50);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 80);
		
	}		
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
	/**
	 * Tests a simple interaction constraint with three resolvers. No resolver
	 * works.
	 */ 	
	public void testSchedulingNoResolverWorks() {
		String plannerFilename = "./domains/test-cases/InteractionConstraints/SchedulingNoResolverWorks/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/InteractionConstraints/SchedulingNoResolverWorks/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * IC that triggers itself. This fails only because of the 
	 * temporal constraint and the limited temporal horizon.
	 */ 	
	public void testSelfEnablingCascade() {
		String plannerFilename = "./domains/test-cases/InteractionConstraints/SelfEnablingCascade/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/InteractionConstraints/SelfEnablingCascade/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Tests a simple interaction constraint with three resolvers. The second
	 * one works.
	 */ 	
	public void testSchedulingSecondResolverWorks() {
		String plannerFilename = "./domains/test-cases/InteractionConstraints/SchedulingSecondResolverWorks/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/InteractionConstraints/SchedulingSecondResolverWorks/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(resultCDB.contains(new AllenConstraint(Term.createConstant("I1"), Term.createConstant("I2"), TemporalRelation.After, new Interval(Term.createInteger(1), Term.createConstant("inf")))));
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 21);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 41);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 80);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 0);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 20);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 39);
		
	}		
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
	/**
	 * Tests a simple interaction constraint with three resolvers. The first
	 * one works.
	 */ 	
	public void testSchedulingFirstResolverWorks() {
		String plannerFilename = "./domains/test-cases/InteractionConstraints/SchedulingFirstResolverWorks/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/InteractionConstraints/SchedulingFirstResolverWorks/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(resultCDB.contains(new AllenConstraint(Term.createConstant("I1"), Term.createConstant("I2"), TemporalRelation.Before, new Interval(Term.createInteger(1), Term.createConstant("inf")))));
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 59);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 20);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 79);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 21);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 80);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 41);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 100);
		
	}		

}

