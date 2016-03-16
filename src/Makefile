CFLAGS=-Wall -g -D_GNU_SOURCE
CC=gcc

JCCFLAGS =
JFLAGS= -ea
JCC=javac

.SUFFIXES: .java .class

CLASSES = Client.java

.java.class:
		$(JCC) $(JCCFLAGS) $*.java

all:default

default:server client

server:Tracker.c List.o
	$(CC) $(CFLAGS) -o $@ $^ -pthread

client: fichier
	$(JCC) $(JCFLAGS) Client.java

fichier:
		$(JCC) $(JCFLAGS) Fichier.java

List.o:List.c
	$(CC) $(CFLAGS) -o List.o -c List.c
classes: $(CLASSES:.java=.class)



clean:
	rm -rf *.o *.class *.dSYM a.out
