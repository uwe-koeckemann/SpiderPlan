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
package org.spiderplan.representation.expressions.temporal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Supports queries on features of temporal intervals such as their
 * duration, start- and end-time bounds.
 * Can be used, e.g., to check that an interval has a minimum duration.
 * 
 * Queries are not constraints: They are either satisfied or not and are
 * not propagated to be made consistent. 
 */
public class TemporalIntervalQuery extends Expression implements Matchable, Substitutable, Mutable {
	
	private Atomic r;
	
	/**
	 * Create copy of {@link TemporalIntervalQuery} gC.
	 * @param gC a {@link TemporalIntervalQuery}
	 */
	public TemporalIntervalQuery( TemporalIntervalQuery gC ) {		
		super(ExpressionTypes.Temporal);
		r = gC.r;
	}
	/**
	 * Create a new {@link TemporalIntervalQuery} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 */
	public TemporalIntervalQuery( Atomic l ) {
		super(ExpressionTypes.Temporal);
		ExpressionTypes.TemporalConstraints.assertSupported(l, this.getClass());
		this.r = l;
	}
	
	/**
	 * Create a new {@link TemporalIntervalQuery} from a {@link String}.
	 * @param s a {@link String}
	 */
	public TemporalIntervalQuery(String s) {		
		this(new Atomic(s));
	}

	/**
	 * Get the query.
	 * @return {@link Atomic} representation of query
	 */
	public Atomic getQuery() {
		return r;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(this.r.getVariableTerms());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.r.getGroundTerms());
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
		return r.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		return this;
	}
	
	@Override
	public TemporalIntervalQuery copy() {
		TemporalIntervalQuery c = new TemporalIntervalQuery(this);
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof TemporalIntervalQuery ) {
			TemporalIntervalQuery rC = (TemporalIntervalQuery)c;
			return this.getQuery().match(rC.getQuery());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof TemporalIntervalQuery ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
