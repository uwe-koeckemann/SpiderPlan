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
package org.spiderplan.representation.expressions.misc;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.logic.Term;

/**
 * Just marks another constraint as asserted.
 * 
 * @author Uwe Köckemann
 */
public class Assertion extends Expression implements Assertable, Matchable  {
	final private Term expressionType;
	final private Term assertionParameters[];
	
	private boolean isAsserted = false;
	
	/**
	 * @param expressionType
	 * @param assertionParameters
	 */
	public Assertion( Term expressionType, Term... assertionParameters ) {
		super(ExpressionTypes.Assertion);
		this.expressionType = expressionType;
		this.assertionParameters = assertionParameters;
	}
	
	private Assertion( Assertion a, boolean isAsserted ) {
		super(ExpressionTypes.Assertion);
		this.expressionType = a.expressionType;
		this.assertionParameters = a.assertionParameters;		
		this.isAsserted = isAsserted;
	}
	
	/**
	 * Get the number of parameters of this assertion.
	 * @return The number of parameters.
	 */
	public int getNumParameters() {
		return assertionParameters.length;
	}
	
	/**
	 * Get the nth parameter of this assertion-
	 * @param n
	 * @return The nth parameter
	 */
	public Term getParameter( int n ) {
		return assertionParameters[n];
	}
	
	/**
	 * Get the expression type asserted by this assertion.
	 * @return The expression type.
	 */
	public Term getExpressionType() {
		return this.expressionType;
	}
		
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMatchable() { return true; }
	
	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		return new ArrayList<Term>();
//	}

//	@Override
//	public Collection<Term> getGroundTerms() {
//		return new ArrayList<Term>();
//	}

//	@Override
//	public Collection<Term> getComplexTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( Term a : this.assertionParameters ) {
//			r.add(a);
//		}
//		return r;
//	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof Assertion ) {
			Assertion a = (Assertion)o;
			if ( a.expressionType.equals(this.expressionType) && a.getNumParameters() == this.getNumParameters() ) {
				for ( int i = 0 ; i < this.assertionParameters.length ; i++ ) {
					if ( !this.assertionParameters[i].equals(a.assertionParameters[i])) {
						return false;
					}
				}
				
			} else {
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return expressionType.hashCode() + 3*assertionParameters.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(:assertion ");
		sb.append(expressionType.toString());
		sb.append(" ");
		for ( int i = 0 ; i < this.assertionParameters.length ; i++ ) {
			sb.append(assertionParameters[i].toString());
			sb.append(" ");
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public boolean isAsserted() {
		return isAsserted;
	}

	@Override
	public Expression setAsserted(boolean asserted) {
		return new Assertion(this, isAsserted);
	}

//	@Override
	@Override
	public boolean appliesTo(Assertion assertion) {
		return false;
	}

	@Override
	public boolean isGround() {
		if ( !this.expressionType.isGround() ) {
			return false;
		}
		for ( Term p : this.assertionParameters ) {
			if ( !p.isGround() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Substitution match(Expression c) {
		if ( c instanceof Assertion ) {
			Assertion a = (Assertion)c;
			
			if ( this.getNumParameters() != a.getNumParameters() ) {
				return null;
			}			
			Substitution sub = this.expressionType.match(a.expressionType);
			if ( sub == null ) {
				return null;
			}
			
			for ( int i = 0 ; i < this.getNumParameters() ; i++ ) {
				if ( !sub.add(this.getParameter(i).match(a.getParameter(i))) ) {
					return null;
				}
			}
			return sub;
		}
		return null;
	}

	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		this.expressionType.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		for ( int i = 0 ; i < assertionParameters.length ; i++ ) {
			assertionParameters[i].getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		}
	}
}
