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

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import org.spiderplan.causal.pocl.heuristics.Heuristic;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Asserted;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.UniqueID;

/**
 * Iterator for resolvers of {@link OpenGoal}s in a causal link planner.
 * 
 * @author Uwe Köckemann
 */
public class CausalLinkIterator extends ResolverIterator {
	
	PriorityQueue<ComparableResolver> queue;
	
	/**
	 * Create a new iterator over resolvers for an {@link OpenGoal}
	 * @param g The {@link OpenGoal} that needs resolving
	 * @param cDB The current context
	 * @param O Available {@link Operator}s
	 * @param heuristic The {@link Heuristic} that is used to sort resolvers
	 * @param name Name of this iterator
	 * @param cM A {@link ConfigurationManager}
	 */
	public CausalLinkIterator( OpenGoal g, ConstraintDatabase cDB, Collection<Operator> O, Heuristic heuristic, String name, ConfigurationManager cM ) {
		super(name, cM);
		ArrayList<Resolver> resolvers = this.getResolvers(g, cDB, O);

		queue = new PriorityQueue<ComparableResolver>();	
		for ( Resolver r : resolvers ) {
			ConstraintDatabase applied = cDB.copy();
			r.apply(applied);
			
			queue.add(new ComparableResolver( r, heuristic.calculateHeuristicValue(applied, O)) );
		}
	}

	@Override
	public Resolver next(ConstraintCollection C) {
		return queue.poll().r;
	}
	
	private ArrayList<Resolver> getResolvers( OpenGoal g, ConstraintDatabase cDB, Collection<Operator> O ) {
		ArrayList<Resolver> resolvers = new ArrayList<Resolver>();

		/**
		 * 1) All statements already in CDB that could provide s
		 * 		Note: Using temporal unification (with EQUALS constraint),
		 * 			  since the statements s and e already have ground keys.
		 */
		for ( Statement s : cDB.getStatements() ) {
			Substitution theta = g.getStatement().matchWithoutKey(s);
			if ( theta != null ) {
				AllenConstraint causalLink = new AllenConstraint(s.getKey(), g.getStatement().getKey(), TemporalRelation.Equals);
				ConstraintDatabase resDB = new ConstraintDatabase();
				resDB.add(causalLink);
				
				resDB.add(new Asserted(g.copy()));
							
				resolvers.add( new Resolver(theta, resDB) );	
			}
		}

		/**
		 * 2) All actions not in PartialPlan that could provide s
		 */
		
		for ( Operator o : O ) {
			Operator oCopy = o.copy();
			long ID = UniqueID.getID();
			oCopy.makeUniqueVariables(ID);

			for ( Statement e : oCopy.getEffects() ) {
				Substitution theta = g.getStatement().matchWithoutKey(e);
				if ( theta != null ) {
					ConstraintDatabase resDB = new ConstraintDatabase();
					
					Operator resOp = oCopy.copy();
					resOp.substitute(theta);
					resOp.makeUniqueEffectKeys(ID);
//					resOp.makeKeysGround();
					
					Term newKey = null;
					Statement eCopy = e.substitute(theta);
					for ( Statement eff : resOp.getEffects() ) {
						if ( eff.getVariable().equals(eCopy.getVariable()) && eff.getValue().equals(eCopy.getValue()) ) {
							newKey = eff.getKey();
						}
					}
										
					/**
					 * Add elements that operator provides (effects and constraints)
					 */
					resDB.addStatements(resOp.getEffects());
//					resDB.addStatements(resOp.getPreconditions());
					resDB.addConstraints(resOp.getConstraints());
					
					/**
					 * Preconditions become OpenGoals
					 */
					for ( Statement pre : resOp.getPreconditions() ) {
						resDB.add(new OpenGoal(pre));
					}
					
					/**
					 * Add Operator
					 */
					resDB.add(resOp);
					

					
					/**
					 * Add causal link in form of AllenConstraint
					 */
					AllenConstraint causalLink = new AllenConstraint(newKey, g.getStatement().getKey(), TemporalRelation.Equals);
					resDB.add(causalLink);
					
					resDB.add(new Asserted(g.copy()));
					
					Resolver r = new Resolver(theta, resDB);
					
//					System.exit(0); 
					
					resolvers.add(r);
				}
			}
		}		
				
		return resolvers;
	}

	private class ComparableResolver implements Comparable<ComparableResolver> {
		private Resolver r;
		private long h;
		
		public ComparableResolver( Resolver r, long heuristicValue ) {
			this.r = r;
			this.h = heuristicValue;
		}
		
		@Override
		public int compareTo( ComparableResolver cr ) {
			return (int) (this.h-cr.h);
		}
		
	}
}
