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
package org.spiderplan.causal.planSpacePlanning.flaws;

import java.util.Collection;

import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.Substitution;

/**
 * Interface for flaw selection strategies.
 * 
 * @author Uwe Köckemann
 *
 */
public abstract class OpenGoalSelector {
	/**
	 * Add an open goal.
	 * 
	 * @param openGoal
	 */
	public abstract void add( OpenGoal openGoal );
	
	/**
	 * Select an open goal.
	 * 
	 * @return next open goal according to flaw selection strategy
	 */
	public abstract OpenGoal select();
	
	/**
	 * Copy this goal selector.
	 * 
	 * @return the copy
	 */
	public abstract OpenGoalSelector copy();
	
	/**
	 * Test if this goal selector is empty.
	 * 
	 * @return <code>true</code> if this flaw collection contains no open goals, <code>false</code> otherwise.
	 */
	public abstract boolean isEmpty();
	
	/**
	 * Returns number of goals in this goal selector.
	 * 
	 * @return number of open goals added to this goal selector
	 */
	public abstract int size();
	
	/**
	 * Get all open goals.
	 * 
	 * @return open goals
	 */
	public abstract Collection<OpenGoal> getAll();
	
	/**
	 * Substitute all goals contained in this selector.
	 * 
	 * @param theta a substitution
	 */
	public abstract void substitute( Substitution theta );
}

