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
package org.spiderplan;

import java.util.ArrayList;

import org.spiderplan.tools.GraphTools;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestGraphTools extends TestCase {

	@Override
	public void setUp() throws Exception {
		
	}

	@Override
	public void tearDown() throws Exception {
	
	}
	
	public void testReachable() {	
		DirectedSparseMultigraph<String, String> g = new DirectedSparseMultigraph<String, String>();
		
		g.addEdge("e1", "A", "B");
		g.addEdge("e2", "B", "A");
		g.addEdge("e3", "B", "C");
		g.addEdge("e4", "C", "D");
		g.addEdge("e5", "D", "C");
		 
		GraphTools<String,String> gt = new GraphTools<String, String>();
		
		assertTrue( gt.reachableFrom(g, "A") == 4 ); 		
		assertTrue( gt.reachableFrom(g, "B") == 4 );
		assertTrue( gt.reachableFrom(g, "C") == 2 );
		assertTrue( gt.reachableFrom(g, "D") == 2 );
	}
	
	public void testStronglyConnectedComponents() {	
		DirectedSparseMultigraph<String, String> g = new DirectedSparseMultigraph<String, String>();
		
		g.addEdge("e1", "A", "B");
		
		g.addEdge("e2", "B", "C");
		g.addEdge("e3", "B", "E");
		g.addEdge("e4", "B", "F");
		
		g.addEdge("e5", "C", "D");
		g.addEdge("e6", "C", "G");
		
		g.addEdge("e7", "D", "C");
		g.addEdge("e8", "D", "H");
		
		g.addEdge("e9", "E", "A");
		g.addEdge("e10", "E", "F");
		
		
		g.addEdge("e11", "F", "G");
		
		g.addEdge("e12", "G", "F");
		g.addEdge("e13", "G", "H");
		
		g.addEdge("e14", "H", "H");
		
		 
		GraphTools<String,String> gt = new GraphTools<String, String>();
		
		ArrayList<Graph<String,String>> scc = gt.getStronglyConnectedComponents(g);
		
		assertTrue( scc.get(0).containsVertex("A"));
		assertTrue( scc.get(0).containsVertex("B"));
		assertTrue( scc.get(0).containsVertex("E"));
		
		assertTrue( scc.get(1).containsVertex("C"));
		assertTrue( scc.get(1).containsVertex("D"));
		
		assertTrue( scc.get(2).containsVertex("F"));
		assertTrue( scc.get(2).containsVertex("G"));
		
		assertTrue( scc.get(3).containsVertex("H"));		
	}
}
