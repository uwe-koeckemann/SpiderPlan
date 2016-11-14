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

import java.util.ArrayList;
import java.util.Collection;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

/**
 * Forces a variable to be substituted by a yet unused constant object
 * of a specific type.
 * 
 * @author Uwe Köckemann
 *
 */
public class NewObject extends Expression implements Substitutable {
	
	private Term variable;
	private Term typeName;
		
	/**
	 * Create new object constraint by providing a variable and a type name.
	 * @param variable variable that has to be assigned an unused object
	 * @param typeName type from which to choose new object
	 */
	public NewObject( Term variable, Term typeName ) {
		super(ExpressionTypes.Domain);
		this.variable = variable;
		this.typeName = typeName;
	}
	
	/**
	 * Get the variable.
	 * @return the variable
	 */
	public Term getVariable() { return variable; };
	/**
	 * Get the type from which to choose new object.
	 * @return the type
	 */
	public Term getTypeName() { return typeName; };
		
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(variable.getVariables());
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		if ( variable.isGround() )
			r.addAll(variable.getVariables());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		return new ArrayList<Atomic>();		
	}
			
	@Override
	public boolean equals(Object o) {
		if ( o instanceof NewObject ) {
			NewObject nO = (NewObject)o;
			return nO.variable.equals(this.variable) && nO.typeName.equals(this.typeName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return "( new-object "+variable+" "+typeName+" )";
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new NewObject(variable.substitute(theta), typeName);
	};
}
