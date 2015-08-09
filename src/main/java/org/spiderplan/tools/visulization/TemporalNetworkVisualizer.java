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
package org.spiderplan.tools.visulization;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.AllenConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.tools.GraphTools;


import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * Draws a {@link GraphFrame} of the temporal network, which is composed
 * of the set of {@link Statement}s and {@link AllenConstraint}s
 * in a {@link ConstraintDatabase}. Using the takeSnapshot() method
 * allows to visualize a history of temporal networks.
 * 
 * @author Uwe Koeckemann
 *
 */
public class TemporalNetworkVisualizer {
	
	private AbstractTypedGraph<String,String> g = null;
	private Vector<AbstractTypedGraph<String, String>> history = null;
	private Map<String,String> edgeLabels;
	
	/**
	 * Draw a GraphFrame of the Statements (Nodes) and TemporalConstraints (Edges) in this TemporalDatabase.
	 */
	public void draw( ConstraintDatabase cdb ) {
		ConstraintDatabase cdbCopy = cdb.copy();
		for ( OpenGoal og : cdbCopy.getConstraints().get(OpenGoal.class) ) {
			cdbCopy.add(og.getStatement());
		}
		this.takeSnapshot(cdbCopy);
		new GraphFrame<String,String>(g, history,  "Temporal Database", GraphFrame.LayoutClass.FR, edgeLabels);
	}
	/**
	 * Draw a GraphFrame of the Statements (Nodes) and TemporalConstraints (Edges) in this TemporalDatabase.
	 * @param title Title of the frame.
	 */
	public void draw( ConstraintDatabase cdb, String title ) {
		this.takeSnapshot(cdb);
		new GraphFrame<String,String>(g, history,  title, GraphFrame.LayoutClass.FR, edgeLabels);
	}
	
	/**
	 * Saves a copy of this TemporalDatabase to history. History can be accessed e.g.
	 * after using draw() to visualize this TemporalDatabase
	 */
	public void takeSnapshot( ConstraintDatabase cdb ) {
		if ( history == null ) {
			history = new Vector<AbstractTypedGraph<String,String>>();
		}
		
		GraphTools<String,String> copyGraph = new GraphTools<String,String>();
		
		g = new DirectedSparseMultigraph<String,String>();
		edgeLabels = new HashMap<String,String>(); 
				
				
		for ( Constraint c : cdb.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;			
				String rStr = tC.getRelation().toString();
				
				for ( int i = 0 ; i < tC.getNumBounds() ; i++) {
					rStr += " " + tC.getBound(i).toString();
				}
				
				String e =  "";
				String v1 = "";
				String v2 = "";
				
				if ( tC.isBinary() ) {
					v1 = tC.getFrom().toString();
					v2 = tC.getTo().toString();
				
					v1 = cdb.getStatement(tC.getFrom()).toString();
					v2 = cdb.getStatement(tC.getTo()).toString();
					e = tC.getRelation() + "(" + v1 +"," +v2+ ")";
					edgeLabels.put(e, rStr);
					g.addEdge(e,v1 ,v2 );
	
								
				} else {
					v1 = tC.getFrom().toString();
					v2 = tC.getFrom().toString();
					v1 = cdb.getStatement(tC.getFrom()).toString();
					v2 = v1;
					e = tC.getRelation() + "(" +v1+"," +v2+ ")";
					edgeLabels.put(e, rStr);
					g.addEdge(e,v1 ,v2 );
				}
			}
		}
		history.add(copyGraph.copyDirSparseMultiGraph(g));
	}
}
