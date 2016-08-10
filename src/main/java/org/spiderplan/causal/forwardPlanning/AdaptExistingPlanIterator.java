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
package org.spiderplan.causal.forwardPlanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set; 
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.causal.AppliedPlan;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.misc.Delete;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.UniqueID;

/**
 * An iterator over possible adaptations of an existing plan to
 * reach additional goals. Works by finding "a good spot" to
 * reach new goals in previous plan. The existing plan is divided
 * into the sequence L and R (Left and Right of that spot).
 * Then a new plan P is created that uses the state resulting from
 * applying L to the original initial state as a new initial state.
 * Plan P reaches all new goals and
 * assures that the concatenation LPR is consistent by reaching all
 * preconditions of R.
 * 
 * @author Uwe Köckemann
 *
 */
public class AdaptExistingPlanIterator extends ResolverIterator {
	
	ConstraintDatabase originalContext;
	Plan originalPlan;
	
	int cutPoint;
	
	ForwardPlanningIterator planIterator;
	
	Collection<OpenGoal> openGoalConstraints;
	ArrayList<Statement> goalStatements;
	
	ConstraintDatabase newDB;
	ConstraintDatabase icResolvers;
	
	ArrayList<OpenGoal> brokenLinkGoals;
	ArrayList<AllenConstraint> includedLinks;
	ArrayList<AllenConstraint> excludedLinks;
	
	ConstraintDatabase deletedDB = new ConstraintDatabase();
	
