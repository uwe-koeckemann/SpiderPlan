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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.spiderplan.causal.taskDecomposition.TaskResolverComparator;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.causal.Task;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

/**
 * Resolves tasks via operators.
 * Operators are considered methods if they contain (sub-)tasks as part
 * of their constraints. Fewer sub-tasks are preferred.
 * 
 * @author Uwe Köckemann
 */
public class TaskResolver extends Module implements SolverInterface {
	
	List<String> heuristicNames = new ArrayList<String>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public TaskResolver(String name, ConfigurationManager cM) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		throw new UnsupportedOperationException("This is not implemented...");
	}

	@Override
	public SolverResult testAndResolve(Core core) {	
		Task selectedFlaw = null;
		for ( Task task : core.getContext().get(Task.class) ) {
			if ( !task.isAsserted() ) {
				selectedFlaw = task;
				break;
			}
		}
		
		if ( selectedFlaw == null ) { // nothing to resolve
			return new SolverResult(State.Consistent);
		} else {
			if ( verbose ) Logger.msg(this.getName(), "Selected Task flaw: " + selectedFlaw, 1);
			
			// Value ordering = List order
			List<Resolver> resolvers = this.getResolvers(selectedFlaw, core.getContext(), core.getOperators(), core.getTypeManager());
			
			if ( verbose ) {
				Logger.msg(this.getName(), "Possible resovlers:", 1);
				
				
				int index = 0;
				
				for ( Resolver r : resolvers ) {
					
//					System.out.println(r);
					
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
	

	private ArrayList<Resolver> getResolvers( Task task, ConstraintDatabase cDB, Collection<Operator> O, TypeManager tM ) {
		ArrayList<Resolver> resolvers = new ArrayList<Resolver>();

		for ( Operator o : O ) {
			System.out.println(o.getName());
			Operator oCopy = o.copy();
			long ID = UniqueID.getID();
			oCopy.makeUniqueVariables(ID);	
			
			Statement oName = oCopy.getNameStateVariable();
			Substitution thetaName = task.getStatement().matchWithoutKey(oName);
			if ( thetaName != null ) {
				Operator resOp = oCopy.copy();
				resOp.substitute(thetaName);

				/**
				 * Create the resolver
				 */
				if ( tM.isConsistentVariableTermAssignment(resOp.getName(), null) ) {
					ConstraintDatabase resDB = new ConstraintDatabase();

					resOp.makeEffectIntervalKeysGround();
//					Term newKey = resOp.getLabel();
					Term newKey = null;
					
					newKey = oName.getKey();
										
					/**
					 * Add elements that operator provides (effects and constraints)
					 */
					resDB.add(resOp.getNameStateVariable());
					for ( Statement eff : resOp.getEffects() ) {
						resDB.add(eff);
					}
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
													
					thetaName.add(task.getStatement().getKey(), newKey);
					
					Task tCopy = task.copy().substitute(thetaName);
					tCopy.setAsserted(true);
					
					resDB.add(tCopy.getAssertion());
					
					Resolver r = new Resolver(thetaName, resDB);
					
					resolvers.add(r);
				}
			}
			
			for ( Statement e : oCopy.getEffects() ) {
			
				Substitution theta = task.getStatement().matchWithoutKey(e);
			
				if ( theta != null ) {
					Operator resOp = oCopy.copy();
					resOp.substitute(theta);
	
					/**
					 * Create the resolver
					 */
					if ( tM.isConsistentVariableTermAssignment(resOp.getName(), null) ) {
						ConstraintDatabase resDB = new ConstraintDatabase();
	
						resOp.makeEffectIntervalKeysGround();
//						Term newKey = resOp.getLabel();
						Term newKey = null;
						
						for ( Statement eff : resOp.getEffects() ) {
							if ( eff.getVariable().equals(task.getStatement().getVariable()) && eff.getValue().equals(task.getStatement().getValue()) ) {
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
														
						theta.add(task.getStatement().getKey(), newKey);
						
						Task tCopy = task.copy().substitute(theta);
						tCopy.setAsserted(true);
						
						resDB.add(tCopy.getAssertion());
						
						Resolver r = new Resolver(theta, resDB);
						
						resolvers.add(r);
					}
				}
			}
		}		
	
		Resolver[] resolverArray = resolvers.toArray(new Resolver[resolvers.size()]);
		
		Arrays.sort(resolverArray,new TaskResolverComparator());
		
		resolvers.clear();
		for ( Resolver r : resolverArray ) {
			resolvers.add(r);
		}
								
		return resolvers;
	}
	
}

