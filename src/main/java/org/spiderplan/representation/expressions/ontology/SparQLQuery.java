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
package org.spiderplan.representation.expressions.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * SparQL queries that will substitute a set of variables.
 * Program IDs are used to allow multiple separate queries programs.
 * 
 * @author Uwe Köckemann
 */
public class SparQLQuery extends Expression implements Substitutable, Mutable, Assertable {
	
	private List<Term> targetVariables;
	private Term queryID;
	private Term modelID;
	private boolean isAsserted = false;
		
	/**
	 * Create a new Prolog constraint for a given relation and a 
	 * program ID
	 * @param targetVariables variables that result from the query
	 * @param queryID 
	 * @param modelID 
	 */
	public SparQLQuery( List<Term> targetVariables, Term queryID, Term modelID ) {
		super(ExpressionTypes.SparQL);
		this.queryID = queryID;
		this.modelID = modelID;
		this.targetVariables = targetVariables;
	}
	
	/**
	 * Get the relation-
	 * @return the relation
	 */
	public List<Term> getVariables() {
		return targetVariables;
	}
	
	/**
	 * Get the query ID.
	 * @return the query ID
	 */
	public Term getQueryID() {
		return queryID;
	}
	
	/**
	 * Get the program ID.
	 * @return the program ID
	 */
	public Term getModelID() {
		return modelID;
	}
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		for ( Term var : this.targetVariables ) {
			r.addAll(var.getVariables());
		}
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Term t : this.targetVariables ) {
			if ( t.isGround() ) {
				r.add(t);
			}
		}
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();
	}
	
	@Override
	public String toString() {
		String s = "(query " + queryID + " " + modelID + " ";
		for ( Term t : targetVariables )  {
			s += t;
		}
		s += ")";
		return s;
	}

	@Override
	public Expression substitute(Substitution theta) {
		for ( int i = 0 ; i < targetVariables.size() ; i++ ) {
			targetVariables.set(i, targetVariables.get(i).substitute(theta));
		}
		queryID = queryID.substitute(theta);
		modelID = modelID.substitute(theta);
		return this;
	}
	
	@Override
	public SparQLQuery copy() {
		SparQLQuery c = new SparQLQuery(this.targetVariables, this.queryID, this.modelID);
		c.setAsserted(this.isAsserted());
		return c;
	}
		
	@Override
	public boolean isAsserted() {
		return isAsserted;
	}
	@Override
	public Expression setAsserted(boolean asserted) {
		isAsserted = asserted;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof SparQLQuery ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.targetVariables.hashCode() + 3*this.queryID.hashCode() + 5*this.modelID.hashCode();
	}	
}
