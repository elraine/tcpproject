#ifndef TRACKER_H
#define TRACKER_H
#include "Seeder.h"
#include "Seeded_file.h"
#include <sys/socket.h>


typedef struct{
	int portno;
	int sockfd;
	struct sockaddr_in addr;
	list* seeded_files;
	list* seeders;
}tracker;

seeded_file* tracker_get_matching_seeded_files(tracker *t, char* key);
char* tracker_search_seeders(tracker *t, char* key);
void tracker_init(tracker *t, int portno);
void tracker_add_seeder(tracker* t, seeder* s);
int tracker_store_info_seeded(char *files, tracker *t, seeder* s);
char *tracker_search_files(tracker *t, char *criteria);

char* tracker_parse_message(char* mess, tracker* t, seeder* s);

void tracker_display_seeded_files(tracker* t);

void tracker_free(tracker *t);

#endif
