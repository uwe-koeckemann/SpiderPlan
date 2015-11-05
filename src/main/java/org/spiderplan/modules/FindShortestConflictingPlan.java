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

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.causal.DiscardedPlan;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Will find the minimal length sub-sequence of a plan that
 * still creates a conflict. As a result {@link DiscardedPlan} 
 * {@link Expression}s will be added to the {@link Core}. 
 * 
 * @author Uwe Köckemann
 *
 */
public class FindShortestConflictingPlan extends Module {

	Module consistencyChecker;
	String consistencyCheckerName;
	IncrementalSTPSolver csp;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public FindShortestConflictingPlan(String name, ConfigurationManager cM) {
		super(name, cM);
	
		super.parameterDesc.add( new ParameterDescription("consistencyChecker", "String", "", "Name of the module that verifies consistency of constraint databases.") );
			
		this.consistencyCheckerName = cM.getString(this.getName(), "consistencyChecker" );
		
		consistencyChecker = ModuleFactory.initModule( this.consistencyCheckerName, cM );

		csp = new IncrementalSTPSolver(0, Global.MaxTemporalHorizon);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
//		ConstraintCollection prunedPlans = new ConstraintCollection(StorageClass.ArrayList);
		
		Plan inconsistentPlan = core.getPlan();
		
		int minPossible = 1;
		int maxPossible = inconsistentPlan.getActions().size();
		int i;
		
		Plan partialPlan = null;
		while ( minPossible != maxPossible ) {
			i = minPossible + (maxPossible-minPossible)/2;
			
			if ( keepStats ) Statistics.increment(msg("Tested partial plans"));
			
			partialPlan = inconsistentPlan.getSubPlan(i);
			
			Core testCore = core.copy();
			testCore.setPlan(partialPlan);
			
			if ( keepTimes ) StopWatch.start("[FindMiniConflict] testing DB");
			testCore = consistencyChecker.run(testCore);
			if ( testCore.getResultingState(consistencyCheckerName).equals("Inconsistent") 
			  || testCore.getResultingState(consistencyCheckerName).equals("Fail")) {				
				maxPossible = i;				
				
				if ( verbose ) Logger.msg(getName(), String.format("Plan length %d is inconsistent. Updated bounds to [%d,%d].", i, minPossible, maxPossible), 0);
			} else {
				minPossible = i+1;
				
				if ( verbose ) Logger.msg(getName(), String.format("Plan length %d is consistent. Updated bounds to [%d,%d].", i, minPossible, maxPossible), 0);
			}				
			
			if ( keepTimes ) StopWatch.stop("[FindMiniConflict] testing DB");	
		}
		
		if ( partialPlan == null ) {
			throw new IllegalStateException(String.format("No conflicting plan found (final range %d to %d). This module should only be called if a plan was found inconsistent before. Make sure to test the same constraints as before.", minPossible, maxPossible));
		}
		
		if ( verbose ) Logger.msg(getName(), "Minimal inconsistent plan length: " + minPossible, 0);
				
		core.getContext().add(new DiscardedPlan(partialPlan));			
		if ( verbose ) Logger.depth--;
		return core;
	}
}
