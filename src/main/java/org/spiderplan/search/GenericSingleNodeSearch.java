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
package org.spiderplan.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple generic class for backtracking over an arbitrary list of lists.
 * Search is guided by user with method advance(), letting the search
 * know if the last node was approved.
 * Current assignment is returned by getAssignment().
 * <b>
 * Successful if forward() returns true.
 * <b>
 * Failed if backtrack() returns false.
 * 
 * @author Uwe Köckemann
 *
 * @param <T>
 */
public class GenericSingleNodeSearch<T> {

	private int i = 0;
	private int [] assignment;
	private int numVariables;
	private int [] numValues;
	private List<List<T>> space;
	
	private boolean success = false;
	private boolean failure = false;
	
	/**
	 * Construct new search by using a list of lists to define a search space over possible choices (inner lists) for each variable (outer list).
	 * @param space list of lists, where the outer list represents variables and the inner lists are choices for each variable
	 */
	public GenericSingleNodeSearch( List<List<T>> space ) {
		if ( space.isEmpty() ) {
			throw new IllegalArgumentException("No variables provided.");
		}
		this.space = space;
		
		numVariables = space.size();
		numValues = new int[space.size()];
		assignment = new int[space.size()];
		
		for ( int k = 0 ; k < assignment.length ; k++ ) {
			if ( space.get(k).isEmpty() ) {
				throw new IllegalArgumentException("Variable " + k + " has no possible values.");
			}
			assignment[k] = 0;
			numValues[k] = space.get(k).size();
		}
		
		if ( space.size() == 1 ) { // if there is only 1 variable, search starts as success...
			success = true;
		}
	}
	
	/**
	 * Check if search was successful.
	 * @return <i>true</i> iff search is done and was successful, <i>false</i> otherwise.
	 */
	public boolean success() {
		return success;
	}
	
	/**
	 * Check if search failed.
	 * @return <i>true</i> iff search is done and failed, <i>false</i> otherwise.
	 */
	public boolean failure() {
		return failure;
	}
	
	/**
	 * Advance search
	 * @param lastAssignmentApproved <i>true</i> if last assignment was approved by user, <i>false</i> otherwise.
	 * @return <i>true</i> if search is done (regardless of success or failure)
	 */
	public boolean advance( boolean lastAssignmentApproved ) {
		success = false;
		failure = false;
		if ( lastAssignmentApproved ) {
			success = forward();
			return success;
		} else {
			failure = !backtrack();
			return failure;
		}
	}

	
	/**
	 * Assign next variable. Call if last assignment was good.
	 * 
	 * @return <i>true</i> if all variables are already assigned (success), <i>false</i> otherwise
	 */
	private boolean forward() {
		if ( i < numVariables-1 ) {
			i++;
			assignment[i] = 0;
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Backtrack. Call if last assignment was bad.
	 * 
	 * @return <i>true</i> if there is something left to explore, <i>false</i> otherwise
	 */
	private boolean backtrack() {
		if ( i >= 0 && assignment[i] < numValues[i]-1 ) {
			assignment[i]++;
			return true;
		} else if ( i >= 0 ) {
			assignment[i] = 0;
			i--;
			return backtrack();
		}
		return false;
	}
	
	/**
	 * Returns current assignment. 
	 * @return list of objects that are currently selected
	 */
	public List<T> getAssignment() {
		ArrayList<T> a = new ArrayList<T>();
		for ( int k = 0 ; k <= i ; k++ ) {
			a.add(space.get(k).get(assignment[k]));
		}
		return a;
	}
	
	/**
	 * Calculates and returns the size of the search space.
	 * @return number of possible combinations of variables
	 */
	public int getNumCombos() {
		int r = 1;
		for ( int i = 0 ; i < numValues.length ; i++ ) {
			r *= i;
		}
		return r;
	}
}
