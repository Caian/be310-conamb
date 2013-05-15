#! /usr/bin/python

import unidb

db = unidb.get_database()

uid = db.post_marker(0,11,35,4,-23.1964709,-46.88133920000001)
print uid
uid = db.post_marker(0,32,62,2,-23.19627863693154,-46.882567405700684)
print uid
uid = db.post_marker(0,14,43,7,-23.195722445117852,-46.8711519241333)
print uid

uid = db.post_news(0,15,'teste1','teste1 testado aqui vish!',-23.19550943562355,-46.882063150405884)
print uid
db.up_vote(0,uid,5)
db.dn_vote(0,uid,2)
uid = db.post_news(0,53,'teste2','sempre testando huEeeE',-23.197221395034305,-46.87814712524414)
print uid
db.up_vote(0,uid,15)
db.dn_vote(0,uid,23)

