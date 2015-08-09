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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.prolog.YapPrologAdapter;
import org.spiderplan.prolog.YapPrologAdapter.FailBehavior;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.IncludedProgram;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.constraints.InteractionConstraint;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Global;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.stopWatch.StopWatch;


/**
 * Replaces all {@link Operator}s and {@link InteractionConstraint}s in a {@link Core}
 * with copies whose {@link PrologConstraint}s are proven to be satisfied.
 * <p> 
 * <b>Note:</b> This works under the assumption that the Prolog programs provided as {@link IncludedProgram}s
 * will not change.  
 *  
 * @author Uwe Köckemann
 */
public class PrologPreprocessor extends Module {
	
	/**
	 * Sets the behavior of this {@link Module} when {@link PrologConstraint}s cannot be satisfied.
	 * Default is {@link FailBehavior}.Ignore
	 */
	public FailBehavior failBehavior = FailBehavior.Ignore;
	
	
	
	private boolean preprocessICs = true;
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public PrologPreprocessor( String name, ConfigurationManager cM ) {
		super(name, cM);
	
		super.parameterDesc.add( new ParameterDescription("failBehavior", "string", "Warning", "Determines what happens in case a query fails without error messages. Choices are \"Warning\", \"Exit\" and \"Ignore\".") );
		super.parameterDesc.add( new ParameterDescription("preprocessICs", "boolean", "true", "If true interaction constraints are preprocessed and replaced by logical consistent ones.") );
		super.parameterDesc.add( new ParameterDescription("yapPath", "string", Global.yapBinaryLocation, "Loction of YAP Prolog binary.") );
							
		if ( cM.hasAttribute(name, "failBehavior") ) {
			failBehavior = FailBehavior.valueOf(cM.getString(name, "failBehavior"));
		}
		if ( cM.hasAttribute(name, "preprocessICs") ) {
			preprocessICs = cM.getBoolean(name, "preprocessICs");
		}
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

		YapPrologAdapter yappy = new YapPrologAdapter();
		yappy.setKeepTimes(this.keepTimes);
		yappy.failBehavior = this.failBehavior;
		
		Set<Term> programIDs = new HashSet<Term>();
		Map<Term,ConstraintCollection> conCollection = new HashMap<Term,ConstraintCollection>(); 
		
		if ( verbose ) {
			Logger.msg(getName(), "Background knowledge (asserted Prolog constraint): ", 2);
		}
		for ( PrologConstraint rC : core.getContext().getConstraints().get(PrologConstraint.class)) {
			if ( !programIDs.contains(rC.getProgramID())) {
				programIDs.add(rC.getProgramID());
				conCollection.put(rC.getProgramID(), new ConstraintCollection());
			}
			if ( rC.isAsserted() ) {
				conCollection.get(rC.getProgramID()).add(rC);
			}
		}
		
		/**
		 * Add background knowledge that is used:
		 */
		for ( IncludedProgram pC : core.getContext().getConstraints().get( IncludedProgram.class ) ) {
			Term programID = pC.getName();
			if ( !programIDs.contains(programID)) {
				programIDs.add(programID);
				conCollection.put(programID, new ConstraintCollection());
			}
			conCollection.get(programID).add(pC);
		}
		
//		ArrayList<Atomic> extendedB = new ArrayList<Atomic>();
//		extendedB.addAll(yappy.saturateConstraints( core.getOperators(), conCollection, core.getTypeManager() ) );
		
		int numBefore = core.getOperators().size();
		
		for ( Term programID : programIDs ) {
			if ( keepTimes ) StopWatch.start(msg("Preprocessing operators"));
			yappy.saturateConstraints( core.getOperators(), conCollection.get(programID), programID, core.getTypeManager() );
			if ( keepTimes ) StopWatch.stop(msg("Preprocessing operators"));
		}

		if ( verbose ) {
			Logger.msg(getName(), "Operators before: " + numBefore + " after: " + core.getOperators().size(), 1);
			for ( Operator o : core.getOperators() ) {
				Logger.msg(getName(), "    - " + o.getName(), 2);
			}
		}
				
		/*
		 * Not working yet. Ideally this should be used instead of above call to saturateConstraints...
		 */
//		yappy.saturateOperatorConstraints(core.getOperators(), conCollection, core.getTypeManager());
				
		if ( preprocessICs ) {
			if ( verbose ) numBefore = core.getContext().getConstraints().get(InteractionConstraint.class).size();
			if ( keepTimes ) StopWatch.start(msg("Preprocessing interaction constraints"));
			for ( Term programID : programIDs ) {
				yappy.saturateInteractionConstraints(core.getContext().getConstraints(), conCollection.get(programID), programID, core.getTypeManager());
			}
			if ( keepTimes ) StopWatch.stop(msg("Preprocessing interaction constraints"));
			if ( verbose ) {
				Logger.msg(getName(), "ICs before: " + numBefore + " after: " + core.getContext().getConstraints().get(InteractionConstraint.class).size(), 1);
				for ( InteractionConstraint ic : core.getContext().getConstraints().get(InteractionConstraint.class) ) {
					Logger.msg(getName(), "    - " + ic.getName(), 2);
				}
			}
		}
		
		
		/**
		 * TODO: change this...
		 */
		Global.initialContext = core.getContext().copy();
		
		/**
		 * Possible substitutions don't arrive here for some reason
		 */
		if ( verbose ) Logger.depth--;
		return core;
	}
}

