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
package org.spiderplan.causal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.prolog.PrologTools;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.constraints.VariableDomainRestriction;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;


/**
 * Contains some static methods used by different causal reasoning classes.
 *  
 * @author Uwe Koeckemann
 */

public class CausalReasoningTools {

	/**
	 * Given a {@link ConstraintDatabase} and {@link Operator}s find the set of variable
	 * names that are relevant to solving that problem. 
	 * 
	 * @param cDB A {@link ConstraintDatabase} containing some {@link OpenGoal} constraints.
	 * @param O A {@link Collection} of {@link Operator}s.
	 * @return Set of unique names of all relevant variables.
	 */
	public static Set<String> getRelevantVariables( ConstraintDatabase cDB, Collection<Operator> O ) {
		Set<String> usedVars = new HashSet<String>();
		
		for ( Operator o : O ) {
			for ( Statement p : o.getPreconditions() ) {
				usedVars.add(p.getVariable().getUniqueName());
			}
		}
		
		for ( OpenGoal g : cDB.get(OpenGoal.class) ) {
			usedVars.add(g.getStatement().getVariable().getUniqueName());
		}
		
		return usedVars;
	}
	
	public static Collection<Operator> getApplicableToMultiState( HashMap<Atomic,Collection<Term>> sReachableValues, Collection<Operator> O, ConstraintDatabase C, Set<String> usedVars, TypeManager tM ) {
		ArrayList<StateVariableOperatorMultiState> svOperators = new ArrayList<StateVariableOperatorMultiState>();
		HashMap<StateVariableOperatorMultiState,Operator> origOperatorLookUp = new HashMap<StateVariableOperatorMultiState, Operator>();

		for ( Operator o : O ) { 
			StateVariableOperatorMultiState svo;
	
			svo = o.getStateVariableBasedOperatorWithMultipleEffectValues(usedVars);
	
			svOperators.add(svo);
			origOperatorLookUp.put(svo, o);
		}

		HashSet<Operator> app = new HashSet<Operator>();
		HashSet<StateVariableOperatorMultiState> checkList = new HashSet<StateVariableOperatorMultiState>();
		
		
		HashSet<Operator> appNew = new HashSet<Operator>();
		
		for ( StateVariableOperatorMultiState svo : svOperators ) {
//			print("Working on:    " + svo.toString().replace("\n","\n    "), 4);
			
			/**
			 * 1) (Partial) grounding based on preconditions
			 */
			Collection<StateVariableOperatorMultiState> svoApp = svo.getApplicablePartialGroundFromMultiState(sReachableValues, tM);
			
//			print( "Reachable:", 4);
//			for ( Atomic key : sReachableValues.keySet() ) {
//				print( "    " + key + " -> " + sReachableValues.get(key), 4);
//			}
			
			/**
			 * 2) (Partial) grounding based on RelationalConstraints
			 */
			Operator o = origOperatorLookUp.get(svo);
			for ( StateVariableOperatorMultiState svAction : svoApp ) {
				if ( !checkList.contains(svAction) ) {
					
					Operator oCopy = o.copy();
					oCopy.substitute( svAction.getSubstitution() );
					Collection<Substitution> relConstraintSubst = PrologTools.getSubstitutionsSatisfyingRelationalConstraints(oCopy,C);
					
					if ( relConstraintSubst != null ) {
								
						if ( relConstraintSubst.isEmpty() ) {	// No additional substitution needed...
							appNew.add(oCopy);
						} else {								// Add action for all substitutions
							for ( Substitution theta : relConstraintSubst ) {
//								print("    " + theta.toString(), 4);
//								Substitution thetaCopy = svAction.getSubstitution().copy();
//								thetaCopy.add(theta);
//								StateVariableOperator svActionRelGround = svo.copy();
//								svActionRelGround.substitute(thetaCopy);
								
								Operator a = oCopy.copy();
								a.substitute(theta);
								appNew.add(a);
//								print("    Adding: " + a.getName(), 4);
							}
						}
					}
				}
			}
		}			
		
		/**
		 * 3) Ground every open variable (not constrained otherwise)
		 * 		It's important to do this last, because there may be
		 * 		a lot of combinations otherwise. This step only grounds
		 * 		variable Terms in the name of the operator, since all
		 * 		other terms should be ground by now.
		 */
		ArrayList<Operator> remList = new ArrayList<Operator>();
		ArrayList<Operator> addList = new ArrayList<Operator>();
		for ( Operator o : appNew ) {
//			print("Ground? " + o.getName(), 4);
			if ( !o.getName().isGround() ) {
//				print("---> No!", 4);
				remList.add(o);
				addList.addAll(o.getAllGround(tM));
			}
		}
		appNew.removeAll(remList);
		appNew.addAll(addList);
		
		/**
		 * 4) Filter out operators violating VariableDomainRestriction
		 */
		remList.clear();
		for ( Operator o : appNew ) {
			boolean violatesVarDomRestriction = false;
			for ( Constraint c : o.getConstraints() ) {
				if ( c instanceof VariableDomainRestriction ) {
					VariableDomainRestriction vdr = (VariableDomainRestriction)c;
					if ( !vdr.isConsistent() ) {
						violatesVarDomRestriction = true;
						break;
					}
				}
			}
			if ( violatesVarDomRestriction ) {
				remList.add(o);
			}
		}
		appNew.removeAll(remList);
		
		return appNew;
	}
	
}
