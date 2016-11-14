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
package org.spiderplan.representation.plans;

import java.util.ArrayList;

import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.tools.UniqueID;


/**
 * Sequence of actions, where an action is an operator name 
 * and a substitution.
 * 
 * Better than using sequence of ground operators, because
 * substitution works the same on all operators. This means
 * the plan can be understood by any other representation easily. 
 */
public class SequentialPlan {
	private ArrayList<Atomic> names = new ArrayList<Atomic>();
	private ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
	private ArrayList<Integer> uniqueKeys = new ArrayList<Integer>();
		
	/**
	 * Test if this plan is part of another plan.
	 * @param p a plan
	 * @return <code>true</code> if this plan is contained in the other, <code>false</code> otherwise
	 */
	public boolean isPartOf( SequentialPlan p ) {
		if ( this.length() > p.length() ) {
			return false;
		}
		for ( int i = 0 ; i < this.length() ; i++ ) {
			if ( !this.names.get(i).equals(p.names.get(i))) {
				return false;
			}
			if ( !this.substitutions.get(i).toString().equals(p.substitutions.get(i).toString())) {
				return false;
			}
		}		
		return true;
	}
		
	
	/**
	 * Add an action name and a substitution.
	 * @param name name of the action
	 * @param theta a substitution
	 */
	public void add(Atomic name, Substitution theta) {
		Integer ID = new Integer((int)UniqueID.getID());
		this.add(name, theta, ID);
	}

	private void add(Atomic name, Substitution theta, Integer ID) {
		this.names.add(name);
		this.substitutions.add(theta);
		this.uniqueKeys.add(ID);		
	}
  

//	public boolean matches( SequentialPlan p ) {
//		if ( this.length() != p.length() ) {
//			return false;
//		}
//		for ( int i = 0 ; i < p.length() ; i++ ) {
//			Atomic a1 = this.getAtomic(i, 0);
//			Atomic a2 = p.getAtomic(i, 0);
//			Substitution t1 = this.getSubstitution(i, 0);
//			Substitution t2 = p.getSubstitution(i, 0);
//			
//			a1 = a1.substitute(t1);
//			a2 = a2.substitute(t2);
//			
//			if ( a1.match(a2) == null) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
	 * Determines if plan <code>sub</code> is matching 
	 * a sub-sequence of or equal to this plan.
	 * 
	 * @param sub a plan
	 * @return <code>true</code> if sub matches a sub-sequence of this plan, <code>false</code> otherwise
	 */
	public boolean isMatchingSubPlan( SequentialPlan sub ) {
		if ( sub.names.size() != this.names.size() ) {
			return false;
		}
		
		Substitution theta = new Substitution();
		
		for ( int i = 0 ; i < sub.names.size(); i++ ) {
			Atomic a1 = sub.names.get(i).substitute(theta); 
			Atomic a2 = this.names.get(i).substitute(theta);
						
			Substitution subst = a1.match(a2); 
						
			if ( subst == null ) {
				return false;
			}
			
			if ( !theta.add(subst) ) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * Determines if the {@link Plan} sub is matching 
	 * a sub-sequence of or equal to this {@link Plan}.
	 * 
	 * @param sub
	 * @return <code>true</code> if sub matches a the end of this plan, <code>false</code> otherwise
	 */
	public boolean matchesEndOf( SequentialPlan sub ) {		
		if ( sub.names.size() < this.names.size() ) {
			return false;
		}
		
		Substitution theta = new Substitution();
		
		int subLen = sub.length();
		int thisLen = this.length();
		
		for ( int i = 0 ; i < thisLen; i++ ) {
			Atomic a1 = this.names.get(thisLen-i-1).substitute(theta);
			Atomic a2 = sub.names.get(subLen-i-1).substitute(theta);
						
			Substitution subst = a1.match(a2); 
						
			if ( subst == null ) {
				return false;
			}
			
			if ( !theta.add(subst) ) {
				return false;
			}
			
		}
		return true;
	}

	//@Override
//	public String getName(int i, int j) {
//		if ( j == 0 )
//			return this.names.get(i).name();
//		else
//			return null;
//	}
//
//	//@Override
	/**
	 * Get substitution of the ith action.
	 * @param i index of action
	 * @return substitution associated to that action
	 */
	public Substitution getSubstitution(int i) {
		return this.substitutions.get(i);
	}

	/**
	 * Get the length of this plan
	 * @return the length
	 */
	public int length() {
		return this.names.size();
	}
	
	//@Override
	@Override
	public String toString() {
		String r = "";
		for ( int i = 0 ; i < names.size() ; i++ ) {
			r += (i + ": " + names.get(i) + " -> " + substitutions.get(i).toString() +"\n" );
		}
		if ( r.length() > 0 ) {
			return r.substring(0, r.length()-1);
		} else {
			return "Empty plan";
		}
	}

//	public SequentialPlan copy() {
//		SequentialPlan c = new SequentialPlan();
//		
//		for ( int i = 0 ; i < names.size() ; i++ ) {
//			c.names.add( new Atomic(this.names.get(i).toString()) );
//			c.substitutions.add(this.substitutions.get(i).copy());
//			c.uniqueKeys.add( new Integer( this.uniqueKeys.get(i)));
//		}
//		
//		return c;
//	}
	
	/**
	 * Get name of the ith action
	 * @param i
	 * @return action name
	 */
	public Atomic getAtomic(int i) {
		return this.names.get(i);
	}
	

	/**
	 * Check if this plan is empty.
	 * @return <code>true</code> is the plan contains no actions, <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return this.names.isEmpty();
	}
}
