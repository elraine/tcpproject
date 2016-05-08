#define _GNU_SOURCE
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

// gcc -Wall test_tracker.c Tracker.o Seeded_file.o List.o Seeder.o Utils.o -lm

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
    fprintf(stderr, "test init\t\tok\n");
    tracker_free(&track);

}

static void test_tracker_announce(){
    
    fprintf(stderr, "test_tracker_announce :\n");

	char* messageAnnounce = "announce listen 2222 seed [file_c.dat 2097153 1023 8905e92afeb80fc7722ec89eb0bf0967 file_a.dat 2097152 1024 8905e92afeb80fc7722ec89eb0bf0966 file_b.dat 3145728 1536 330a57722ec8b0bf09669a2b35f88e9e]";
	tracker track;
	tracker_init(&track,get_next_port());

	struct sockaddr_in seeder_addr;
	seeder* s = seeder_init(seeder_addr,42, (socklen_t) 45);

	char* reply = tracker_parse_message(messageAnnounce,&track,s);
	tracker_display_seeded_files(&track);
	assert(strcmp("OK",reply)==0);

	tracker_free(&track);
    fprintf(stderr, "test announce\t\tok\n");

}

static void test_tracker_getfile(){

	fprintf(stderr, "test_tracker_getfile :\n");

	char* messageAnnounce1 = "announce listen 2222 seed [file_c.dat 2097153 1023 8905e92afeb80fc7722ec89eb0bf0967 file_a.dat 2097152 1024 8905e92afeb80fc7722ec89eb0bf0966 file_b.dat 3145728 1536 330a57722ec8b0bf09669a2b35f88e9e]";
	char* messageAnnounce2 = "announce listen 2222 seed [file_c.dat 2097153 1023 8905e92afeb80fc7722ec89eb0bf0967]";
	tracker track;
	tracker_init(&track,get_next_port());

	struct sockaddr_in seeder_addr;
	seeder* s1 = seeder_init(seeder_addr,42, (socklen_t) 45);
	seeder* s2 = seeder_init(seeder_addr,48, (socklen_t) 46);

	tracker_parse_message(messageAnnounce1,&track,s1);
	tracker_parse_message(messageAnnounce2,&track,s2);

	char* messageGetFile = "getfile 8905e92afeb80fc7722ec89eb0bf0967";
	seeder* s3 = seeder_init(seeder_addr,49, (socklen_t) 47);

	char* reply = tracker_parse_message(messageGetFile,&track,s3);

	char* resultat_attendu;
	asprintf(&resultat_attendu,"peers 8905e92afeb80fc7722ec89eb0bf0967 [%s:%d %s:%d]",s1->seeder_IP,s1->portno,s2->seeder_IP,s2->portno);
	assert(strcmp(reply,resultat_attendu)==0);
	tracker_free(&track);

    fprintf(stderr, "test getfile\t\tok\n");
}



static void test_tracker_look(){

    fprintf(stderr, "test_tracker_look :\n");

	tracker track;
	tracker_init(&track,get_next_port());

	struct sockaddr_in seeder_addr;
	seeder* s = seeder_init(seeder_addr,42, (socklen_t) 45);

	char* messageAnnounce = "announce listen 2222 seed [file_a.dat 499 53 45578178564785 file_a.dat 500 56 58287524278737 file_b.dat 500 70 467278342837]";
	tracker_parse_message(messageAnnounce,&track,s);

	char* messageLook;
	asprintf(&messageLook,"look [filename=%cfile_a.dat%c filesize<%c501%c]",'"','"','"','"');

	char* reply = tracker_parse_message(messageLook,&track,s);
	assert(strcmp(reply,"list [file_a.dat 499 53 45578178564785 file_a.dat 500 56 58287524278737]")==0);
	tracker_free(&track);
    fprintf(stderr, "test look\t\tok\n");

}

int main()
{
	mylog = fopen(LOGFILE, "w");
	LOG("\n");
	LOG("starting server test_tracker\n");

	test_tracker_init();
	test_tracker_announce();
	test_tracker_getfile();
	test_tracker_look();
	fclose(mylog);

	return 0;
}
