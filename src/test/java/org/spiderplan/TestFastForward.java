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
import java.util.HashMap;

import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.fastForward.FastForwardHeuristic;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.*;
import org.spiderplan.representation.types.TypeManager;


import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestFastForward extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Example taken from Jörg Hoffmann, FF: The Fast-Forward Planning System, AI magazine, 2001, 22, 57-62
	 */
	public void testFastForward() {
		
		Atomic svG1 = new Atomic("svG1");
		Atomic svG2 = new Atomic("svG2");
		Atomic svP = new Atomic("svP");
		
		Term bool = Term.createConstant("true");
		
		StateVariableOperator opG1 = new StateVariableOperator();
		opG1.setName( new Atomic("opG1") );
		opG1.getPreconditions().put(svP, bool);
		opG1.getEffects().put(svG1, bool);
		
		StateVariableOperator opG2 = new StateVariableOperator();
		opG2.setName( new Atomic("opG2") );
		opG2.getPreconditions().put(svP, bool);
		opG2.getEffects().put(svG2, bool);
	
		StateVariableOperator opP = new StateVariableOperator();
		opP.setName( new Atomic("opP") );
		opP.getEffects().put(svP, bool);
		
		ArrayList<StateVariableOperator> A = new ArrayList<StateVariableOperator>();
		A.add(opG1);
		A.add(opG2);
		A.add(opP);
		
		FastForwardHeuristic h = new FastForwardHeuristic();
		
		ArrayList<Goal> goal = new ArrayList<Goal>();
		goal.add(new SingleGoal(svG1, bool));
		goal.add(new SingleGoal(svG2, bool));
		
		h.initializeHeuristic( goal, A, new TypeManager());
		
		long hValue = h.calculateHeuristicValue( new HashMap<Atomic, Term>(), goal, new CommonDataStructures() );
		
//		System.out.println(hValue);
		assertTrue( hValue == 3 );
	}
	
}

