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
//package org.spiderplan.representation.constraints;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.spiderplan.representation.ConstraintDatabase;
//import org.spiderplan.representation.logic.Atomic;
//import org.spiderplan.representation.logic.Substitutable;
//import org.spiderplan.representation.logic.Substitution;
//import org.spiderplan.representation.logic.Term;
//
//
///**
// * Contains a {@link ConstraintDatabase} representing new goals. 
// */
//public class NewGoal extends Constraint implements Substitutable {
//	
//	final private static Term ConstraintType = new Term("delete",false);
//	
//	private ConstraintDatabase goal;
//	
//	/**
//	 * Create {@link NewGoal} constraint by providing the new goal
//	 * in form of a {@link ConstraintDatabase}.
//	 * 
//	 * @param goal
//	 */
//	public NewGoal( ConstraintDatabase goal ) {
//		this.goal = goal;
//	}
//	
//	/**
//	 * Copy constructor
//	 * @param g
//	 */
//	public NewGoal( NewGoal g ) {		
//		this.goal = g.goal.copy();
//		this.setAsserted(g.isAsserted());
//	}
//	
//	/**
//	 * Returns the new goal {@link ConstraintDatabase}
//	 * @return
//	 */
//	public ConstraintDatabase getNewGoalDB() {
//		return goal;
//	}
//	
//	@Override
//	public Collection<Term> getVariableTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(goal.getVariableTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Term> getGroundTerms() {
//		ArrayList<Term> r = new ArrayList<Term>();
//		r.addAll(goal.getGroundTerms());
//		return r;		
//	}
//	@Override
//	public Collection<Atomic> getAtomics() {
//		ArrayList<Atomic> r = new ArrayList<Atomic>();
//		r.addAll(goal.getAtomics());
//		return r;		
//	}
//	
//	@Override
//	public void substitute(Substitution theta) {
//		goal.substitute(theta);
//	}
//	
//	@Override
//	public NewGoal copy() {
//		NewGoal c = new NewGoal(this);
//		c.setDescription(this.getDescription());
//		c.setAsserted(this.isAsserted());
//		return c;
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if ( o instanceof NewGoal ) {
//			return this.toString().equals(o.toString());
//		}
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return this.toString().hashCode();
//	}	
//	
//	@Override
//	public String toString() {
//		return "NewGoal:\n    " + goal.toString().replace("\n", "\n    ");
//	}
//
//}
