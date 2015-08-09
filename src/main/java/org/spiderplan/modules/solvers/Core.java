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
package org.spiderplan.modules.solvers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.plans.Plan;
import org.spiderplan.representation.types.TypeManager;

/**
 * Main data-structure manipulated by {@link Module}s. 
 * It contains a context in form of a {@link ConstraintDatabase},
 * a {@link Plan} and a {@link TypeManager}.
 * 
 * 
 * @author Uwe Köckemann
 *
 */
public class Core {	
	/**
	 * Used by solvers to assign their decision regarding decidability 
	 * of the problem posed by a {@link Core}.
	 * @author Uwe Köckemann
	 */
	public enum State { 
	/**
	 * Problem may be solvable, but requires search over {@link Resolver}s to address a (set of) flaws.
	 */
	Searching, /**
	 * The problem is not solvable (by this module). 
	 */
	Inconsistent, /**
	 * The problem is solvable and no more {@link Resolver}s need to be applied.
	 */
	Consistent, /**
	 * Module was killed during execution and no decision was reached.
	 */
	Killed };
		
	

	private ConstraintDatabase context = new ConstraintDatabase();
	private Collection<Operator> O = new HashSet<Operator>();
	private Plan plan = new Plan();
	private TypeManager tM;
	
	private Collection<String> inSignals = new ArrayList<String>();
	private HashMap<String,State> outSignals = new HashMap<String, State>();
		
	/**
	 * Create a new core.
	 */
	public Core() {
	}
	/**
	 * Set the context {@link ConstraintDatabase}.
	 * @param context A {@link ConstraintDatabase}
	 */
	public void setContext( ConstraintDatabase context ) {
		this.context = context;
	}
	/**
	 * Set the {@link Operator}s that are available to a planner
	 * @param O A {@link Collection} of {@link Operator}s
	 */
	public void setOperators( Collection<Operator> O ) {
		this.O = O;
	}
	/**
	 * Set the plan that was created to solve the problem
	 * posed by (a predecessor of) this {@link Core}.
	 * @param plan A {@link Plan}
	 */
	public void setPlan ( Plan plan ) {
		this.plan = plan;
	}
	
	/**
	 * Set the {@link TypeManager}
	 * @param tM A {@link TypeManager}
	 */
	public void setTypeManager( TypeManager tM ) { this.tM = tM; };
	

	/**
	 * Get the context {@link ConstraintDatabase}
	 * @return A {@link ConstraintDatabase} describing the context
	 */
	public ConstraintDatabase getContext() { return context; };
	/**
	 * Get the set of {@link Operator}s
	 * @return A {@link Set} of {@link Operator}s
	 */
	public Collection<Operator> getOperators() { return O ;};
	/**
	 * Get the {@link Plan}
	 * @return A {@link Plan}
	 */
	public Plan getPlan() { return plan; };
	/**
	 * Get the {@link TypeManager}
	 * @return A {@link TypeManager}
	 */
	public TypeManager getTypeManager() { return tM ; };
	
	/**
	 * Return a copy of this {@link Core} that can be changed without changing the original
	 * @return A copy of this {@link Core}
	 */
	public Core copy() {
		Core c = new Core();
		
		c.setContext(this.context.copy());
		
		Set<Operator> O = new HashSet<Operator>();
		for ( Operator o : this.O ) {
			O.add(o.copy());
		}
		
		c.setOperators(O);
		c.setPlan(this.plan.copy());
		
		c.setTypeManager(this.tM);
		
		for ( String k : this.outSignals.keySet() ) {
			c.setResultingState(k, this.outSignals.get(k));
		}
		
		ArrayList<String> inSignals = new ArrayList<String>();
		for ( String sig : this.inSignals ) {
			inSignals.add(sig);
		}
		c.setInSignals(inSignals);
							
		return c;
	}
	
	/**
	 * Used by {@link Module}s to change their behavior (e.g. NoGood, FromScratch)
	 * @return A {@link Collection} of {@link String}s that are signals to change a {@link Module}s behavior
	 */
	public Collection<String> getInSignals() { return inSignals; }
	/**
	 * Set the input signals of this {@link Core}. Which signals are understood depends on the specific {@link Module}
	 * @param inSignals A {@link Collection} of {@link String} describing signals that change the way in which {@link Module}s behave.
	 */
	public void setInSignals( Collection<String> inSignals ) { this.inSignals = inSignals ; }	
	/**
	 * Set the output {@link State} of a {@link Module}
	 * @param mName The name of the {@link Module}
	 * @param signal The {@link State} assigned by the {@link Module}
	 */
	public void setResultingState( String mName, State signal ) { outSignals.put(mName, signal); }
	/**
	 * Get the {@link State} resulting from a call to a {@link Module}. The {@link State} summarizes the
	 * {@link Module}'s judgment of the input.
	 * @param mName The name of the {@link Module}
	 * @return A {@link State} that is Consistent, Inconsistent, Searching or Killed
	 */
	public State getResultingState ( String mName ) { return outSignals.get(mName); } 
	/**
	 * Get a {@link String} representation of all resulting {@link State}s.
	 * @return A {@link String} summarizing the results of all {@link Module}s that worked on this {@link Core}
	 */
	public String getOutSignalsString() {
		String r = "";
		for ( String k : outSignals.keySet() ) {
			r += k+"."+outSignals.get(k)+",";
		}
		if ( r.length() != 0 ) {
			r = r.substring(0, r.length()-1);
		}
		return r;
	}

}
