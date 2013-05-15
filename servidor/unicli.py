#! /usr/bin/python

import socket
import threading
import base64
import struct
import unidb

class UnilinkClient:

	def __init__(self, request, addr):
		self.alive = True
		self.request = request
		self.addr = addr

	def loop(self):
		while 1:
			data = self.request.recv(1024)
			if not data:
				break

			print 'Received a', data

			head = self.extract_head(data)
			if head == None:
				print 'Malformed request from client', self.addr
			elif head == 'NEAR':
				self.update_location(data)
			elif head == 'GETD':
				self.update_uid(data)
			elif head == 'UPVT':
				self.set_upvote(data)
			elif head == 'DNVT':
				self.set_dnvote(data)
			#cur_thread = threading.current_thread()
			#response = "{}: {}".format(cur_thread.name, data)
			#self.request.sendall(response)
		self.alive = False

	def extract_head(self, message):
		if len(message) < 4:
			return None
		else:
			return message[0:4]

	def update_location(self, message):
		try:
			latq = [int(message[4:7]),int(message[7:9])]
			lonq = [int(message[9:12]),int(message[12:14])]
		except:
			print 'Malformed NEAR request from client', self.addr
			return
		try:
			print 'Received NEAR request from client', self.addr
			db = unidb.get_database()
			query = db.find_quadrant(latq, lonq)
			print 'Queried NEAR data for client', self.addr
			for x in query[0]:
				response = "NEAR M {} {}\n".format(x['uid'],x['date'])
				print 'Sending', response.strip(), 'to client', self.addr
				self.request.sendall(response)
			for x in query[1]:
				response = "NEAR N {} {}\n".format(x['uid'],x['date'])
				print 'Sending', response.strip(), 'to client', self.addr
				self.request.sendall(response)
			response = "EORQ\n"
			self.request.sendall(response)
			print 'NEAR request complete'
		except:
			print 'Error processing NEAR request from client', self.addr

	def update_uid(self, message):
		try:
			uid = int(message[4:])
		except:
			print 'Malformed GETD request from client', self.addr
			return
		try:
			db = unidb.get_database()
			query = db.find_uid(uid)
			print 'Queried GETD data for client', self.addr
			for x in query[0]:
				response = "MARK {} {} {} {} {} {}\n".format(x['uid'],x['date'],x['type'],x['icon'],x['lat'],x['lon'])
				print 'Sending', response.strip(), 'to client', self.addr
				self.request.sendall(response)
			for x in query[1]:
				response = "NEWS {} {} {} {} {} {} {} {}\n".format(x['uid'],x['date'],x['lat'],x['lon'],x['upvt'],x['dnvt'],base64.urlsafe_b64encode(x['name']),base64.urlsafe_b64encode(x['text']))
				print 'Sending', response.strip(), 'to client', self.addr
				self.request.sendall(response)
			print 'GETD request complete'
		except:
			print 'Error processing GETD request from client', self.addr

	def set_upvote(self, message):
		try:
			uid = int(message[4:])
		except:
			print 'Malformed UPVT request from client', self.addr
		try:
			db = unidb.get_database()
			db.up_vote(0, uid, 1)
		except:
			print 'Error processing UPVT request from client', self.addr
		try:
			print 'Sending update to client', self.addr, '...'
			self.update_uid('GETD'+str(uid))
		except:
			print 'Error sending update to client', self.addr
		print 'UPVT request complete'
	
	def set_dnvote(self, message):
		try:
			uid = int(message[4:])
		except:
			print 'Malformed DNVT request from client', self.addr
		try:
			db = unidb.get_database()
			db.dn_vote(0, uid, 1)
		except:
			print 'Error processing DNVT request from client', self.addr
		try:
			print 'Sending update to client', self.addr
			self.update_uid('GETD'+str(uid))
		except:
			print 'Error sending update to client', self.addr
		print 'DNVT request complete'

