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
package org.spiderplan.causal.planSpacePlanning.flaws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.logic.Substitution;

/**
 * Implements goal selection strategy based on a stack. 
 * Open goals added last will always be resolved first.
 * 
 * @author Uwe Köckemann
 *
 */
public class AllLIFO extends OpenGoalSelector {
	
	private Stack<OpenGoal> s = new Stack<OpenGoal>();

	@Override
	public void add(OpenGoal openGoal) {
		s.add(openGoal);
	}

	@Override
	public OpenGoal select() {
		return s.pop();
	}

	@Override
	public OpenGoalSelector copy() {
		AllLIFO c = new AllLIFO();
		for ( OpenGoal oG : s ) {
			c.add(oG.copy());
		}
		return c;
	}

	@Override
	public boolean isEmpty() {
		return s.isEmpty();
	}

	@Override
	public int size() {
		return s.size();
	}

	@Override
	public void substitute(Substitution theta) {
		for ( OpenGoal oG : this.s ) {
			oG.substitute(theta);
		}
	}

	@Override
	public Collection<OpenGoal> getAll() {
		ArrayList<OpenGoal> all = new ArrayList<OpenGoal>();
		all.addAll(s);
		return all;
	}
	
}
