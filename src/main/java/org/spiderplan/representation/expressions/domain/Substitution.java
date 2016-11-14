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
package org.spiderplan.representation.expressions.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.SimpleParsing;


/**
 * Represents a mapping between {@link Term}s.
 * @author Uwe Köckemann
 *
 */
public class Substitution extends Expression implements Mutable, Assertable {
	private Map<Term,Term> map = new HashMap<Term,Term>();
	private boolean isAsserted = false;
	
	/**
	 * Create new empty substitution
	 */
	public Substitution() {
		super(ExpressionTypes.Domain);
	}
	
	/**
	 * Create a new {@link Substitution} from a {@link String} representation.
	 * Is compatible to {@link Substitution}'s <i>toString()</i>.
	 * <br><br>Example: <i>new Substitution("{ X/a, Y/f(X) }")</i>
	 * @param s {@link String} representation of the {@link Substitution}
	 */
	public Substitution(String s) {
		super(ExpressionTypes.Domain);
		ArrayList<String> tmp = SimpleParsing.complexSplit(s.replace(" ","").replace("{","").replace("}",""), ",");
		for ( int i = 0 ; i < tmp.size(); i++ ) {
			String[] subst = tmp.get(i).split("/");
			this.add(Term.parse(subst[0]), Term.parse(subst[1]));
		}
	}
	
	/**
	 * Apply this {@link Substitution} to {@link Term} <i>t</i>.
	 * @param from The {@link Term} to substitute
	 * @return new {@link Term} if substitute for <i>t</i> exists, <i>t</i> otherwise.
	 */
	public Term substitute( Term from ) {
		return map.get(from);
	}
	
	/**
	 * Apply this {@link Substitution} to {@link String} <i>t</i>.
	 * @param t The {@link String} to be substituted.
	 * @return new {@link String} if substitute for <i>t</i> exists, <i>t</i> otherwise.
	 */
//	public String substitute( String fromStr ) {
//		Term from = new Term(fromStr);
//		Term to = map.get(from);
//		if ( to == null ) {
//			return from.toString();
//		} else {
//			return to.toString();
//		}
//			
//	}
	
	/**
	 * Add a new substitution to mapping providing two {@link Term} for <i>from</i> and <i>to</i>.
	 * Only adds if no loop of substitutions is created.
	 * Overwrites any existing substitution of <i>from</i>.
	 * @param from {@link Term} that will be replaced 
	 * @param to {@link Term} that will be substituted
	 */
	public void add(Term from, Term to) {
		if ( from == null || to == null ) {
			throw new IllegalArgumentException("Cannot add null arguments: " + from + "/" + to);
		}
		if (!addingWouldCauseLoop(from,to)) {
			map.put(from, to);
		}
	}
	
	/**
	 * Add a new substitution to mapping providing two variable {@link Term}s for <i>X</i> and <i>Y</i> 
	 * If <i>X</i> is already substituted by another value <i>Z</i> then we recursively try to
	 * add <i>Y/Z</i> instead.
	 * @param from 
	 * @param to 
	 * @return  <code>true</code> if the substitution remains consistent 
	 */
	public boolean addAndMergeVariables(Term from, Term to) {
		Term toPrev = map.get(from);
		if ( toPrev != null && !compatibleWithOne(from, to, from, map.get(from)) ) {
			
			if ( to.isVariable() && toPrev.isVariable() ) {
				
				Substitution thisCopy = this.copy();
				if ( thisCopy.addAndMergeVariables( toPrev,  to ) ) {
					this.map = thisCopy.map;
					return true;
				}
			
				
			} else {	// Two incompatible constants
				return false;
			}
		} else {
			if ( toPrev != null && toPrev.isConstant() ) {
				this.add(to, toPrev);
				return true;
			} else {
				this.add(from, to);	
			}			
		}
		return true;
	}
	
	/**
	 * Add another {@link Substitution} <i>s</i> to this one, if the two are compatible.
	 * Two {@link Substitution}s are incompatible if they try to substitute the same {@link Term}
	 * with two different {@link Term}s.
	 * @param s The {@link Substitution} to add.
	 * @return <i>true</i> if {@link Substitution} <i>s</i> was compatible and successfully added, <i>false</i> otherwise.
	 */
	public boolean add(Substitution s) {
		if ( s == null ) {
			return false;
		}
			
		for ( Term from : s.map.keySet() ) {
			
			if ( addingWouldCauseLoop(from, s.getMap().get(from)) ) {
				return false;
			}
			
			if ( ! this.compatibleWithAll(from, s.map.get(from)) ) {
				return false;
			}
		} 
		
		for ( Term from : s.map.keySet() ) {
			Term to = s.map.get(from);
			if ( !from.equals(to)) {
				map.put(from, s.map.get(from));
			}
		}
				
		return true;
	}
	
