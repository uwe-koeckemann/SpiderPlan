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
 * Returned by {@link SolverInterface}. Collects resulting {@link State}
 * and {@link ResolverIterator} in one instance.
 * 
 * @author Uwe Köckemann
 *
 */
public class SolverResult {
	
	private Core.State state;
	private ResolverIterator rI;

	/**
	 * Create a new {@link SolverResult} with a {@link State}
	 * @param state A {@link State} describing the result.
	 */
	public SolverResult( State state ) {
		this(state,null);
	}	
	/**
	 * Create a new {@link SolverResult} with a {@link State}
	 * and a {@link ResolverIterator}
	 * @param state A {@link State} describing the result.
	 * @param resolverIterator A {@link ResolverIterator} that 
	 * provides all ways to fix the flaws found by a solver.
	 */
	public SolverResult( Core.State state, ResolverIterator resolverIterator ) {
		this.state = state;
		this.rI = resolverIterator;
	}
	/**
	 * Get the state.
	 * @return A {@link State} describing the type of result.
	 */
	public Core.State getState() { return state; }
	/**
	 * Get the {@link ResolverIterator} that will provide fixes suggested by a
	 * solver.
	 * @return The {@link ResolverIterator}
	 */
	public ResolverIterator getResolverIterator() { return rI; }
}
