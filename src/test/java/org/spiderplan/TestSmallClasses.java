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
import java.util.List;
import java.util.logging.Level;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverCombination;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Cost;

import org.spiderplan.representation.constraints.Delete;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.TemporalConstraint;
import org.spiderplan.representation.logic.*;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.tools.ExecuteSystemCommand;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.GenericComboIterator;
import junit.framework.TestCase;

/**
 * Test cases for small classes that do not require 
 * more than a few tests.
 * 
 * @author Uwe Köckemann
 *
 */
public class TestSmallClasses extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
			

	
	/**
	 * Simple test for {@link TemporalConstraint}s
	 */
	public void testTemporalConstraint() {	
		AllenConstraint tC = new AllenConstraint("S0 Duration [0,10]");
		
		String s = tC.toString();
			
		String s2 = tC.toString();
		
		assertTrue ( tC.isUnary() );
		assertTrue ( tC.isUnary() );
		
		assertTrue ( s.equals(s2) );
		
		assertTrue ( tC.getFrom().equals(Term.createConstant("S0")) );
	}
	
	/**
	 * Testing the {@link Delete} constraint on a {@link ConstraintDatabase}
	 * with and without {@link Resolver}.
	 */
	public void testDeleteConstraint() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new PrologConstraint(new Atomic("(p x)"), Term.createConstant("pProg")));
		cDB.add(new AllenConstraint(new Atomic("(Meets a b)")));
		Cost cost = new Cost(new Atomic("(add c ?X)")); 
		cDB.add(cost);
		
		assertTrue(cDB.getConstraints().size() == 3);
		
		Delete d = new Delete(new PrologConstraint(new Atomic("(p x)"), Term.createConstant("pProg")));
		
		d.apply(cDB);
		
		assertTrue(cDB.getConstraints().size() == 2);
		
		d = new Delete(cost.copy());
		
		d.apply(cDB);
		
		assertTrue(cDB.getConstraints().size() == 1);
		
		d = new Delete(new AllenConstraint(new Atomic("(Meets a b)")));
		ConstraintDatabase rcDB = new ConstraintDatabase();
		rcDB.add(d);
		Resolver r = new Resolver(rcDB);
		
		r.apply(cDB);
			
		assertTrue(cDB.getConstraints().size() == 1); // the Delete constraint is added
	}
	
	/**
	 * Test {@link GenericComboBuilder}
	 */
	public void testComboBuilderSingleList()  {
		ArrayList<String> in = new ArrayList<String>();
		in.add("A"); 
		in.add("B");
		in.add("C");
		in.add("D");
		
		GenericComboBuilder<String> cB = new GenericComboBuilder<String>();
		
		List<List<String>> combos = cB.getCombosSingleList(in, 2, false);
		
		assertTrue( combos.size() == 12 );
	}
	
	public void testComboIterator()  {
		List<String> in1 = new ArrayList<String>();
		in1.add("A"); 
		in1.add("B");
		in1.add("C");
		in1.add("D");
		
		List<String> in2 = new ArrayList<String>();
		in2.add("1"); 
		in2.add("2");
		in2.add("3");
		in2.add("4");
		
		List<List<String>> toCombine = new ArrayList<List<String>>();
		toCombine.add(in1);
		toCombine.add(in2);
		
		GenericComboIterator<String> cB = new GenericComboIterator<String>(toCombine);
		
		int count = 0;
		for ( List<String> combo : cB ) {
			count++;
		}
		
		assertTrue( count == 16 );
	}
	
	public void testComboIteratorEmptyArg()  {
		List<String> in1 = new ArrayList<String>();	
		List<List<String>> toCombine = new ArrayList<List<String>>();
		toCombine.add(in1);
		
		GenericComboIterator<String> cB = new GenericComboIterator<String>(toCombine);
		
		int count = 0;
		for ( List<String> combo : cB ) {
			count++;
		}
		
		assertTrue( count == 0 );
	}
	
	public void testComboIteratorSingleElement()  {
		List<String> in1 = new ArrayList<String>();	
		in1.add("A");
		List<List<String>> toCombine = new ArrayList<List<String>>();
		toCombine.add(in1);
		
		GenericComboIterator<String> cB = new GenericComboIterator<String>(toCombine);
		
		int count = 0;
		for ( List<String> combo : cB ) {
			count++;
		}
		
		assertTrue( count == 1 );
	}
	
	/**
	 * Test {@link GenericComboBuilder} fringe case with a single
	 * list and single choice
	 */
	public void testComboBuilderSingleListSingleValue()  {
		ArrayList<ArrayList<String>> in = new ArrayList<ArrayList<String>>();
		ArrayList<String> inner = new ArrayList<String>();		
		inner.add("A"); 
		in.add(inner);
		
		GenericComboBuilder<String> cB = new GenericComboBuilder<String>();
		
		ArrayList<ArrayList<String>> combos = cB.getCombos(in);
				
		assertTrue( combos.size() == 1 );
	}
	
	/**
	 * Test the {@link GenericSingleNodeSearch} by setting up
	 * a simple problem and traversing through it by providing the
	 * outside feedback
	 */
	public void testGenericSingleNodeSearch() {
		List<List<Integer>> space = new ArrayList<List<Integer>>();
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(1);
		values.add(2);
		values.add(3);
		space.add(values);
		space.add(values);
		space.add(values);
		
		GenericSingleNodeSearch<Integer> search = new GenericSingleNodeSearch<Integer>(space);
		
		search.advance(true);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(true);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		search.advance(false);
		assertTrue(search.advance(false));
		assertTrue(search.failure());
		assertFalse(search.success());
	}
	
	/**
	 * Check if {@link ResolverCombination} works with two lists
	 */
	public void testResolverCombination() {
		Resolver a = new Resolver(new Substitution("{X/a}"));
		Resolver b = new Resolver(new Substitution("{X/b}"));
		Resolver c = new Resolver(new Substitution("{Y/c}"));
		Resolver d = new Resolver(new Substitution("{Y/d}"));
		
		List<List<Resolver>> ll = new ArrayList<List<Resolver>>();
		List<Resolver> l1 = new ArrayList<Resolver>();
		l1.add(a);
		l1.add(b);
		ll.add(l1);
		List<Resolver> l2 = new ArrayList<Resolver>();
		l2.add(c);
		l2.add(d);
		ll.add(l2);
		
		ResolverCombination rC = new ResolverCombination(ll, "ResCombo", new ConfigurationManager("ResCombo"));
		
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() == null );
	}
	
	/**
	 * Check if {@link ResolverCombination} works with only one list containing four value
	 */
	public void testResolverCombinationSingleList() {
		Resolver a = new Resolver(new Substitution("{X/a}"));
		Resolver b = new Resolver(new Substitution("{X/b}"));
		Resolver c = new Resolver(new Substitution("{Y/c}"));
		Resolver d = new Resolver(new Substitution("{Y/d}"));
		
		List<List<Resolver>> list = new ArrayList<List<Resolver>>();
		List<Resolver> l1 = new ArrayList<Resolver>();
		l1.add(a);
		l1.add(b);
		l1.add(c);
		l1.add(d);
		list.add(l1);
		
		ResolverCombination rC = new ResolverCombination(list, "ResCombo", new ConfigurationManager("ResCombo"));
		
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() != null );
		assertTrue( rC.next() == null );
	}
	
	public void testResolverCombinationSingleListSingleResolver() {
		Resolver a = new Resolver(new Substitution("{X/a}"));
		
		List<List<Resolver>> list = new ArrayList<List<Resolver>>();
		List<Resolver> l1 = new ArrayList<Resolver>();
		l1.add(a);

		list.add(l1);
		
		ResolverCombination rC = new ResolverCombination(list, "ResCombo", new ConfigurationManager("ResCombo"));
		
		assertTrue( rC.next() != null );
		assertTrue( rC.next() == null );
	}
	
	/**
	 * Check if {@link ResolverCombination} works with only one list containing one value
	 */
	public void testResolverCombinationSingleListSingleValue() {
		Resolver a = new Resolver(new Substitution("{X/a}"));
		
		List<List<Resolver>> ll = new ArrayList<List<Resolver>>();
		List<Resolver> l1 = new ArrayList<Resolver>();
		l1.add(a);

		ll.add(l1);
		
		ResolverCombination rC = new ResolverCombination(ll, "ResCombo", new ConfigurationManager("ResCombo"));
		
		assertTrue( rC.next() != null );
		assertTrue( rC.next() == null );
	}
	
	
	public void testComplieWithGroups() throws ParseException { 
		Core c = Compile.compile("./data/domains/test-cases/test-groups.uddl");
		
//		System.out.println(c.getContext());
		
		assertTrue(c.getContext().getConstraints().get(AllenConstraint.class).size() == 12);
	}
	
	
