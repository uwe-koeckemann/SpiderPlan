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
package org.spiderplan.causal.pocl;

import java.util.Collection;
import java.util.List;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.types.TypeManager;

/**
 * Iterator for resolvers of {@link OpenGoal}s in a causal link planner.
 * 
 * @author Uwe Köckemann
 */
public class CausalLinkIteratorAllOpen extends ResolverIterator {
	
	POCLSearch search;
	/**
	 * Create a new iterator over resolvers for an {@link OpenGoal}
	 * @param g The {@link OpenGoal} that needs resolving
	 * @param cDB The current context
	 * @param O Available {@link Operator}s
	 * @param heuristicNames Names of used heuristics
	 * @param tM A {@link TypeManager} (may be needed by some heuristics)
	 * @param name Name of this iterator
	 * @param cM A {@link ConfigurationManager}
	 */
	public CausalLinkIteratorAllOpen( ConstraintDatabase cDB, Collection<Operator> O, List<String> heuristicNames, TypeManager tM, String name, ConfigurationManager cM ) {
		super(name, cM);
		
		search = new POCLSearch( cDB, O, heuristicNames, tM, this.getName(), this.verbose, this.verbosity );
	}

	@Override
	public Resolver next(ConstraintCollection C) {
		search.run();
		if ( search.success ) {
			POCLNode s = (POCLNode)search.solution;
			return s.getCombinedResolver();
		}
		
		return null;
	}
}
