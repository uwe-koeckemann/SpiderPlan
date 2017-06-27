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

import org.spiderplan.modules.solvers.Module;

/**
 * Thread to check if a timeout is reached during planning.
 * @author Uwe Köckemann
 */
public class TimeOutThread extends Thread {
	
	long startTimeMillis;
	long maxTimeMillis;
	boolean stopped = false;
	
	/**
	 * Create new thread for maximum amount of time
	 * @param maxTimeMillis maximum time in milliseconds
	 */
	public TimeOutThread( long maxTimeMillis ) {
		this.maxTimeMillis = maxTimeMillis;
	}
	
	/**
	 * Set stop flag for this thread.
	 */
	public void stopThread() {
		this.stopped = true;
	}
	
	@Override
	public void run() {
		startTimeMillis = System.currentTimeMillis();
		
		while ( !stopped && (System.currentTimeMillis()-startTimeMillis) < maxTimeMillis ) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if ( !stopped ) {
			Module.setKillFlag(true);
		}
	}
};
