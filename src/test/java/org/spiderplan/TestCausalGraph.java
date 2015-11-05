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
import java.util.Map;
import java.util.Map.Entry;

import org.spiderplan.causal.forwardPlanning.CausalReasoningTools;
import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.causalGraph.CausalGraph;
import org.spiderplan.causal.forwardPlanning.causalGraph.CausalGraphHeuristic;
import org.spiderplan.causal.forwardPlanning.causalGraph.DomainTransitionEdge;
import org.spiderplan.causal.forwardPlanning.causalGraph.DomainTransitionGraph;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.SimpleParsing;


import junit.framework.TestCase;

public class TestCausalGraph extends TestCase {
	
	TypeManager tM;		
	
	
	@Override
	public void setUp() throws Exception {
		tM = new TypeManager();		
		tM.addSimpleEnumType("boolean", "true,false");		
	}

	@Override
	public void tearDown() throws Exception {
	
	}
	
	public void testDomainTransitionGraph() {		
		tM.addSimpleEnumType("locations", "a,b,c,d");
		
		tM.attachTypes("(drive locations locations)");
		tM.attachTypes("(at)=locations");
		tM.attachTypes("(good)=locations");
		
		StateVariableOperator o = new StateVariableOperator();
		o.setName(new Atomic("(drive ?A ?B)"));
		o.getPreconditions().put( new Atomic("(at)"), Term.createVariable("?A") );
		o.getEffects().put(new Atomic("(at)"), Term.createVariable("?B"));
		
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		O.add(o);
	
		DomainTransitionGraph dtg = new DomainTransitionGraph(new Atomic("(at)"), o.getAllGround(tM), tM);	
				
		assertTrue( dtg.getGraph().getVertexCount() == 4 );
		assertTrue( dtg.getGraph().getEdgeCount() == 12 );
		
		// no conditions attached to edges, since only condition is previous value 
		// (which is the source edge in graph)
		for ( DomainTransitionEdge e : dtg.getGraph().getEdges()) { 
			assertTrue( e.getConditions().size() == 0 );
		}		
	}
		
	public void testCost1() {
		
		Atomic v1 = new Atomic("(sv1)");
		Atomic v2 = new Atomic("(sv2)");
		

		
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.addSimpleEnumType("sv2_t", "c,d");
		
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(sv2)=sv2_t");
		
		tM.attachTypes("(changeSV1 sv1_t sv1_t)");
		tM.attachTypes("(changeSV2 sv2_t sv2_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(changeSV1 ?A ?B)"));
		o1.getPreconditions().put(new Atomic("(sv1)"), Term.createVariable("?A"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createVariable("?B"));
	
		StateVariableOperator o2 = new StateVariableOperator();
		o2.setName(new Atomic("(changeSV2 ?A ?B)"));
		o2.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("b"));
		o2.getPreconditions().put(new Atomic("(sv2)"), Term.createVariable("?A"));
		o2.getEffects().put(new Atomic("(sv2)"), Term.createVariable("?B"));
		
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		A.addAll( o2.getAllGround(tM));
		
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		DomainTransitionGraph dtgSV2 = new DomainTransitionGraph(v2, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		DTGs.put(v2, dtgSV2);
		
		CausalGraph cg = new CausalGraph(A);
		
//		ComputeCost computeCost = new ComputeCost(DTGs, cg, tM);
		
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
		s.put(v1, Term.createConstant("a"));
		s.put(v2, Term.createConstant("c"));
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
//		fdh.initializeHeuristic(s, g, A, tM);
						
//		fdh.computeCost(s, v2, Term.Constant("c"));
//		fdh.computeCost(s, v2, Term.Constant("d"));
		
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("d")) == 2 );
		
		// staying is free:
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("a"), Term.createConstant("a")) == 0 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("b"), Term.createConstant("b")) == 0 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("c")) == 0 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("d"), Term.createConstant("d")) == 0 );
				
		// change without causal graph predecessor (just transition cost of 1)
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("b")) == 1 );
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("b"), Term.createConstant("a")) == 1 );
				
		// requires (sv1) to change as well: cost 2
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("d")) == 2 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("d"), Term.createConstant("c")) == 2 );
		
