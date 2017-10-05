#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import sys
import time
import datetime

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Bind the socket to the port
server_address = ('localhost', 12347)
print >>sys.stderr, 'starting up on %s port %s' % server_address
sock.bind(server_address)

sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

# Listen for incoming connections
sock.listen(1)


while True:
	# Wait for a connection
	print >>sys.stderr, 'waiting for a connection'
	connection, client_address = sock.accept()
	#sock.setblocking(0)


	try:
		print >>sys.stderr, 'connection from', client_address

		# Receive the data in small chunks and retransmit it
		data = ""
		while True:
			byte = connection.recv(1)
			if byte == "\n":
				print "Received:", data
				data = "[" + time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime()) + "," + data + "]"
				connection.sendall(data+"\n")
				data = ""
			else: 
				data += byte
			#time.sleep(0.5)
	#data = connection.recv(16)
	#print >>sys.stderr, 'received "%s"' % data
	#if data:
	#print >>sys.stderr, 'sending data back to the client'
	#connection.sendall(data)
	#else:
	#print >>sys.stderr, 'no more data from', client_address
	#break
	except socket.error, v:
		print v
		

	finally:
		# Clean up the connection
		connection.close()
