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

typedef struct{
	int portno;	
	int sockfd;
	struct sockaddr_in addr;
	socklen_t clilen;

	char *seeder_IP;
}seeder;




void error(const char *msg) {
    perror(msg);
    exit(1);
}

typedef struct{
	int portno;
	int sockfd;
	struct sockaddr_in addr;
	
}tracker;

void *connection_handler(void *sockfd){
	
	seeder seed;
	seed.sockfd = *(int*)sockfd;
	
	char buffer[2000];
	int nb_read;
	
	char* essai = "coucou me voila";
	write(seed->sockfd , essai , strlen(essai));
	
	while( (nb_read = recv(seed.sockfd, client_message , 2000 , 0)) > 0 ){
		//TODO read
	}
	
	if(nb_read==0){
		//connection terminée
	}
	return 0;
}

/*
void parse_message(char* mess){
	char *tmp;
	tmp = strtok(mess," ");
	
	if(strcmp(tmp, "announce"){
		
		
	}
	else if(strcmp(tmp, "look"){
		
		
	}
	else if(strcpmp(tmp,"getfile"){
		
	}
	else{
		perror("Message non reconnu");
	}
	
	//tmp = strtok(line," ");
	//tmp = strtok(NULL," ");
	//p->x=atof(tmp);
}*/

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
	
    /*
    char buffer[256];
    int n;
    
    listen(sockfd, 5);
    clilen = sizeof(cli_addr);
    newsockfd = accept(sockfd,(struct sockaddr *) &cli_addr,&clilen);
    
    if (newsockfd < 0)
        error("ERROR on accept");
        
    bzero(buffer, 256);
    n = read(newsockfd, buffer, 255);
    
    if (n < 0)
		error("ERROR reading from socket");
		
    printf("Here is the message: %s\n", buffer);
    n = write(newsockfd, "I got your message", 18);
    
    if (n < 0)
		error("ERROR writing to socket");
		
    close(newsockfd);
    close(sockfd);*/
    

