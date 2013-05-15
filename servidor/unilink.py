#!/usr/bin/python

import socket, threading, unisc, unicli, sys

def client(ip, port, message):
   sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
   sock.connect((ip, port))
   try:
      sock.sendall(message)
      response = sock.recv(1024)
      print "Received: {}".format(response)
   finally:
      sock.close()

print 'Unilink prototype server v0.1a'

port = 3136

server = unisc.UnilinkServerCore()
server.start(port)

#client('127.0.0.1', port, 'hi there 1!!')
#client('127.0.0.1', port, 'hi there 2!!')
#client('127.0.0.1', port, 'hi there 3!!')
#client('127.0.0.1', port, 'hi there 4!!')
#client('127.0.0.1', port, 'hi there 5!!')

while 1:
	sys.stdout.write('>> ')
	str = sys.stdin.readline().replace('\n','')
	if str == 'quit':
		break

server.stop()
