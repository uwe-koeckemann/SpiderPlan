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
package org.spiderplan.causal.pocl.heuristics;

import java.util.Collection;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.constraints.OpenGoal;

/**
 * Most basic possible POCL heuristic (originally used in SNLP and UCPOP)
 * <p>
 * Relevant papers:
 * <li> Younes, H. L. S. & Simmons, R. G. VHPOP: Versatile heuristic partial order planner Journal of AI Research, 2003, 20, 405-430 
 * <p>
 * @author Uwe K&ouml;ckemann
 *
 */
public class NumOpenGoals implements Heuristic {

	@Override
	public long calculateHeuristicValue( ConstraintDatabase cDB, Collection<Operator> O) {
		int openGoals = 0;
		for ( OpenGoal og : cDB.getConstraints().get(OpenGoal.class) ) {
			if ( !og.isAsserted() ) {
				openGoals++;
			}
		}
		
		return openGoals + cDB.getConstraints().get(Operator.class).size();
	}

}
