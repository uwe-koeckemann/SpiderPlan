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
import java.util.Map;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Unique;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Special constraint that contains the propagated bounds of temporal intervals.
 * It makes this information available to other solvers without the need to repeat 
 * propagation. 
 * <p>
 * <b>Note:</b> Implements {@link Unique} interface so adding more than one instance of this constraint to a {@link ConstraintDatabase} 
 * will overwrite the existing one. 
 * 
 * @author Uwe Köckemann
 * 
 */
public class TemporalIntervalLookup extends Expression implements Unique {
	
	Map<Term,Long[]> bounds;
	
	/**
	 * Create a new lookup from a map.
	 * @param bounds map from interval terms to bound vectors with earliest and latest start and end times.
	 */
	public TemporalIntervalLookup( Map<Term,Long[]> bounds ) {
		super(ExpressionTypes.Temporal);
		this.bounds = bounds;
	}
	
	/**
	 * Get earliest start time (EST) of an interval.
	 * @param interval
	 * @return EST
	 */
	public long getEST( Term interval ) {
		return this.bounds.get(interval)[0];
	}
	/**
	 * Get latest start time (LST) of an interval.
	 * @param interval
	 * @return LST
	 */
	public long getLST( Term interval ) {
		return this.bounds.get(interval)[1];
	}
	/**
	 * Get earliest end time (EET) of an interval.
	 * @param interval
	 * @return EET
	 */
	public long getEET( Term interval ) {
		return this.bounds.get(interval)[2];
	}
	/**
	 * Get latest end time (LET) of an interval.
	 * @param interval
	 * @return LET
	 */
	public long getLET( Term interval ) {
		return this.bounds.get(interval)[3];
	}
	
//	public String getFlexibleIntervalString( Term interval ) {
//		return "[["+ this.getEST(interval) + " " + this.getLST(interval) + "] [" + this.getEET(interval) + " " + this.getLET(interval) + "]]"; 
//	}

	@Override
	public boolean isUnique() { return true; }
	
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
