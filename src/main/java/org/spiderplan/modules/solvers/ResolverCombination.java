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
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.search.GenericSingleNodeSearch;

/**
 * Simple extension of {@link ResolverIterator} that uses a list of {@link LinkedList}s.
 * Can be used to provide a unified {@link Resolver} for many flaws by giving a list
 * of {@link Resolver}s for each individual flaw. The next method of this class will
 * provide the next combination of resolvers for all flaws.
 * 
 * @author Uwe Köckemann
 *
 */
public class ResolverCombination extends ResolverIterator {
	
	private GenericSingleNodeSearch<Resolver> cB;
	private boolean lastAssignmentApproved = true;
	private boolean failed = false;
	
	/**
	 * Create a new {@link ResolverIterator} by providing a {@link List} of <code>n</code> {@link List}s
	 * of {@link Resolver}s. Each call of of next() will provide the next combination elements of the <code>n</code>
	 * lists.
	 * @param R Input {@link List} of {@link List}s of {@link Resolver}s that will be combined.
	 * @param name Name of the {@link ResolverIterator} (used for logging)
	 * @param cM Contains planner configuration (not needed for this class)
	 */
	public ResolverCombination( List<List<Resolver>> R, String name, ConfigurationManager cM) {
		super(name, cM);
		cB = new GenericSingleNodeSearch<Resolver>(R);
	}

	@Override
	public Resolver next( ConstraintDatabase C ) {
		if ( failed ) {
			return null;
		}
		Resolver combo = null;
				
		do {
			cB.advance(lastAssignmentApproved);
			
			if ( cB.failure() ) {
				failed = true;
				return null;
			}
			
			combo = buildResolver(cB.getAssignment());
			
			if ( combo != null ) {
				lastAssignmentApproved = true;
			} else {
				lastAssignmentApproved = false;
			}
		} while ( !cB.success() ); 
		
		lastAssignmentApproved = false;
		return combo;
	}
	
	private Resolver buildResolver( List<Resolver> resolvers  ) {
		ConstraintDatabase cDB = new ConstraintDatabase();
		Substitution s = new Substitution();
		
		for ( Resolver r : resolvers ) {
			if ( !s.add(r.getSubstitution()) ) {
				return null;
			}
			cDB.add(r.getConstraintDatabase());
		}
		Resolver combo = new Resolver(s,cDB);
		return combo;
	}
}
