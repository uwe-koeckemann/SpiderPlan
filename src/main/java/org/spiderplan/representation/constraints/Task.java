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
 * Contains a {@link Statement} representing a task.
 * 
 * @author Uwe Köckemann
 *
 */
public class Task extends Constraint implements Substitutable, Mutable, Assertable {
	
	private Statement task;
	private boolean isAsserted = false;
		
	public Task( Statement g ) {
		super(ConstraintTypes.Goal);
		this.task = g;
	}	
	
	public Statement getStatement() { return task; };
	
	@Override
	public Collection<Term> getVariableTerms() {
		return task.getVariableTerms();		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		return task.getGroundTerms();
	}
	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();		
	}
	
	@Override
	public Task copy() {
		Task c = new Task(task);
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Task ) {
			Task piC = (Task)o;
			return piC.task.equals(this.task);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return task.toString();
	}

	@Override
	public Task substitute(Substitution theta) {
		task = task.substitute(theta);
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