	/**
	 * Check if a substitution would create a circle (e.g., adding X/Y to {Y/X} would do so).
	 * @param from {@link Term} that will be replaced 
	 * @param to {@link Term} that will be substituted
	 * @return <code>true</code> if adding from/to to this substitution would cause a loop
	 */
	public boolean addingWouldCauseLoop(Term from, Term to) {
		Set<Term> cloud = new HashSet<Term>();
		cloud.add(to);
		
		int sizeBefore = -1;
		while ( sizeBefore != cloud.size() ) {
			
			sizeBefore = cloud.size();
			
			Set<Term> addList = new HashSet<Term>();
			for ( Term t : cloud ) {
				addList.add(map.get(t));
			}
			cloud.addAll(addList);
			
			if ( cloud.contains(from) ) {	
				/* 
				 * Path between to and from exists,
				 * so new substitution would create a loop. 
				 */
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove a part of the substitution
	 * @param from {@link Term} to remove from substitutions
	 */
	public void remove( Term from ) {
		this.map.remove(from);
	}
	
//	/**
//	 * Removes trivial mappings where key equals value. Used after adding another {@link Substitution}.
//	 */
//	private void removeTrivial() {
//		ArrayList<Term> removeList = new ArrayList<Term>();
//		for ( Term from : map.keySet() ) {
//			if ( from.equals(map.get(from)) ) {
//				removeList.add(from);
//			} 			
//		}
//		for ( Term k : removeList ) {
//			map.remove(k);
//		}
//	}
	/**
	 * Checks if a <i>from</i> and <i>to</i> {@link Term} can be added to this {@link Substitution} without 
	 * conflicting with any of its existing substitutions. 
	 * @param from {@link Term} that should be replaced by <i>to</i>.
	 * @param to {@link Term} that <i>from</i> is to be replaced with.
	 * @return <i>true</i> if new substitution can be added without conflict, <i>false</i> otherwise.
	 */
	private boolean compatibleWithAll(Term from, Term to) {
		for ( Term fromThis : map.keySet() ) {
			if ( ! this.compatibleWithOne(fromThis,map.get(fromThis),from,to) ) {
				return false;
			}
		}		
		return true;
	}
	
	/**
	 * Checks if a substitution <i>f1/t1</i> is compatible with another substitution <i>f2/t2</i>.
	 * @param f1
	 * @param t1
	 * @param f2
	 * @param t2
	 * @return <i>true</i> if <i>f1</i> != <i>f2</i> or (<i>f1</i> == <i>f2</i> and <i>t1</i> == <i>t2</i> and <i>t1,t2</i> are constants), <i>false<i> otherwise. 
	 */
	private boolean compatibleWithOne(Term f1, Term t1, Term f2, Term t2) {
		if ( f1.equals(f2) ) {
			if ( t1.isConstant() && t2.isConstant() ) {
				if ( ! t1.equals(t2)) {
					return false;
				}
			} else if ( t1.isVariable() && t2.isVariable() ) {
				if ( !t1.equals(t2) ) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Get the map of this substitution
	 * @return the map
	 */
	public Map<Term,Term> getMap() {
		return map;
	}
	
	/**
	 * Check if substitution is empty.
	 * @return <code>true</code> iff this substitution contains no elements	 
	 */ 	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	/**
	 * How many substitutions are contained in <i>map</i>.
	 * @return Size of map.
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * Create a copy of this {@link Substitution}
	 * @return The copy.
	 */
	@Override
	public Substitution copy() {
		Substitution s = new Substitution();
		
		for ( Term from : map.keySet() ) {
			s.map.put(from, map.get(from));
		}
		
		return s;
	}
	
	@Override
	public String toString() {
		if ( size() > 0 ) {
			StringBuilder r = new StringBuilder();
			r.append("{");
			boolean firstIteration = true;
			for ( Term from : map.keySet() ) {
				if ( !firstIteration ) {
					r.append(", ");
				} else {
					firstIteration = false;
				}
				
				r.append(from.toString());
				r.append("/");
				r.append(map.get(from).toString());
			}

			r.append("}");
			return r.toString();
		} else {
			return "{}";
		}
	
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
		return new ArrayList<Atomic>();
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Substitution) {
			Substitution s = (Substitution)o;
			return s.map.equals(this.map);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	@Override
	public boolean isAsserted() {
		return isAsserted;
	}

	@Override
	public Expression setAsserted(boolean asserted) {
		this.setAsserted(true);
		return this;
	}
}
