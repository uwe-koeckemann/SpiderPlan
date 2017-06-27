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
package org.spiderplan.modules.solvers;

import org.spiderplan.modules.solvers.Core.State;

/**
 * An interface for modules that reason about different types of constraints.
 * 
 * @author Uwe Köckemann
 */
public interface SolverInterface {		
	/**
	 * Returns a generator for {@link Resolver}s that impose decisions required by this module.
	 * <br><br>
	 * <b>Note:</b> This method is forbidden to change <code>core</code>
	 * or any of its members.
	 * It proposes changes via the {@link ResolverIterator}. 
	 * @param core The {@link Core} the module is working on (remains unchanged)
	 * @return {@link SolverResult} that contains the {@link State} which the module
	 * assigned to the input (Consistent,Inconsistent,Searching). If there are flaws to resolve
	 * it {@link SolverResult} also contains a {@link ResolverIterator} that will propose
	 * {@link Resolver}s to a flaw encountered by the module.
	 */
	public SolverResult testAndResolve( Core core );	
}
