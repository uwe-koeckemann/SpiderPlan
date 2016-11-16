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
package org.spiderplan.representation.expressions;

import java.util.Collection;

import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Superclass to all constraints. The only information kept in this (mostly) abstract class
 * is a flag that is set when a constraint is asserted.
 * An asserted constraint can not be violated (unless other 
 * constraints are removed) and can thus be treated as knowledge.
 * <p>
 * Example: An {@link AllenConstraint} <i>A before B</i> in non-asserted state is
 * a requirement. Once it is propagated it becomes asserted and
 * can thus be treated as given.
 * <p>
 * Note: Constraints should only be removed if it can be assured that constraints
 * that depend on them are set to non-asserted. This is possible, for instance,
 * when {@link AllenConstraint}s are added to resolve resource conflicts, but
 * removed again before another action is added by the planner so that the scheduler
 * does not need to commit to a certain resolver.  
 * <p>
 * Often a sub-class to this class implements 
 * {@link Substitutable} and/or {@link Matchable}
 * 
 * @author Uwe Köckemann
 *
 */
public abstract class Expression {
	private final Term type;
//	private List<ExpressionTag> tags = new ArrayList<ExpressionTag>();
	
	/**
	 * Create new constraint by providing its type.
	 * @param type A {@link Term} representing the type.
	 */
	public Expression( Term type ) { this.type = type; };

	/**
	 * Get the type of a constraint.
	 * @return A {@link Term} representing the type of constraint.
	 */
	public Term getType() { return type; }
	
	/**
	 * Get all variable {@link Term}s in this {@link Expression}.
	 * @return all variable terms used in this constraint
	 */
	public abstract Collection<Term> getVariableTerms();
	/**
	 * Get all ground {@link Term}s in this {@link Expression}.
	 * @return all ground terms used in this constraint
	 */
	public abstract Collection<Term> getGroundTerms();
	/**
	 * Get all atomics used in this constraint.
	 * @return all atomics used in this constraint
	 */
	public abstract Collection<Atomic> getAtomics();
		
	/**
	 * Some constraints are self-sufficient to determine consistency and can overwrite
	 * this function to decide whether or not they are satisfied.
	 * @return <i>true</i> iff the constraint is satisfied.
	 */
	public boolean isConsistent() {
		throw new IllegalAccessError("Not supported by this class. Check support with ");
	}
	
	/**
	 * Returns Check if this constraints can be asserted.
	 * @return <code>true</code> if the constraint can be asserted, <code>false</code> otherwise
	 */
	public boolean isAssertable() { return false; }
	/**
	 * Check if this constraint can be matched against other constraints.
	 * @return <code>true</code> if the constraint is matchable, <code>false</code> otherwise
	 */
	public boolean isMatchable() { return false; }
	/**
	 * Check if this constraints can be changed after it was constructed.
	 * @return <code>true</code> if the constraint is mutable, <code>false</code> otherwise
	 */
	public boolean isMutable() { return false; }
	/**
	 * Check if this is a unique constraint (i.e., there can only be one object of this constraint in a constraint database).
	 * @return <code>true</code> if the constraint is unique, <code>false</code> otherwise
	 */
	public boolean isUnique() { return false; }
	/**
	 * Check if multiple equal instances of this constraint can be added to a constraint database.
	 * @return <code>true</code> if the constraint is repeatable, <code>false</code> otherwise
	 */
	public boolean isRepeatable() { return false; }
	/**
	 * Check if this constraint can be substituted.
	 * @return <code>true</code> if the constraint is substitutable, <code>false</code> otherwise
	 */
	public boolean isSubstitutable() { return false; }
	
	@Override
	public abstract boolean equals( Object o );
	
	@Override
	public abstract int hashCode( );
	
	@Override
	public abstract String toString();
	
}
