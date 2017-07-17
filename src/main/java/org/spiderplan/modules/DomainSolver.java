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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.expressions.ExpressionTypes.DomainRelation;
import org.spiderplan.representation.expressions.domain.DomainMemberConstraint;
import org.spiderplan.representation.expressions.domain.NewObject;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.domain.VariableDomainRestriction;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.logging.Logger;

/**
 * Checks all constraints that limit the allowed values for a variable.
 * If variable was assigned it just checks if the assignment it consistent
 * with the allowed values. If the variable was not assigned and the intersection
 * of all {@link VariableDomainRestriction}s is empty there is an inconsistency. 
 * 
 * TODO: Add propagation into a single {@link VariableDomainRestriction} constraint for each variable.
 * 
 * @author Uwe Köckemann
 *
 */
public class DomainSolver extends Module implements SolverInterface {
		
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public DomainSolver( String name, ConfigurationManager cM ) {
		super(name, cM);
	}

	@Override
	public Core run( Core core ) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		SolverResult result = this.testAndResolve(core);
		
		if ( result.getResolverIterator() != null ) {
			Resolver r = result.getResolverIterator().next();
			r.apply(core.getContext());
		}
			
		if ( verbose ) Logger.msg(getName(), result.getState().toString(), 0);
		core.setResultingState(this.getName(), result.getState());
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		ResolverList resolverList = null;
		
		TypeManager tM = core.getTypeManager();
		
		boolean isConsistent = true;
		
		for ( DomainMemberConstraint dC : core.getContext().get(DomainMemberConstraint.class) ) {
			Term r = dC.getConstraint();
			Term a = r.getArg(0);
			Term b = r.getArg(1);
			
			if ( dC.getRelation().equals(DomainRelation.Equal) ) { //r.getUniqueName().equals("equals/2") || r.getUniqueName().equals("equal/2") ) {	
				if ( verbose ) Logger.msg(getName(),dC.toString(), 1);
				if ( a.isGround() && b.isGround() ) {
					isConsistent = a.equals(b);	
				}						
			} else if  ( dC.getRelation().equals(DomainRelation.NotEqual)  ) { // r.getUniqueName().equals("not-equals/2") || r.getUniqueName().equals("not-equal/2") ) {
				if ( verbose ) Logger.msg(getName(),dC.toString(), 1);
				isConsistent = !a.equals(b);
			}
			
			if ( !isConsistent ) {
				break;
			}
		}

		if ( isConsistent ) {

			HashMap<Term,Set<Term>> domainLookUp = new HashMap<Term, Set<Term>>();
			
			for ( VariableDomainRestriction c : core.getContext().get(VariableDomainRestriction.class) ) {
				if ( verbose ) Logger.msg(getName(), "Checking: " + c, 1);
				if ( c.getVariable().isGround() ) {
					isConsistent = c.isConsistent();
				} else {
					if ( !domainLookUp.containsKey(c.getVariable()) ) {
						/**
						 * New variable: All values in the domain of this constraint are allowed
						 */
						if ( c.getRelation().equals(DomainRelation.In) ) {
							HashSet<Term> s = new HashSet<Term>();
							s.addAll(c.getDomain());
							domainLookUp.put(c.getVariable(),s);
							if ( verbose ) Logger.msg(getName(), "--> new domain: " + s, 2);
						}
					} else {
						/**
						 * Variable has another domain restriction constraint:
						 * 	Remove all values that are not allowed by new constraint. 
						 */
						ArrayList<Term> remList = new ArrayList<Term>();
						Set<Term> allowedValues = domainLookUp.get(c.getVariable());
						for ( Term val : allowedValues ) {
							if ( c.getRelation().equals(DomainRelation.In) ) {
								if ( !c.getDomain().contains(val) ) {
									remList.add(val);
								}
							} else if ( c.getRelation().equals(DomainRelation.NotIn) ) {
								if ( c.getDomain().contains(val) ) {
									remList.add(val);
								}
							}
						}
						if ( verbose ) Logger.msg(getName(), "--> intersecting: " + allowedValues + " and " + c.getDomain(), 2);
						allowedValues.removeAll(remList);
						if ( allowedValues.isEmpty() ) {
							isConsistent = false;
						} else { 
							isConsistent = true;
						}
					}
				}
				if ( !isConsistent ) {
					if ( verbose ) Logger.msg(getName(), "-> inconsistent", 1);
					break;
				} 
			}
		}
		
		Collection<NewObject> newObjects = core.getContext().get(NewObject.class);
		
		if ( isConsistent && !newObjects.isEmpty() ) {
			Map<Term,Set<Term>> usedObjects = new HashMap<Term, Set<Term>>();

			Substitution theta = new Substitution();
			
			for ( NewObject nO : newObjects ) {			
				if ( !nO.getVariable().isGround() ) { // removed: !nO.isAsserted() since that should not be necessary
					Term typeName = nO.getTypeName();
					if ( verbose ) Logger.msg(getName(),nO.toString(), 0);
					
					if ( !usedObjects.containsKey(typeName) ) {
//						Collection<Term> atomics = core.getContext().getAtomics();
						Set<Term> atomics = new HashSet<Term>();
						core.getContext().getAllTerms(atomics, false, false, true);
//						Collection<Term> atomics = core.getContext().getAllTerms(collectedTerms, false, false, true);
						Set<Term> objects = new HashSet<Term>();
						for ( Term a : atomics ) {
							if ( a.isComplex() && tM.hasSignature(a.getUniqueName()) ) {
								objects.addAll(tM.getAllObjectsFromDomains(typeName, a));
							}
						}
						if ( verbose ) Logger.msg(getName() ,"Used objects: " + objects, 2);
						usedObjects.put(typeName, objects);
					}
					
					Set<Term> objects = usedObjects.get(typeName);
					
	//				tM.updateTypeDomains();
					Type type = tM.getTypeByName(typeName);
						
					if ( verbose ) Logger.msg(getName() ,"Type " + type, 2);
					
					isConsistent = false;
					for ( Term t : type.generateDomain(tM) ) {
						if ( !objects.contains(t) ) {
							if ( verbose ) Logger.msg(getName() ,"Trying to match: " + t + " to " + nO.getVariable(), 2);
													
							Substitution sub = nO.getVariable().match(t);
							
							if ( sub != null ) {
								theta.add(sub);
								objects.add(t);
//								nO.setAsserted(true);
								isConsistent = true;
								if ( verbose ) Logger.msg(getName() ,"Adding new object: " + t, 1);
								break;
							}
						}
					}
				
				}
			}
			if ( !theta.isEmpty() ) {
				List<Resolver> rList = new ArrayList<Resolver>();
				Resolver r = new Resolver(theta);
				rList.add(r);
				resolverList = new ResolverList(rList,this.getName(),this.cM);
			}
		}

		SolverResult result;		
		if ( isConsistent ) {
			if ( resolverList  != null ) {
				result = new SolverResult(State.Searching, resolverList);
			} else {
				result = new SolverResult(State.Consistent);
			}
		} else {
			result = new SolverResult(State.Inconsistent);
		}
		
		return result;
	}

}
