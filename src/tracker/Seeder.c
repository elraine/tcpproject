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
	s->portnoListen = -1;
	s->addr = seeder_addr;
	s->sockfd = seeder_sockfd;
	s->portno = ntohs(seeder_addr.sin_port);
	s->seeder_IP = inet_ntoa(seeder_addr.sin_addr);
	return s;
}

//returns a char containt for a seeder s IP:port
char* seeder_get_info(seeder *s){
	char *ret;
	asprintf(&ret,"%s:%d",s->seeder_IP,s->portnoListen);
	return ret;
}

//set an IP for a seeder (used for the tests)
void seeder_set_IP(seeder *s,char*ip){
	s->seeder_IP= ip;
}

//returns a string containning the data of a seeder (used to display data)
char* seeder_to_string(seeder *s){
   char* buffer;
   asprintf(&buffer,"seeder : %s:%d",s->seeder_IP,s->portnoListen);
   return buffer;
}

void seeder_free(seeder *s){
}

//the equality of two seeders depends on the IP addr
int seeder_is_equals(seeder *s1, seeder *s2){
	return (strcmp(s1->seeder_IP,s2->seeder_IP)==0);
}

element* seeder_remove_from_list(list* l, seeder* s){
	element* e = l->head;
	element* prev= NULL;

	while(!list_is_end_mark(e)){
		if(seeder_is_equals((seeder*)e->data,s)){
			if(prev==NULL){
				l->head=e->next;
				return e;
			}
			else{
				prev->next=e->next;
				return e;
			}
		}
		prev = e;
		e = e->next;	
	}
	return e;
}


