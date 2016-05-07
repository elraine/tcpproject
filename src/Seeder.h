#ifndef SEEDER_H
#define SEEDER_H

//#include "List.h"


typedef struct{
	int portno;	
	int sockfd;
	struct sockaddr_in addr;
	socklen_t clilen;
	char *seeder_IP;
}seeder;

seeder* seeder_init(struct sockaddr_in seeder_addr, int seeder_sockfd, socklen_t c);
int seeder_get_size(seeder *s);
char* seeder_get_info(seeder *s);

char* seeder_to_string(seeder *s);

#endif
