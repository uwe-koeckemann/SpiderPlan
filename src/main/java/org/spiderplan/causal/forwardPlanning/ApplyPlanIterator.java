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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.temporal.TemporalNetworkTools;
import org.spiderplan.tools.logging.Logger;
 
/**
 * Given a {@link Plan} and a {@link ConstraintDatabase} this class
 * will provide possible ways to apply the plan. If intermediate
 * decisions need to be tested an external consistency checking 
 * {@link Module} can be provided.
 * 
 * @author Uwe Köckemann
 *
 */
public class ApplyPlanIterator extends ResolverIterator {
	
	private ArrayList<ArrayList<Substitution>> matchesForAllOpenPreconditions = new ArrayList<ArrayList<Substitution>>();
	private ArrayList<ArrayList<AllenConstraint>> linksForAllOpenPreconditions = new ArrayList<ArrayList<AllenConstraint>>();
	private Substitution forcedSubs = new Substitution();
	private ArrayList<AllenConstraint> forcedLinks = new ArrayList<AllenConstraint>();
	
	private GenericSingleNodeSearch<Integer> search;
	
//	private boolean addGoalDB = false;
	
	private Plan originalPlan;
	private ConstraintDatabase originalCDB;
	
	private boolean searchDone;
	private boolean searchSuccess;
	
//	private boolean incremental = false;
//	private String consistencyChecker;
//	private Module checkingModule = null;
	
	/**
	 * These will be applied to output:
	 */
	Substitution subst = null;
	List<AllenConstraint> causalLinks = null;
	
	/**
	 * This will be the output (unless the module fails)
	 */
	ConstraintDatabase outCDB = null;
	Plan outPlan = null; 
	
	boolean wasNoGood = false;
	
	private boolean firstTime = true;
	
	boolean addGoals = false;
	
