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
package org.spiderplan.modules.tools;

import java.util.ArrayList;

import org.spiderplan.modules.solvers.Core;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.temporal.PlanningInterval;
import org.spiderplan.representation.logic.Substitution;

/**
 * Contains some static methods to retrieve certain types of {@link Expression}s
 * from a {@link Core}.
 * 
 * @author Uwe Köckemann
 *
 */
public class ConstraintRetrieval {

	/**
	 * Get a unique {@link PlanningInterval} {@link Expression} from a {@link Core}.
	 * Throws an exception if there is more than one and <code>null</code> if it
	 * does not exist.
	 * @param core The {@link Core} in which we are looking for the {@link PlanningInterval}. 
	 * @return A unique {@link PlanningInterval} if exactly one exists, <code>null</code> otherwise.
	 */
	public static PlanningInterval getPlanningInterval( Core core ) {
		ArrayList<PlanningInterval> pIcons = new ArrayList<PlanningInterval>();
		//pIcons.addAll(core.get(PlanningInterval.class));
		pIcons.addAll(core.getContext().get(PlanningInterval.class));
		pIcons.addAll(core.getContext().get(PlanningInterval.class));
		if ( pIcons.size() == 1 ) {
			return pIcons.get(0);
		} else if ( pIcons.size() > 1 ) {
			for ( int i = 0 ; i < pIcons.size()-1 ; i++ ) {
				if ( !pIcons.get(i).equals(pIcons.get(i+1))) {
					String listStr = "";
					for ( PlanningInterval pI : pIcons ) {
						listStr += "\n" + pI.toString();
					}
					throw new IllegalStateException("Found multiple non-equal PlanningInterval constraints:"+listStr);
				}
			}
			return pIcons.get(0);
		}
		return null;
	}
	
	/**
	 * Get a unique {@link PlanningInterval} {@link Expression} from a {@link ConstraintDatabase}.
	 * Throws an exception if there is more than one and <code>null</code> if it
	 * does not exist.
	 * @param cDB The {@link ConstraintDatabase} in which we are looking for the {@link PlanningInterval}. 
	 * @return A unique {@link PlanningInterval} if exactly one exists, <code>null</code> otherwise.
	 */
	public static PlanningInterval getPlanningInterval( ConstraintDatabase cDB ) {
		ArrayList<PlanningInterval> pIcons = new ArrayList<PlanningInterval>();
		//pIcons.addAll(core.get(PlanningInterval.class));
		pIcons.addAll(cDB.get(PlanningInterval.class));
		pIcons.addAll(cDB.get(PlanningInterval.class));
		if ( pIcons.size() == 1 ) {
			return pIcons.get(0);
		} else if ( pIcons.size() > 1 ) {
			for ( int i = 0 ; i < pIcons.size()-1 ; i++ ) {
				if ( !pIcons.get(i).equals(pIcons.get(i+1))) {
					String listStr = "";
					for ( PlanningInterval pI : pIcons ) {
						listStr += "\n" + pI.toString();
					}
					throw new IllegalStateException("Found multiple non-equal PlanningInterval constraints:"+listStr);
				}
			}
			return pIcons.get(0);
		}
		return null;
	}
		
	/**
	 * Checks if there is an asserted constraint in a {@link ConstraintDatabase} that matches a 
	 * given {@link Expression}
	 * @param cdb A {@link ConstraintDatabase}
	 * @param c A {@link Matchable}
	 * @return <code>true</code> if there exists a {@link Expression} in <code>cDB</code> that matches <code>c</code>
	 */
	public static boolean hasNoMatchingAssertedConstraint( ConstraintDatabase cdb, Expression c ) {
		if ( ! (c instanceof Assertable) ) {
			return true;
		}
		
		if ( c instanceof Matchable ) {
			Matchable mC = (Matchable)c;
			for ( Expression cIn : cdb.get(c.getClass()) ) {
				Substitution s = mC.match(cIn); 
				if ( s != null ) {
					if ( ((Assertable)cIn).isAsserted() ) {
						return false;
					}
				}
			}
		} else {
			for ( Expression cIn : cdb.get(c.getClass()) ) {
				if ( c.equals(cIn) ) {
					if ( ((Assertable)cIn).isAsserted() ) {
						return false;
					}
				}
			}
		}	
		return true;
	}
	
	/**
	 * Check if a constraint has been asserted in a context.
	 * 
	 * @param cdb context
	 * @param c constraint
	 * @return <code>true/code> if <code>c</code> has been asserted in <code>cdb</code>, <code>false</code> otherwise.
	 */
	public static boolean hasNoEqualAssertedConstraint( ConstraintDatabase cdb, Expression c ) {
		if ( ! (c instanceof Assertable) ) {
			return true;
		}
		for ( Expression cIn : cdb.get(c.getClass()) ) {
			if ( c.equals(cIn) ) {
				if ( ((Assertable)cIn).isAsserted() ) {
					return false;
				}
			}
		}

		return true;
	}
}
