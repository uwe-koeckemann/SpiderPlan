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
package org.spiderplan.representation.graph;

import java.util.ArrayList;
import java.util.Collection;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class DirectedGraph<V, E> extends DirectedSparseMultigraph<V, E> {
	private static final long serialVersionUID = 2L;

	@Override
	public boolean isNeighbor( V v1, V v2 ) {
		return containsVertex(v1) && containsVertex(v2) && isNeighbor(v1, v2);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<V> getNeighbors( V v ) {
		Collection<V> c = super.getNeighbors(v);
		if ( c != null ) {
			return c; 
		} else {
			return (ArrayList<V>)(new ArrayList<Object>());
		}
	}
	
	@Override
	public String toString() {
		String r = "";
		
		for ( V v : this.getVertices() ) {
			r += v + " -> " + this.getNeighbors(v);
		}
		
		return r;
	}
}
