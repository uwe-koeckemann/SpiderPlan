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
package org.spiderplan.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spiderplan.representation.graph.DirectedGraph;
import org.spiderplan.representation.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

/**
 * @author Uwe Köckemann
 *
 * @param <V> Class of nodes
 * @param <E> Class of edges
 */
public class GraphTools<V,E> {
	
	/**
	 * Get the graph of strongly connected components.
	 * @param g input graph
	 * @return strongly connected components 
	 */
	public ArrayList<Graph<V,E>> getStronglyConnectedComponents( Graph<V,E> g ) {
		ArrayList<V> vList = new ArrayList<V>();
		vList.addAll(g.getVertices());
		int[] finishTimes = new int[vList.size()];
		
		/*
		 * DFS on G
		 */
		ArrayList<SortableTuple<V>> sortList = new ArrayList<SortableTuple<V>>(vList.size());
		for ( int i = 0 ; i < finishTimes.length ; i++ ) {
			SortableTuple<V> t = new SortableTuple<V>();
			t.object = vList.get(i);
			t.value = new Integer(this.reachableFrom(g, vList.get(i)));
			
			sortList.add(t);
		}
		
		Collections.sort(sortList);
		Collections.reverse(sortList);
		
		ArrayList<V> procList = new ArrayList<V>(vList.size());
		for ( SortableTuple<V> st : sortList ) {
			procList.add(st.object);
		}
		/*
		 * Reverse G
		 */
		Graph<V, E> gr = this.reverseDirGraph(g);
		
		/*
		 * Inverse reachable vertices -> strongly connected components.
		 */
		HashSet<V> done = new HashSet<V>();
		ArrayList<Graph<V,E>> r = new ArrayList<Graph<V,E>>();
		while ( !procList.isEmpty() ) {
			V v = procList.get(0);
			procList.remove(0);
			Set<V> reach = this.reachableSet(gr, v);
			
			DirectedSparseMultigraph<V, E> gComp = new DirectedSparseMultigraph<V, E>();
			
			for ( V reached : reach ) {
				if ( !done.contains(reached) ) {
					for ( E e : g.getOutEdges(reached)) {
						if ( reach.contains(g.getDest(e)) ) {
							gComp.addEdge(e, reached, g.getDest(e));
						}
					}
				}
			}
			done.addAll(reach);
			procList.removeAll(reach);
			if ( gComp.getVertexCount() > 0 ) {
				r.add(gComp);
			}
		}
		
		return r;
	}
	
	/**
	 * Reverse all edges in a directed graph.
	 * @param g input graph
	 * @return inverse graph
	 */
	public Graph<V,E> reverseDirGraph( Graph<V,E> g ) {
		DirectedSparseMultigraph<V, E> gr = new DirectedSparseMultigraph<V, E>();
		for ( E e : g.getEdges() ) {
			gr.addEdge(e, g.getDest(e), g.getSource(e));
		}
		return gr;
	}
	
	/**
	 * Remove duplicate edges from input.
	 * @param g input graph
	 */
	public void removeMultiEdges( Graph<V,E> g ) {
		HashSet<E> remList = new HashSet<E>();
		
		for ( V v : g.getVertices() ) {
			HashSet<V> destSet = new HashSet<V>();
			
			for ( E e : g.getOutEdges(v) ) {
				if ( destSet.contains(g.getDest(e)) ) {
					remList.add(e);
				} else {
					destSet.add(g.getDest(e));
				}
			}
		}
		
		for ( E e : remList ) {
			g.removeEdge(e);
		}
	}
	
	/**
	 * Compute the set of reachable vertices given an initial vertex.
	 * @param g input graph
	 * @param vInit initial vertex
	 * @return set of reachable vertices
	 */
	public Set<V> reachableSet( Graph<V,E> g, V vInit ) {
		ArrayList<V> fringe = new ArrayList<V>();
		HashSet<V> reached = new HashSet<V>();
		
		fringe.add(vInit);
		while ( !fringe.isEmpty() ) {
			V v = fringe.get(0);
			fringe.remove(0);
			
			reached.add(v);
			
			for ( E e: g.getOutEdges(v) ) {
				V v2 = g.getDest(e);
				if ( !reached.contains(v2) && !fringe.contains(v2)) {
					fringe.add(v2);
				}
			}
		}
		return reached;
	}
	
