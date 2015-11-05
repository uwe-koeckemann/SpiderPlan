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
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;


/**
 * Contains a {@link Plan} that was applied to reach 
 * (a set of) {@link OpenGoal}s.
 * 
 * TODO: get rid of this?
 * 
 * @author Uwe Köckemann
 *
 */
public class AppliedPlan extends Expression implements Substitutable, Mutable, Assertable {

	final private static Term ConstraintType = Term.createConstant("plan");
	
	private Plan plan;
	private boolean isAsserted = false;
	
	/**
	 * Create a new {@link AppliedPlan}
	 * @param plan The plan that was applied
	 */
	public AppliedPlan( Plan plan ) {
		super(ConstraintType);
		this.plan = plan;
	}
	/**
	 * Get the {@link Plan}
	 * @return The {@link Plan}
	 */
	public Plan getPlan() { return plan; };
	
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Expression substitute(Substitution theta) {
		plan.substitute(theta);
		return this;
	}

	@Override
	public Expression copy() {
		AppliedPlan c = new AppliedPlan(plan.copy());
		c.setAsserted(this.isAsserted());
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof AppliedPlan ) {
			AppliedPlan dC = (AppliedPlan)o;
			return this.plan.equals(dC.plan);
		}
		
		
		return false;
	}

	@Override
	public int hashCode() {
		return plan.hashCode();
	}

	@Override
	public String toString() {
		return "DiscardedPlan: " + plan.toString();
	}

	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Operator o : this.plan.getActions() ) {
			r.addAll(o.getVariableTerms());
		}
		for ( Expression c : this.plan.getConstraints() ) {
			r.addAll(c.getVariableTerms() );
		}
		return r;	
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Operator o : this.plan.getActions() ) {
			r.addAll(o.getGroundTerms());			
		}
		for ( Expression c : this.plan.getConstraints() ) {
			r.addAll(c.getGroundTerms());
		}
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		for ( Operator o : this.plan.getActions() ) {
			r.addAll(o.getAtomics() );
		}
		for ( Expression c : this.plan.getConstraints() ) {
			r.addAll(c.getAtomics() );
		}
		return r;
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
}
