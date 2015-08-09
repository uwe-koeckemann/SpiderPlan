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
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Interval;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import junit.framework.TestCase;

/**
 * Test the {@link MathSolver} module that test all types of graph related constraints.
 * 
 * @author Uwe Köckemann
 */
public class TestIncrementalSTPSolver extends TestCase {
	
	Term INF = Term.createConstant("inf");
	Term L0 = Term.createInteger(0);
	Term L1 = Term.createInteger(1);
			
	
	Term I1 = Term.createConstant("I1");
	Term I2 = Term.createConstant("I2");
	Term I3 = Term.createConstant("I3");
	
	Statement s1 = new Statement(I1, new Atomic("x"), Term.createConstant("v"));
	Statement s2 = new Statement(I2, new Atomic("x"), Term.createConstant("v"));
	Statement s3 = new Statement(I3, new Atomic("x"), Term.createConstant("v"));
	

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void test001_NoConstraints() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 10);
	}
	
	public void test002_Duration() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(20), Term.createInteger(20)) ));
			
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertFalse(test);
		
		cDB = new ConstraintDatabase();
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(5), Term.createInteger(5)) ));
		cDB.add(s1);
		
		test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 5);
		assertTrue(incSTP.getEET(I1) == 5);
		assertTrue(incSTP.getLET(I1) == 10);
	}
	
	public void test003_Before() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.Before, 
				new Interval(L1, INF) )
		);
		
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.Before, 
				new Interval(L1, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
	
		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 5);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 6);
		
		assertTrue(incSTP.getEST(I2) == 2);
		assertTrue(incSTP.getLST(I2) == 7);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 8);
		
		assertTrue(incSTP.getEST(I3) == 4);
		assertTrue(incSTP.getLST(I3) == 9);
		assertTrue(incSTP.getEET(I3) == 5);
		assertTrue(incSTP.getLET(I3) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.Before,
				new Interval(L1, INF) )
		);
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.Before,
				new Interval(L1, INF) )
		);
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test004_After() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.After, 
				new Interval(L1, INF) )
		);
		
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.After, 
				new Interval(L1, INF) )
		);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I3) == 0);
		assertTrue(incSTP.getLST(I3) == 5);
		assertTrue(incSTP.getEET(I3) == 1);
		assertTrue(incSTP.getLET(I3) == 6);
		
		assertTrue(incSTP.getEST(I2) == 2);
		assertTrue(incSTP.getLST(I2) == 7);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 8);
		
		assertTrue(incSTP.getEST(I1) == 4);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 5);
		assertTrue(incSTP.getLET(I1) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.After,
				new Interval(L1, INF) )
		);
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.After,
				new Interval(L1, INF) )
		);
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test005_Equals_Release_Duration() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add( new AllenConstraint(I1, I2,TemporalRelation.Equals) );
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.Duration, new Interval(Term.createInteger(10),Term.createInteger(10))));
		cDB.add( new AllenConstraint(I1, TemporalRelation.Release, new Interval(Term.createInteger(5),Term.createInteger(5))));

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,20L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 5);
		assertTrue(incSTP.getEET(I1) == 15);
		assertTrue(incSTP.getLET(I1) == 15);
		
		assertTrue(incSTP.getEST(I2) == 5);
		assertTrue(incSTP.getLST(I2) == 5);
		assertTrue(incSTP.getEET(I2) == 15);
		assertTrue(incSTP.getLET(I2) == 15);
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.Release, new Interval(Term.createInteger(2),Term.createInteger(2))));
	
		assertFalse(incSTP.isConsistent(cDB,null));
	}
	
	
	public void test006_Meets() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.Meets));
		
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.Meets));
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 7);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 8);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 9);
		
		assertTrue(incSTP.getEST(I3) == 2);
		assertTrue(incSTP.getLST(I3) == 9);
		assertTrue(incSTP.getEET(I3) == 3);
		assertTrue(incSTP.getLET(I3) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.Meets,
				new Interval(L0, INF) )
		);
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.Meets,
				new Interval(L0, INF) )
		);
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test007_MetBy() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.MetBy));
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.MetBy));
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I3) == 0);
		assertTrue(incSTP.getLST(I3) == 7);
		assertTrue(incSTP.getEET(I3) == 1);
		assertTrue(incSTP.getLET(I3) == 8);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 9);
		
		assertTrue(incSTP.getEST(I1) == 2);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 3);
		assertTrue(incSTP.getLET(I1) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.MetBy));
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.MetBy));
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test008_BeforeOrMeets() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.BeforeOrMeets, 
				new Interval(L0, INF) )
		);
		
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.BeforeOrMeets, 
				new Interval(L0, INF) )
		);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
	
		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 7);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 8);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 9);
		
		assertTrue(incSTP.getEST(I3) == 2);
		assertTrue(incSTP.getLST(I3) == 9);
		assertTrue(incSTP.getEET(I3) == 3);
		assertTrue(incSTP.getLET(I3) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.BeforeOrMeets,
				new Interval(L0, INF) )
		);
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.BeforeOrMeets,
				new Interval(L0, INF) )
		);
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test009_MetByAfter() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		cDB.add(s3);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.MetByOrAfter, 
				new Interval(L0, INF) )
		);
		
		cDB.add(new AllenConstraint(I2, I3,TemporalRelation.MetByOrAfter, 
				new Interval(L0, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		cDB.add(new AllenConstraint(I3, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));

		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);
		boolean test = incSTP.isConsistent(cDB,null);
		
		assertTrue(test);
		
		assertTrue(incSTP.getEST(I3) == 0);
		assertTrue(incSTP.getLST(I3) == 7);
		assertTrue(incSTP.getEET(I3) == 1);
		assertTrue(incSTP.getLET(I3) == 8);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 9);
		
		assertTrue(incSTP.getEST(I1) == 2);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 3);
		assertTrue(incSTP.getLET(I1) == 10);
		
		ConstraintDatabase cDB2 = cDB.copy();
		
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.MetByOrAfter,
				new Interval(L0, INF) )
		);
		
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB, null);
		assertFalse(test);
		test = incSTP.isConsistent(cDB2, null);
		assertTrue(test);
		
		// Different CDB should lead to from scratch propagation
		cDB = new ConstraintDatabase();
		cDB.add(s1);
		cDB.add(s3);
		cDB.add(new AllenConstraint(I3, I1,TemporalRelation.MetByOrAfter,
				new Interval(L0, INF) )
		);
		test = incSTP.isConsistent(cDB, null);
		assertTrue(test);
	}
	
	public void test010_Starts() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.Starts, 
				new Interval(L1, INF) )
		);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 8);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 10);

	}
	
	public void test011_StartedBy() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);

		
		cDB.add(new AllenConstraint(I2, I1,TemporalRelation.StartedBy, 
				new Interval(L1, INF) )
		);
		
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 8);
		assertTrue(incSTP.getEET(I1) == 1);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test012_During() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.During, 
				new Interval(L1, INF), new Interval(L1, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));
		


		assertTrue(incSTP.getEST(I1) == 1);
		assertTrue(incSTP.getLST(I1) == 8);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 7);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test013_Contains() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I2, I1,TemporalRelation.Contains, 
				new Interval(L1, INF), new Interval(L1, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));
		


		assertTrue(incSTP.getEST(I1) == 1);
		assertTrue(incSTP.getLST(I1) == 8);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 7);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test014_Finishes() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.Finishes, 
				new Interval(L1, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 1);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 10);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test015_Finishes() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I2, I1,TemporalRelation.FinishedBy, 
				new Interval(L1, INF) )
		);
		cDB.add(new AllenConstraint(I1, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));		
		cDB.add(new AllenConstraint(I2, TemporalRelation.Duration, 
				new Interval(Term.createInteger(1), Term.createConstant("inf"))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 1);
		assertTrue(incSTP.getLST(I1) == 9);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 10);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 2);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test016_Overlaps() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I1, I2,TemporalRelation.Overlaps, 
				new Interval(L1, INF) )
		);
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 7);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test017_OverlappedBy() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add(new AllenConstraint(I2, I1,TemporalRelation.OverlappedBy, 
				new Interval(L1, INF) )
		);
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,10L);

		assertTrue(incSTP.isConsistent(cDB,null));

		assertTrue(incSTP.getEST(I1) == 0);
		assertTrue(incSTP.getLST(I1) == 7);
		assertTrue(incSTP.getEET(I1) == 2);
		assertTrue(incSTP.getLET(I1) == 9);
		
		assertTrue(incSTP.getEST(I2) == 1);
		assertTrue(incSTP.getLST(I2) == 8);
		assertTrue(incSTP.getEET(I2) == 3);
		assertTrue(incSTP.getLET(I2) == 10);
	}
	
	public void test018_At_Deadline() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.At, 
					new Interval(Term.createInteger(5),Term.createInteger(10)),
					new Interval(Term.createInteger(20),Term.createInteger(30))));
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0),Term.createInteger(20)),
				new Interval(Term.createInteger(10),Term.createInteger(30))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,40L);
		
		assertTrue(incSTP.isConsistent(cDB,null));
		
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 10);
		assertTrue(incSTP.getEET(I1) == 20);
		assertTrue(incSTP.getLET(I1) == 30);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 10);
		assertTrue(incSTP.getLET(I2) == 30);
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.Deadline, 
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.Deadline, 
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		assertTrue(incSTP.isConsistent(cDB,null));
		
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 10);
		assertTrue(incSTP.getEET(I1) == 25);
		assertTrue(incSTP.getLET(I1) == 25);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 25);
		assertTrue(incSTP.getLET(I2) == 25);
				
		cDB.add( new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(5),Term.createInteger(5)),
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		assertTrue(incSTP.isConsistent(cDB,null));
		
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 5);
		assertTrue(incSTP.getEET(I1) == 25);
		assertTrue(incSTP.getLET(I1) == 25);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 25);
		assertTrue(incSTP.getLET(I2) == 25);
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(25),Term.createInteger(25)),
				new Interval(Term.createInteger(30),Term.createInteger(30))));
		
		assertFalse(incSTP.isConsistent(cDB,null));
	}
	
	public void test018_Rigidity() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(s1);
		cDB.add(s2);
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.At, 
					new Interval(Term.createInteger(5),Term.createInteger(10)),
					new Interval(Term.createInteger(20),Term.createInteger(30))));
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(0),Term.createInteger(20)),
				new Interval(Term.createInteger(10),Term.createInteger(30))));
		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,40L);
		
		assertTrue(incSTP.isConsistent(cDB,null));
		
		IncrementalSTPSolver csp = new IncrementalSTPSolver(0, 40);
		csp.isConsistent(cDB, new TypeManager());
		
		assertTrue(csp.getRigidity() == incSTP.getRigidity());
		
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 10);
		assertTrue(incSTP.getEET(I1) == 20);
		assertTrue(incSTP.getLET(I1) == 30);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 10);
		assertTrue(incSTP.getLET(I2) == 30);
		
		assertTrue(csp.getEST(I1) == 5);
		assertTrue(csp.getLST(I1) == 10);
		assertTrue(csp.getEET(I1) == 20);
		assertTrue(csp.getLET(I1) == 30);
		
		assertTrue(csp.getEST(I2) == 0);
		assertTrue(csp.getLST(I2) == 20);
		assertTrue(csp.getEET(I2) == 10);
		assertTrue(csp.getLET(I2) == 30);
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.Deadline, 
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.Deadline, 
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		assertTrue(incSTP.isConsistent(cDB,null));
		
		csp.isConsistent(cDB, new TypeManager());

			
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 10);
		assertTrue(incSTP.getEET(I1) == 25);
		assertTrue(incSTP.getLET(I1) == 25);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 25);
		assertTrue(incSTP.getLET(I2) == 25);
				
		assertTrue(csp.getEST(I1) == 5);
		assertTrue(csp.getLST(I1) == 10);
		assertTrue(csp.getEET(I1) == 25);
		assertTrue(csp.getLET(I1) == 25);
		
		assertTrue(csp.getEST(I2) == 0);
		assertTrue(csp.getLST(I2) == 20);
		assertTrue(csp.getEET(I2) == 25);
		assertTrue(csp.getLET(I2) == 25);
		