//		ArrayList<StateVariableOperator> helpful = new ArrayList<StateVariableOperator>();
//		helpful.addAll(fdh.helpfulTransisions(s, v2, Term.Constant("c"), Term.Constant("d")));
//		assertTrue( helpful.size() == 1 );
//		assertTrue( helpful.get(0).getName().toString().equals("changeSV1(a,b)") );		
	}
	
	public void testCost2() {	
		Atomic v1 = new Atomic("(sv1)");
		Atomic v2 = new Atomic("(sv2)");
		Atomic v3 = new Atomic("(sv3)");
			
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.addSimpleEnumType("sv2_t", "c,d");
		tM.addSimpleEnumType("sv3_t", "e,f");
		
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(sv2)=sv2_t");
		tM.attachTypes("(sv3)=sv3_t");
		
		tM.attachTypes("(changeSV1 sv1_t sv1_t)");
		tM.attachTypes("(changeSV2 sv2_t sv2_t)");
		tM.attachTypes("(changeSV3 sv3_t sv3_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(changeSV1 ?A ?B)"));
		o1.getPreconditions().put(new Atomic("(sv1)"), Term.createVariable("?A"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createVariable("?B"));
	
		StateVariableOperator o2 = new StateVariableOperator();
		o2.setName(new Atomic("(changeSV2 ?A ?B)"));
		o2.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("b"));
		o2.getPreconditions().put(new Atomic("(sv2)"), Term.createVariable("?A"));
		o2.getEffects().put(new Atomic("(sv2)"), Term.createVariable("?B"));
		
		StateVariableOperator o3 = new StateVariableOperator();
		o3.setName(new Atomic("(changeSV3 ?A ?B)"));
		o3.getPreconditions().put(new Atomic("(sv2)"), Term.createConstant("d"));
		o3.getPreconditions().put(new Atomic("(sv3)"), Term.createVariable("?A"));
		o3.getEffects().put(new Atomic("(sv3)"), Term.createVariable("?B"));
		
		
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		A.addAll( o2.getAllGround(tM));
		A.addAll( o3.getAllGround(tM));
				
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		DomainTransitionGraph dtgSV2 = new DomainTransitionGraph(v2, A, tM);
		DomainTransitionGraph dtgSV3 = new DomainTransitionGraph(v3, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		DTGs.put(v2, dtgSV2);
		DTGs.put(v3, dtgSV3);
			
		CausalGraph cg = new CausalGraph(A);
			
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
		s.put(v1, Term.createConstant("a"));
		s.put(v2, Term.createConstant("c"));
		s.put(v3, Term.createConstant("e"));
		
//		fdh.computeCost(s, v3, Term.Constant("e"));
//		fdh.computeCost(s, v3, Term.Constant("f"));
				
		// staying is free:
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("a")) == 0 );
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("b"), Term.createConstant("b")) == 0 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("c")) == 0 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("d"), Term.createConstant("d")) == 0 );
		assertTrue( fdh.computeCost(s, v3, Term.createConstant("e"), Term.createConstant("e")) == 0 );
		assertTrue( fdh.computeCost(s, v3, Term.createConstant("f"), Term.createConstant("f")) == 0 );
				
		// change without causal graph predecessor (just transition cost of 1)
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("b")) == 1 );
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("b"), Term.createConstant("a")) == 1 );
				
		// requires (sv1) to change as well: cost 2
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("d")) == 2 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("d"), Term.createConstant("c")) == 2 );
		
		// requires to change (sv1) and (sv2): cost 3
		assertTrue( fdh.computeCost(s, v3, Term.createConstant("e"), Term.createConstant("f")) == 3 );
		
