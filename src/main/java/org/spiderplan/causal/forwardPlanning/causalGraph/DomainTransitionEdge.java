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
package org.spiderplan.causal.forwardPlanning.causalGraph;

import java.util.HashMap;
import java.util.Map;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Data structure describing possible transitions of a single state-variable.
 * Used by {@link DomainTransitionGraph}.
 * <p>
 * Relevant papers:
 * <li> Helmert, M. The fast downward planning system Journal of Artificial Intelligence Research, 2006, 26, 191-246 
 * <p>
 * 
 * @author Uwe Köckemann
 *
 */
public class DomainTransitionEdge {
	long ID;
	Term source;
	Term dest;
	private Map<Atomic,Term> conditions = new HashMap<Atomic, Term>();
	
	/**
	 * Construct new edge with source and destination values and and ID
	 * @param source the source of the transition
	 * @param dest the destination of the transition
	 * @param ID edge ID
	 */
	public DomainTransitionEdge( Term source, Term dest, long ID ) {
		this.source = source;
		this.dest = dest;
		this.ID = ID;
		
	}
	
	/**
	 * Get source value of transition
	 * @return source value
	 */
	public Term getSource() {
		return source;
	}
	/**
	 * Get destination of transition
	 * @return destination value
	 */
	public Term getDest() {
		return dest;
	}
		
	/**
	 * Get the conditions under which this transition can be performed.
	 * @return map from state-variables to values that are required in a state to perform this transition
	 */
	public Map<Atomic,Term> getConditions() { 
		return conditions;
	}
		
	@Override
	public boolean equals( Object o ) {
		if ( ! (o instanceof DomainTransitionEdge) ) {
			return false;
		}
		DomainTransitionEdge e = (DomainTransitionEdge)o;
		return ID == e.ID && conditions.equals(e.conditions);
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public String toString() {
		return ID + ": " + conditions.toString();
	}
}
