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
package org.spiderplan.representation.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Marks a key to be ignored by any planner. 
 * Can be used to have more complex goals that require statements
 * that are not achieved by the planner but instead just used to
 * create temporal constraints.
 * 
 * @author Uwe Köckemann
 *
 */
public class IgnoredByCausalReasoner extends Constraint implements Mutable {
	
	final private static Term ConstraintType = Term.createConstant("ignored");

	private Term key;
	
	public IgnoredByCausalReasoner( Term key ) {
		super(ConstraintType);
		this.key = key;
	}
	
	public Term getKey() { return this.key; };
	
	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(key.getVariables());
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		if ( key.isGround() ) 
			r.add(key);
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		return r;
	}
	
	@Override
	public Constraint copy() {
		Constraint c = new IgnoredByCausalReasoner(key);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof IgnoredByCausalReasoner ) {
			IgnoredByCausalReasoner ignored = (IgnoredByCausalReasoner)o;
			return ignored.key.equals(this.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 17 + key.hashCode();
	}

	@Override
	public String toString() {
		return "_ignoredByCausalReasoner("+key+")";
	}

}
