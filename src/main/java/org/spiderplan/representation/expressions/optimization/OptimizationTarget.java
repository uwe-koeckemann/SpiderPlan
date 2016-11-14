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
package org.spiderplan.representation.expressions.optimization;

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.OptimizationRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Defines a term as a target for optimization.
 */
public class OptimizationTarget extends Expression implements Matchable, Substitutable {
	
	private OptimizationRelation relation;
	private Term targetTerm;
	
	/**
	 * Create a new {@link OptimizationTarget} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 */
	public OptimizationTarget( Atomic l ) {
		super(ExpressionTypes.Optimization);
		relation = ExpressionTypes.OptimizationExpressions.assertSupported(l, this.getClass());
		this.targetTerm = l.getArg(0);
	}
	
	/**
	 * Create new optimization target.
	 * @param relation min or max
	 * @param target target term
	 */
	public OptimizationTarget( OptimizationRelation relation, Term target ) {
		super(ExpressionTypes.Optimization);
		this.relation = relation;
		this.targetTerm = target;
	}
	
//	/**
//	 * Get relational representation of this math constraint.
//	 * @return the relation
//	 */
//	public Atomic getConstraint() {
//		return con;
//	}
	
	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public OptimizationRelation getRelation() {
		return relation;
	}
	
	/**
	 * Get the term that has to be optimized.
	 * @return optimization target term
	 */
	public Term getTargetTerm() {
		return targetTerm;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		return this.targetTerm.getVariables();
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		if ( this.targetTerm.isGround() ) {
			r.add(this.targetTerm);
		}
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
//		r.add(this.r);
		return r;		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if ( this.relation.equals(OptimizationRelation.Minimize)) {
			sb.append("(min ");	
		} else {
			sb.append("(max ");
		}
		sb.append(targetTerm.toString());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new OptimizationTarget(this.relation, this.targetTerm.substitute(theta));
	}
	
	@Override
	public boolean isGround() {
		return targetTerm.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof OptimizationTarget ) {
			OptimizationTarget rC = (OptimizationTarget)c;
			return this.targetTerm.match(rC.targetTerm);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof OptimizationTarget ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