	/**
	 * Construct iterator by providing all necessary input.
	 * @param cDB context that needs to be fixed
	 * @param G new set of goals.
	 * @param p previous plan
	 * @param O available actions
	 * @param tM type manager
	 * @param cManager configuration manager
	 * @param name name that is used in logging output
	 */
	public AdaptExistingPlanIterator( ConstraintDatabase cDB, Collection<OpenGoal> G, Plan p, Collection<Operator> O, TypeManager tM, ConfigurationManager cManager, String name ) {
		super(name, cManager);
		
		throw new UnsupportedOperationException("Don't use this until the bug in the TODO is fixed...");

//		this.originalContext = cDB.copy();
//		this.originalPlan = p.copy();
//		
//		goalStatements = new ArrayList<Statement>();
//		openGoalConstraints = G; 
//		
//	
//		if ( keepStats ) stats.increment(msg("#NewGoals (encountered)"));		
//		
//		ArrayList<Constraint> goalConstraints = new ArrayList<Constraint>();
//		ArrayList<Statement> connectedStatements = new ArrayList<Statement>();	
//				
//		ConstraintDatabase ogCDB = new ConstraintDatabase();
//		ogCDB.addStatements(goalStatements);
//		Set<String> relevantVars = CausalReasoningTools.getRelevantVariables(ogCDB, O);							
//		
//		for ( Statement gStatement : goalStatements ) {
//			connectedStatements.addAll(TemporalNetworkTools.directlyConnectedStatements(cDB, gStatement.getKey()));
//		}
//		
//		connectedStatements.removeAll(goalStatements);
//		
//		ArrayList<Statement> remList = new ArrayList<Statement>();
//		for ( Statement s : connectedStatements ) {
//			if ( !relevantVars.contains(s.getVariable().getUniqueName()) ) {
//				remList.add(s);
//			}
//		}
//		connectedStatements.removeAll(remList);
//		
//		if ( verbose ) {
//			print("Need to achieve:", 3);
//			for ( Statement gStatement : goalStatements ) {
//				print("    " + gStatement, 3 );
//			}
//			print("Relevant variables:", 3);
//			for ( String uName : relevantVars ) {
//				print("    " + uName, 3 );
//			}					
//			print("Connected statements:",4);
//			for ( Statement s : connectedStatements ) {
//				print("    " + s.toString(), 4);
//			}
//		}
//
//		ArrayList<Term> goalIntervals = new ArrayList<Term>();
//		for ( Statement gStatement : goalStatements ) {
//			goalIntervals.add(gStatement.getKey());
//		}
//		for ( int i = 0 ; i < goalIntervals.size()-1 ; i++ ) {
//			for ( int j = i+1 ; j < goalIntervals.size() ; j++ ) {
//				goalConstraints.addAll(TemporalNetworkTools.getTemporalConstraintsBetween(cDB, goalIntervals.get(i), goalIntervals.get(j)));
//			}
//		}
//		
//		ConstraintDatabase plannedContext = new ConstraintDatabase();
//		plannedContext.add(cDB);
//		
//		ConstraintCollection goal = new ConstraintCollection();
//		for ( Statement s: goalStatements ) {
//			goalConstraints.add(new OpenGoal(s));
//		}
//		goal.addAll(goalConstraints);
//
//		ConstraintDatabase init = new ConstraintDatabase();
//		init.add(cDB.copy());
//			
//
//		
//		
//		/**
//		 * Find point where to insert new plan:
//		 * 	Include all actions such that their effects provide all
//		 *  statements that are connected to the new goal with a binary
//		 *  temporal constraint.
//		 * We will start planning from the cut point and
//		 * create goals for all causal links that are broken by the cut.
//		 */
//		cutPoint = 0;
//
//		boolean foundCutPoint = false;
//		for ( Operator a : p.getActions() ) {	
//			plannedContext.addStatements(a.getEffects());		// Add all actions here, so we can find  
//			plannedContext.addStatements(a.getPreconditions()); // new statements that are not effects later
//			plannedContext.addConstraints(a.getConstraints());  // (e.g. statements from resolvers of interaction
//			plannedContext.add(a.getNameStateVariable());		// constraints)
//		}
//		plannedContext.addConstraints(p.getConstraints());
//		plannedContext.addStatements(goalStatements);
//		
//		/**
//		 * Add things that are not in initial context or effects of actions
//		 * -> These are statements added by resolvers/conditions of conditional/interaction constraints 
//		 * 
//		 * TODO: there is a problem here in combination with the forgetting mechanism in execution.
//		 */
//		newDB = cDB.difference(plannedContext); 
//
//		ArrayList<InteractionConstraint> ICs = new ArrayList<InteractionConstraint>();
////				ICs.addAll(core.get(InteractionConstraint.class));		
//		ICs.addAll(cDB.get(InteractionConstraint.class));
//					
//		icResolvers = new ConstraintDatabase();
//		
//		for ( InteractionConstraint ic : ICs ) {
//			if ( ic.isAsserted() ) {
//				ArrayList<Term> newGoals = new ArrayList<Term>();
//				for ( OpenGoal ngIC : ic.getResolvers().get(ic.getResolverIndex()).get(OpenGoal.class) ) {
//					newGoals.add(ngIC.getStatement().getKey());
//				}						
//				for ( Statement s : ic.getResolvers().get(ic.getResolverIndex()).getStatements() ) {
//					if ( !goalStatements.contains(s) && !newGoals.contains(s.getKey()) ) {
//						icResolvers.add(s);
//					} 
//				}
//				
//				for ( AllenConstraint tC : ic.getResolvers().get(ic.getResolverIndex()).get(AllenConstraint.class) ) {
//					if ( ! ( goalIntervals.contains(tC.getFrom()) || goalIntervals.contains(tC.getTo()) ) ) {
//						icResolvers.add(tC);
//					}
//				}
//			}
//		}			
//		init.add(icResolvers);
//						
//		for ( Operator a : p.getActions() ) {
//			if ( !foundCutPoint ) {		// Add actions until we find cut point
//				Operator aCopy = a.copy();
//				init.addStatements(aCopy.getEffects());
//				init.addStatements(aCopy.getPreconditions());
//				init.addConstraints(aCopy.getConstraints());
//				init.add(aCopy.getNameStateVariable());
//				init.add(new IgnoredByCausalReasoner(aCopy.getNameStateVariable().getKey()));
//			}					
//			if ( !foundCutPoint && init.getStatements().containsAll(connectedStatements) ) {	// Cut here?
//				foundCutPoint = true;
//				break;
//			}  
//			if ( !foundCutPoint ) { 
//				cutPoint++;
//			}					
//		}
//		
//		if ( verbose ) {
//			print("Cut point: " + cutPoint + "/" + p.getActions().size(),1);
////			Loop.start();
//			for ( int i = 0 ; i <= cutPoint ; i++ ) {
//				print(i + ":" + p.getActions().get(i).getName().toString(), 4);
//			}
//			print("<~~~~~~~CUTTING HERE~~~~~~~~>", 4);
//			for ( int i = cutPoint+1 ; i < p.getActions().size() ; i++ ) {
//				print(i + ":" + p.getActions().get(i).getName().toString(), 4);
//			}
//		}
//				
//		/**
//		 * - Create goals for all causal links that are broken by the cut.
//		 * - Add causal links that are included in the cut. 
//		 * - Remember causal links that are unbroken but not included
//		 */
//		brokenLinkGoals = new ArrayList<OpenGoal>(); 
//		includedLinks = new ArrayList<AllenConstraint>();
//		excludedLinks = new ArrayList<AllenConstraint>();
//		
//		
//		/**
//		 * TODO: For some reason core.getContext() can be missing stuff here...
//		 * (using plannedContext to get brokenLinkGoal as a workaround)
//		 */
//		for ( AllenConstraint tC : p.get(AllenConstraint.class) ) {
//			if ( init.hasKey(tC.getFrom()) && !init.hasKey(tC.getTo()) ) { 	// Cut broke a link that needs to be re-achieved by plan
//				deletedDB.add(tC);
//				
//				OpenGoal brokenLinkGoal;
////						if ( plannedContext.hasKey(tC.getTo()) ) {
////							brokenLinkGoal = plannedContext.getStatement(tC.getTo()).copy();
////						} else {
//				brokenLinkGoal = new OpenGoal(plannedContext.getStatement(tC.getTo())); // before: plannedContext = cDB
////						}
//				if ( verbose) print("Broke link: " + tC + " to " + brokenLinkGoal, 3);
//				goal.add(brokenLinkGoal);		// Make broken link a goal
//				brokenLinkGoals.add(brokenLinkGoal);
//				for ( Statement s : goalStatements ) {						// Achieve link goals after all others
//					goal.add(new AllenConstraint(s.getKey(), tC.getTo(), TemporalRelation.Before, new Interval(Time1,TimeInf)));
//				}
//			} else if ( !init.hasKey(tC.getFrom()) && init.hasKey(tC.getTo()) ) {  // Cut broke a link that needs to be re-achieved by plan
//				deletedDB.add(tC);
//				
//				OpenGoal brokenLinkGoal;
////						if ( plannedContext.hasKey(tC.getTo()) ) {
////							brokenLinkGoal = plannedContext.getStatement(tC.getFrom()).copy();
////						} else {
//				brokenLinkGoal = new OpenGoal(plannedContext.getStatement(tC.getFrom())); // before: plannedContext = cDB
////						}
//				goal.add(brokenLinkGoal);		// Make broken link a goal
//				if ( verbose) print("Broke link: " + tC + " to " + brokenLinkGoal, 3);
//				brokenLinkGoals.add(brokenLinkGoal);
//				for ( Statement s : goalStatements ) {								// Achieve link goals after all others
//					goal.add(new AllenConstraint(s.getKey(), tC.getFrom(), TemporalRelation.Before, new Interval(Time1,TimeInf)));
//				}						
//			} else if ( init.hasKey(tC.getFrom()) && init.hasKey(tC.getTo()) ) { // Link is included in cut
//				if ( verbose) print("Included link: " + tC, 3);
//				init.add(tC);  // Add causal link
//				includedLinks.add(tC);
//			} else if ( !init.hasKey(tC.getFrom()) && !init.hasKey(tC.getTo()) ) { // Link is excluded from cut
//				if ( verbose) print("Excluded link: " + tC, 3);
//				excludedLinks.add(tC);
//			}
//		}			
//		
//		/**
//		 * Remove some constraint that may not be connected at this point.
//		 * (This happened with constraints added by InteractionConstraints, that 
//		 * related to Statements added by actions).
//		 */
//		ArrayList<AllenConstraint> remConList = new ArrayList<AllenConstraint>();
//		ConstraintDatabase initDB = init.copy();
//		for ( AllenConstraint tC : initDB.get(AllenConstraint.class) ) {
//			if ( !initDB.hasKey(tC.getFrom()) || !initDB.hasKey(tC.getTo()) ) {
//				remConList.add(tC);
//			}
//		}
//		initDB.getConstraints().removeAll(remConList);
////			for ( OpenGoal og : initDB.get(OpenGoal.class)) { // open goals in initial context have been reached
////				og.setAsserted(true);
////			}
//		
//		
//		/**
//		 * Create and solve planning problem
//		 */
//		Core planningProblem = new Core();
//		planningProblem.setContext(initDB);
//
//		//				planningProblem.setGoalContext(goal);			
//		planningProblem.getContext().addConstraints(goal);
//		
//		planningProblem.setTypeManager(tM);
//		planningProblem.setOperators(O);
////				planningProblem.setConstraints(core.getConstraints());
//		
//		planIterator = new ForwardPlanningIterator(initDB, openGoalConstraints, O, tM, cManager, getName());
	}
	
