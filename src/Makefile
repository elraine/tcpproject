CFLAGS=-Wall -g -D_GNU_SOURCE
CC=gcc

JCCFLAGS =
JFLAGS= -ea
JCC=javac

.SUFFIXES: .java .class

CLASSES = \
        	Server.java \
	        Client.java

.java.class:
		$(JCC) $(JCCFLAGS) $*.java


all:default

default:server client

server:server.c
	$(CC) $(CFLAGS) -o $@ $^

client:client.java
	$(JCC) $(JCFLAGS) $*.java

classes: $(CLASSES:.java=.class)



clean:
	rm -f 
