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

import java.util.Random;

import org.spiderplan.representation.constraints.Statement;

/**
 * Extends reactor with a simple simulation of random start and end times 
 * 
 * @author Uwe Koeckemann
 *
 */
public class ReactorRandomSimulation extends Reactor {
		
	public ReactorRandomSimulation(Statement target) {
		super(target);
	}
		
	@Override
	public boolean hasStarted( long EST, long LST ) {		
		/**
		 * Start if actual start time reached
		 */
		if ( t >= EST ) {
			print("Starting time of "+ target +" is " + t + " [EST LST] = [" + EST + " " + LST + "]", 2);
			return true;
		} 
		return false;
	}
	
	@Override
	public boolean hasEnded( long EET, long LET ) {	
		Random r = new Random();
		
		int maxRange = 10;
		maxRange = Math.min((int)(LET-EET),maxRange);
		int pick;
		if ( maxRange > 1 ) { 
			pick = r.nextInt(maxRange);
		} else {
			pick = 0;
		}
		
		print("Randomly setting end time to " + (EET + pick), 2);
		
		/**
		 * End when actual end time reached
		 */
		if ( t >= (EET + pick) ) {
			print("Reached random end time at " + t, 2);
			return true;
		}		
		return false;
	}
}
