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
package org.spiderplan.representation.expressions.execution.sockets;

import java.util.Collection;

import org.spiderplan.executor.sockets.SocketExecutionManager;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.SocketRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;


/**
 * Allows connecting to sockets, sending and receiving data via {@link SocketExecutionManager}.
 */
public class SocketExpression extends Expression implements Matchable, Substitutable {
	
	private Term con;
	private SocketRelation relation;
	
	/**
	 * Create a new {@link SocketExpression} based on {@link Term} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param l an {@link Term}
	 */
	public SocketExpression( Term l ) {
		super(ExpressionTypes.Math);
		relation = ExpressionTypes.SocketExecutionExpressions.assertSupported(l, this.getClass());
		this.con = l;
	}
	
	/**
	 * Get relational representation of this math constraint.
	 * @return the relation
	 */
	public Term getConstraint() {
		return con;
	}

	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public SocketRelation getRelation() {
		return relation;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
	
	@Override
	public String toString() {
		return con.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new SocketExpression(con.substitute(theta));
	}
	
	@Override
	public boolean isGround() {
		return con.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof SocketExpression ) {
			SocketExpression rC = (SocketExpression)c;
			return this.getConstraint().match(rC.getConstraint());
		}
		return null;
	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		con.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof SocketExpression ) {
			return this.toString().equals(o.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
