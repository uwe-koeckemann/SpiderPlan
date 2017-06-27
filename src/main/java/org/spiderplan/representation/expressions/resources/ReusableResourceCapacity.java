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
package org.spiderplan.representation.expressions.resources;

import java.util.Collection;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.logic.Term;


/**
 * Constraints a state-variable to be a reusable resource.
 * 
 * @author Uwe Köckemann
 */
public class ReusableResourceCapacity extends Expression implements Mutable {
		
	private Term variable;
	private int capacity;
	
	/**
	 * Create a new resource with a specific capacity.
	 * 
	 * @param variable resource state-variable
	 * @param capacity resource capacity
	 */
	public ReusableResourceCapacity( Term variable, int capacity ) {
		super(ExpressionTypes.Resource);
		this.variable = variable;
		this.capacity = capacity;
	}
		
	/**
	 * Get the state-variable of this resource capacity.
	 * @return the state-variable
	 */
	public Term getVariable() { return variable; }
	/**
	 * Get the maximum capacity of this resource
	 * @return the capacity
	 */
	public int getCapacity()    { return capacity; }
	
	@Override
	public boolean isMutable() { return true; }

	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.variable.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		Term.createInteger(capacity).getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public Expression copy() {
		Expression c = new ReusableResourceCapacity(variable, capacity);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof ReusableResourceCapacity ) {
			ReusableResourceCapacity rrCap = (ReusableResourceCapacity)o;
			return this.variable.equals(rrCap.variable) && this.capacity == rrCap.capacity;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "( reusable " + variable + "  " + capacity + " )";
	}
	
}
