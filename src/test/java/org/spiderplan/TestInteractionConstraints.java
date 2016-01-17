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
import org.spiderplan.tools.ExecuteSystemCommand;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import junit.framework.TestCase;

public class TestInteractionConstraints extends TestCase {
	
	boolean yapExists = true;

	@Override
	public void setUp() throws Exception {
		yapExists = (ExecuteSystemCommand.testIfCommandExists("yap -?"));
	}  

	@Override
	public void tearDown() throws Exception {
		
	}
	
	public void testInteractionConstraint() {
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 (p a b) c)"));
		s.add(new Statement("(s2 (q a) f)"));
		
		
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?C ?F ?S1 ?S2)"));
	
		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) ?C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?A) ?F)"));
		
		ConstraintDatabase c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		
		ic.addResolver(c);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
//		cM.set("icResolver","verbose","true");
//		cM.set("icResolver","verbosity","1");
//		Logger.addPrintStream("icResolver", System.out);
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraintSubstituteAndCopy() {
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?C ?F ?S1 ?S2)"));
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) ?C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?A) ?F)"));	
		ConstraintDatabase c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(c);
		
		Substitution theta = new Substitution();
		theta.add(Term.createVariable("?S1"), Term.createConstant("i1"));
		theta.add(Term.createVariable("?S2"), Term.createConstant("i2"));
		
		InteractionConstraint icCopy = ic.copy();
		
		icCopy.substitute(theta);
					
		assertFalse( ic.equals(icCopy) );
		assertFalse( ic.getCondition().equals(icCopy.getCondition()) );
		for ( int i = 0 ; i < ic.getResolvers().size() ; i++ ) {
			assertFalse( ic.getResolvers().get(i).equals(icCopy.getResolvers().get(i)) );
		}
	}
	
	
	
	public void testInteractionConstraint2() {
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 (p a b) c)"));
		s.add(new Statement("(s2 (q b) f)"));
		s.add(new AllenConstraint("s1 After s2 [1,10]"));
		
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?C ?F ?S1 ?S2)"));
		

		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) ?C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?B) ?F)"));
		
		ConstraintDatabase c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		
		ic.addResolver(c);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Inconsistent) );
	}
	
	public void testInteractionConstraint3() {
		ConstraintDatabase s = new ConstraintDatabase();
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic A B C F S1 S2)"));
		
		s.add(new Statement("(?S1 (p ?A ?B) c)"));
		s.add(new Statement("(?S2 (q ?B) f)"));
		s.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?B) F)"));
		
		ConstraintDatabase c1 = new ConstraintDatabase();
		c1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(c1);
		
		ConstraintDatabase c2 = new ConstraintDatabase();
		c2.add(new AllenConstraint("?S1 After ?S2 [1,inf]"));
		ic.addResolver(c2);

		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraint4() {
		ConstraintDatabase s = new ConstraintDatabase();
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic A B C F S1 S2)"));
		
		s.add(new Statement("(?S1 (p ?A ?B) c)"));
		s.add(new Statement("(?S2 (q b c) f)"));
		s.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?B) F)"));
		
		ConstraintDatabase c1 = new ConstraintDatabase();
		c1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(c1);
		
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraint5() {
		ConstraintDatabase s = new ConstraintDatabase();
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?C ?F ?S1 ?S2)"));
		
		s.add(new Statement("(s1 (p a b) c)"));
		s.add(new Statement("(s2 (q b) f)"));
		s.add(new AllenConstraint("s1 After s2 [1,10]"));
		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) ?C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?B) ?F)"));
		ic.getCondition().add(new AllenConstraint("?S1 Before ?S2 [1,inf]"));
		
		ConstraintDatabase c1 = new ConstraintDatabase();
		c1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(c1);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	/**
	 * Create all possible ways satisfying all supported ways of one IC
	 * @throws NonGroundThing
	 * @throws UnknownThing
	 */
	public void testInteractionConstraint6() {
		if ( !yapExists ) {
			System.out.println("[Warning] Skipping this because yap binary not in $PATH. To run this test install yap (http://www.dcc.fc.up.pt/~vsc/Yap/) and make sure the binary is in $PATH. When using solvers that require yap it is also possible to set the path to the binary as part of the solver configuration.");
		} else {
			ConstraintDatabase s = new ConstraintDatabase();
			InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?C ?D ?E ?F ?S1 ?S2)"));
			Term bkbName = Term.createConstant("prolog");
			
			s.add(new Statement("(s1 (p a b) c)"));
			s.add(new Statement("(s2 (p d e) f)"));
			s.add( new IncludedProgram(bkbName, "notEqual(A,B) :- A \\== B."));		
			
			ic.getCondition().add(new Statement("(?S1 (p ?A ?B) ?C)"));
			ic.getCondition().add(new Statement("(?S2 (p ?D ?E) ?F)"));
			ic.getCondition().add(new PrologConstraint(new Atomic("(notEqual ?S1 ?S2)"), bkbName)); // otherwise S1 == S2 will cause conflict
			
			ConstraintDatabase r1 = new ConstraintDatabase();
			r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
			ic.addResolver(r1);
			
			ConstraintDatabase r2 = new ConstraintDatabase();
			r2.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
			ic.addResolver(r2);
			
			ConstraintDatabase r3 = new ConstraintDatabase();
			r3.add(new AllenConstraint("?S1 During ?S2 [1,10] [1,10]"));
			ic.addResolver(r3);
			
			/**
			 * Setup Modules
			 */
			ConfigurationManager cM = new ConfigurationManager();		
			cM.set("T","class","STPSolver");
			cM.set("T","verbose","true");
			cM.set("T","verbosity","2");
			cM.set("R","class","PrologSolver");
			cM.set("R","verbose","true");
			cM.set("R","verbosity","2");
			cM.set("checker","class","FlowModule");
			cM.set("checker","verbose","true");
			cM.set("checker","modules","R,T");
			cM.set("checker","verbosity","2");
			cM.set("checker","rules","Start=>R;R=>R.Inconsistent=>Fail;R=>T;T=>T.Inconsistent=>Fail;T=>Success");
			
			cM.set("icResolver","class","InteractionConstraintSolver");
			cM.set("icResolver","consistencyChecker","checker");
			
			cM.set("icResolver","verbose","true");
			cM.set("icResolver","verbosity","4");
					
			Module icResolver = ModuleFactory.initModule("icResolver",cM);
			
			/**
			 * Setup Core
			 */
			Core testCore = new Core();
			testCore.setContext( s );
			
			testCore.getContext().add(ic);
			TypeManager tM = new TypeManager();
			tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
			tM.attachTypes("(p t t)=t");
			testCore.setTypeManager(tM);
			
			testCore = icResolver.run(testCore);
			
			assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
		}
	}
	
	public void testInteractionConstraint7() {
		ConstraintDatabase s = new ConstraintDatabase();
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic A B C F S1 S2)"));
		
		s.add(new Statement("(?S1 (p ?A ?B) c)"));
		s.add(new Statement("(?S2 (q ?B) f)"));
		s.add(new Statement("(s3 (r f) a)"));
		
		ic.getCondition().add(new Statement("(?S1 (p ?A ?B) C)"));
		ic.getCondition().add(new Statement("(?S2 (q ?B) F)"));
		
		ConstraintDatabase c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		
		ic.addResolver(c);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b,c,d,e,f,g");
		tM.attachTypes("(p t t)=t");
		tM.attachTypes("(q t)=t");
		tM.attachTypes("(r t)=t");
		testCore.setTypeManager(tM);
		
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraint8() {
		ConstraintDatabase s = new ConstraintDatabase();
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic A B C D S1 S2)"));
		
		s.add(new Statement("(?S1 (p a) b)"));
		s.add(new Statement("(?S2 (p c) c)"));
		s.add(new Statement("(s3 (q a) b)"));
		s.add(new Statement("(s4 (q c) c)"));		
		
		ic.getCondition().add(new Statement("(?S1 (p A) B)"));
		ic.getCondition().add(new Statement("(?S2 (q C) D)"));
		
		ConstraintDatabase c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(c);
		
		c = new ConstraintDatabase();
		c.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
		ic.addResolver(c);
		
		c = new ConstraintDatabase();
		c.add(new AllenConstraint("S1 During S2 [1,10] [1,10]"));
		ic.addResolver(c);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("(p t)=t");
		tM.attachTypes("(q t)=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	
	public void testInteractionConstraintModule() {
		/**
		 * Setup context
		 */
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 p a)"));
		s.add(new Statement("(s2 q b)"));
		s.add(new Statement("(s3 p a)"));
		s.add(new Statement("(s4 q b)"));
//		s.add(new TemporalConstraint("?S1 Before ?S2 [1,inf]"));
		
		/**
		 * Setup InteractionConstraint
		 */
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?S1 ?S2)"));
		ic.getCondition().add(new Statement("(?S1 p ?A)"));
		ic.getCondition().add(new Statement("(?S2 q ?B)"));
