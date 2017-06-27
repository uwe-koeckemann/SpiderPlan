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
import java.util.HashMap;
import java.util.List;

import org.spiderplan.causal.forwardPlanning.ForwardPlanningNode;
import org.spiderplan.causal.forwardPlanning.ForwardPlanningNodeComparator;
import org.spiderplan.causal.forwardPlanning.StateVariableOperatorMultiState;
import org.spiderplan.causal.forwardPlanning.ForwardPlanningNode.EqualityCriteria;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.search.MultiHeuristicNodeComparatorIndex;
import org.spiderplan.search.MultiHeuristicNodeComparatorLexicographic;


import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestForwardPlanning extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	 
	public void testNodeEquality() {
		ForwardPlanningNode n1 = new ForwardPlanningNode(1);
		ForwardPlanningNode n1pred = new ForwardPlanningNode(1);
		ForwardPlanningNode n2 = new ForwardPlanningNode(1);
		ForwardPlanningNode n2pred = new ForwardPlanningNode(1);
		
		
		StateVariableOperatorMultiState a1 = new StateVariableOperatorMultiState();
		a1.setName(Term.createConstant("action1"));
		StateVariableOperatorMultiState a2 = new StateVariableOperatorMultiState();
		a2.setName(Term.createConstant("action2"));
		
		
		Term var1 = Term.createConstant("p");
		Term val1 = Term.createConstant("x");
		List<Term> valList = new ArrayList<Term>();
		valList.add(val1);
		
		n1.s = new HashMap<Term, List<Term>>();
		n1.s.put(var1, valList);
		n1.a = a1;
		n1.prev = n1pred;
		n1pred.a = a2;
		
		n2.s = new HashMap<Term, List<Term>>();
		n2.s.put(var1, valList);
		n2.a = a1;
		n2.prev = n2pred;
		n2pred.a = a1;
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.PlanBased;
		
		assertFalse( n1.equals(n2) );
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.StateBased;
		
		assertTrue( n1.equals(n2) );
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.ResultingStateBased;
		
		assertTrue( n1.equals(n2) );
		
		n2.a = a2;
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.PlanBased;
		
		assertFalse( n1.equals(n2) );
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.StateBased;
		
		assertFalse( n1.equals(n2) );
		
		ForwardPlanningNode.eqCriteria = EqualityCriteria.ResultingStateBased;
		
		assertTrue( n1.equals(n2) );
	}
	
	public void testNodeComparison() {
		ForwardPlanningNode a = new ForwardPlanningNode(2);
		ForwardPlanningNode b = new ForwardPlanningNode(2);
		
		a.setHeuristicValue(0, 1);
		a.setHeuristicValue(1, 2);
		
		b.setHeuristicValue(0, 1);
		b.setHeuristicValue(1, 2);
		
		ForwardPlanningNodeComparator compLexicographic = new ForwardPlanningNodeComparator( new MultiHeuristicNodeComparatorLexicographic() );
		
		assertTrue( compLexicographic.compare(a,b) == 0 );
		
		a.setHeuristicValue(1, 1);
		
		assertTrue( compLexicographic.compare(a,b) == -1 );
		
		ForwardPlanningNodeComparator compIndex0 = new ForwardPlanningNodeComparator( new MultiHeuristicNodeComparatorIndex(0) );
				
		assertTrue( compIndex0.compare(a,b) == 0 );
		
		ForwardPlanningNodeComparator compIndex1 = new ForwardPlanningNodeComparator( new MultiHeuristicNodeComparatorIndex(1) );
				
		assertTrue( compIndex1.compare(a,b) == -1 );
		
		
	}
	
	public void testNodeComparisonWithGoals() {
		ForwardPlanningNode a = new ForwardPlanningNode(1);
		ForwardPlanningNode b = new ForwardPlanningNode(1);
		
		ForwardPlanningNodeComparator compLexicographic = new ForwardPlanningNodeComparator( new MultiHeuristicNodeComparatorLexicographic() );
				
		a.setHeuristicValue(0, 1);
		b.setHeuristicValue(0, 5);
		
		assertTrue( compLexicographic.compare(a,b) == -1 );			// a wins 
		
		Goal gA = new SingleGoal(Term.createConstant("sv"), Term.createConstant("val"));
		Goal gB = gA.copy();
		
		a.g.add(gA);
		b.g.add(gB);
		
		assertTrue( compLexicographic.compare(a,b) == -1 );			// a still wins
		
		gB.setReached(true);
		
		assertTrue( compLexicographic.compare(a,b) == 1 );			// b wins because ahead in solved goals and goals are the same
	}
	
	public void testGoals() {
		Goal g1 = new SingleGoal(Term.createConstant("sv1"), Term.createConstant("val"));
		Goal g2 = new SingleGoal(Term.createConstant("sv2"), Term.createConstant("val"));
		g2.addRequirement(g1);
		
		assertTrue( !g1.wasReached() );
		assertTrue( g1.requirementsSatisfied() );
		assertTrue( !g2.wasReached() );
		assertTrue( !g2.requirementsSatisfied() );
		
		g1.setReached(true);
		
		assertTrue( g1.wasReached() );
		assertTrue( g1.requirementsSatisfied() );
		assertTrue( !g2.wasReached() );
		assertTrue( g2.requirementsSatisfied() );
		
	}
}