	/**
	 * Create a new {@link ApplyPlanIterator} that will provide {@link Resolver}s for
	 * every way of applying a {@link Plan} to a {@link ConstraintDatabase}
	 * @param cDB The {@link ConstraintDatabase} the {@link Plan} has to be applied to
	 * @param p The {@link Plan}
	 * @param name Name of this iterator
	 * @param cM A {@link ConfigurationManager}
	 * @param isSolution use <code>true</code> if the plan to be applied is a solution (i.e., it reaches all open goals), <code>false</code> otherwise
	 * @param tM type manager
	 */
	public ApplyPlanIterator( ConstraintDatabase cDB, Plan p, String name, ConfigurationManager cM, boolean isSolution, TypeManager tM ) {
		super(name, cM);
		
		addGoals = isSolution;
		
		super.parameterDesc.add( new ParameterDescription("consistencyChecker", "String", "", false, "Name of the module that verifies consistency of constraint databases.") );
						
//		if ( cM.hasAttribute(name, "consistencyChecker") ) {
//			this.incremental = true;
//			this.consistencyChecker = cM.getString(this.getName(), "consistencyChecker" );
//			this.checkingModule = ModuleFactory.initModule( this.consistencyChecker, cM );
//		}
		
		Map<Atomic,List<Term>> sortedIntervals = TemporalNetworkTools.getSequencedIntervals(cDB);

		/**
		 * Collect goal statements
		 */
		if ( verbose ) print("Collecting goal statements...",3);
		ArrayList<Statement> goalStatements = new ArrayList<Statement>();
		for ( OpenGoal og : cDB.get(OpenGoal.class) ) {
			
			if ( verbose ) print (og.toString(), 3);
//			if ( !og.isAsserted() ) {  // TODO: why did I comment this out?
				goalStatements.add(og.getStatement());
//			} else {
//				if ( verbose ) print ("Already asserted...", 3);
//			}
		}
		
		matchesForAllOpenPreconditions.clear();
		linksForAllOpenPreconditions.clear();
		
		originalPlan = p.copy();
		originalCDB = cDB.copy();
		
		Plan plan = originalPlan.copy();
		
		/**
		 * Find all Statements in context that could be used for open preconditions.
		 */
		for ( Operator a : plan.getActions() ) {
			for ( Statement pre : a.getPreconditions() ) {
				if ( !pre.getKey().isGround() ) {		
					if ( verbose ) print("Precondition: " + pre, 3);
					ArrayList<Substitution> possibleMachtes = new ArrayList<Substitution>();
					ArrayList<AllenConstraint> possibleLinks = new ArrayList<AllenConstraint>();
					for ( Atomic var : sortedIntervals.keySet() ) {
						Substitution theta = pre.getVariable().match(var);	
						if ( theta != null ) {	
							Substitution sub = theta.copy();
							for ( Term interval : sortedIntervals.get(var) ) {	
								Statement s = cDB.getStatement(interval);
								if ( s.getValue().equals(pre.getValue()) ) {
									if ( !goalStatements.contains(s) ) {
										Term newKey = pre.getKey().makeConstant();
										sub.add(pre.getKey(),newKey);								
										
										AllenConstraint newLink = new AllenConstraint(interval, newKey, TemporalRelation.Equals);
										
										if ( verbose ) print("    Possible link (open-precondition): " + newLink, 3);
										
										possibleMachtes.add(sub);
										possibleLinks.add(newLink);
									}
								}
							}
						}
						
					}
//					for ( Statement s : cDB.getStatements() ) {
//						if ( !goalStatements.contains(s) ) {
//							Substitution theta = pre.matchWithoutKey(s);	
//							if ( theta != null ) {		
//								Term newKey = pre.getKey().copy();
//								newKey.makeConstant();
//								theta.add(pre.getKey(),newKey);									
//								AllenConstraint newLink = new AllenConstraint(s.getKey().copy(), newKey, TemporalRelation.Equals);
//								newLink.setDescription("Causal Link");
//								
//								if ( verbose ) print("    Possible link (open-precondition): " + newLink, 3);
//								
//								possibleMachtes.add(theta);
//								possibleLinks.add(newLink);
//							}
//						}
//					}
					if ( possibleMachtes.isEmpty() ) {
						System.err.println(cDB);
						for ( Operator ac : plan.getActions() ) {
							System.err.println("-> " + ac);
						}
						throw new IllegalStateException("Statement (precondition) " + pre.toString() + " does not exist in initial context.");
					}
				
					Collections.reverse(possibleMachtes); 
					matchesForAllOpenPreconditions.add(possibleMachtes);
					Collections.reverse(possibleLinks); 
					linksForAllOpenPreconditions.add(possibleLinks);
				} 
			}
		}	
		
		/**
		 * If goal has to be added (necessary for solutions)
		 * we need to map open goal statements to any possible statement from plan 
		 * or context.
		 */
		addGoals = true; //TODO: Why was this set to always true? And why did it not crash more often?
		if ( addGoals ) {
			for ( Statement goal : goalStatements ) {	
				if ( verbose ) print("Goal: " + goal, 3);
				ArrayList<Substitution> possibleMatches = new ArrayList<Substitution>();
				ArrayList<AllenConstraint> possibleLinks = new ArrayList<AllenConstraint>();
				for ( Atomic var : sortedIntervals.keySet() ) {
					Substitution theta = goal.getVariable().match(var);	
					
					if ( theta != null ) {		

						Substitution sub = theta.copy();
						for ( Term interval : sortedIntervals.get(var) ) {
							Statement s = cDB.getStatement(interval);
							if ( s.getValue().equals(goal.getValue()) ) {
								if ( !goalStatements.contains(s) ) {
//									Term newKey = goal.getKey().makeConstant();
//									sub.add(goal.getKey(),newKey);								
									AllenConstraint newLink = new AllenConstraint(interval, goal.getKey(), TemporalRelation.Equals);
//									AllenConstraint newLink = new AllenConstraint(interval, newKey, TemporalRelation.Equals);
									
									if ( verbose ) print("    Possible link (goal): " + newLink, 3);
									
									possibleMatches.add(sub);
									possibleLinks.add(newLink);
								}
							}
						}
					}
					
				}
//				for ( Statement s : cDB.getStatements() ) {
//					if ( !goalStatements.contains(s) ) {
//						Substitution theta = goal.matchWithoutKey(s);	
//						if ( theta != null ) {		
//							
//							Term newKey = goal.getKey().copy();
//							newKey.makeConstant();
//							theta.add(goal.getKey(),newKey);									
//			
//							AllenConstraint newLink = new AllenConstraint(s.getKey().copy(), newKey, ConstraintTypes.TemporalRelation.Equals);
//							newLink.setDescription("Causal Link");
//							
//							if ( verbose ) print("    Possible link (goal): " + newLink, 3);
//							
//							possibleLinks.add(newLink);									
//							possibleMachtes.add(theta);	
//						}
//					}
//				}
				for ( Operator a : p.getActions() ) {
					for ( Statement s : a.getEffects() ) {
						Substitution theta = goal.matchWithoutKey(s);	
						if ( theta != null ) {		
//							Term newKey = goal.getKey().makeConstant();
//							sub.add(goal.getKey(),newKey);								
							AllenConstraint newLink = new AllenConstraint(s.getKey(), goal.getKey(), TemporalRelation.Equals);
//							AllenConstraint newLink = new AllenConstraint(interval, newKey, TemporalRelation.Equals);
//							Term newKey = goal.getKey().makeConstant();
//							theta.add(goal.getKey(),newKey);		
							
//							AllenConstraint newLink = new AllenConstraint(s.getKey(), newKey, ExpressionTypes.TemporalRelation.Equals);
							
							if ( verbose ) print("    Possible link (goal): " + newLink, 3);
							
							possibleLinks.add(newLink);									
							possibleMatches.add(theta);	
						}
					}
				} 
								
				if ( possibleMatches.isEmpty() ) {
					//TODO: Adding only goals that exist instead of throwing an exception. Is this the right thing todo?
//					throw new IllegalStateException("Statement (goal) " + goal.toString() + " does not exist in context. Asserted goals whose achieving statements were forgotten can throw this exception.");
				}  else {
				
					// reverse list to try last one added first, since later effects are more likely to be intended for goals
					Collections.reverse(possibleMatches); 
					matchesForAllOpenPreconditions.add(possibleMatches);
					Collections.reverse(possibleLinks); 
					linksForAllOpenPreconditions.add(possibleLinks);
				}	
			}
		}
		
		for ( int i = 0 ; i < matchesForAllOpenPreconditions.size() ; i++ ) {
			if ( matchesForAllOpenPreconditions.get(i).size() != linksForAllOpenPreconditions.get(i).size() ) {
				throw new IllegalStateException("Something went wrong... (a)");
			}
		}
		
		/**
		 * Take out forced substitutions (with only one choice) so that this module
		 * does not need to propose them one by one.
		 */
		int numChoices = 1;
		forcedLinks = new ArrayList<AllenConstraint>();
		forcedSubs = new Substitution();
		for ( int j = 0 ; j < matchesForAllOpenPreconditions.size() ; j++ ) {
			if ( matchesForAllOpenPreconditions.get(j).size() == 1 ) {
				if ( !forcedSubs.add(matchesForAllOpenPreconditions.get(j).get(0)) ) {
					throw new IllegalStateException("Substitution " + matchesForAllOpenPreconditions.get(j).get(0)+ " is not compatible with " + forcedSubs 
							+ ". This shoud never happen since all variables that are substituted in this module are supposed to be unique.");
				}
				forcedLinks.add(linksForAllOpenPreconditions.get(j).get(0));
				matchesForAllOpenPreconditions.remove(j);
				linksForAllOpenPreconditions.remove(j);
				j--;						
			} else {
				numChoices *= matchesForAllOpenPreconditions.get(j).size();
			}
		}
		
		if ( verbose ) print("Removed forced substitutions. Left with " + numChoices + " combination(s) of choices for open precondition keys.",1);

		
		
		for ( int i = 0 ; i < matchesForAllOpenPreconditions.size() ; i++ ) {
			if ( matchesForAllOpenPreconditions.get(i).size() != linksForAllOpenPreconditions.get(i).size() ) {
				throw new IllegalStateException("Something went wrong...(b)");
			}
		}
	}

