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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ExpressionTypes.SamplingRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.sampling.SamplingConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.tools.logging.Logger;

/**
 * Can be used to randomize variables in {@link ConstraintDatabase}s via 
 * {@link SamplingConstraint}s.
 *  
 * @author Uwe Köckemann
 *
 */
public class SamplingSolver extends Module implements SolverInterface {
	
	private ResolverIterator resolverIterator = null;
	private ConstraintDatabase originalContext = null;
	private Random randomGenerator = new Random();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SamplingSolver(String name, ConfigurationManager cM) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
						
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
	public SolverResult testAndResolve( Core core ) {
		boolean isConsistent = true;
		
		Collection<SamplingConstraint> C = core.getContext().get(SamplingConstraint.class);
		
		Map<Term,List<Term>> domains = new HashMap<Term, List<Term>>();

		Atomic r;
		if ( verbose ) Logger.msg(getName(), "Loading random variable domains...", 1);
		for ( SamplingConstraint pC : C ) {
			r = pC.getConstraint();
			
			if ( pC.getRelation().equals(SamplingRelation.RandomVariable) ) { // r.getUniqueName().equals("random-variable/2") ) {
				if ( verbose ) Logger.msg(getName(), r.toString(), 1);
				Term randomVariable = r.getArg(0);
				Term domTerm = r.getArg(1);
				ArrayList<Term> D = new ArrayList<Term>();
				if ( domTerm.nameEquals(Term.createConstant("list")) ) {
					for ( int i = 0 ; i < domTerm.getNumArgs() ; i++ ) {
						D.add( domTerm.getArg(i) );
					}
					domains.put(randomVariable, D);
				} else if ( domTerm.nameEquals(Term.createConstant("interval")) ) {
					try {
						int lowerBound = Integer.valueOf(domTerm.getArg(0).toString());
						int upperBound = Integer.valueOf(domTerm.getArg(1).toString());
						for ( int i = lowerBound ; i <= upperBound ; i++ ) {
							D.add(Term.createInteger(i));
						}
						domains.put(randomVariable, D);
					} catch ( NumberFormatException e ) {
						if ( verbose ) Logger.msg(getName(), "    Ignored: Non-integer bound.", 1);
					}
				} else if ( !domTerm.isVariable() ) {
					try {
						Type type = core.getTypeManager().getTypeByName(domTerm);
						
						for ( Term value : type.getDomain() ) {
							D.add(value);
						}
						domains.put(randomVariable, D);
					} catch ( IllegalStateException e ) {
						if ( verbose ) Logger.msg(getName(), "    Ignored: Type not found.", 1);
					}
					
				} else {
					if ( verbose ) Logger.msg(getName(), "    Ignored: Domain term is variable.", 1);
				}
				
			}
			
		}
		if ( verbose ) Logger.msg(getName(), "Sampling...", 1);
		Substitution theta = new Substitution();
		for ( SamplingConstraint pC : C ) {
			r = pC.getConstraint();
			
			if ( pC.getRelation().equals(SamplingRelation.Sample) ) {
				if ( verbose ) Logger.msg(getName(), "    " + r, 1);
				Term randomVariable = r.getArg(0);
				if ( randomVariable.isVariable() && domains.containsKey(randomVariable) ) {
					List<Term> D = domains.get(randomVariable);
					Term pick = D.get(randomGenerator.nextInt(D.size()));
					if ( verbose ) Logger.msg(getName(), "    -> " + pick, 1);
					theta.add(randomVariable,pick);
				} else if ( !domains.containsKey(randomVariable) ) {
					if ( verbose ) Logger.msg(getName(), "    Ignored: No domain defined for " + randomVariable, 1);
				}
			}
		}
			
		Resolver res = null;
		if ( !theta.isEmpty() ) {
			res = new Resolver(theta);
		}
		
		State state;
		if ( isConsistent ) {
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			state = State.Consistent;
		} else {
			if ( verbose ) Logger.msg(getName(), "Inconsistent", 0);
			state = State.Inconsistent;
		}

		
		if ( res == null ) {
			return new SolverResult(state);
		} else {
			
			List<Resolver> resList = new ArrayList<Resolver>();
			resList.add(res);
			ResolverList resIterator = new ResolverList(resList, getName(), cM);
			return new SolverResult(State.Searching,resIterator);
		}
	}
}
