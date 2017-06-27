#!/bin/python
# -*- coding: utf-8 -*-

#Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>

#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:

#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.

#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

import fileinput
import socket
import os
import signal
import sys
import time

def signal_handler(signal, frame):
	print('Caught Ctrl+C. Closing socket...')
	conn.close()
	s.close()	
	sys.exit(0)

 
signal.signal(signal.SIGINT, signal_handler)


TCP_IP = '127.0.0.1'
TCP_PORT = 6789
BUFFER_SIZE = 1024  

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((TCP_IP, TCP_PORT))
s.listen(1)

reqID = 0

while 1:
	reqID += 1
	print "Waiting..."
	conn, addr = s.accept()
	startAll = time.time()
	print 'Connection address:', addr
	print 'Reading message until newline...'
	data = ""
	while not "\n" in data:
		data += conn.recv(BUFFER_SIZE)
	
	cmd = data.split("<CMD>")[1].replace("\n","")
	dir = data.split("<CMD>")[0]
	
	outFileName = "stdout%d" %( reqID )
	errFileName = "stderr%d" %( reqID )
	cmd = "%s > %s 2> %s" %(cmd, outFileName,errFileName)
	#print "Directory:", dir
	os.chdir(dir)
	print "Request:", cmd
	startCall = time.time()
	os.system(cmd)
	endCall = time.time()
	print "External call time: %.2fs" % (endCall-startCall)
	
	f = open(outFileName,"r")
	r = ""
	for l in f.readlines():
		r += l
	r += "<ERR>"
	f.close()
	f = open(errFileName,"r")
	for l in f.readlines():
		r += l
	r += "<EOF>"
	#print "Response:\n",r
	
	f.close()	
	
	conn.send(r.replace("\n","<LINEBREAK>")) 
	conn.close()
	
	endAll = time.time()
	
	os.system("rm " + outFileName);
	os.system("rm " + errFileName);	
	
	print "Total request time: %.2fs" % (endCall-startCall)
	
	
	
