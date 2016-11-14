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
package org.spiderplan.representation.expressions.causal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;


/**
 * Every plan that matches this one can be pruned.
 * 
 * @author Uwe Köckemann
 *
 */
public class DiscardedPlan extends Expression implements Substitutable, Mutable {
	
	final private static Term ConstraintType = Term.createConstant("discarded-plan");

	private Plan dPlan;
	private boolean standaloneInconsistency = false;
	
	/**
	 * Create a new {@link DiscardedPlan}
	 * @param plan The plan that was applied
	 */
	public DiscardedPlan( Plan plan ) {
		super(ConstraintType);
		this.dPlan = plan;
	}
	
	/**
	 * Create a new {@link DiscardedPlan}
	 * @param plan A {@link Plan} that can be pruned.
	 * @param standaloneInconsistency True if the plan was found inconsistent even 
	 * 			when disregarding the initial context. This allows to prune everything 
	 * 			that contains the given plan as a subsequence. 
	 */
	public DiscardedPlan( Plan plan, boolean standaloneInconsistency ) {
		super(ConstraintType);
		this.dPlan = plan;
		this.standaloneInconsistency = standaloneInconsistency;
	}
	
	/**
	 * Get the {@link Plan}
	 * @return The {@link Plan}
	 */
	public Plan getPlan() { return dPlan; };
	
	/**
	 * Check if the plan can be discarded regardless of the context it is applied to.
	 * @return <code>true</code> if the {@link Plan} is inconsistent regardless of the context it is applied to, false otherwise.
	 */
	public boolean standaloneInconsistency() {
		return standaloneInconsistency;
	}
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Expression substitute(Substitution theta) {
		dPlan.substitute(theta);
		return this;
	}

	@Override
	public Expression copy() {
		DiscardedPlan c = new DiscardedPlan(dPlan.copy(), this.standaloneInconsistency);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof DiscardedPlan ) {
			DiscardedPlan dC = (DiscardedPlan)o;
			return this.dPlan.equals(dC.dPlan);
		}
		
		
		return false;
	}

	@Override
	public int hashCode() {
		return dPlan.hashCode();
	}

	@Override
	public String toString() {
		return "DiscardedPlan: " + dPlan.toString();
	}

	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Operator o : this.dPlan.getActions() ) {
			r.addAll(o.getVariableTerms());
		}
		for ( Expression c : this.dPlan.getConstraints() ) {
			r.addAll(c.getVariableTerms() );
		}
		return r;	
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Operator o : this.dPlan.getActions() ) {
			r.addAll(o.getGroundTerms());			
		}
		for ( Expression c : this.dPlan.getConstraints() ) {
			r.addAll(c.getGroundTerms());
		}
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		for ( Operator o : this.dPlan.getActions() ) {
			r.addAll(o.getAtomics() );
		}
		for ( Expression c : this.dPlan.getConstraints() ) {
			r.addAll(c.getAtomics() );
		}
		return r;
	}
}
