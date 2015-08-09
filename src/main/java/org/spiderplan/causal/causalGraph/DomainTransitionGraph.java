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
package org.spiderplan.causal.causalGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.spiderplan.causal.StateVariableOperator;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.visulization.GraphFrame;


import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Data structure describing possible transitions of a single state-variable.
 * <p>
 * Relevant papers:
 * <li> Helmert, M. The fast downward planning system Journal of Artificial Intelligence Research, 2006, 26, 191-246 
 * <p>
 * @author Uwe Köckemann
 *
 */
public class DomainTransitionGraph {
	
	private Atomic v;
	private DirectedSparseMultigraph<Term, DomainTransitionEdge> g;
	private final static Term UnknownValue = Term.createConstant("unknown_value");
	
	
	public Atomic getVariable() {
		return v;
	}
	
	public DirectedSparseMultigraph<Term, DomainTransitionEdge> getGraph() {
		return g;
	}
	
	public DomainTransitionGraph( Atomic v, Collection<StateVariableOperator> O, TypeManager tM ) {
		this.v = v;
		
		g = new DirectedSparseMultigraph<Term, DomainTransitionEdge>();
		long edgeCount = 0;
		
		for ( StateVariableOperator o : O ) {
			Term newValue = o.getEffects().get(v);
			Term oldValue = o.getPreconditions().get(v);
			
			if ( newValue != null ) {
				if ( oldValue != null ) {
					DomainTransitionEdge edge = new DomainTransitionEdge();
					
					edge.ID = edgeCount++;
					edge.addOperator(o);
					
					for ( Atomic var : o.getPreconditions().keySet() ) {
						if ( !var.equals(v)) {
							edge.getConditions().put(var, o.getPreconditions().get(var));
						}
					}
					if ( !oldValue.equals(newValue)) {
						g.addEdge(edge, oldValue, newValue);
						edge.setSource(oldValue); 
						edge.setDest(newValue);
					}
					
				} else {
					for ( Term value : tM.getPredicateTypes(v.getUniqueName(), -1).getDomain() ) {
						if ( !value.equals(newValue) ) {
							DomainTransitionEdge edge = new DomainTransitionEdge();
							edge.getConditions().putAll(o.getPreconditions());
							edge.ID = edgeCount++;
							oldValue = value;
							g.addEdge(edge, oldValue, newValue);
							edge.setSource(oldValue); 
							edge.setDest(newValue);
						}
					}
					
					/**
					 * Add an edge from "unknown_value" which is used
					 * in cases where the state is not fully determined.
					 */
					DomainTransitionEdge edge = new DomainTransitionEdge();
					edge.getConditions().putAll(o.getPreconditions());
					edge.ID = edgeCount++;
					oldValue = UnknownValue;
					g.addEdge(edge, oldValue, newValue);
					edge.setSource(oldValue); 
					edge.setDest(newValue);
				}
				
			}
		}
	}
	
//	private HashMap<DomainTransitionEdge,Term> destCache = new HashMap<DomainTransitionEdge, Term>();
	
//	public Term getDest( DomainTransitionEdge t ) {
////		Term r = destCache.get(t);
////		if ( r != null ) {
////			return r;
////		}
////		
////		r = g.getDest(t);
////		destCache.put(t, r);
////		return r;
//		return destCache.get(t);
//	}
	
	public void prune( ArrayList<Atomic> totalOrder ) {
		
//		HashSet<Atomic> remListSV;
		
		for ( DomainTransitionEdge e : this.g.getEdges() ) {
//			remListSV = new HashSet<Atomic>();
			
			for ( Atomic v_prime : e.getConditions().keySet() ) {
				if ( lowerThan( this.v, v_prime, totalOrder ) ) {
					e.getConditions().remove(v_prime);
//					remListSV.add(v_prime);
				}
			}
//			e.getConditions().removeAll(remListSV);
		}
		
		HashSet<DomainTransitionEdge> remList = new HashSet<DomainTransitionEdge>();
				
		for ( Term v1 : g.getVertices()) {
			for ( Term v2 : g.getVertices()) {
				ArrayList<DomainTransitionEdge> edges = new ArrayList<DomainTransitionEdge>();
				
				for ( DomainTransitionEdge e : g.getOutEdges(v1) ) {
					if ( g.getDest(e).equals(v2)) {
						edges.add(e);
					}
				}
				
				
				for ( DomainTransitionEdge e1 : edges ) {
					for ( DomainTransitionEdge e2 : edges ) {
						if ( !e1.equals(e2) ) {
							if ( e1.getConditions().equals(e2.getConditions()) ) {
								e1.associatedOperators.addAll(e2.associatedOperators);
								remList.add(e2);
							} else if ( e1.getConditions().entrySet().containsAll(e2.getConditions().entrySet()) ) {
								remList.add(e2);
							}						
						}
					}
				}
			}
		}  
		
		for ( DomainTransitionEdge e : remList ) {
			g.removeEdge(e);
		}
	}
	
	public void draw() {	
		HashMap<DomainTransitionEdge,String> edgeLabels = new HashMap<DomainTransitionEdge, String>();
		for ( DomainTransitionEdge e : this.g.getEdges() ) {
			edgeLabels.put(e, e.toString());
		}
		
		new GraphFrame<Term, DomainTransitionEdge>(this.g, null,  "Domain Transion Graph ("+this.v+")", GraphFrame.LayoutClass.FR, edgeLabels);
	}
	
	public boolean lowerThan( Atomic v1, Atomic v2, ArrayList<Atomic> totalOrder ) {
		return totalOrder.indexOf(v1) < totalOrder.indexOf(v2);
	}
	
}