//		ic.getCondition().add(new TemporalConstraint("?S1 After ?S2 [1,inf]"));
		ConstraintDatabase r1 = new ConstraintDatabase();
		r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(r1);
		ConstraintDatabase r2 = new ConstraintDatabase();
		r2.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
		ic.addResolver(r2);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("p=t");
		tM.attachTypes("q=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraintModule2() {
		/**
		 * Setup context
		 */
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 p a)"));
		s.add(new Statement("(s2 q b)"));
		s.add(new AllenConstraint("s1 After s2 [1,inf]"));
		
		/**
		 * Setup InteractionConstraint
		 */
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?S1 ?S2)"));
		ic.getCondition().add(new Statement("(?S1 p ?A)"));
		ic.getCondition().add(new Statement("(?S2 q ?B)"));
//		ic.getCondition().add(new TemporalConstraint("?S1 After ?S2 [1,inf]"));
		ConstraintDatabase r1 = new ConstraintDatabase();
		r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(r1);
		ConstraintDatabase r2 = new ConstraintDatabase();
		r2.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
		ic.addResolver(r2);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		
//		cM.set("icResolver","verbose","true");
//		cM.set("icResolver","verbosity","3");
//		Logger.addPrintStream("icResolver",System.out);
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("p=t");
		tM.attachTypes("q=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	public void testInteractionConstraintModule3() {
		/**
		 * Setup context
		 */
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(s1 p a)"));
		s.add(new Statement("(s2 q b)"));
		s.add(new AllenConstraint("s1 After s2 [1,inf]"));
		
		/**
		 * Setup InteractionConstraint
		 */
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?S1 ?S2)"));
		ic.getCondition().add(new Statement("(?S1 p ?A)"));
		ic.getCondition().add(new Statement("(?S2 q ?B)"));
		ConstraintDatabase r1 = new ConstraintDatabase();
		r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(r1);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("p=t");
		tM.attachTypes("q=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Inconsistent) );
	}
	
	/**
	 * Testing a case where no condition applies.
	 */
	public void testInteractionConstraintModule4() {
		/**
		 * Setup context
		 */
		ConstraintDatabase s = new ConstraintDatabase();
		
		/**
		 * Setup InteractionConstraint
		 */
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?S1 ?S2)"));
		ic.getCondition().add(new Statement("(?S1 p ?A)"));
		ic.getCondition().add(new Statement("(?S2 q ?B)"));
		ConstraintDatabase r1 = new ConstraintDatabase();
		r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
		ic.addResolver(r1);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
		
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
		
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("p=t");
		tM.attachTypes("q=t");
		testCore.setTypeManager(tM);
		
		testCore = icResolver.run(testCore);
		
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	/**
	 * Testing with a resolver that adds new {@link Statement}s.
	 */
	public void testInteractionConstraintsWithStatementsInResolver() {
		/**
		 * Setup context
		 */
		ConstraintDatabase s = new ConstraintDatabase();
		s.add(new Statement("(x p a)"));
		s.add(new Statement("(y q b)"));
		s.add(new Statement("(z r b)"));
		s.add(new Statement("(z1 r b)"));
		s.add(new Statement("(z2 r b)"));
		
		s.add(new AllenConstraint("x Before y [1,inf]"));
		s.add(new AllenConstraint("z1 Before x [1,inf]"));
		s.add(new AllenConstraint("z2 After y [1,inf]"));
		
		/**
		 * Setup InteractionConstraint
		 */
		InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?X ?Y)"));
		ic.getCondition().add(new Statement("(?X p ?A)"));
		ic.getCondition().add(new Statement("(?Y q ?B)"));
		ic.getCondition().add(new AllenConstraint("?X Before ?Y [1,inf]"));
		ConstraintDatabase r1 = new ConstraintDatabase();
		r1.add(new Statement("(?Z r ?B)"));
		r1.add(new AllenConstraint("?X Overlaps ?Z [1,inf]"));
		r1.add(new AllenConstraint("?Z Overlaps ?Y [1,inf]"));
		ic.addResolver(r1);
		
		/**
		 * Setup Modules
		 */
		ConfigurationManager cM = new ConfigurationManager();		
		cM.set("temporalChecker","class","STPSolver");
		cM.set("icResolver","class","InteractionConstraintSolver");
		cM.set("icResolver","consistencyChecker","temporalChecker");
