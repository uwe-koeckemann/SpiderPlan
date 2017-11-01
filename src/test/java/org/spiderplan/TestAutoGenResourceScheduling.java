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
public class TestAutoGenResourceScheduling extends TestCase {
	
	@Override
	public void setUp() throws Exception {
	}  

	@Override
	public void tearDown() throws Exception { 
	}
	
	/**
	 * Test a single non-binary resource with three overlapping usages.
	 */ 	
	public void testNonBinarySatisfiable() {
		String plannerFilename = "./domains/test-cases/ResourceScheduling/NonBinarySatisfiable/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/ResourceScheduling/NonBinarySatisfiable/problem.uddl");

	
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
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 20);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 60);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 40);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 60);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 80);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 100);
		assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 20);
		assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 40);
		assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 60);
		
	}		
	/**
	 * Test a single binary resource with two overlapping usages that cannot be separated.
	 */ 	
	public void testBinaryUnsatisfiable() {
		String plannerFilename = "./domains/test-cases/ResourceScheduling/BinaryUnsatisfiable/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/ResourceScheduling/BinaryUnsatisfiable/problem.uddl");

	
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
	 * Test a single binary resource with two overlapping usages.
	 */ 	
	public void testBinarySatisfiable() {
		String plannerFilename = "./domains/test-cases/ResourceScheduling/BinarySatisfiable/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/ResourceScheduling/BinarySatisfiable/problem.uddl");

	
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
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 20);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 60);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 40);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 60);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 80);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 100);
		
	}		
	/**
	 * Test a single non-binary resource with three overlapping usages that cannot be separated.
	 */ 	
	public void testNonBinaryUnsatisfiable() {
		String plannerFilename = "./domains/test-cases/ResourceScheduling/NonBinaryUnsatisfiable/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/ResourceScheduling/NonBinaryUnsatisfiable/problem.uddl");

	
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
	 * Test a single binary resource with two overlapping usages.
	 */ 	
	public void testBinaryMultipleConflicts() {
		String plannerFilename = "./domains/test-cases/ResourceScheduling/BinaryMultipleConflicts/planner.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/ResourceScheduling/BinaryMultipleConflicts/problem.uddl");

	
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
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 1);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 10);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 11);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 15);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 11);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 15);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 16);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 20);
		assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 16);
		assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 20);
		assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 21);
		assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 25);
		assertTrue(valueLookup.getEST(Term.createConstant("I4")) == 21);
		assertTrue(valueLookup.getLST(Term.createConstant("I4")) == 25);
		assertTrue(valueLookup.getEET(Term.createConstant("I4")) == 26);
		assertTrue(valueLookup.getLET(Term.createConstant("I4")) == 30);
		assertTrue(valueLookup.getEST(Term.createConstant("I5")) == 26);
		assertTrue(valueLookup.getLST(Term.createConstant("I5")) == 30);
		assertTrue(valueLookup.getEET(Term.createConstant("I5")) == 31);
		assertTrue(valueLookup.getLET(Term.createConstant("I5")) == 40);
		assertTrue(valueLookup.getEST(Term.createConstant("I6")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I6")) == 10);
		assertTrue(valueLookup.getEET(Term.createConstant("I6")) == 1);
		assertTrue(valueLookup.getLET(Term.createConstant("I6")) == 10);
		
	}		

}

