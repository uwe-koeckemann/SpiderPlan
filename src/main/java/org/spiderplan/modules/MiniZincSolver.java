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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import org.spiderplan.minizinc.MiniZincIterator;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.IncludedProgram;
import org.spiderplan.representation.constraints.MiniZincConstraint;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

/**
 * Create and run MiniZinc CSPs.
 * 
 * @author Uwe Köckemann
 *
 */
public class MiniZincSolver extends Module implements SolverInterface {
		
	private ResolverIterator mzIterator = null;
	private ConstraintDatabase originalContext;
	
	String minizincBinaryLocation = "minizinc"; 
	
	private final static Term MiniZincTerm = Term.createConstant("miniZinc");
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public MiniZincSolver(String name, ConfigurationManager cM) {
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
			mzIterator = null;
		}
		
		SolverResult result;
		
		if ( mzIterator == null ) {
			originalContext = core.getContext();
			
			result = this.testAndResolve(core);
			if ( result.getState().equals(State.Searching)) {
				this.mzIterator = result.getResolverIterator();
			} else {
				this.mzIterator = null;
			}
		} 
		
		Resolver r = null;
		if ( mzIterator != null ) {
			r = mzIterator.next(null);
		} 
		
		if ( this.mzIterator != null && r == null ) {
			core.setResultingState( getName(), State.Inconsistent );
		} else {
			ConstraintDatabase cDB = originalContext.copy();
			
			if ( r != null ) 
				r.apply(cDB);
			
			core.setContext(cDB);
			core.setResultingState(getName(), State.Consistent);
		}
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		Collection<MiniZincConstraint> C = core.getContext().getConstraints().get(MiniZincConstraint.class)	;
		Collection<IncludedProgram> B = core.getContext().getConstraints().get(IncludedProgram.class);
		
		Set<Term> reqOutput = new HashSet<Term>();
		
		/**
		 * Collect program from background knowledge
		 */
		String csp = "";
		for ( IncludedProgram b : B ) {
			if ( b.getName().nameEquals(MiniZincTerm) ) {
				csp += b.getCode() + "\n";
			}
		}
		/**
		 * Collect data from constraints and required output terms
		 */
		String dataStr = "";
		for ( MiniZincConstraint c : C ) {
			if ( c.getRelation().name().equals("output")) {
				reqOutput.addAll(c.getRelation().getVariableTerms());
			} else {
				dataStr += c.getMiniZincCode() + "\n";	
			}
		}
		
		if ( verbose ) Logger.msg(getName(),"CSP:\n" +  csp, 3);
		if ( verbose ) Logger.msg(getName(),"Data:\n" + dataStr, 3);
		
		State state;
		ResolverIterator resolverIterator = null;
		
		if ( reqOutput.isEmpty() ) {
			state = State.Consistent;
		} else {
			state = State.Searching;
			resolverIterator = new MiniZincIterator(minizincBinaryLocation, csp, dataStr, this.getName(), cM);
		}
		return new SolverResult(state,resolverIterator);
	}

}
