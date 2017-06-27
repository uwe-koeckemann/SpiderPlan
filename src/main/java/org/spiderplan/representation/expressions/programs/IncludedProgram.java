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
package org.spiderplan.representation.expressions.programs;

import java.util.Collection;
import java.util.regex.Pattern;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
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
 * 
 * TODO: Make immutable
 * TODO: Load code on demand not during parsing
 * 
 * @author Uwe Köckemann
 *
 */
public class IncludedProgram extends Expression implements Substitutable, Mutable {
	
	final private static Term ConstraintType = Term.createConstant("include");

	private Term name;
	private String src_path;
	private String code;
	private String subStart = "";
	private String subEnd = "";
	
	/**
	 * Create new program object by providing name and string representation of the code.
	 * @param name name used by the program
	 * @param code the code
	 */
	public IncludedProgram( Term name, String code ) {
		super(ConstraintType);
		this.code = code;
		this.name = name;
	}
	
	/**
	 * Create new program object by providing name and string representation of the code.
	 * @param name name used by the program
	 * @param path path to file (local or URL)
	 * @param code the code
	 */
	public IncludedProgram( Term name, String path, String code ) {
		super(ConstraintType);
		this.code = code;
		this.src_path = path;
		this.name = name;
	}
	
	/**
	 * Create new program object by providing name and string representation of the code.
	 * This constructor also allows to modify the substitution behavior by providing start
	 * and end strings that need to be wrapped around a variable name to substitute into the program.
	 * This allows to use SpiderPlan variable substitutions in any type of program.
	 * 
	 * @param name name used by the program
	 * @param path path to file (local or URL)
	 * @param code the code
	 * @param subStart variable substitutions are required to start with this string 
	 * @param subEnd variable substitutions are required to end with this string
	 */
	public IncludedProgram( Term name, String path, String code, String subStart, String subEnd ) {
		super(ConstraintType);
		this.code = code;
		this.src_path = path;
		this.name = name;
	}
	
	/**
	 * Get the name of this program.
	 * @return the name
	 */
	public Term getName() {
		return name;
	}
	
	/**
	 * Get the code of this program as a string.
	 * @return the code
	 */
	public String getCode() { return code; }
	
	/**
	 * Get the path to this program as a string.
	 * @return the path
	 */
	public String getPath() { return src_path; }
	
	
	@Override
	public boolean isMutable() { return true; }
	
	@Override
	public boolean isSubstitutable() { return true; }

	@Override
	public IncludedProgram copy() {
		IncludedProgram bk = new IncludedProgram( this.name, this.code );
		return bk;
	}
	
	@Override
	public void getAllTerms(Collection<Term> collectedTerms, boolean getConstants, boolean getVariables, boolean getComplex) {
		super.type.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
		this.name.getAllTerms(collectedTerms, getConstants, getVariables, getComplex);
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
	public Expression substitute(Substitution theta) {
		/**
		 * Replacing inside arbitrary KB using <<<?from?>>>
		 */
		for ( Term from : theta.getMap().keySet() ) {
			this.code = this.code.replaceAll( Pattern.quote(subStart+from+subEnd), theta.getMap().get(from).toString());
		}
		this.name = this.name.substitute(theta);
		return this;
	}
}
