#ifndef TRACKER_H
#define TRACKER_H

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
int tracker_store_info_seeded(char *files, tracker *t, int portno);
char *tracker_search_files(tracker *t, char *criteria);
void tracker_parse_message(char* mess, tracker* t);

#endif
