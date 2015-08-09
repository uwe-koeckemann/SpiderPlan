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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Simple {@link Constraint} that limits the possible assignments to a variable {@link Term}.
 * Unlike more complex constraints consistency of {@link VariableDomainRestriction}s can
 * be checked independent of others by calling the method isConsistent().
 * 
 * @author Uwe Köckemann
 *
 */
public class VariableDomainRestriction extends Constraint implements Substitutable, Mutable {
			
	private Term var;
	private ArrayList<Term> D = new ArrayList<Term>();
	private Relation rel;
	
	/**
	 * Relation between variable-term and domain.
	 * @author Uwe Köckemann
	 */
	public enum Relation { 
		/**
		 * Variable-term must be assigned a value in given domain.
		 */
		In, 
		/**
		 * Variable-term must be assigned a value not in given domain.
		 */
		NotIn 
	};
	
	public VariableDomainRestriction( Relation rel, Term var, Collection<Term> D ) {
		super(ConstraintTypes.Domain);
		this.rel = rel;
		this.var = var;
		this.D.addAll(D);
	}
	
	public Collection<Term> getDomain() { return D; };
	public Term getVariable() { return var; }
	
	public boolean appliesTo ( Term v ) {
		return this.var.equals(v);
	}
	
	/**
	 * Checks if variable <i>var</i> is/can be in domain <i>D</i>.
	 * @return
	 */
	@Override
	public boolean isConsistent() {
		if ( var.isGround()  && groundDomain() ) {
			return D.contains(var);
		} else if ( !var.isGround() ) {
			return true;
		} 
		return true;
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(var.getVariables());
		for ( Term t : D ) {
			r.addAll(t.getVariables());
		}
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>(); 
		if ( var.isGround() )
			r.add(var);
		for ( Term t : D ) {
			if ( t.isGround() )
				r.addAll(t.getVariables());
		}
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>(); 
		return r;
	}
	
	private boolean groundDomain() {
		for ( Term val : D ) {
			if ( !val.isGround() ) {
				return false;
			}
		}
		return true;
	}	

	@Override
	public Constraint copy() {
		Term varCopy = this.var;
		HashSet<Term> Dcopy = new HashSet<Term>();
		
		Dcopy.addAll(D);
				
		Constraint c = new VariableDomainRestriction(this.rel, varCopy, Dcopy);
		return c;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof VariableDomainRestriction ) {
			VariableDomainRestriction vdR = (VariableDomainRestriction)o;
			return vdR.var.equals(this.var) && vdR.D.equals(this.D);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (31 + var.hashCode() + (17 * D.hashCode()));   
	}

	@Override
	public String toString() {
		return  "( " + rel + " " + var + " " + D.toString().replace("[", "{").replace("]", "}").replace(",", "") + " )";
	}

	@Override
	public Constraint substitute(Substitution theta) {
		var = var.substitute(theta);
		for ( int i = 0 ; i < D.size() ; i++ ) {
			D.set(i, D.get(i).substitute(theta));
		}
		return this;
	};
}
