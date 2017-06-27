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
package org.spiderplan.search;

import java.util.logging.Logger;

import org.spiderplan.tools.stopWatch.StopWatch;

/**
 * Abstract super class for search algorithms.
 * Contains some common data and methods.
 * @author Uwe Köckemann
 *
 */
public abstract class AbstractSearch {

	protected String name = "UnnamedSearch";
	
	protected AbstractNode n;
	protected AbstractNode solution;
	
	/**
	 * <code>true</code> if the search is finished (successfully or unsuccessfully)
	 */
	protected boolean done = false;
	/**
	 * <code>true</code> if the search was successful, <code>false</code> if it was unsuccessful or 
	 * it is not done yet  
	 */
	protected boolean success = false;	
	
	protected boolean verbose = false;
	protected int verbosity = 0;
	protected boolean keepTimes = false;
	
	/**
	 * Search failed if it is done and was not successful. 
	 * @return <code>true</code> iff the search has failed, <code>false</code> otherwise.
	 */
	public boolean failed() {
		return done && ! success;
	}
	
	/**
	 * Return currently selected node.
	 * @return node currently selected by search
	 */
	public AbstractNode getCurrentNode() {
		return n;
	}
	
	/**
	 * Return goal node.
	 * @return node currently selected by search
	 */
	public AbstractNode getGoalNode() {
		return solution;
	}
	
	/**
	 * Continue search after a solution was found.
	 */
	public void continueSearch() { this.done = false; this.success = false;	}
	/**
	 * Check if the search is done.
	 * @return <code>true</code> if the search is done, <code>false</code> otherwise
	 */
	public boolean isDone() { return done; }
	/**
	 * Check if the search was successful.
	 * @return <code>true</code> if the search was successful, <code>false</code> otherwise
	 */
	public boolean isSuccess() { return success; }
	
	/**
	 * Get the name of this search
	 * @return the name
	 */
	public String getName() { return name; }
	
	/**
	 * Set name of this search (used, e.g.,  by {@link Logger})
	 * @param name the name
	 */
	public void setName( String name ) { this.name = name; }
	
	/**
	 * Enable output and set verbosity level. Verbosity level ranges from 0 (few messages) to 5 (all messages).
	 * @param verbose <code>true</code> to enable {@link Logger} messages by this search, <code>false</code> to disable.
	 * @param verbosity verbosity level
	 */
	public void setVerbose( boolean verbose, int verbosity ) {
		this.verbose = verbose;
		this.verbosity = verbosity;
	}
	
	
	/**
	 * Enables recording of times with {@link StopWatch}.
	 * @param keepTimes <code>true</code> to enable recording times, <code>false</code> to disable
	 */
	public void setKeepTimes( boolean keepTimes ) {
		this.keepTimes = keepTimes;
	}
	
}
