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

import org.spiderplan.representation.constraints.constraintInterfaces.Matchable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Supports several math operations for "on-the-fly" computations.
 * Output type (arg3) depends on input types (integer or float).
 * Input types must be the same. mod/3 only works for integers.
 */
public class MiniZincConstraint extends Constraint implements Matchable, Substitutable, Mutable {
	
	private Atomic r;
	private Term programID;
	
	private static final String[][] SUPPORTED = {  
		{"assign/2", "(assign x y)", "x = y;"},
		{"array/2", "(array x (list x1 x2 .. xn))", "x = [x1,x2,...,xn];"}, //TODO: can be done with asign
		{"output/1", "(outout (list y1 y2 .. yn))", "Provides a list of variables that need to be assigned by substitution that results from MiniZinc call."}
	};

	/**
	 * Create copy of {@link MiniZincConstraint} gC.
	 * @param gC a {@link MiniZincConstraint}
	 */
	public MiniZincConstraint( MiniZincConstraint gC ) {	
		super(ConstraintTypes.MiniZinc);
		this.r = gC.r;
		programID = gC.programID;
	}
	/**
	 * Create a new {@link MiniZincConstraint} based on {@link Atomic} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Atomic}
	 */
	public MiniZincConstraint( Atomic l, Term programID ) {
		super(ConstraintTypes.MiniZinc);
		boolean supported = false;
		this.programID = programID;
		for ( String[] list : SUPPORTED ) {
			if ( l.getUniqueName().equals(list[0])) {
				supported = true;
			}
		}
		
		if ( supported ) {
			this.r = l;
		} else {
			String s = l.toString() + " ("+l.getUniqueName()+") not supported. List of supported relations:\n";
			for ( String[] list : SUPPORTED ) {
				s += list[0] + " : " + list[1] + "\n";
			}
			
			throw new IllegalArgumentException(s);
		}
	}
	
	public Atomic getRelation() {
		return r;
	}
	
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
			int i = Integer.valueOf(t.toString());
			return t.toString();
		} catch ( NumberFormatException e ) {
			return "\""+t+"\"";
		}
	}
	
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
	public Constraint substitute(Substitution theta) {
		r = r.substitute(theta);
		programID = programID.substitute(theta);
		return this;
	}
	
	@Override
	public MiniZincConstraint copy() {
		MiniZincConstraint c = new MiniZincConstraint(this);
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
	public Substitution match( Constraint c ) {
		if ( c instanceof MiniZincConstraint ) {
			MiniZincConstraint rC = (MiniZincConstraint)c;
			return this.getRelation().match(rC.getRelation());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof MiniZincConstraint ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
