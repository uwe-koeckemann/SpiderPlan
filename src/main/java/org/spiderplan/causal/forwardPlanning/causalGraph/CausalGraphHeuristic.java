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
package org.spiderplan.causal.forwardPlanning.causalGraph;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.CommonDataStructures;
import org.spiderplan.causal.forwardPlanning.Heuristic;
import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.goals.Goal;
import org.spiderplan.causal.forwardPlanning.goals.SingleGoal;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Implementation of Causal Graph heuristic.
 * <p>
 * Relevant papers:
 * <li> Helmert, M. The fast downward planning system Journal of Artificial Intelligence Research, 2006, 26, 191-246 
 * <p>
 * @author Uwe Köckemann
 *
 */
public class CausalGraphHeuristic implements Heuristic {
 
	HashMap<Atomic, DomainTransitionGraph> DTGs;
	CausalGraph CG;
	TypeManager tM;
	
	Map<SingleGoal,Long> lastHeuristicValues;
	
	private final static Term UnknownValue = Term.createConstant("unknown_value");
	
	private int recursionDepth = 0;	
	
	private boolean keepTimes = false;
	
	/**
	 * Default constructor
	 */
	public CausalGraphHeuristic() {};
	
	/**
	 * Constructor that uses manually provided domain transition graphs, causal graph and type manager
	 * @param DTGs map from state-variable to its domain transition graph
	 * @param CG causal graph
	 * @param tM type manager
	 */
	public CausalGraphHeuristic( HashMap<Atomic, DomainTransitionGraph> DTGs, CausalGraph CG, TypeManager tM ) {
		this.DTGs = DTGs;
		this.CG = CG;
		this.tM = tM;
	}
	
//	private String space(int n) {
//		String s = "";
//		for ( int i = 0 ; i < n ; i++ ) {
//			s += "    ";
//		}
//		return s;
//	}
		
	
	/**
	 * Compute cost to change state-variable <code>v</code> from value <code>d</code> to <code>d_target</code> in state <code>s</code> 
	 * 
	 * @param s state
	 * @param v state-variable
	 * @param d current value
	 * @param d_target target value
	 * @return heuristic value for changing state-variable to target value
	 */
	public long computeCost( Map<Atomic,Term> s , Atomic v, Term d, Term d_target ) {
		if ( d.equals(d_target) ) {
			return 0;
		}
		
		if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Getting context key");
		ContextID contextID = new ContextID(s, v, d);		
		if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Getting context key");
		
		Long c = costCache.get(new QueryID(contextID, d, d_target));
		if ( c != null )
			return c;
		
		long costToTarget = Long.MAX_VALUE;

		/**
		 * Immediate predecessors of v in causal graph:
		 */			
		HashMap<Term,HashMap<Atomic,Term>> localState = new HashMap<Term,HashMap<Atomic,Term>>();
		HashMap<Atomic,Term> localState_d = new HashMap<Atomic,Term>();
		Term d_relevant;
		
		QueryID costKey;
		Term e_prime, e;
		Term d_prime, d_second;
		HashMap<Atomic,Term> localState_d_second;
		
		for ( Atomic v_relevant : CG.getPredecessors(v) ) {
			d_relevant = s.get(v_relevant);
			if ( d_relevant != null ) {					// value is set
				localState_d.put( v_relevant, d_relevant );			
			} else {									// unknown value (still allows transitions from special term UnknownValue)
				localState_d.put( v_relevant, UnknownValue );
			}
		}

		localState.put(d, localState_d);

		DomainTransitionGraph DTG = DTGs.get(v);
		HashSet<Term> unreached = new HashSet<Term>();
		
		if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Init costs... (depth " + recursionDepth+")");
		HashMap<Term,QueryID> keyLookUp = new HashMap<Term, QueryID>();
		for ( Term d_prime_t : tM.getPredicateTypes(v.getUniqueName(), -1).getDomain() ) {
			keyLookUp.put( d_prime_t , new QueryID(contextID, d, d_prime_t) );
			unreached.add(d_prime_t);
			if ( !d_prime_t.equals(d) ) {
				costCache.put(keyLookUp.get(d_prime_t), Long.MAX_VALUE);
			} else {
				costCache.put(keyLookUp.get(d_prime_t), (long)0);
			}
		}	

		/**
		 * Add UnknownValue Term which can be changed by actions that 
		 * have no precondition on a variable but can set its value.
		 */
		keyLookUp.put( UnknownValue , new QueryID(contextID, d, UnknownValue) );
		unreached.add(UnknownValue);
		if ( !UnknownValue.equals(d) ) {
			costCache.put(keyLookUp.get(UnknownValue), new Long(Long.MAX_VALUE));
		} else {
			costCache.put(keyLookUp.get(UnknownValue), new Long(0));
		}
		
		if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Init costs... (depth " + recursionDepth+")");
		
		if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Loop (depth " + recursionDepth+")");
		Collection<DomainTransitionEdge> outEdges;
					
		while ( !loopDone(unreached, d, keyLookUp) ) {
			
			if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Choose next (depth " + recursionDepth+")");
			d_prime = chooseNext(unreached, d, keyLookUp);
			if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Choose next (depth " + recursionDepth+")");
			
			
			unreached.remove(d_prime);
			HashMap<Atomic,Term> localState_d_prime = localState.get(d_prime);	
			
			if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Getting out edges (depth " + recursionDepth+")");
			outEdges =  DTG.getGraph().getOutEdges(d_prime);
			if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Getting out edges (depth " + recursionDepth+")");
			
			
			
			if ( outEdges != null ) {
				for ( DomainTransitionEdge t : outEdges ) { 
					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Calc. sub-cost (depth " + recursionDepth+")");
					
					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] getDest(t) (depth " + recursionDepth+")");
					d_second = t.getDest(); 
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] getDest(t) (depth " + recursionDepth+")");
					
					long transitionCost = 1;
					
					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] inner loop (depth " + recursionDepth+")");
					for ( Atomic v_prime : t.getConditions().keySet() ) { 
						e_prime = t.getConditions().get(v_prime);
						e = localState_d_prime.get(v_prime);
						
						
						// Change that could cause problems?
						// Should work though, because if v_prime's domain transition graph
						// has no outgoing edge for UnknownValue it should return INF
//							if ( e == null ) {
//								e = UnknownValue;
//							}
											
						if ( e != null ) {
							if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Recursive call (depth " + recursionDepth+")");
							recursionDepth++;
							long subCost = computeCost(s, v_prime, e, e_prime);
							recursionDepth--;
							if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Recursive call (depth " + recursionDepth+")");
							

							if ( subCost == Long.MAX_VALUE ) {
								transitionCost = Long.MAX_VALUE;
								break;
							}
							transitionCost += subCost;
						}
					}
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] inner loop (depth " + recursionDepth+")");

					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] NewCost (depth " + recursionDepth+")");
					long stepCost = costCache.get(new QueryID(contextID,d,d_prime));
					
					long newCost;
					if ( stepCost == Long.MAX_VALUE || transitionCost == Long.MAX_VALUE ) {
						newCost = stepCost;
					} else {
						newCost =  stepCost + transitionCost;
					}
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] NewCost (depth " + recursionDepth+")");
					
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Calc. sub-cost (depth " + recursionDepth+")");
					
					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Cost update (depth " + recursionDepth+")");
					costKey = new QueryID( contextID, d, d_second );
					if ( newCost < costCache.get(costKey) ) {
						if ( d_second.equals(d_target) ) {
							costToTarget = newCost;
						}
						costCache.put(costKey, newCost);
						localState_d_second = new HashMap<Atomic, Term>();
						localState_d_second.putAll(localState_d_prime);
						localState_d_second.putAll( t.getConditions() );
						localState.put(d_second, localState_d_second);
					}
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Cost update (depth " + recursionDepth+")");
				}
			}
		}
		if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Loop (depth " + recursionDepth+")");
		
		return costToTarget;	
	}
	
	
	private Term chooseNext( HashSet<Term> unreached, Term d, HashMap<Term,QueryID> keyLookUp ) {
		
		Term argMin = null;
		Long min = Long.MAX_VALUE;
		
		for ( Term d_prime : unreached ) {
			Long cost = costCache.get(keyLookUp.get(d_prime));
			if ( cost != null  && cost.longValue() < min ) {
				min = cost.longValue();
				argMin = d_prime;
			}
		}
		
		return argMin;
	}
	
	private boolean loopDone( HashSet<Term> unreached, Term d, HashMap<Term,QueryID> keyLookUp ) {
		if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Loop done");
		if ( unreached.isEmpty() ) {
			if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Loop done");
			return true;
		}
		for ( Term d_prime : unreached ) {
			if ( costCache.get(keyLookUp.get(d_prime)) < Long.MAX_VALUE ) {
				if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Loop done");
				return false;
			}

		}
		if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Loop done");
		return true;
	}
	
