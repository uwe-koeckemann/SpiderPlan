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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;


/**
 * Contains the String representation of an included program.
 * This is not parsed, so there is not much done in this class.
 * It will be used to create files for the reasoner that uses this
 * database. Association with a reasoner is done with the assigned name.
 * <p>
 * Note: Substitution on {@link IncludedProgram} is possible when using 
 * the special sequence <i><<<?x?>>></i> inside the string description of 
 * the background knowledge. This means that not substituting will
 * most likely lead to syntactic errors.
 * <p>
 * This type of constraint is set to asserted in the constructor, which
 * means that it is not used to model a requirement, but rather
 * to assert knowledge directly. If an inconsistent 
 * {@link IncludedProgram} is provided the planner will fail
 * and exit when trying to use it. The interpretation of the code 
 * is entirely left for external programs. 
 * <p>
 * @author Uwe Köckemann
 *
 */
public class IncludedProgram extends Constraint implements Substitutable, Mutable {
	
	final private static Term ConstraintType = Term.createConstant("include");

	private Term name;
	private String code;
	
	public IncludedProgram( Term name, String code ) {
		super(ConstraintType);
		this.code = code;
		this.name = name;
	}
	
	public Term getName() {
		return name;
	}
	
	public String getCode() { return code; }

	@Override
	public Constraint copy() {
		IncludedProgram bk = new IncludedProgram( this.name, this.code );
		return bk;
	}

	@Override
	public boolean equals(Object o) {
		if ( o instanceof IncludedProgram ) {
			IncludedProgram pC = (IncludedProgram)o;
			return pC.toString().equals(this.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		r.append("( ");
		r.append(name.toString());
		r.append("\n\t<begin-escape-syntax>\n");
		r.append(code);
		r.append("\n\t<end-escape-syntax>\n)");
		return r.toString();
	}

	@Override
	public Constraint substitute(Substitution theta) {
		/**
		 * Replacing inside arbitrary KB using <<<?from?>>>
		 */
		for ( Term from : theta.getMap().keySet() ) {
			this.code = this.code.replaceAll( Pattern.quote("<<<"+from+">>>"), theta.getMap().get(from).toString());
		}
		this.name = this.name.substitute(theta);
		return this;
	}

	@Override
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>(); 
		return r;
	}

	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		return r;
	}

	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		return r;
	}
}
