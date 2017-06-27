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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private ConcurrentLinkedQueue<String> msgQueue = new ConcurrentLinkedQueue<String>();
	private ConcurrentLinkedQueue<String> msgQueueOut = new ConcurrentLinkedQueue<String>();
	
	
	public SocketMessageThreadSend( Socket socket ) {
		this.socket = socket;
		this.setDaemon(true);
	}
	
	public void queueMessageToSend( String message ) {
		msgQueueOut.add(message);
	}
	
	public ConcurrentLinkedQueue<String> getMessages() {
		return msgQueue;
	}
	
    @Override
	public void run() {
    	OutputStreamWriter outStream;
		try {
			outStream = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
			
			while ( true ) {
		        
		        while ( !msgQueueOut.isEmpty() ) {
		        	String nextMessage = msgQueueOut.poll();
					outStream.write(nextMessage.replaceAll("\"", "") + "\n\n");
					outStream.flush();		
		        }
		        
		        Thread.sleep(1000);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			
			System.exit(1);
		} finally {

		}
    }
}
