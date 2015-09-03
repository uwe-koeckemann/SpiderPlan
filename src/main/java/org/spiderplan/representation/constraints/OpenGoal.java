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
package org.spiderplan.representation.constraints;

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.representation.constraints.constraintInterfaces.Assertable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;

/**
 * Marks a list of intervals as goals to make clear
 * that they have to be achieved.
 * 
 * @author Uwe Köckemann
 *
 */
public class OpenGoal extends Constraint implements Substitutable, Mutable, Assertable {
	
	private Statement goal;
	private boolean isAsserted = false;
		
	public OpenGoal( Statement g ) {
		super(ConstraintTypes.Goal);
		this.goal = g;
	}	
	
	public Statement getStatement() { return goal; };
	
	@Override
	public Collection<Term> getVariableTerms() {
		return goal.getVariableTerms();		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		return goal.getGroundTerms();
	}
	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();		
	}
	
	@Override
	public OpenGoal copy() {
		OpenGoal c = new OpenGoal(goal);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof OpenGoal ) {
			OpenGoal piC = (OpenGoal)o;
			return piC.goal.equals(this.goal);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return goal.toString();
	}

	@Override
	public OpenGoal substitute(Substitution theta) {
		goal = goal.substitute(theta);
		return this;
	};
	
	@Override
	public boolean isAsserted() {
		return isAsserted;
	}
	@Override
	public Constraint setAsserted(boolean asserted) {
		isAsserted = asserted;
		return this;
	}
}
