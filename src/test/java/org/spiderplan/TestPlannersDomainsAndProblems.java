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
//package org.spiderplan;
//
//import java.util.logging.Level;
//
//import org.metacsp.utility.logging.MetaCSPLogging;
//
//import junit.framework.TestCase;
//import org.spiderplan.tools.logging.Logger;
//import org.spiderplan.modules.configuration.ConfigurationManager;
//import org.spiderplan.modules.solvers.Core;
//import org.spiderplan.modules.solvers.Core.State;
//import org.spiderplan.representation.ConstraintDatabase;
//import org.spiderplan.representation.parser.Compile;
//import org.spiderplan.representation.parser.Compile.DomainVersion;
//import org.spiderplan.representation.parser.pddl.ParseException;
//import org.spiderplan.runnable.RunPlanner;
//import org.spiderplan.temporal.metaCSP.MetaCSPAdapter;
//import org.spiderplan.temporal.metaCSP.MetaCSPAdapterWithHistory;
//import org.spiderplan.tools.Global;
//import org.spiderplan.tools.Loop;
//import org.spiderplan.tools.stopWatch.StopWatch;
//
//public class TestPlannersDomainsAndProblems extends TestCase {
//	
//	@Override
//	public void setUp() throws Exception {
//		MetaCSPLogging.setLevel( Level.OFF );
//		Logger.reset();
//	}
//
//	@Override
//	public void tearDown() throws Exception {
//	}
//	
//	/**
//	 * IPC-MATCH-CELLAR
//	 */
//	public void testIPC_MatchCellar_CreateNewOperators() { 
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/experiment/p01.uddl";
//		String planner = "./data/domains/ipc-match-cellar/planners/CreateNewOperators.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//		
//	public void testIPC_MatchCellar_CausalGraph() { 
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/experiment/p01.uddl";
//		String planner = "./data/domains/ipc-match-cellar/planners/CausalGraph.spider";
//		
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_MatchCellar_LiftedPruning() { 
//		String domain = "./data/domains/ipc-match-cellar/domain.uddl";
//		String problem = "./data/domains/ipc-match-cellar/experiment/p01.uddl";
//		String planner = "./data/domains/ipc-match-cellar/planners/LiftedPruning.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	/**
//	 * IPC-ELEVATORS-RESOURCES
//	 */
//	public void testIPC_ElevatorsResources_FirstTest_CausalGraph() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/domains/ipc-elevators-resources/planners/CausalGraph.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_ElevatorsResources_FirstTest_CreateNewOperators() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/planners/CreateNewOperators.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_ElevatorsResources_FirstTest_MultiQueueCG_FF_() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/planners/MultiQueueCG-FF.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_ElevatorsResources_FirstTest_LiftedPruning() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/firstTest.uddl";
//		String planner = "./data/domains/ipc-elevators-resources/planners/LiftedPruning.uddl";
//
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//			
//	public void testIPC_ElevatorsResources_DoubleTransportNeeded_CausalGraph() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/doubleTransportNeeded.uddl";
//		String planner = "./data/planners/CausalGraph.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_ElevatorsResources_DoubleTransportNeeded_MultiQueueCG_FF() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/doubleTransportNeeded.uddl";
//		String planner = "./data/planners/MultiQueueCG-FF.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testIPC_ElevatorsResources_DoubleTransportNeeded_LiftedPruning() { 
//		String domain = "./data/domains/ipc-elevators-resources/domain.uddl";
//		String problem = "./data/domains/ipc-elevators-resources/test-cases/doubleTransportNeeded.uddl";
//		String planner = "./data/planners/LiftedPruning.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	/**
//	 * IKEPS-PETROBAS
//	 */
//	public void testICKEPS_Pretrobras_CausalGraph() { 
//		String domain = "./data/domains/ickeps-petrobras/domain-v3.uddl";
//		String problem = "./data/domains/ickeps-petrobras/test-cases/test01.uddl";
//		String planner = "./data/domains/ickeps-petrobras/planners/CausalGraph.spider";
//
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//		
//	public void testICKEPS_Pretrobras_MultiQueueCG_FF() { 
//		String domain = "./data/domains/ickeps-petrobras/domain.uddl";
//		String problem = "./data/domains/ickeps-petrobras/test-cases/firstTest.uddl";;
//		String planner = "./data/planners/MultiQueueCG-FF.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testICKEPS_Pretrobras_LiftedPruning() { 
//		String domain = "./data/domains/ickeps-petrobras/domain.uddl";
//		String problem = "./data/domains/ickeps-petrobras/test-cases/firstTest.uddl";
//		String planner = "./data/planners/LiftedPruning.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testHandlingFutureEvents_test01() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test01-open_door.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//		
//	}
//	
//	public void testHandlingFutureEvents_test02() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test02-door-will-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testHandlingFutureEvents_test03() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test03-door-will-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testHandlingFutureEvents_test04() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test04-door-will-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//	public void testHandlingFutureEvents_test05() {
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test05-door-will-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	
//
//	public void testHandlingFutureEvents_test07() {
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test07-door-will-open.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test08() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test08-two-doors-close.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test09() {
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test09-door-closes-with-alternative.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test10() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test10-door-closes-with-alternative.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test11() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test11-kidnapped-robot.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test12() {
//		Logger.stop = true;
//		MetaCSPLogging.setLevel(Level.OFF);		
//		
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test12-ic-new-goal.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testHandlingFutureEvents_test13() {
//		String domain = "./data/domains/handling-future-events/domain.uddl";
//		String problem = "./data/domains/handling-future-events/test-cases/test13-ic-new-goal.uddl";
//		String planner = "./data/domains/handling-future-events/planners/SocialConstraints.uddl";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//
//	public void testAAAI_01() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test01.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();	
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_02() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test02.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_03() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test03.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_04() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test04.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_05() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test05.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_06() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test06.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//	public void testAAAI_07() {
//		String domain = "./data/domains/aaai-domain/domain-all.uddl";
//		String problem = "./data/domains/aaai-domain/test-cases/test07.uddl";
//		String planner = "./data/domains/aaai-domain/planners/BackjumpingBinSearch.spider";
//		
//		Core result = RunPlanner.run(domain, problem, planner, false, false);
//		ConstraintDatabase res = result.getContext();
//		assertTrue( result.getResultingState("main").equals(Core.State.Consistent) );
//		MetaCSPAdapter csp = new MetaCSPAdapter(Global.MaxTemporalHorizon);
//		assertTrue(csp.isConsistent(res,result.getTypeManager()));
//	}
//}
//
