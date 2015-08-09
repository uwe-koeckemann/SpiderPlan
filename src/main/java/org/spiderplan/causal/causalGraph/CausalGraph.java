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
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.StateVariableOperator;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.tools.GraphTools;
import org.spiderplan.tools.visulization.GraphFrame;


//import representation.Operator;
//import representation.statement.Statement;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * Data structure for the causal graph.
 * Contains methods to remove cycles and extracta total order as
 * required by the causal graph heuristic.
 * <p>
 * Relevant papers:
 * <li> Helmert, M. The fast downward planning system Journal of Artificial Intelligence Research, 2006, 26, 191-246 
 * <p>
 * @author Uwe Köckemann
 *
 */
public class CausalGraph {
	
	private DirectedSparseMultigraph<Atomic, String> g;
	private ArrayList<Atomic> totalOrder;
	
	public CausalGraph( Collection<StateVariableOperator> A ) {
		g = new DirectedSparseMultigraph<Atomic, String>();
		long edgeCount = 0;
		
		for ( StateVariableOperator a : A ) {
			for ( Atomic v1 : a.getEffects().keySet() ) {
				for ( Atomic v2 : a.getEffects().keySet() ) {
					if ( !v1.equals(v2) ) {
						g.addEdge(""+(edgeCount++), v1 ,v2);
					}
				}
				for ( Atomic v2 : a.getPreconditions().keySet() ) {
					g.addEdge(""+(edgeCount++), v2, v1);
				}
			}
		}
		this.removeCycles();
	}
	
	public DirectedSparseMultigraph<Atomic, String> getGraph() {
		return g;
	}
	
	protected ArrayList<Atomic> getTotalOrder() {
		return totalOrder;
	}
	
	Map<Atomic,Set<Atomic>> predecessorCache = new HashMap<Atomic,Set<Atomic>>(); 
	Map<Atomic,Set<Atomic>> ancestorCache = new HashMap<Atomic,Set<Atomic>>(); 
	
	/**
	 * Get all predecessors of a variable v
	 * @param v
	 * @return
	 */
	protected Set<Atomic> getPredecessors( Atomic v ) {
		Set<Atomic> p = predecessorCache.get(v);
		
		if ( p != null ) {
			return p;
		}
		p = new HashSet<Atomic>();
		Collection<Atomic> c = g.getPredecessors(v);
		if ( c != null ) {
			p.addAll( c );
		}
		predecessorCache.put(v,p);
		return p;
	}
	
	/**
	 * Get all ancestors of a variable v
	 * @param v
	 * @return
	 */
	protected Set<Atomic> getAncestors( Atomic v ) {
		Set<Atomic> p = ancestorCache.get(v);
		
		if ( p != null ) {
			return p;
		}
		
		ArrayList<Atomic> checkList = new ArrayList<Atomic>();
		HashSet<Atomic> done = new HashSet<Atomic>();
		checkList.add(v);		
		p = new HashSet<Atomic>();
		
		while ( !checkList.isEmpty() ) {
			Atomic nextV = checkList.get(0);
			checkList.remove(0);
			done.add(nextV);
			
			Collection<Atomic> c = g.getPredecessors(nextV);
			
			if ( c != null ) {
				for ( Atomic vNew : c ) {
					if ( !done.contains(vNew) ) {
						checkList.add(vNew);
					}
					p.add(vNew);
				}
				
			}
		}
		
		
		ancestorCache.put(v,p);
		return p;
	}
	
	/**
	 * Remove cycles in causal graph by using total order of variables
	 * to decide which edges to keep. This is what makes the heuristic
	 * computation possible in polynomial time.
	 */
	private void removeCycles() {
		
		GraphTools<Atomic,String> gTools = new GraphTools<Atomic,String>();
		
		ArrayList<Graph<Atomic,String>> scc = gTools.getStronglyConnectedComponents(g);
	
		for ( Graph<Atomic,String> subG : scc ) {
			
			totalOrder = new ArrayList<Atomic>();
			
			this.getTotalOrder(subG, totalOrder);
				
			for ( Atomic v1 : subG.getVertices() ) {
				for ( String e : subG.getOutEdges(v1)) {
					Atomic v2 = subG.getDest(e);
					
					if ( !lowerThan(v1,v2,totalOrder) ) {
						g.removeEdge(e);
					}
				}
			}
		}
		gTools.removeMultiEdges(g);
	}
	
	/**
	 * Compare two variable v1 and v2 according to a total order.
	 * @param v1 First variable	
	 * @param v2 Second variable
	 * @param totalOrder A total order of variables
	 * @return True if v1 is lower in total order than v2
	 */
	private boolean lowerThan( Atomic v1, Atomic v2, ArrayList<Atomic> totalOrder ) {
		return totalOrder.indexOf(v1) < totalOrder.indexOf(v2);
	}
	
	/**
	 * Compute total order of graph based in in-degree of all vertices.
	 * Warning: changes the input graph g in the process.
	 * @param g 
	 * @param order
	 */
	public void getTotalOrder( Graph<Atomic, String> g, ArrayList<Atomic> order ) {
		
		if ( g.getVertexCount() <= 1 ) {
			order.addAll(g.getVertices());
			return;
		}
		
		Atomic argMin = null;
		int min=Integer.MAX_VALUE;
		
		for ( Atomic v : g.getVertices() ) {
			if ( g.getInEdges(v).size() < min ) {
				argMin = v;
				min = g.getInEdges(v).size();
			}
		}
				
		order.add(argMin);
		
		ArrayList<String> remList = new ArrayList<String>();
		for ( String e : g.getIncidentEdges(argMin)) {
			if ( !remList.contains(e)) {
				remList.add(e);
			}
		}
		
		ArrayList<Atomic> remEdgeSource = new ArrayList<Atomic>();
		ArrayList<Atomic> remEdgeDest = new ArrayList<Atomic>();
		
		for ( String e : remList ) {
			remEdgeSource.add(g.getSource(e));
			remEdgeDest.add(g.getDest(e));
			g.removeEdge(e);
		}
		g.removeVertex(argMin);
		
		getTotalOrder(g, order);
		
		g.addVertex(argMin);
		for ( int i = 0 ; i < remList.size() ; i++ ) {
			g.addEdge(remList.get(i), remEdgeSource.get(i), remEdgeDest.get(i));
		}
	}
	
	/**
	 * Draw the causal graph in a {@link GraphFrame} for debugging
	 */
	public void draw() {		
		new GraphFrame<Atomic, String>(this.g, null,  "Causal Graph", GraphFrame.LayoutClass.FR, null);
	}
}
