#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <strings.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "Seeded_file.h"
#include "Seeder.h"


seeder* seeder_init(struct sockaddr_in seeder_addr, int seeder_sockfd, socklen_t c){
	seeder* s = malloc(sizeof(seeder));
	s->clilen = c;
	s->addr = seeder_addr;
	s->sockfd = seeder_sockfd;
	s->portno = ntohs(seeder_addr.sin_port);
	s->seeder_IP = inet_ntoa(seeder_addr.sin_addr);
	return s;
}


//returns the size required to write ONE seeder info: 'IP:port'+1char (space or ']')
int seeder_get_size(seeder *s){
   int size=0;
   size+= strlen(itoa(s->portno));
   size+= strlen(s->seeder_IP);
   size+= 2;
   return size;
}

//returns a char containt for a seeder s IP:port
char* seeder_get_info(seeder *s){
	
	char *ret;
	asprintf(&ret,"%s:%d",s->seeder_IP,s->portno);
	return ret;
}

char* seeder_to_string(seeder *s){

   char* buffer;
   asprintf(&buffer,"seeder : %d:%s",s->sockfd,s->seeder_IP);
   return buffer;
}

