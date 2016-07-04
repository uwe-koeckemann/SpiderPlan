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
import java.util.List;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.misc.Finally;
import org.spiderplan.tools.logging.Logger;

/**
 * When called this solver simply adds the {@link ConstraintDatabase}s in {@link Finally} 
 * constraints as part of a resolver. This solver should only be applied to 
 * {@link Core}s which are otherwise (i.e. by all other modules) considered solutions.
 * This module cannot fail by itself since it just adds whatever constraints have
 * to be added finally, but other solver may fail due to the constraints added
 * here.
 * 
 * @author Uwe Köckemann
 */
public class FinallySolver extends Module implements SolverInterface {
		
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public FinallySolver( String name, ConfigurationManager cM ) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		Resolver r = this.testAndResolve(core).getResolverIterator().next();
		r.apply(core.getContext());
		core.setResultingState(getName(), State.Searching);
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		
		ConstraintDatabase resDB = new ConstraintDatabase();
		
		for ( Finally finCon: core.getContext().get(Finally.class)) {
			if ( !finCon.isAsserted() ) {
				finCon.apply(resDB);
				resDB.add(new Asserted(finCon));
			}
		}
			
		ResolverList resIt = null;
		if ( !resDB.isEmpty() ) {
			List<Resolver> rList = new ArrayList<Resolver>();
			rList.add(new Resolver(resDB));
			resIt = new ResolverList(rList, this.getName(), this.cM);
		}
		SolverResult result;
		if ( resIt != null ) {
			result = new SolverResult(State.Searching, resIt);
			if ( verbose ) Logger.msg(getName(), "Found 'finally' constraint. Continuing search...", 0);
		} else {
			result = new SolverResult(State.Consistent);
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
		}
		return result;
	}

}
