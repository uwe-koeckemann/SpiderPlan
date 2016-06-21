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
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.StateVariableOverride;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestFutureEvents extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testSimpleConflict() {
		
		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(new Statement("(s0 p a)"));
		cDB.add(new Statement("(s1 p b)"));
		cDB.add(new Statement("(s2 p c)"));
		
		cDB.add( new AllenConstraint("s0 Before s1 [1,inf]"));
		cDB.add( new AllenConstraint("s1 Before s2 [1,inf]"));
		
		cDB.add( new PlanningInterval(Term.parse("[0 10000]")));
		
		/** Setup Modules */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("findConflicts","class","FindConflictsWithFutureEvents");
		
		Module futureEventsChecker = ModuleFactory.initModule("findConflicts",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( cDB );
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c");
		tM.attachTypes("p=t");
		testCore.setTypeManager(tM);
		
		testCore = futureEventsChecker.run(testCore);
				
		assertTrue( testCore.getContext().size() == 6 );
		
		ConstraintDatabase cDB2 = new ConstraintDatabase();
		
		cDB2.add(new Statement("(s0 p a)"));
		cDB2.add(new Statement("(s1 p b)"));
		cDB2.add(new Statement("(s2 p c)"));
		
		cDB2.add( new AllenConstraint("s0 Overlaps s1 [1,inf]"));
		cDB2.add( new AllenConstraint("s1 Before s2 [1,inf]"));
		
		cDB2.add( new PlanningInterval(Term.parse("[0 10000]")));
		
		testCore.setContext(cDB2);
		
		testCore = futureEventsChecker.run(testCore);
		
	}	
	
	public void testSimpleConflictWithPlan() {
		/** Setup Modules */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("findConflicts","class","FindConflictsWithFutureEvents");
		
		Module futureEventsChecker = ModuleFactory.initModule("findConflicts",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
	
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d");
		tM.attachTypes("p=t");
		testCore.setTypeManager(tM);
			
		ConstraintDatabase cDB2 = new ConstraintDatabase();
		
		cDB2.add(new Statement("(s0 p a)"));
		cDB2.add(new Statement("(s1 p b)"));
		cDB2.add(new Statement("(s2 p d)"));
		cDB2.add(new Statement("(e0 p c)"));
		
		
		cDB2.add( new AllenConstraint("s0 Release [0,0]"));
		cDB2.add( new AllenConstraint("s0 Duration [133,inf]"));
		cDB2.add( new AllenConstraint("s1 At [150,150] [300,300]"));
		cDB2.add( new AllenConstraint("s0 Before e0 [1,inf]"));
		cDB2.add( new AllenConstraint("s2 At [500,500] [700,700]"));
		cDB2.add( new AllenConstraint("e0 Duration [100,100]"));
		
		cDB2.add( new PlanningInterval(Term.parse("[0 10000]")));
		
		testCore.setPlan(new Plan());
		Operator a = new Operator();
		a.setName(new Atomic("change"));
		a.addPrecondition(new Statement("(s0 p a)"));
		a.addEffect(new Statement("(e0 p c)"));
		testCore.getPlan().getActions().add(a);
		
		testCore.setContext(cDB2);
		
		testCore = futureEventsChecker.run(testCore);
				
		assertTrue(testCore.getContext().size() == 12 );
	}	
	
	public void testEffectConflictWithPlan() {
		/** Setup Modules */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("findConflicts","class","FindConflictsWithFutureEvents");
//		cM.set("findConflicts","verbose","true");
//		cM.set("findConflicts","verbosity","3");
//		Logger.addPrintStream("findConflicts",System.out);

		Module futureEventsChecker = ModuleFactory.initModule("findConflicts",cM);

		/**
		 * Setup Core
		 */
		Core testCore = new Core();
	
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d");
		tM.attachTypes("p=t");
		testCore.setTypeManager(tM);

		ConstraintDatabase cDB = new ConstraintDatabase();
		
		cDB.add(new Statement("(s0 p a)"));
		cDB.add(new Statement("(s1 p b)"));
		cDB.add(new Statement("(e0 p c)"));
		
		cDB.add( new AllenConstraint("s0 Release [0,0]"));
		cDB.add( new AllenConstraint("s0 Duration [50,inf]"));
		cDB.add( new AllenConstraint("s1 At [150,150] [300,300]"));
		cDB.add( new AllenConstraint("e0 Duration [150,150]"));
		cDB.add( new AllenConstraint("s0 Meets e0"));
		
		cDB.add( new PlanningInterval(Term.parse("[0 10000]")));
		
		testCore.setPlan(new Plan());
		Operator a = new Operator();
		a.setName(new Atomic("change"));
		a.addPrecondition(new Statement("(s0 p a)"));
		a.addEffect(new Statement("(e0 p c)"));
		a.addConstraint( new AllenConstraint("e0 Duration [100,100]"));
		a.addConstraint( new AllenConstraint("s0 Meets e0"));
		
		testCore.getPlan().getActions().add(a);
		
		testCore.setContext(cDB);
			
		testCore = futureEventsChecker.run(testCore);
				
		ArrayList<StateVariableOverride> cList = new ArrayList<StateVariableOverride>();
		cList.addAll( testCore.getContext().get(StateVariableOverride.class) );
		
		assertTrue( cList.size() == 1 );
		assertTrue( cList.get(0).toString().equals("(s0 p a) -> (s1 p b)") );
	}	
}