	/**
	 * Count the number of reachable vertices given an initial vertex.
	 * @param g input graph
	 * @param vInit initial vertex
	 * @return number of reachable vertices
	 */
	public int reachableFrom( Graph<V,E> g, V vInit ) {
		ArrayList<V> fringe = new ArrayList<V>();
		HashSet<V> reached = new HashSet<V>();
		
		fringe.add(vInit);
		while ( !fringe.isEmpty() ) {
			V v = fringe.get(0);
			fringe.remove(0);
			
			reached.add(v);
			
			for ( E e: g.getOutEdges(v) ) {
				V v2 = g.getDest(e);
				if ( !reached.contains(v2) && !fringe.contains(v2)) {
					fringe.add(v2);
				}
			}
		}
		return reached.size();
	}

	/**
	 * Copy method for a sparse multi graph
	 * @param in
	 * @return the copy
	 */
	public UndirectedSparseMultigraph<V,E> copyUndirSparseMultiGraph( Graph<V,E> in ) {
		UndirectedGraph<V,E> c = new UndirectedGraph<V,E>();
		
		for ( V v : in.getVertices() ) {
			c.addVertex(v);
		}
		for ( E e : in.getEdges() ) {
			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
		}
		return c;
	}
//	public UndirectedSparseGraph<V,E> copyUndirSparseGraph( Graph<V,E> in ) {
//		UndirectedSparseGraph<V,E> c = new UndirectedSparseGraph<V,E>();
//		
//		for ( V v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( E e : in.getEdges() ) {
//			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	/**
	 * Copy method for a directed sparse multi graph
	 * @param in
	 * @return the copy
	 */
	public DirectedGraph<V,E> copyDirSparseMultiGraph( Graph<V,E> in ) {
		DirectedGraph<V,E> c = new DirectedGraph<V,E>();
		
		for ( V v : in.getVertices() ) {
			c.addVertex(v);
		}
		for ( E e : in.getEdges() ) {
			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
		}
		return c;
	}
//	public static DirectedGraph<Atomic,Atomic> copyDirSparseMultiGraphStrings( Graph<Atomic,Atomic> in ) {
//		DirectedGraph<Atomic,Atomic> c = new DirectedGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	
//	public static DirectedGraph<Atomic,Atomic> getNewDirSparseMultiGraphStrings( Graph<Atomic,Atomic> in ) {
//		DirectedGraph<Atomic,Atomic> c = new DirectedGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	
//	public static UndirectedGraph<Atomic,Atomic> copyUndirSparseMultiGraphStrings( Graph<Atomic,Atomic> in ) {
//		UndirectedGraph<Atomic,Atomic> c = new UndirectedGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	
//	public static UndirectedGraph<Atomic,Atomic> getNewUndirSparseMultiGraphStrings( Graph<Atomic,Atomic> in ) {
//		UndirectedGraph<Atomic,Atomic> c = new UndirectedGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e, in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	
//	public static UndirectedSparseGraph<Atomic,Atomic> copyUndirSparseGraphStrings( Graph<Atomic,Atomic> in ) {
//		UndirectedSparseGraph<Atomic,Atomic> c = new UndirectedSparseGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e,in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
	
//	public static UndirectedSparseGraph<Atomic,Atomic> getNewUndirSparseGraphStrings( Graph<Atomic,Atomic> in ) {
//		UndirectedSparseGraph<Atomic,Atomic> c = new UndirectedSparseGraph<Atomic,Atomic>();
//		
//		for ( Atomic v : in.getVertices() ) {
//			c.addVertex(v);
//		}
//		for ( Atomic e : in.getEdges() ) {
//			c.addEdge(e,in.getEndpoints(e).getFirst(), in.getEndpoints(e).getSecond());
//		}
//		return c;
//	}
}
