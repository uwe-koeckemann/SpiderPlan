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
package org.spiderplan.representation.expressions.minizinc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


/**
 * Allows to provide input to a MiniZinc program. 
 * 
 * @author Uwe Köckemann
 */
public class MiniZincInput extends Expression implements Matchable, Substitutable, Mutable {
	
	private Atomic r;
	private Term programID;
	
	/**
	 * Create copy of {@link MiniZincInput} gC.
	 * @param gC a {@link MiniZincInput}
	 */
	public MiniZincInput( MiniZincInput gC ) {	
		super(ExpressionTypes.MiniZinc);
		this.r = gC.r;
		programID = gC.programID;
	}
	/**
	 * Create a new {@link MiniZincInput} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 * @param programID the program ID used for this constraint (see {@link IncludedProgram})
	 */
	public MiniZincInput( Atomic l, Term programID ) {
		super(ExpressionTypes.MiniZinc);
		this.programID = programID;
		ExpressionTypes.MiniZincExpressions.assertSupported(l, this.getClass());
	}
	
	/**
	 * Get the relational form of this constraint.
	 * @return the relation
	 */
	public Atomic getRelation() {
		return r;
	}
	
	/**
	 * Get the ID of the program associated to this constraint.
	 * @return program ID (see {@link IncludedProgram})
	 */
	public Term getProgramID() {
		return programID;
	}
	
	/**
	 * Returns this constraint as MiniZinc syntax so it can be put in a dzn file (i.e. MiniZinc data input) 
	 * @return A {@link String} representation of this constraint in MiniZinc syntax.
	 */
	public String getMiniZincCode() {
		if ( r.name().equals("assign") ) {
			return r.getArg(0) + " = " + r.getArg(1) + ";";
		} else if  ( r.name().equals("array") ) {
			Term t = r.getArg(1).getArg(0);
			if ( !t.isGround() ) {
				throw new IllegalStateException(this.toString() + " has non-ground term " + t);
			}
			String s = r.getArg(0) + " = [" + addQuotes(t);
			for ( int i = 1; i < r.getArg(1).getNumArgs(); i++ ) {
				t = r.getArg(1).getArg(i);
				if ( !t.isGround() ) {
					throw new IllegalStateException(this.toString() + " has non-ground term " + t);
				}
				s += "," + addQuotes(t);
			}
			return s+"];";
		}
			
		return "";
	}
	
	private String addQuotes( Term t ) {
		try {
			Integer.valueOf(t.toString());
			return t.toString();
		} catch ( NumberFormatException e ) {
			return "\""+t+"\"";
		}
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		r.addAll(this.r.getVariableTerms());
		return r;
	}
	@Override
	public Collection<Term> getGroundTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		r.addAll(this.r.getGroundTerms());
		return r;		
	}
	@Override
	public Collection<Atomic> getAtomics() {
		ArrayList<Atomic> r = new ArrayList<Atomic>();
//		r.add(this.r);
		return r;		
	}
	
	@Override
	public String toString() {
		return r.toString(); // + "[wrt. "+programID+"]";
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		programID = programID.substitute(theta);
		return this;
	}
	
	@Override
	public MiniZincInput copy() {
		MiniZincInput c = new MiniZincInput(this);
		return c;
	}

	@Override
	public boolean isGround() {
		return r.isGround();
	}

//	@Override
//	public Collection<Term> getTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( Term t : this.r.getTerms() ) {
//			r.add(t);
//		}
//		return r;		
//	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof MiniZincInput ) {
			MiniZincInput rC = (MiniZincInput)c;
			return this.getRelation().match(rC.getRelation());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof MiniZincInput ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