//		cM.set("icResolver","verbose","true");
//		cM.set("icResolver","verbosity","3");
		Module icResolver = ModuleFactory.initModule("icResolver",cM);
			
		/**
		 * Setup Core
		 */
		Core testCore = new Core();
		testCore.setContext( s );
		
		testCore.getContext().add(ic);
		TypeManager tM = new TypeManager();
		tM.addSimpleEnumType("t","a,b");
		tM.attachTypes("p=t");
		tM.attachTypes("q=t");
		tM.attachTypes("r=t");
		testCore.setTypeManager(tM);
		
		ArrayList<String> inSignals = new ArrayList<String>();
		inSignals.add("AddConstraints");
		
		testCore.setInSignals(inSignals);
		
		testCore = icResolver.run(testCore);
			
		assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
	}
	
	/**
	 * Simple example using {@link PrologConstraint}s in {@link InteractionConstraint}
	 * condition.
	 */
	public void testInteractionConstraintModuleWithPrologConstraints() {
		if ( !yapExists ) {
			System.out.println("[Warning] Skipping this because yap binary not in $PATH. To run this test install yap (http://www.dcc.fc.up.pt/~vsc/Yap/) and make sure the binary is in $PATH. When using solvers that require yap it is also possible to set the path to the binary as part of the solver configuration.");
		} else {
			Term bkbName = Term.createConstant("prolog");
			/**
			 * Setup context
			 */
			ConstraintDatabase s = new ConstraintDatabase();
			s.add(new Statement("(s1 p a)"));
			s.add(new Statement("(s2 q b)"));
			s.add(new Statement("(s3 p c)"));
			s.add(new Statement("(s4 q d)"));
			s.add( new AllenConstraint("s3 After s4 [1,10]"));
			ConstraintDatabase C = new ConstraintDatabase();
			C.add(new IncludedProgram(bkbName,"good(a)."));
			C.add(new IncludedProgram(bkbName,"good(b)."));
			
			/**
			 * Setup InteractionConstraint
			 */
			InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic A B S1 S2)"));
			ic.getCondition().add(new Statement("(?S1 p ?A)"));
			ic.getCondition().add(new Statement("(?S2 q ?B)"));
			ic.getCondition().add(new PrologConstraint(new Atomic("(good ?A)"), bkbName));
			ic.getCondition().add(new PrologConstraint(new Atomic("(good ?B)"), bkbName));
			ConstraintDatabase r1 = new ConstraintDatabase();
			r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
			ic.addResolver(r1);
			
			C.add(ic);
			
			/**
			 * Setup Modules
			 */
			ConfigurationManager cM = new ConfigurationManager();
			Global.resetStatics();
			cM.set("T","class","STPSolver");
			cM.set("R","class","PrologSolver");
			cM.set("checker","class","FlowModule");
			cM.set("checker","modules","R,T");
			cM.set("checker","rules","Start=>R;R=>R.Inconsistent=>Fail;R=>T;T=>T.Inconsistent=>Fail;T=>Success");
			
			cM.set("icResolver","class","InteractionConstraintSolver");
			cM.set("icResolver","consistencyChecker","checker");
			
			Module icResolver = ModuleFactory.initModule("icResolver",cM);
			
			/**
			 * Setup Core
			 */
			Logger.init();
			Core testCore = new Core();
			testCore.setContext( s );
			TypeManager tM = new TypeManager();
			tM.addSimpleEnumType("t","a,b");
			tM.attachTypes("p=t");
			tM.attachTypes("q=t");
			testCore.setTypeManager(tM);
			testCore.getContext().addAll(C);
			
			testCore = icResolver.run(testCore);
			
	
			assertTrue( testCore.getResultingState("icResolver").equals(State.Consistent) );
		}
	}
	
	/**
	 * Testing with {@link PrologConstraint}s in Resolver leading to an unsatisfiable
	 * problem.
	 */
	public void testInteractionConstraintModuleWithPrologConstraintsInResolver() {
		if ( !yapExists ) {
			System.out.println("[Warning] Skipping this because yap binary not in $PATH. To run this test install yap (http://www.dcc.fc.up.pt/~vsc/Yap/) and make sure the binary is in $PATH. When using solvers that require yap it is also possible to set the path to the binary as part of the solver configuration.");
		} else {
			Term bkbName = Term.createConstant("prolog");
			
			/**
			 * Setup context
			 */
			ConstraintDatabase s = new ConstraintDatabase();
			s.add(new Statement("(s1 p ?X)"));
			s.add(new Statement("(s2 q ?Y)"));
			s.add(new Statement("(s3 p c)"));
			s.add(new Statement("(s4 q d)"));
			ConstraintDatabase C = new ConstraintDatabase();
			C.add(new IncludedProgram(bkbName, "good(a)."));
			C.add(new IncludedProgram(bkbName, "good(b)."));
			
			/**
			 * Setup interaction constraint
			 */
			InteractionConstraint ic = new InteractionConstraint(new Atomic("(ic ?A ?B ?S1 ?S2)"));
			ic.getCondition().add(new Statement("(?S1 p ?A)"));
			ic.getCondition().add(new Statement("(?S2 q ?B)"));
			ConstraintDatabase r1 = new ConstraintDatabase();
			r1.add(new AllenConstraint("?S1 Before ?S2 [1,10]"));
			r1.add(new PrologConstraint(new Atomic("(good ?A)"), bkbName));
			r1.add(new PrologConstraint(new Atomic("(good ?B)"), bkbName));
			ic.addResolver(r1);
			ConstraintDatabase r2 = new ConstraintDatabase();
			r2.add(new AllenConstraint("?S1 After ?S2 [1,10]"));
			r2.add(new PrologConstraint(new Atomic("(good ?A)"), bkbName));
			r2.add(new PrologConstraint(new Atomic("(good ?B)"), bkbName));
			ic.addResolver(r2);
			
			C.add(ic);
			
			/**
			 * Setup Modules
			 */
			ConfigurationManager cM = new ConfigurationManager();		
			cM.set("T","class","STPSolver");
//			cM.set("T","verbose","true");
//			cM.set("T","verbosity","2");
			cM.set("R","class","PrologSolver");
//			cM.set("R","verbose","true");
//			cM.set("R","verbosity","2");
			cM.set("checker","class","SolverStack");
//			cM.set("checker","verbose","true");
			cM.set("checker","solvers","T,R");
//			cM.set("checker","verbosity","2");
			
			cM.set("main","class","SolverStack");
//			cM.set("main","verbose","true");
			cM.set("main","solvers","T,R,icResolver");
//			cM.set("main","verbosity","2");
			
			cM.set("icResolver","class","InteractionConstraintSolver");
			cM.set("icResolver","consistencyChecker","checker");
			
//			cM.set("icResolver","verbose","true");
//			cM.set("icResolver","verbosity","5");
			
			Logger.addPrintStream("main", System.out);
			Logger.addPrintStream("checker", System.out);
			Logger.addPrintStream("icResolver", System.out);
			
		
			Module icResolver = ModuleFactory.initModule("main",cM);
			
			/**
			 * Setup Core
			 */
			Core testCore = new Core();
			testCore.setContext( s );
			TypeManager tM = new TypeManager();
			tM.addSimpleEnumType("t","a,b");
			tM.attachTypes("p=t");
			tM.attachTypes("q=t");
			testCore.setTypeManager(tM);
			testCore.getContext().addAll(C);
			
			testCore = icResolver.run(testCore);
	
			assertTrue( testCore.getResultingState("main").equals(State.Inconsistent) );
		}
	}
}

