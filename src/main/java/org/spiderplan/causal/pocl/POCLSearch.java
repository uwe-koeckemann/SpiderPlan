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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

import org.spiderplan.search.MultiHeuristicNode;
import org.spiderplan.search.MultiQueueSearch;
import org.spiderplan.search.MultiHeuristicNode.CompareMethod;
import org.spiderplan.causal.pocl.flaws.AllLIFO;
import org.spiderplan.causal.pocl.heuristics.Heuristic;
import org.spiderplan.causal.pocl.heuristics.HeuristicFactory;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Asserted;
import org.spiderplan.representation.constraints.ConstraintTypes.TemporalRelation;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;

/**
* Causal link plan search.
* 
* @author Uwe Köckemann
*
*/
public class POCLSearch extends MultiQueueSearch {
	private Collection<Operator> O;
	private TypeManager tM;
	
	ArrayList<Heuristic> heuristics;
	
	boolean multiQueue = true;
	
	public POCLSearch( ConstraintDatabase init, Collection<Operator> O, List<String> heuristicNames, TypeManager tM, String name, boolean verbose, int verbosity ) {
		super.name = name;
		super.verbose = verbose;
		super.verbosity = verbosity;
	
		this.O = O;
		this.tM = tM;
		
		this.heuristics = new ArrayList<Heuristic>();
		for ( String hName : heuristicNames ) {
			this.heuristics.add(HeuristicFactory.createHeuristic(hName, init, O, tM));
		}
		
		ArrayList<Integer> queueToHeuristicMap = new ArrayList<Integer>();
		
		for ( int i = 0 ; i < heuristics.size() ; i++ ) {
			queueToHeuristicMap.add(i);
			super.addNewQueue();
		}
		 
		this.queueToHeuristicMap = new int[queueToHeuristicMap.size()];
		for ( int i = 0 ; i < queueToHeuristicMap.size() ; i++ ) {
			this.queueToHeuristicMap[i] = queueToHeuristicMap.get(i).intValue();
		}

		POCLNode initPPlan = new POCLNode(heuristicNames.size());
		
		initPPlan.resolver = new Resolver(new Substitution(), new ConstraintDatabase());
		initPPlan.openGoals = new AllLIFO();
		initPPlan.context = init.copy();
		
		for ( OpenGoal og : init.getConstraints().get(OpenGoal.class) ) {
			initPPlan.openGoals.add(og);
		}
		
		super.init(initPPlan);
	}
	
	@Override
	public boolean isGoal(MultiHeuristicNode n) {
		POCLNode p = (POCLNode)n;
		return p.isSolution();
	}

	@Override
	public ArrayList<LinkedList<MultiHeuristicNode>> expand(MultiHeuristicNode n) {
		POCLNode p = (POCLNode)n;		
		
		ArrayList<LinkedList<MultiHeuristicNode>> expansion = new ArrayList<LinkedList<MultiHeuristicNode>>();
		for ( int i = 0 ; i < this.heuristics.size(); i++ ) {
			expansion.add( new LinkedList<MultiHeuristicNode>() );	
		}
		
		/**
		 * 1) Select first flaw
		 */
		OpenGoal oG = p.openGoals.select();
		
		List<Resolver> resolvers = this.getResolvers(oG, p.context, O, tM);
		
		if ( verbose ) Logger.msg(getName(),"Selected flaw: " + oG, 1);
		
		/**
		 * 2) Apply all resolvers 
		 */
		Logger.depth++;
		for ( Resolver r : resolvers ) {
			
			if ( verbose ) Logger.msg(getName(),"Possible resolver:\n" + r, 1);	
			
			ConstraintDatabase succDB = p.context.copy();
			r.apply(succDB);
			
			POCLNode succ = new POCLNode(this.heuristics.size());
			succ.resolver = r;		
			succ.context = succDB; 
			Substitution allSubst = succ.getCombinedSubstitution(); 
			
			if ( allSubst != null ) {
				succ.openGoals = p.openGoals.copy();		
				for ( OpenGoal newGoal : r.getConstraintDatabase().getConstraints().get(OpenGoal.class)) {
					succ.openGoals.add(newGoal);
				}
				succ.pred = p;
				
				succ.openGoals.substitute(allSubst);
				succ.context.substitute(allSubst);
			
			
				boolean goalReachable = true;
				if ( verbose ) Logger.msg(getName(),"Heuristic values:" , 1);
				Logger.depth++;
				for ( int i = 0 ; i < heuristics.size() ; i++ ) {
					long hVal = heuristics.get(i).calculateHeuristicValue(succ.context, O); 
					
					hVal += r.getConstraintDatabase().getConstraints().get(Operator.class).size(); // h(n) + g(n)
					
					if ( verbose ) Logger.msg(getName(), this.heuristics.get(i).getClass().getSimpleName() + " = " + hVal, 1);
									
					succ.setHeuristicValue(i, hVal);
					if ( succ.getHeuristicValue(i) == Long.MAX_VALUE ) {					
						goalReachable = false;
						break;
					}
				}
				Logger.depth--;
				
				if ( goalReachable ) {
					if ( multiQueue ) {			// One queue per heuristic
						succ.compareMethod = CompareMethod.Index;
						for ( int i = 0 ; i < heuristics.size() ; i++ ) {
							expansion.get(i).add(succ);
						}
					} 
				}
			}
//			if ( verbose ) Logger.msg(getName(),"Successor ("+Arrays.toString(succ.getHeuristicValues())+"):\n" + r, 1);
		}
		Logger.depth--;
		
		return expansion;
	}
	
	/**
	 * Get all resolvers for open goal <code>s</code> given 
	 * @param s Open goal
	 * @param O Available {@link Operator}s
	 * @return List of possible resolvers
	 */
	private ArrayList<Resolver> getResolvers( OpenGoal g, ConstraintDatabase cDB, Collection<Operator> O, TypeManager tM ) {
		ArrayList<Resolver> resolvers = new ArrayList<Resolver>();

//		cDB.substitute(prevSubst);
//		g = g.substitute(prevSubst);
		
		/**
		 * 1) All statements already in CDB that could provide s
		 * 		Note: Using "temporal unification" (with EQUALS constraint),
		 * 			  since the statements s and e already have ground keys.
		 */
		for ( Statement s : cDB.getConstraints().get(Statement.class) ) {
			Substitution theta = g.getStatement().matchWithoutKey(s);
			if ( theta != null ) {
				ConstraintDatabase resDB = new ConstraintDatabase();
//				AllenConstraint causalLink = new AllenConstraint(s.getKey(), g.getStatement().getKey(), TemporalRelation.Equals);
//				resDB.add(causalLink);
				resDB.add(new Asserted(g.copy()));
				
				theta.add(g.getStatement().getKey(), s.getKey());
							
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

						resOp.makeEffectAndNameKeysGround();
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
						resDB.addStatements(resOp.getEffects());
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
//						AllenConstraint causalLink = new AllenConstraint(newKey, g.getStatement().getKey(), TemporalRelation.Equals);
//						resDB.add(causalLink);
						
						theta.add(g.getStatement().getKey(), newKey);
						
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