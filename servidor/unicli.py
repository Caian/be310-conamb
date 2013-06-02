#! /usr/bin/python

import socket
import threading
import base64
import struct
import unidb
from datetime import datetime
import os
import shutil
import math

download_dir = './temp'
resource_dir = './images'

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
			elif head == 'VALU':
				self.validate_user(data)
			elif head == 'POSM':
				self.post_marker(data)
			elif head == 'POSN':
				self.post_news(data)
			#cur_thread = threading.current_thread()
			#response = "{}: {}".format(cur_thread.name, data)
			#self.request.sendall(response)
		self.alive = False

	def extract_head(self, message):
		if len(message) < 4:
			return None
		else:
			return message[0:4]

	def extract_auth(self, message):
		tokens = message[4:].split(' ');
		if (len(tokens) < 2):
			print 'Malformed authentication header'
			return None
		try:	
			username = base64.urlsafe_b64decode(tokens[0].strip())
			password = base64.urlsafe_b64decode(tokens[1].strip())
			return [message[4+len(tokens[0])+1+len(tokens[1]):], [username, password]]
		except:
			print 'Error processing authentication header'
			return None

	def validate_user(self, message):
		r = self.extract_auth(message)
		if r == None or len(r[0]) > 0:
			print 'Malformed VALU request from client', self.addr
			return
		try:
			print 'Received VALU request from client', self.addr
			username = r[1][0]
			password = r[1][1]
			db = unidb.get_database()
			query = db.validate_user(username, password)
			print 'Queried VALU data for client', self.addr
			if query > 0:
				response = 'UUSID '+str(query)+'\n'
			else:
				response = 'UFAIL\n'
			print 'Sending', response.strip(), 'to client', self.addr
			self.request.sendall(response)
			print 'VALU request complete'
		except:
			print 'Error processing VALU request from client', self.addr
		

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
		r = self.extract_auth(message)
		if r == None or len(r[0]) == 0:
			print 'Malformed UPVT request from client', self.addr
			return
		try:
			print 'Received UPVT request from client', self.addr
			username = r[1][0]
			password = r[1][1]
			uid = int(r[0][1:])
		except:
			print 'Malformed UPVT request from client', self.addr
		try:
			db = unidb.get_database()
			uus = db.validate_user(username, password)
			if uus <= 0:
				print 'Invalid authentication from client', self.addr
				return
			count = db.up_vote(uus, uid, 1)
		except:
			print 'Error processing UPVT request from client', self.addr
			return
		try:
			if count == 0:
				print 'News not found', self.addr
			else:
				print 'Sending update to client', self.addr, '...'
				self.update_uid('GETD'+str(uid))
		except:
			print 'Error sending update to client', self.addr
		print 'UPVT request complete'
	
	def set_dnvote(self, message):
		r = self.extract_auth(message)
		if r == None or len(r[0]) == 0:
			print 'Malformed DNVT request from client', self.addr
			return
		try:
			print 'Received DNVT request from client', self.addr
			username = r[1][0]
			password = r[1][1]
			uid = int(r[0][1:])
		except:
			print 'Malformed DNVT request from client', self.addr
			return
		try:
			db = unidb.get_database()
			uus = db.validate_user(username, password)
			if uus <= 0:
				print 'Invalid authentication from client', self.addr
				return
			count = db.dn_vote(uus, uid, 1)
		except:
			print 'Error processing DNVT request from client', self.addr
		try:
			if count == 0:
				print 'News not found', self.addr
			else:
				print 'Sending update to client', self.addr
				self.update_uid('GETD'+str(uid))
		except:
			print 'Error sending update to client', self.addr
		print 'DNVT request complete'

	def post_news(self, message):
		r = self.extract_auth(message)
		if r == None or len(r[0]) == 0:
			print 'Malformed POSN request from client', self.addr
			return
		try:
			print 'Received POSN request from client', self.addr
			message = r[0][1:]
			username = r[1][0]
			password = r[1][1]
			tokens = message.split(' ')
			if len(tokens) != 5:
				print 'Malformed POSN request from client', self.addr
				return
			nname = base64.urlsafe_b64decode(tokens[0].strip())
			ntext = base64.urlsafe_b64decode(tokens[1].strip())
			lat = float(tokens[2].strip())
			lon = float(tokens[3].strip())
			dlen = int(tokens[4].strip())
			utcd = datetime.utcnow()
			utcd = int((utcd-datetime(1970,1,1)).total_seconds())
		except:
			print 'Malformed POSN request from client', self.addr
			return
		print utcd
		try:
			db = unidb.get_database()
			uus = db.validate_user(username, password)
			if uus <= 0:
				print 'Invalid authentication from client', self.addr
				returna
			if dlen > 0:
				fn = os.path.join(download_dir,str(uus) + '.temp')
				with open(fn,'wb') as ftemp:
					count = 0
					print 'Receiving data stream from client', self.addr
					while count < dlen:
						data = self.request.recv(1024)
						count += len(data)
						ftemp.write(data)
					ftemp.close()
			uid = db.post_news(uus, utcd, nname, ntext, lat, lon)
			if uid < 0:
				print 'Failed to post news from client', self.addr
				if dlen > 0:
					os.remove(fn)
				return
			else:
				if dlen > 0:
					fnd = os.path.join(resource_dir,str(uid) + '.jpg')
					os.rename(fn, fnd)
				print 'Sending update to client', self.addr
				self.update_uid('GETD'+str(uid))
		except:
			print 'Error processing POSN request from client', self.addr
		print 'POSN request complete'

	def post_marker(self, message):
		r = self.extract_auth(message)
		if r == None or len(r[0]) == 0:
			print 'Malformed POSM request from client', self.addr
			return
		try:
			print 'Received POSM request from client', self.addr
			message = r[0][1:]
			username = r[1][0]
			password = r[1][1]
			tokens = message.split(' ')
			for t in tokens:
				print t
			if len(tokens) != 4:
				print 'Malformed POSM request from client', self.addr
				return
			type = int(tokens[0].strip())
			print type
			icon = int(tokens[1].strip())
			print icon
			lat = float(tokens[2].strip())
			print lat
			lon = float(tokens[3].strip())
			print lon
			utcd = datetime.utcnow()
			utcd = int((utcd-datetime(1970,1,1)).total_seconds())
		except:
			print 'Malformed POSM request from client', self.addr
			return
		print utcd
		try:
			db = unidb.get_database()
			uus = db.validate_user(username, password)
			if uus <= 0:
				print 'Invalid authentication from client', self.addr
				return
			uid = db.post_marker(uus, utcd, type, icon, lat, lon)
			if uid < 0:
				print 'Failed to post maker from client', self.addr
				return
			else:
				print 'Sending update to client', self.addr
				self.update_uid('GETD'+str(uid))
		except:
			print 'Error processing POSM request from client', self.addr
		print 'POSM request complete'

