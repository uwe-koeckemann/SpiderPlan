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

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.modules.DomainSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.NewObject;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.VariableDomainRestriction.Relation;
import org.spiderplan.representation.constraints.VariableDomainRestriction;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

/**
 * Testing evaluation of domain constraints using the {@link DomainSolver}.
 *  
 * @author Uwe Köckemann
 */
public class TestDomainSolver extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Some simple cases of {@link VariableDomainRestriction} before
	 * and after applying a {@link Substitution}. Contains both consistent
	 * and inconsistent cases.
	 */
	public void testVariableDomainRestriction() {
		ConstraintDatabase context = new ConstraintDatabase();
		Collection<Term> D = new ArrayList<Term>();
		D.add(Term.createConstant("a"));
		D.add(Term.createConstant("b"));
		D.add(Term.createConstant("c"));
		VariableDomainRestriction dr = new VariableDomainRestriction(Relation.In, Term.createVariable("X"), D);
		context.add(dr);
	
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.add("DomainSolver");
		DomainSolver dSolver = new DomainSolver("DomainSolver", cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( context );
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		testCore.setTypeManager(tM);
		
		testCore = dSolver.run(testCore);
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Consistent));
		
		testCore.getContext().substitute(new Substitution("{?X/a}"));
		
		testCore = dSolver.run(testCore);
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Consistent));
		
		VariableDomainRestriction drNew = new VariableDomainRestriction(Relation.In, Term.createVariable("Y"), D);
		context.add(drNew);
		
		testCore = dSolver.run(testCore);	
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Consistent));
		
		testCore.getContext().substitute(new Substitution("{?Y/d}"));
				
		testCore = dSolver.run(testCore);	
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Inconsistent));	
	}
	
	/**
	 * Some simple cases of {@link VariableDomainRestriction} before
	 * and after applying a {@link Substitution}. Contains both consistent
	 * and inconsistent cases.
	 */
	public void testNewObject() {
		ConstraintDatabase context = new ConstraintDatabase();

		NewObject no = new NewObject(Term.createVariable("?X"), Term.createConstant("t")); // X is an unused object of type t
		context.add(no);
	
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.addSimpleEnumType("boolean","true,false");
		tM.attachTypes("(q t)");								// q has type t
		
		context.add(new Statement(Term.createConstant("k1"), new Atomic("(q ?X)"), Term.createConstant("true")));		// should become b beause:	
		context.add(new Statement(Term.createConstant("k2"), new Atomic("(q a)"), Term.createConstant("true")));		// a is used already
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.add("DomainSolver");
		DomainSolver dSolver = new DomainSolver("DomainSolver", cM);

		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( context );
		
		testCore.setTypeManager(tM);
		
		testCore = dSolver.run(testCore);
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Searching));
		assertTrue(context.get(Statement.class).contains(new Statement("(k1 (q b) true)")));   // X was substituted by new object b
		
		testCore = dSolver.run(testCore);	// running test again should produce same result
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Consistent)); 
		assertTrue(context.get(Statement.class).contains(new Statement("(k1 (q b) true)")));   
		
		
		NewObject no2 = new NewObject(Term.createVariable("?Y"), Term.createConstant("t")); // Y is an unused object of type t (but no such object exists)
		context.add(no2);
		
		ConstraintDatabase cdbBefore = context.copy();
				
		testCore = dSolver.run(testCore);
		assertTrue(testCore.getResultingState("DomainSolver").equals(State.Inconsistent));
		
		assertTrue(context.equals(cdbBefore)); // context should be unaltered after inconsistency		
	}
}


