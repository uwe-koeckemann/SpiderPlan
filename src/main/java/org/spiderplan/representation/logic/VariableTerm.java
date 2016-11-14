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

import org.spiderplan.representation.expressions.domain.Substitution;

/**
 * 
 *  
 * @author Uwe Köckemann
 *
 */
public class VariableTerm extends Term {
	private int hashCode;
	private String value;
		
	protected VariableTerm( String name ) {
		this.value = name;
		this.hashCode = this.value.hashCode();
	}

	@Override
	public String getName() { return value; }
	@Override
	public boolean nameEquals( Term t ) { return value.equals(t.getName()); }
	@Override
	public boolean isGround() { return false; }
	@Override
	public boolean isComplex() { return false; }
	@Override
	public boolean isVariable() { return true; }
	@Override
	public boolean isConstant() { return false; }
	@Override
	public int getNumArgs() { return 0; }
	@Override
	protected Term[] getArgs() { return null; }
	@Override
	public Term getArg( int i ) { return null; }

	@Override
	public ArrayList<Term> getVariables() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.add(this);
		return r;
	}
	 		
	@Override
	public String getPrologStyleString() { 
		return value.substring(0, 1).toUpperCase() + value.substring(1); 
	}

	@Override
	public Term substitute(Substitution theta) { 
		Term newTerm = theta.substitute(this);
		if ( newTerm == null ) {
			return this;
		} else {
			return newTerm.substitute(theta);
		}
	}
	
	@Override
	public String toString() { 	return "?"+value; }
    @Override
	public int hashCode() { return this.hashCode; }
    @Override
    public boolean equals( Object o ) {  
//    	Profiler.probe(0);
//    	StopWatch.start("Variable.equals");
    	if ( this == o ) {
//    		StopWatch.stop("Variable.equals");
    		return true;
    	}
    	if ( !(o instanceof VariableTerm) ) {
//    		StopWatch.stop("Variable.equals");
    		return false;
    	}
    	
    	VariableTerm v = (VariableTerm)o;
    	boolean r = v.value.equals(this.value);
//    	StopWatch.stop("Variable.equals");
    	return r;
    }
}
