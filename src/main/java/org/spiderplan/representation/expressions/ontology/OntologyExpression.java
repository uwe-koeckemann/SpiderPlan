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
package org.spiderplan.representation.expressions.ontology;

import java.util.Collection;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.ExpressionTypes.OntologyRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.OptimizationRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;


/**
 * Expressions for ontologies (defining prefixes and triplets to formulate queries)
 */
public class OntologyExpression extends Expression implements Matchable, Substitutable {
	
	private OntologyRelation relation;
	private Term ontologyID;
	private Term expression;
	
	/**
	 * Create a new {@link OntologyExpression} based on {@link Term} l. Generates exception if predicate of <code>l</code> is not supported.
	 * @param ontologyID 
	 * @param expression an {@link Term}
	 */
	public OntologyExpression( Term ontologyID, Term expression ) {
		super(ExpressionTypes.Ontology);
		relation = ExpressionTypes.OntologyExpressions.assertSupported(expression, this.getClass());
		this.ontologyID = ontologyID;
		this.expression = expression;		
	}
	
	private OntologyExpression( OntologyRelation relation, Term ontologyID, Term expression ) {
		super(ExpressionTypes.Ontology);
		this.relation = relation;
		this.ontologyID = ontologyID;
		this.expression = expression;		
	}
		
	/**
	 * Get constraint relation.
	 * @return the relation
	 */
	public OntologyRelation getRelation() {
		return relation;
	}
	
	/**
	 * Get ontology ID.
	 * @return term representing ID of main ontology
	 */
	public Term getExpression() {
		return expression;
	}
	
	/**
	 * Get ontology ID.
	 * @return term representing ID of main ontology
	 */
	public Term getOntologyID() {
		return ontologyID;
	}
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }
		
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.expression.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
	}
	
	@Override
	public String toString() {
		return expression.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		return new OntologyExpression(this.relation, this.ontologyID, this.expression.substitute(theta));
	}
	
	@Override
	public boolean isGround() {
		return expression.isGround();
	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof OntologyExpression ) {
			OntologyExpression rC = (OntologyExpression)c;
			return this.expression.match(rC.expression);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof OntologyExpression ) {
			return this.expression.equals(((OntologyExpression)o).expression);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}	
}
