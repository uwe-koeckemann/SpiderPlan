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
package org.spiderplan.executor;

import java.util.Map;

import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.logic.Atomic;

/**
 * Waits for a {@link Statement} to be observed (e.g., via ROS message).
 * Used to wait for effects that the system has to wait for.
 * 
 * @author Uwe Koeckemann
 *
 */
public class ReactorObservation extends Reactor {
		
	Map<Atomic,Statement> lastChangingStatement;
	
	/**
	 * Reactor for a statement that needs to be observed.
	 * 
	 * @param target statement which needs to be observed
	 * @param lastChangingStatement lookup for most recent observations for all variables TODO: this could be solver better
	 */
	public ReactorObservation( Statement target, Map<Atomic,Statement> lastChangingStatement ) {
		super(target);
		this.lastChangingStatement = lastChangingStatement;
	}
	
	@Override
	public void initStart( ) {
	}
	
	@Override
	public boolean hasStarted( long EST, long LST ) {
		/**
		 * Start if actual start time reached
		 */
		if ( lastChangingStatement.containsKey(target.getVariable()) 
				&& lastChangingStatement.get(target.getVariable()).getValue().equals(target.getValue()) ) {
			print("Observed at " + t  + " [EST LST] = [" + EST + " " + LST + "]", 2);
			return true;
		} 
		return false;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET ) {	
		/**
		 * End when actual end time reached
		 */
		if ( !lastChangingStatement.get(target.getVariable()).getValue().equals(target.getValue()) ) {
			print("Observation interval: " + t  + " [EET LET] = [" + EET + " " + LET + "]", 2);
			return true;
		} 
		return false;
	}
}
