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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import org.spiderplan.causal.pocl.CausalLinkIteratorAllOpen;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.tools.logging.Logger;

public class POCL extends Module implements SolverInterface {
	
	List<String> heuristicNames = new ArrayList<String>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public POCL(String name, ConfigurationManager cM) {
		super(name, cM);
		
		super.parameterDesc.add( new ParameterDescription("heuristics", "string", "HAddReuse", "Comma-seperated list of heuristics (supported: FastDownward).") );
		
		if ( cM.hasAttribute(name, "heuristics" ) ) {
			heuristicNames = cM.getStringList(name, "heuristics");
		} else {
			heuristicNames = new ArrayList<String>();
			heuristicNames.add("HAddReuse");
		}
	}

	@Override
	public Core run(Core core) {
		throw new NotImplementedException();
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		
		boolean hasOpen = false;
		for ( OpenGoal og : core.getContext().getConstraints().get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				hasOpen = true;
				break;
			}
		}
		
		if ( !hasOpen ) {
			return new SolverResult(State.Consistent);
		} else {
			if ( verbose ) Logger.msg(this.getName(), "Preparing plan iterator...", 1);
			return new SolverResult(State.Searching, new CausalLinkIteratorAllOpen(core.getContext(), core.getOperators(), heuristicNames, core.getTypeManager(), this.getName(), cM));
		}
	}
	
	
}
