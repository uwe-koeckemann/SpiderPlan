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
package org.spiderplan.executor.ROS;

import org.spiderplan.representation.logic.Term;

/**
 * Thread that ready message from a ROS topic using ROSProxy.
 * 
 * @author Uwe Köckemann
 */
class ROSMessageThread extends Thread {

	public String topicName;
	public int numReqRepeats;
	
	public Term outputMsg;
	public Term lastReceivedMsg;
	public int currentRepeats = 0;
	
	public ROSMessageThread( String topicName, int numReqRepeats ) {
		this.topicName = topicName;
		this.numReqRepeats = numReqRepeats;
	}
	
    @Override
	public void run() {
    	while ( true ) {
	    	Term currentMsg = ROSProxy.read_msg(this.topicName);
	    	
	    	if ( currentMsg != null ) {   		
	    		if ( !currentMsg.equals(lastReceivedMsg)) {
	    			lastReceivedMsg = currentMsg;
	    			currentRepeats = 1;
				} else {
					currentRepeats++;
				}	
	    		if ( currentRepeats >= numReqRepeats ) {
	        		outputMsg = currentMsg;
	        	}
	    	}	
	    	
	    	try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
}
