#!/bin/python
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

import rospy
from std_msgs.msg import *
from geometry_msgs.msg import *

from rospy_tutorials.srv import *

from actionlib_msgs.msg import *
from move_base_msgs.msg import MoveBaseAction, MoveBaseGoal

from actionlib_tutorials.msg import *

from sound_play.msg import SoundRequest

rosClassMap = {}
rosServiceMap = {}

###########################################################
# ROS Messages
###########################################################

# std_msgs
rosClassMap["String"] = String
rosClassMap["Bool"] = Bool
rosClassMap["Float32"] = Float32
rosClassMap["Float32MultiArray"] = Float32MultiArray
rosClassMap["Float64"] = Float64
rosClassMap["Float64MultiArray"] = Float64MultiArray
rosClassMap["Int8"] = Int8
rosClassMap["Int8MultiArray"] = Int8MultiArray
rosClassMap["Int16"] = Int16
rosClassMap["Int16MultiArray"] = Int16MultiArray
rosClassMap["Int32"] = Int32
rosClassMap["Int32MultiArray"] = Int32MultiArray
rosClassMap["Int64"] = Int64
rosClassMap["Int64MultiArray"] = Int64MultiArray
rosClassMap["UInt8"] = UInt8
rosClassMap["UInt8MultiArray"] = UInt8MultiArray
rosClassMap["UInt16"] = UInt16
rosClassMap["UInt16MultiArray"] = UInt16MultiArray
rosClassMap["UInt32"] = UInt32
rosClassMap["UInt32MultiArray"] = UInt32MultiArray
rosClassMap["UInt64"] = UInt64
rosClassMap["UInt64MultiArray"] = UInt64MultiArray
rosClassMap["Time"] = Time
rosClassMap["Header"] = Header

# geometry_msgs
rosClassMap["Pose2D"] = Pose2D
rosClassMap["Pose"] = Pose
rosClassMap["PoseStamped"] = PoseStamped
rosClassMap["Point"] = Point
rosClassMap["Quaternion"] = Quaternion

###########################################################
# ROS Action Lib
###########################################################

# move_base_msgs
rosClassMap["MoveBaseAction"] = MoveBaseAction
rosClassMap["MoveBaseGoal"] = MoveBaseGoal

# actionlib_msgs

# action_lib_tutorial
rosClassMap["FibonacciAction"] = FibonacciAction
rosClassMap["FibonacciGoal"] = FibonacciGoal


###########################################################
# ROS Services (link to request in rosClassMap)
###########################################################
rosClassMap["AddTwoInts"] = AddTwoIntsRequest
rosServiceMap["AddTwoInts"] = AddTwoInts




intTypes = ["int8","int16","int32","int64","uint8","uint16","uint32","uint64"]
floatTypes = ["float8","float16","float32","float64"]

basicTypes = ["int8","int16","int32","int64","uint8","uint16","uint32","uint64","float8","float16","float32","float64","string","bool","string"]
basicTypesMain = ["Bool", "String", "Float32","Float64", "Int8", "Int16", "Int32", "Int64", "UInt8", "UInt16", "UInt32", "UInt64"]

def split(s):
	r = []
	depth = 0
	sub = ""
	for c in s:
		if c == "(" or c == "{":
			depth += 1
			sub += c
		elif c == ")" or c == "}":
			depth -= 1
			sub += c
		elif c == " " and depth == 0:
			if sub != "":
				r.append(sub)
			sub = ""
		else:
			sub += c
	if sub != "":
		r.append(sub)
	return r
	
test = split("Pose2D pose (Float64 x 10.0) (Float64 y 20.0) (Float64 theta 45.0)")

assert test[0] == "Pose2D"
assert test[1] == "pose"
assert test[2] == "(Float64 x 10.0)"
assert test[3] == "(Float64 y 20.0)"
assert test[4] == "(Float64 theta 45.0)"

def parse_array(className,s):
	s = s[1:len(s)-1]
	a = []
	for entry in split(s):
		if "{" in entry:
			a.append(parse_array(className,entry))
		else:
			if "String" == className:
				a.append(entry)
			elif "Int" in className and not "MultiArray" in className:
				a.append(int(entry))
			elif "Float" in className and not "MultiArray" in className:
				a.append(float(entry))
			elif "Bool" == className:
				a.append(bool(entry))
			else:
				a.append(create_ros_msg_from_str(entry))
	return a
	
test = parse_array("Float64", "{1.0 2.0 3.0}")
assert test == [1.0, 2.0, 3.0]
test = parse_array("Float64", "{{1.0 2.0 3.0} {4.0 5.0 6.0} {7.0 8.0 9.0}}")
assert test[0] == [1.0, 2.0, 3.0]
assert test[1] == [4.0, 5.0, 6.0]
assert test[2] == [7.0, 8.0, 9.0]

