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
