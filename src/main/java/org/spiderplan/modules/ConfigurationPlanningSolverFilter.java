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
package org.spiderplan.modules;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.spiderplan.minizinc.MiniZincAdapter;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverCombination;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.ConfigurationPlanningRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.configurationPlanning.ConfigurationPlanningConstraint;
import org.spiderplan.representation.expressions.cost.Cost;
import org.spiderplan.representation.expressions.graph.GraphConstraint;
import org.spiderplan.representation.expressions.math.MathConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.logic.IntegerTerm;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Handles {@link Expression}s of type {@link GraphConstraint}.
 * 
 * Does not yet support backtracking over its decisions.
 * This will become much easier after a general overhaul of the way
 * conflicts are resolved.
 * 
 * @author Uwe Köckemann
 *
 */
public class ConfigurationPlanningSolverFilter extends Module implements SolverInterface { 
	
	private ResolverIterator resolverIterator = null;
	private ConstraintDatabase originalContext = null;
	
	String minizincBinaryLocation = "minizinc"; 
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public ConfigurationPlanningSolverFilter(String name, ConfigurationManager cM) {
		super(name, cM);
		
		
		
		super.parameterDesc.add( new ParameterDescription("binaryLocation", "string", "minizinc", "Set minizink binary location.") );
		
		if ( cM.hasAttribute(name, "binaryLocation")  ) {
			minizincBinaryLocation = cM.getString(this.name, "binaryLocation");
		}
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
				
		if ( core.getInSignals().contains("FromScratch") ) {
			Logger.msg(getName(),"Running FromScratch", 0);
			core.getInSignals().remove("FromScratch");
			resolverIterator = null;
		}
		
		boolean isConsistent = true;
		
		if ( resolverIterator == null ) {
			SolverResult result = testAndResolve(core);
			originalContext = core.getContext().copy();
			if ( result.getState().equals(State.Searching) ) {
				resolverIterator = result.getResolverIterator();
			} else if (  result.getState().equals(State.Inconsistent)  ) {
				isConsistent = false;
			}
		} 
			
		if ( isConsistent ) {
			if ( resolverIterator == null ) {
				core.setResultingState( getName(), State.Consistent );
			} else {
				Resolver r = resolverIterator.next();
				if ( r == null ) {
					core.setResultingState( getName(), State.Inconsistent );
				} else {
					ConstraintDatabase cDB = originalContext.copy();
					r.apply(cDB);
					core.setContext(cDB);
					core.setResultingState(getName(), State.Consistent);
				}
			}
		} else {
			core.setResultingState( getName(), State.Inconsistent );
		} 
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		
		boolean isConsistent = true;
		
		Collection<ConfigurationPlanningConstraint> C = core.getContext().get(ConfigurationPlanningConstraint.class);
		
		List<Term> concepts = new ArrayList<Term>();
		
//		List<Expression> goals = new ArrayList<Expression>();
		List<Term> targetIntervals = new ArrayList<Term>();
		List<Term> targetConcepts = new ArrayList<Term>();

		List<Term> ruleTargets = new ArrayList<Term>();
		List<List<Term>> rules = new ArrayList<List<Term>>();
		
		List<Term> unavailable = new ArrayList<Term>();
		List<Integer> costs = new ArrayList<Integer>();
		
		Map<Term,Integer> idLookup = new HashMap<Term, Integer>();
		List<Term> ruleNames = new ArrayList<Term>();
		
		String pathToProgram = "./src/main/minizinc/configuration-planning/configuration-planning.mzn";
		if ( verbose ) {
			Logger.msg(getName(), "Loading MiniZinc program file from " + pathToProgram, 1);
		}
		String program = "";
		try {
			Scanner scanner = new Scanner(new java.io.File(pathToProgram));
			scanner.useDelimiter("\\Z");			
			program  = scanner.next();
			scanner.close();
//			program = new Scanner(new File(pathToProgram)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		ConfigurationPlanningConstraint selectedGoal = null;
		Long goalEST = null;
		ValueLookup vLookup = core.getContext().getUnique(ValueLookup.class);
		
		
//		for ( int i = 0 ; i < goals.size() ; i++  ) {
//			Term goalInterval = targetIntervals.get(i);
//			if ( vLookup.hasInterval(goalInterval) ) {
//				if ( EST == null || vLookup.getEST(goalInterval) < EST ) {
//					EST = vLookup.getEST(goalInterval);
//					selectedGoal = goals.get(i);
//					selectedTargetConcept = targetConcepts.get(i);
//					selectedTargetInterval = targetIntervals.get(i);
//				}
//			}
//		}
		
//		if ( keepTimes ) StopWatch.start("["+this.getName()+"] Selecting goal...");
		Term r;
		for ( ConfigurationPlanningConstraint c : C ) {
			r = c.getConstraint();
			if ( c.getRelation().equals(ConfigurationPlanningRelation.Goal) ) {
				if ( !c.isAsserted() ) {
					Term goalInterval = c.getConstraint().getArg(0);
					if ( selectedGoal == null || vLookup.getEST(goalInterval) < goalEST ) {
						selectedGoal = c;
						goalEST = vLookup.getEST(goalInterval);
						if ( verbose ) Logger.msg(getName(), "    Selected goal: " + c, 2);
						targetIntervals.add(r.getArg(0));
						targetConcepts.add(r.getArg(1));
						if ( !concepts.contains(r.getArg(1)) ) {
							concepts.add(r.getArg(1));
						}
					}
				}
			}
		}
		List<List<Resolver>> resolverLists = new ArrayList<List<Resolver>>();
		
		if ( selectedGoal != null ) {
			List<ConfigurationPlanningConstraint> selectedLinks = new ArrayList<ConfigurationPlanningConstraint>();
			List<Term> reachable = new ArrayList<Term>();
			reachable.add(selectedGoal.getConstraint().getArg(1));
			boolean change = true;
			while ( change ) {
				change = false;
				for ( ConfigurationPlanningConstraint c : C ) {
					if ( c.getRelation().equals(ConfigurationPlanningRelation.Link) ) {
						Term linkTarget = c.getConstraint().getArg(0);
						Term linkDependencies = c.getConstraint().getArg(1);
						
						if ( reachable.contains(linkTarget) && !selectedLinks.contains(c) ) {
							if ( verbose ) Logger.msg(getName(), "    Adding link: " + c, 2);
							
							change = true;
							selectedLinks.add(c);
							for ( int i = 0 ; i < linkDependencies.getNumArgs() ; i++ ) {
								reachable.add(linkDependencies.getArg(i));
							}
						}
					} 
				}
			}
//			goals.add(selectedGoal);
			targetIntervals.add(selectedGoal.getConstraint().getArg(0));
			targetConcepts.add(selectedGoal.getConstraint().getArg(1));
			if ( !concepts.contains(selectedGoal.getConstraint().getArg(1)) ) {
				concepts.add(selectedGoal.getConstraint().getArg(1));
			}
			
			for ( ConfigurationPlanningConstraint c : selectedLinks ) {
				r = c.getConstraint();
				if ( verbose ) Logger.msg(getName(), "    " + c, 2);
								
				ruleNames.add(c.getConstraint());
				ruleTargets.add(r.getArg(0));
				if ( !concepts.contains(r.getArg(0)) ) {
					concepts.add(r.getArg(0));
				}
				
				List<Term> required = new ArrayList<Term>();
				for ( int i = 0 ; i < r.getArg(1).getNumArgs() ; i++ ) {
					Term req = r.getArg(1).getArg(i);
					required.add(req);
					if ( !concepts.contains(req) ) {
						concepts.add(req);
					}
				}
				rules.add(required);
				
				// Handle link cost
				if ( r.getNumArgs() > 1 ) {
					if ( r.getArg(2) instanceof IntegerTerm ) {
						costs.add(Integer.valueOf(r.getArg(2).toString()));
					} else {
						costs.add(Integer.valueOf(0));
					}
				}
			}
			
		
			for ( int i = 0 ; i < concepts.size() ; i++ ) {
				idLookup.put(concepts.get(i), i+1);
			}
			
			StringBuilder data = new StringBuilder();
			
			data.append(String.format("n = %d;\n", concepts.size()));
			data.append(String.format("m = %d;\n\n", rules.size()));
			
			
			data.append("conceptNames = [ ");
			data.append("\"");
			data.append(concepts.get(0).toString());
			data.append("\"");
			for ( int i = 1; i < concepts.size() ; i++ ) {
				data.append(", \"");
				data.append(concepts.get(i).toString());
				data.append("\"");
			}
			data.append("];\n\n");
			
			data.append("ruleNames = [ ");
			data.append("\"");
			data.append(ruleNames.get(0).toString());
			data.append("\"");
			for ( int i = 1; i < ruleNames.size() ; i++ ) {
				data.append(", \"");
				data.append(ruleNames.get(i).toString());
				data.append("\"");
			}
			data.append("];\n\n");
			
			
			data.append("ruleTargets = [ ");
			data.append(idLookup.get(ruleTargets.get(0)));
			for ( int i = 1; i < ruleTargets.size() ; i++ ) {
				data.append(", ");
				data.append(idLookup.get(ruleTargets.get(i)));
			}
			data.append("];\n\n");
				
			data.append("rules = [");
	
			for ( int i = 0; i < ruleTargets.size() ; i++ ) {
				String row = "|";
				for ( Term concept : concepts ) {
					if ( rules.get(i).contains(concept) ) {
						row += "1,";
					} else {
						row += "0,";
					}
				}
				
				if ( i == ruleTargets.size()-1 ) {
					row = row.substring(0, row.length()-1) + "|];\n\n";
				}
				
				data.append(row+"\n");
			}
			
			data.append("unavailable = { ");
			if ( !unavailable.isEmpty() ) {
				data.append(idLookup.get(unavailable.get(0)));
				for ( int i = 1; i < unavailable.size() ; i++ ) {
					data.append(", ");
					data.append(idLookup.get(unavailable.get(i)));
				}
			}
			data.append("};\n\n");
			
			data.append("targetConcepts = { <TARGET> };\n\n");
			
			data.append("cost = [ ");
			data.append(costs.get(0));
			for ( int i = 1; i < costs.size() ; i++ ) {
				data.append(", ");
				data.append(costs.get(i));
			}
			data.append("];\n\n");
			
//			if ( keepTimes ) StopWatch.stop("["+this.getName()+"] Creating problem");

//			for ( int i = 0 ; i < targetConcepts.size() ; i++ ) {
				List<Resolver> resolverList = new ArrayList<Resolver>();
				Term targetConcept = selectedGoal.getConstraint().getArg(1);
				
				String dataStr = data.toString().replace("<TARGET>", String.valueOf(idLookup.get(targetConcept)));//targetConcepts.get(i))) );
				if ( keepTimes ) StopWatch.start("["+this.getName()+"] Solving problem");
				String output = MiniZincAdapter.runMiniZincRaw(minizincBinaryLocation, program,  dataStr, true, -1);
				if ( keepTimes ) StopWatch.stop("["+this.getName()+"] Solving problem");
				output = output.replace("==========", "").replace("\n", "");
				
				if ( output.contains("UNSATISFIABLE")) {
					isConsistent = false;
//					break;
				}
				
				Term targetInterval = selectedGoal.getConstraint().getArg(0);//targetIntervals.get(i);
				
				for ( String answer : output.split("----------") ) {
					String chosenConcepts = answer.split(";")[0];
					String chosenRules = answer.split(";")[1];
					List<Statement> inferenceList = new ArrayList<Statement>();
					List<AllenConstraint> tcList = new ArrayList<AllenConstraint>();
					List<MathConstraint> mathList = new ArrayList<MathConstraint>();
					List<Cost> costList = new ArrayList<Cost>();
					Term activationList = Term.parse(chosenConcepts);
					Term ruleList = Term.parse(chosenRules);
					for ( int j = 0 ; j < activationList.getNumArgs() ; j++ ) {
						Term target = activationList.getArg(j);
						Term interval;
						if ( !targetConcept.equals(target) ) {
							interval = Term.createConstant(targetInterval.toString() + "_" + UniqueID.getID());
							tcList.add(new AllenConstraint(targetInterval, interval, TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
							Term var = Term.createComplex("inferring", target);
							Statement s = new Statement(interval, var, Term.createConstant("true"));
							inferenceList.add(s);
						}
						
					}
					for ( int j = 0 ; j < ruleList.getNumArgs() ; j++ ) {
						Term rule = ruleList.getArg(j);
						Term interval = Term.createConstant(targetInterval.toString() + "_R_" + UniqueID.getID());
						tcList.add(new AllenConstraint(targetInterval, interval, TemporalRelation.During, new Interval(Term.createInteger(1), Term.createConstant("inf")), new Interval(Term.createInteger(1), Term.createConstant("inf"))));
						Statement s = new Statement(interval, rule , Term.createConstant("true"));
						inferenceList.add(s);
						
						MathConstraint mathCon = new MathConstraint(Term.parse(String.format("(eval-int (intervalCost %s) (mult (sub (EET %s) (LST %s)) %s))", interval.toString(), interval.toString(), interval.toString(), rule.getArg(2).toString())));
						mathList.add(mathCon);
						
						String costStr = String.format("(add link-cost (intervalCost %s))", interval.toString());
						Cost cost = new Cost(Term.parse(costStr));
						costList.add(cost);
						
						//(:math (eval-int (intervalCost ?E1) (mult (sub (EET ?E1) (LST ?E1)) ?Cost)))
						//(:cost (add link-cost (intervalCost ?E1)))
					}
					
					ConstraintDatabase resDB = new ConstraintDatabase();
					resDB.addAll(inferenceList);
					resDB.addAll(tcList);
					resDB.addAll(mathList);
					resDB.addAll(costList);
//					resDB.add(new Asserted(selectedGoal));
					resDB.add(selectedGoal.getAssertion());
					
					
					if ( verbose ) {
						Logger.msg(getName(), "Resolver for " + selectedGoal, 2);
						Logger.depth++;
						for ( Statement s : inferenceList ) 
							Logger.msg(getName(), s.toString(), 2);
						for ( AllenConstraint tc : tcList) 
							Logger.msg(getName(), tc.toString(), 2);
						Logger.msg(getName(), "-------\n"+resDB.toString(), 2);
						
						Logger.depth--;
					}
					
					Resolver resolver = new Resolver(resDB);
					resolverList.add(resolver);
				}
				
				if ( resolverList.isEmpty() ) {
					isConsistent = false;
//					break;
				}
				Collections.reverse(resolverList); // because minizinc output is from worst to best solution
				resolverLists.add(resolverList);
			}
//		}
								
		State state;
		ResolverIterator resolverIterator = null;
				
		if ( isConsistent && !resolverLists.isEmpty() ){
			state = State.Searching;
			resolverIterator = new ResolverCombination(resolverLists, this.getName(), this.cM);
		} else if ( isConsistent ) {
			state = State.Consistent;
		} else {
			state = State.Inconsistent;
		}
		return new SolverResult(state, resolverIterator);
	}
}
