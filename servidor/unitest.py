#! /usr/bin/python

import tables
from tables import *

db = tables.openFile("db.h5", mode = "r+")
mtable = db.root.data.markers
ntable = db.root.data.news

marker = mtable.row

marker['uus'] = 0
marker['uid'] = 1
marker['date'] = 11
marker['type'] = 35
marker['icon'] = 4
marker['lat'] = -23.1964709
marker['lon'] = -46.88133920000001

marker.append()

marker['uus'] = 0
marker['uid'] = 2
marker['date'] = 11
marker['type'] = 35
marker['icon'] = 4
marker['lat'] = -23.19627863693154
marker['lon'] = -46.882567405700684

marker.append()

marker['uus'] = 0
marker['uid'] = 3
marker['date'] = 11
marker['type'] = 35
marker['icon'] = 4
marker['lat'] = -23.195722445117852
marker['lon'] = -46.8711519241333

marker.append()
mtable.flush()

news = ntable.row

news['name'] = 'teste1'
news['text'] = 'teste1 testado aqui vish!'
news['uus'] = 0
news['uid'] = 4
news['date'] = 15
news['lat'] = -23.19550943562355
news['lon'] = -46.882063150405884
news['upvt'] = 5
news['dnvt'] = 2

news.append()

news['name'] = 'teste2'
news['text'] = 'sempre testando huEeeE'
news['uus'] = 0
news['uid'] = 5
news['date'] = 53
news['lat'] = -23.197221395034305
news['lon'] = -46.87814712524414
news['upvt'] = 15
news['dnvt'] = 23

news.append()
ntable.flush()
