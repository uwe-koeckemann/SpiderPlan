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
package org.spiderplan.causal.causalGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.StateVariableOperator;
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
	
//	private List<Atomic> conditionVars = new ArrayList<Atomic>();
//	private List<Term>   conditionVals = new ArrayList<Term>();
	
	Set<StateVariableOperator> associatedOperators = new HashSet<StateVariableOperator>();
	
	public Term getSource() {
		return source;
	}
	public Term getDest() {
		return dest;
	}
	public void setSource( Term v ) {
		source = v;
	}
	public void setDest( Term v ) {
		dest = v;
	}
	
	
	public void addOperator( StateVariableOperator o ) {
		associatedOperators.add(o);
	}
	
	public Set<StateVariableOperator> getOperators( ) {
		return associatedOperators;
	}
	
	public Map<Atomic,Term> getConditions() { 
		return conditions;
	}
	
	public void add( Atomic v, Term d ) {
		conditions.put(v,d);
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
