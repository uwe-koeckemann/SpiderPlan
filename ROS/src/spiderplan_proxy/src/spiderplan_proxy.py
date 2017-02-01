#!/usr/bin/env python
# -*- coding: utf-8 -*-

#Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>

#Permission is hereby granted, free of charge, to any person obtaining
#a copy of this software and associated documentation files (the
#"Software"), to deal in the Software without restriction, including
#without limitation the rights to use, copy, modify, merge, publish,
#distribute, sublicense, and/or sell copies of the Software, and to
#permit persons to whom the Software is furnished to do so, subject to
#the following conditions:

#The above copyright notice and this permission notice shall be
#included in all copies or substantial portions of the Software.

#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
#EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
#MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
#NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
#LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
#OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
#WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

import fileinput
import socket
import os
import signal
import sys
import time

import rospy

import actionlib

from std_msgs.msg import *
from geometry_msgs.msg import *

from actionlib_tutorials.msg import *

import ROSMessageConversion
import ROSMessageConversion as msg_conv

currentTicket = 0
nextFreeTicket = 0

lastMessage = {}

publishers = {}
publisherMsg = {}
subscriberVar = {}


nextRequestID = 0
actionClientMap = {}

someone_writing =	False

def give_back_ticket():
	if nextFreeTicket > 0:
		nextFreeTicket -= 1

# Provide callbacks with an ID:
class CallbackProvider:
	def __init__(self,requestID):
		self.requestID = requestID
	
	def done_cb(self,state,data):
		lastMessage[(self.requestID,"done")] = msg_conv.get_str_from_ros_msg("done", data)
		
	def active_cb(self):
		lastMessage[(self.requestID,"active")] = True
		
	def feedback_cb(self,data):
		lastMessage[(self.requestID,"feedback")] = msg_conv.get_str_from_ros_msg("feedback", data) 
		
# Provide callbacks that know their topic name:
class SubscriberCallbackProvider:
	def __init__(self,topicName):
		self.topicName = topicName
	
	def callback(self,data):
		lastMessage[self.topicName] = msg_conv.get_str_from_ros_msg(subscriberVar[self.topicName],data)
	
def reg_simple_action_client(server_name,action_name):
	print "Registering action", action_name, " at ", server_name
	print rospy.get_name()	
	client = actionlib.SimpleActionClient(server_name, msg_conv.rosClassMap[action_name])
	client.wait_for_server()
	actionClientMap[(server_name,action_name)] = client

def send_goal(server_name,action_name,goal_msg_str):
	global nextRequestID
	cbp = CallbackProvider(nextRequestID)
	nextRequestID += 1
	print goal_msg_str
	goal = ROSMessageConversion.create_ros_msg_from_str(goal_msg_str)[1]
	client = actionClientMap[(server_name,action_name)]
	client.send_goal(goal,feedback_cb=cbp.feedback_cb,done_cb=cbp.done_cb,active_cb=cbp.active_cb)
	
	nextRequestID += 1
	return cbp.requestID



def subscribe(topicName,msgType,varName):
	print "SUBSCRIBE_TO:", topicName, msgType, varName
	subscriberVar[topicName] = varName
	#rospy.Subscriber(topicName.replace("/",""), msg_conv.rosClassMap.get(msgType), callback)
	cbp = SubscriberCallbackProvider(topicName)
	rospy.Subscriber(topicName, msg_conv.rosClassMap.get(msgType), cbp.callback)
	
def publish(topicName,msgType):
	#publishers[topicName] = rospy.Publisher(topicName.replace("/",""), msg_conv.rosClassMap.get(msgType), queue_size=10)
	publishers[topicName] = rospy.Publisher(topicName, msg_conv.rosClassMap.get(msgType), queue_size=10)
	publisherMsg[topicName] = msgType
	
def send_msg(topicName,msg):
	publishers[topicName].publish(msg_conv.create_ros_msg_from_str(msg)[1])
		
def signal_handler(signal, frame):
	print('Caught Ctrl+C. Closing socket...')
	conn.close()
	s.close()	
	sys.exit(0)
	
