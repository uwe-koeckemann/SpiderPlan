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
package org.spiderplan.executor.simulation;

import org.spiderplan.executor.Reactor;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;

/**
 * Extends reactor with a simple simulation of perfect start and end times 
 * (i.e., it uses earliest-time solution). 
 * 
 * @author Uwe Koeckemann
 *
 */
public class ReactorPerfectSimulation extends Reactor {
	
	/**
	 * Default constructor.
	 * 
	 * @param target statement to be executed
	 */
	public ReactorPerfectSimulation(Statement target) {
		super(target);
	}
	
	@Override
	public boolean hasStarted( long EST, long LST, ConstraintDatabase execCDB ) {
		/**
		 * Start if actual start time reached
		 */
		if ( t >= EST ) {
			print("Starting at " + t  + " [EST LST] = [" + EST + " " + LST + "]", 2);
			return true;
		} 
		return false;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET, ConstraintDatabase execCDB ) {	
		/**
		 * End when actual end time reached
		 */
		if ( t >= EET ) {
			print("Finishing at " + t  + " [EET LET] = [" + EET + " " + LET + "]", 2);
			return true;
		} 
		return false;
	}
}
