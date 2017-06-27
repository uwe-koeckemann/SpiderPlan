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

import java.util.HashMap;
import java.util.HashSet;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.StateVariableOperatorMultiState;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Term;
import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestOperator extends TestCase {

	Term bkbName = Term.createConstant("prolog");
	
	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testMatching() {
		Operator o = new Operator();
		o.setName(Term.parse("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E (at ?R) ?B)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(Term.parse("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(div ?D ?V ?T)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(Term.parse("(drive r a b)"));
		oGround.addPrecondition(new Statement("(p (at r) a)"));
		oGround.addEffect(new Statement("(e (at r) b)"));
		oGround.addConstraint(new AllenConstraint("op Duration [5,5]"));
		oGround.addConstraint(new PrologConstraint(Term.parse("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(div 20 4 5)"), bkbName));
		
		Substitution theta = o.match(oGround);
		
		assertTrue( theta.getMap().get(Term.parse("?D")).equals(Term.parse("20")) );
		assertTrue( theta.getMap().get(Term.parse("?R")).equals(Term.parse("r")) );
		assertTrue( theta.getMap().get(Term.parse("?A")).equals(Term.parse("a")) );
		assertTrue( theta.getMap().get(Term.parse("?B")).equals(Term.parse("b")) );
		assertTrue( theta.getMap().get(Term.parse("?E")).equals(Term.parse("e")) );
		assertTrue( theta.getMap().get(Term.parse("?P")).equals(Term.parse("p")) );
		assertTrue( theta.getMap().get(Term.parse("?V")).equals(Term.parse("4")) );
		assertTrue( theta.getMap().get(Term.parse("?T")).equals(Term.parse("5")) );
		assertTrue( theta.getMap().get(Term.parse("?THIS")).equals(Term.parse("op")) );		
	}
	
	
	public void testMatching2() {
		Operator o = new Operator();
		o.setName(Term.parse("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E (at ?R) ?B)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(Term.parse("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(div ?D ?V ?T)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(Term.parse("(drive r a b)"));
		oGround.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		oGround.addEffect(new Statement("(e (at r) b)"));
		oGround.addConstraint(new AllenConstraint("op Duration [6,6]"));
		oGround.addConstraint(new PrologConstraint(Term.parse("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(div 20 4 5)"), bkbName));
		
		Substitution theta = o.match(oGround);
		
		assertTrue( theta == null );
	}

	public void testLifting() {
		Operator o = new Operator();
		o.setName(Term.parse("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E1 (at ?R) ?B)"));
		o.addEffect(new Statement("(?E2 (space ?B) ?S)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(Term.parse("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(div ?D ?V ?T)"), bkbName));
		o.addConstraint(new PrologConstraint(Term.parse("(size ?R ?S)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(Term.parse("(drive r a b)"));
		oGround.addPrecondition(new Statement("(p (at r) a)"));
		oGround.addEffect(new Statement("(e1 (at r) b)"));
		oGround.addEffect(new Statement("(e2 (space b) 3)"));
		oGround.addConstraint(new AllenConstraint("op Duration [5,5]"));
		oGround.addConstraint(new PrologConstraint(Term.parse("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(div 20 4 5)"), bkbName));
		oGround.addConstraint(new PrologConstraint(Term.parse("(size r 3)"), bkbName));
		
		Operator a =  oGround.liftVariable(0, o);
		Operator b =  oGround.liftVariable(1, o);
		Operator c =  oGround.liftVariable(2, o);
			
		Operator ab = a.liftVariable(1, o);
		Operator ac = a.liftVariable(2, o);
		Operator bc = b.liftVariable(2, o);
		
		Operator abc = ab.liftVariable(2, o);
		
		assertTrue( a.getName().getArg(0).isVariable() );
		assertTrue( !a.getName().getArg(1).isVariable() );
		assertTrue( !a.getName().getArg(2).isVariable() );
		
		assertTrue( !b.getName().getArg(0).isVariable() );
		assertTrue(  b.getName().getArg(1).isVariable() );
		assertTrue( !b.getName().getArg(2).isVariable() );
		
		assertTrue( !c.getName().getArg(0).isVariable() );
		assertTrue( !c.getName().getArg(1).isVariable() );
		assertTrue( c.getName().getArg(2).isVariable() );
		
		assertTrue( ab.getName().getArg(0).isVariable() );
		assertTrue( ab.getName().getArg(1).isVariable() );
		assertTrue( !ab.getName().getArg(2).isVariable() );
		
		assertTrue( ac.getName().getArg(0).isVariable() );
		assertTrue( !ac.getName().getArg(1).isVariable() );
		assertTrue( ac.getName().getArg(2).isVariable() );
		
		assertTrue( !bc.getName().getArg(0).isVariable() );
		assertTrue(  bc.getName().getArg(1).isVariable() );
		assertTrue(  bc.getName().getArg(2).isVariable() );
		
		assertTrue(  abc.getName().getArg(0).isVariable() );
		assertTrue(  abc.getName().getArg(1).isVariable() );
		assertTrue(  abc.getName().getArg(2).isVariable() );
		
	}
	
	public void testStateVariableOperatorApplicability() {
		Term sv1_1 = Term.parse("(p a)");		
		Term sv1_val1 = Term.createConstant("g");
		Term sv2_1 = Term.parse("(q d)");
		Term sv2_val1 = Term.createConstant("i");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(Term.parse("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(Term.parse("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(Term.parse("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Term,Term> s1 = new HashMap<Term, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv2_1, sv2_val1);
		
		assertTrue( o.getApplicableActions(s1).size() == 1 ); 
	}
	
	public void testStateVariableOperatorApplicability2() {
		Term sv1_1 = Term.parse("(p a)");		
		Term sv1_2 = Term.parse("(p b)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		
		Term sv2_1 = Term.parse("(q d)");
		Term sv2_2 = Term.parse("(q e)");
		Term sv2_val1 = Term.createConstant("i");
		Term sv2_val2 = Term.createConstant("j");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(Term.parse("(op W X Y Z)"));
		o.getPreconditions().put(Term.parse("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(Term.parse("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Term,Term> s1 = new HashMap<Term, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv2_1, sv2_val1);
		s1.put(sv1_2, sv1_val2);
		s1.put(sv2_2, sv2_val2);
		
		assertTrue( o.getApplicableActions(s1).size() == 4 ); 
	}
	
	public void testStateVariableOperatorApplicability3() {
		Term sv1_1 = Term.parse("(p a)");		
		Term sv1_2 = Term.parse("(p b)");
		Term sv1_3 = Term.parse("(p c)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		Term sv1_val3 = Term.createConstant("i");
		
		Term sv2_1 = Term.parse("(q d)");
		Term sv2_2 = Term.parse("(q e)");
		Term sv2_3 = Term.parse("(q f)");
		Term sv2_val1 = Term.createConstant("j");
		Term sv2_val2 = Term.createConstant("k");
		Term sv2_val3 = Term.createConstant("l");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(Term.parse("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(Term.parse("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(Term.parse("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Term,Term> s1 = new HashMap<Term, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv2_1, sv2_val1);
		s1.put(sv1_2, sv1_val2);
		s1.put(sv2_2, sv2_val2);
		s1.put(sv1_3, sv1_val3);
		s1.put(sv2_3, sv2_val3);

		assertTrue( o.getApplicableActions(s1).size() == 9 ); 
	}
		
	/**
	 * Two state variables that could be matched to be equal
	 * have to be merged to different.
	 */
	public void testStateVariableOperatorApplicability4() {
		Term sv1_1 = Term.parse("(p a)");		
		Term sv1_2 = Term.parse("(p b)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
				
		StateVariableOperator o = new StateVariableOperator();
		o.setName(Term.parse("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(Term.parse("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(Term.parse("(p ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Term,Term> s1 = new HashMap<Term, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv1_2, sv1_val2);
		
		
		assertTrue( o.getApplicableActions(s1).size() == 2 ); 
	}
	
//	public void testStateVariableOperatorApplicabilityMultiState() {
//		Atomic sv1_1 = Atomic.parse("(p a)");		
//
//		Term sv1_val1 = Term.createConstant("g");
//		Term sv1_val2 = Term.createConstant("h");
//		Term sv1_val3 = Term.createConstant("i");
//		
//		Atomic sv2_1 = Atomic.parse("(q d)");
//
//		Term sv2_val1 = Term.createConstant("j");
//		Term sv2_val2 = Term.createConstant("k");
//		Term sv2_val3 = Term.createConstant("l");
//		
//		StateVariableOperator o = new StateVariableOperator();
//		o.setName(Atomic.parse("(op ?W ?X ?Y ?Z)"));
//		o.getPreconditions().put(Atomic.parse("(p ?W)"), Term.createVariable("?X"));
//		o.getPreconditions().put(Atomic.parse("(q ?Y)"), Term.createVariable("?Z"));
//		
//		HashMap<Atomic,Collection<Term>> s1 = new HashMap<Atomic, Collection<Term>>();
//		s1.put(sv1_1, new HashSet<Term>());
//		s1.get(sv1_1).add(sv1_val1);
//		s1.get(sv1_1).add(sv1_val2);
//		s1.get(sv1_1).add(sv1_val3);
//		
//		
//		s1.put(sv2_1, new HashSet<Term>());
//		s1.get(sv2_1).add(sv2_val1);
//		s1.get(sv2_1).add(sv2_val2);
//		s1.get(sv2_1).add(sv2_val3);
//
//		assertTrue( o.getApplicableActionsFromMultiState(s1, new TypeManager()).size() == 9 ); 
//	}
		
//	public void testStateVariableOperatorMultiStateApplicability() {
//		Atomic sv1_1 = Atomic.parse("p");		
//
//		Term sv1_val1 = Term.createConstant("g");
//		Term sv1_val2 = Term.createConstant("h");
//		Term sv1_val3 = Term.createConstant("i");
//		
//		Atomic sv2_1 = Atomic.parse("q");
//
//		Term sv2_val1 = Term.createConstant("j");
//		Term sv2_val2 = Term.createConstant("k");
//		Term sv2_val3 = Term.createConstant("l");
//		
//		StateVariableOperatorMultiState o = new StateVariableOperatorMultiState();
//		o.setName(Atomic.parse("(op ?X ?Y)"));
//		o.getPreconditions().put(Atomic.parse("p"), Term.createVariable("?X"));
//		o.getPreconditions().put(Atomic.parse("q"), Term.createVariable("?Y"));
//		
//		HashMap<Atomic,List<Term>> s1 = new HashMap<Atomic, List<Term>>();
//		s1.put(sv1_1, new ArrayList<Term>());
//		s1.get(sv1_1).add(sv1_val1);
//		s1.get(sv1_1).add(sv1_val2);
//		s1.get(sv1_1).add(sv1_val3);
//		
//		
//		s1.put(sv2_1, new ArrayList<Term>());
//		s1.get(sv2_1).add(sv2_val1);
//		s1.get(sv2_1).add(sv2_val2);
//		s1.get(sv2_1).add(sv2_val3);
//		
//		TypeManager tM = new TypeManager();
//		
//		assertTrue( o.getApplicableActionsFromMultiState(s1, tM).size() == 9 );
//	}
	
	public void testConversion() {
		Operator o = new Operator();
		o.setName(Term.parse("test"));
		
		o.addEffect(new Statement("(e1 var val1)"));
		o.addEffect(new Statement("(e2 var val2)"));
		o.addEffect(new Statement("(e3 var val3)"));
		
		HashSet<String> usedVars = new HashSet<String>();
		usedVars.add("var/0");
		
		StateVariableOperatorMultiState svo = o.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
				
		assertTrue(svo.getEffects().get(Term.parse("var")).size() == 3); 
	}
}

