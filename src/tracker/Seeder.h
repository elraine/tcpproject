#ifndef SEEDER_H
#define SEEDER_H
#include "List.h"

typedef struct{
	int portno;
	int portnoListen;
	int sockfd;
	struct sockaddr_in addr;
	socklen_t clilen;
	char *seeder_IP;
}seeder;

seeder* seeder_init(struct sockaddr_in seeder_addr, int seeder_sockfd, socklen_t c);

void seeder_set_IP(seeder *s,char*ip);

char* seeder_get_info(seeder *s);
char* seeder_to_string(seeder *s);
void seeder_free(seeder *s);
int seeder_is_equals(seeder *s1, seeder *s2);
element* seeder_remove_from_list(list* l, seeder* s);

#endif
