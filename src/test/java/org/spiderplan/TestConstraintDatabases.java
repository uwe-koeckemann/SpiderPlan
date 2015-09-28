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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.Cost;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.temporal.TemporalNetworkTools;
import junit.framework.TestCase;


/**
 * Test-cases for {@link ConstraintDatabase} instances.
 * 
 * @author Uwe Köckemann
 *
 */
public class TestConstraintDatabases extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testConstraintDatabaseSupport1() {
		ConstraintDatabase a = new ConstraintDatabase();
		ConstraintDatabase b = new ConstraintDatabase();
		
		a.add(new Statement("(s1 (p a b) c)"));
		b.add(new Statement("(?K (p ?X ?Y) ?Z )"));
		
		Collection<Substitution> s = a.getSubstitutions(b);
				
		assertTrue( s.size() == 1 );
	}
	
	public void testConstraintDatabaseSupport2() {
		ConstraintDatabase a = new ConstraintDatabase();
		ConstraintDatabase b = new ConstraintDatabase();
		
		a.add(new Statement("(s1 (p a b) c)"));
		a.add(new Statement("(s2 (p a b) c)"));
		b.add(new Statement("(?K (p ?X ?Y) ?Z )"));
		
		Collection<Substitution> s = a.getSubstitutions(b);
		
		assertTrue( s.size() == 2 );
	}
	
	public void testConstraintDatabaseSupport3() {
		ConstraintDatabase a = new ConstraintDatabase();
		ConstraintDatabase b = new ConstraintDatabase();
		
		a.add(new Statement("(s1 (p a b) c)"));
		a.add(new Statement("(s2 (p a b) c)"));
		a.add(new Statement("(s3 (q d) f)"));
		a.add(new Statement("(s4 (q e) g)"));
		b.add(new Statement("(?K1 (p ?X ?Y) ?Z )"));
		b.add(new Statement("(?K2 (q ?V) ?W )"));
		
		Collection<Substitution> s = a.getSubstitutions(b);
		
		assertTrue( s.size() == 4 );
	}
	
	public void testConstraintDatabaseSupport4() {
		ConstraintDatabase a = new ConstraintDatabase();
		ConstraintDatabase b = new ConstraintDatabase();
		
//		a.F.add(new StateVariable("s1,p(a,b),c )"));
//		a.F.add(new StateVariable("s2,p(a,b),c )"));
		a.add(new Statement("(s3 (q d) f)"));
		a.add(new Statement("(s4 (q e) g)"));
		b.add(new Statement("(?K1 (p ?X ?Y) ?Z)"));
//		b.F.add(new StateVariable("K2,q(V),W )"));
		
		Collection<Substitution> s = a.getSubstitutions(b);
		
		assertTrue( s.size() == 0 );
	}
		
	/**
	 * Test difference function. Added after a problem caused by using 
	 * {@link HashSet} to store {@link Constraint}s and changing them
	 * afterwards.
	 */
	public void testDifference()  {
		ConstraintDatabase cDB1 = new ConstraintDatabase();
		cDB1.add( new Statement("(a (a X) Y)") );
		cDB1.add( new PrologConstraint(new Atomic("(rel X Y)"),Term.createConstant("prolog")));
		
		ConstraintDatabase cDB2 = new ConstraintDatabase();
		cDB2.add( new Statement("(a (a a) b)") );
		cDB2.add( new PrologConstraint(new Atomic("(rel a b)"), Term.createConstant("prolog")));
		
		Substitution theta = new Substitution("{X/a,Y/b}");
		
		assertTrue( cDB1.difference(cDB2).size() == 2 );
		assertTrue( cDB2.difference(cDB1).size() == 2 );
				
		cDB1.substitute(theta);		
	}
	
	public void testCompression()  {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new Statement("(a x v)"));
		cDB.add(new Statement("(b x v)"));
		cDB.add(new Statement("(c x v)"));
		cDB.add(new Statement("(d x v)"));
		cDB.add(new Statement("(e x v)"));
		cDB.add(new Statement("(f x v)"));
		
		cDB.add(new AllenConstraint("a Equals b"));
		cDB.add(new AllenConstraint("c Equals d"));
		cDB.add(new AllenConstraint("e Equals f"));
		
		TemporalNetworkTools.compressTemporalConstraints(cDB);
			
		assertTrue(cDB.get(Statement.class).size() == 3);
	}
	
	public void testCompression2()  {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new Statement("(a x v)"));
		cDB.add(new Statement("(b x v)"));
		cDB.add(new Statement("(c x v)"));
		cDB.add(new Statement("(d x v)"));
		cDB.add(new Statement("(e x v)"));
		cDB.add(new Statement("(f x v)"));
		
		cDB.add(new AllenConstraint("e Equals f"));
		cDB.add(new AllenConstraint("a Equals b"));
		cDB.add(new AllenConstraint("c Equals d"));
		cDB.add(new AllenConstraint("e Equals f"));
		
		TemporalNetworkTools.compressTemporalConstraints(cDB);
		
		assertTrue(cDB.size() == 3);
	}	
	
	public void testCompression3()  {

		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new Statement("(goal_30 (state 4 labAssistance lab1) processing)"));
		cDB.add(new Statement("(c3 (calenderEntry jimmy labWork3 lab1) true)"));
		cDB.add(new Statement("(e1_20 (state 4 labAssistance lab1) processing)"));
		cDB.add(new Statement("(THIS_20_41 (Do r2 4 labAssistance lab1) executing)"));
		
		cDB.add(new AllenConstraint("goal_30 Equals c3"));
		cDB.add(new AllenConstraint("e1_20 Equals THIS_20_41"));
		cDB.add(new AllenConstraint("goal_30 Equals c3"));
		cDB.add(new AllenConstraint("e1_20 Equals goal_30"));
				
		TemporalNetworkTools.compressTemporalConstraints(cDB);
		
		assertTrue(cDB.size() == 5);
	}
	
	public void testSetToCount()  {

		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new Statement("(goal_30 (state 4 labAssistance lab1) processing)"));
		cDB.add(new Statement("(c3 (calenderEntry jimmy labWork3 lab1) true)"));
		cDB.add(new AllenConstraint("goal_30 Equals c4"));
		cDB.add(new AllenConstraint("e1_21 Equals goal_30"));
		
		Map<Class<? extends Constraint>,Integer> cCount = cDB.getConstraintCount();
		cDB.setToConstraintCount(cCount);
		
		assertTrue(cDB.size() == 4);
		
		cDB.add(new Cost(new Atomic("(add c 10)")));
		
		cDB.add(new Statement("(e1_20 (state 4 labAssistance lab1) processing)"));
		cDB.add(new Statement("(THIS_20_41 (Do r2 4 labAssistance lab1) executing)"));
		cDB.add(new AllenConstraint("goal_30 Equals c3"));
		cDB.add(new AllenConstraint("e1_20 Equals THIS_20_41"));
		
		assertTrue(cDB.size() == 9);
		
		cDB.setToConstraintCount(cCount);
						
		assertTrue(cDB.size() == 4);
	}
	
}

