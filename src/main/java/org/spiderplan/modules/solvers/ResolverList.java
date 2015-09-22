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
package org.spiderplan.modules.solvers;

import java.util.LinkedList;
import java.util.List;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.representation.ConstraintDatabase;

/**
 * Simple extension of {@link ResolverIterator} that uses a {@link LinkedList}.
 * Can be used in cases where a more refined extension of a {@link ResolverIterator}
 * is not needed (i.e. when all resolvers are known/created as soon as the flaw
 * is found.
 * 
 * @author Uwe Köckemann
 *
 */
public class ResolverList extends ResolverIterator {
	
	private LinkedList<Resolver> R;
	
	/**
	 * Create a new {@link ResolverIterator} by providing a {@link List} 
	 * of {@link Resolver}s.
	 * @param R Input {@link List} of {@link Resolver}s.
	 * @param name Name of the {@link ResolverIterator} (used for logging)
	 * @param cM Contains planner configuration (not needed for this class)
	 */
	public ResolverList( List<Resolver> R, String name, ConfigurationManager cM) {
		super(name, cM);
		this.R = new LinkedList<Resolver>(R);
	}

	@Override
	public Resolver next( ConstraintDatabase C ) {
		if ( !R.isEmpty() ) {
			Resolver r = R.get(0);
			R.remove(0);
			return r;
		} else {
			return null;
		}
	}
}
