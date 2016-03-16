#ifndef SEEDER_H
#define SEEDER_H

typedef struct{
	int portno;	
	int sockfd;
	struct sockaddr_in addr;
	socklen_t clilen;

	char *seeder_IP;
}seeder;


#endif
