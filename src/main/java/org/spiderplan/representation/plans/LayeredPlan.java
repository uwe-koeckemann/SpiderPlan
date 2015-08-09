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
import java.util.HashSet;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.tools.UniqueID;


public class LayeredPlan extends OrderedPlan {
	private ArrayList<ArrayList<Atomic>> names = new ArrayList<ArrayList<Atomic>>();
	private ArrayList<ArrayList<Substitution>> substitutions = new ArrayList<ArrayList<Substitution>>();
	private ArrayList<ArrayList<Integer>> uniqueKeys = new ArrayList<ArrayList<Integer>>();
	
	public LayeredPlan( ) {
		
	}
	
	public LayeredPlan( int n ) {
		for ( int i = 0 ; i < n ; i++ ) {
			names.add( new ArrayList<Atomic>() );
			substitutions.add( new ArrayList<Substitution>() );
			uniqueKeys.add( new ArrayList<Integer>() );
		}
	}
	
	@Override
	public void add(Atomic name, Substitution theta) {
		Integer ID = new Integer((int)UniqueID.getID());
		this.add(name, theta, ID);
	}

	@Override
	public void add(Atomic name, Substitution theta, Integer ID) {
		names.add( new ArrayList<Atomic>() );
		names.get(names.size()-1).add(name);
		substitutions.add( new ArrayList<Substitution>() );
		substitutions.get(substitutions.size()-1).add(theta);
		uniqueKeys.add( new ArrayList<Integer>() );
		uniqueKeys.get(uniqueKeys.size()-1).add(ID);
	}

	@Override
	public void add(int i, Atomic name, Substitution theta) {
		Integer ID = new Integer((int)UniqueID.getID());
		this.add(i,name,theta,ID);
	}

	@Override
	public void add(int i, Atomic name, Substitution theta, Integer ID) {
		while ( this.length() <= i ) {
			names.add( new ArrayList<Atomic>() );
			substitutions.add( new ArrayList<Substitution>() );
			uniqueKeys.add( new ArrayList<Integer>() );
		}
		names.get(i).add(name);
		substitutions.get(i).add(theta);
		uniqueKeys.get(i).add(ID);
	}

	@Override
	public boolean contains(int i, Atomic name ) {
		for ( int j = 0 ; j < this.size(i); j++ ) {
			Atomic oName = this.getAtomic(i,j).substitute(this.getSubstitution(i, j));
			if ( oName.equals(name) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void remove(int i) {
		this.names.remove(i);
		this.substitutions.remove(i);
		this.uniqueKeys.remove(i);
	}

	@Override
	public void remove(int i, int j) {
		this.names.get(i).remove(j);
		this.substitutions.get(i).remove(j);
		this.uniqueKeys.get(i).remove(j);
	}

	@Override
	public ArrayList<Atomic> getAtomics(int i) {
		return this.names.get(i);
	}
	
	@Override
	public ArrayList<String> getNames(int i) {
		ArrayList<String> nameList = new ArrayList<String>();
		for ( Atomic a : this.getAtomics(i))
			nameList.add(a.name());
		return nameList;
	}

	@Override
	public ArrayList<Substitution> getSubstitutions(int i) {
		return this.substitutions.get(i);
	}

	@Override
	public ArrayList<Integer> getUniqueIDs(int i) {
		return this.uniqueKeys.get(i);
	}
	
	@Override
	public Atomic getAtomic(int i , int j ) {
		return this.names.get(i).get(j);
	}

	@Override
	public String getName(int i, int j) {
		return this.names.get(i).get(j).name();
	}

	@Override
	public Substitution getSubstitution(int i, int j) {
		return this.substitutions.get(i).get(j);
	}

	@Override
	public Integer getUniqueID(int i, int j) {
		return this.uniqueKeys.get(i).get(j);
	}

	@Override
	public int length() {
		return this.names.size();
	}

	@Override
	public int size(int i) {
		return this.names.get(i).size();
	}
	@Override
	public LayeredPlan copy() {
		LayeredPlan c = new LayeredPlan(this.length());
		
		for ( int i = 0 ; i < names.size() ; i++ ) {
			for ( int j = 0 ; j < names.get(i).size() ; j++ ) {
				c.add(i, new Atomic(this.names.get(i).get(j).toString()), 
						this.substitutions.get(i).get(j).copy(), 
						new Integer( this.uniqueKeys.get(i).get(j)));
			}
		}
		
		return c; 
	}

	@Override
	public SequentialPlan getSequentialPlan() {
		SequentialPlan sPlan = new SequentialPlan();
		for ( int i = 0 ; i < this.length() ; i++ ) {
			for ( int j = 0; j < this.size(i) ; j++ ) {
				sPlan.add(this.getAtomic(i, j), this.getSubstitution(i, j).copy(), new Integer(this.getUniqueID(i,j)) );
			}
		}
		return sPlan;
	}
	
	@Override
	public String toString() {
		String r = "";
		
		for ( int i = 0 ; i < this.length() ; i++ ) {
			r += "["+i+"] ";
			for ( int j = 0 ; j < this.size(i) ; j++ ) {
				r += this.getAtomic(i,j).toString() + " ";
			}
			r += "\n";
		}
		
		if ( r.length() == 0 ) {
			return "EMPTY PLAN";
		}
		
		return r.substring(0, r.length()-1);
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
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof LayeredPlan ) {
			LayeredPlan p = (LayeredPlan)o;
			
			if ( p.length() != this.length() ) {
				return false;
			}
			
			for ( int i = 0 ; i < p.length() ; i++ ) {
				HashSet<Atomic> a = new HashSet<Atomic>();
				HashSet<Atomic> b = new HashSet<Atomic>();
				
				a.addAll( this.getAtomics(i));
				b.addAll( p.getAtomics(i));
				
				if ( !a.equals(b ) ) {
					return false;
				}
			}
			return true;
		}
		return false;	
	}
}
