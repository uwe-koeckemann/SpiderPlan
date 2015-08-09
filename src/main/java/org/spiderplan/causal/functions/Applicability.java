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
package org.spiderplan.causal.functions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.spiderplan.causal.StateVariableOperator;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;



public class Applicability {
	
	public static Collection<StateVariableOperator> getAllSVOActions( Map<Atomic,Term> s, Collection<StateVariableOperator> O , TypeManager tM  ){
		Collection<StateVariableOperator> app = new HashSet<StateVariableOperator>();
		
		HashMap<Atomic,Collection<Term>> sReachableValues = new HashMap<Atomic, Collection<Term>>();
		
		for ( Atomic var : s.keySet() ) {
			sReachableValues.put(var, new HashSet<Term>());
			sReachableValues.get(var).add(s.get(var));
		}
		
		int appSizeBefore = -1;
		while ( ! (appSizeBefore == app.size()) ) {
			appSizeBefore = app.size();
			Collection<StateVariableOperator> newApp = new HashSet<StateVariableOperator>();
			
			for ( StateVariableOperator svo : O ) {
				Collection<StateVariableOperator> svoApp = svo.getApplicablePartialGroundFromMultiState(sReachableValues, tM);
				for ( StateVariableOperator svoPartialGround : svoApp ) {
					newApp.addAll(svoPartialGround.getAllGround(tM));
				}
			}
			
			app.addAll(newApp);
			
			for ( StateVariableOperator sva :newApp ) {
				for ( Atomic var : sva.getEffects().keySet() ) {
					if ( !sReachableValues.containsKey(var) ) {
						sReachableValues.put(var, new HashSet<Term>());
					}
					sReachableValues.get(var).add(sva.getEffects().get(var));
				}
			}
		}

		
		return app;
	}
	
}
