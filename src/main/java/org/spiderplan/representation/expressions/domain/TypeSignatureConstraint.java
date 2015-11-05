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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Attach a type signature to a state-variable (and optionally its value). 
 * 
 * @author Uwe Köckemann
 *
 */
public class TypeSignatureConstraint extends Expression implements Mutable {
	
	private Atomic variable = null;
	private Term valueType = null;
	
	/**
	 * Create signature for a state-variable. Each argument of the state-variable 
	 * should be a type (see {@link TypeDomainConstraint}).
	 * @param signature state-variable with types as arguments
	 */
	public TypeSignatureConstraint ( Atomic signature ) {
		super(ExpressionTypes.Domain);
		this.variable = signature;
	}
	
	/**
	 * Create signature a state-variable. Each argument of the state-variable 
	 * should be a type (see {@link TypeDomainConstraint}).
	 * @param signature state-variable with types as arguments
	 * @param valueType type of value
	 */
	public TypeSignatureConstraint ( Atomic signature, Term valueType ) {
		super(ExpressionTypes.Domain);
		this.variable = signature;
		this.valueType = valueType;
	}
	
	/**
	 * Get the signature of the state-variable
	 * @return the signature state-variable
	 */
	public Atomic getVariableSignature() { return variable; }
	/**
	 * Get the value type.
	 * @return type of the value
	 */
	public Term getValueTypeName() { return valueType; }
				
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>(); 
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>(); 
		return r;
	}
	
	@Override
	public Expression copy() {
		TypeSignatureConstraint c = new TypeSignatureConstraint(variable);
		if ( valueType != null ) {
			c.valueType = this.valueType;
		}
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof TypeSignatureConstraint ) {
			TypeSignatureConstraint vC = (TypeSignatureConstraint)o;
			if ( !this.variable.equals(vC.variable) )
				return false;
			if ( this.valueType == null && vC.valueType == null ) 
				return true;
			if ( this.valueType == null && vC.valueType != null ) 
				return false;
			if ( this.valueType != null && vC.valueType == null ) 
				return false;
			if ( !this.valueType.equals(vC.valueType) )
				return false;
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		String r = "(sig " + variable.toString();
		if ( valueType != null ) {
			r += " "  + valueType.toString();
		}
		return r + ")";
	}

}
