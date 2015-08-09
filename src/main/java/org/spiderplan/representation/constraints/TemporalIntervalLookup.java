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
import java.util.Map;

import org.spiderplan.representation.constraints.constraintInterfaces.Unique;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Cost constraints either add or subtract a value from a cost variable
 * or pose inequalities on a cost variable.
 * 
 * @author Uwe Koeckemann
 * 
 */
public class TemporalIntervalLookup extends Constraint implements Unique {
	
	Map<Term,Long[]> bounds;
	
	public TemporalIntervalLookup( Map<Term,Long[]> bounds ) {
		super(ConstraintTypes.Temporal);
		this.bounds = bounds;
	}
	
	public long getEST( Term interval ) {
		return this.bounds.get(interval)[0];
	}
	public long getLST( Term interval ) {
		return this.bounds.get(interval)[1];
	}
	public long getEET( Term interval ) {
		return this.bounds.get(interval)[2];
	}
	public long getLET( Term interval ) {
		return this.bounds.get(interval)[3];
	}
	
	public String getFlexibleIntervalString( Term interval ) {
		return "[["+ this.getEST(interval) + " " + this.getLST(interval) + "] [" + this.getEET(interval) + " " + this.getLET(interval) + "]]"; 
	}

	@Override
	public Collection<Term> getVariableTerms() {
		return new ArrayList<Term>();
	}	
	
	@Override
	public Collection<Term> getGroundTerms() {
		return new ArrayList<Term>();
	}
	
	@Override
	public Collection<Atomic> getAtomics() {
		return new HashSet<Atomic>();
	}
	
	@Override
	public boolean equals(Object o) {
		if ( o instanceof TemporalIntervalLookup ) {
			TemporalIntervalLookup oTI = (TemporalIntervalLookup)o;
			return oTI.bounds.equals(this.bounds); 
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.bounds.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for ( Term k : this.bounds.keySet() ) {
			Long[] bound = this.bounds.get(k);
			sb.append(k.toString());
			sb.append(" [");
			sb.append(bound[0]);
			sb.append(" ");
			sb.append(bound[1]);
			sb.append("] [");
			sb.append(bound[2]);
			sb.append(" ");			
			sb.append(bound[3]);
			sb.append("]\n");
		}
		
		return sb.toString();
	}
}
