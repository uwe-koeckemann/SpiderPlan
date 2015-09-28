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
package org.spiderplan.executor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Loop;

public class ROSProxy {
	
	private final static int SUBSCRIBE_TO = 0;
	private final static int PUBLISH_TO = 1;
	private final static int READ_MSG = 2;
	private final static int SEND_MSG = 3;
	private final static int SERVICE_CALL = 4;
	private final static int REGISTER_ACTION = 5;
	private final static int SEND_GOAL = 6;
	private final static int HAS_STARTED = 7;
	private final static int HAS_FINISHED = 8;
	
	private static String splitStr = "<//>";
	
	private static Map<String,ROSMessageThread> subsriberThread = new HashMap<String, ROSMessageThread>();
	

	
	public static boolean subscribeToTopic( String topicName, String msgType, String varName ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer.writeBytes(SUBSCRIBE_TO + splitStr + topicName + splitStr + msgType + splitStr + varName + "\n");
			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			if ( answer.equals("<OK>") ) {
				ROSMessageThread thread = new ROSMessageThread(topicName, 2);
				thread.start();
				subsriberThread.put(topicName, thread);
				return true;
			} 
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
	
	public static boolean publishToTopic( String topicName, String msgType ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer.writeBytes(PUBLISH_TO + splitStr + topicName+splitStr+msgType+"\n");
			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return answer.equals("<OK>");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
	
	public static Term get_last_msg( String topicName ) {
		return subsriberThread.get(topicName).outputMsg;
	}
	
	public static Term read_msg( String topicName ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	
			outToServer.writeBytes(READ_MSG + splitStr + topicName+"\n");

			String answer = "";			
			answer = inFromServer.readLine();

			clientSocket.close(); 
			
			if ( answer.equals("<NONE>") ) {
				return null;
			}			
			return Term.parse(answer);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return null;
	}
	
	public static boolean send_msg( String topicName, Term msg ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(SEND_MSG + splitStr + topicName + splitStr +msg+ "\n");

			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return answer.equals("<OK>");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
	
	public static Term srv_call( Term request ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(SERVICE_CALL + splitStr + request + "\n");

			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return Term.parse(answer);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return null;
	}
	
	public static boolean register_action( Term serverID, Term actionName ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(REGISTER_ACTION + splitStr + serverID + splitStr + actionName + "\n");

			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return answer.equals("<OK>");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
	
	public static int send_goal( Term serverID, Term actionName, Term goalMsg ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(SEND_GOAL + splitStr + serverID + splitStr + actionName + splitStr + goalMsg + "\n");

			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return Integer.valueOf(answer).intValue();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return -1;
	}
	
	public static boolean has_started( int requestID ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(HAS_STARTED + splitStr + requestID + "\n");

			String answer = "";			
			answer = inFromServer.readLine();
			clientSocket.close(); 
			return Boolean.valueOf(answer).booleanValue();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
	
	
	public static boolean has_finished( int requestID, Term resultMsg ) {
		try {
			Socket clientSocket;
			clientSocket = new Socket("localhost", 6790);
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			outToServer.writeBytes(HAS_FINISHED + splitStr + requestID + "\n");

			boolean r;
			
			String answer = "";			
			answer = inFromServer.readLine();
			
			
			
			if ( answer.equals("false") ) {
				r = false;
			} else {
				if ( resultMsg != null ) {
					Term result = Term.parse(answer);			
					Substitution subst = resultMsg.match(result);
					resultMsg = resultMsg.substitute(subst);
				}
				r = true;
			}
			
			clientSocket.close(); 
			return r;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			Loop.start();
		} catch (IOException e) {
			e.printStackTrace();
			Loop.start();
		}
		return false;
	}
}