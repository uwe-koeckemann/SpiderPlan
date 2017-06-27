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
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Repeatable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.logic.Term;

/**
 * For relations of custom classes with their own reasoning modules.
 * 
 * Examples: spatial relations are not part of the language but can be added
 * by defining custom variables, such as, "custom:spatial:rel overlapping(A,B)" in
 * the domain file. 
 * 
 * If custom relations are added as constraints (e.g. in operators) they will use 
 * CustomRelationalConstraint rather than RelationalConstraint.
 * 
 * Very similar to RelationalConstraint, except that it expects the name of the custom
 * class in the constructors.
 * 
 * Another difference is the ID that is used to keep all CustomRelationalConstraints
 * unique, because sometimes we have to accumulate them (e.g. in case of scores).
 * 
 */
public class CustomConstraint extends Expression implements Matchable, Substitutable, Assertable, Repeatable {
	
	final private static Term ConstraintType = Term.createConstant("custom");
	
	private Term r;
	private Term customClass;
	private boolean isAsserted = false;
	
	/**
	 * Construct custom constraint.
	 * @param relation relational representation of the constraint
	 * @param customClass relation representing the custom constraint class
	 */
	public CustomConstraint( Term relation, Term customClass ) {
		super(ConstraintType);
		this.r = relation;
		this.customClass = customClass;
	}
		
	/**
	 * Get relation.
	 * @return the relation
	 */
	public Term getRelation() {
		return r;
	}
	
	/**
	 * Get custom constraint class.
	 * @return the class
	 */
	public Term getCustomClass() {
		return customClass;
	}
	@Override
	public boolean isAssertable() { return true; }
	
	@Override
	public boolean isMatchable() { return true; }
	
	@Override
	public boolean isRepeatable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }

	@Override
	public String toString() {
		return "["+this.getCustomClass()+"] " + r.toString();
	}

	@Override
	public Expression substitute(Substitution theta) {
		r = r.substitute(theta);
		return this;
	}
	
	@Override
	public boolean isGround() {
		return r.isGround();
	}

//	@Override
//	public Collection<Term> getVariableTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(this.r.getVariableTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		Set<Term> r = new HashSet<Term>();
//		r.addAll(this.r.getGroundTerms());
//		return r;
//	}
//	@Override
//	public Collection<Term> getComplexTerms() {
//		Set<Term> r = new HashSet<Term>();
//		r.add(this.r);
//		return r;
//	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.customClass.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);

	}
	
	@Override
	public Substitution match( Expression c ) {
		if ( c instanceof CustomConstraint ) {
			CustomConstraint rC = (CustomConstraint)c;
			return this.getRelation().match(rC.getRelation());
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof CustomConstraint ) {
			CustomConstraint c = (CustomConstraint)o;
			return this.customClass.equals(c.customClass) && this.r.equals(c.r);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.r.hashCode() + 3*this.customClass.hashCode();
	}

	@Override
	public boolean isAsserted() {
		return isAsserted;
	}

	@Override
	public Expression setAsserted(boolean asserted) {
		this.isAsserted = asserted;
		return this;
	}

	@Override
	public boolean appliesTo(Assertion assertion) {
		// TODO Auto-generated method stub
		return false;
	}	
}
