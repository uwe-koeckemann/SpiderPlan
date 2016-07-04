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

import org.spiderplan.causal.planSpacePlanning.OpenGoalResolverIterator;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.representation.expressions.causal.OpenGoal;

/**
 * Select an {@link OpenGoal} flaw and create a list of resolvers for that flaw.
 * Uses ideas and heuristics of partial order causal link planning but threats
 * are left for temporal and resource constraints.
 * 
 * TODO: Easy integration of value selection heuristics
 * 
 * <p>
 * Relevant papers:
 * <li> Younes, H. L. S. & Simmons, R. G. VHPOP: Versatile heuristic partial order planner Journal of Artificial Intelligence Research, 2003, 20, 405-430
 * <li> McAllester, D. & Rosenblitt, D. Systematic Nonlinear Planning Proceedings of the 9th National Conference on Artificial intelligence (AAAI), 1991, 634-639
 * <p>
 * 
 * @author Uwe Köckemann
 *
 */
public class OpenGoalResolverAllFlaws extends Module implements SolverInterface {
	
	List<String> heuristicNames = new ArrayList<String>();
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public OpenGoalResolverAllFlaws(String name, ConfigurationManager cM) {
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
		throw new IllegalAccessError("This is not implemented...");
	}

	@Override
	public SolverResult testAndResolve(Core core) {	
		boolean flawExists = false;
		for ( OpenGoal og : core.getContext().get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				flawExists = true;
				break;
			}
		}
		
		SolverResult result;
		if ( flawExists ) {
			OpenGoalResolverIterator solutionIterator = new OpenGoalResolverIterator(core, name, cM);
			result = new SolverResult(State.Searching, solutionIterator);
		} else {
			result = new SolverResult(State.Consistent);
		}
		
		return result;
	}
	
}
