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
/**
 * Representing a list of tracks
 * 
 * @author Uwe Köckemann
 */
package org.spiderplan.tools.visulization.timeLineViewer;

import java.util.ArrayList;

public class State {
	ArrayList<Track> tracks = new ArrayList<Track>();
	
	/**
	 * Update start and end time of a {@link Value} with id 
	 * @param id ID of value to be updated
	 * @param start New start time
	 * @param end New end time
	 */
	public void update( String id, int start, int end ) {
		for ( Track t : tracks ) {
			t.update(id, start,end);
		}
	}
	
	public State copy() {
		State s = new State();
		for ( Track t : tracks ) {
			s.tracks.add(t.copy());
		}  
		return s;
	}
}
