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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.spiderplan.causal.goals.Goal;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;



/**
 * Implementation of causal reasoning lookahead.
 * <p>
 * Relevant papers:
 * <li> Vidal, V. YAHSP2: Keep It Simple, Stupid Proc.~of the Int'l Planning Competition (IPC-7), 2011
 * <p>
 * @author Uwe Köckemann
 *
 */
public class YAHSPLookahead {
	private static class CostActionPair implements Comparable<CostActionPair> {
		Long c;
		StateVariableOperatorMultiState a;
		
		public CostActionPair( Long c, StateVariableOperatorMultiState a ) {
			this.c = c;
			this.a = a;
		}

		@Override
		public int compareTo(CostActionPair o) {
			if ( this.c.longValue() < o.c.longValue() ) {
				return -1;
			} else if ( this.c.longValue() > o.c.longValue() ) {
				return 1;
			} else {
				/**
				 * If costs are equal we prefer an order based
				 * on interference if possible:
				 */
				boolean thisInterferesWithO = false;
				boolean oInterferesWithThis = false;
				
				for ( Atomic preKey : this.a.getPreconditions().keySet() ) {
					List<Term> effList = o.a.getEffects().get(preKey);
					Term preVal = this.a.getPreconditions().get(preKey);
					if ( effList != null && !effList.contains(preVal) ) {	
						// o has effect that overwrites but does not include precondition of this
						oInterferesWithThis = true;
						break;
					}
				}
				
				for ( Atomic preKey : o.a.getPreconditions().keySet() ) {
					List<Term> effList = this.a.getEffects().get(preKey);
					Term preVal = o.a.getPreconditions().get(preKey);
					if ( effList != null && !effList.contains(preVal) ) {
						// this has effect that overwrites but does not include precondition of o
						thisInterferesWithO = true;
						break;
					}
				}

				if ( thisInterferesWithO && !oInterferesWithThis ) {
					return 1;	// this is better after o
				} else if ( !thisInterferesWithO && oInterferesWithThis ) {
					return -1;	// o is better after this
				} else {
					return 0;  // no -or- mutual interference -> whatever
				}
			}	
		}
	}
	
	/**
	 * Implementation of algorithm 5 of
	 * [Vidal, 2010, YAHSP2: Keep It Simple, Stupid,
	 *   Proceedings of the International Planning Competition (IPC-7)]
	 * with some changes to allow using multi-states.
	 * Also note that the provided list of goals is only the list of
	 * goals whose requirements are fulfilled. This means that a single
	 * lookahead & extractRelaxedPlan call may not solve the problem. 
	 * @param multiState
	 * @param cost
	 * @return
	 */
	private static LinkedList<StateVariableOperatorMultiState> extractRelaxedPlan( Map<Atomic,List<Term>> multiState, Map<Object,Long> cost, Collection<Entry<Atomic,Term>> G, Collection<StateVariableOperatorMultiState> A ) {
		LinkedList<CostActionPair> rPlanTmp = new LinkedList<CostActionPair>();
		
		LinkedList<Entry<Atomic,Term>> goals = new LinkedList<Entry<Atomic,Term>>();
		goals.addAll(G);
		
		Set<Entry<Atomic,Term>> satisfied = new HashSet<Map.Entry<Atomic,Term>>();
		
		for ( Atomic k : multiState.keySet() ) {
			for ( Term v : multiState.get(k) ) {
				satisfied.add(new AbstractMap.SimpleEntry<Atomic,Term>(k,v));
			}
		}
		
		while ( !goals.isEmpty() ) {
			Entry<Atomic,Term> g = goals.getFirst();
			goals.removeFirst();
			
			if ( !satisfied.contains(g) ) {
				satisfied.add(g);
				
				StateVariableOperatorMultiState a = null;
				long min = Long.MAX_VALUE;
				
				for ( StateVariableOperatorMultiState sva : A ) {
					List<Term> effects = sva.getEffects().get(g.getKey());
					if ( effects != null ) {
						if ( effects.contains(g.getValue())) {
							long c = cost.get(sva.getName()); 
							if ( c < min ) {
								min = c;
								a = sva;
							}
						}
					}
				}
				
				if ( !rPlanTmp.contains(a) && a != null ) {
					CostActionPair cap = new CostActionPair(Long.valueOf(min), a);
					rPlanTmp.add(cap);
					goals.addAll(a.getPreconditions().entrySet());
				}
			}
		}
		
		Collections.sort(rPlanTmp);
				
		LinkedList<StateVariableOperatorMultiState> rPlan = new LinkedList<StateVariableOperatorMultiState>();
		
		for ( CostActionPair cap : rPlanTmp ) {
			rPlan.add(cap.a);
		}
		
		return rPlan;
	}
	
