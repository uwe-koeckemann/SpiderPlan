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
package org.spiderplan.causal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.goals.GoalCNF;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.LayeredPlan;
import org.spiderplan.representation.plans.SequentialPlan;
import org.spiderplan.search.MultiHeuristicNode;


//import representation.Operator;


public class ForwardPlanningNode extends MultiHeuristicNode {
	
	public enum EqualityCriteria { StateBased, PlanBased, ResultingStateBased };
	public static EqualityCriteria eqCriteria = EqualityCriteria.PlanBased;
	
	public ForwardPlanningNode prev;	
	public Map<Atomic,List<Term>> s;
	public StateVariableOperatorMultiState a;
	public GoalCNF g = new GoalCNF();
	
	public ConstraintDatabase C = new ConstraintDatabase();
	
	public ForwardPlanningNode( int numHeuristicValues ) {
		super(numHeuristicValues);
	}
	
	public Map<Atomic,List<Term>> getState() {
		return s;
	}
	
	public SequentialPlan getPlan( ) {
		SequentialPlan p;
		if ( prev != null ) {
			p = prev.getPlan();
			p.add(a.getName(), a.getSubstitution());
		} else {
			p = new SequentialPlan();
		}
		return p;
	}
	
	public ArrayList<StateVariableOperatorMultiState> getPlanList( ) {
		ArrayList<StateVariableOperatorMultiState> p;
		if ( prev != null ) {
			p = prev.getPlanList();
			p.add(a);
		} else {
			p = new ArrayList<StateVariableOperatorMultiState>();
			p.add(a);
		}
		return p;
	}
	
	public LayeredPlan getLayerdPlan( ) {
		ArrayList<StateVariableOperatorMultiState> pList = this.getPlanList();
		ArrayList<Set<StateVariableOperatorMultiState>> pLayered = new ArrayList<Set<StateVariableOperatorMultiState>>();
		int currentLayer = 0;
		
		LayeredPlan p = new LayeredPlan();
		pLayered.add( new HashSet<StateVariableOperatorMultiState>() );
		
		for ( StateVariableOperatorMultiState a : pList ) {
			boolean foundMutex = false; 
			
			for ( StateVariableOperatorMultiState aLast : pLayered.get(currentLayer) ) {
				if ( a.isMutex(aLast) ) {
					foundMutex = true;
					break;
				}
			}
			
			if ( !foundMutex ) {
				pLayered.get(currentLayer).add(a);
				p.add(currentLayer, a.getName(), a.getSubstitution());
			} else {
				currentLayer++;
				pLayered.add( new HashSet<StateVariableOperatorMultiState>() );
				pLayered.get(currentLayer).add(a);
				p.add(currentLayer, a.getName(), a.getSubstitution());
			}
		}
		
		return p;
	}

	@Override
	public int depth() {
		if ( prev == null ) 
			return 0;
		else
			return 1 + prev.depth();
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof ForwardPlanningNode ) {
			ForwardPlanningNode n = (ForwardPlanningNode)o;
			
			/**
			 * Different goal sets or reached goals
			 * -> different node
			 */
			if ( n.g.size() != this.g.size() ) {
				return false;
			} else {
				for ( int i = 0 ; i < this.g.size() ; i++ ) {
					if ( !n.g.get(i).equals(this.g.get(i)) ) {
						return false;
					} else {
						if ( n.g.get(i).wasReached() != this.g.get(i).wasReached() ) {
							return false;
						}
					}
				}
			}
			
			boolean equals;

			if ( eqCriteria.equals(EqualityCriteria.PlanBased ) ) {		// Equal if same sequence of actions
				equals = this.getPlanList().equals(n.getPlanList());
			} else if ( eqCriteria.equals(EqualityCriteria.StateBased) ) {													// Equal if same state and same action
				equals = n.a.equals(this.a) && n.s.equals(this.s);
			} else {
				HashMap<Atomic,List<Term>> resultingStateThis = new HashMap<Atomic, List<Term>>(); 
				HashMap<Atomic,List<Term>> resultingStateN = new HashMap<Atomic, List<Term>>();
				
				resultingStateThis.putAll(this.s);
				if ( this.a != null ) {
					resultingStateThis.putAll(this.a.getEffects());
				}
				resultingStateN.putAll(n.s);
				if ( n.a != null ) {
					resultingStateN.putAll(n.a.getEffects());
				}
				
				equals = resultingStateN.equals(resultingStateThis);
				
				
			}
			return equals;	
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if ( eqCriteria.equals(EqualityCriteria.PlanBased ) ) {		// Equal if same sequence of actions
			return this.getPlanList().hashCode();
		} else if ( eqCriteria.equals(EqualityCriteria.StateBased) ) {													// Equal if same state and same action
			if ( a == null ) { 
				return s.hashCode();
			}
			return 17 + 31 * s.hashCode() + 31 * a.hashCode();
		} else {
			HashMap<Atomic,List<Term>> resultingStateThis = new HashMap<Atomic, List<Term>>(); 
			
			resultingStateThis.putAll(this.s);
			
			if ( this.a != null ) {
				resultingStateThis.putAll(this.a.getEffects());
			}
			
			return resultingStateThis.hashCode();
		}
	}
	
	public ForwardPlanningNode getPredecessor( int depth ) {
		if ( depth > depth() ) {
			throw new IllegalArgumentException("Requested depth greater node depth.");
		} else if ( depth == depth() ) {
			return this;
		} else {
			return prev.getPredecessor(depth);
		}
		
	}
	
	@Override
	public String toString() {
		String r = "<null>";
		if ( s != null ) {
			r = "s = " + s.toString();
		}
		if ( a != null ) 
			r += "\na = " + a.getName().toString();
		if ( super.getHeuristicValues() != null ) {
			r += "\nh = " + Arrays.toString(getHeuristicValues());
		}
		if ( g != null ) {
			r += "\ng = " + g.toString();
		}
		return  r;
	}
	
//	@Override
//	public int compareTo(MultiHeuristicNode arg0) {	
//		ForwardPlanningMultiStateNode fdn = (ForwardPlanningMultiStateNode)arg0;
//		
//		return this.depth() - fdn.depth();
//	}
	
	@Override
	public int compareTo(MultiHeuristicNode arg0) {	
		ForwardPlanningNode fdn = (ForwardPlanningNode)arg0;
		
		boolean equalGoals = this.g.size() == fdn.g.size();
		int solvedThis = 0;
		int solvedArg0 = 0;
		
		if ( equalGoals ) {
			for ( int i = 0 ; i < this.g.size(); i++ ) {
				if ( !this.g.get(i).equals(fdn.g.get(i)) ) {
					equalGoals = false;
					break;
				}
				if ( this.g.get(i).wasReached() ) { 
					solvedThis++;
				}
				if ( fdn.g.get(i).wasReached() ) { 
					solvedArg0++;
				}
			}
		}
		
		if ( !equalGoals || (solvedThis - solvedArg0) == 0 ) {
			int hCompare = super.compareTo(arg0);

			if ( hCompare == 0 ) {
				if ( this.depth() < arg0.depth() ) {
					return -1;
				} else if ( this.depth() > arg0.depth() ) {
					return 1;
				}
				return 0;
			}
			return hCompare;
		} else {
			return solvedArg0 - solvedThis;
		}
	}
	
}
