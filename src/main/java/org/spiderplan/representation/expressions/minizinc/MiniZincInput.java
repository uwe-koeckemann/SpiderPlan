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
package org.spiderplan.representation.expressions.minizinc;

import java.util.Collection;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.SubProblemSupport;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.logic.Term;


/**
 * Allows to provide input to a MiniZinc program. 
 * 
 * @author Uwe Köckemann
 */
public class MiniZincInput extends Expression implements Matchable, Substitutable, SubProblemSupport {
	
	private Term r;
	private Term subProblemID;
	
	/**
	 * Create copy of {@link MiniZincInput} gC.
	 * @param gC a {@link MiniZincInput}
	 */
	public MiniZincInput( MiniZincInput gC ) {	
		super(ExpressionTypes.MiniZinc);
		this.r = gC.r;
		subProblemID = gC.subProblemID;
	}
	/**
	 * Create a new {@link MiniZincInput} based on {@link Term} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Term}
	 * @param programID the program ID used for this constraint (see {@link IncludedProgram})
	 */
	public MiniZincInput( Term l, Term programID ) {
		super(ExpressionTypes.MiniZinc);
		this.subProblemID = programID;
		ExpressionTypes.MiniZincExpressions.assertSupported(l, this.getClass());
	}
	
	/**
	 * Get the relational form of this constraint.
	 * @return the relation
	 */
	public Term getRelation() {
		return r;
	}
	
	/**
	 * Returns this constraint as MiniZinc syntax so it can be put in a dzn file (i.e. MiniZinc data input) 
	 * @return A {@link String} representation of this constraint in MiniZinc syntax.
	 */
	public String getMiniZincCode() {
		if ( r.getName().equals("assign") ) {
			return r.getArg(0) + " = " + r.getArg(1) + ";";
		} else if  ( r.getName().equals("array") ) {
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
	public Term getSubProblemID() {
		return subProblemID;
	}

	@Override
	public boolean isPartOf(Term subProblemID) {
		return null != subProblemID.match(subProblemID);
	}	
	
	@Override
	public boolean isMatchable() { return true; }
		
	@Override
	public boolean isSubstitutable() { return true; }
		
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.r.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.subProblemID.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public String toString() {
		return r.toString(); // + "[wrt. "+programID+"]";
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		subProblemID = subProblemID.substitute(theta);
		return this;
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