//		System.out.println(csp.getRigidity());
//		System.out.println(incSTP.getRigidity());
//		
//		System.out.println(incSTP.getDistanceMatrixString());
			
		assertTrue(Math.abs(csp.getRigidity() - incSTP.getRigidity()) < 0.000001);
		
		cDB.add( new AllenConstraint(I1, TemporalRelation.At, 
				new Interval(Term.createInteger(5),Term.createInteger(5)),
				new Interval(Term.createInteger(25),Term.createInteger(25))));
		
		assertTrue(incSTP.isConsistent(cDB,null));
		csp.isConsistent(cDB, new TypeManager());
			
		assertTrue(Math.abs(csp.getRigidity() - incSTP.getRigidity()) < 0.00000001);
			
		assertTrue(incSTP.getEST(I1) == 5);
		assertTrue(incSTP.getLST(I1) == 5);
		assertTrue(incSTP.getEET(I1) == 25);
		assertTrue(incSTP.getLET(I1) == 25);
		
		assertTrue(incSTP.getEST(I2) == 0);
		assertTrue(incSTP.getLST(I2) == 20);
		assertTrue(incSTP.getEET(I2) == 25);
		assertTrue(incSTP.getLET(I2) == 25);
		
		assertTrue(csp.getEST(I1) == 5);
		assertTrue(csp.getLST(I1) == 5);
		assertTrue(csp.getEET(I1) == 25);
		assertTrue(csp.getLET(I1) == 25);
		
		assertTrue(csp.getEST(I2) == 0);
		assertTrue(csp.getLST(I2) == 20);
		assertTrue(csp.getEET(I2) == 25);
		assertTrue(csp.getLET(I2) == 25);
		
		cDB.add( new AllenConstraint(I2, TemporalRelation.At, 
				new Interval(Term.createInteger(25),Term.createInteger(25)),
				new Interval(Term.createInteger(30),Term.createInteger(30))));
		
		assertFalse(incSTP.isConsistent(cDB,null));
	}
