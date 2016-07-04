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
import java.util.Set;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestLogic extends TestCase {
	
	
	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testAtomic() {	
		Atomic a = new Atomic("(p a ?B (f ?x))");
				
		assertTrue( a.name().equals("p") );

		assertTrue( a.getArg(0).toString().equals("a") );
		
		assertTrue( a.getArg(1).toString().equals("?B") );
		assertTrue( a.getArg(2).toString().equals("(f ?x)") );
		assertTrue( a.getNumArgs() == 3 );
		assertTrue( a.toString().equals("(p a ?B (f ?x))"));
		
		Atomic b = new Atomic("(sv1 sv1_t sv2_t)");
		assertTrue(b.toString().equals("(sv1 sv1_t sv2_t)"));
	}
	
	public void testIsVariable() {
		Term a = Term.parse("?A");
		assertTrue( a.isVariable() );
	}
	
	public void testComplex() {
		Term a = Term.parse("(p x y)");
		assertTrue(a.isComplex());
		Term b = Term.parse("(p)");
		assertFalse(b.isComplex());
	}
	
	public void testTermEquals() {	
		Term a = Term.createComplex("f", Term.createConstant("x"));
		Term b = Term.createComplex("f", Term.createConstant("x"));

		assertTrue( a.hashCode() == b.hashCode() );
		assertTrue( a.equals(b) );		
	}
	
	public void testAtomicEquals() {	
		Atomic a = new Atomic("(p a B (f x))");
		Atomic b = new Atomic("(p a B (f x))");
		
		assertTrue( a.hashCode() == b.hashCode() );
		assertTrue( a.equals(b) ); //
		
		
		Atomic c = new Atomic("p");
		Atomic d = new Atomic("(p)");
				
		assertTrue( c.equals(d) );		
	}
	
	public void testMatching() {	
		Atomic a1 = new Atomic("(p A ?x1 ?x2)");
		Atomic a2 = new Atomic("(p ?x3 ?x4 C)");
		
		Substitution theta = new Substitution();
		
		theta = a1.match(a2);
		
		assertTrue( theta.getStringMap().containsKey("?x3") );
		assertTrue( theta.substitute(Term.parse("?x3")).equals(Term.parse("A")) );
		assertTrue( theta.getStringMap().containsKey("?x1") );
		assertTrue( theta.substitute(Term.parse("?x1")).equals(Term.parse("?x4")) );
		assertTrue( theta.getStringMap().containsKey("?x2") );
		assertTrue( theta.substitute(Term.parse("?x2")).equals(Term.parse("C")) );
	}
	
	public void testMatching2() {	
		Atomic a1 = new Atomic("(p A x1 x2)");
		Atomic a2 = new Atomic("(p B x4 C)");
		
		Substitution theta = new Substitution();
		
		theta = a1.match(a2);
		
		assertTrue( theta == null );
	}
	
	public void testMatching3() {	
		Atomic a1 = new Atomic("(p A B C)");
		Atomic a2 = new Atomic("(p ?x1 ?x2 ?x3)");
		
		Substitution theta = new Substitution();
		
		theta = a1.match(a2);
		
		assertTrue( theta.getStringMap().containsKey("?x1") );
		assertTrue( theta.substitute(Term.parse("?x1")).equals(Term.parse("A")) );
		assertTrue( theta.getStringMap().containsKey("?x2") );
		assertTrue( theta.substitute(Term.parse("?x2")).equals(Term.parse("B")) );
		assertTrue( theta.getStringMap().containsKey("?x3") );
		assertTrue( theta.substitute(Term.parse("?x3")).equals(Term.parse("C")) );
	}
	
//	public void testLiteral() {
//		Literal l,l2;
//
//		l = new Literal("!p(a,B,f(x))");
//		l2 = new Literal("p(a,B,f(x))");
//		
//		assertTrue( l.isNegated() );
//		assertTrue( ! l2.isNegated() );
//		assertTrue( l2.toString().equals("p(a,B,f(x))"));
//		assertTrue ( l.toString().equals("!p(a,B,f(x))"));
//	}
//	
//	public void testLiteralSubstitution() {
//		Literal l,l2;
//
//		l = new Literal("!p(a,B,f(x))");
//		l2 = new Literal("p(a,B,f(x))");
//		Substitution theta = new Substitution();
//		theta.add(Term.Parse("x"),Term.Parse("H"));
//		
//		l2.substitute(theta);
//
//		Literal l3 = l.copySubstitute(theta);
//		
//		assertTrue( l.toString().equals("!p(a,B,f(x))"));
//		
//		assertTrue( l3.toString().equals("!p(a,B,f(H))"));		
//	}
	
	public void testMatchBug() {
		
		Atomic a = new Atomic("(movable (ground handRob2))");
		Atomic b = new Atomic("(movable a)");
		Substitution theta = a.match(b);
		assertTrue( theta == null );
		
	}
	
	/**
	 * It's possible to substitute ground terms:
	 */
	public void testTermSubstitution() {
		Term x = Term.parse("?x");
		Term y = Term.parse("?y");
		
		Substitution theta = new Substitution("{?x/?y}");
		x = x.substitute(theta);
		assertTrue( x.equals(y) );
	}
	
	/**
	 * It's possible to substitute ground terms:
	 */
	public void testComplexSubstitution() {
		Term x = Term.parse("(f a (g b ?N))");
				
		Substitution theta = new Substitution("{?N/20}");
		Term x1 = x.substitute(theta);

		assertTrue( x1.toString().equals("(f a (g b 20))") );
		assertTrue( x.toString().equals("(f a (g b ?N))") );
	}

	/**
	 * Substitute complex terms:
	 */
	public void testTermSubstitutionComplex() {
		Term x = Term.createVariable("A");
		Term y = Term.createComplex("p", Term.createConstant("x"), Term.createConstant("y"));
		
		Substitution theta = new Substitution();
		theta.add(x,y);
		
		x = x.substitute(theta);

		assertTrue( x.equals(y) );
		
	}
	/*
	 * Multiple substitution necessary
	 */
	public void testTermMultiSubstitution() {
		
		Term x = Term.parse("?x");
		Term z = Term.parse("z");
		
		Substitution theta = new Substitution("{?x/?y,?y/z}");
		
		x = x.substitute(theta);

		assertTrue( x.equals(z) );
		
		
	}
	
	/**
	 * Substitute a variable twice
	 */
	public void testTermSubstVarTwice() {
		
		Term x = Term.createConstant("x");
		Term y = Term.createConstant("y");
		Term z = Term.createConstant("z");
		
		Substitution theta = new Substitution();
		Substitution thetaAdd = new Substitution();
		
		theta.add(x,y);
		thetaAdd.add(x,z);

		assertFalse( theta.add(thetaAdd) );
	
		
	}
	/**
	 * Substitute a variable twice
	 */
	public void testSubstAddMerge() {
		Term x = Term.parse("?X");
		Term y = Term.parse("?Y");
		Term z = Term.parse("?Z");
		Term c = Term.parse("c");
		
		Substitution theta = new Substitution();

		theta.add(x,y);
		theta.add(y,c);
		
		assertTrue( theta.addAndMergeVariables(x,z) );
		assertTrue( theta.substitute(z).equals(c) );
	}
	
	/**
	 * Substitute a variable twice
	 */
	public void testSubstAddMerge2() {
		
		Term x1 = Term.parse("?X1");
		Term x2 = Term.parse("?X2");
		Term x3 = Term.parse("?X3");
		Term x4 = Term.parse("?X4");
		
		Substitution theta = new Substitution();

		theta.add(x1,x2);
		theta.add(x2,x3);
		
		assertTrue( theta.addAndMergeVariables(x1,x4) );
		
		assertTrue( theta.substitute(x3).equals(x4) );
		
		
	}
	
	public void testMatchBug2() {
		
		
//		Bug:		
//		Action: Put(rob1,d,ground(loc3),loc3)
//		Operator: Put(R,Thing,Thing2,L)
//		Theta: {R/rob1, Thing/d, L/loc3}
		
		Atomic a = new Atomic("Put", Term.createConstant("rob1"), Term.createConstant("d"), Term.createComplex("ground", Term.createConstant("loc3")),Term.createConstant("loc3"));
		Atomic b = new Atomic("Put", Term.createVariable("?R"),Term.createVariable("?Thing"),Term.createVariable("?Thing2"),Term.createVariable("?L"));
		Substitution theta = a.match(b);

		assertTrue( theta.size() == 4 );
		
	}
	
//	public void testMatch() {
//		Literal a = new Literal("location(T,storage(A))");
//		Literal b = new Literal("location(a,storage(rob1))");
//		
//		Substitution theta = a.match(b);
//		assertTrue( theta.size() == 2 );
//	}
//	
//	public void testMatchComplexTerms() {
//		
//		Literal a = new Literal("location(T,storage(A))");
//		Literal b = new Literal("location(a,storage(rob1))");
//		
//		Substitution theta = a.match(b);
//		assertTrue( theta.size() == 2 );
//		
//		Literal c = a.copySubstitute(theta);
//		
//		assertTrue(b.toString().equals(c.toString()));
//		
//	}
	
	public void testGetAllGround() {
		
		TypeManager tM;
		
		Atomic v1 = new Atomic("sv1", Term.parse("?A"), Term.parse("?B"));
	
		tM = new TypeManager();
		
		tM.addSimpleEnumType("boolean", "true,false");
		
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.addSimpleEnumType("sv2_t", "c,d");
		
		tM.attachTypes("(sv1 sv1_t sv2_t)");
		
		Collection<Atomic> a = tM.getAllGroundAtomics(v1);
			
		assertTrue( a.size() == 4 );
		
		assertTrue( a.contains(new Atomic("(sv1 a d)")) );
		assertTrue( a.contains(new Atomic("(sv1 a c)")) );
		assertTrue( a.contains(new Atomic("(sv1 b c)")) );
		assertTrue( a.contains(new Atomic("(sv1 b d)")) );
		
		
	}
	
	public void testSetMembership() {
		Set<Atomic> S = new HashSet<Atomic>();
		
		Atomic v1 = new Atomic("(p a b)");
		Atomic v2 = new Atomic("(p a b)");
		
		S.add(v1);
		
		assertTrue( S.contains(v2) );
		
	}
	
	public void testNoChange() {
		
		Term x = Term.createVariable("?x");
		Term a = Term.createConstant("a");
				
		Substitution theta = new Substitution();
			
		theta.add(x,a);
		
		Term after = x.substitute(theta);
		
		assertTrue( x.equals(Term.createVariable("?x")) );
		assertTrue( after.equals(a) );
			
		after = x.makeConstant();
		
		assertTrue( x.equals(Term.createVariable("?x")) );
		assertTrue( after.equals(Term.createConstant("x")) );
		
		after = Term.createVariable(a.getName().toString());
		assertTrue( a.equals(Term.createConstant("a")) );
		assertTrue( after.equals(Term.createVariable("?a")) );
		

		
		after = x.makeUnique(0);
		
		assertTrue( x.equals(Term.createVariable("?x")) );
		assertTrue( after.equals(Term.createVariable("x_0")) );
		
		
	}
	
//	public void testContainsTerm() {
//		Term t1 = Term.Parse("g1");
//		Term t2 = Term.Parse("g1");
//		
//		assertTrue( t1.equals(t2) );
//		assertTrue( t2.equals(t1) );
//		
//		ArrayList<Term> l = new ArrayList<Term>();
//		l.add(t1);
//		
//		boolean contains = false;
//		for ( Object t : l ) {
//			if ( t.equals(t2) ) {
//				contains = true;
//			}
//		}			
//		assertTrue( contains );
//		
//	
//		assertTrue(l.contains(t2));
//		
//	}
}
