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
package org.spiderplan.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.spiderplan.causal.pocl.flaws.AllFIFO;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Asserted;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

/**
 * Select an {@link OpenGoal} flaw and create a list of resolvers for that flaw.
 * Uses ideas and heuristics of partial order causal link planning but threats
 * are left for temporal and resource constraints.
 * 
 * TODO:
 * - Heuristic selection should be option
 * - Easy integration of value selection heuristics
 * - Multi-heuristic support
 * 
 * <p>
 * Relevant papers:
 * <li> Younes, H. L. S. & Simmons, R. G. VHPOP: Versatile heuristic partial order planner Journal of Artificial Intelligence Research, 2003, 20, 405-430
 * <li> McAllester, D. & Rosenblitt, D. Systematic Nonlinear Planning Proceedings of the 9th National Conference on Artificial intelligence (AAAI), 1991, 634-639
 * <p>
 * 
 * @author Uwe Köckemann
 *
 */
public class OpenGoalResolverSingleFlaw extends Module implements SolverInterface {
	
	List<String> heuristicNames = new ArrayList<String>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public OpenGoalResolverSingleFlaw(String name, ConfigurationManager cM) {
		super(name, cM);
		
		super.parameterDesc.add( new ParameterDescription("heuristics", "string", "HAddReuse", "Comma-seperated list of heuristics (supported: FastDownward).") );
		
		if ( cM.hasAttribute(name, "heuristics" ) ) {
			heuristicNames = cM.getStringList(name, "heuristics");
		} else {
			heuristicNames = new ArrayList<String>();
			heuristicNames.add("HAddReuse");
		}
	}

	@Override
	public Core run(Core core) {
		throw new IllegalAccessError("This is not implemented...");
	}

	@Override
	public SolverResult testAndResolve(Core core) {	
		AllFIFO flawSelector = new AllFIFO(); // Variable ordering heuristic 
	
		for ( OpenGoal og : core.getContext().get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				flawSelector.add(og);
			}
		}
		
		if ( flawSelector.isEmpty() ) {
			return new SolverResult(State.Consistent);
		} else {
			OpenGoal og = flawSelector.select();
			if ( verbose ) Logger.msg(this.getName(), "Selected OpenGoal flaw: " + og, 1);
			
			// Value ordering = List order
			List<Resolver> resolvers = this.getResolvers(og, core.getContext(), core.getOperators(), core.getTypeManager());
			
			if ( verbose ) {
				Logger.msg(this.getName(), "Possible resovlers:", 1);
				
				
				int index = 0;
				
				for ( Resolver r : resolvers ) {
					
					Logger.msg(this.getName(), "Resolver " + (index++) + ":", 1);
					Logger.depth++;
					Logger.msg(this.getName(), r.toString(), 1);
					Logger.depth--;
					
				}
				
				
				
			}
			
			ResolverList resolverList = new ResolverList(resolvers, this.getName(), this.cM);		
			return new SolverResult(State.Searching, resolverList);
		}
	}
	
	
	/**
	 * Get all resolvers for open goal <code>s</code> given 
	 * @param s Open goal
	 * @param O Available {@link Operator}s
	 * @return List of possible resolvers
	 */
	private ArrayList<Resolver> getResolvers( OpenGoal g, ConstraintDatabase cDB, Collection<Operator> O, TypeManager tM ) {
		ArrayList<Resolver> resolvers = new ArrayList<Resolver>();

		/**
		 * 1) All statements already in CDB that could provide s
		 * 		Note: Using "temporal unification" (with EQUALS constraint),
		 * 			  since the statements s and e already have ground keys.
		 */
		for ( Statement s : cDB.get(Statement.class) ) {
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
		 * 2) All actions that could reach provide g
		 */
		for ( Operator o : O ) {
			Operator oCopy = o.copy();
			long ID = UniqueID.getID();
			
			oCopy.makeUniqueVariables(ID);
			
			for ( Statement e : oCopy.getEffects() ) {
				Substitution theta = g.getStatement().matchWithoutKey(e);
				if ( theta != null ) {
					Operator resOp = oCopy.copy();
					resOp.substitute(theta);
					
					/**
					 * Create the resolver
					 */
					if ( tM.isConsistentVariableTermAssignment(resOp.getName(), null) ) {
						ConstraintDatabase resDB = new ConstraintDatabase();
						
						resOp.makeUniqueEffectKeys(ID);
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
						resDB.add(resOp.getNameStateVariable());
						for ( Statement eff : resOp.getEffects() ) {
							resDB.add(eff);
						}
//						resDB.addStatements(resOp.getEffects());
						resDB.addAll(resOp.getConstraints());
						
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
						
						
						resolvers.add(r);
					}
				}
			}
		}		
				
		return resolvers;
	}
	
}
