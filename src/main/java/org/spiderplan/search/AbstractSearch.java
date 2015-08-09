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
package org.spiderplan.search;

/**
 * Abstract super class for search algorithms.
 * Contains some common data and methods.
 * @author Uwe Köckemann
 *
 */
public abstract class AbstractSearch {

	public String name = "SomeSearch";
	
	public Node n;
	public Node solution;
	
	public boolean done = false;
	public boolean success = false;	
	
	public boolean verbose = false;
	public int verbosity = 0;
	public boolean keepTimes = false;
	
	public int depth = 0;
		
	/**
	 * Search failed if it is done and was not successful. 
	 * @return
	 */
	public boolean failed() {
		return done && ! success;
	}
	
	/**
	 * Return current {@link Node}
	 * @return {@link Node} that was last expanded.
	 */
	public Node getCurrentNode() {
		return n;
	}
	
	/**
	 * Get the name of this search
	 * @return A {@link String} name
	 */
	public String getName() { return name; }
	
}
