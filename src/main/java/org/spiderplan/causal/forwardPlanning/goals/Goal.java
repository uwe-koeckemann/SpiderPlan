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
package org.spiderplan.causal.forwardPlanning.goals;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spiderplan.representation.logic.Term;


/**
 * Methods common to all goal classes.
 * 
 * @author Uwe Köckemann
 *
 */
public interface Goal {
	
	/**
	 * Get all {@link SingleGoal}s that belong to this {@link Goal}
	 * @return all single goals contained in this goal
	 */
	public Collection<SingleGoal> getSingleGoals();
	
	/**
	 * Add another {@link Goal} as a requirement for this {@link Goal} 
	 * to become active.
	 * @param req The required {@link Goal}
	 */
	public void addRequirement( Goal req ); 
	/**
	 * Add a {@link Collection} of required {@link Goal}.
	 * @param reqs The required {@link Goal}s
	 */
	public void addRequirements( Collection<Goal> reqs );
	/**
	 * Get all requirements for this {@link Goal}.
	 * @return A {@link Collection} of {@link Goal}s.
	 */
	public Collection<Goal> getRequirements();
	/**
	 * Checks if all requirements of this {@link Goal} are satisfied.
	 * @return <i>true</i> if all requirements are satisfied, <i>false</i> otherwise. 
	 */
	public boolean requirementsSatisfied();
	/**
	 * Check if this {@link Goal} is reached in a state.
	 * @param state Map of {@link Term} variables to value {@link Term}s representing a state.
	 * @return <i>true</i> if {@link Goal} is reached in state, <i>false</i> otherwise.
	 */
	public boolean reachedInState( Map<Term,Term> state );
	/**
	 * Check if this {@link Goal} is reached in a multi-state.
	 * @param state Map of {@link Term} variables to lists of value {@link Term}s representing a multi-state.
	 * @return <i>true</i> if {@link Goal} is reached in state, <i>false</i> otherwise.
	 */
	public boolean reachedInMultiState( Map<Term,List<Term>> state );  
	/**
	 * Set this {@link Goal}'s reached state.
	 * @param reached <i>true</i> if goal has been reached, <i>false</i> otherwise.
	 */
	public void setReached( boolean reached );
	/**
	 * Set a {@link Goal} <i>g</i>'s reached state. <i>g</i> can also be a requirement. 
	 * @param g A goal.
	 * @param reached <i>true</i> if <i>g</i> should be set as reached, <i>false</i> otherwise.
	 */
	public void setReached( Goal g, boolean reached );
	/**
	 * Check if this {@link Goal} has been set as reached.
	 * @return <i>true</i> if goal was reached, <i>false</i> otherwise. 
	 */
	public boolean wasReached();
	/**
	 * Check if this {@link Goal} is a landmark. Landmarks are intermediate goals that have
	 * to be reached but can be ignored after (i.e., they don't have to be reached in the final
	 * state).
	 * @return <i>true</i> if goal is a landmark, <i>false</i> otherwise. 
	 */
	public boolean isLandmark();
	/**
	 * Set this {@link Goal} as a landmark. Landmarks are intermediate goals that have
	 * to be reached but can be ignored after (i.e., they don't have to be reached in the final
	 * state).
	 * @param isLandmark <i>true</i> if goal should become a landmark, <i>false</i> otherwise. 
	 */
	public void setLandmark(  boolean isLandmark );
	
	/**
	 * Get a {@link Collection} of all {@link Term} variable and value {@link Term} pairs in 
	 * this {@link Goal} (without requirements.
	 * @return {@link Collection} of {@link Entry} objects with an {@link Term} variable and a value {@link Term}.
	 */
	public Collection<Entry<Term,Term>> getEntries();	
	
	/**
	 * Return copy of this goal (without requirements)
	 * @return A new {@link Goal} object that can be changed without changing the original.
	 */
	public Goal copy();
	
	@Override
	public boolean equals( Object o );
	@Override
	public int hashCode();
	@Override
	public String toString();
	
}
