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
package org.spiderplan.tools.visulization;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;

/**
 * Visualization of time-lines (statements and temporal constraints)
 * @author Uwe Köckemann
 */
public class DrawTemporalDatabaseTimeline {
	
	/**
	 * Draw time-lines of a constraint-database.
	 * @param tDB the constraint database
	 */
	public static void drawTimeline( ConstraintDatabase tDB ) {

		IncrementalSTPSolver csp = new IncrementalSTPSolver(0,1000);
	
		csp.isConsistent(tDB);
	
		TimeLineViewer timeLineViewer = new TimeLineViewer();
		for ( Statement s : tDB.get(Statement.class) ) {
			String tName = s.getVariable().toString();
			String value = s.getValue().toString(); 
			Term id = s.getKey();
			long[] bounds = csp.getBoundsArray(id);
			
			if ( ! timeLineViewer.hasTrack(tName) ) {
				timeLineViewer.createTrack(tName);
			}
			timeLineViewer.createValue(tName, value, id.toString(), (int)bounds[0], (int)bounds[2]);
		}
		timeLineViewer.update();
		timeLineViewer.snapshot();
	}
}