def ros_service_call(arg_msgs):
	request = msg_conv.create_ros_msg_from_str(arg_msgs)[1]
	service_name = arg_msgs[1:].split(" ")[0]
	
	rospy.wait_for_service(service_name)
	try:
		serviceProxy = rospy.ServiceProxy(service_name, ROSMessageConversion.rosServiceMap[service_name])
		
		print "Request:\n", request

		response = serviceProxy.call(request)
		
		responseStr = ROSMessageConversion.get_str_from_ros_msg("response",response)
		#responseStr = ROSMessageConversion.split(responseStr[1:len(responseStr)-1])[2]
		print "Response:\n",responseStr

		return responseStr
	except rospy.ServiceException, e:
		print "Service call failed: %s"%e

signal.signal(signal.SIGINT, signal_handler)
	
TCP_IP = '127.0.0.1'
TCP_PORT = 6790
BUFFER_SIZE = 1024  

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((TCP_IP, TCP_PORT))
s.listen(1)

rospy.init_node("SpiderPlanROSProxy", anonymous=True)

ros_namespace = rospy.get_namespace()

print ros_namespace

SUBSCRIBE_TO = 0
PUBLISH_TO = 1
READ_MSG = 2
SEND_MSG = 3
SERVICE_CALL = 4
REGISTER_ACTION = 5
SEND_GOAL = 6
HAS_STARTED = 7
HAS_FINISHED = 8

splitStr = "<//>"


while 1:
	#print "Waiting..."
	conn, addr = s.accept()
	startAll = time.time()
	#print 'Connection address:', addr
	data = ""
	while not "\n" in data:
		data += conn.recv(BUFFER_SIZE)
	data = data.replace("\n","")
	print 'Request:', data.replace(splitStr,"|")
	
	reqType = int(data.split(splitStr)[0])
	
	returnMessage = ""
	
	if reqType == SUBSCRIBE_TO:
		topicName = ros_namespace + data.split(splitStr)[1]
		topicName = topicName.replace("//", "/")
		msgType = data.split(splitStr)[2]
		varName = data.split(splitStr)[3]
		subscribe(topicName,msgType,varName)
		returnMessage = "<OK>"
	elif reqType == PUBLISH_TO:
		topicName = ros_namespace + "/"+data.split(splitStr)[1]
		topicName = topicName.replace("//", "/")
		msgType = data.split(splitStr)[2]
		publish(topicName,msgType)
		returnMessage = "<OK>"
	elif reqType == READ_MSG:
		topicName = ros_namespace + data.split(splitStr)[1]

		if topicName in lastMessage.keys():
			returnMessage = lastMessage[topicName]
			lastMessage[topicName] = "<NONE>"
		else:
			returnMessage = "<NONE>"
	elif reqType == SEND_MSG:
		topicName = ros_namespace + data.split(splitStr)[1]
		#topicName = topicName.replace("//", "/")
		msg = data.split(splitStr)[2]
		send_msg(topicName,msg)
		returnMessage = "<OK>"
	elif reqType == SERVICE_CALL:
		request = data.split(splitStr)[1]
		ros_service_call(request)
		returnMessage = ros_service_call(request)
	elif reqType == REGISTER_ACTION:
		server_name = data.split(splitStr)[1]
		action_name = data.split(splitStr)[2]
		reg_simple_action_client(server_name,action_name)
		returnMessage = "<OK>"
	elif reqType == SEND_GOAL:
		server_name  = data.split(splitStr)[1]
		action_name  = data.split(splitStr)[2]
		goal_msg_str = data.split(splitStr)[3]
		requestID = send_goal(server_name,action_name,goal_msg_str)
		returnMessage = str(requestID)
	elif reqType == HAS_STARTED:
		requestID  = int(data.split(splitStr)[1])
		if (requestID,"active") in lastMessage.keys():
			returnMessage = "true"
		else:
			returnMessage = "false"
	elif reqType == HAS_FINISHED:
		requestID  = int(data.split(splitStr)[1])
		if (requestID,"done") in lastMessage.keys():
			returnMessage = lastMessage[(requestID,"done")]
		else:
			returnMessage = "false"
	elif reqType == "get_feedback":
		requestID  = int(data.split(splitStr)[1])
		if (requestID,"feedback") in lastMessage.keys():
			returnMessage = lastMessage[(requestID,"feedback")]
		else:
			returnMessage = "<NONE>"
		
	
	conn.send(returnMessage) 
	conn.close()
	
	endAll = time.time()

	print 'Response: %s (took %.2fs)' % (returnMessage,endAll-startAll)
	
	
	
