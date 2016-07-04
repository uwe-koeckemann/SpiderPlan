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
package org.spiderplan.representation.expressions.interfaces;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.cost.Cost;
/**
 * Constraints using this interface may be added more than once to a {@link ConstraintDatabase}. 
 * <p>
 * Example: {@link Cost} constraints can add the same value to the same cost twice so {@link Cost} 
 * implements this interface to signal this fact.
 * 
 * @author Uwe Koeckemann
 *
 */
public interface Repeatable {
	/**
	 * Check if multiple equal instances of this constraint can be added to a constraint database.
	 * @return <code>true</code> if the constraint is repeatable, <code>false</code> otherwise
	 */
	public boolean isRepeatable();
}
