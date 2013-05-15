#! /usr/bin/python

import socket
import threading
import SocketServer
import unicli

class ThreadedTCPRequestHandler(SocketServer.BaseRequestHandler):
	client = None

	def setup(self):
		return SocketServer.BaseRequestHandler.setup(self)

	def finish(self):
		print 'Client disconnected', self.client_address
		return SocketServer.BaseRequestHandler.finish(self)

	def handle(self):
		self.client = unicli.UnilinkClient(self.request, self.client_address)
		print 'Handling client', self.client_address
		self.client.loop()

class ThreadedTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):
    pass

class UnilinkServerCore:
	running = False
	server = None

	def start(self, port):
		if self.running:
			stop()
		HOST, PORT = "0.0.0.0", port
		print 'Starting server on port', port
		server = ThreadedTCPServer((HOST, PORT), ThreadedTCPRequestHandler)
		ip, port = server.server_address
		server_thread = threading.Thread(target=server.serve_forever)
		server_thread.daemon = True
		server_thread.start()
		print "Server loop running in thread:", server_thread.name
		self.running = True
		self.server = server

	def stop(self):
		if self.running:
			print 'Server shutting down...'
			self.server.shutdown()
			self.server = None
			self.running = False

