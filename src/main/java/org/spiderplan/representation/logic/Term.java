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
import java.util.LinkedList;

/**
 * {@link Term}s are symbols that represent objects. A {@link Term} can be a constant 
 * (e.g. <code>robot</code>) a variable (e.g. <code>X</code>) or complex (e.g. <code>storage(robot)</code>, <code>storage(R)</code>).
 * Variables and constants are distinguished by the case of the first character in their <code>name</code>.
 * To influence which case represents constants/variables change the static flag <code>upperCaseVariables</code> accordingly. 
 * @author Uwe Köckemann
 *
 */
public abstract class Term {
	
	protected static boolean allowConstantSubstitutions = false;
	
	protected Term() { }
			
	public static Term createInteger( long n ) {
		return new IntegerTerm(n);
	}
	
	public static Term createFloat( double n ) {
		return new FloatTerm(n);
	}
	
	public static Term createVariable( String name ) {
		name = name.replace("?", "");
		Term v =  new VariableTerm(name);
//		poolVariable.add(v);
		return v;
	}
	
	public static Term createConstant( String name ) {		
		Term v =  new ConstantSymbolicTerm(name);
//		poolConstant.add(v);
		return v;
	}
	
//	public static Term createConstantID() {
//		Term v =  new ConstantIDTerm();
////		poolID.add(v);
//		return v;
//	}
	
	public static Term createSymbolic( String name, boolean isVariable ) {
		if ( isVariable ) {
			return Term.createVariable(name);
		} else {
			return Term.createConstant(name);
		}
	}
		
	public static Term createComplex( String name, Term... args ) {
		return new ComplexTerm(name,args);
	}
	
	public static Term parse( String s ) {		
		s = s.replace("{", "(list ").replace("}", ")").replace("  ", " ");
		
		if ( s.contains("(") && !s.startsWith("(") ) {
			System.out.println("Old format. This should not happen!");
		}
		
		String value = s;
		Term[] args = null;
	
		if ( s.contains("(") ) {
			LinkedList<String> terms = new LinkedList<String>();
			int countTerms = 0;
			
			int bracketLevel = 0;
			for ( int i = 0; i < s.length(); i++ ) {
				if ( String.valueOf(s.charAt(i)).equals("(") ) {
					bracketLevel++;
				} else if  ( String.valueOf(s.charAt(i)).equals(")") ) {
					bracketLevel--;
				}
				
				if ( bracketLevel == 1 &&  String.valueOf(s.charAt(i)).equals(" ") ) {
					countTerms++;
				} else if ( bracketLevel >= 1 ) {
					if ( terms.size() <= countTerms ) {
						terms.add("");
					}
					if ( ! (bracketLevel == 1 && String.valueOf(s.charAt(i)).equals("(")) )
						terms.set(countTerms, terms.get(countTerms) + String.valueOf(s.charAt(i)));
				}
			}
			while ( terms.remove("") );
			
			value = terms.get(0);
			terms.remove(0);
			
			if ( terms.size() > 0 ) {
				args = new Term[terms.size()];
				for ( int i = 0 ; i < terms.size(); i++ ) {
					args[i] = Term.parse(terms.get(i));
				}
			}
		} 
		
		if ( args == null ) {
			if ( value.startsWith("?") ) {
				return Term.createVariable(value);
			}
			
			try  {  
				long intValue = Long.parseLong(value);  
				return Term.createInteger(intValue);
			} catch( Exception e1 ) {
				try  {  
					double floatValue = Double.parseDouble( value );  
					return Term.createFloat(floatValue);
				} catch( Exception e2) {}
			} 
			return Term.createConstant(value);
		} else {
			return Term.createComplex(value,args);
		}
	}
	
	public static void setAllowConstantSubstitution( boolean isAllowed ) {
		allowConstantSubstitutions = isAllowed;
	}
		
	public abstract Term getArg( int i );
	public abstract int getNumArgs();	
	public abstract String getName();
	
	protected abstract Term[] getArgs();
		
