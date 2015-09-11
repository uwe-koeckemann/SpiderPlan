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
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.causal.ApplyPlanIterator;
import org.spiderplan.causal.ForwardPlanningIterator;
import org.spiderplan.causal.ForwardPlanningNode;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import junit.framework.TestCase;

public class TestPlan extends TestCase {
	
	
	@Override
	public void setUp() throws Exception {

	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testIsMatchingSubPlan() {
		
		Operator a1 = new Operator();
		a1.setName(new Atomic("(a1 o1)"));
		Operator a2 = new Operator();
		a2.setName(new Atomic("(a2 o1)"));
		Operator a3 = new Operator();
		a3.setName(new Atomic("(a4 o1)"));
		Operator a4 = new Operator();
		a4.setName(new Atomic("(a5 o1)"));
		Operator a5 = new Operator();
		a5.setName(new Atomic("(a5 o1)"));
		
		Operator a6 = new Operator();
		a6.setName(new Atomic("(a5 ?O)"));		
		
		Plan p1 = new Plan();
		p1.addAction(a1);
		p1.addAction(a2);
		p1.addAction(a3);
		p1.addAction(a4);
		p1.addAction(a5);		
		Plan p2 = new Plan();
		p2.addAction(a1);
		p2.addAction(a2);
		
		assertTrue( p1.isMatchingSubPlan(p2) );
		
		p2.addAction(a2);
		
		assertFalse( p1.isMatchingSubPlan(p2) );
		
		p2.removeAction(2);
		p2.addAction(a3);
		p2.addAction(a4);
		p2.addAction(a5);
		
		assertTrue( p1.isMatchingSubPlan(p2)) ; 
		
		p2.removeAction(4);
		p2.addAction(a6);
		
		assertTrue( p1.isMatchingSubPlan(p2)) ;
	}
	
	public void testApplyPlanIterator() {
		
		ConstraintDatabase context = new ConstraintDatabase();
		context.add(new Statement("(i1 x a)"));
		context.add(new Statement("(i2 x a)"));
		context.add(new Statement("(i3 y b)"));
		context.add(new Statement("(i4 y b)"));
		
		Plan p = new Plan();
		Operator a = new Operator();
		a.setName(new Atomic("op"));
		a.addPrecondition(new Statement("(?P1 x a)"));
		a.addPrecondition(new Statement("(?P2 y b)"));
		p.addAction(a);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("ApplyMod");
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("type", "a,b");
		tM.attachTypes("x=type");
		tM.attachTypes("y=type");
		
		IncrementalSTPSolver stp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		stp.isConsistent(context, tM);
		context.add(stp.getPropagatedTemporalIntervals());
		
		ApplyPlanIterator aI = new ApplyPlanIterator(context, p, "ApplyMod", cM, false, tM);
		
		assertTrue( aI.next() != null );
		assertTrue( aI.next() != null );
		assertTrue( aI.next() != null );
		assertTrue( aI.next() != null );
		assertTrue( aI.next() == null );
	}
	
	public void testApplyPlanIteratorWithEmptyPlan() {
		
		ConstraintDatabase context = new ConstraintDatabase();
		context.add(new Statement("(i1 x a)"));
		context.add(new Statement("(i2 x a)"));
		context.add(new Statement("(i3 y b)"));
		context.add(new Statement("(i4 y b)"));
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("type", "a,b");
		tM.attachTypes("x=type");
		tM.attachTypes("y=type");
		
		Plan p = new Plan();
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("ApplyMod");
		
		IncrementalSTPSolver stp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		stp.isConsistent(context, tM);
		context.add(stp.getPropagatedTemporalIntervals());
		
		ApplyPlanIterator aI = new ApplyPlanIterator(context, p, "ApplyMod", cM, false, tM);
		
		assertTrue( aI.next() != null );
		assertTrue( aI.next() == null );
	}
	
	public void testPlanIterator() {
		Global.resetStatics();
		
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t", "a,b");
		tM.attachTypes(new Atomic("x"), Term.createConstant("t")); 
		tM.attachTypes(new Atomic("y"), Term.createConstant("t"));
		tM.attachTypes(new Atomic("z"), Term.createConstant("t"));
		
		ConstraintDatabase context = new ConstraintDatabase();
		context.add(new Statement("(i1 x a)"));
		context.add(new Statement("(i2 x a)"));
		context.add(new Statement("(i3 z b)"));
		context.add(new Statement("(i4 z b)"));
		context.add(new OpenGoal(new Statement("(g1 y b)")));
		
		ArrayList<OpenGoal> G = new ArrayList<OpenGoal>();
		G.add(new OpenGoal(new Statement("(g1 y b)")));
		
		Operator a1 = new Operator();
		a1.setName(new Atomic("op1"));
		a1.addPrecondition(new Statement("(?P x a)"));
		a1.addEffect(new Statement("(?E y b)"));
		a1.addConstraint(new AllenConstraint("?P Meets ?E"));
		
		Operator a2 = new Operator();
		a2.setName(new Atomic("op2"));
		a2.addPrecondition(new Statement("(?P z b)"));
		a2.addEffect(new Statement("(?E y b)"));
		a2.addConstraint(new AllenConstraint("?P Meets ?E"));
		
		ArrayList<Operator> O = new ArrayList<Operator>();
		O.add(a1);
		O.add(a2);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("Planner");
		
		IncrementalSTPSolver stp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		stp.isConsistent(context, tM);
		context.add(stp.getPropagatedTemporalIntervals());
				
		ForwardPlanningIterator pI = new ForwardPlanningIterator(context, G, O, tM, cM, "Planner");
		
		Resolver r = pI.next();
		assertTrue( r != null );
		r = pI.next();
		assertTrue( r != null );
		r = pI.next();
		assertTrue( r != null );
		r = pI.next();
		assertTrue( r != null );
		assertTrue( pI.next() == null );
		
	}
	
	public void testPlanIteratorWithFutureEventConflict() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("room", "a,b,c");
		tM.addSimpleEnumType("door", "d1,d2");
		tM.addSimpleEnumType("key", "k1,k2");
		tM.addSimpleEnumType("doorState", "open,closed");
		tM.addSimpleEnumType("robot", "r");
		tM.addSimpleEnumType("bool", "true,false");
		tM.addSimpleEnumType("location", "room,robot");
		tM.addSimpleEnumType("object", "robot,key");
		tM.addSimpleEnumType("executing", "executing");
		
		tM.updateTypeDomains();
		
		tM.attachTypes(new Atomic("(at object)"), Term.createConstant("location")); 
		tM.attachTypes(new Atomic("(doorBetween location location)"), Term.createConstant("door"));
		tM.attachTypes(new Atomic("(state door)"), Term.createConstant("doorState"));
		tM.attachTypes(new Atomic("(requires door)"), Term.createConstant("key"));
		
		tM.attachTypes(new Atomic("(move robot room room)"), Term.createConstant("executing"));
		tM.attachTypes(new Atomic("(open robot door room key)"), Term.createConstant("executing"));
		tM.attachTypes(new Atomic("(pickup robot key location)"), Term.createConstant("executing"));

		ConstraintDatabase context = new ConstraintDatabase();
		context.add(new Statement("(s0 (at r) a)"));
		context.add(new Statement("(s1 (at k1) a)"));
		context.add(new Statement("(s2 (at k2) a)"));
		context.add(new Statement("(s3 (doorBetween a b) d1)"));
		context.add(new Statement("(s4 (doorBetween b c) d2)"));
		context.add(new Statement("(s5 (doorBetween b a) d1)"));
		context.add(new Statement("(s6 (doorBetween c b) d2)"));
		context.add(new Statement("(s7 (state d1) open)"));
		context.add(new Statement("(s8 (state d2) closed)"));
		context.add(new Statement("(s9 (requires d1) k1)"));
		context.add(new Statement("(s10 (requires d2) k2)"));

		context.add(new Statement("(sClose (state d2) closed)"));
		
		context.add(new OpenGoal(new Statement("(g1 (at r) c)")));
		context.add(new AllenConstraint("s0 Release [0,0]"));
		context.add(new AllenConstraint("s1 Release [0,0]"));
		context.add(new AllenConstraint("s2 Release [0,0]"));
		context.add(new AllenConstraint("s3 Release [0,0]"));
		context.add(new AllenConstraint("s4 Release [0,0]"));
		context.add(new AllenConstraint("s6 Release [0,0]"));
		context.add(new AllenConstraint("s7 Release [0,0]"));
		context.add(new AllenConstraint("s8 Release [0,0]"));
		context.add(new AllenConstraint("s9 Release [0,0]"));
		context.add(new AllenConstraint("s10 Release [0,0]"));
		
		context.add(new AllenConstraint("sClose Release [25,25]"));
		
		ArrayList<OpenGoal> G = new ArrayList<OpenGoal>();
		G.add(new OpenGoal(new Statement("(g1 (at r) c)")));
		
		Operator a1 = new Operator();
		a1.setName(new Atomic("(move ?R ?A ?B)"));
		a1.addPrecondition(new Statement("(?P1 (at ?R) ?A)"));
		a1.addPrecondition(new Statement("(?P2 (doorBetween ?A ?B) ?D)"));
		a1.addPrecondition(new Statement("(?P3 (state ?D) open)"));
		a1.addEffect(new Statement("(?E (at ?R) ?B)"));
		a1.addConstraint(new AllenConstraint("?P1 Meets ?THIS"));
		a1.addConstraint(new AllenConstraint("?THIS Meets ?E"));
		a1.addConstraint(new AllenConstraint("?THIS Duration [10,10]"));
		a1.addConstraint(new AllenConstraint("?THIS During  ?P3 [1,inf] [1,inf]"));
		
		Operator a2 = new Operator();
		a2.setName(new Atomic("(open ?R ?D ?L ?K)"));
		a2.addPrecondition(new Statement("(?P1 (at ?R) ?L)"));
		a2.addPrecondition(new Statement("(?P2 (at ?K) ?R)"));
		a2.addPrecondition(new Statement("(?P3 (doorBetween ?L ?B) ?D)"));
		a2.addPrecondition(new Statement("(?P4 (state ?D) closed)"));
		a2.addPrecondition(new Statement("(?P5 (requires ?D) ?K)"));
		a2.addEffect(new Statement("(?E (state ?D) open)"));
		a2.addConstraint(new AllenConstraint("?P4 Meets ?E"));
		
		Operator a3 = new Operator();
		a3.setName(new Atomic("(pickup ?R ?K ?L)"));
		a3.addPrecondition(new Statement("(?P1 (at ?R) ?L)"));
		a3.addPrecondition(new Statement("(?P2 (at ?K) ?L)"));
		a3.addEffect(new Statement("(?E (at ?K) ?R)"));
		a3.addConstraint(new AllenConstraint("?P2 Meets ?E"));
		
		ArrayList<Operator> O = new ArrayList<Operator>();
		O.add(a1);
		O.add(a2);
		O.add(a3);
		
		ConfigurationManager cM = new ConfigurationManager();
		cM.add("Planner");
		
		IncrementalSTPSolver stp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
		stp.isConsistent(context, tM);
		context.add(stp.getPropagatedTemporalIntervals());
		
		
		ForwardPlanningIterator pI = new ForwardPlanningIterator(context, G, O, tM, cM, "Planner");
		
		
		assertTrue(pI.next() != null);		
	}
	