//	System.out.println("EST(I1) " + incSTP.getEST(I1));
//	System.out.println("LST(I1) " + incSTP.getLST(I1));
//	System.out.println("EET(I1) " + incSTP.getEET(I1));
//	System.out.println("LET(I1) " + incSTP.getLET(I1));
//	System.out.println("EST(I2) " + incSTP.getEST(I2));
//	System.out.println("LST(I2) " + incSTP.getLST(I2));
//	System.out.println("EET(I2) " + incSTP.getEET(I2));
//	System.out.println("LET(I2) " + incSTP.getLET(I2));

	
	public void test019_HistorySize() {
		ConstraintDatabase cDB1 = new ConstraintDatabase();
		cDB1.add(s1);
		cDB1.add(s2);
		cDB1.add(s3);
		cDB1.add(new AllenConstraint(I1, I2,TemporalRelation.Before, new Interval(L1, INF) ));
		
		ConstraintDatabase cDB2 = cDB1.copy();		
		cDB2.add(new AllenConstraint(I2, I3,TemporalRelation.Before, new Interval(L1, INF) ));
		
		ConstraintDatabase cDB3 = cDB2.copy();
		cDB3.add( new AllenConstraint(I1, TemporalRelation.Release, new Interval(Term.createInteger(0),Term.createInteger(5))));

		ConstraintDatabase cDB3alt = cDB2.copy();
		cDB3alt.add( new AllenConstraint(I1, TemporalRelation.Release, new Interval(Term.createInteger(0),Term.createInteger(0))));

		
		ConstraintDatabase cDB4 = cDB3.copy();
		cDB4.add( new AllenConstraint(I2, TemporalRelation.Release, new Interval(Term.createInteger(10),Term.createInteger(15))));

		ConstraintDatabase cDB5 = cDB4.copy();
		cDB5.add( new AllenConstraint(I3, TemporalRelation.Release, new Interval(Term.createInteger(20),Term.createInteger(25))));

		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,50L);
		
		assertTrue(incSTP.getHistorySize() == 0);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB2,null));
		assertTrue(incSTP.getHistorySize() == 2);
		assertTrue(incSTP.isConsistent(cDB3,null));
		assertTrue(incSTP.getHistorySize() == 3);
		assertTrue(incSTP.isConsistent(cDB4,null));
		assertTrue(incSTP.getHistorySize() == 4);
		assertTrue(incSTP.isConsistent(cDB4,null));
		assertTrue(incSTP.getHistorySize() == 4);
		assertTrue(incSTP.isConsistent(cDB5,null));
		assertTrue(incSTP.getHistorySize() == 5);
		assertTrue(incSTP.isConsistent(cDB2,null));
		assertTrue(incSTP.getHistorySize() == 2);
		
		assertTrue(incSTP.isConsistent(cDB5,null));
		assertTrue(incSTP.getHistorySize() == 3);
		
		assertTrue(incSTP.isConsistent(cDB3alt,null));
		assertTrue(incSTP.getHistorySize() == 3);
	}
	
	public void test020_HistorySize() {
		ConstraintDatabase cDB1 = new ConstraintDatabase();
		cDB1.add(s1);
		cDB1.add(s2);
		cDB1.add(s3);
		cDB1.add(new AllenConstraint(I1, I2,TemporalRelation.Before, new Interval(L1, INF) ));
		
		ConstraintDatabase cDB2 = cDB1.copy();		
		cDB2.add(new AllenConstraint(I2, I3,TemporalRelation.Before, new Interval(L1, INF) ));
		
		ConstraintDatabase cDB3 = cDB2.copy();
		cDB3.add( new AllenConstraint(I1, TemporalRelation.Release, new Interval(Term.createInteger(0),Term.createInteger(5))));

		ConstraintDatabase cDB3alt = cDB2.copy();
		cDB3alt.add( new AllenConstraint(I1, TemporalRelation.Release, new Interval(Term.createInteger(0),Term.createInteger(0))));

		
		ConstraintDatabase cDB4 = cDB3.copy();
		cDB4.add( new AllenConstraint(I2, TemporalRelation.Release, new Interval(Term.createInteger(10),Term.createInteger(15))));

		ConstraintDatabase cDB5 = cDB4.copy();
		cDB5.add( new AllenConstraint(I3, TemporalRelation.Release, new Interval(Term.createInteger(20),Term.createInteger(25))));

		
		IncrementalSTPSolver incSTP = new IncrementalSTPSolver(0L,50L);
		incSTP.setMaxHistorySize(1);
		
		assertTrue(incSTP.getHistorySize() == 0);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB1,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB2,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB3,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB4,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB4,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB5,null));
		assertTrue(incSTP.getHistorySize() == 1);
		assertTrue(incSTP.isConsistent(cDB2,null));
		assertTrue(incSTP.getHistorySize() == 1);
		
		assertTrue(incSTP.isConsistent(cDB5,null));
		assertTrue(incSTP.getHistorySize() == 1);
		
		assertTrue(incSTP.isConsistent(cDB3alt,null));
		assertTrue(incSTP.getHistorySize() == 1);
	}
	
}

