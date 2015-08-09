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
package org.spiderplan.representation.constraints.constraintInterfaces;

import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;

/**
 * Used for {@link Constraint}s that can be matched
 * to constraints of their own class.
 * 
 * @author Uwe Köckemann
 *
 */
public interface Matchable {
	/**
	 * Returns true if this {@link Constraint} is ground.
	 * (Meaning it does not contain any variable {@link Term}s.)
	 * @return
	 */
	public abstract boolean isGround();
	/**
	 * Match this constraint to another one.
	 * @param c Another {@link Constraint}
	 * @return Substitution that makes {@link Constraint}s this and c equal.
	 */
	public abstract Substitution match( Constraint c );
}
