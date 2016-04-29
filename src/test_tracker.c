#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include <arpa/inet.h>
#include <assert.h>

#include "List.h"
#include "Tracker.h"
#include "Seeded_file.h"
#include "Seeder.h"

// gcc -Wall test_tracker.c Tracker.o Seeded_file.o List.o

static int int_port = 2000;

int get_next_port(){
	int_port++;
	return int_port;
}

static void test_tracker_init(){
    
    fprintf(stderr, "test_tracker_init :\n");

	tracker track;
	tracker_init(&track,get_next_port());
	assert(track.portno==int_port);
	assert(track.seeded_files->head==NULL);
	assert(track.seeders->head==NULL);
    fprintf(stderr, "test ok\n");

}

static void test_tracker_parser(){

    fprintf(stderr, "test_tracker_parse :\n");

	char* messageAnnounce = "announce listen 2222 seed [file_a.dat 2097152 1024 8905e92afeb80fc7722ec89eb0bf0966 file_b.dat 3145728 1536 330a57722ec8b0bf09669a2b35f88e9e]";
	//char* messageGetFile = "getfile test no message";
	//char* messageLook = "look test no message";
	//char* messageOther = "plop test no message";

	tracker track;
	tracker_init(&track,get_next_port());
	
	char* reply = tracker_parse_message(messageAnnounce,&track);
	printf("%s\n",reply);
	assert(strcmp("OK",reply)==0);

	//tracker_free(&track);
    fprintf(stderr, "test ok\n");

}


static void test_tracker_announce(){
    
    fprintf(stderr, "test_tracker_announce :\n");

	char* messageAnnounce = "announce listen 2222 seed [file_a.dat 2097152 1024 8905e92afeb80fc7722ec89eb0bf0966 file_b.dat 3145728 1536 330a57722ec8b0bf09669a2b35f88e9e]";
	tracker track;
	tracker_init(&track,get_next_port());
	tracker_parse_message(messageAnnounce,&track);



    fprintf(stderr, "test ok\n");

	tracker_free(&track);
}

int main()
{

	test_tracker_init();
	test_tracker_parser();
	test_tracker_announce();

	return 0;
}
