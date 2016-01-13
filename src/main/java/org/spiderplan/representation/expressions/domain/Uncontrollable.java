package org.spiderplan.representation.expressions.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.DomainRelation;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;

/**
 * Makes terms uncontrollable by the system. Uncontrollable variables, e.g., cannot be chosen
 * by any solver. As a result solvers are forced to find a solution that works for every possible
 * value of an uncontrollable variable.
 * <p>
 * <b>Note:</b> For intervals the intended meaning is that the underlying flexible interval cannot
 * be chosen by the STP solver. 
 * <b>Note:</b> Supporting uncontrollable variables may require specialized solvers.
 * 
 * @author Uwe Köckemann
 *
 */
public class Uncontrollable extends Expression implements Substitutable {
	private List<Term> terms;
	
	private DomainRelation relation;

	/**
	 * Create a new {@link DomainMemberConstraint} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 */
	public Uncontrollable( Atomic l ) {
		super(ExpressionTypes.Domain);
		relation = ExpressionTypes.DomainConstraints.assertSupported(l, this.getClass());
		
		if ( l.getArg(0).getName().equals("list") ) {
			terms = new ArrayList<Term>();
			for ( int i = 0 ; i < l.getArg(0).getNumArgs() ; i++ ) {
				terms.add(l.getArg(0).getArg(i) );
			}		
		} else {
			String s = l.toString() + " ("+l.getUniqueName()+") not supported. List of supported relations:\n" + ExpressionTypes.DomainConstraints.toString();
			throw new IllegalArgumentException(s);
		}
	}
	
	/**
	 * Create new list of uncontrollable terms.
	 * @param terms Terms that are not controllable by the planner
	 */
	public Uncontrollable( List<Term> terms ) {
		super(ExpressionTypes.Domain);
		relation = ExpressionTypes.DomainRelation.Uncontrollable;
		
		this.terms = new ArrayList<Term>();
		for ( Term t : terms ) {
			this.terms.add(t);
		}		
	}
	
	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public DomainRelation getRelation() {
		return relation;
	}
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		for ( Term t : this.terms ) {
			r.addAll(t.getVariables());
		}
		return r;
	}
	
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Term t : this.terms ) {
			if ( t.isGround() ) {
				r.add(t);
			}
		}
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		return r;		
	}
		
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(uncontrollable (list");
		for ( Term t : terms ) {
			sb.append(" ");
			sb.append(t.toString());
		}
		sb.append("))");
		return sb.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		List<Term> newTerms = new ArrayList<Term>();
		for ( Term t : this.terms ) {
			newTerms.add(t.substitute(theta));
		}
		
		return new Uncontrollable(newTerms);
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof DomainMemberConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
