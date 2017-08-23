/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.executor.observation;

import javax.swing.plaf.synth.SynthSpinnerUI;

import org.spiderplan.executor.Reactor;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;

/**
 * Waits for a {@link Statement} to be observed (e.g., via ROS message).
 * Used to wait for effects that the system has to wait for.
 * 
 * @author Uwe Koeckemann
 *
 */
public class ObservationReactor extends Reactor {
//	Substitution observedSub = null; TODO: This might be needed
	
	/**
	 * Reactor for a statement that needs to be observed.
	 * 
	 * @param target statement which needs to be observed
	 * @param lastChangingStatement lookup for most recent observations for all variables TODO: this could be solver better
	 */
	public ObservationReactor( Statement target, ConstraintDatabase cdb ) {
		super(target);
	}
	
	@Override
	public void initStart( ) {
	}
	
	@Override
	public boolean hasStarted( long EST, long LST, ConstraintDatabase execCDB ) {
		ValueLookup vLookup = execCDB.getUnique(ValueLookup.class);
		Statement lastChangingStatement = null;
		Substitution lastChangingValueSub = null;
		long latestEST = -1;
		
		for ( Statement s : execCDB.get(Statement.class) ) {
			
//			if ( !s.equals(this.target) ) {
				Substitution appliedSub = target.getVariable().match(s.getVariable());
				if ( appliedSub != null ) {  											// Variable matches
					
					if ( vLookup.hasInterval(s.getKey()) && vLookup.getEST(s.getKey()) > latestEST ) { 					// We only care about latest start time
						if ( appliedSub.add(target.getValue().match(s.getValue())) ) { 	// Value matches -> We found our observation
							if ( verbose ) print("Observation detected: " + s + " substitution: " + appliedSub.toString(), 2);
							latestEST = vLookup.getEST(s.getKey());
							lastChangingStatement = s;
							lastChangingValueSub = appliedSub;
							// This substitution cannot be undone unless we copy the previous CDB. But that should be okay.
							execCDB.substitute(appliedSub); 							// Substitution allows to make decisions based on observed values
							target = target.substitute(appliedSub);
						} else {
							lastChangingStatement = null;
							lastChangingValueSub = null;
						}
					} 
//				}
			}
		}
		/**
		 * Start if most recent statement matches what we want to observer
		 */
		if ( lastChangingStatement != null ) {
			print("Observed at " + t  + " [EST LST] = [" + EST + " " + LST + "]", 2);
			target = lastChangingStatement;
			AllenConstraint equals = new AllenConstraint(this.target.getKey(), lastChangingStatement.getKey(), TemporalRelation.Equals);
			execCDB.add(equals);
//			observedSub = lastChangingValueSub; //TODO: This needs to be applied to cdb in case the observed value is important.
			return true;
		} 
		return false;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET, ConstraintDatabase execCDB ) {	
		ValueLookup vLookup = execCDB.getUnique(ValueLookup.class);
//		Statement lastChangingStatement = null;
		
//		for ( Statement s : execCDB.get(Statement.class) ) {
//			if ( target.getVariable().equals(s.getVariable()) ) {
//				if ( !s.equals(this.target) ) {
//				if (  vLookup.hasInterval(target.getKey()) && vLookup.getLET(s.getKey()) > latestEST ) {
//					lastChangingStatement = s;
//				} 
//				}
//			}
//		}
		return vLookup.getEET(target.getKey()) <= t;
		
//		/**
//		 * End when actual end time reached
//		 */
//		if ( !lastChangingStatement.equals(target) ) {
//			print("Observation interval: " + t  + " [EET LET] = [" + EET + " " + LET + "]", 2);
//			return true;
//		} 
//		return false;
	}
}
