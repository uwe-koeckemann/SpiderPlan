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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread that reads messages from a socket and stores them in a queue to be processed later. 
 * 
 * @author Uwe Köckemann
 */
public class SocketMessageThreadReceiver extends Thread {

	private Socket socket;
	private ConcurrentLinkedQueue<String> msgQueue = new ConcurrentLinkedQueue<String>();
	private boolean verbose = false;
	
	/**
	 * Create a new thread to receive messages from a socket
	 * @param socket socket to listen to
	 * @param verbose switches console output on/off
	 */
	public SocketMessageThreadReceiver( Socket socket, boolean verbose ) {
		this.socket = socket;
		this.setDaemon(true);
		this.verbose = verbose;
	}
		
	/**
	 * Create a new thread to receive messages from a socket
	 * @param socket socket to listen to
	 */
	public SocketMessageThreadReceiver( Socket socket ) {
		this(socket, false);
	}
	
	/**
	 * Get all messages received on the socket
	 * @return the messages
	 */
	public ConcurrentLinkedQueue<String> getMessages() {
		return msgQueue;
	}
	
    @Override
	public void run() {
    	BufferedReader in;
		try {
			while ( true ) {
		        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		       
		        String line = in.readLine();
		        if ( line != null ){
		            msgQueue.add(line);
		            if ( verbose ) {
		            	System.out.println("-------------------------------------------------------------------");
		            	System.out.println("- INCOMING MESSAGE");
		            	System.out.println("-------------------------------------------------------------------");
		            	System.out.println(line);
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