//	private HashSet<ContextID> processedContext = new HashSet<ContextID>();	
	private HashMap<QueryID,Long> costCache = new HashMap<QueryID, Long>();
	
	private class ContextID {
		private Map<Atomic,Term> s;
		private Atomic v;
		private Term d;
		
		public ContextID( Map<Atomic,Term> s, Atomic v, Term d ) {
			Term val;
			this.s = new HashMap<Atomic, Term>();
			for ( Atomic var : CG.getAncestors(v) ) {
				 val = s.get(var);
				 if ( val != null ) {
					this.s.put(var, val);
				 }
			}
			this.v = v; 
			this.d = d;
		}
		
		@Override
		public boolean equals( Object o ) {
			ContextID cID = (ContextID)o;
			return cID.s.equals(this.s) && cID.v.equals(this.v) && cID.d.equals(this.d); 
		}
		@Override
		public int hashCode() {
			int result = 11;
			result = 37*result+s.hashCode();
			result = 37*result+v.hashCode();
			result = 37*result+d.hashCode();
			return result;
		}
	}
	
	private class QueryID {
		private ContextID cID;
		private Term d_from;
		private Term d_to;
		
		public QueryID( ContextID cID, Term d_from, Term d_to ) {
			this.cID = cID; this.d_from = d_from; this.d_to = d_to;
		}
		
		@Override
		public boolean equals( Object o ) {
			QueryID qID = (QueryID)o;
			return qID.cID.equals(this.cID) && qID.d_from.equals(this.d_from) && qID.d_to.equals(this.d_to); 
		}
		@Override
		public int hashCode() {
			int result = 11;
			result = 37*result+cID.hashCode();
			result = 37*result+d_from.hashCode();
			result = 37*result+d_to.hashCode();
			return result;
		}
	}
	
	@Override
	public void initializeHeuristic( Collection<Goal> g, Collection<StateVariableOperator> A, TypeManager tM ) {
		this.tM = tM;
		  
		DTGs = new HashMap<Atomic, DomainTransitionGraph>();
		Set<Atomic> variables = new HashSet<Atomic>();
		
		for ( Goal goal : g ) {
			for ( SingleGoal sg : goal.getSingleGoals() ) {
				variables.add(sg.getVariable());
			}
		}

		for ( StateVariableOperator a : A ) {
			variables.addAll(a.getPreconditions().keySet());
			variables.addAll(a.getEffects().keySet());
		}
		for ( Atomic v : variables ) {
			DTGs.put( v, new DomainTransitionGraph(v, A, tM));	
		}
		CG = new CausalGraph(A);
		
//		for ( Atomic k : DTGs.keySet() ) {
//			DTGs.get(k).draw();
//		}
//		CG.draw();
//		Loop.start();
	}


	@Override
	public long calculateHeuristicValue( Map<Atomic, Term> s, Collection<Goal> g, CommonDataStructures dStructs ) {
		if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Calculate heuristic");			
		
		HashMap<SingleGoal,Long> singleGoalCosts = new HashMap<SingleGoal, Long>();

		for ( Goal goals : g ) {
			for ( SingleGoal goal : goals.getSingleGoals() ) {
				if ( !goal.wasReached() && goal.requirementsSatisfied() ) {
					Entry<Atomic,Term> g_sva = new AbstractMap.SimpleEntry<Atomic, Term>(goal.getVariable(), goal.getValue() );
					
					if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Checking relevance");
					Term svaValue = s.get(g_sva.getKey());
					boolean hasPreviousValue = s.containsKey( g_sva.getKey() );
					boolean hasDifferentPreviousValue = false;
					if ( hasPreviousValue ) {
						hasDifferentPreviousValue = !g_sva.equals(svaValue);
					}
					if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Checking relevance");
					
					if ( hasPreviousValue && hasDifferentPreviousValue ) {					
						if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Getting goal value");
						Term goalValue = g_sva.getValue();
						if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Getting goal value");
		
						if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Compute cost (caching)");
						long c1 = this.computeCost( s, g_sva.getKey(), svaValue, goalValue );					
						if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Compute cost (caching)");
						
						singleGoalCosts.put(goal, Long.valueOf(c1));
	
					} else if ( !hasPreviousValue ) {		// Only possible to leave UnknownValue if an action has no precondition on g_sva.getValue()
						if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Getting goal value");
						Term goalValue = g_sva.getValue();
						if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Getting goal value");
		
						if ( keepTimes ) StopWatch.start("[FastDownwardHeuristic] Compute cost (caching)");
						long c1 = this.computeCost( s, g_sva.getKey(), UnknownValue, goalValue );					
						if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Compute cost (caching)");

						singleGoalCosts.put(goal, Long.valueOf(c1));
					}
				}	
			}
		}

		if ( keepTimes ) StopWatch.stop("[FastDownwardHeuristic] Calculate heuristic");
		
		/**
		 * Add up costs. In case of disjunctions take minimum of members.
		 */
		long h = 0;

		for ( Goal goal : g ) {
			if (  !goal.wasReached() &&  goal.requirementsSatisfied() ) {
				if ( goal instanceof SingleGoal ) {				// Just add up or fail for INF
					if ( singleGoalCosts.get(goal).longValue() == Long.MAX_VALUE ) {
						h = Long.MAX_VALUE;
						break;
					} else {
						long tmp = singleGoalCosts.get(goal).longValue();

						h += tmp;
					}
				} else {
					long disjunctionCost = Long.MAX_VALUE;
					for ( SingleGoal disGoal : goal.getSingleGoals() ) {	// Take min
						long singleGoalCost = singleGoalCosts.get(disGoal).longValue();
						if ( singleGoalCost < disjunctionCost ) {
							disjunctionCost = singleGoalCost;
						}
					}
					if ( disjunctionCost == Long.MAX_VALUE ) {		// All members are impossible
						h = Long.MAX_VALUE;
						break;
					}
				}
			}
		}		
		lastHeuristicValues = singleGoalCosts;
		
//		Loop.start();
		
		return h;
	}

	@Override
	public Map<SingleGoal, Long> getLastHeuristicValues() {
		return lastHeuristicValues;
	}

	@Override
	public void setKeepTimes(boolean keepTimes ) {
		this.keepTimes = keepTimes;		
	}

}
