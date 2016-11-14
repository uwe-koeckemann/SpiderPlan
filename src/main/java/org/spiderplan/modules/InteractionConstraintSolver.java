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
import java.util.Map;
import java.util.Stack;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ConstraintRetrieval;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.interaction.InteractionConstraint;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Solves {@link InteractionConstraint}s
 * 
 * @author Uwe Köckemann
 */
public class InteractionConstraintSolver extends Module implements SolverInterface {

	Module consistencyChecker;
	String consistencyCheckerName;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public InteractionConstraintSolver(String name, ConfigurationManager cM ) {
		super(name, cM);

		super.parameterDesc.add( new ParameterDescription("consistencyChecker", "String", "", false, "Name of the module that verifies consistency of constraint databases.") );
				
		this.consistencyCheckerName = cM.getString(this.getName(), "consistencyChecker" );
		
		consistencyChecker = ModuleFactory.initModule( this.consistencyCheckerName, cM );
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		boolean isSatisfiable = true;
		
		Stack<ConstraintDatabase> cDBstack = new Stack<ConstraintDatabase>();
		cDBstack.push(core.getContext().copy());
		Stack<ResolverIterator> stack = new Stack<ResolverIterator>();
		
		ConstraintDatabase current = core.getContext().copy();
		
		Core testCore = new Core();
		testCore.setContext(current);
		testCore.setTypeManager(core.getTypeManager());
		
		SolverResult result = new SolverResult(State.Searching);
		
		while ( result.getState().equals(State.Searching ) && isSatisfiable ) {
//			System.out.println(result.state + " " +  stack.size() );
//			System.out.println("Current beginning of loop: " + current.getStatements().size() + " and " + current.getConstraints().size());
			result = this.testAndResolve(testCore);
			if ( result.getState().equals(State.Searching) ) {
				stack.push(result.getResolverIterator());
				cDBstack.push(current);
				Resolver r = null;
				do {
					r = stack.peek().next();
					
					if ( r == null ) {
//						System.out.println("No more resolvers...");
						stack.pop();
						cDBstack.pop();
						if ( stack.isEmpty() ) {
							isSatisfiable = false;
							break;
						}
					} else {
//						System.out.println("Found resolver...");
						current = cDBstack.peek().copy();
						r.apply(current);
//						System.out.println(r.getConstraintDatabase());
						testCore.setContext(current);
//						System.out.println("Current after applying: " + current.getStatements().size() + " and " + current.getConstraints().size());
						testCore = consistencyChecker.run(testCore);
					}
				} while ( !testCore.getResultingState(consistencyCheckerName).equals(State.Consistent) ); 
					
//				System.out.println("Resolver consistent...");
				
				if ( !isSatisfiable ) {
					break;
				}
			}
		}
		
		if ( !isSatisfiable ) {
			core.setResultingState(getName(), State.Inconsistent);
		} else {
			core.setResultingState(getName(), State.Consistent);
			core.setContext(current);
		}
		
		if ( verbose ) Logger.depth--;
		return core;
	}

//	@Override
//	public Core run(Core core) {
//		if ( killFlag ) {
//			core.setOutSignal(this.getName(), State.Killed);
//			return core;
//		}
//		depth++;
//		
//		if ( keepStats ) {
//			ArrayList<InteractionConstraint> ICs = new ArrayList<InteractionConstraint>();
//			//ICs.addAll(core.get(InteractionConstraint.class));		
//			ICs.addAll(core.getContext().get(InteractionConstraint.class));
//					
//			for ( InteractionConstraint ic : ICs ) {
//				for ( int i = 0 ; i < ic.getResolvers().size() ; i++ ) {
//					stats.setLong(msg(ic.getName().name() + "/" + i), Long.valueOf(0));
//				}
//			}
//		}
//				
//		core.setContext(core.getContext().copy());
//		Core r = resolveOneConflict(core);
//		
//		if ( r != null ) {
//			if ( core.getInSignals().contains("AddConstraints") ) {
//				core.setContext(r.getContext());
//				core.getContext().addConstraints(r.getContext().getConstraints());
//				core.setOutSignal(getName(), State.Consistent);
//				if ( verbose ) print("Consistent (adding constraints)",0);
//			} else {
//				core.setOutSignal(getName(), State.Consistent);
//				if ( verbose ) print("Consistent",0);
//			}
//		} else {
//			if ( verbose ) print("Inconsistent",0);
//			core.setOutSignal(getName(), State.Inconsistent);
//		}
//		
//		if ( keepStats ) {
//			ArrayList<InteractionConstraint> ICs = new ArrayList<InteractionConstraint>();
////			ICs.addAll(core.get(InteractionConstraint.class));		
//			ICs.addAll(core.getContext().get(InteractionConstraint.class));
//					
//			for ( InteractionConstraint ic : ICs ) {
//				if ( ic.isAsserted() ) {
//					stats.addToLong(msg(ic.getName().name() + "/" + ic.getResolverIndex()), Long.valueOf(1));
//				}
//			}
//		}
//		
//		this.stopWatch.absorbRecords(this.consistencyChecker.stopWatch);
//		depth--;
//		return core;
//	}

//	/**
//	 * Recursively resolves single flaws. Recursion terminates iff no resolvers can be found or 
//	 * no flaws can be detected.
//	 * @param core {@link Core} that needs resolving
//	 * @return {@link Core} that satisfies all {@link InteractionConstraint} or {@link Core} with 
//	 * {@link State} Inconsistent
//	 */
//	private Core resolveOneConflict(Core core) {
//				
//		ArrayList<InteractionConstraint> ICs = new ArrayList<InteractionConstraint>();
//		ICs.addAll(core.getContext().get(InteractionConstraint.class));
//				
//		ConstraintDatabase cdb = core.getContext().copy();
//		for ( OpenGoal og : cdb.get(OpenGoal.class)) {
//			cdb.add(og.getStatement());
//		}
//		TemporalNetworkTools.compressTemporalConstraints(cdb);				
//		
//		for ( InteractionConstraint ic : ICs ) {		
//			if ( !ic.isAsserted() ) {					
//				
//				ArrayList<Substitution> enablingSubst = new ArrayList<Substitution>();
//				enablingSubst.addAll(cdb.getSubstitutions(ic.getCondition()));
//									
//				if ( verbose ) Logger.msg(getName() ,"Found "+ enablingSubst.size() +" potential applicants for: " + ic.getName(), 4);
//				if ( keepStats ) stats.addToLong(msg("|ICs|"), Long.valueOf(enablingSubst.size()));
//				
//				for ( int i = 0 ; i < enablingSubst.size() ; i++ ) {	
//					/* Create copy of IC and make all its variables unique */
//					InteractionConstraint icCopy = ic.copy();
//					icCopy.substitute(enablingSubst.get(i));
//					Collection<Term> vars = icCopy.getVariableTerms();
//					Substitution uniqueSub = new Substitution();
//					long ID = UniqueID.getID();
//					for ( Term var : vars ) {
//						Term uniqueVar = new Term(var.toString()+"_"+ID);
//						uniqueSub.add(var, uniqueVar);
//					}
//					icCopy.substitute(uniqueSub);
//					
//					if ( !ConstraintRetrieval.hasNoMatchingAssertedConstraint(core.getContext(), icCopy) ) {
//						if ( verbose ) Logger.msg(getName() ,"Already resolved: " + ic.getName(), 3);
//					} else {
//						ConstraintDatabase enabledDB = core.getContext().copy();
//						
////						ArrayList<InteractionConstraint> icsInCoreGlobal = new ArrayList<InteractionConstraint>();
//						ArrayList<InteractionConstraint> icsInCoreContext = new ArrayList<InteractionConstraint>();
//						
////						for ( InteractionConstraint icIn : core.get(InteractionConstraint.class) ) {
////							if ( !icIn.isAsserted() ) {
////								icsInCoreGlobal.add(icIn);
////							}
////						}
//						for ( InteractionConstraint icIn : core.getContext().get(InteractionConstraint.class) ) {
//							if ( !icIn.isAsserted() ) {
//								icsInCoreContext.add(icIn);
//							}
//						}
//						
//						enabledDB.addConstraints(icCopy.getCondition().getConstraints());
//						enabledDB.getConstraints().removeAll(icsInCoreContext);	// (a) We only consider ICs belonging to the tested condition
//						
////						ConstraintCollection enabledCons = core.getContext().getConstraints().copy();
////						enabledCons.removeAll(icsInCoreGlobal);
//														
//						if ( verbose ) Logger.msg(getName() ,"Trying condition for " + icCopy.getName(), 2);
//			
//						if ( verbose ) Logger.depth++;
//						Core checkCore = new Core();
//						checkCore.setContext(enabledDB);
//						checkCore.setPlan(core.getPlan());
//						checkCore.setOperators(core.getOperators());;
//						checkCore.setTypeManager(core.getTypeManager());
////						checkCore.setConstraints(enabledCons);
//								
//						if ( verbose ) Logger.msg(getName(), "Running IC consistency module...", 3);
//						checkCore = consistencyChecker.run(checkCore, this.verbose, this.verbosity);
//						if ( keepStats ) stats.increment(msg("Potential meta variables"));
//						
//						if ( verbose ) Logger.depth--;
//						
//						State outSignal = checkCore.getResultingState(consistencyCheckerName);
//						
//						if ( outSignal.equals(Core.State.Consistent) ) {
//							if ( verbose ) Logger.msg(getName(), "Condition applies: " +  icCopy.getName()+")", 2);
//							if ( verbose ) Logger.msg(getName(), "Trying to find consistent resolver...", 2);
//							if ( keepStats ) stats.increment(msg("Meta variables"));
//							
//							for ( int j = 0 ; j < icCopy.getResolvers().size(); j++ ) {
//								ConstraintDatabase resolver = icCopy.getResolvers().get(j);
//								/* Make interval keys that are added by resolver ground */
//								Substitution groundIntervalSubst = new Substitution();
//								for ( Statement s : resolver.getStatements() ) {
//									if ( !s.getKey().isGround() ) {
//										Term groundKey = s.getKey().copy();
//										groundKey.makeConstant();
//										groundIntervalSubst.add(s.getKey(), groundKey);
//									}
//								}
//
//								icCopy.substitute(groundIntervalSubst);
//								icCopy.setResolverIndex(j);
//								
//								if ( verbose ) Logger.msg(getName(),"Trying resolver:\n" + resolver, 2);
//								
//								
//								
//								if ( verbose ) Logger.depth++;
//								
//								if ( keepStats ) stats.increment(msg("Tried meta values"));
//														
//								ConstraintDatabase resolvedDB = core.getContext().copy();
//								resolvedDB.add(resolver);
//								icCopy.setAsserted(true);
//								resolvedDB.add(icCopy);
//
//								checkCore = new Core();
//								checkCore.setContext(resolvedDB);
//								checkCore.setPlan(core.getPlan().copy());
//								checkCore.setOperators(core.getOperators());
//								checkCore.setTypeManager(core.getTypeManager());
////								checkCore.setConstraints(core.getConstraints().copy());
//								
////								checkCore.getConstraints().removeAll(icsInCoreGlobal);
//								checkCore.getContext().getConstraints().removeAll(icsInCoreContext);
//								
//								if ( verbose ) Logger.msg(getName(), "Running IC consistency module...", 3);
//								checkCore = consistencyChecker.run(checkCore, this.verbose, this.verbosity);
//								
//								outSignal = checkCore.getResultingState(consistencyCheckerName);					
//								if ( verbose ) Logger.depth--;						
//								if ( outSignal.equals(Core.State.Consistent) ) {
//									if ( verbose ) Logger.msg(getName(), "Resolver consistent", 3);							
//									if ( verbose ) Logger.msg(getName(),"Recursive call", 2);
//									if ( verbose ) if ( verbose ) Logger.depth++;
//									if ( keepStats ) stats.increment(msg("#Forward"));	
//									
////									checkCore.getConstraints().addAll(icsInCoreGlobal);
//									checkCore.getContext().getConstraints().addAll(icsInCoreContext);
//									
//									
//									Core returnCore = this.resolveOneConflict(checkCore);
//									if ( verbose ) if ( verbose ) Logger.depth--;
//									if ( verbose ) Logger.msg(getName(),"Returned from recursive call", 2);
//									
//									if ( returnCore != null ) {
//										return returnCore;
//									} 
//								} else {
//									if ( verbose ) Logger.msg(getName(), "Resolver inconsistent", 3);
//								}	
//								icCopy.setAsserted(false);
//							}
//							if ( verbose ) Logger.msg(getName(), "No consistent resolver found: Backtracking", 2);
//							if ( keepStats ) stats.increment(msg("#Backtracking"));			
//							
//							return null; 
//						} else {
//							if ( verbose ) Logger.msg(getName(), "Condition inconsistent for "+icCopy.getName(), 3);
//						}
//					} 
//				}
//			}
//		}
//				
//		return core;	
//	}

