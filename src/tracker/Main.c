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

tracker track;

void *connection_handler(void *s){

	seeder* seed = (seeder*)s;
	
	char buffer[BUFFERSIZE];
	int nb_read;	
	char* mess_bienv = "Bienvenue sur le tracker, vous pouvez maintenant rechercher des fichiers par critères ou par key";
	printf("Nouveau Client \t: %s:%d\n",seed->seeder_IP,seed->portno);
	LOG("Nouveau Client \t: %s:%d\n",seed->seeder_IP,seed->portno);

	while( (nb_read = recv(seed->sockfd, buffer , BUFFERSIZE , 0)) > 0 ){
		
		printf("message recu \t: %s\n",buffer);
		char* reply = tracker_parse_message(buffer,&track,seed);
		if(strcmp(reply,"error")==0){
			LOG("server : unknown command\n");
		}

		printf("reponse envoyé\t: %s\n",reply);		
		write(seed->sockfd , reply , strlen(reply));

		memset(buffer,(char)'\0',BUFFERSIZE);
	}

	if(nb_read==0){
		LOG("Client %s:%d disconnected\n",seed->seeder_IP,seed->portno);
        LOG("Client %s:%d disconnected\n",seed->seeder_IP,seed->portno);
        fflush(stdout);
	}
	return 0;
}


#define param 1
int main(int argc, char *argv[]){

	if (argc != param+1) usage(argv[0]);

	mylog = fopen(LOGFILE, "w");
	LOG("\n");
	LOG("starting server %d\n", atoi(argv[1]));
	printf("Starting server %d\n", atoi(argv[1]));

	int c, seeder_sockfd;
	struct sockaddr_in seeder_addr;
	pthread_t thread_id;
	tracker_init(&track,atoi(argv[1]));

	listen(track.sockfd, 5);
    c = sizeof(struct sockaddr_in);

	while( (seeder_sockfd = accept(track.sockfd, (struct sockaddr *)&seeder_addr, (socklen_t*)&c)) ){

		LOG("server : Connection accepted\n");

		seeder* s = seeder_init(seeder_addr, seeder_sockfd, c);
		tracker_add_seeder(&track,s);
		
        if( pthread_create( &thread_id , NULL ,  connection_handler , s) < 0)
        {

            LOG("server : ERROR on creating thread");
            return 1;
        }

        //pthread_join( thread_id , NULL);
        LOG("server : Handler assigned");
    }

    if (seeder_sockfd < 0)
    {
        error("ERROR on accepting connexion");
        return 1;
    }

	fclose(mylog);
    tracker_free(&track);

    return 0;
}
