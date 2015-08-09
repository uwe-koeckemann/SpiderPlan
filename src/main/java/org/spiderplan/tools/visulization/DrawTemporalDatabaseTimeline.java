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
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.temporal.stpSolver.IncrementalSTPSolver;
import org.spiderplan.tools.visulization.timeLineViewer.TimeLineViewer;

public class DrawTemporalDatabaseTimeline {
	
	public static void drawTimeline( ConstraintDatabase tDB, TypeManager tM ) {

		IncrementalSTPSolver csp = new IncrementalSTPSolver(0,1000);
	
		csp.isConsistent(tDB, tM);
	
		TimeLineViewer timeLineViewer = new TimeLineViewer();
		for ( Statement s : tDB.getStatements() ) {
			String tName = s.getVariable().toString();
			String value = s.getValue().toString(); 
			Term id = s.getKey();
			System.out.println(s);
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
