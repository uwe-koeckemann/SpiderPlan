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
import java.util.HashSet;
import java.util.Set;


import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.tools.logging.Logger;

/**
 * Preprocessing {@link Module} that will try to reach single {@link OpenGoal}s
 * and create new {@link Operator}s based on the plans that reached them.
 * 
 * @author Uwe Köckemann
 *
 */
public class CreateSingleGoalOperators extends Module {

	private String planningModuleName;
	private Module planningModule;
		
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public CreateSingleGoalOperators(String name, ConfigurationManager cM) {
		super(name, cM);
		this.cM = cM;

		super.parameterDesc.add(  new ParameterDescription("planningModule", "String", "", "Module that will solve the problem for each goal.") );
		
		
		this.planningModuleName = cM.getString(name, "planningModule");
		this.planningModule = ModuleFactory.initModule(this.planningModuleName, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
//		ConstraintDatabase goalDB = core.getGoalContext();
		ArrayList<Statement> goalStatements = new ArrayList<Statement>();
		for ( OpenGoal og : core.getContext().get(OpenGoal.class) ) {
			goalStatements.add(og.getStatement());
		}
		
		Set<String> checkList = new HashSet<String>();
				
		for ( Statement g : goalStatements ) {
			Logger.msg(this.getName(), "Trying goal: " + g, 0);
			if ( !checkList.contains(g.getVariable().getUniqueName()) ) {
				
				boolean trivial = false;				
				for ( Statement s : core.getContext().get(Statement.class) ) {
					if ( s.getVariable().equals(g.getVariable()) && s.getValue().equals(g.getValue()) ) {
						Logger.msg(this.getName(), "    Trivial: Reached in initial context.", 0);
						trivial = true;
					}
				}
				
				
				if ( !trivial ) {
					Logger.msg(this.getName(), "    Solving single goal...", 0);
					checkList.add(g.getVariable().getUniqueName());
					this.planningModule = ModuleFactory.initModule(this.planningModuleName, cM);
					ConstraintDatabase singleGoalDB = new ConstraintDatabase();
					singleGoalDB.add(g);
					
					Core singleGoalCore = core.copy();
					
					singleGoalCore.getContext().removeType(OpenGoal.class);
					singleGoalCore.getContext().add(new OpenGoal(g));

					Core solution = planningModule.run(singleGoalCore);
					
					if ( solution.getResultingState(planningModuleName).equals(Core.State.Consistent) ) {
						Logger.msg(this.getName(), "Found solution: Extracting operator...", 0);
						Operator oNew = solution.getContext().getUnique(Plan.class).createOperator( "solve_" + g.getVariable().name() ,  singleGoalCore.getOperators(), core.getTypeManager() );
						core.getOperators().add(oNew);
						Logger.msg(this.getName(), "New operator:\n" + oNew, 0);
//						Loop.start();
					} else {
						Logger.msg(this.getName(), "Unsolvable: Overall problem is unsatisfiable since a single goal is not reachable...", 0);
						core.setResultingState(this.getName(), Core.State.Inconsistent);
						break;
					}
				}
			} else {
				Logger.msg(this.getName(), "    Ignored: Goal variable already processed...", 0);
			}
		}
		
		core.setResultingState(this.getName(), Core.State.Consistent);
		if ( verbose ) Logger.depth--;
		return core;
	}

}
