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

import org.spiderplan.modules.PrologSolver;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.prolog.YapPrologAdapter;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.IncludedProgram;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.ExecuteSystemCommand;

import junit.framework.TestCase;

public class TestPrologSolver extends TestCase {
	
	Term bkbName = Term.createConstant("prolog");
	
	boolean yapExists = true;

	@Override
	public void setUp() throws Exception {
		yapExists = (ExecuteSystemCommand.testIfCommandExists("yap"));
		if ( !yapExists ) {  
			System.out.println("[Warning] Could not find yap binary in $PATH when testing Prolog constraints and solvers. Skipping some tests accordingly. To run these tests install yap (http://www.dcc.fc.up.pt/~vsc/Yap/) and make sure the binary is in $PATH. When using solvers that require yap it is also possible to set the path to the binary as part of the solver configuration.");
		}
	}  

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testPrologSolver() {
		if ( yapExists ) {
			ConfigurationManager cM = new ConfigurationManager();
			cM.add("PrologSolver");
			
			ConstraintDatabase s = new ConstraintDatabase();
			s.add(new IncludedProgram(bkbName,"good(a)."));
			s.add(new IncludedProgram(bkbName,"good(b)."));
			s.add(new PrologConstraint(new Atomic("(good ?X)"), bkbName));
			
			Core core = new Core();
			core.setContext(s);
			core.setTypeManager(new TypeManager());
			
			PrologSolver pSolver = new PrologSolver("PrologSolver", cM);
			SolverResult result = pSolver.testAndResolve(core);
			
			assertTrue(result.getState().equals(State.Searching));
			
			assertTrue(result.getResolverIterator().next() != null);
			assertTrue(result.getResolverIterator().next() != null);
			assertTrue(result.getResolverIterator().next() == null);
		}
	}
	
	public void testYAPQueryPositive() {	
		if ( yapExists ) {
			ConstraintCollection kb = new ConstraintCollection();
			ArrayList<PrologConstraint> q = new ArrayList<PrologConstraint>();
			
			kb.add(new IncludedProgram(bkbName, "t(a)."));
			kb.add(new IncludedProgram(bkbName, "t(c)."));
			kb.add(new IncludedProgram(bkbName, "s(a)."));
			kb.add(new IncludedProgram(bkbName, "s(b)."));
			kb.add(new IncludedProgram(bkbName, "p(A) :- s(A)."));
			
			q.add(new PrologConstraint(new Atomic("(s ?A)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(t ?A)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(p ?B)"), bkbName));
			
			YapPrologAdapter p = new YapPrologAdapter();
			
			Collection<Substitution> qResult = p.query(kb,q, bkbName, new TypeManager());
			
			assertTrue( qResult != null );
			
			ArrayList<Substitution> qResultList = new ArrayList<Substitution>();
			qResultList.addAll(qResult);
	
			assertTrue( qResultList.get(0).getStringMap().containsKey("?A") ) ;
			assertTrue( qResultList.get(0).getStringMap().get("?A").equals("a") ) ;
			assertTrue( qResultList.get(0).getStringMap().containsKey("?B") ) ;
			assertTrue( qResultList.get(0).getStringMap().get("?B").equals("a") ) ;
			assertTrue( qResultList.get(1).getStringMap().containsKey("?A") ) ;
			assertTrue( qResultList.get(1).getStringMap().get("?A").equals("a") ) ;
			assertTrue( qResultList.get(1).getStringMap().containsKey("?B") ) ;
			assertTrue( qResultList.get(1).getStringMap().get("?B").equals("b") ) ;
		}
	}
	
	public void testYAPQueryNegative() {	
		if ( yapExists ) {
			ConstraintCollection kb = new ConstraintCollection();
			ArrayList<PrologConstraint> q = new ArrayList<PrologConstraint>();
	
			kb.add(new IncludedProgram(bkbName, "t(a)."));
			kb.add(new IncludedProgram(bkbName, "t(c)."));
			kb.add(new IncludedProgram(bkbName, "s(a)."));
			kb.add(new IncludedProgram(bkbName, "s(b)."));
			kb.add(new IncludedProgram(bkbName, "p(c)."));
			
			q.add(new PrologConstraint(new Atomic("(s ?A)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(t ?A)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(p ?A)"), bkbName));
			
			YapPrologAdapter p = new YapPrologAdapter();
			
			assertTrue( p.query(kb,q, bkbName, new TypeManager()) == null );	
		}
	}
	
	public void testYAPQueryOnlyConstantsPositive() {	
		if ( yapExists ) {
			ConstraintCollection kb = new ConstraintCollection();
			ArrayList<PrologConstraint> q = new ArrayList<PrologConstraint>();
	
			kb.add(new IncludedProgram(bkbName, "t(a)."));
			kb.add(new IncludedProgram(bkbName, "t(c)."));
			kb.add(new IncludedProgram(bkbName, "s(a)."));
			kb.add(new IncludedProgram(bkbName, "s(b)."));
			kb.add(new IncludedProgram(bkbName, "p(A) :- s(A)."));
			
			q.add(new PrologConstraint(new Atomic("(s a)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(t a)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(p b)"), bkbName));
			
			YapPrologAdapter p = new YapPrologAdapter();
			
			assertTrue( p.query(kb, q, bkbName, new TypeManager()) != null );
		}
	}
	
	public void testYAPQueryOnlyConstantsNegative() {	
		if ( yapExists ) {
			ConstraintCollection kb = new ConstraintCollection();
			ArrayList<PrologConstraint> q = new ArrayList<PrologConstraint>();
	
			kb.add(new IncludedProgram(bkbName, "t(a)."));
			kb.add(new IncludedProgram(bkbName, "t(c)."));
			kb.add(new IncludedProgram(bkbName, "s(a)."));
			kb.add(new IncludedProgram(bkbName, "s(b)."));
			kb.add(new IncludedProgram(bkbName, "p(A) :- s(A)."));
			
			q.add(new PrologConstraint(new Atomic("(s a)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(t a)"), bkbName));
			q.add(new PrologConstraint(new Atomic("(p z)"), bkbName));
			
			YapPrologAdapter p = new YapPrologAdapter();
			
			assertTrue( p.query(kb, q, bkbName, new TypeManager()) == null );
		}
	}
}

