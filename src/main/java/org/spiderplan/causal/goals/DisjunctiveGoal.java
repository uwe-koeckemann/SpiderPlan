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
package org.spiderplan.causal.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;


public class DisjunctiveGoal implements Goal {
	private static int currentID = 0;
	
	private ArrayList<SingleGoal> disGoals = new ArrayList<SingleGoal>();
	private boolean reached = false;
	private int ID;
	
	private boolean isLandmark = false;
	
	private ArrayList<Goal> requires = new ArrayList<Goal>();
	
	public DisjunctiveGoal( ArrayList<SingleGoal> disGoals ) {
		this.disGoals = disGoals;
		this.ID = currentID++;
	}
	
	private DisjunctiveGoal( ArrayList<SingleGoal> disGoals, int ID ) {
		this.disGoals = disGoals;
		this.ID = ID;
	}
	
	public static void resetID() { currentID = 0; }
	
	@Override
	public void addRequirements( Collection<Goal> reqs ) {
		requires.addAll(reqs);
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
	public boolean reachedInState( Map<Atomic,Term> state ) {
		for ( SingleGoal g : this.disGoals ) {
			if ( state.get(g.getVariable()).equals(g.getValue()) ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean reachedInMultiState(Map<Atomic, List<Term>> state) {
		for ( SingleGoal g : this.disGoals ) {
			if ( g.reachedInMultiState(state) ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setReached( boolean reached ) {
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
			for ( Goal gReq : this.disGoals ) {
				if ( gReq.equals(g) ) {
					gReq.setReached(reached);
				}
			}
		}
				
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
	public boolean wasReached() {
		return reached;
	}
	
	@Override
	public ArrayList<Entry<Atomic,Term>> getEntries() {
		ArrayList<Entry<Atomic,Term>> r = new ArrayList<Map.Entry<Atomic,Term>>();
		for ( SingleGoal g : disGoals ) {
			r.addAll(g.getEntries() );
		}
		return r;
	}
	
	@Override
	public void addRequirement(Goal req) {
		this.requires.add(req);
	}

	@Override
	public ArrayList<SingleGoal> getSingleGoals() {
		return disGoals;
	}
	
	@Override
	public String toString() {
		String r = disGoals.get(0).toString();
		for ( int i = 1 ; i < disGoals.size() ; i++ ) {
			r += " v " + disGoals.get(i);
		}
		return r + " req: " + requires.toString(); 
	}
	
	/**
	 * Return copy of this goal
	 * @return
	 */
	@Override
	public DisjunctiveGoal copy() {
		ArrayList<SingleGoal> cDisGoals = new ArrayList<SingleGoal>();
		for ( SingleGoal g : this.disGoals ) {
			cDisGoals.add( g.copy() );
		}
		
		DisjunctiveGoal c = new DisjunctiveGoal( cDisGoals, ID );
		
		for ( Goal req : requires ) {
			c.requires.add(req.copy());
		}
		
		c.reached = this.reached;
		
		return c;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof DisjunctiveGoal ) {
			DisjunctiveGoal g = (DisjunctiveGoal)o;
			
			return g.ID == this.ID;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return ID;
	}


}
