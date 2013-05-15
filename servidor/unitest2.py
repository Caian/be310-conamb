#! /usr/bin/python

import unidb
db = unidb.get_database()
latq = [66, 48]
lonq = [133, 7]
r = db.find_quadrant(latq, lonq)
for x in r[0]:
	print x['uus'], x['uid'], x['date']
for x in r[1]:
	print x['uus'], x['uid'], x['date']
