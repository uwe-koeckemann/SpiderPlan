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
package org.spiderplan.representation.expressions;

import java.util.ArrayList;
import java.util.List;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


// TODO: make generic to return always the right symbolic type
// TODO: rework entries to use private class
// TODO: allow alternative names for constraints
// TODO: domain factory using default constructors based on Term
/**
 * @author Uwe Köckemann
 *
 * @param <T>
 */
public class SupportedExpressions<T> {
	
	private Term constraintType;
	
	private List<SupportedExpression<T>> supported = new ArrayList<SupportedExpression<T>>();
	private String generalHelpText = null;
	
	/**
	 * Create supported expressions for a constraint type.
	 * @param constraintType The type of constraint of these expressions 
	 */
	public SupportedExpressions( Term constraintType ) {
		this.constraintType = constraintType;
	}
		
	/**
	 * Add a new expression.
	 * @param uniqueName string of the form <code>name/arity</code>
	 * @param exampleUsage example usage of this expression
	 * @param helpText explanation of the meaning of this expression 
	 * @param relation internally used symbol to represent this expression
	 * @param c class that is used to represent objects of this expression
	 */
	public void add( String uniqueName, String exampleUsage, String helpText, T relation, Class<? extends Expression> c ) {
		supported.add(new SupportedExpression<T>(uniqueName, exampleUsage, helpText, relation, c));
	}
	
	/**
	 * Add alternative name for some relation.
	 * @param relation 
	 * @param name
	 */
	public void addAlias( T relation, String name ) {
		for ( SupportedExpression<T> entry : supported ) {
			if ( entry.getRelation().equals(relation) ) {
				entry.addName(name);
				return;
			}
		}
	}
	
	/**
	 * Set a help text for this type of constraint.
	 * @param s a help text
	 */
	public void setGeneralHelpText( String s ) {
		this.generalHelpText = s;
	}
		
	/**
	 * Check if {@link Atomic} <code>a</code> represents a supported expression for class <code>c</code>.
	 * @param a an atomic representing a constraint
	 * @param c a class
	 * @return relation used by atomic a
	 */
	public T assertSupported( Atomic a, Class<? extends Expression> c ) {
		for ( SupportedExpression<T> entry : supported ) {
			if ( entry.getNames().contains(a.getUniqueName()) && entry.getImplementingClass().equals(c) ) {
				
//					a.getUniqueName().equals(entry.get(0)) && simpleClassName.equals(entry.get(4)) ) {
				return entry.getRelation();
			}
		}
		String s = a.toString() + " ("+a.getUniqueName()+") not supported. List of supported relations:\n" + this.toString();
		throw new IllegalArgumentException(s);
	}	
	
	SupportedExpression<T> getSupportedEntry( String uniquename ) {
		for ( SupportedExpression<T> entry : this.supported ) {
			if ( entry.getNames().contains(uniquename) ) {
				return entry;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sB = new StringBuilder(); 
		sB.append("==========================================\n");
		sB.append("Constraint type: ");
		sB.append(this.constraintType.toString());
		sB.append("\n==========================================\n");
		if ( generalHelpText != null ) {
			sB.append(generalHelpText); 
			sB.append("\n==========================================\n");
		}
		for (  SupportedExpression<T> entry : supported ) {
			sB.append(entry.toString());
		}
		return sB.toString();
	}
}
