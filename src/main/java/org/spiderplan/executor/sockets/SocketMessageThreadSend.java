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
package org.spiderplan.executor.sockets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread that sends messages in a queue to a socket. 
 * 
 * @author Uwe Köckemann
 */
public class SocketMessageThreadSend extends Thread {

	private Socket socket;
	private ConcurrentLinkedQueue<String> msgQueueOut = new ConcurrentLinkedQueue<String>();
	private boolean verbose = false;
	
	/**
	 * Create a thread to send messages through a socket
	 * @param socket socket to use
	 * @param verbose switches console output on/off
	 */
	public SocketMessageThreadSend( Socket socket, boolean verbose ) {
		this.socket = socket;
		this.setDaemon(true);
		this.verbose = verbose;
	}
	
	/**
	 * Queue a message to be sent through the socket.
	 * @param message the message to send
	 */
	public void queueMessageToSend( String message ) {
		msgQueueOut.add(message);
	}
	
    @Override
	public void run() {
    	OutputStreamWriter outStream;
		try {
			outStream = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
			
			while ( true ) {
		        
		        while ( !msgQueueOut.isEmpty() ) {
		        	String nextMessage = msgQueueOut.poll();
					outStream.write(nextMessage + "\n");
					outStream.flush();		
		            if ( verbose ) {
		            	System.out.println("-------------------------------------------------------------------");
		            	System.out.println("- OUTGOING MESSAGE");
		            	System.out.println("-------------------------------------------------------------------");
		            	System.out.println(nextMessage);
		            }
		        }
		        
		        Thread.sleep(100);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			
			System.exit(1);
		} finally {

		}
    }
}