	private static boolean contributesToPreconditions( StateVariableOperatorMultiState a, StateVariableOperatorMultiState b ) {
		for ( Atomic k : b.getPreconditions().keySet() ) {
			List<Term> effList = a.getEffects().get(k);
			if ( effList != null ) {
				if ( effList.contains( b.getPreconditions().get(k) ) ) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static Map<Atomic,List<Term>> getEffectOverlap( StateVariableOperatorMultiState a, StateVariableOperatorMultiState b ) {
		Map<Atomic,List<Term>> effOverlap = new HashMap<Atomic, List<Term>>();
		
		for ( Atomic k : a.getEffects().keySet() ) {
			if ( b.getEffects().containsKey(k) ) {
				List<Term> sharedEff = new ArrayList<Term>();
				for ( Term e : a.getEffects().get(k) ) {
					if ( b.getEffects().get(k).contains(e) ) {
						sharedEff.add(e);
					}
				}
				effOverlap.put(k, sharedEff);
			}
		}
		return effOverlap;
	}
	
	private static Collection<StateVariableOperatorMultiState> getCandidates( Map<Atomic,List<Term>> s,  StateVariableOperatorMultiState a_i, StateVariableOperatorMultiState a_j, Collection<StateVariableOperatorMultiState> A ) {
		ArrayList<StateVariableOperatorMultiState> candidates = new ArrayList<StateVariableOperatorMultiState>();
		
		for ( StateVariableOperatorMultiState a : SequentialStateFunctions.getApplicable(s, A)) {
			Map<Atomic,List<Term>> effOverlap = YAHSPLookahead.getEffectOverlap( a_i, a );
			if ( !effOverlap.isEmpty() ) {
				StateVariableOperatorMultiState tmpOp = new StateVariableOperatorMultiState();
				tmpOp.getEffects().putAll(effOverlap);
				if ( YAHSPLookahead.contributesToPreconditions(tmpOp, a_j) ) {
					candidates.add(a);
				}
			}
		}
		
		return candidates;
	}
	
	public static ForwardPlanningNode lookahead( ForwardPlanningNode n, Map<Object,Long> cost, Collection<Entry<Atomic,Term>> G, Collection<StateVariableOperatorMultiState> A ) {
		Map<Atomic,List<Term>> s = new HashMap<Atomic, List<Term>>();
		s.putAll(n.s);
		
		LinkedList<StateVariableOperatorMultiState> plan = new LinkedList<StateVariableOperatorMultiState>();
		LinkedList<StateVariableOperatorMultiState> rPlan = YAHSPLookahead.extractRelaxedPlan(s, cost, G, A);
		
		boolean loop = true;

		while ( loop ) {
			loop = false;
			boolean foundApplicable = false;
			for ( int i = 0 ; i < rPlan.size() ; i++ ) {	// Apply what we can:
				StateVariableOperatorMultiState a = rPlan.get(i);
				if ( SequentialStateFunctions.applicable(s,a)  ) {
					foundApplicable = true;
					
					loop = true;
					s = SequentialStateFunctions.apply(s, a);
					
					plan.addLast(a);
					rPlan.remove(i);
					
					break;
				}
			}
			if ( !foundApplicable ) { // Attempt repair
				int i = 0;
				int j = 0;
				while ( !loop && i < rPlan.size() ) {
					while ( !loop && j < rPlan.size() ) {
						if ( i != j && YAHSPLookahead.contributesToPreconditions(rPlan.get(i), rPlan.get(j)) ) {
							Collection<StateVariableOperatorMultiState> candidates = YAHSPLookahead.getCandidates( s, rPlan.get(i), rPlan.get(j), A );
							
							if ( !candidates.isEmpty() ) {
								StateVariableOperatorMultiState a = null;
								long min = Long.MAX_VALUE;
								for ( StateVariableOperatorMultiState can : candidates ) {
									long c = cost.get(can.getName()).longValue();
									if ( c < min ) {
										min = c;
										a = can;
									}
								}
								rPlan.set(i,a);
							}
						}
						
						j += 1;
					}
					i += 1;
				}
			}
		}
		
		if ( plan.isEmpty() ) {
			return null;
		}
		/**
		 * Create a lookahead node and return it:
		 */
		ForwardPlanningNode prev = n;
		for ( StateVariableOperatorMultiState a : plan ) {
			ForwardPlanningNode r = new ForwardPlanningNode(n.getHeuristicValues().length);
			r.s = SequentialStateFunctions.apply(prev.s, a);
			r.a = a;
			r.prev = prev;
			r.g = prev.g.copy();
			r.setForceExploration(prev.forceExploration());
			prev = r;
		}
		
		boolean change = true;
		while ( change ) {
			change = false;
		
//			for ( Goal goal : prev.g ) {
//				if ( !goal.wasReached() && goal.requirementsSatisfied() && goal.reachedInMultiState(prev.s)) {
//					prev.g.setReached(goal, true);
//					change = true;
//				}					
//			}
			for ( Goal goal : prev.g ) {
				if ( goal.isLandmark() && !goal.wasReached() && goal.requirementsSatisfied() && goal.reachedInMultiState(prev.s)) {
					prev.g.setReached(goal, true);
					change = true;
				} else if ( !goal.isLandmark() && goal.requirementsSatisfied() ) {
					boolean prevState = goal.wasReached();
					boolean currState = goal.reachedInMultiState(prev.s);
					prev.g.setReached(goal, currState);
					if ( prevState != currState ) {
						change = true;
					}
				}
			}
			
		}
		return prev;
	}
}