	@Override
	public Resolver next( ConstraintDatabase C ) {
		
		Resolver r = planIterator.next();
		
		if ( r == null ) {
			return null;
		}
		
		Plan planForCut = r.getConstraintDatabase().get(AppliedPlan.class).iterator().next().getPlan();
					
		ConstraintDatabase context = new ConstraintDatabase();
//		context.add(originalContext.copy());

		context.add(icResolvers);
		context.add(newDB);
						
		/**
		 * Sew solution into existing plan, and update context.
		 */
		Plan newPlan = new Plan();
		/**
		 *  Before the cut
		 */
		for ( int i = 0; i <= cutPoint ; i++ ) {
			newPlan.addAction(originalPlan.getActions().get(i));
		}
		newPlan.getConstraints().addAll(includedLinks);
		/**
		 * Into the cut
		 */
		Substitution keySubst = new Substitution();
		Set<Term> internallyLinked = new HashSet<Term>();
		for ( AllenConstraint tC : planForCut.getConstraints().get(AllenConstraint.class) ) {
			internallyLinked.add(tC.getFrom());
			internallyLinked.add(tC.getTo());
		}
		/**
		 * Link unlinked preconditions:
		 */								
		long subPlanID = UniqueID.getID();
		for ( Operator a : planForCut.getActions() ) {
			keySubst.add(a.getNameStateVariable().getKey(), a.getNameStateVariable().getKey().makeUnique(subPlanID));
	
			for ( Statement s : a.getPreconditions() ) {
				if ( internallyLinked.contains( s.getKey() ) || s.getKey().isVariable() )  {
					keySubst.add(s.getKey(), s.getKey().makeUnique(subPlanID));
				}
			}
			for ( Statement s : a.getEffects() ) {
				keySubst.add(s.getKey(), s.getKey().makeUnique(subPlanID));
			}
		}
		planForCut.substitute(keySubst);
		keySubst = new Substitution();
		
		Map<Term,Term> lastChangingStatement = new HashMap<Term,Term>();
		for ( Operator a : planForCut.getActions() ) {
			for ( Statement s : a.getPreconditions() ) {
				if ( s.getKey().isVariable() ) {
					for ( Statement initStatement : context.get(Statement.class) ) {
						if ( s.getVariable().equals(initStatement.getVariable()) 
								&& s.getValue().equals(initStatement.getValue())) {
							
							Term newKey = s.getKey().makeConstant();
							keySubst.add(s.getKey(),newKey);
							lastChangingStatement.put(newKey, initStatement.getKey());
						}								
					}
					for ( int i = 0 ; i <= cutPoint ; i++ ) {
						Operator aBeforeCut = originalPlan.getActions().get(i);
						for ( Statement effect : aBeforeCut.getEffects() ) {
							if ( s.getVariable().equals(effect.getVariable()) 
									&& s.getValue().equals(effect.getValue())) {
								Term newKey = s.getKey().makeConstant();
								keySubst.add(s.getKey(),newKey);
								lastChangingStatement.put(newKey, effect.getKey());
							}	
						}
					}
				}
			}
		}
		for ( Term key : lastChangingStatement.keySet() ) {
			AllenConstraint newLink = new AllenConstraint(lastChangingStatement.get(key), key, TemporalRelation.Equals);
			newPlan.getConstraints().add(newLink);							
			if ( verbose ) print("New link (open-precondition): " + newLink, 3);
		}
		planForCut.substitute(keySubst);

		newPlan.getActions().addAll(planForCut.getActions());				
		newPlan.getConstraints().addAll(planForCut.getConstraints());
		newPlan.getConstraints().addAll(excludedLinks);
		
		/**
		 *  After the cut
		 */
		//Find intended causal links for previously broken links
		lastChangingStatement = new HashMap<Term,Term>();
		for ( Statement s : originalContext.get(Statement.class) ) {
			for ( OpenGoal brokenLinkGoal : brokenLinkGoals ) {
				if ( s.getVariable().equals(brokenLinkGoal.getStatement().getVariable()) 
					&& s.getValue().equals(brokenLinkGoal.getStatement().getValue())) {
					lastChangingStatement.put(brokenLinkGoal.getStatement().getKey(),s.getKey());
				}
			}
		}
		Substitution goalSubst = new Substitution();
		for ( Operator a : newPlan.getActions() ) {
			for ( Statement s : a.getEffects() ) {
				for ( OpenGoal brokenLinkGoal : brokenLinkGoals ) {
					if ( s.getVariable().equals(brokenLinkGoal.getStatement().getVariable()) 
							&& s.getValue().equals(brokenLinkGoal.getStatement().getValue())) {
						lastChangingStatement.put(brokenLinkGoal.getStatement().getKey(),s.getKey());
						print("Linking effect: " + s + " to repair broken link to " + brokenLinkGoal, 3);
					}
				}
				for ( Statement goalStatement : goalStatements ) {
					if ( s.getVariable().equals(goalStatement.getVariable()) 
							&& s.getValue().equals(goalStatement.getValue())) {
						goalSubst.add(goalStatement.getKey(),s.getKey());
						print("Linking effect: " + s + " to achieve goal " + goalStatement, 3);
					}
				}
			}
		}
		
		if ( verbose ) {
			for ( Expression c : newPlan.getConstraints() ) {
				if ( verbose ) print("Old link: " + c, 3);
			}
		}
									
		for ( Term key : lastChangingStatement.keySet() ) {
			AllenConstraint newLink = new AllenConstraint(lastChangingStatement.get(key), key, TemporalRelation.Equals);
			newPlan.getConstraints().add(newLink);							
			if ( verbose ) print("New link: " + newLink, 3);
		}
		for ( int i = cutPoint+1 ; i < originalPlan.getActions().size(); i++ ) {
			newPlan.addAction(originalPlan.getActions().get(i));
		}
		
		context = newPlan.apply(context);
			
		for ( OpenGoal og : openGoalConstraints ) {
			context.add(og.getStatement());
		}
		
		for ( InteractionConstraint ic : context.get(InteractionConstraint.class)) {
			if ( ic.isAsserted() ) {
				context.add(ic.getResolvers().get(ic.getResolverIndex()));
			}
		}
					
		newPlan.substitute(goalSubst);
		context.substitute(goalSubst);
		
		for ( OpenGoal og : this.openGoalConstraints ) {	
			context.add(new Asserted(og.copy()));
			context.add(og.getStatement());
		}
		
//		System.out.println("Asserted prev");
//		for ( Asserted a : originalContext.get(Asserted.class)) {
//			System.out.println(a);
//		}
//		System.out.println("Asserted now");
//		for ( Asserted a : context.get(Asserted.class)) {
//			System.out.println(a);
//		}
		
		if ( verbose ) {
			super.print("===========================================", 2);
			super.print("Adding:", 2);
			super.print("===========================================", 2);
			
			super.print(context.difference(originalContext).toString(), 2);
			
			super.print("===========================================", 2);
			super.print("Deleting:", 2);
			super.print("===========================================", 2);
			super.print(deletedDB.toString(), 2);
		}
	
//		Loop.start();
		
		/**
		 * Resolver adds everything that is in context but not in original context:
		 */			
		Resolver result = new Resolver( context.difference(originalContext) );
		/**
		 * We also need to remove causal links that were broken and replaced by new ones
		 */
		for ( Expression deletedCon : deletedDB ) {
			result.getConstraintDatabase().add( new Delete(deletedCon) );
		}
		/**
		 * Remove old plan(s) from CDB
		 */
		for ( AppliedPlan plan : originalContext.get(AppliedPlan.class) ) {
			result.getConstraintDatabase().add(new Delete(plan));	
		}
		
		result.getConstraintDatabase().add(new AppliedPlan(newPlan));

		if ( verbose ) {
			for ( int i = 0 ; i < newPlan.getActions().size() ; i++ ) {
				print(i + ": " + newPlan.getActions().get(i).getName().toString(), 3);
			}
		}
					
		return result;
	}		
}
