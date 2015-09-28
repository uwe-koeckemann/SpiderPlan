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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.constraints.SetConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

/**
 * Handles simple {@link SetConstraint}s.
 *  
 * @author Uwe Köckemann
 *
 */
public class SetSolver extends Module implements SolverInterface {
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public SetSolver(String name, ConfigurationManager cM) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
				
		core.setResultingState(this.getName(), this.testAndResolve(core).getState());		
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve( Core core ) {
		boolean isConsistent = true;
		
		Collection<SetConstraint> C = core.getContext().get(SetConstraint.class);
		
		HashMap<Term,Set<Term>> sets = new HashMap<Term,Set<Term>>();
		/**
		 * Create Sets
		 */
		Atomic r;
		if ( verbose ) Logger.msg(getName(), "Creating sets...", 1);
		for ( SetConstraint sC : C ) {
			r = sC.getRelation();
			Term setID = r.getArg(0);
			r = sC.getRelation();
			if ( r.getUniqueName().equals("set/1") ) {
				if ( verbose ) Logger.msg(getName(), "    Creating set: " + sC, 2);
				sets.put(setID, new HashSet<Term>());
			} else if  ( r.getUniqueName().equals("is-domain/2") ) {
				Term setName = r.getArg(0);
				Term typeName = r.getArg(1);
				HashSet<Term> domain = new HashSet<Term>();
				domain.addAll(core.getTypeManager().getTypeByName(typeName).getDomain());
				sets.put(setName, domain);
			}
		}
		/**
		 * Add to sets
		 */
		Set<Term> S,S_prime;
		for ( SetConstraint sC : C ) {
			r = sC.getRelation();
			Term setID = r.getArg(0);
		
			S = sets.get(setID);
			if ( S == null ) {
				S = new HashSet<Term>();
				sets.put(setID, S);
			}
			
			if ( sC.getRelation().getUniqueName().equals("add/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Adding: " + sC, 2);
				S.add(r.getArg(1));
			} 
		}
		
		/**
		 * Check set constraints
		 */
		for ( SetConstraint sC : C ) {
			if ( !isConsistent ) {
				break;
			}
			r = sC.getRelation();
			Term setID = r.getArg(0);
			S = sets.get(setID);
			
			if ( sC.getRelation().getUniqueName().equals("in/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + sC, 1);
				Term e = r.getArg(1);
				isConsistent = S.contains(e);
			} else if  ( sC.getRelation().getUniqueName().equals("notin/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + sC, 1);
				Term e = r.getArg(1);
				isConsistent = !S.contains(e);
			} else if  ( sC.getRelation().getUniqueName().equals("equals/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + sC, 1);
				S_prime = sets.get(r.getArg(1));
				
				isConsistent = S.equals(S_prime);
				
			} else if  ( sC.getRelation().getUniqueName().equals("subset/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + sC, 1);
				S_prime = sets.get(r.getArg(1));
				isConsistent = S_prime.containsAll(S);
			} else if  ( sC.getRelation().getUniqueName().equals("proper-subset/2") ) {
				if ( verbose ) Logger.msg(getName(), "    Checking: " + sC, 1);
				S_prime = sets.get(r.getArg(1));
				isConsistent = S_prime.containsAll(S) && S.size() < S_prime.size();
			}
		}		
		
		State state;
		if ( isConsistent ) {
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			state = State.Consistent;
		} else {
			if ( verbose ) Logger.msg(getName(), "Inconsistent", 0);
			state = State.Inconsistent;
		}
		return new SolverResult(state);
	}
}
