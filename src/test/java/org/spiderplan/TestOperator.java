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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.spiderplan.causal.StateVariableOperator;
import org.spiderplan.causal.StateVariableOperatorMultiState;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;

import junit.framework.TestCase;

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
		o.setName(new Atomic("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E (at ?R) ?B)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(new Atomic("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(div ?D ?V ?T)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(new Atomic("(drive r a b)"));
		oGround.addPrecondition(new Statement("(p (at r) a)"));
		oGround.addEffect(new Statement("(e (at r) b)"));
		oGround.addConstraint(new AllenConstraint("op Duration [5,5]"));
		oGround.addConstraint(new PrologConstraint(new Atomic("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(div 20 4 5)"), bkbName));
		
		Substitution theta = o.match(oGround);
		
		assertTrue( theta.getStringMap().get("?D").equals("20") );
		assertTrue( theta.getStringMap().get("?R").equals("r") );
		assertTrue( theta.getStringMap().get("?A").equals("a") );
		assertTrue( theta.getStringMap().get("?B").equals("b") );
		assertTrue( theta.getStringMap().get("?E").equals("e") );
		assertTrue( theta.getStringMap().get("?P").equals("p") );
		assertTrue( theta.getStringMap().get("?V").equals("4") );
		assertTrue( theta.getStringMap().get("?T").equals("5") );
		assertTrue( theta.getStringMap().get("?THIS").equals("op") );		
	}
	
	
	public void testMatching2() {
		Operator o = new Operator();
		o.setName(new Atomic("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E (at ?R) ?B)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(new Atomic("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(div ?D ?V ?T)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(new Atomic("(drive r a b)"));
		oGround.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		oGround.addEffect(new Statement("(e (at r) b)"));
		oGround.addConstraint(new AllenConstraint("op Duration [6,6]"));
		oGround.addConstraint(new PrologConstraint(new Atomic("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(div 20 4 5)"), bkbName));
		
		Substitution theta = o.match(oGround);
		
		assertTrue( theta == null );
	}

	public void testLifting() {
		Operator o = new Operator();
		o.setName(new Atomic("(drive ?R ?A ?B)"));
		o.addPrecondition(new Statement("(?P (at ?R) ?A)"));
		o.addEffect(new Statement("(?E1 (at ?R) ?B)"));
		o.addEffect(new Statement("(?E2 (space ?B) ?S)"));
		o.addConstraint(new AllenConstraint("?THIS Duration [?T,?T]"));
		o.addConstraint(new PrologConstraint(new Atomic("(distance ?A ?B ?D)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(speed ?R ?V)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(div ?D ?V ?T)"), bkbName));
		o.addConstraint(new PrologConstraint(new Atomic("(size ?R ?S)"), bkbName));
		
		Operator oGround = new Operator();
		oGround.setName(new Atomic("(drive r a b)"));
		oGround.addPrecondition(new Statement("(p (at r) a)"));
		oGround.addEffect(new Statement("(e1 (at r) b)"));
		oGround.addEffect(new Statement("(e2 (space b) 3)"));
		oGround.addConstraint(new AllenConstraint("op Duration [5,5]"));
		oGround.addConstraint(new PrologConstraint(new Atomic("(distance a b 20)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(speed r 4)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(div 20 4 5)"), bkbName));
		oGround.addConstraint(new PrologConstraint(new Atomic("(size r 3)"), bkbName));
		
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
		Atomic sv1_1 = new Atomic("(p a)");		
		Term sv1_val1 = Term.createConstant("g");
		Atomic sv2_1 = new Atomic("(q d)");
		Term sv2_val1 = Term.createConstant("i");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(new Atomic("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Atomic,Term> s1 = new HashMap<Atomic, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv2_1, sv2_val1);
		
		assertTrue( o.getApplicableActions(s1).size() == 1 ); 
	}
	
	public void testStateVariableOperatorApplicability2() {
		Atomic sv1_1 = new Atomic("(p a)");		
		Atomic sv1_2 = new Atomic("(p b)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		
		Atomic sv2_1 = new Atomic("(q d)");
		Atomic sv2_2 = new Atomic("(q e)");
		Term sv2_val1 = Term.createConstant("i");
		Term sv2_val2 = Term.createConstant("j");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(op W X Y Z)"));
		o.getPreconditions().put(new Atomic("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Atomic,Term> s1 = new HashMap<Atomic, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv2_1, sv2_val1);
		s1.put(sv1_2, sv1_val2);
		s1.put(sv2_2, sv2_val2);
		
		assertTrue( o.getApplicableActions(s1).size() == 4 ); 
	}
	
	public void testStateVariableOperatorApplicability3() {
		Atomic sv1_1 = new Atomic("(p a)");		
		Atomic sv1_2 = new Atomic("(p b)");
		Atomic sv1_3 = new Atomic("(p c)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		Term sv1_val3 = Term.createConstant("i");
		
		Atomic sv2_1 = new Atomic("(q d)");
		Atomic sv2_2 = new Atomic("(q e)");
		Atomic sv2_3 = new Atomic("(q f)");
		Term sv2_val1 = Term.createConstant("j");
		Term sv2_val2 = Term.createConstant("k");
		Term sv2_val3 = Term.createConstant("l");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(new Atomic("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Atomic,Term> s1 = new HashMap<Atomic, Term>();
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
		Atomic sv1_1 = new Atomic("(p a)");		
		Atomic sv1_2 = new Atomic("(p b)");
		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
				
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(new Atomic("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("(p ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Atomic,Term> s1 = new HashMap<Atomic, Term>();
		s1.put(sv1_1, sv1_val1);
		s1.put(sv1_2, sv1_val2);
		
		
		assertTrue( o.getApplicableActions(s1).size() == 2 ); 
	}
	
	public void testStateVariableOperatorApplicabilityMultiState() {
		Atomic sv1_1 = new Atomic("(p a)");		

		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		Term sv1_val3 = Term.createConstant("i");
		
		Atomic sv2_1 = new Atomic("(q d)");

		Term sv2_val1 = Term.createConstant("j");
		Term sv2_val2 = Term.createConstant("k");
		Term sv2_val3 = Term.createConstant("l");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(op ?W ?X ?Y ?Z)"));
		o.getPreconditions().put(new Atomic("(p ?W)"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("(q ?Y)"), Term.createVariable("?Z"));
		
		HashMap<Atomic,Collection<Term>> s1 = new HashMap<Atomic, Collection<Term>>();
		s1.put(sv1_1, new HashSet<Term>());
		s1.get(sv1_1).add(sv1_val1);
		s1.get(sv1_1).add(sv1_val2);
		s1.get(sv1_1).add(sv1_val3);
		
		
		s1.put(sv2_1, new HashSet<Term>());
		s1.get(sv2_1).add(sv2_val1);
		s1.get(sv2_1).add(sv2_val2);
		s1.get(sv2_1).add(sv2_val3);

		assertTrue( o.getApplicableActionsFromMultiState(s1, new TypeManager()).size() == 9 ); 
	}
		
	public void testStateVariableOperatorMultiStateApplicability() {
		Atomic sv1_1 = new Atomic("p");		

		Term sv1_val1 = Term.createConstant("g");
		Term sv1_val2 = Term.createConstant("h");
		Term sv1_val3 = Term.createConstant("i");
		
		Atomic sv2_1 = new Atomic("q");

		Term sv2_val1 = Term.createConstant("j");
		Term sv2_val2 = Term.createConstant("k");
		Term sv2_val3 = Term.createConstant("l");
		
		StateVariableOperatorMultiState o = new StateVariableOperatorMultiState();
		o.setName(new Atomic("(op ?X ?Y)"));
		o.getPreconditions().put(new Atomic("p"), Term.createVariable("?X"));
		o.getPreconditions().put(new Atomic("q"), Term.createVariable("?Y"));
		
		HashMap<Atomic,List<Term>> s1 = new HashMap<Atomic, List<Term>>();
		s1.put(sv1_1, new ArrayList<Term>());
		s1.get(sv1_1).add(sv1_val1);
		s1.get(sv1_1).add(sv1_val2);
		s1.get(sv1_1).add(sv1_val3);
		
		
		s1.put(sv2_1, new ArrayList<Term>());
		s1.get(sv2_1).add(sv2_val1);
		s1.get(sv2_1).add(sv2_val2);
		s1.get(sv2_1).add(sv2_val3);
		
		TypeManager tM = new TypeManager();
		
		assertTrue( o.getApplicableActionsFromMultiState(s1, tM).size() == 9 );
	}
	
	public void testConversion() {
		Operator o = new Operator();
		o.setName(new Atomic("test"));
		
		o.addEffect(new Statement("(e1 var val1)"));
		o.addEffect(new Statement("(e2 var val2)"));
		o.addEffect(new Statement("(e3 var val3)"));
		
		HashSet<String> usedVars = new HashSet<String>();
		usedVars.add("var/0");
		
		StateVariableOperatorMultiState svo = o.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
		
		assertTrue(svo.getEffects().get(new Atomic("var")).size() == 3); 
	}
}

