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

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.spiderplan.executor.ExecutionManager;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.SocketRelation;
import org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation;
import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.expressions.execution.sockets.SocketExpression;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.UniqueID;
import org.spiderplan.tools.logging.Logger;

/**
 * @author Uwe Köckemann
 *
 */
public class SocketExecutionManager extends ExecutionManager {
	
	/**
	 * Initialize with name.
	 * @param name
	 */
	public SocketExecutionManager( String name ) {
		super(name);
	}
	
	List<SocketExpression> receivers = new ArrayList<SocketExpression>();
	List<SocketExpression> senders = new ArrayList<SocketExpression>();
	Term sockerReceiverInterval = Term.createVariable("?I_Socket");

	List<Term> socketAliases = new ArrayList<Term>();
	Map<Term,Socket> socketLookup = new HashMap<Term, Socket>();
	Map<Term,SocketMessageThreadReceiver> messageThreadLookup = new HashMap<Term, SocketMessageThreadReceiver>();
	Map<Term,SocketMessageThreadSend> messageThreadSendLookup = new HashMap<Term, SocketMessageThreadSend>();
		
	@Override
	public void initialize( ConstraintDatabase cdb ) {
		/**
		 * ROS subscriptions
		 */
		for ( SocketExpression socketExp : cdb.get(SocketExpression.class) ) {
			if ( socketExp.getRelation().equals(SocketRelation.Send) ) {
				if ( verbose ) Logger.msg(this.getName(),"Adding sender: " + socketExp, 1);
				this.senders.add(socketExp);
			} else if ( socketExp.getRelation().equals(SocketRelation.Receive) ) {
				if ( verbose ) Logger.msg(this.getName(),"Adding receiver: " + socketExp, 1);
				this.receivers.add(socketExp);
			} else {
				Term expression = socketExp.getConstraint();
				Term alias = expression.getArg(0);
				String hostName = expression.getArg(1).toString().replace("\"", "");
				int portNumber = Integer.valueOf(expression.getArg(2).toString());
				
				// Create socket and threat put both in lookup under alias
				
				try {
					Socket socket = new Socket(hostName, portNumber);
//					socket.setSoTimeout(100);
					SocketMessageThreadReceiver socketThread = new SocketMessageThreadReceiver(socket, false);
					SocketMessageThreadSend socketThreadSend = new SocketMessageThreadSend(socket, false);
					socketThread.start();
					socketThreadSend.start();
					
					socketLookup.put(alias, socket);
					messageThreadLookup.put(alias, socketThread);
					messageThreadSendLookup.put(alias, socketThreadSend);
					socketAliases.add(alias);
				} catch ( Exception e ) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	}
	

	@Override
	public boolean update(long t, ConstraintDatabase cdb) {
		/**
		 * What this should do:
		 * 
		 * - Read all messages that arrived on sockets and put then into statements
		 * - Send messages from statements when the time comes
		 */			
		ValueLookup propagatedTimes = cdb.getUnique(ValueLookup.class);
		
		for ( Term socketAlias : this.socketAliases ) {
			SocketMessageThreadReceiver thread = this.messageThreadLookup.get(socketAlias);
			ConcurrentLinkedQueue<String> msgQueue = thread.getMessages();
			while ( !msgQueue.isEmpty() ) {
				String msg = msgQueue.poll();
				
				for ( SocketExpression receiver : this.receivers ) {
					Term receiverAlias = receiver.getConstraint().getArg(0);
					if ( receiverAlias.equals(socketAlias) ) {
						Term variable = receiver.getConstraint().getArg(1);
						Term value = receiver.getConstraint().getArg(2);

						if ( receiver.getConstraint().getNumArgs() == 3 ) {
							Term msgTerm = Term.createConstant(msg); // TODO: need parsing
						
							Substitution sub = value.match(msgTerm);
						
							if ( sub != null ) { // message applies to expression and will be added
								Term interval = this.sockerReceiverInterval.makeUnique(UniqueID.getID());
								variable = variable.substitute(sub);
								value = value.substitute(sub);
								Statement msgStatement = new Statement(interval, variable, value);
								AllenConstraint msgConstraint = new AllenConstraint(interval, TemporalRelation.At, new Interval(t, t), new Interval(t+1, t+1));
								cdb.add(msgStatement);
								cdb.add(msgConstraint);
								
								System.out.println("Received message: " + msg);
								if ( verbose ) Logger.msg(this.getName(),"[Socket] Received statement "+msgStatement+" from "+socketAlias, 2);
							}
						} else {
							String msgCopy = msg;
							Term msgMatchTerm = receiver.getConstraint().getArg(3);
							Term stringOps = receiver.getConstraint().getArg(4);
							
							if ( verbose ) Logger.msg(this.getName(),"[Socket] Message before string operations:" + msgCopy, 3);
							
							for ( int i = 0 ; i < stringOps.getNumArgs() ; i++ ) {
								Term op = stringOps.getArg(i);
								if ( op.getName().equals("head") ) {
									String arg = op.getArg(0).toString();
									arg = arg.substring(1, arg.length()-1);
									msgCopy = arg+msgCopy;
								} else if ( op.getName().equals("tail") ) {
									String arg = op.getArg(0).toString();
									arg = arg.substring(1, arg.length()-1);
									msgCopy = msgCopy+arg;
								} else if ( op.getName().equals("replace") ) {
									String arg1 = op.getArg(0).toString();
									arg1 = arg1.substring(1, arg1.length()-1);
									String arg2 = op.getArg(1).toString();
									arg2 = arg2.substring(1, arg2.length()-1);
									
									System.out.println("|"+arg1+"|");
									System.out.println("|"+arg2+"|");
									
									msgCopy = msgCopy.replaceAll(arg1, arg2);
								}
							}
							
							if ( verbose ) {
								Logger.msg(this.getName(),"[Socket] Message after string operations: " + msgCopy, 3);
								Logger.msg(this.getName(),"[Socket] Matching message to: " + msgMatchTerm, 3);
							}
							
							
							Term parsedMsg = Term.parse(msgCopy);
							Substitution sub = msgMatchTerm.match(parsedMsg);
							if ( sub != null ) {
								Logger.msg(this.getName(),"[Socket] Successful matching: " + sub, 3);
								Term interval = this.sockerReceiverInterval.makeUnique(UniqueID.getID());
								variable = variable.substitute(sub);
								value = value.substitute(sub);
								Statement msgStatement = new Statement(interval, variable, value);
								AllenConstraint msgConstraint = new AllenConstraint(interval, TemporalRelation.At, new Interval(t, t), new Interval(t+1, t+1));
								cdb.add(msgStatement);
								cdb.add(msgConstraint);
								if ( verbose ) Logger.msg(this.getName(),"[Socket] Received statement "+msgStatement+" from "+socketAlias, 2);
							}
						}
					}
				}
			}
		}
		
		for ( SocketExpression sender : senders ) {
			Term alias = sender.getConstraint().getArg(0);
			Term variable = sender.getConstraint().getArg(1); 
			Term value = sender.getConstraint().getArg(2);
			
			for ( Statement s : cdb.get(Statement.class) ) {
				Substitution subVar = s.getVariable().match(variable);
				Substitution subVal = s.getValue().match(value);
				
				if ( subVar != null && subVal != null && propagatedTimes.getEST(s.getKey()) <= t && propagatedTimes.getEET(s.getKey()) > t ) {
					
					SocketMessageThreadSend msgThread = messageThreadSendLookup.get(alias);
					boolean foundMatchingInclude = false;
					for ( IncludedProgram ip : cdb.get(IncludedProgram.class) ) {
						Substitution incSub = ip.getName().match(s.getValue());
						if ( incSub != null ) {
							foundMatchingInclude = true;
							IncludedProgram ipCopy = ip.copy();
							ipCopy = (IncludedProgram)ipCopy.substitute(incSub);
							msgThread.queueMessageToSend(ipCopy.getCode());
							
							System.out.print("Sending action request: " + ipCopy.getCode());
							break;
						}
					}
					if ( !foundMatchingInclude ) {
						msgThread.queueMessageToSend(s.getValue().toString());
					}
					
					
					if ( verbose ) Logger.msg(this.getName(),"[Socket] Sending message "+s.getValue()+" to socket " + alias, 2);
				}
			}
		}	
		
		
		super.update(t, cdb);
		
		return true;
	}
}
