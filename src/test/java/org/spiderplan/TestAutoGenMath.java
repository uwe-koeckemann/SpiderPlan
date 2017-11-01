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


import junit.framework.TestCase;

/**
 * Automatically generated. Any changes will be overwritten the 
 * next time you run gradle.
 */
public class TestAutoGenMath extends TestCase {
	
	@Override
	public void setUp() throws Exception {
	}  

	@Override
	public void tearDown() throws Exception { 
	}
	
	/**
	 * Tests a simple duration constraint.
	 */ 	
	public void testSimpleIntegerOperations() {
		String plannerFilename = "./domains/test-cases/Math/SimpleIntegerOperations/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/SimpleIntegerOperations/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);
		assertTrue(values.getInt(Term.createConstant("d")) == 21);
		assertTrue(values.getInt(Term.createConstant("x")) == 42);
		assertTrue(values.getInt(Term.createConstant("y")) == 420);
		assertTrue(values.getInt(Term.createConstant("z")) == 42);
		assertTrue(values.getInt(Term.createConstant("m")) == 2);
		
		
	}		
	/**
	 * Tests a simple floating point operations.
	 */ 	
	public void testSimpleFloatOperations() {
		String plannerFilename = "./domains/test-cases/Math/SimpleFloatOperations/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/SimpleFloatOperations/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);
		assertTrue(values.getFloat(Term.createConstant("d")) == 2.5);
		assertTrue(values.getFloat(Term.createConstant("x")) == 2.0);
		assertTrue(values.getFloat(Term.createConstant("y")) == 0.25);
		assertTrue(values.getFloat(Term.createConstant("z")) == 0.75);
		
	}		
	/**
	 * Tests a simple duration constraint.
	 */ 	
	public void testNestedIntegerOperations() {
		String plannerFilename = "./domains/test-cases/Math/NestedIntegerOperations/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/NestedIntegerOperations/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);;
		assertTrue(values.getInt(Term.createConstant("x")) == 42);	
		
	}		
	/**
	 * Simple inequality that cannot be satisfied.
	 */ 	
	public void testTestInequalityFailure() {
		String plannerFilename = "./domains/test-cases/Math/TestInequalityFailure/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/TestInequalityFailure/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
	/**
	 * Simple inequality between float and int values that cannot be satisfied.
	 */ 	
	public void testTestInequalityInconsistentFloatInt() {
		String plannerFilename = "./domains/test-cases/Math/TestInequalityInconsistentFloatInt/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/TestInequalityInconsistentFloatInt/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
	/**
	 * Simple inequality between float and int values that can be satisfied.
	 */ 	
	public void testTestInequalityConsistentFloatInt() {
		String plannerFilename = "./domains/test-cases/Math/TestInequalityConsistentFloatInt/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/TestInequalityConsistentFloatInt/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
	/**
	 * Simple inequality that can be satisfied.
	 */ 	
	public void testTestInequalityConsistent() {
		String plannerFilename = "./domains/test-cases/Math/TestInequalityConsistent/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Math/TestInequalityConsistent/problem.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		

}

