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
package org.spiderplan.representation.logic;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a first-order logic atomic sentence. 
 * It consists of a predicate name <i>p</i> and a list of {@link Term}s. 
 * Construction is done by providing a string representation of the atomic. 
 * <br><br>
 * Example: adjacent(a,b)
 * <br><br>
 * Provides support for matching complex {@link Term}s, substitution and making terms unique by adding an ID to every {@link Term}. 
 * @author Uwe Köckemann
 *
 */
public class Atomic {
	String p;
	Term[] terms;
	
	private String string;
	private String uniqueName = null;
	
//	private boolean isGround;
			
	public Atomic(String s) {		
		if ( s.contains("(") && !s.startsWith("(") ) {
			System.out.println("Old format. This should not happen!");
		}
		
		ArrayList<String> terms = new ArrayList<String>();
		if ( s.contains("(") ) {
			String term = "";
			int depth = 0;
			for ( int i = 0 ; i < s.length() ; i++ ) {
				String currentChar = s.substring(i, i+1);
				if ( currentChar.equals("(") ) {
					depth++;
					if ( depth > 1 )
						term += currentChar;
				} else if  ( currentChar.equals(")") ) {
					depth--;
					if ( depth > 1 )
						term += currentChar;
				} else if  ( currentChar.equals(" ") && depth == 1 ) {
					if ( !term.equals("") ) {
						terms.add(term);
						term = "";
					}
				} else {
					term += currentChar;
				}
			}
			
			if ( !term.equals("") ) {
				terms.add(term);
			}
			this.p = terms.get(0);
			terms.remove(0);
			
		} else {
			this.p = s;
		}
		String []termList = terms.toArray(new String[terms.size()]);
		this.terms = new Term[termList.length];
		for (int i = 0; i < termList.length; i++) {
			this.terms[i] = Term.parse(termList[i]);
		}
		this.update();
		this.uniqueName = this.p + "/" + this.getNumArgs();
	}
	
	public Atomic( String name, Term... terms ) {
		this.p = name;
		this.terms = terms;
		if ( this.terms != null )
			this.uniqueName = this.p + "/" + this.terms.length;
		else
			this.uniqueName = this.p + "/0";
		this.update();
	}
		
	/**
	 * Matches this to another {@link Atomic}
	 * @param a {@link Atomic} to be matched to <i>this</i>
	 * @return {@link Substitution} that will make <it>this</it> and <i>a</i> equal. <i>null</i> if the two {@link Term}s can not be matched.
	 */
	public Substitution match(Atomic a) {
		Substitution theta = new Substitution();
		if ( this.p.equals(a.p) && this.getNumArgs() == a.getNumArgs() ) {
			Substitution theta1;
			for (int i = 0; i < this.terms.length; i++) {
				theta1 = this.terms[i].match(a.terms[i]);
				if ( theta1 != null ) {
					theta.add(theta1);
				} else {
					return null;
				}
			}
			
		} else {
			return null;
		}
		return theta;
	}

	/**
	 * Checks if this {@link Atomic} is ground.
	 * @return <i>true</i> if all its {@link Term}s are ground, <i>false</i> otherwise.
	 */
	public boolean isGround() {
		for ( Term t : terms ) {
			if ( !t.isGround() ) {
				return false;
			}
		}
		return true;
//		return isGround;
	}

	/**
	 * Returns arity of this {@link Atomic} as an integer.
	 * @return The arity.
	 */
	public int getNumArgs() {
		return terms.length;
	}

	/**
	 * Returns predicate name of this {@link Atomic}
	 * @return The name.
	 */
	public String name() {
		return this.p;
	}
	
	public Term getArg( int i ) {
		return terms[i];
	}
	
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Term t: terms ) {
			r.addAll( t.getVariables() );
		}
		return r;		
	}
	
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Term t: terms ) {
			if ( t.isGround() )
				r.add( t );
		}
		return r;		
	}	
			
	public String getUniqueName() {
		return uniqueName; //this.p + "/" + this.arity();
	}

	/**
	 * Substitute all {@link Term}s of this {@link Atomic} by matching substitutions in <i>theta</i>.
	 */
	public Atomic substitute(Substitution theta) {
		if ( theta.isEmpty() ) {
			return this;
		}
		Term[] args = new Term[this.terms.length];
		
		boolean change = false;
		
		for (int i = 0; i < args.length; i++) {
			args[i] = this.terms[i].substitute(theta);
			change = change || !args[i].equals(this.terms[i]);
		}		
		
		if ( !change ) {
			return this;
		}
		return new Atomic(this.name(), args);
	}
			
	protected void update() {
		if ( terms != null && terms.length > 0 ) {
			StringBuilder r = new StringBuilder();
			r.append("(");
			r.append(p);	
			for (int i = 0; i < terms.length; i++) {
				r.append(" ");
				r.append(terms[i].toString());
			}
			r.append(")");
			this.string = r.toString();
		} else {
			this.string = p;
		}
	}
	
	public String getPrologStyleString() {
		StringBuilder r = new StringBuilder();
		
		if ( terms.length > 0 ) {
			boolean first = true;
			r.append(p);
			r.append("(");
			for (int i = 0; i < terms.length; i++) {
				if ( !first ) {
					r.append(",");	
				} else {
					first = false;
				}
				r.append(terms[i].getPrologStyleString());
				
			}
			r.append(")");
			return r.toString();
		} else {
			return p + "()";
		}
	}
		
	@Override
	public String toString() {
		return this.string;
	}
	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o instanceof Atomic ) {
			Atomic a = (Atomic)o;
			if ( !this.p.equals(a.p) ) {
				return false;
			}
			if ( this.terms.length != a.terms.length ) {
				return false;
			}
			for ( int i = 0 ; i < terms.length ; i++ ) {
				if ( !this.terms[i].equals(a.terms[i])) {
					return false;
				}
			}
			return true;	
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return string.hashCode();
	}
}
