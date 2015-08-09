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
import org.spiderplan.prolog.YapPrologAdapter;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Asserted;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;


/**
 * Solves {@link PrologConstraint}s by backtracking over possible {@link Substitution}s
 * of variables.
 * @author Uwe Köckemann
 */
public class PrologSolver extends Module implements SolverInterface {
	
	private ResolverIterator resolverIterator = null;
	private ConstraintDatabase originalContext = null;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public PrologSolver( String name, ConfigurationManager cM ) {
		super(name, cM);
		
		super.parameterDesc.add( new ParameterDescription("yapPath", "string", Global.yapBinaryLocation, "Loction of YAP Prolog binary.") );
		
		if ( cM.hasAttribute(name, "yapPath") ) {
			Global.yapBinaryLocation = cM.getString(name, "yapPath");
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
	public SolverResult testAndResolve( Core core ) {
		

		Map<Term,ArrayList<PrologConstraint>> queries = new HashMap<Term,ArrayList<PrologConstraint>>();
		
		List<List<Resolver>> resolvers = new ArrayList<List<Resolver>>();
		
		YapPrologAdapter yappy = new YapPrologAdapter();
		yappy.setKeepTimes(this.keepTimes);
		
		for ( PrologConstraint rC : core.getContext().getConstraints().get(PrologConstraint.class)) {
			if ( !rC.isAsserted() ) {
				if ( !queries.containsKey(rC.getProgramID()) ) {
					queries.put(rC.getProgramID(), new ArrayList<PrologConstraint>());
				}
				queries.get(rC.getProgramID()).add(rC);
			}
		}		
		
		
		Map<Term,ConstraintCollection> programs = core.getContext().getIncludedPrograms(queries.keySet());
		
		boolean satisfiable = true;
		
		for ( Term programID : queries.keySet() ) {
			if ( queries.get(programID).isEmpty() ) {
				if ( verbose ) Logger.msg(getName(), "No query for program ID " + programID, 0);
			} else {
				if ( !programs.containsKey(programID) ) {
					throw new IllegalStateException("Program ID " + programID + " does not have any IncludedProgram. Check domain and problem definition.");
				}
				
				if ( verbose ) {
					Logger.msg(getName(), "Runniny query (wrt. "+programID+"): ", 1);
					for ( Constraint c : queries.get(programID) ) {
						Logger.msg(getName(), "    " + c.toString(), 1);
					}
				}
				
				Collection<Substitution> answer = yappy.query(programs.get(programID), queries.get(programID), programID, core.getTypeManager());
				
				if ( answer == null ) {
					satisfiable = false; 
					break;
				} else {
					List<Resolver> resolverList = new ArrayList<Resolver>();
					for ( Substitution sub : answer ) {
						ConstraintDatabase cDB = new ConstraintDatabase();
						for ( PrologConstraint c : queries.get(programID) ) {
//							Constraint cCopy = c.copy();
//							if ( c instanceof Substitutable ) {
//								((Substitutable)cCopy).substitute(sub); //TODO: before: c was substituted which seemed odd...
//							}
							PrologConstraint cCopy = c.copy();
							cCopy.substitute(sub);
							Asserted a = new Asserted(cCopy);
							cDB.add(a);
						}	
						resolverList.add(new Resolver(sub,cDB));
					}		
					if ( !resolverList.isEmpty() ) {
						resolvers.add(resolverList);
					}
				} 
			}
		}
		
		State state;
		ResolverIterator resolverIterator = null;
		
		if ( satisfiable ) {
			if ( resolvers.isEmpty() ) {
				if ( verbose ) Logger.msg(getName(), "Consistent", 0);
				state = State.Consistent;
			} else {
				if ( verbose ) Logger.msg(getName(), "Proposing resolvers...", 0);
				state = State.Searching;
				resolverIterator = new ResolverCombination(resolvers, this.getName(), this.cM);
			}
		} else {
			if ( verbose ) Logger.msg(getName(), "Inconsistent", 0);
			state = State.Inconsistent;
		}
		
		return new SolverResult(state, resolverIterator);
	}
}