	@Override
	public Resolver next( ConstraintDatabase C ) {
		if ( !firstTime ) {
			if ( verbose ) print("Last node was NoGood",0);
			wasNoGood = true;	
		} 
		
		if ( matchesForAllOpenPreconditions.isEmpty() && !wasNoGood ) { // No choices
			if ( verbose ) print("All substitutions are forced.",1);
			
			subst = new Substitution();
			subst.add(forcedSubs);
			
			causalLinks = new ArrayList<AllenConstraint>();
			for ( AllenConstraint link : forcedLinks ) {
				causalLinks.add(link);
			}	
			
			searchSuccess = true;
			searchDone = true;
		} else if ( matchesForAllOpenPreconditions.isEmpty() && wasNoGood ) { // No choice and NoGood
			searchDone = true;
			searchSuccess = false;						
		} else {
			if ( verbose ) print("Need to search for working combination.",1);
			
			if ( firstTime ) { 
				/** Setup search */
				List<List<Integer>> choices = new ArrayList<List<Integer>>();
				for ( int i = 0 ; i < matchesForAllOpenPreconditions.size() ; i++ ) {
					ArrayList<Integer> choice = new ArrayList<Integer>();
					for ( int j = 0 ; j < matchesForAllOpenPreconditions.get(i).size() ;j++ ) {
						choice.add(Integer.valueOf(j));
					}
					choices.add(choice);
				}
				search = new GenericSingleNodeSearch<Integer>(choices);
				
				print("Initialized search... (with "+choices.size()+" variables)",2);
						
				if ( search.success() )
					searchDone = true;
				else
					searchDone = false;
					
			}
			
			while ( !searchDone ) { 
				/** Advance search (except for first time) */
				if ( verbose ) {
					if ( wasNoGood ) {
						print("Advancing search... (backtracking)",2);	
					} else {
						print("Advancing search... (forward)",2);
					}
					
				}
				searchDone = search.advance(!wasNoGood);
				
				wasNoGood = false;
//				if ( this.incremental ) {
//					outPlan = originalPlan.copy();	
//					outCDB = originalCDB.copy();
//					
//					for ( OpenGoal og : outCDB.get(OpenGoal.class) ) {
//						og.setAsserted(true);
//					}
//					System.out.println(subst);
//
//					outCDB.substitute(subst);
//					outPlan.substitute(subst);
//					outCDB = outPlan.apply(outCDB);
//					outCDB.getConstraints().addAll(causalLinks);
//					outPlan.getConstraints().addAll(causalLinks); 
//					
//					if ( verbose ) {
//						print("Adding causal links: ",3);
//						for ( AllenConstraint cl : causalLinks ) {
//							print("    " + cl,3);
//						}
//					}
//					
//					Core testCore = new Core();
//					testCore.setPlan(outPlan); 
//					testCore.setContext(outCDB);
//					testCore = this.checkingModule.run(testCore);
//					wasNoGood =  testCore.getResultingState(consistencyChecker).equals("Failure") || testCore.getResultingState(consistencyChecker).equals("Inconsistent");
//				}
			}
			
			searchSuccess = search.success();
			
			/**
			 * Add forced substitution and causal links (i.e., the ones without choices)
			 */
			subst = new Substitution();
			subst.add(forcedSubs);
			causalLinks = new ArrayList<AllenConstraint>();
			for ( AllenConstraint link : forcedLinks ) {
				causalLinks.add(link);
			}
			/**
			 * Add current choice
			 */
			List<Integer> a = search.getAssignment();
			for ( int i = 0 ; i < a.size() ; i++ ) {
				subst.add(matchesForAllOpenPreconditions.get(i).get(a.get(i).intValue()));
				causalLinks.add(linksForAllOpenPreconditions.get(i).get(a.get(i).intValue()));
			}
		}

		if ( causalLinks != null ) {	/* Found way to apply plan to initial context */
			/**
			 * Create output according to choices
			 */
			if ( originalPlan != null ) {
				outPlan = originalPlan.copy();	
			} else {
				outPlan = new Plan();
			}
			
//			outPlan.getConstraints().addAll(causalLinks); // Caused a problem when creating new operators from Plan
//			outCDB = core.getInitialContext().copy();
			outCDB = originalCDB.copy();
			for ( OpenGoal og : outCDB.get(OpenGoal.class) ) {
				if ( !og.isAsserted() ) {
					if ( verbose ) print("Adding goal statement: "+og.getStatement(),2);
					outCDB.add(og.getStatement()); 
				}
			}

			outCDB.substitute(subst);
			outPlan.substitute(subst);
			outCDB = outPlan.apply(outCDB);
			outCDB.addAll(causalLinks);
			outPlan.getConstraints().addAll(causalLinks); 
						
			if ( verbose ) {
				print("Adding causal links: ",3);
				for ( AllenConstraint cl : causalLinks ) {
					print("    " + cl,3);
				}
			}
		}
		
		Resolver r = null;
				
		if ( searchSuccess ) {
			ConstraintDatabase rCDB = new ConstraintDatabase();
			rCDB.add(outPlan);
			
			r = new Resolver(subst, outPlan.apply(rCDB));

			searchSuccess = false;	
			searchDone = false;	
		} 		
		 
		firstTime = false;
		Logger.depth--;
		return r;			
	}
}
