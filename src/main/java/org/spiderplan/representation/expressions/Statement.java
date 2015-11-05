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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.SimpleParsing;


/**
 * Relates a state-variable assignment to a temporal interval.
 * 
 * @author Uwe Köckemann
 */
public class Statement extends Expression implements Substitutable {
	
	Term intervalKey;
	Atomic variable;
	Term value;
		
	/**
	 * Assigns the key {@link Term} representing the interval for
	 * which this {@link Statement} holds. 
	 * 
	 * @param interval {@link Term} representing a temporal interval.
	 * @param x the state-variable
	 * @param v the assigned value
	 */
	public Statement( Term interval , Atomic x, Term v ) {
		super(ExpressionTypes.Statement);
		this.intervalKey = interval;
		this.variable = x;
		this.value = v;
	};
	
	/**
	 * Parsing constructor that takes a string of the form <code>(I x v)</code>.
	 * @param s string to be parsed
	 */
	public Statement( String s ) {
		super(ExpressionTypes.Statement);
		this.intervalKey = Term.parse(SimpleParsing.complexSplit(s.substring(1, s.length()-1), " ").get(0));		
		ArrayList<String> tmp = SimpleParsing.complexSplit(s.substring(1, s.length()-1), " ");
		this.variable = new Atomic(tmp.get(1));
		this.value = Term.parse(tmp.get(2));		
	}
	
	/**
	 * The value {@link Term} that is assigned to the interval by this {@link Statement}.
	 * @return The value {@link Term}.
	 */
	public Term getValue() {
		return value;
	}


	/**
	 * Returns the variable that is assigned in this {@link Statement}
	 * @return The variable of this {@link Statement}
	 */
	public Atomic getVariable() {
		return variable;	
	}
	
	/**
	 * Get the key {@link Term} representing the temporal interval for
	 * which this {@link Statement} holds. 
	 * @return A {@link Term} that represents the interval key.
	 */
	public Term getKey() {	return intervalKey;	}
	
	/**
	 * Checks if all components of this {@link Statement} are ground.
	 * @return Returns <i>true</i> when key {@link Term}, variable and value {@link Term} are ground and <i>false</i> otherwise.
	 */
	public boolean isGround() {
		return this.getKey().isGround() && this.getVariable().isGround() && this.getValue().isGround();
	}
	
	
	/**
	 * Match this {@link Statement} to another one and return a {@link Substitution}
	 * which makes them equal.
	 * @param s {@link Statement} to match against
	 * @return A {@link Substitution} which makes <i>this</i> and <i>s</i> equal.
	 */
	public Substitution match( Statement s ) {
		Substitution theta = this.intervalKey.match(s.intervalKey);
		if ( theta == null || !theta.add( this.getVariable().match(s.getVariable())) ) {
			return null;
		}
		if ( theta == null || !theta.add( this.getValue().match(s.getValue())) ) {
			return null;
		}
		
		return theta;
	}
	
	/**
	 * Create unifier with s ignoring the label. Allows to unify with temporal EQUALS constraint
	 * between two labels, making the interval the same.
	 * @param s The statement to unify with.
	 * @return Substitution that unifies variable and value of this with s. 
	 */
	public Substitution matchWithoutKey( Statement s ) {
		Substitution theta = this.getVariable().match(s.getVariable());
		
		if ( theta == null || !theta.add( this.getValue().match(s.getValue())) ) {
			return null;
		}
		
		return theta;
	}
	
	/**
	 * Create unifier with s if s matches but return only substitution for the label.
	 * @param s The statement to unify with.
	 * @return Substitution that unifies variable and value of this with s. 
	 */
	public Substitution matchOnlyKey( Statement s ) {
		Substitution theta = this.getVariable().match(s.getVariable());
		if (  theta == null || !theta.add( this.getValue().match(s.getValue())) ) {
			return null;
		}		
		return this.intervalKey.match(s.intervalKey);
	}
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(this.getKey().getVariables() );
		r.addAll(this.variable.getVariableTerms());
		r.addAll(this.getValue().getVariables() );
		return r;
	}

	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>(); 
		if ( this.getKey().isGround() )
			r.add(this.getKey());
		r.addAll(this.variable.getGroundTerms());
		if ( this.getValue().isGround() )
			r.add(this.getValue() );
		return r;
	}
	
	
		
	@Override
	public Statement substitute( Substitution theta ) {
		return new Statement(intervalKey.substitute(theta), this.variable.substitute(theta), this.value.substitute(theta));
	}
		
	@Override
	public String toString() {
		return "("+this.intervalKey + " " + variable.toString() + " " + value + ")";
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;			
		}
		
		if ( !(o instanceof Statement) ) {
			return false;
		}
		Statement s = (Statement)o;
		
		if ( !this.intervalKey.equals(s.intervalKey) ) {
			return false;
		}
		
		if ( !this.value.equals(s.value) ) {
			return false;
		}
		
		if ( !this.variable.equals(s.variable) ) {
			return false;
		}		
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.intervalKey.hashCode() + 7*this.variable.hashCode() + 17*this.value.hashCode();
	}

	@Override
	public Collection<Atomic> getAtomics() {
		Collection<Atomic> r = new ArrayList<>();
		r.add(this.variable);
		return r;
	}	
}
