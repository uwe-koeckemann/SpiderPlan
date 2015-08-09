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
package org.spiderplan.representation.constraints;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


public class TypeSignatureConstraint extends Constraint implements Mutable {
	
	final private static Term ConstraintType = Term.createConstant("domain");
	
	private Atomic variable = null;
	private Term valueType = null;
	
	public TypeSignatureConstraint ( Atomic variable ) {
		super(ConstraintType);
		this.variable = variable;
	}
	
	public TypeSignatureConstraint ( Atomic variable, Term valueType ) {
		super(ConstraintType);
		this.variable = variable;
		this.valueType = valueType;
	}
	
	public Atomic getVariableSignature() { return variable; }
	public Term getValueTypeName() { return valueType; }
		
	/**
	 * Check if this definition applies to {@link Atomic} <i>a</i>.
	 * @param a
	 * @return
	 */
	public boolean applies( Atomic a ) {
		return a.name().equals(this.variable.name()) && a.getNumArgs() == this.variable.getNumArgs();
	}
	
	/**
	 * Get type name of nth argument.
	 * @param n
	 * @return
	 */
	public Term getType( int n ) {
		n = n % (this.variable.getNumArgs()+1);
		
		if ( n < this.variable.getNumArgs() ) {
			return this.variable.getArg(n);
		}
		return this.valueType;
	}
	
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
	public Constraint copy() {
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
