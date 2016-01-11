#!/usr/bin/python
# test_server.py

import sys
import socket
from struct import *

server = socket.socket()
host = socket.gethostname()
port = 8080
interactive = False

print "Host:", host, ", listening on Port:", port
server.bind((host, port))
print "Server created"

# Now wait for connection
server.listen(5)
print "Wait for connection ..."
client, addr = server.accept()
print "Connected by", addr
print
client.setblocking(1)
while 1:
    try:
        data = client.recv(512)
    except socket.error as e:
        print e.strerror
    else:
        if not data:
            print "Lost Connection, now exit..."
            break
        elif data[3:] == 'exit':
            client.send('close')
            break
        else:
            print "Server: Message received (%d bytes):" % (len(data))
            header = unpack("!hB", data[0:3])
            print "Packet Header:", header
            print "Packet Data:", data[3:]
            #client.send("Message Received (%d bytes)\n" % (len(data)));
            
            msg = "Hello Client"
            packet = pack("!hB%ds" % (len(msg)), len(msg), 0x10, msg)
            client.sendall(packet)
            print "Server: Message sent. (%d bytes)" % (len(packet))
            
    if interactive:
        msg_to_client = raw_input("Enter your message (enter 'exit' to exit):\n")
        #resp = "Hi client, your message is received.\n"
        #bytes = client.send(msg_to_client.encode('utf-8'))
        bytes = client.send(msg_to_client + '\n')
        #bytes = client.send('\x10' + 'Hello')
        print bytes, " bytes are sent to client"
        if msg_to_client == 'exit': break

client.close()
server.close()


