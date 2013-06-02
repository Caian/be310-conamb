#!/usr/bin/python

import socket, threading, unisc, unicli, sys

cfg = open('unilink.conf', 'rt')
port = int(cfg.readline().strip())
unicli.download_dir = cfg.readline().strip()
unicli.resource_dir = cfg.readline().strip()
cfg.close()

print 'Unilink prototype server v0.4a'

server = unisc.UnilinkServerCore()
server.start(port)

while 1:
	sys.stdout.write('>> ')
	str = sys.stdin.readline().replace('\n','')
	if str == 'quit':
		break

server.stop()
