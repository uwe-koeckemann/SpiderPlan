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
package org.spiderplan.representation.types;

import java.util.ArrayList;

import org.spiderplan.representation.logic.Term;

/**
 * Super-class to all types 
 * @author Uwe Köckemann
 */
public abstract class Type {
	protected Term name;
		
	/**
	 * Returns type name as a term.
	 * @return the name
	 */
	public Term getName() { return name; };
	/**
	 * Set name of this type.
	 * @param name the name
	 */
	public void setName( Term name ) { this.name = name; };
	
	/**
	 * Set name of this type with a string.
	 * @param name
	 */
	public void setName( String name ) { this.name = Term.createConstant(name); };
	
	/**
	 * Test if this type's domain contains value.
	 * @param s the value
	 * @return <code>true</code> if the type's domain contains <code>s</code>, <code>false</code> otherwise
	 */
	public abstract boolean contains(Term s);
	/**
	 * Returns the domain of this type.
	 * @return the domain
	 */
	public abstract ArrayList<Term> getDomain();

	@Override
	public abstract String toString();
}
