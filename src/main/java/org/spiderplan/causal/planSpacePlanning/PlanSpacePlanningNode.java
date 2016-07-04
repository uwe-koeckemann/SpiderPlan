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
package org.spiderplan.causal.planSpacePlanning;

import org.spiderplan.causal.planSpacePlanning.flaws.OpenGoalSelector;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.search.MultiHeuristicNode;

/**
 * Represents a node in the search space for plan-space planning.
 * 
 * @author Uwe Köckemann
 */
public class PlanSpacePlanningNode extends MultiHeuristicNode {
		
	private ConstraintDatabase context;
	private Resolver resolver;
	private OpenGoalSelector openGoals;
	private PlanSpacePlanningNode pred = null;
	
	/**
	 * Create a new node.
	 * 
	 * @param cdb context to be resolved
	 * @param r resolver
	 * @param ogs open goal selector
	 * @param pred predecessor in the search space
	 * @param numHeuristics number of heuristics
	 */
	public PlanSpacePlanningNode( ConstraintDatabase cdb, Resolver r, OpenGoalSelector ogs, PlanSpacePlanningNode pred, int numHeuristics ) {
		super(numHeuristics);
		
		this.context = cdb;
		this.resolver = r;
		this.openGoals = ogs;
		this.pred = pred;
	}
	
	/**
	 * Test if all open goals were reached.
	 * 
	 * @return <code>true</code> if all open goals were reached, <code>false</code> otherwise.
	 */
	public boolean isSolution() {
		return openGoals.isEmpty();
	}	
	
	/**
	 * Get the context this node works on.
	 * 
	 * @return the context
	 */
	public ConstraintDatabase getContext() { return context; }
	
	/**
	 * Get the resolver used by this node.
	 * 
	 * @return the resolver
	 */
	public Resolver getResolver() { return resolver; }
	
	/**
	 * Returns goal selector of this node.
	 * 
	 * @return the goal selector
	 */
	public OpenGoalSelector getGoalSelector() { return this.openGoals; }
	
	/**
	 * Get combined resolver for this node and all predecessors.
	 * 
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
	
	/**
	 * Recursively apply resolver of this node
	 * and all predecessors in the search space.
	 * 
	 * @return combined resolver
	 */
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
		if ( o instanceof PlanSpacePlanningNode ) {
			PlanSpacePlanningNode p = (PlanSpacePlanningNode)o;
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

	/**
	 * Copy this node.
	 * 
	 * @return the copy
	 */
	public PlanSpacePlanningNode copy() {
		return new PlanSpacePlanningNode(context.copy(), resolver.copy(), openGoals.copy(), pred, this.getHeuristicValues().length);
	}
	
}
