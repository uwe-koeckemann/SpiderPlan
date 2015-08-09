package org.spiderplan.executor;

import org.spiderplan.representation.logic.Term;

public class ROSMessageThread extends Thread {

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