def create_ros_msg_from_str(s):
	#print s
	s = s[1:len(s)-1]
	parts = split(s)
		#print parts
	className = parts[0]
	varName = parts[1]

	value = None
	
	if "String" == className:
		if not "{" in parts[2]:
			value = parts[2]
		else:
			value = parse_array(className, parts[2])
	elif className == "int" or className == "int8" or className == "int16" or className == "int32" or className == "int64" or ("Int" in className and not "MultiArray" in className):
		if not "{" in parts[2]:
			value = int(parts[2])
		else:
			value = parse_array(className,parts[2])
	elif className == "float" or ("Float" in className and not "MultiArray" in className):
		if not "{" in parts[2]:
			value = float(parts[2])
		else:
			value = parse_array(className, parts[2])
	elif "Bool" == className:
		if not "{" in parts[2]:
			if parts[2] == "False" or parts[2] == "false":
				value = False
			elif parts[2] == "True" or parts[2] == "true":
				value = True
			else:
				value = bool(parts[2])
		else:
			value = parse_array(className, parts[2])
	else:
		args = {}
		for part in parts[2:]:
			entry = create_ros_msg_from_str(part)
			args[entry[0]] = entry[1]
		value = rosClassMap[className](**args)
	return (varName,value)
		
	
test = create_ros_msg_from_str("(Float32 x 10.0)")

assert test[0] == "x"
assert test[1] == 10.0

test = create_ros_msg_from_str("(Pose pose (Point position (float x 1.0) (float y 2.0) (float z 3.0)) (Quaternion orientation (float x 4.0) (float y 5.0) (float z 6.0) (float w 7.0)))")

assert test[1].position.x == 1.0
assert test[1].position.y == 2.0
assert test[1].position.z == 3.0
assert test[1].orientation.x == 4.0
assert test[1].orientation.y == 5.0
assert test[1].orientation.z == 6.0
assert test[1].orientation.w == 7.0

def get_str_from_ros_msg(attName,msg):
	#print attName, " ", msg, " ", type(msg)
	
	if str(msg.__class__.__name__) in basicTypesMain:
		if not str(msg.__class__.__name__) == "Bool":
			return "(" + str(msg.__class__.__name__) + " " + attName + " " + str(getattr(msg,"data")) + ")"
		else:
			return "(" + str(msg.__class__.__name__) + " " + attName + " " + str(getattr(msg,"data")).replace("True","true").replace("False","false") + ")"
			
	
	msgStr = "(" + str(msg.__class__.__name__) + " " + attName + " "
	for i in range(0,len(msg.__slots__)):
		#print "Types: ", msg.__slots__[i], " ", msg._slot_types[i]
		if msg._slot_types[i] in basicTypes:
			if not msg._slot_types[i] == "bool":
				st = str(getattr(msg,msg.__slots__[i]))
			else:
				st = str(getattr(msg,msg.__slots__[i])).replace("True","true").replace("False","false") 
			msgStr += "(" + getattr(msg,msg.__slots__[i]).__class__.__name__ + " " + msg.__slots__[i] + " " + st + ")"
		elif msg._slot_types[i] == "int8[]" or msg._slot_types[i] == "int16[]" or msg._slot_types[i] == "int32[]" or msg._slot_types[i] == "int64[]" or msg._slot_types[i] == "int[]":
			sub = "(int " + msg.__slots__[i] + " {"
			for value in getattr(msg,msg.__slots__[i]):
				sub += " " + str(value)
			sub += " } )"
			msgStr += sub
		elif msg._slot_types[i] == "float32[]" or msg._slot_types[i] == "float64[]" or msg._slot_types[i] == "float[]":
			sub = "(float " + msg.__slots__[i] + " {"
			for value in getattr(msg,msg.__slots__[i]):
				sub += " " + str(value)
			sub += " } )"
			msgStr += sub
		else:
			msgStr += get_str_from_ros_msg(msg.__slots__[i],getattr(msg,msg.__slots__[i]))
		msgStr += " "
	return msgStr[0:len(msgStr)-1].replace("  "," ") + ")"

msg = create_ros_msg_from_str("(Point position (float x 1.0) (float y 2.0) (float z 3.0))")[1]
str_msg = get_str_from_ros_msg("position", msg)

assert str_msg == "(Point position (float x 1.0) (float y 2.0) (float z 3.0))"

msg = create_ros_msg_from_str("(Pose pose (Point position (float x 1.0) (float y 2.0) (float z 3.0)) (Quaternion orientation (float x 4.0) (float y 5.0) (float z 6.0) (float w 7.0)))")[1]
str_msg = get_str_from_ros_msg("pose", msg)
assert str_msg == "(Pose pose (Point position (float x 1.0) (float y 2.0) (float z 3.0)) (Quaternion orientation (float x 4.0) (float y 5.0) (float z 6.0) (float w 7.0)))"
	