	@Override
	public SolverResult testAndResolve(Core core) {
				
		if ( keepTimes ) StopWatch.start(msg("1) Preparing"));
		ArrayList<InteractionConstraint> ICs = new ArrayList<InteractionConstraint>();
		ICs.addAll(core.getContext().get(InteractionConstraint.class));
				
		ConstraintDatabase cdb = core.getContext().copy();
		for ( OpenGoal og : cdb.get(OpenGoal.class)) {
			cdb.add(og.getStatement());
		}
			
//		TemporalNetworkTools.compressTemporalConstraints(cdb);
				
		ArrayList<InteractionConstraint> icsInCoreContext = new ArrayList<InteractionConstraint>();	
		for ( InteractionConstraint icIn : core.getContext().get(InteractionConstraint.class) ) {
			if ( !icIn.isAsserted() ) {
				icsInCoreContext.add(icIn);
			}
		}
		
		ConstraintDatabase enabledDB = core.getContext().copy();
		enabledDB.removeAll(icsInCoreContext);	// (a) We only consider ICs belonging to the tested condition
		Map<Class<? extends Expression>,Integer> cCount = enabledDB.getConstraintCount();
		
		Collection<Term> variablesInCDB = enabledDB.getVariableTerms();
		
//		ConstraintDatabase focused = new ConstraintDatabase();
//		focused.addStatements(TemporalNetworkTools.getStatementsInPlanningInterval(cdb, core.getTypeManager()));
		if ( keepTimes ) StopWatch.stop(msg("1) Preparing"));

		for ( InteractionConstraint ic : ICs ) {				
			if ( !ic.isAsserted() ) {					
//				ArrayList<Substitution> enablingSubst = new ArrayList<Substitution>();
				if ( keepTimes ) StopWatch.start(msg("2) Getting enablers"));
//				enablingSubst.addAll(cdb.getSubstitutions(ic.getCondition()));
				
//				GenericComboIterator<Substitution> enablerIterator = cdb.getSubstitutionIterator(ic.getCondition());
				
				GenericSingleNodeSearch<Substitution> enablerSearch = cdb.getSubstitutionSearch(ic.getCondition());
				
				if ( keepTimes ) StopWatch.stop(msg("2) Getting enablers"));					
//				if ( verbose ) Logger.msg(getName() ,"Found "+ enablerSearch.getNumCombos() +" potential applicants for: " + ic.getName(), 4);
//				if ( keepStats ) stats.addToLong(msg("|ICs|"), Long.valueOf(enablingSubst.size()));
//				for ( int i = 0 ; i < enablingSubst.size() ; i++ ) {
//				boolean foundFlaw = false;
				boolean approvedSubstitution;
				if ( enablerSearch != null ) {
					while ( !enablerSearch.failure() ) {
						if ( verbose ) Logger.msg(getName() ,"Next try "+ enablerSearch.getAssignment(), 2);
	
						if ( keepTimes ) StopWatch.start(msg("3) Preparing next enabler"));
						Substitution enabler = new Substitution();
						for ( Substitution theta_i : enablerSearch.getAssignment() ) {
							if ( !enabler.add(theta_i) ) {
								enabler = null;
								break; 
							}
						}
						if ( keepTimes ) StopWatch.stop(msg("3) Preparing next enabler"));
						
						if ( enabler == null ) {
							enablerSearch.advance(false);
							continue; // illegal substitution -> try next one
						}
						
						if ( keepTimes ) StopWatch.start(msg("4) Preparing enabled IC"));
						/* Create copy of IC and make all its variables unique */
						InteractionConstraint icCopy = ic.copy();
						icCopy.substitute(enabler);
	
						Asserted a = new Asserted(icCopy);
						if ( cdb.contains(a) ) {
							if ( keepTimes ) StopWatch.stop(msg("4) Preparing enabled IC"));
							enablerSearch.advance(false);
							continue;
						}
			
						icCopy.makeUniqueGroundKeys();
						if ( keepTimes ) StopWatch.stop(msg("4) Preparing enabled IC"));
						boolean alreadyResolved = false;
						
						
						if ( enablerSearch.success() ) {
							if ( keepTimes ) StopWatch.start(msg("5) IC already asserted?"));
							alreadyResolved = !ConstraintRetrieval.hasNoEqualAssertedConstraint(core.getContext(), icCopy);
							if ( keepTimes ) StopWatch.stop(msg("5) IC already asserted?"));
						}
						
						if ( alreadyResolved ) {
							if ( verbose ) Logger.msg(getName() ,"Already resolved:" + icCopy.getName(), 3);
							enablerSearch.advance(false);
							continue;
						} else {
							if ( keepTimes ) StopWatch.start(msg("6) Preparing condition"));
							
							List<Statement> remList = new ArrayList<Statement>();
							List<Statement> S = enabledDB.get(Statement.class);
							
							for ( Statement s : icCopy.getCondition().get(Statement.class)){
								if ( !S.contains(s)) {
									remList.add(s);
								}
							}

							enabledDB.addAll(icCopy.getCondition());
							enabledDB.removeAll(remList);
							
							if ( verbose ) Logger.msg(getName() ,"Trying condition for " + icCopy.getName(), 2);
							
							if ( verbose ) Logger.depth++;
							Core checkCore = new Core();
							checkCore.setContext(enabledDB);
							Plan p = core.getContext().getUnique(Plan.class);
							if ( p != null ) {
								enabledDB.add(core.getContext().getUnique(Plan.class));
							}
							checkCore.setOperators(core.getOperators());;
							checkCore.setTypeManager(core.getTypeManager());						
							if ( keepTimes ) StopWatch.stop(msg("6) Preparing condition"));
							
						
							if ( verbose ) Logger.msg(getName(), "Running IC consistency module...", 3);
							if ( keepTimes ) StopWatch.start(msg("7) Testing condition"));
							checkCore = consistencyChecker.run(checkCore, this.verbose, this.verbosity);
							if ( keepTimes ) StopWatch.stop(msg("7) Testing condition"));
							if ( keepStats ) Statistics.increment(msg("Testing condition"));
							
							enabledDB.setToConstraintCount(cCount);
							
							if ( verbose ) Logger.depth--;
							
							State outSignal = checkCore.getResultingState(consistencyCheckerName);
							
							approvedSubstitution = outSignal.equals(Core.State.Consistent);
							
							if ( enablerSearch.success() && approvedSubstitution ) { // search successful -> needs resolving
								if ( verbose ) Logger.msg(getName(), "Condition applies: " +  icCopy.getName()+")", 0);
								if ( verbose ) Logger.msg(getName(), "Trying to find consistent resolver...", 2);
								if ( keepStats ) Statistics.increment(msg("Satisfied Condition"));
								
								if ( keepTimes ) StopWatch.start(msg("8) Setting up resolver iterator"));
								List<Resolver> resolverList = new ArrayList<Resolver>();
								
								for ( int j = 0 ; j < icCopy.getResolvers().size(); j++ ) {
									ConstraintDatabase resolverCDB = icCopy.getResolvers().get(j).copy();
									/* Make interval keys that are added by resolver ground */
	//								Substitution groundIntervalSubst = new Substitution();
	//								for ( Statement s : resolverCDB.getStatements() ) {
	//									if ( !s.getKey().isGround() ) {
	//										groundIntervalSubst.add(s.getKey(), s.getKey().makeConstant());
	////										groundIntervalSubst.add(s.getKey(), Term.createConstantID());
	//									}
	//								}
	//
	//								icCopy.substitute(groundIntervalSubst);
									
									Substitution s = new Substitution();
									long ID = UniqueID.getID();
									for ( Term var : icCopy.getVariableTerms() ) {
										if ( !variablesInCDB.contains(var) ) {
											Term newVar = var.makeUnique(ID);
											s.add(var, newVar);
										}
									}
									icCopy.substitute(s);
									
									
									icCopy.setResolverIndex(j);
									resolverCDB.substitute(s);
									
									
									resolverCDB.add(icCopy.copy());
									resolverCDB.add(new Asserted(icCopy));
									Resolver r = new Resolver(resolverCDB);
									resolverList.add(r);
								}
								State state;
								ResolverIterator resolverIterator = null;
								if ( resolverList.isEmpty() ) {
									state = State.Inconsistent;
								} else {
									resolverIterator = new ResolverList(resolverList, this.getName(), this.cM);
									state = State.Searching;
								}
								if ( keepTimes ) StopWatch.stop(msg("8) Setting up resolver iterator"));
								return new SolverResult(state,resolverIterator);
							} else {
								enablerSearch.advance(approvedSubstitution);
							}
						} 
					}
				}
			}
		}
		return new SolverResult(State.Consistent);	
	}
}
