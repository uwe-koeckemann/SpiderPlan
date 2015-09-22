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
package org.spiderplan.causal.pocl;

import org.spiderplan.causal.pocl.flaws.FlawCollection;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.search.MultiHeuristicNode;

public class POCLNode extends MultiHeuristicNode {
	
	public ConstraintDatabase context;
	public Resolver resolver;
	public FlawCollection openGoals;
	public POCLNode pred = null;
	
	public POCLNode( int numHeuristics ) {
		super(numHeuristics);
	}
	
	public boolean isSolution() {
		return openGoals.isEmpty();
	}	
	
	/**
	 * Get combined resolver for this node and all predecessors.
	 * @return A single resolver containing all resolvers of this node and its predecessors.
	 */
	public Resolver getCombinedResolver() {
		Substitution theta = this.getCombinedSubstitution();
		if ( theta == null ) {
			return null;
		}
		ConstraintDatabase cdb = this.getCombinedCDB();
		
		return new Resolver(theta, cdb);
	}
	
	public ConstraintDatabase getCombinedCDB() {
		ConstraintDatabase cdb = this.resolver.getConstraintDatabase().copy();
		if ( pred != null ) { 
			cdb.add(pred.getCombinedCDB());
		}
		return cdb;
	}
	
	/**
	 * Get combined resolver for this node and all predecessors.
	 * @return A single resolver containing all resolvers of this node and its predecessors.
	 */
	public Substitution getCombinedSubstitution() {
		Substitution s = this.resolver.getSubstitution().copy();
		if ( pred != null ) {
			Substitution preSubst = pred.getCombinedSubstitution(); 
			if ( preSubst == null || !s.add(preSubst) ) {
				return null;
			}
		}
		return s;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof POCLNode ) {
			POCLNode p = (POCLNode)o;
			return this.resolver.equals(p.resolver);
		} 
		return false;
	}
		
	@Override
	public int depth() {
		return resolver.getConstraintDatabase().get(Operator.class).size();
	}

	@Override
	public String toString() {
		return "Resolver:\n" + resolver.toString();
	}

	public POCLNode copy() {
		POCLNode c = new POCLNode(this.getHeuristicValues().length);
		c.resolver = this.resolver.copy();
		c.openGoals = this.openGoals.copy();
		return c;
	}
	
}
