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
package org.spiderplan.temporal;

import java.util.ArrayList;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.PossibleIntersection;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.stopWatch.StopWatch;

public interface TemporalReasoningInterface {
	
	public boolean isConsistent( ConstraintDatabase cDB, TypeManager tM );
	public boolean isTemporalConsistent();
	public boolean isResourceConsistent();
	
	public ArrayList<Constraint> getSchedulingDecisions();
	
	public boolean hasInterval( Term k );
	public long getEST( Term interval );
	public long getLST( Term interval );
	public long getEET( Term interval );
	public long getLET( Term interval );
	public long[] getBoundsArray( Term interval );
	public AllenConstraint getBoundsConstraint( Term interval );
	
	public boolean possibleIntersection( PossibleIntersection pi );
	
	/**
	 * Get latest earliest end time ignoring all {@link Statement}s added to the
	 * ignored list. Ignored statements are used e.g. to avoid considering
	 * events that are out of control of any reasoner.
	 * 
	 * @param ignoredStatements These statements are ignored for makespan calculation. 
	 * @return Makespan of earliest time solutions.
	 */
	public long getMakespan( ConstraintDatabase cDB, ArrayList<Statement> ignoredStatements );
	public double getRigidity();
	
	/**
	 * Draw the earliest time-line solution
	 */
//	public void drawEarliestTimelines();
	/**
	 * Decides if {@link StopWatch} commands are used or ignored
	 * @param keepTimes <code>true</code> if {@link StopWatch} commands should be used,
	 * <code>false</code> otherwise.
	 */
	public void setKeepTimes( boolean keepTimes );
	
	/**
	 * Decides if statistics are kept.
	 * @param keepStats <code>true</code> if statistics should be kept,
	 * <code>false</code> otherwise.
	 */
	public void setKeepStatistics( boolean keepStats );
}
