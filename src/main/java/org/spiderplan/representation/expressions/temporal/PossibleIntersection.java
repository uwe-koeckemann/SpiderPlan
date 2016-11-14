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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Satisfied if there is a possible intersection between given intervals.
 * 
 * @author Uwe Köckemann
 * 
 */
public class PossibleIntersection extends Expression implements Substitutable {

	private ArrayList<Term> intervals = new ArrayList<Term>();

	/**
	 * Create intersection constraint for set of intervals
	 * @param intervals 
	 */
	public PossibleIntersection(Collection<Term> intervals) {
		super(ExpressionTypes.Temporal);
		for (Term t : intervals) {
			if (t.getName().equals("list")) {
				for (int i = 0; i < t.getNumArgs(); i++) {
					this.intervals.add(t.getArg(i));
				}
			} else {
				this.intervals.add(t);
			}
		}
	}

	/**
	 * Get intervals of this intersection.
	 * @return set of intervals of this intersection
	 */
	public Collection<Term> getIntervals() { return Collections.unmodifiableList(intervals); };
	 
	@Override
	public boolean isSubstitutable() { return true; }

	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for (Term t : intervals) {
			if (t.isVariable()) {
				r.add(t);
			}
		}
		return r;
	}

	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for (Term t : intervals) {
			if (t.isGround())
				r.add(t);
		}
		return r;
	}

	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();
	}

//	@Override
//	public Constraint copy() {
//		ArrayList<Term> intervalsCopy = new ArrayList<Term>();
//		for (Term v : this.intervals) {
//			intervalsCopy.add(v);
//		}
//		PossibleIntersection c = new PossibleIntersection();
//		c.intervals = intervalsCopy;
//		return c;
//	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof PossibleIntersection) {
			PossibleIntersection piC = (PossibleIntersection) o;
			return piC.intervals.containsAll(this.intervals)
					&& this.intervals.containsAll(piC.intervals);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "( possible-intersection {"
				+ intervals.toString()
						.substring(1, intervals.toString().length() - 1)
						.replace(",", "") + "} )";
	}

	@Override
	public Expression substitute(Substitution theta) {
		ArrayList<Term> intervalsCopy = new ArrayList<Term>();
		for (int i = 0; i < intervals.size(); i++) {
			intervalsCopy.add(this.intervals.get(i).substitute(theta));
		}
		return new PossibleIntersection(intervalsCopy);
	}
}
