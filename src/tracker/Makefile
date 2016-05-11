CFLAGS=-std=c99 -Wall -g -D_GNU_SOURCE
CC=gcc

.SUFFIXES: .java

all: clean default
default: serverc

#___________________________________________________SERVER


serverc: clean server 
	
server: Main.c Utils.o Tracker.o List.o Seeder.o Seeded_file.o
	$(CC) $(CFLAGS) -o $@ $^ -lm -pthread

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

clean:
	rm -rf *.o *.class *.dSYM a.out

test: clean test_tracker.c Utils.o Tracker.o List.o Seeder.o Seeded_file.o
	gcc -Wall test_tracker.c Tracker.o Seeded_file.o List.o Seeder.o Utils.o -lm -o test_tracker