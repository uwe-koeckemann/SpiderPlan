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

/**
 * {@link ComplexTerm}s are symbols that represent objects. A {@link ComplexTerm} can be a constant 
 * (e.g. <code>robot</code>) a variable (e.g. <code>X</code>) or complex (e.g. <code>storage(robot)</code>, <code>storage(R)</code>).
 * Variables and constants are distinguished by the case of the first character in their <code>name</code>.
 * To influence which case represents constants/variables change the static flag <code>upperCaseVariables</code> accordingly. 
 * @author Uwe Köckemann
 *
 */
public class ComplexTerm extends Term {
	private boolean isGround;
	
	protected String value;
	private Term[] args = null;
	
	private String string;
	private int hashCode;
		
	protected ComplexTerm( String name, Term... args ) {
		this.value = name;		
		this.args = args;
		this.isGround = true;
		for ( Term t : args ) {
			if ( !t.isGround() ) {
				this.isGround = false;
			}
		}
		if ( args.length == 0 ) {
			args = null;
		}
		this.createString();
	}
	
		
	@Override
	public Term getArg( int i ) {
		return args[i];
	}
	
	@Override
	public int getNumArgs() {
		return args.length;
	}
	
	@Override
	public String getName() {
		return value; 
	}
		
	/**
	 * Match <code>this</code> {@link ComplexTerm} to another {@link ComplexTerm} <code>a</code>. Returns a {@link Substitution} which makes them
	 * equal or <code>null</code> if such a {@link Substitution} does not exist. If both <code>this</code> and <code>a</code> are variables
	 * the {@link Substitution} will replace <code>this</code> by <code>a</code> when applied.
	 * @param a The {@link ComplexTerm} to match to <code>this</code>.
	 * @return {@link Substitution} that makes both {@link ComplexTerm}s match or <code>null</code> if there is none.
	 */
	public Substitution match( ComplexTerm a ) {
		Substitution theta = new Substitution();
		
		if ( this.isConstant() && a.isConstant() ) {
			if ( this.value.equals(a.value) ) {
				return theta;
			} else {
				return null;
			}
		} else if ( this.isVariable() ) {
			theta.add( this, a );
		} else if ( a.isVariable() ) {
			theta.add( a, this );
		} else if ( args != null && a.isConstant() ) {
			return null;
		} else if ( a.isComplex() && this.isConstant() ) {
			return null;
		} else if  ( args != null && a.args != null ) {
			if ( !this.value.equals(a.value) || this.args.length != a.args.length ) {
				return null;
			} else {
				Substitution theta1;
				for ( int i = 0 ; i < this.args.length ; i++ ) {
					theta1 = this.args[i].match(a.args[i]);
					if ( theta1 == null ) {
						return null;
					} else {
						theta.add(theta1);
					}
				}
			}
		}
			
		return theta;
	}
	
	/**
	 * Checks if this {@link ComplexTerm} is ground. Ground {@link ComplexTerm}s are either constants or complex {@link ComplexTerm}s containing no variables.
	 * @return <code>true</code> if {@link ComplexTerm} is ground, <code>false</code> otherwise.
	 */
	@Override
	public boolean isGround() {
		return isGround;
	}
	
	/**
	 * Checks if this {@link ComplexTerm} is complex.
	 * @return <code>true</code> if {@link ComplexTerm} is complex, <code>false</code> otherwise.
	 */
	@Override
	public boolean isComplex() {
		return this.args != null;
	}

	/**
	 * Checks if this {@link ComplexTerm} is a variable.
	 * @return <code>true</code> if {@link ComplexTerm} is variable, <code>false</code> otherwise.
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**
	 * Checks if this {@link ComplexTerm} is a constant.
	 * @return <code>true</code> if {@link ComplexTerm} is constant, <code>false</code> otherwise.
	 */
	@Override
	public boolean isConstant() {
		return false;
	}
	
	/**
	 * Returns a list of all variable {@link ComplexTerm}s. In case of compex {@link ComplexTerm}s this function is called recursively on all sub-terms.
	 * @return List of all variable {@link ComplexTerm}s.
	 */
	@Override
	public ArrayList<Term> getVariables() {
		ArrayList<Term> r = new ArrayList<Term>();
		
		if ( this.isConstant() ) {
			return r;
		} else if ( this.isVariable() ) {
			r.add(this);
			return r;
		} else if ( args != null && !this.isGround() ) {
			for ( Term t : this.args ) {
				r.addAll(t.getVariables());
			}
		}
		return r;
	}
	 
	@Override
	public String getUniqueName() {
		return this.value + "/" + this.args.length;
	}
	
	public boolean nameEquals( ComplexTerm t ) {
		return value.equals(t.value);
	}
	
//	protected Term copy() {
//		ComplexTerm c = new ComplexTerm(); 
//		c.value = this.value;
//		c.string = this.string;
//		c.isGround = this.isGround;
//		c.hashCode = this.hashCode;
//		
//		if ( args != null ) {
//			c.args = new ComplexTerm[this.args.length];
//			for ( int i = 0; i < args.length; i++ ) {
//				c.args[i] = this.args[i].copy();
//			}
//		}
//		return c;
//	}
	
	@Override
	public String getPrologStyleString() {
		StringBuilder r = new StringBuilder();
		
		if ( args != null ) {
			boolean first = true;
			r.append(value);
			r.append("(");
			for (int i = 0; i < args.length; i++) {
				if ( !first ) {
					r.append(",");	
				} else {
					first = false;
				}
				r.append(args[i].getPrologStyleString());
				
			}
			r.append(")");
			return r.toString();
		} else {
			return value;
		}
	}
	
	private void createString() {
		StringBuilder r = new StringBuilder();
		r.append("(");
		r.append(value);
		for ( int i = 0; i < args.length ; i++) {
			r.append(" ");
			r.append(args[i].toString());
		}
		r.append(")");
		this.string = r.toString();
		
		this.hashCode = this.string.hashCode();
	}
    
	@Override
	public String toString() {
		return string;
	}
	
    @Override
	public int hashCode() {
		return this.hashCode;
	}
    
    @Override
    public boolean equals( Object o ) { 
    	if ( this == o ) {
    		return true;
    	}
    	if ( !(o instanceof ComplexTerm) ) {
    		return false;
    	}
    	ComplexTerm t = (ComplexTerm)o;
    	
    	boolean r = this.string.equals(t.string);
    	return r;
    }

	@Override
	protected Term[] getArgs() {
		return args;
	}

	@Override
	public Term substitute(Substitution theta) {
		Term[] tArgs = new Term[args.length];
		for ( int i = 0 ; i < args.length ; i++ ) {
			tArgs[i] = this.args[i].substitute(theta);
		}
		return new ComplexTerm(value,tArgs);
	}

}