//		ArrayList<StateVariableOperator> helpful = new ArrayList<StateVariableOperator>();
//		helpful.addAll(computeCost.helpfulTransisions(s, v2, Term.Constant("c"), Term.Constant("d")));
//		
//		assertTrue( helpful.size() == 1 );
//		assertTrue( helpful.get(0).getName().toString().equals("changeSV1(a,b)") );	
		
		
	}
	
	public void testCost3() {
		Atomic v1 = new Atomic("(sv1)");
		Atomic v2 = new Atomic("(sv2)");
		Atomic v3 = new Atomic("(sv3)");
			
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.addSimpleEnumType("sv2_t", "c,d");
		tM.addSimpleEnumType("sv3_t", "e,f");
		
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(sv2)=sv2_t");
		tM.attachTypes("(sv3)=sv3_t");
		
		tM.attachTypes("(changeSV1 sv1_t sv1_t)");
		tM.attachTypes("(changeSV2 sv2_t sv2_t)");
		tM.attachTypes("(changeSV3 sv3_t sv3_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(changeSV1 ?A ?B)"));
		o1.getPreconditions().put(new Atomic("(sv1)"), Term.createVariable("?A"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createVariable("?B"));
	
		StateVariableOperator o2 = new StateVariableOperator();
		o2.setName(new Atomic("(changeSV2 ?A ?B)"));
		o2.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("b"));
		o2.getPreconditions().put(new Atomic("(sv2)"), Term.createVariable("?A"));
		o2.getEffects().put(new Atomic("(sv2)"), Term.createVariable("?B"));
		
		StateVariableOperator o3 = new StateVariableOperator();
		o3.setName(new Atomic("(changeSV3 ?A ?B)"));
		o3.getPreconditions().put(new Atomic("(sv2)"), Term.createConstant("d"));
		o3.getPreconditions().put(new Atomic("(sv3)"), Term.createVariable("?A"));
		o3.getEffects().put(new Atomic("(sv3)"), Term.createVariable("?B"));
		
		
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		A.addAll( o2.getAllGround(tM));
		A.addAll( o3.getAllGround(tM));
				
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		DomainTransitionGraph dtgSV2 = new DomainTransitionGraph(v2, A, tM);
		DomainTransitionGraph dtgSV3 = new DomainTransitionGraph(v3, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		DTGs.put(v2, dtgSV2);
		DTGs.put(v3, dtgSV3);
				
		CausalGraph cg = new CausalGraph(A);
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
		s.put(v1, Term.createConstant("b"));
		s.put(v2, Term.createConstant("c"));
		s.put(v3, Term.createConstant("e"));

//		computeCost.computeCost(s, v3, Term.Constant("e"));
//		computeCost.computeCost(s, v3, Term.Constant("f"));
				
		// change without causal graph predecessor (just transition cost of 1)
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("b"), Term.createConstant("a")) == 1 );
				
		// requires (sv1)=d, but this is already in s, so cost is 1
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("c"), Term.createConstant("d")) == 1 );
		assertTrue( fdh.computeCost(s, v2, Term.createConstant("d"), Term.createConstant("c")) == 1 );
		
		// requires to change (sv1) and (sv2) but (sv1) is already good: cost 2
		assertTrue( fdh.computeCost(s, v3, Term.createConstant("e"), Term.createConstant("f")) == 2 );
		
//		ArrayList<StateVariableOperator> helpful = new ArrayList<StateVariableOperator>();
//		helpful.addAll(computeCost.helpfulTransisions(s, v2, Term.Constant("c"), Term.Constant("d")));
//		
//		assertTrue( helpful.size() == 1 );
//		assertTrue( helpful.get(0).getName().toString().equals("changeSV2(c,d)") );	
	}
	
	public void testCost4() {
		Atomic v1 = new Atomic("(sv1)");
	
		tM.addSimpleEnumType("sv1_t", "a,b,c,d");
	
		tM.attachTypes("(sv1)=sv1_t");
		
		tM.attachTypes("(change1 sv1_t sv1_t)");
		tM.attachTypes("(change2 sv1_t sv1_t)");
		tM.attachTypes("(change3 sv1_t sv1_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(change1 a b)"));
		o1.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("a"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createConstant("b"));
	
		StateVariableOperator o2 = new StateVariableOperator();
		o2.setName(new Atomic("(change2 b c)"));
		o2.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("b"));
		o2.getEffects().put(new Atomic("(sv1)"), Term.createConstant("c"));
		
		StateVariableOperator o3 = new StateVariableOperator();
		o3.setName(new Atomic("(change3 c d)"));
		o3.getPreconditions().put(new Atomic("(sv1)"), Term.createConstant("c"));
		o3.getEffects().put(new Atomic("(sv1)"), Term.createConstant("d"));

		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		A.addAll( o2.getAllGround(tM));
		A.addAll( o3.getAllGround(tM));
		
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		
		CausalGraph cg = new CausalGraph(A);
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
		s.put(v1, Term.createConstant("a"));
		
