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
package org.spiderplan.executor.ROS;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.spiderplan.executor.CommunicationProxy;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.Loop;

/**
 * Manages all communication with ROS. 
 * <p>
 * <b>Note:</b> Requires to run "spliderplan_proxy.py" server. 
 * 
 * @author Uwe Köckemann
 *
 */
public class ROSProxy implements CommunicationProxy {
	
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
	

	
	/**
	 * Request subscription to a ROS topic.
	 * 
	 * @param topicName name of the topic
	 * @param msgType type of ROS message
	 * @param varName name of variable
	 * @return <code>true</code> if request was successful, <code>false</code> otherwise.
	 */
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
	
	/**
	 * Register a publisher to a ROS topic.
	 * 
	 * @param topicName name of ROS topic
	 * @param msgType message type
	 * @return <code>true</code> if request was successful, <code>false</code> otherwise
	 */
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
	
	/**
	 * Get last message published on a ROS topic.
	 * 
	 * @param topicName name of ROS topic
	 * @return message as a term
	 */
	public static Term get_last_msg( String topicName ) {
		return subsriberThread.get(topicName).outputMsg;
	}
	
	/**
	 * Read message from ROS topic.
	 * 
	 * @param topicName name of ROS topic
	 * @return message as a term
	 */
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
	
	/**
	 * Send message to a ROS topic.
	 * 
	 * @param topicName name of ROS topic
	 * @param msg term representing the message
	 * @return <code>true</code> if message was sent successfully, <code>false</code> otherwise.
	 */
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
	
	/**
	 * Perform a ROS service call.
	 * 
	 * @param request term representation of service call and its arguments
	 * @return term representation of message returned by service call
	 */
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
	
	/**
	 * Register with ROS action server to send goals there later.
	 * 
	 * @param serverID ID of server
	 * @param actionName name of action to register
	 * @return <code>true</code> if request was successful, <code>false</code> otherwise
	 */
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
	
	/**
	 * Send goal to ROS action server. Used by ReactorROS.
	 * 
	 * @param serverID ID of server
	 * @param actionName name of action
	 * @param goalMsg ROS goal message
	 * @return a request ID that can be used to see if the action corresponding to the goal has started/finished
	 */
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
	
	/**
	 * Test if an action has started. Used by ReactorROS.
	 * 
	 * @param requestID ID returned when the goal was sent
	 * @return <code>true</code> if the action is being executed, <code>false</code> otherwise.
	 */
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
	
	/**
	 * Test if an action has finished. Used by ReactorROS.
	 * 
	 * @param requestID ID returned when the goal was sent
	 * @param resultMsg message returned by action when it finished
	 * @return <code>true</code> if the action has finished execution, <code>false</code> otherwise.
	 */
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

	@Override
	public void initialize(ConstraintDatabase cdb) {
		
//		/**
//		 * ROS subscriptions
//		 */
//		for ( ROSConstraint rosCon : cdb.get(ROSConstraint.class) ) {
//			if ( rosCon.getRelation().equals(ROSRelation.PublishTo) ) {
//				ROSProxy.publishToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName());
//				this.ROSpubs.add(rosCon);
//			} else {
//				ROSProxy.subscribeToTopic(rosCon.getTopic().toString(), rosCon.getMsg().getName(), rosCon.getMsg().getArg(0).toString());
//				this.ROSsubs.add(rosCon);
//				variablesObservedByROS.add(rosCon.getVariable());
//			}
//		}
//		
//		for ( ROSRegisterAction regAction : execDB.get(ROSRegisterAction.class) ) {
//			ROSProxy.register_action(regAction.getServerID(), regAction.getActionName());
//		}
		
	}

	@Override
	public ConstraintDatabase update(long t, ConstraintDatabase cdb) {
		// TODO Auto-generated method stub
		return null;
	}
}