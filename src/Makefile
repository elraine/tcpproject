CFLAGS=-std=c99 -Wall -g -D_GNU_SOURCE
CC=gcc

JCCFLAGS =
JFLAGS= -ea
JCC=javac

.SUFFIXES: .java .class

CLASSES = Client.java Fichier.java

.java.class:
		$(JCC) $(JCCFLAGS) $*.java

all:default

default:server client

server:Tracker.c List.o SeededF.o
	$(CC) $(CFLAGS) -o $@ $^ -pthread

client: fichier
	$(JCC) $(JCFLAGS) Client.java

fichier:
		$(JCC) $(JCFLAGS) Fichier.java

List.o:List.c
	$(CC) $(CFLAGS) -o List.o -c List.c

SeededF.o:Seeded_file.c
	$(CC) $(CFLAGS) -o SeededF.o -c Seeded_file.c

classes: $(CLASSES:.java=.class)



clean:
	rm -rf *.o *.class *.dSYM a.out