//		computeCost.computeCost(s, v1, Term.Constant("a"));
		
//		ArrayList<Term> shortestPath = fdh.shortestPath(v1, Term.Constant("a"), Term.Constant("d"));
//		
//		assertTrue( shortestPath.get(0).equals(Term.Constant("a")));
//		assertTrue( shortestPath.get(1).equals(Term.Constant("b")));
//		assertTrue( shortestPath.get(2).equals(Term.Constant("c")));
//		assertTrue( shortestPath.get(3).equals(Term.Constant("d")));
		
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("b")) == 1 );
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("c")) == 2 );
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("d")) == 3 );

//		ArrayList<StateVariableOperator> helpful = new ArrayList<StateVariableOperator>();
//		helpful.addAll(computeCost.helpfulTransisions(s, v1, Term.Constant("a"), Term.Constant("d")));
//		
//		assertTrue( helpful.size() == 1 );
//		assertTrue( helpful.get(0).getName().toString().equals("(change1 a b)") );	
	}
	
	
	public void testCostNoPreviousValue() {
		Atomic v1 = new Atomic("(sv1)");
		
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(setSV1 sv1_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(setSV1 ?X)"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createVariable("?X"));
	
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		
		CausalGraph cg = new CausalGraph(A);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
//		s.put(v1, Term.Constant("a"));
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
								
		assertTrue( fdh.computeCost(s, v1, Term.createConstant("a"), Term.createConstant("b")) == 1 );
	}
	
	public void testCostUnknownValue() {
		Atomic v1 = new Atomic("(sv1)");
			
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(setSV1 sv1_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(setSV1 ?X)"));
		o1.getEffects().put(new Atomic("sv1"), Term.createVariable("?X"));
	
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
		
		CausalGraph cg = new CausalGraph(A);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
//		s.put(v1, Term.Constant("a"));
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);

		assertTrue( fdh.computeCost(s, v1, Term.createConstant("unknown_value"), Term.createConstant("b")) == 1 );
	}
	
	/**
	 * Caused a bug where the cost of reaching some dependencies became 0
	 * where it should be 1. 
	 * 
	 * The cause was setting local_state_d_prime to unknownValue for all variables, 
	 * even when they were not a predecessor in the causal graph.
	 * 
	 * Fixed by creating local_state_d with unknownValue and 
	 * ignore everything that is not added to local_state_d  in this way
	 * (i.e. not a predecessor in the causal graph).  
	 * 
	 * @throws UnknownThing
	 */
	public void testCostUnknownValueBug() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("match", "match0,match1,match2");
		tM.addSimpleEnumType("fuse", "fuse0,fuse1,fuse2");
		tM.addSimpleEnumType("boolean", "true,false");	
		
		tM.attachTypes("(LightMatch match)");
		tM.attachTypes("(MendFuse fuse match)");
		
		tM.attachTypes("(unused match)=boolean");
		tM.attachTypes("(mended fuse)=boolean");
		tM.attachTypes("(light match)=boolean");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(LightMatch ?M)"));
		o1.getPreconditions().putAll(SimpleParsing.createMap("(unused ?M)<-true"));
		o1.getEffects().putAll(SimpleParsing.createMap("(unused ?M)<-false;(light ?M)<-true"));
	
		StateVariableOperator o2 = new StateVariableOperator();
		o2.setName(new Atomic("(MendFuse ?F ?M)"));
		o2.getPreconditions().put(new Atomic("(light ?M)"), Term.createConstant("true"));
		o2.getEffects().put(new Atomic("(mended ?F)"), Term.createConstant("true"));
		
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		A.addAll( o2.getAllGround(tM));
			
		HashMap<Atomic,Term> s0 = new HashMap<Atomic,Term>();
		s0.put(new Atomic("(unused match0)"), Term.createConstant("true"));
		s0.put(new Atomic("(unused match1)"), Term.createConstant("true"));
		s0.put(new Atomic("(unused match2)"), Term.createConstant("true"));
		
		ArrayList<Goal> g = new ArrayList<Goal>();
		g.add(new SingleGoal(new Atomic("(mended fuse0)"), Term.createConstant("true")));
		g.add(new SingleGoal(new Atomic("(mended fuse1)"), Term.createConstant("true")));
		g.add(new SingleGoal(new Atomic("(mended fuse2)"), Term.createConstant("true")));
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		fdh.initializeHeuristic( g, A, tM);
					
		assertTrue( fdh.calculateHeuristicValue(s0, g, new CommonDataStructures()) == 6 );
	}
	
	
	public void testCostNoPreviousValueUsingGoalAndState() {	
		Atomic v1 = new Atomic("(sv1)");
				
		tM.addSimpleEnumType("sv1_t", "a,b");
		tM.attachTypes("(sv1)=sv1_t");
		tM.attachTypes("(setSV1 sv1_t)");
		
		StateVariableOperator o1 = new StateVariableOperator();
		o1.setName(new Atomic("(setSV1 ?X)"));
		o1.getEffects().put(new Atomic("(sv1)"), Term.createVariable("?X"));
	
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		
		A.addAll( o1.getAllGround(tM));
		
		DomainTransitionGraph dtgSV1 = new DomainTransitionGraph(v1, A, tM);
		
		HashMap<Atomic,DomainTransitionGraph> DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		DTGs.put(v1, dtgSV1);
			
		CausalGraph cg = new CausalGraph(A);
		
		HashMap<Atomic,Term> s = new HashMap<Atomic,Term>();
		
		ArrayList<Goal> g = new ArrayList<Goal>();
		g.add(new SingleGoal(v1, Term.createConstant("b")));
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic(DTGs, cg, tM);
					
		assertTrue( fdh.calculateHeuristicValue(s, g, new CommonDataStructures()) == 1 );
	}

	public void testElevatorDomain() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("elevator", "e1");
		tM.addSimpleEnumType("person", "p1,p2");
		tM.addSimpleEnumType("floor", "f0,f1,f2,f3,f4,f5,f6");
		tM.addSimpleEnumType("boolean", "true,false");	
		tM.addSimpleEnumType("location", "floor,elevator");
		
		tM.attachTypes("(Move elevator floor floor)");
		tM.attachTypes("(Board person elevator floor)");
		tM.attachTypes("(Leave person elevator floor)");
		
		tM.attachTypes("(liftAt elevator)=floor");
		tM.attachTypes("(passengerAt person)=location");
		tM.attachTypes("(reachable floor elevator)=boolean");
		
		tM.updateTypeDomains();
			
		StateVariableOperator o1 = SimpleParsing.createSVO("(Move ?E ?F1 ?F2)" +
									"<p>(liftAt ?E)<-?F1;(reachable ?F2 ?E)<-true" +
									"<e>(liftAt ?E)<-?F2");
	
		StateVariableOperator o2 =  SimpleParsing.createSVO("(Board ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?F" +
									"<e>(passengerAt ?P)<-?E");
		
		StateVariableOperator o3 = SimpleParsing.createSVO("(Leave ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?E" +
									"<e>(passengerAt ?P)<-?F");
	
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		
		O.add( o1 );
		O.add( o2 );
		O.add( o3 );
		
		
		Map<Atomic,Term> s0 = SimpleParsing.createMap(
				"(passengerAt p1)<-f4;" +
				"(passengerAt p2)<-f4;" +
				"(liftAt e1)<-f0;" +
				"(reachable f0 e1)<-true;" +
				"(reachable f1 e1)<-true;" +
				"(reachable f2 e1)<-true;" +
				"(reachable f3 e1)<-true;" +
				"(reachable f4 e1)<-true");


		Map<Atomic,Term> gParsed = SimpleParsing.createMap(
				"(passengerAt p1)<-f0;" +
				"(passengerAt p2)<-f0");
		
		ArrayList<Goal> g = new ArrayList<Goal>();
		for ( Entry<Atomic,Term> goal : gParsed.entrySet() ) {
			g.add( new SingleGoal( goal.getKey(), goal.getValue() ));
		}
		
		Collection<StateVariableOperator> A = CausalReasoningTools.getAllSVOActions(s0, O, tM);
				
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		fdh.initializeHeuristic( g, A, tM);
			
		assertTrue( fdh.calculateHeuristicValue(s0, g, new CommonDataStructures()) == 8 );
	}
	
	
	public void testElevatorDomainWithDoubleTransport() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("elevator", "slow0,slow1,slow2");
		tM.addSimpleEnumType("person", "p1");
		tM.addSimpleEnumType("floor", "f0,f1,f2,f3,f4,f5,f6");
		tM.addSimpleEnumType("boolean", "true,false");	
		tM.addSimpleEnumType("location", "floor,elevator");
		
		tM.attachTypes("(Move elevator floor floor)");
		tM.attachTypes("(Board person elevator floor)");
		tM.attachTypes("(Leave person elevator floor)");
		
		tM.attachTypes("(liftAt elevator)=floor");
		tM.attachTypes("(passengerAt person)=location");
		tM.attachTypes("(reachable floor elevator)=boolean");
		
		tM.updateTypeDomains();
			
		StateVariableOperator o1 = SimpleParsing.createSVO("(Move ?E ?F1 ?F2)" +
									"<p>(liftAt ?E)<-?F1;(reachable ?F2 ?E)<-true" +
									"<e>(liftAt ?E)<-?F2");
	
		StateVariableOperator o2 =  SimpleParsing.createSVO("(Board ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?F" +
									"<e>(passengerAt ?P)<-?E");
		
		StateVariableOperator o3 = SimpleParsing.createSVO("(Leave ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?E" +
									"<e>(passengerAt ?P)<-?F");
	
		Map<Atomic,Term> s0 = SimpleParsing.createMap(
				"(passengerAt p1)<-f4;" +
				"(liftAt slow0)<-f0;" +
				"(liftAt slow1)<-f3;" +
				"(reachable f0 slow0)<-true;" +
				"(reachable f1 slow0)<-true;" +
				"(reachable f2 slow0)<-true;" +
				"(reachable f2 slow1)<-true;" +
				"(reachable f3 slow1)<-true;" +
				"(reachable f4 slow1)<-true");
					
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		
		O.add(o1);
		O.add(o2);
		O.add(o3);
		
		Collection<StateVariableOperator> A = CausalReasoningTools.getAllSVOActions(s0, O, tM);
	
		Map<Atomic,Term> gParsed = SimpleParsing.createMap(
				"(passengerAt p1)<-f0");
							
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		ArrayList<Goal> g = new ArrayList<Goal>();
		for ( Entry<Atomic,Term> goal : gParsed.entrySet() ) {
			g.add( new SingleGoal( goal.getKey(), goal.getValue() ));
		}
		
		
		fdh.initializeHeuristic( g, A, tM);
			
		assertTrue( fdh.calculateHeuristicValue(s0, g, new CommonDataStructures()) == 8 );
	}
	
	public void testElevatorDomainWithDoubleTransportAndFast() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("elevator", "slow0,slow1,slow2,fast0,fast1");
		tM.addSimpleEnumType("person", "p1");
		tM.addSimpleEnumType("floor", "f0,f1,f2,f3,f4,f5,f6");
		tM.addSimpleEnumType("boolean", "true,false");	
		tM.addSimpleEnumType("location", "floor,elevator");
		
		tM.attachTypes("(Move elevator floor floor)");
		tM.attachTypes("(Board person elevator floor)");
		tM.attachTypes("(Leave person elevator floor)");
		
		tM.attachTypes("(liftAt elevator)=floor");
		tM.attachTypes("(passengerAt person)=location");
		tM.attachTypes("(reachable floor elevator)=boolean");
		
		tM.updateTypeDomains();
			
		StateVariableOperator o1 = SimpleParsing.createSVO("(Move ?E ?F1 ?F2)" +
									"<p>(liftAt ?E)<-?F1;(reachable ?F2 ?E)<-true" +
									"<e>(liftAt ?E)<-?F2");
	
		StateVariableOperator o2 =  SimpleParsing.createSVO("(Board ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?F" +
									"<e>(passengerAt ?P)<-?E");
		
		StateVariableOperator o3 = SimpleParsing.createSVO("(Leave ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?E" +
									"<e>(passengerAt ?P)<-?F");
	
		Map<Atomic,Term> s0 = SimpleParsing.createMap(
				"(passengerAt p1)<-f4;" +
				"(liftAt slow0)<-f0;" +
				"(liftAt slow1)<-f3;" +
				"(liftAt fast0)<-f0;" +
				"(reachable f0 fast0)<-true;" +
				"(reachable f4 fast0)<-true;" +
				"(reachable f0 slow0)<-true;" +
				"(reachable f1 slow0)<-true;" +
				"(reachable f2 slow0)<-true;" +
				"(reachable f2 slow1)<-true;" +
				"(reachable f3 slow1)<-true;" +
				"(reachable f4 slow1)<-true");
					
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		
		O.add(o1);
		O.add(o2);
		O.add(o3);
		
		Collection<StateVariableOperator> A = CausalReasoningTools.getAllSVOActions(s0, O, tM);
	
		Map<Atomic,Term> gParsed = SimpleParsing.createMap(
				"(passengerAt p1)<-f0");
							
		ArrayList<Goal> g = new ArrayList<Goal>();
		for ( Entry<Atomic,Term> goal : gParsed.entrySet() ) {
			g.add( new SingleGoal( goal.getKey(), goal.getValue() ));
		}
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		fdh.initializeHeuristic( g, A, tM);
		
		assertTrue( fdh.calculateHeuristicValue(s0,g, new CommonDataStructures()) == 4 );
	}
	
	public void testElevatorDomainWithTripleNormalDoubleFastTransport() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("elevator", "slow0,slow1,slow2,fast0");
		tM.addSimpleEnumType("person", "p1");
		tM.addSimpleEnumType("floor", "f0,f1,f2,f3,f4,f5,f6");
		tM.addSimpleEnumType("boolean", "true,false");	
		tM.addSimpleEnumType("location", "floor,elevator");
		
		tM.attachTypes("(Move elevator floor floor)");
		tM.attachTypes("(Board person elevator floor)");
		tM.attachTypes("(Leave person elevator floor)");
		
		tM.attachTypes("(liftAt elevator)=floor");
		tM.attachTypes("(passengerAt person)=location");
		tM.attachTypes("(reachable floor elevator)=boolean");
		
		tM.updateTypeDomains();
			
		StateVariableOperator o1 = SimpleParsing.createSVO("(Move ?E ?F1 ?F2)" +
									"<p>(liftAt ?E)<-?F1;(reachable ?F2 ?E)<-true" +
									"<e>(liftAt ?E)<-?F2");
	
		StateVariableOperator o2 =  SimpleParsing.createSVO("(Board ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?F" +
									"<e>(passengerAt ?P)<-?E");
		
		StateVariableOperator o3 = SimpleParsing.createSVO("(Leave ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?E" +
									"<e>(passengerAt ?P)<-?F");
	
		Map<Atomic,Term> s0 = SimpleParsing.createMap(
				"(passengerAt p1)<-f5;" +
				"(liftAt slow0)<-f0;" +
				"(liftAt slow1)<-f2;" +
				"(liftAt slow2)<-f4;" +
				"(liftAt fast0)<-f0;" +
				"(reachable f0 fast0)<-true;" +
				"(reachable f6 fast0)<-true;" +
				"(reachable f0 slow0)<-true;" +
				"(reachable f1 slow0)<-true;" +
				"(reachable f2 slow0)<-true;" +
				"(reachable f2 slow1)<-true;" +
				"(reachable f3 slow1)<-true;" +
				"(reachable f4 slow1)<-true;" +
				"(reachable f4 slow2)<-true;" +
				"(reachable f5 slow2)<-true;" +
				"(reachable f6 slow2)<-true");
							
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		
		O.add(o1);
		O.add(o2);
		O.add(o3);
		
		Collection<StateVariableOperator> A = CausalReasoningTools.getAllSVOActions(s0, O, tM);
	
		Map<Atomic,Term> gParsed = SimpleParsing.createMap("(passengerAt p1)<-f0");
							
		ArrayList<Goal> g = new ArrayList<Goal>();
		for ( Entry<Atomic,Term> goal : gParsed.entrySet() ) {
			g.add( new SingleGoal( goal.getKey(), goal.getValue() ));
		}
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		fdh.initializeHeuristic( g, A, tM);
		
		assertTrue( fdh.calculateHeuristicValue(s0,g, new CommonDataStructures()) == 8 );
	}
	
	public void testElevatorDomainWithTripleNormalTransport() {
		TypeManager tM = new TypeManager();
		
		tM.addSimpleEnumType("elevator", "slow0,slow1,slow2");
		tM.addSimpleEnumType("person", "p1");
		tM.addSimpleEnumType("floor", "f0,f1,f2,f3,f4,f5,f6");
		tM.addSimpleEnumType("boolean", "true,false");	
		tM.addSimpleEnumType("location", "floor,elevator");
		
		tM.attachTypes("(Move elevator floor floor)");
		tM.attachTypes("(Board person elevator floor)");
		tM.attachTypes("(Leave person elevator floor)");
		
		tM.attachTypes("(liftAt elevator)=floor");
		tM.attachTypes("(passengerAt person)=location");
		tM.attachTypes("(reachable floor elevator)=boolean");
		
		tM.updateTypeDomains();
			
		StateVariableOperator o1 = SimpleParsing.createSVO("(Move ?E ?F1 ?F2)" +
									"<p>(liftAt ?E)<-?F1;(reachable ?F2 ?E)<-true" +
									"<e>(liftAt ?E)<-?F2");
	
		StateVariableOperator o2 =  SimpleParsing.createSVO("(Board ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?F" +
									"<e>(passengerAt ?P)<-?E");
		
		StateVariableOperator o3 = SimpleParsing.createSVO("(Leave ?P ?E ?F)" +
									"<p>(liftAt ?E)<-?F;(passengerAt ?P)<-?E" +
									"<e>(passengerAt ?P)<-?F");
	
		Map<Atomic,Term> s0 = SimpleParsing.createMap(
				"(passengerAt p1)<-f5;" +
				"(liftAt slow0)<-f0;" +
				"(liftAt slow1)<-f2;" +
				"(liftAt slow2)<-f4;" +
				"(reachable f0 slow0)<-true;" +
				"(reachable f1 slow0)<-true;" +
				"(reachable f2 slow0)<-true;" +
				"(reachable f2 slow1)<-true;" +
				"(reachable f3 slow1)<-true;" +
				"(reachable f4 slow1)<-true;" +
				"(reachable f4 slow2)<-true;" +
				"(reachable f5 slow2)<-true;" +
				"(reachable f6 slow2)<-true");
							
		ArrayList<StateVariableOperator> O = new ArrayList<StateVariableOperator>();
		
		O.add(o1);
		O.add(o2);
		O.add(o3);
		
		Collection<StateVariableOperator> A = CausalReasoningTools.getAllSVOActions(s0, O, tM);
			
		Map<Atomic,Term> gParsed = SimpleParsing.createMap(
				"(passengerAt p1)<-f0");
		ArrayList<Goal> g = new ArrayList<Goal>();
		for ( Entry<Atomic,Term> goal : gParsed.entrySet() ) {
			g.add( new SingleGoal( goal.getKey(), goal.getValue() ));
		}
		
		
		CausalGraphHeuristic fdh = new CausalGraphHeuristic();
		
		fdh.initializeHeuristic( g, A, tM);
				
		assertTrue( fdh.calculateHeuristicValue(s0, g, new CommonDataStructures()) == 12 );
	}
}
