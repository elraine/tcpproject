CFLAGS=-std=c99 -Wall -g -D_GNU_SOURCE
CC=gcc

JCCFLAGS =
JFLAGS= -ea
JCC=javac

.SUFFIXES: .java .class

CLASSES = Client.java Fichier.java

.java.class:
		$(JCC) $(JCCFLAGS) $*.java

all: default
default: server client

#___________________________________________________SERVER


server: Main.c Utils.c Tracker.c List.o Seeder.o Seeded_file.o
	$(CC) $(CFLAGS) -o $@ $^ -pthread

Utils.o: Utils.c
	$(CC) $(CFLAGS) -o Utils.o -c Utils.c

Tracker.o: Tracker.c
	$(CC) $(CFLAGS) -o Tracker.o -c Tracker.c

List.o: List.c
	$(CC) $(CFLAGS) -o List.o -c List.c

Seeder.o: Seeder.c
	$(CC) $(CFLAGS) -o Seeder.o -c Seeder.c

Seeded_file.o: Seeded_file.c
	$(CC) $(CFLAGS) -o Seeded_file.o -c Seeded_file.c

#___________________________________________________CLIENT

client: fichier
	$(JCC) $(JCFLAGS) Client.java

fichier:
	$(JCC) $(JCFLAGS) Fichier.java

classes: $(CLASSES:.java=.class)

clean:
	rm -rf *.o *.class *.dSYM a.out


test: Seeded_file.o Seeder.o Tracker.o List.o
	gcc -Wall test_tracker.c Tracker.o Seeded_file.o Seeder.o List.o