	/**
	 * Match <code>this</code> {@link Term} to another {@link Term} <code>a</code>. Returns a {@link Substitution} which makes them
	 * equal or <code>null</code> if such a {@link Substitution} does not exist. If both <code>this</code> and <code>a</code> are variables
	 * the {@link Substitution} will replace <code>this</code> by <code>a</code> when applied.
	 * @param a The {@link Term} to match to <code>this</code>.
	 * @return {@link Substitution} that makes both {@link Term}s match or <code>null</code> if there is none.
	 */
	public Substitution match( Term a ) {
		Substitution theta = new Substitution();
		
		if ( this.isConstant() && a.isConstant() ) {			
			if ( this.getName().equals(a.getName()) ) {
				return theta;
			} else {
				return null;
			}
		} else if ( this.isVariable() ) {
			theta.add( this, a );
		} else if ( a.isVariable() ) {
			theta.add( a, this );
		} else if ( this.getArgs() != null && a.isConstant() ) {
			return null;
		} else if ( a.isComplex() && this.isConstant() ) {
			return null;
		} else if  ( this.getArgs() != null && a.getArgs() != null ) {
			if ( !this.getName().equals(a.getName()) || this.getArgs().length != a.getArgs().length ) {
				return null;
			} else {
				Substitution theta1;
				for ( int i = 0 ; i < this.getArgs().length ; i++ ) {
					theta1 = this.getArg(i).match(a.getArg(i));
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
	 * Checks if this {@link Term} is ground. Ground {@link Term}s are either constants or complex {@link Term}s containing no variables.
	 * @return <code>true</code> if {@link Term} is ground, <code>false</code> otherwise.
	 */
	public abstract boolean isGround();
	
	/**
	 * Checks if this {@link Term} is complex.
	 * @return <code>true</code> if {@link Term} is complex, <code>false</code> otherwise.
	 */
	public abstract boolean isComplex();

	/**
	 * Checks if this {@link Term} is a variable.
	 * @return <code>true</code> if {@link Term} is variable, <code>false</code> otherwise.
	 */
	public abstract boolean isVariable();

	/**
	 * Checks if this {@link Term} is a constant.
	 * @return <code>true</code> if {@link Term} is constant, <code>false</code> otherwise.
	 */
	public abstract boolean isConstant();
	
	/**
	 * Returns a list of all variable {@link Term}s. In case of compex {@link Term}s this function is called recursively on all sub-terms.
	 * @return List of all variable {@link Term}s.
	 */
	public abstract ArrayList<Term> getVariables();
	 
	public String getUniqueName() {
		return this.getName() + "/" + this.getNumArgs();
	}
	
	public boolean nameEquals( Term t ) {
		return this.getName().equals(t.getName());
	}
	
	/**
	 * Substitute this {@link Term} with <code>theta</code> if theta is applicable.
	 * If this {@link Term} is complex, recursively substitute its sub-terms. 
	 * @param theta A {@link Substitution}
	 * @return The {@link Term} that results from applying the substitution.
	 */
	public abstract Term substitute( Substitution theta );
		
	public Term makeConstant() {
		if ( this.isVariable() ) {
			return Term.createConstant(this.getName().replace("?", ""));
		}	
		return this;
	}
	
	public Term makeUnique( long ID ) {
		if ( this.isVariable() ) {
			return Term.createVariable(this.getName() + "_" + ID);
		}
		return this;
	}
	
//	protected abstract Term copy();
	
	public String getPrologStyleString() {
		StringBuilder r = new StringBuilder();
		
		if ( this.getArgs() != null ) {
			boolean first = true;
			r.append(this.getName());
			r.append("(");
			for (int i = 0; i < this.getNumArgs(); i++) {
				if ( !first ) {
					r.append(",");	
				} else {
					first = false;
				}
				r.append(this.getArg(i).toString());
				
			}
			r.append(")");
			return r.toString();
		} else {
			return this.getName();
		}
	}
	
	public static void resetPools() {
		ConstantIDTerm.nextID = 0;
//		Term.variablePool.clear();
//		Term.constantPool.clear();
	}
    
	@Override
	public abstract String toString();
	
    @Override
	public abstract int hashCode();
    
    @Override
    public abstract boolean equals( Object o );
}