	public void testLiftingTerm() {
		
		Operator oLoad1 = new Operator();
		oLoad1.setName(new Atomic("(load r c1 l)"));
		oLoad1.addPrecondition(new Statement("(p1 (at r) l)"));
		oLoad1.addPrecondition(new Statement("(p2 (at c1) l)"));
		oLoad1.addEffect(new Statement("(e1 (at c1) r)"));
		oLoad1.addEffect(new Statement("(e2 (space r) 3)"));
		oLoad1.addConstraint(new AllenConstraint(new Atomic("(duration this1 (interval 10 10))")));
		oLoad1.addConstraint(new PrologConstraint(new Atomic("(size c1 3)"), Term.createConstant("prologProgram")));
		oLoad1.addConstraint(new PrologConstraint(new Atomic("(timeToLift c1 10)"), Term.createConstant("prologProgram")));
		
		Operator oLoad2 = new Operator();
		oLoad2.setName(new Atomic("(load r c2 l)"));
		oLoad2.addPrecondition(new Statement("(p1 (at r) l)"));
		oLoad2.addPrecondition(new Statement("(p2 (at c2) l)"));
		oLoad2.addEffect(new Statement("(e1 (at c2) r)"));
		oLoad2.addEffect(new Statement("(e2 (space r) 1)"));
		oLoad2.addConstraint(new AllenConstraint(new Atomic("(duration this2 (interval 20 20))")));
		oLoad2.addConstraint(new PrologConstraint(new Atomic("(size c2 1)"), Term.createConstant("prologProgram")));
		oLoad2.addConstraint(new PrologConstraint(new Atomic("(timeToLift c2 20)"), Term.createConstant("prologProgram")));
		
		Plan plan = new Plan();
		plan.addAction(oLoad1);
		plan.addAction(oLoad2);
				
		plan.liftTerm(Term.createConstant("r"));
		Set<Term> S = new HashSet<Term>();
		S.addAll(plan.getActions().get(0).getVariableTerms());
		S.addAll(plan.getActions().get(1).getVariableTerms());
		assertTrue(S.size() == 1);
		
		plan.liftTerm(Term.createConstant("c1"));
		S = new HashSet<Term>();
		S.addAll(plan.getActions().get(0).getVariableTerms());
		S.addAll(plan.getActions().get(1).getVariableTerms());
		assertTrue(S.size() == 2);

		plan.liftTerm(Term.createConstant("c2"));
		S = new HashSet<Term>();
		S.addAll(plan.getActions().get(0).getVariableTerms());
		S.addAll(plan.getActions().get(1).getVariableTerms());
		assertTrue(S.size() == 3);

		plan.liftTerm(Term.createConstant("l"));
		S = new HashSet<Term>();
		S.addAll(plan.getActions().get(0).getVariableTerms());
		S.addAll(plan.getActions().get(1).getVariableTerms());
		assertTrue(S.size() == 4);
	}
}
