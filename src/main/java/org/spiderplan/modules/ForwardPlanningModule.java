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
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.causal.AdaptExistingPlanIterator;
import org.spiderplan.causal.ForwardPlanningIterator;
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
import org.spiderplan.representation.constraints.AppliedPlan;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.plans.Plan;

/**
 * Create a {@link Plan} using heuristic forward search. If a {@link Plan} exists
 * it will be adapter to reach all {@link OpenGoal}s that are not asserted.
 * @author Uwe Köckemann
 */
public class ForwardPlanningModule extends Module implements SolverInterface {
	
	ForwardPlanningIterator planGenerator = null;
	
	Collection<OpenGoal> G = null;
	ConstraintDatabase originalContext;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public ForwardPlanningModule( String name, ConfigurationManager cM ) {
		super(name, cM);
		
		super.parameterDesc.add( new ParameterDescription("lookAhead", "int", "0", "Number of steps taken by one iteration of the causal reasoner. This can help to overcome plateaus in the heuristic function or to solve problems with required concurrency.") );
		super.parameterDesc.add( new ParameterDescription("incremental", "boolean", "false", "In incremental mode the planner will perform only one search step at each call. This allows to check for advanced constraints during planning and could yield earlier pruning of nodes as soon as they become inconsistent. It can also be a high computational overhead especially when consistency checks by later modules are expensive.") );
		super.parameterDesc.add( new ParameterDescription("ignoredLabels", "string", "", "Comma-seperated list of labels that are ignored by Graph Plan. This is useful for Statements that are only used to achieve some temporal effect (like synchronization) but do not contribute to the state space of the planner. An example of this would be a statement g1: < sync() > that is required to be during all goal statements (so all goals have to be solved at the same time). In this case we have to set \"g1\" as an ignored label, so that Graph Plan does not try to reach sync() (since it is not reachable).") );
		super.parameterDesc.add( new ParameterDescription("symbolicValueScheduling", "boolean", "true", "Determines if this modules uses symbolic value scheduling.") );		
		super.parameterDesc.add( new ParameterDescription("multiEffectSupport", "boolean", "true", "Determines if this module uses multiple effects in case an operator uses the same state variable twice.") );
		super.parameterDesc.add( new ParameterDescription("yahspLookahead", "boolean", "false", "If true the lookahead function from YAHSP 2.0 is used.") );
		super.parameterDesc.add( new ParameterDescription("uniqueInitialState", "boolean", "false", "If true initial state will be created based on latest statements.") );
		
		super.parameterDesc.add( new ParameterDescription("queueStyle", "string", "Lexicographic", "Decides if multiple heuristics are sorted lexicographicals (\"Lexicographic\") or a queue is created for each heuristic (\"MultiQueue\").") );
		super.parameterDesc.add( new ParameterDescription("heuristics", "string", "", "Comma-seperated list of heuristics (supported: FastDownward).") );
		super.parameterDesc.add( new ParameterDescription("useHelpfulTransitions", "string", "", "Comma-seperated list of true/false indicating whether heuristics use helpful actions.") );
		super.parameterDesc.add( new ParameterDescription("nodeEquality", "string", "PlanBased", "Determines when two nodes in the planner's search space are equal. In (\"PlanBased\") mode the sequence of actions has to be the same. In (\"StateBased\") mode state and actions have to be the same. In (\"ResultingStateBased\") result of applying action to state is taken for equality. While the second option will detect more loops it may throw away solutions in cases where the planner is under informed (i.e. states look the same to the causal reasoner, but are different in the constraint database).") );
		
		if ( cM.hasAttribute(name, "consistencyChecker") && this.verbose ) {
			String checkerName = cM.getString(this.getName(), "consistencyChecker");
			boolean verbose = false;
			int verbosity = 0;
			if ( cM.hasAttribute(checkerName,"verbose") ) {
				verbose = cM.getBoolean(checkerName, "verbose");
			}
			if ( cM.hasAttribute(checkerName,"verbosity") ) {
				verbosity = cM.getInt(checkerName, "verbosity");
			}
			if ( verbose ) {
				Logger.registerSource(checkerName, verbosity);
			}
		}
		if ( cM.hasAttribute(name, "pruningModule") && this.verbose ) {
			String checkerName = cM.getString(this.getName(), "pruningModule");
			boolean verbose = false;
			int verbosity = 0;
			if ( cM.hasAttribute(checkerName,"verbose") ) {
				verbose = cM.getBoolean(checkerName, "verbose");
			}
			if ( cM.hasAttribute(checkerName,"verbosity") ) {
				verbosity = cM.getInt(checkerName, "verbosity");
			}
			if ( verbose ) {
				Logger.registerSource(checkerName, verbosity);
			}
		}
	}
	
	@Override
	public Core run(Core core )  {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		
		if ( verbose ) Logger.depth++;
		
		if ( core.getInSignals().contains("FromScratch") ) {
			Logger.msg(getName(),"Running FromScratch", 0);
			core.getInSignals().remove("FromScratch");
			planGenerator = null;
		}
		
		if ( planGenerator == null ) {
			Collection<OpenGoal> G = core.getContext().get(OpenGoal.class);
			planGenerator = new ForwardPlanningIterator(core.getContext(), G, core.getOperators(), core.getTypeManager(), this.cM, this.getName());
			originalContext = core.getContext().copy();
		} 
		
		Resolver r = planGenerator.next(null);
		
		if ( r == null ) {
			core.setResultingState( getName(), State.Inconsistent );
		} else {
			ConstraintDatabase cDB = originalContext.copy();
			
			r.apply(cDB);
			
			Plan plan = r.getConstraintDatabase().get(AppliedPlan.class).iterator().next().getPlan();
					
			core.setPlan(plan); // TODO: Meh...
									
			core.setContext(cDB);
			core.setResultingState(getName(), State.Consistent);
		}
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override 
	public SolverResult testAndResolve( Core core ) {

		G = new ArrayList<OpenGoal>();
		for ( OpenGoal og : core.getContext().get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				if ( verbose ) Logger.msg(this.getName(),"Open goal: " + og, 1);
				G.add(og);
			}
		}

		
		State state;
		ResolverIterator resolverIterator = null;
		
		if ( !G.isEmpty() ) {
			if ( core.getPlan().getActions().isEmpty() ) {
				state = State.Searching;
				resolverIterator = new ForwardPlanningIterator(core.getContext(), G, core.getOperators(), core.getTypeManager(), this.cM, this.getName());
			} else {
				boolean uniqueInitialStateSetting = false;
				if ( cM.hasAttribute(getName(), "uniqueInitialState")) {
					uniqueInitialStateSetting = cM.getBoolean(this.getName(), "uniqueInitialState");
				}
				cM.set(this.getName(), "uniqueInitialState", "true");				
				state = State.Searching;
				resolverIterator = new AdaptExistingPlanIterator(core.getContext(), G, core.getPlan(), core.getOperators(), core.getTypeManager(), this.cM, this.getName());
				cM.set(getName(), "uniqueInitialState", (uniqueInitialStateSetting ? "true" : "false" )  );
			}
		} else {
			state = State.Consistent;
		}
		return new SolverResult(state,resolverIterator);
	}
}
