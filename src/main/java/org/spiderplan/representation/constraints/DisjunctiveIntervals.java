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
//package org.spiderplan.representation.constraints;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.spiderplan.representation.logic.Atomic;
//import org.spiderplan.representation.logic.Substitutable;
//import org.spiderplan.representation.logic.Substitution;
//import org.spiderplan.representation.logic.Term;
//
//
///**
// * 
// * 
// * @author Uwe Köckemann
// *
// */
//public class DisjunctiveIntervals extends Constraint implements Substitutable {
//
//	public enum Type { Or, Xor };
//	
//	private Type type;	
//	private ArrayList<Term> intervals;
//	
//	public DisjunctiveIntervals( Type disjType, ArrayList<Term> intervals ) {
//		this.type = disjType;
//		this.intervals = intervals;
//	}
//	
//	public ArrayList<Term> getIntervals() {		
//		return this.intervals;
//	}
//	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( Term t : intervals ) {
//				r.addAll(t.getVariables());
//		}
//		return r;		
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( Term t : intervals ) {
//			if ( t.isGround() )
//				r.add(t);
//		}
//		return r;		
//	}
//	@Override
//	public Collection<Atomic> getAtomics() {
//		Set<Atomic> r = new HashSet<Atomic>();
//		return r;
//	}
//	
//	@Override
//	public void substitute(Substitution theta) {
//		for ( Term t : this.intervals ) {
//			t.substitute(theta);
//		}
//	}
//
//	@Override
//	public Constraint copy() {
//		ArrayList<Term> intervals = new ArrayList<Term>();
//		for ( Term t : this.intervals ) {
//			intervals.add(t.copy());
//		}
//		Constraint c = new DisjunctiveIntervals(this.type, intervals);
//		c.setAsserted(this.isAsserted());
//		c.setDescription(this.getDescription());
//		return c;
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if ( o instanceof DisjunctiveIntervals ) {
//			DisjunctiveIntervals dC = (DisjunctiveIntervals)o;
//			return this.intervals.equals(dC.intervals);
//		}
//		
//		
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return intervals.hashCode();
//	}
//
//	@Override
//	public String toString() {
//		String r = this.type.toString() + "(";
//		r += intervals.get(0);
//		for ( int i = 1 ; i < this.intervals.size() ; i++ ) {
//			r += "," + intervals.get(i);
//		}
//		return r + ")";
//	}
//	
//	
//
//}
