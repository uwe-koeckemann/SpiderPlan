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
package org.spiderplan.causal.forwardPlanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Class computing and collecting data structures that are
 * commonly used by several heuristic functions.
 * <p>
 * Every structure that is requested will be computed if it 
 * does not exist and provided in case it does.
 * <p>
 * These data structures (will) include: 
 * <ul>
 * <li>Planning Graph</li>
 * <li>Domain Transition Graphs</li>
 * <li>Causal Graph</li>
 * <li>Set of applicable actions</li>
 * </ul>
 * 
 * @author Uwe Köckemann
 *
 */
public class CommonDataStructures {
	
	private Map<Map<Atomic,Term>,Map<Object,Long>> hadd_cost = new HashMap<Map<Atomic,Term>,Map<Object,Long>>();
	private Map<Map<Atomic,Term>,Set<StateVariableOperator>> app = new HashMap<Map<Atomic,Term>,Set<StateVariableOperator>>();
	
	private Collection<StateVariableOperator> O;
	
	/**
	 * Assign operators used by the data structures.
	 * 
	 * @param O the operators
	 */
	public void setOperators( Collection<StateVariableOperator> O ) {
		this.O = O;	
	}
		
	/**
	 * Given a state compute the costs of applying actions to this state
	 * and reaching preconditions.
	 * 
	 * @param s state
	 * @return lookup for cost to apply actions and reach preconditions (keys are operator names and state-variables)
	 */
	public Map<Object,Long> getActionCosts( Map<Atomic,Term> s ) {
		Map<Object,Long> cost = this.hadd_cost.get(s);
		if ( cost != null ) {
			return cost;
		}
		
		
		LinkedList<StateVariableOperator> update = new LinkedList<StateVariableOperator>();
		
//		this.A.addAll(s.entrySet());
		
		Set<StateVariableOperator> app = new HashSet<StateVariableOperator>();
		Map<Object,List<StateVariableOperator>> effToPreMap = new HashMap<Object, List<StateVariableOperator>>();
		
		cost = new HashMap<Object, Long>();
		
		for ( StateVariableOperator a : O ) {
			cost.put(a.getName(), Long.valueOf(Long.MAX_VALUE));
			if ( a.getPreconditions().isEmpty() ) {
				update.add(a);
			}
			for ( Entry<Atomic,Term> pre : a.getPreconditions().entrySet() ) {
				List<StateVariableOperator> list = effToPreMap.get(pre);
				if ( list == null ) {
					list = new ArrayList<StateVariableOperator>();
					effToPreMap.put(pre, list);
				}
				list.add(a);
			}
		}
		
		for ( Entry<Atomic,Term> p : s.entrySet() ) {
			cost.put(p, Long.valueOf(0));
			List<StateVariableOperator> list = effToPreMap.get(p);
			if ( list != null ) {
				update.addAll(list);
			}
		}
			
		Long lookUpcost;
		StateVariableOperator a;
		while ( !update.isEmpty() ) {
			a = update.poll();
		
			long c = 0;
			for ( Entry<Atomic,Term> pre : a.getPreconditions().entrySet() ) {
				lookUpcost = cost.get(pre);
				if ( lookUpcost == null ) {
					c = Long.MAX_VALUE;
					break;
				}
				c = smartAddition(c, lookUpcost);
				
				if ( c == Long.MAX_VALUE ) break;
			}
			
			lookUpcost = cost.get(a.getName());
			if ( lookUpcost != null && c < cost.get(a.getName() ) ) {
				cost.put(a.getName(), c);
				if ( c == 0 ) {
					app.add(a);
				}
				for ( Entry<Atomic,Term> eff : a.getEffects().entrySet() ) {
					lookUpcost = cost.get(eff);
					if ( lookUpcost == null || c + 1 < lookUpcost ) {
						cost.put(eff, c + 1 );
						List<StateVariableOperator> list = effToPreMap.get(eff);
						if ( list != null ) {
							for ( StateVariableOperator a_prime : list ) {
								update.add(a_prime);
							}
						}
					}
				}
				
			}
		}
		
		this.hadd_cost.put(s, cost);
		this.app.put(s, app);
		return cost;
	}

	
	/**
	 * Get cost map of a sequential state by taking the minimal costs of all combinations
	 * of regular states.
	 * @param sSeq A sequential state.
	 * @return A map from {@link Object}s ({@link Atomic} action names or Entry<Atomic,Term> state value pairs) to {@link Long} costs
	 */
	public Map<Object,Long> getActionCostsOfSequentialState( Map<Atomic,List<Term>> sSeq ) {
		Map<Object,Long> cost = new HashMap<Object, Long>();
		
		for ( Map<Atomic,Term> s : SequentialStateFunctions.getAllStateCombos(sSeq) ) {
			StopWatch.start("Action Costs");
			Map<Object,Long> s_cost = this.getActionCosts(s);
			
			for ( Object key : s_cost.keySet() ) {
				Long c = s_cost.get(key);
				
				Long c_stored = cost.get(key);
				
				if ( c_stored == null || c.longValue() < c_stored.longValue() ) {
					cost.put(key, c);
				}
			}
		}
		return cost;
	}
	
	private long smartAddition( long a, long b ) {
		if ( a == Long.MAX_VALUE || b == Long.MAX_VALUE ) {
			return Long.MAX_VALUE;
		}
		return a+b;
	}

}
