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
import java.util.List;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverCombination;
import org.spiderplan.representation.ConstraintDatabase;

import org.spiderplan.representation.expressions.cost.Cost;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.misc.Delete;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.DateTimeReference;
import org.spiderplan.representation.logic.*;
import org.spiderplan.representation.parser.Compile;
import org.spiderplan.representation.parser.pddl.ParseException;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.GenericComboIterator;
import org.spiderplan.tools.SimpleParsing;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
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
	
	public void testDateTimeReference() throws java.text.ParseException {
		DateTimeReference ref = new DateTimeReference(Term.createComplex("(date-time-reference", 
				Term.createConstant("yyyy/MM/dd hh:mm:ss.SSS"), 
				Term.parse("(datetime now)"), 
				Term.parse("(timespan (hours 1))")));
	
	
		long internalTimeOffset = ref.term2internal(Term.parse("(offset (datetime now) (timespan (hours 10))"));
		assertTrue(internalTimeOffset == 10);
		
		long internalTimeSpan = ref.term2internal(Term.parse("(timespan (hours 10)"));
		assertTrue( internalTimeSpan == 10 );
		
		internalTimeSpan = ref.term2internal(Term.parse("(timespan (hours 10) (minutes 30))"));
		assertTrue( internalTimeSpan == 10 );
		
		internalTimeSpan = ref.term2internal(Term.parse("(timespan (hours 10) (minutes 90))"));
		assertTrue( internalTimeSpan == 11 );
		
		internalTimeSpan = ref.term2internal(Term.parse("(timespan (days 2))"));
		assertTrue( internalTimeSpan == 48 );
	}
	
	/**
	 * Testing the {@link Delete} constraint on a {@link ConstraintDatabase}
	 * with and without {@link Resolver}.
	 */
	public void testDeleteConstraint() {
		ConstraintDatabase cDB = new ConstraintDatabase();
		cDB.add(new PrologConstraint(Term.parse("(p x)"), Term.createConstant("pProg")));
		cDB.add(new AllenConstraint(Term.parse("(Meets a b)")));
		Cost cost = new Cost(Term.parse("(add c ?X)")); 
		cDB.add(cost);
		
		assertTrue(cDB.size() == 3);
		
		Delete d = new Delete(new PrologConstraint(Term.parse("(p x)"), Term.createConstant("pProg")));
		
		d.apply(cDB);
		
		assertTrue(cDB.size() == 2);
		
		d = new Delete(cost);
		
		d.apply(cDB);
		
		assertTrue(cDB.size() == 1);
		
		d = new Delete(new AllenConstraint(Term.parse("(Meets a b)")));
		ConstraintDatabase rcDB = new ConstraintDatabase();
		rcDB.add(d);
		Resolver r = new Resolver(rcDB);
		
		r.apply(cDB);
			
		assertTrue(cDB.size() == 1); // the Delete constraint is added
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
		
//		int count = 0;
//		for ( List<String> combo : cB ) {
//			count++;
//		}
		
		assertTrue( cB.getNumCombos() == 16 );
	}
	
	public void testComboIteratorEmptyArg()  {
		List<String> in1 = new ArrayList<String>();	
		List<List<String>> toCombine = new ArrayList<List<String>>();
		toCombine.add(in1);
		
		GenericComboIterator<String> cB = new GenericComboIterator<String>(toCombine);
		
//		int count = 0;
//		for ( List<String> combo : cB ) {
//			count++;
//		}
		
		assertTrue( cB.getNumCombos() == 0 );
	}
	
	public void testComboIteratorSingleElement()  {
		List<String> in1 = new ArrayList<String>();	
		in1.add("A");
		List<List<String>> toCombine = new ArrayList<List<String>>();
		toCombine.add(in1);
		
		GenericComboIterator<String> cB = new GenericComboIterator<String>(toCombine);
		
		assertTrue( cB.getNumCombos() == 1 );
	}
	
	/**
	 * Test {@link GenericComboBuilder} fringe case with a single
	 * list and single choice
	 */
	public void testComboBuilderSingleListSingleValue()  {
		List<List<String>> in = new ArrayList<List<String>>();
		List<String> inner = new ArrayList<String>();		
		inner.add("A"); 
		in.add(inner);
		
		GenericComboBuilder<String> cB = new GenericComboBuilder<String>();
		
		List<List<String>> combos = cB.getCombos(in);
				
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
	
	/**
	 * Test conversion of Prolog format to s-expressions.
	 * 
	 * @throws ParseException
	 */
	public void testConvertPrologToMine() throws ParseException {
		assertTrue(SimpleParsing.convertTermFormat("f(x)").equals("(f x)"));
		assertTrue(SimpleParsing.convertTermFormat("f(x,y,z)").equals("(f x y z)"));
		assertTrue(SimpleParsing.convertTermFormat("f(x,y,z,g(z))").equals("(f x y z (g z))"));
		assertTrue(SimpleParsing.convertTermFormat("f(f(f(f(f(x)))))").equals("(f (f (f (f (f x)))))"));
	}
	
	public void testComplieWithGroups() throws ParseException { 
		Core c = Compile.compile("./domains/test-cases/test-groups.uddl");
		
//		System.out.println(c.getContext());
		
		assertTrue(c.getContext().get(AllenConstraint.class).size() == 13);
	}
	
//	public void testSparQL() throws IOException {
//		// create an empty model
//		 Model model = ModelFactory.createDefaultModel();
//
//		 InputStream in = FileManager.get().open( "./foaf.rdf" );
//		 if (in == null) {
//		     throw new IllegalArgumentException("File: " + "./foaf.rdf" + " not found");
//	 	}
//		model.read(in, null);  
//		
//		
//		byte[] encoded = Files.readAllBytes(Paths.get("./query.sparql"));
//		String queryStr =  new String(encoded, Charset.defaultCharset());
//		
//		System.out.println(queryStr);
//
//		List <QuerySolution> resultList;
//		Query query = QueryFactory.create(queryStr);
//		QueryExecution qexec = QueryExecutionFactory.create(query, model);
//
//		List<Term> argList = new ArrayList<Term>();
//		argList.add(Term.createVariable("homepage"));
//		
//		try {	
//			ResultSet resultSet = qexec.execSelect();	
//			System.out.println(resultSet.hasNext());
//			resultList = ResultSetFormatter.toList(resultSet);
//			for ( QuerySolution sol : resultList ) {
//				for ( Term arg : argList ) {
//					Term value = Term.createConstant(sol.getResource(arg.toString()).toString());
//					System.out.println(arg + " := " + value);
//				}
//			}
//		}
//		catch(Exception e) {
//			resultList = null;
//		}
//		finally{
//			qexec.close();
//		}
//						
////		List <QuerySolution> solutionList = getQueryResultSet(program.toString());
////		String[] sensingInfo = new String[2];
////		sensingInfo[0] = solutionList.get(0).getResource("s").toString().split(SmartHomeOntology.URI_ENTITY_SPLITTER)[1];
////		sensingInfo[1] = solutionList.get(0).getResource("foi").toString().split(SmartHomeOntology.URI_ENTITY_SPLITTER)[1];
//		
//	}
	
	
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

