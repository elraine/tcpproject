/* A simple server in the internet domain using TCP
   The port number is passed as an argument */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <strings.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include <arpa/inet.h>
#include "Seeded_file.h"
#include "Seeder.h"
#include "List.h"

#define BUFFERSIZE 2000

void error(const char *msg) {
    perror(msg);
    exit(1);
}

typedef struct{
	int portno;
	int sockfd;
	struct sockaddr_in addr;

	list* seeded_files;

}tracker;

void *connection_handler(void *sockfd){

	seeder seed;
	seed.sockfd = *(int*)sockfd;

	char buffer[BUFFERSIZE];
	int nb_read;

	char* essai = "coucou me voila";
	write(seed.sockfd , essai , strlen(essai));
  printf("sockfd : %d",seed.sockfd);
	while( (nb_read = recv(seed.sockfd, buffer , BUFFERSIZE , 0)) > 0 ){
		//parse_message(buffer);
		printf("message reçu : %s\n",buffer);
    memset(buffer,(int)' ',BUFFERSIZE);
	}

	if(nb_read==0){
		//connection terminée
	}
	return 0;
}

void parse_message(char* mess){
	char *tmp;
	tmp = strtok(mess," ");

	/*if(strcmp(tmp, "announce")){


	}
	else if(strcmp(tmp, "look"){


	}
	else if(strcpmp(tmp,"getfile"){

	}
	else{
		perror("Message non reconnu");
	}
*/
	//tmp = strtok(line," ");
	//tmp = strtok(NULL," ");
	//p->x=atof(tmp);
}

void tracker_init(tracker *t, int portno){

	t->sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (t->sockfd < 0)
		error("ERROR opening socket");
	puts("Socket created");

	bzero((char *) &(t->addr), sizeof(t->addr));
	t->addr.sin_family = AF_INET;
    t->addr.sin_addr.s_addr = INADDR_ANY;
    t->addr.sin_port = htons(portno);

    if (bind(t->sockfd, (struct sockaddr *) &(t->addr), sizeof(t->addr)) < 0)
        error("ERROR on binding");

	puts("Binding done");

	t->seeded_files = list_empty();

}

void usage (char *s){
 	fprintf(stderr, "Usage: %s <portno>\n", s);
 	exit(EXIT_FAILURE);
}

#define param 1
int main(int argc, char *argv[]){

	int c, seeder_sockfd;
	struct sockaddr_in seeder_addr;

	if (argc != param+1)
		usage(argv[0]);

	tracker track;
	tracker_init(&track,atoi(argv[1]));

	listen(track.sockfd, 5);

    c = sizeof(struct sockaddr_in);
	pthread_t thread_id;

	while( (seeder_sockfd = accept(track.sockfd, (struct sockaddr *)&seeder_addr, (socklen_t*)&c)) ){

		puts("Connection accepted");

        if( pthread_create( &thread_id , NULL ,  connection_handler , (void*) &seeder_sockfd) < 0)
        {
            error("ERROR on creating thread");
            return 1;
        }

        //pthread_join( thread_id , NULL);
        puts("Handler assigned");
    }

    if (seeder_sockfd < 0)
    {
        error("ERROR on accpeting connection");
        return 1;
    }

    return 0;
}
