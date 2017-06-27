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
package org.spiderplan.causal.forwardPlanning.goals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spiderplan.representation.logic.Term;


/**
 * A single {@link Goal} and its requirements. 
 * 
 * @author Uwe Köckemann
 *
 */
public class SingleGoal implements Goal { 
	private static int currentID = 0;
	
	private Term var;
	private Term val;
	private boolean reached = false;
	private int ID;
	
	private boolean isLandmark = false;
	
	private ArrayList<Goal> requires = new ArrayList<Goal>();
	
	/**
	 * Create {@link SingleGoal} with variable and value {@link Term}s.
	 * @param variable
	 * @param value
	 */
	public SingleGoal( Term variable, Term value ) {
		this.var = variable;
		this.val = value;
		this.ID = currentID++;
	}
		
	/**
	 * The copy constructor.
	 * @param variable
	 * @param value
	 * @param ID
	 */
	private SingleGoal( Term variable, Term value, int ID ) {
		this.var = variable;
		this.val = value;
		this.ID = ID;
	}
	
	/**
	 * Reset internal static ID counter. TODO: static
	 */
	public static void resetID() { currentID = 0; }
	
	/**
	 * Get state-variable used by this goal.
	 * @return this goal's state-variable
	 */
	public Term 	getVariable() 	{ return var; };
	/**
	 * Get goal value.
	 * @return the value
	 */
	public Term 	getValue() 		{ return val; };
	
	@Override
	public void addRequirement( Goal req ) {
		requires.add(req);
	}
	@Override
	public void addRequirements( Collection<Goal> reqs ) {
		requires.addAll(reqs);
	}
	
	@Override
	public ArrayList<SingleGoal> getSingleGoals() {
		ArrayList<SingleGoal> r = new ArrayList<SingleGoal>();
		r.add(this);
		return r;
	}
	
	@Override
	public ArrayList<Goal> getRequirements() { 
		return requires;
	}
	
	@Override
	public boolean requirementsSatisfied() {
		for ( Goal req : requires ) {
			if ( !req.wasReached() ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean reachedInState( Map<Term,Term> state ) {
		if ( state.get(var) == null )
			return false;
		return state.get(var).equals(val); 
	}
	    
	@Override
	public boolean reachedInMultiState( Map<Term,List<Term>> state ) { 
		if ( state.get(var) == null )
			return false;
		return state.get(var).contains(val); 
	}
	
	@Override
	public void setReached(  boolean reached ) {
		this.reached = reached;
	}
	
	@Override
	public void setReached( Goal g, boolean reached ) {
		if ( this.equals(g) ) {
			this.reached = reached;
		} else {
			for ( Goal gReq : this.requires ) {
				if ( gReq.equals(g) ) {
					gReq.setReached(reached);
				}
			}
		}
	}
	
	@Override
	public boolean wasReached() {
		return reached;
	}
	
	@Override
	public boolean isLandmark() {
		return isLandmark;
	}
	
	@Override
	public void setLandmark(  boolean isLandmark ) {
		this.isLandmark = isLandmark;
	}
	
	@Override
	public ArrayList<Entry<Term,Term>> getEntries() {
		ArrayList<Entry<Term,Term>> r = new ArrayList<Map.Entry<Term,Term>>();
		r.add( new AbstractMap.SimpleEntry<Term, Term>(var, val) );
		return r;
	}
	
	@Override
	public String toString() {
		String r = "";
		if ( reached ) {
			r += "(x) ";
		} else {
			r += "( ) ";
		}
		
		r += ID + " " + var.toString() + "<-" + val.toString(); 
		
		if ( !requires.isEmpty() ) {
			r+= " requires: " + requires.toString();
		}
		return r;
	}
	
	/**
	 * Copy this goal.
	 * @return the copy
	 */
	@Override
	public SingleGoal copy() {
		SingleGoal c = new SingleGoal( var, val, ID );
		for ( Goal req : requires ) {
			c.requires.add(req.copy());
		}		
		c.reached = this.reached;		
		return c;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof SingleGoal ) {
			SingleGoal g = (SingleGoal)o;
			
			return g.ID == this.ID;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return ID;
	}
}
