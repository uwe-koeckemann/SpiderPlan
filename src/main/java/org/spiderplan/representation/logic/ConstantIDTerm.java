/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.representation.logic;

import org.spiderplan.representation.expressions.domain.Substitution;

/**
 * 
 *  
 * @author Uwe Köckemann
 *
 */
public class ConstantIDTerm extends Term {

	protected static long nextID = 0;
	
	private Long value;
		
	protected ConstantIDTerm() {
		this.value = nextID++;
	}
	
	@Override
	public Term getArg( int i ) {
		return null;
	}
	
	@Override
	public int getNumArgs() {
		return 0;
	}
	
	@Override
	public String getName() {
		return String.valueOf(value); 
	}
	
	/**
	 * Checks if this {@link ConstantIDTerm} is ground. Ground {@link ConstantIDTerm}s are either constants or complex {@link ConstantIDTerm}s containing no variables.
	 * @return <code>true</code> if {@link ConstantIDTerm} is ground, <code>false</code> otherwise.
	 */
	@Override
	public boolean isGround() {
		return true;
	}
	
	/**
	 * Checks if this {@link ConstantIDTerm} is complex.
	 * @return <code>true</code> if {@link ConstantIDTerm} is complex, <code>false</code> otherwise.
	 */
	@Override
	public boolean isComplex() {
		return false;
	}

	/**
	 * Checks if this {@link ConstantIDTerm} is a variable.
	 * @return <code>true</code> if {@link ConstantIDTerm} is variable, <code>false</code> otherwise.
	 */
	@Override
	public boolean isVariable() {
		return false;
	}

	/**
	 * Checks if this {@link ConstantIDTerm} is a constant.
	 * @return <code>true</code> if {@link ConstantIDTerm} is constant, <code>false</code> otherwise.
	 */
	@Override
	public boolean isConstant() {
		return true;
	}
	
	
	@Override
	public boolean nameEquals( Term t ) {
		return String.valueOf(value).equals(t.getName());
	}
			
	@Override
	public String getPrologStyleString() {
		return value.toString();
	}
	@Override
	public String toString() {
		return "#"+value.toString();
	}
    @Override
	public int hashCode() {
		return value.hashCode()*3;
	}
    @Override
    public boolean equals( Object o ) {  
//    	Profiler.probe(0);
//    	StopWatch.start("ConstantID.equals");
    	if ( this == o ) {
//    		StopWatch.stop("ConstantID.equals");
    		return true;
    	}
    	if ( !(o instanceof ConstantIDTerm) ) {
//    		StopWatch.stop("ConstantID.equals");
    		
    		if ( this.toString().equals(o.toString() )) {
    			throw new IllegalStateException();
    		}
    		
    		return false;
    	}
    	ConstantIDTerm v = (ConstantIDTerm)o;
    	boolean b = v.value.equals(this.value);
//    	StopWatch.stop("ConstantID.equals");
    	return b;
    }
	@Override
	protected Term[] getArgs() {
		return null;
	}
	@Override
	public Term substitute(Substitution theta) {
		return this;
	}
}