//	public void testROSSub() {
//		System.out.println(ROSProxy.subscribeToTopic("/chatter", "Pose", "pose"));
//		
//		while ( true ) {
//			Term t = ROSProxy.read_msg("/chatter");
//			System.out.println("Received:\n" + t);
//			try {
//				Thread.sleep(500);
//			} catch ( Exception e ) {
//				
//			}
//		}
//	}
//	public void testROSPub() {
//		System.out.println(ROSProxy.publishToTopic("/chatterPub", "Point"));
//				
//		while ( true ) {
//			ROSProxy.send_msg("/chatterPub", Term.Parse("(Point position (float x 1.0) (float y 2.0) (float z 3.0))"));
//			try {
//				Thread.sleep(2000);
//			} catch ( Exception e ) {
//				
//			}
//		}
//	}
//	public void testROSServiceCall() {
//		while ( true ) {
//			Term a = Term.Integer(20);
//			Term b = Term.Integer(33);
//			Term req = Term.Parse("(AddTwoInts srv (int a 13) (int b 44))");
//			Term response = ROSProxy.srv_call(req);
//			System.out.println(response);
//			try {
//				Thread.sleep(2000);
//			} catch ( Exception e ) {
//				
//			}
//		}
//	}
	
//	public void testSayStuff() {
//		ExecuteSystemCommand.call("/tmp/", "rosrun sound_play say.py 'If youre havin A.I. problems, I feel bad for you son. Ive got 99 problems, but a human aint one.'");
//	}
	
//	public void testExternalCommand() {
//		
//		String[] result = ExecuteSystemCommand.call("/tmp/", "ls");
//		
//		System.out.println(result[0]);
//		
//	}
}

