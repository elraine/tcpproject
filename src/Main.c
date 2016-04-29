/* A simple server in the internet domain using TCP
   The port number is passed as an argument */

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

#include "Seeded_file.h"
#include "Seeder.h"
#include "List.h"
#include "Tracker.h"
#include "Utils.h"
#define BUFFERSIZE 2000


void *connection_handler(void *s){

	seeder* seed = (seeder*)s;
	
	printf("essai portno : %d\n", seed->portno);
	printf("essai addr IP : %s\n", inet_ntoa( seed->addr.sin_addr));

	char buffer[BUFFERSIZE];
	int nb_read;
	char* essai = "coucou me voila";
	
	write(seed->sockfd , essai , strlen(essai));
	
	while( (nb_read = recv(seed->sockfd, buffer , BUFFERSIZE , 0)) > 0 ){
		
		//parse_message(buffer);
		printf("message reçu : %s",buffer);
		memset(buffer,(char)'\0',BUFFERSIZE);
	}

	if(nb_read==0){
		//connection terminée
	}
	return 0;
}


#define param 1
int main(int argc, char *argv[]){

	if (argc != param+1) usage(argv[0]);

	int c, seeder_sockfd;
	struct sockaddr_in seeder_addr;
	tracker track;
	pthread_t thread_id;
	
	tracker_init(&track,atoi(argv[1]));
	listen(track.sockfd, 5);
    c = sizeof(struct sockaddr_in);

	while( (seeder_sockfd = accept(track.sockfd, (struct sockaddr *)&seeder_addr, (socklen_t*)&c)) ){

		puts("Connection accepted");
		
		seeder* s = seeder_init(seeder_addr, seeder_sockfd, c);
		tracker_add_seeder(&track,s);
		
        if( pthread_create( &thread_id , NULL ,  connection_handler , s) < 0)
        {
            error("ERROR on creating thread");
            return 1;
        }

        //pthread_join( thread_id , NULL);
        puts("Handler assigned");
    }

    if (seeder_sockfd < 0)
    {
        error("ERROR on accepting connexion");
        return 1;
    }

    return 0;
}
