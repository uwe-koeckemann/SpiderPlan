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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.graph.GraphConstraint;
import org.spiderplan.representation.expressions.resources.ReusableResourceCapacity;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.scheduling.ReusableResourceScheduler;
import org.spiderplan.scheduling.StateVariableScheduler;
import org.spiderplan.tools.logging.Logger;

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
public class SchedulingSolver extends Module implements SolverInterface {
	
	private ResolverIterator resolverIterator = null;
	private ConstraintDatabase originalContext = null;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SchedulingSolver(String name, ConfigurationManager cM) {
		super(name, cM);
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
					core.setResultingState(getName(), State.Consistent); // Not State.Searching since r.apply resolves all flaws here
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
		ResolverIterator resolverIterator = null;
		
		Set<Atomic> scheduledVariables = new HashSet<Atomic>();
		
		List<Resolver> resolverList = new ArrayList<Resolver>();
		
		Collection<ReusableResourceCapacity> RCs = core.getContext().get(ReusableResourceCapacity.class);
		
		if ( verbose ) Logger.msg(getName(), "Found " + RCs.size() + " reusable resource constraints." , 1);
		
		for ( ReusableResourceCapacity rrc : RCs ) {
			if ( verbose ) Logger.msg(getName(), "Testing: " + rrc , 2); 
			scheduledVariables.add(rrc.getVariable());
			
			ReusableResourceScheduler scheduler = new ReusableResourceScheduler(rrc.getVariable(), rrc.getCapacity());
			
			List<AllenConstraint> resolvers = scheduler.resolveFlaw(core.getContext());
			
			if ( resolvers == null ) {
				isConsistent = false;
				resolverList.clear();
				if ( verbose ) Logger.msg(getName(), "---> No legal resolvers!" , 2); 
				break;
			}
			
			if ( !resolvers.isEmpty() ) {
				if ( verbose ) Logger.depth++;
				for ( AllenConstraint resCon : resolvers ) {
					if ( verbose ) Logger.msg(getName(), "Resolver: " + resCon , 3); 
					ConstraintDatabase resolverCDB = new ConstraintDatabase();
					resolverCDB.add(resCon);
					resolverList.add(new Resolver(resolverCDB));
				}
				if ( verbose ) Logger.depth--;
				break;
			} else {
				if ( verbose ) Logger.msg(getName(), "---> No conflicts found!" , 2); 
			}
		}
		
		if ( isConsistent ) {
			if ( verbose ) Logger.msg(getName(), "Scheduling statements..." , 1);
			for ( Statement s : core.getContext().get(Statement.class)) {
				boolean hasNoVariables = s.getVariable().getVariableTerms().isEmpty() && s.getValue().getVariables().isEmpty();
				if ( hasNoVariables && !scheduledVariables.contains(s.getVariable())) {
					if ( verbose ) Logger.msg(getName(), "Testing: " + s.getVariable() , 2); 
					scheduledVariables.add(s.getVariable());
					
					StateVariableScheduler scheduler = new StateVariableScheduler(s.getVariable());
					List<AllenConstraint> resolvers = scheduler.resolveFlaw(core.getContext());
					
					if ( resolvers == null ) {
						isConsistent = false;
						resolverList.clear();
						if ( verbose ) Logger.msg(getName(), "---> No legal resolvers!" , 2); 
						break;
					}
					
					if ( !resolvers.isEmpty() ) {
						if ( verbose ) Logger.depth++;
						for ( AllenConstraint resCon : resolvers ) {
							if ( verbose ) Logger.msg(getName(), "Resolver: " + resCon , 3); 
							ConstraintDatabase resolverCDB = new ConstraintDatabase();
							resolverCDB.add(resCon);
							resolverList.add(new Resolver(resolverCDB));
						}
						if ( verbose ) Logger.depth--;
						break;
					} else {
						if ( verbose ) Logger.msg(getName(), "---> No conflicts found!" , 2); 
					}
				}
			}
		}
		State state;
				
		if ( !resolverList.isEmpty() ){
			state = State.Searching;
			resolverIterator = new ResolverList(resolverList, getName(), cM);
		} else if ( isConsistent ) {
			state = State.Consistent;
		} else {
			state = State.Inconsistent;
		}
		return new SolverResult(state,resolverIterator);
	}
}
