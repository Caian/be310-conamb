#! /usr/bin/python

import tables
from tables import *
from threading import Lock

class UnilinkDBMarker(IsDescription):
	uus = UInt32Col()
	uid = UInt32Col()
	date = UInt32Col()
	type = UInt32Col()
	icon = UInt32Col()
	lat = Float64Col()
	lon = Float64Col()

class UnilinkDBNews(IsDescription):
	uus = UInt32Col()
	uid = UInt32Col()
	date = UInt32Col()
	name = StringCol(32)
	text = StringCol(140)
	lat = Float64Col()
	lon = Float64Col()
	upvt = Int32Col()
	dnvt = Int32Col()

class UnilinkDBVote(IsDescription):
	uus = UInt32Col()
	uid = UInt32Col()
	dir = Int32Col()

class UnilinkDBUser(IsDescription):
	name = StringCol(64)
	passw = StringCol(32)
	uus = UInt32Col()

class UnilinkDatabase:

	def __init__(self, initialize):
		self.uidlock = Lock()
		self.uidref = 0
		if initialize:
			h5file = openFile('db.h5', mode = "w", title = "MAINDB")
			group = h5file.createGroup("/", 'data', 'MAINDATA')
			h5file.createTable(group, 'markers', UnilinkDBMarker, "MARKERS")
			h5file.createTable(group, 'news', UnilinkDBNews, "NEWS")
			h5file.createTable(group, 'users', UnilinkDBUser, "USERS")
			h5file.createTable(group, 'votes', UnilinkDBVote, "VOTES")
			h5file.close()
		self.tables = tables.openFile("db.h5", mode = "r+")
		self.mtable = self.tables.root.data.markers
		self.ntable = self.tables.root.data.news
		self.utable = self.tables.root.data.users
		self.vtable = self.tables.root.data.votes
		for i in self.mtable.cols.uid[:]:
			if i > self.uidref:
				self.uidref = i
			elif i == self.uidref:
				print 'Warning: UID duplicate detected in mtable'
		for i in self.ntable.cols.uid[:]:
			if i > self.uidref:
				self.uidref = i
			elif i == self.uidref:
				print 'Warning: UID duplicate detected in ntable'

	def validate_user(self, username, password):
		q = '(name == \'' + username + '\') & (passw == \'' + password + '\')'
		ru = self.utable.where(q)

		uus = -1
		uuc = 0
		for user in ru:
			uuc += 1
			if uus == -1:
				uus = user['uus']
		if uuc > 1:
				print 'Warning: User duplicate detected in utable'
		if uus == 1:
				print 'Error: Blocked connection as administrator'
				uus = -1
		return uus

	def find_quadrant(self, latq, lonq):
		latu = (latq[0] + (latq[1] + 2) / 60.0) - 90.0
		lonu = (lonq[0] + (lonq[1] + 2) / 60.0) - 180.0
		latl = (latq[0] + (latq[1] - 2) / 60.0) - 90.0
		lonl = (lonq[0] + (lonq[1] - 2) / 60.0) - 180.0
		q = '(lat > '+str(latl)+') & (lat < '+str(latu)+') & (lon > '+str(lonl)+') & (lon < '+str(lonu)+')'
		rm = self.mtable.where(q)
		rn = self.ntable.where(q)
		return [rm, rn]

	def find_uid(self, uid):
		q = '(uid == ' + str(uid) + ')'
		rm = self.mtable.where(q)
		rn = self.ntable.where(q)
		return [rm, rn]

	def post_marker(self, uus, date, type, icon, lat, lon):
		self.uidlock.acquire()
		uid = self.uidref + 1
		try:
			marker = self.mtable.row
			marker['uus'] = 0
			marker['uid'] = uid
			marker['date'] = date
			marker['type'] = type
			marker['icon'] = icon
			marker['lat'] = lat
			marker['lon'] = lon
			marker.append()
			self.mtable.flush()
			self.uidref = uid
		except:
			print 'Exception occurred during marker POST'
			uid = -1
		self.uidlock.release()
		return uid
	
	def post_news(self, uus, date, name, text, lat, lon):
		self.uidlock.acquire()
		uid = self.uidref + 1
		try:
			news = self.ntable.row
			news['uus'] = 0
			news['uid'] = uid
			news['date'] = date
			news['name'] = name
			news['text'] = text
			news['lat'] = lat
			news['lon'] = lon
			news['upvt'] = 0
			news['dnvt'] = 0
			news.append()
			self.ntable.flush()
			self.uidref = uid
		except:
			print 'Exception occurred during news POST'
			uid = -1
		self.uidlock.release()
		return uid

	def up_vote(self, uus, uid, val):
		self.uidlock.acquire()
		#try:
		#	votedir = 0
		#	voterow = None
		#	for x in self.vtable.where('(uid =='+str(uid)+')&(uus=='+str(uus)+')')):
		#		votedir = x['dir']
		#		voterow = x
		#	if votedir == 1:
		#		print 'Blocked duplicated upvote from'
		try:
			for row in self.ntable.where('uid == ' + str(uid)):
				row['upvt'] += val
				row.update()
		except:
			print 'Exception occurred during news UPVT'
		self.uidlock.release()

	def dn_vote(self, uus, uid, val):
		self.uidlock.acquire()
		try:
			for row in self.ntable.where('uid == ' + str(uid)):
				row['dnvt'] += val
				row.update()
		except:
			print 'Exception occurred during news DNVT'
		self.uidlock.release()

singleton = None

def debug_initdb():
	global singleton
	if singleton != None:
		print 'DB already created, there\'s nothing to do'
	else:
		singleton = UnilinkDatabase(True)

def get_database():
	global singleton
	if singleton == None:
		singleton = UnilinkDatabase(False)
	return singleton
