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

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.tools.UniqueID;


/*
 * Sequence of actions, where an action is an operator name 
 * and a substitution.
 * 
 * Better than using sequence of ground operators, because
 * substitution works the same on all operators. This means
 * the plan can be understood by any other representation easily. 
 */
public class SequentialPlan extends OrderedPlan {
	private ArrayList<Atomic> names = new ArrayList<Atomic>();
	private ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
	private ArrayList<Integer> uniqueKeys = new ArrayList<Integer>();
	

	
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
		
	@Override
	public void add(Atomic name, Substitution theta) {
		Integer ID = new Integer((int)UniqueID.getID());
		this.add(name, theta, ID);
	}

	@Override
	public void add(int i, Atomic name, Substitution theta) {
		Integer ID = new Integer((int)UniqueID.getID());
		this.add(i, name, theta, ID);
	}
	
	@Override 
	public void add(Atomic name, Substitution theta, Integer ID) {
		this.names.add(name);
		this.substitutions.add(theta);
		this.uniqueKeys.add(ID);		
	}
  
	@Override
	public void add(int i, Atomic name, Substitution theta, Integer ID) {
		this.names.add(i,name);
		this.substitutions.add(i,theta);
		this.uniqueKeys.add(i,ID);
	}
	
	@Override
	public void remove( int i ) {
		this.names.remove(i);
		this.substitutions.remove(i);
		this.uniqueKeys.remove(i);
	}
	
	@Override
	public void remove( int i, int j ) {
		this.names.remove(i);
		this.substitutions.remove(i);
		this.uniqueKeys.remove(i);
	}

	@Override
	public ArrayList<String> getNames(int i) {
		ArrayList<String> r = new ArrayList<String>();
		r.add(this.names.get(i).name());
		return r;
	}

	@Override
	public ArrayList<Substitution> getSubstitutions(int i) {
		ArrayList<Substitution> r = new ArrayList<Substitution>();
		r.add(this.substitutions.get(i));
		return r;
	}

	@Override
	public ArrayList<Integer> getUniqueIDs(int i) {
		ArrayList<Integer> r = new ArrayList<Integer>();
		r.add(this.uniqueKeys.get(i));
		return r;
	}
	
	public void setUniqueID( int i, int j, Integer key ) {
		if ( j == 0 ) {
			this.uniqueKeys.remove(i);
			this.uniqueKeys.add(i, key);
		}
	}
	
	public boolean matches( SequentialPlan p ) {
		if ( this.length() != p.length() ) {
			return false;
		}
		for ( int i = 0 ; i < p.length() ; i++ ) {
			Atomic a1 = this.getAtomic(i, 0);
			Atomic a2 = p.getAtomic(i, 0);
			Substitution t1 = this.getSubstitution(i, 0);
			Substitution t2 = p.getSubstitution(i, 0);
			
			a1 = a1.substitute(t1);
			a2 = a2.substitute(t2);
			
			if ( a1.match(a2) == null) {
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
	 * @return
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
	 * @return
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

	@Override
	public String getName(int i, int j) {
		if ( j == 0 )
			return this.names.get(i).name();
		else
			return null;
	}

	@Override
	public Substitution getSubstitution(int i, int j) {
		if ( j == 0 )
			return this.substitutions.get(i);
		else
			return null;
	}

	@Override
	public Integer getUniqueID(int i, int j) {
		if ( j == 0 )
			return this.uniqueKeys.get(i);
		else
			return null;
	}
	
	@Override
	public int length() {
		return this.names.size();
	}

	@Override
	public int size(int i) {
		if ( i < this.length() )
			return 1;
		else
			return 0;
	}
	
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
	

	
	@Override
	public SequentialPlan copy() {
		SequentialPlan c = new SequentialPlan();
		
		for ( int i = 0 ; i < names.size() ; i++ ) {
			c.names.add( new Atomic(this.names.get(i).toString()) );
			c.substitutions.add(this.substitutions.get(i).copy());
			c.uniqueKeys.add( new Integer( this.uniqueKeys.get(i)));
		}
		
		return c;
	}

	@Override
	public boolean contains(int i, Atomic name )  {
		Atomic oName = this.getAtomic(i, 0);
		return oName.toString().equals(name.toString());
	}

	@Override
	public ArrayList<Atomic> getAtomics(int i) {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
		r.add(this.names.get(i));
		return r;
	}
	@Override
	public SequentialPlan getSequentialPlan() {
		return this;
	}

	@Override
	public Atomic getAtomic(int i, int j) {
		if ( j == 0 )
			return this.names.get(i);
		else 
			return null;
	}
	
	@Override
	public boolean isEmpty() {
		for ( int i = 0 ; i < this.length() ; i++ ) {
			if ( this.size(i) > 0 ) {
				return false;
			}
		}
		return true;
	}
}
