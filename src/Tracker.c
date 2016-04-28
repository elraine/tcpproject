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
#include "Parser.h"

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
	list* seeders;
}tracker;

seeder* seeder_init(struct sockaddr_in seeder_addr, int seeder_sockfd, socklen_t c){
	seeder* s = malloc(sizeof(seeder));
	s->clilen = c;
	s->addr = seeder_addr;
	s->sockfd = seeder_sockfd;
	s->portno = ntohs(seeder_addr.sin_port);	
	return s;
}

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

int storeInfoSeeded(char *files, tracker *t, int portno){          //probably replace portno by seeder.
	if (files[0] != '\0'){
		char* tmp;
		tmp = strtok(files," "); //tmp = filename1
		while(tmp != NULL){
			//seeder *seed = initSeederWithInfo();              need seeder

			/*
			file is a temporary seeded_file, used to check if the files uploaded by the user already exists in the database
			*/
			seeded_file *file = malloc(sizeof(seeded_file));    
			file->file_name = tmp;
			
			tmp = strtok(NULL," ");
			file->file_length = atoi(tmp);

			tmp = strtok(NULL," ");
			file->piece_size = atoi(tmp);

			tmp = strtok(NULL," ");
			file->key = tmp;

			element* search = list_sf_find(t->seeded_files, file);
			if (search != NULL){
				//list_add_head(search->seeders, seed);              need seeder.
				free(file);             //temporary file is already in database : we can free it
			} else {
				file->seeders = list_empty();
				//list_add_head(file->seeders, seed);               need seeder
				
				element* el=element_init(file);
				list_add_head(t->seeded_files, el);
			}

			tmp = strtok(NULL," ");
		}
	}
	return 0;
}

/*
Returns a string containing all seeded files verifying the listed criteria.
Filename is required.
Authorised criteria are :
filename="..."
filesize>"..." | filesize<"..."           One of the two or none.
piecesize>"..." | piecesize<"..."		  One of the two or none.

TODO : want to return a string following that sequence :

*/
char *searchFiles(tracker *t, char *criteria){
	char* crit[3];
	int i=0;
	char *name;
	char *tmp;
	int fileSize=0;
	int pieceSize=0;
	int isBiggerFile=0;
	int isBiggerPiece=0;

	crit[0] = strtok(criteria, " ");

	while (crit[i] != NULL){
		i++;
		crit[i] = strtok(NULL, " ");
	}
	for(int j=0; j<i-1; j++){
		tmp = strtok(crit[i], "\"");
		if (strcmp(tmp, "filename=") ==0){           //then criteria is filename
			name = strtok(NULL, "\"");
		} else if (strcmp(tmp, "filesize<") ==0){   //then criteria is filesize<
			isBiggerFile = -1;
			fileSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp, "filesize>") ==0){    //then criteria is filesize>
			isBiggerFile = 1;
			fileSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp, "piecesize<") ==0){    //then criteria is piecesize<
			isBiggerPiece = -1;
			pieceSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp, "piecesize>") ==0){    //then criteria is piecesize<
			isBiggerPiece = 1;
			pieceSize = atoi(strtok(NULL, "\""));
		}
	}

	/*
	Establish list of files matching criteria
	*/
	list *matchingFiles =list_empty();
	element *current = t->seeded_files->head;
	while(!list_is_end_mark(current)){
		if( (strcmp(getSfFilename(current->data), name) ==0) && (isBiggerInt(getSfFilesize(current->data), fileSize, isBiggerFile)) && (isBiggerInt(getSfPiecesize(current->data), pieceSize, isBiggerPiece)) ){
			list_add_head(matchingFiles, current);
		}
		current = current->next;
	}

	/*
	Create a string ret = list[fileinfo1 fileinfo2]
	*/	
	current = matchingFiles->head;
	int retSize =0;
	while (!list_is_end_mark(current)){
	  retSize += sfSize(current->data);
	  current = current->next;
	}
	retSize +=7;

	current = 	matchingFiles->head;
	char *ret = malloc(retSize*sizeof(char));
	strcpy(ret, "list [");

	while (!list_is_end_mark(current->next)){
	  char* tmp = sfToChar(current->data);
	  strcat(ret, tmp);
	  strcat(ret, " ");
	  current = current->next;
	}
	if(!list_is_end_mark(current)){             //avoids having an extra " " before "]"
		char* tmp = sfToChar(current->data);
		strcat(ret, tmp);
	}
	strcat(ret, "]");
	
	return ret;
}


//returns the size required to write ONE seeder info: 'IP:port'+1char (space or ']')
int getSeederSize(seeder *s){
   int size=0;
   size+= stringSize(itoa(s->portno));
   size+= stringSize(s->seeder_IP);
   size+= 2;
   return size;
}

//returns the size required to write ALL seeders : 'IP:port IP:port]\0' 
int getSeedersSize(seeded_file *s){
	int size=0;
	element *current = malloc(sizeof(element));
	current = s->seeders->head;
	while(!list_is_end_mark(current)){
		size+=getSeederSize(current->data);
		current=current->next;
	}

	size++;                                                   //space for '\0'

	return size;
}

//returns a char containt for a seeder s IP:port
char* getSeederInfo(seeder *s){
	int size= getSeederSize(s);
	char *ret = malloc(sizeof(char)*size);

	strcpy(ret, itoa(s->portno));
	strcat(ret, inet_ntoa(s->addr.sin_addr));
	strcat(ret, " \0");

	return ret;
}


char *getSeedersInfo(seeded_file *s){
	int size=getSeedersSize(s);
	size+=stringSize(s->key);
	char* ret=malloc(sizeof(char)*(size+8));   //8='peers ' (key) ' [' 

	strcpy(ret, "peers ");
	strcat(ret, s->key);
	strcat(ret, " [");

	element *current = malloc(sizeof(element));
	current = s->seeders->head;
	while(!list_is_end_mark(current)){
		strcat(ret, getSeederInfo(current->data));
	}

	strcat(ret,"]");

	return ret;
}

//returns a seeding_file* matching a key
//returns NULL if not match is found
//key is supposed unique (=> the first match found is returned)
seeded_file* getSeededFileMatch(tracker *t, char* key){
	element* current= malloc(sizeof(element));
	current = t->seeded_files->head;

	while(!list_is_end_mark(current)){
		if(strcmp(getSfKey(current->data), key)==0){
			return current->data;
		}
		current=current->next;
	}
	return NULL;
}

/*
Returns a list of seeders having file corresponding to key

peers $key$ [seederInfo(1) seederInfo(2)]
*/
char* searchSeeders(tracker *t, char* key){
	seeded_file* match = malloc(sizeof(seeded_file));
	match = getSeededFileMatch(t,key);

	char* ret=getSeedersInfo(match);
	return ret;
}


/*
Parses messages mess sent by the client, and stores the information in tracker t.
Current version assumes correct syntax in the messages sent.
*/
void parse_message(char* mess, tracker* t){
	char *tmp;
	tmp = strtok(mess," ");
	
	if(strcmp(tmp, "announce")==0){ //tmp = announce
		int portno;
		tmp = strtok(NULL," "); //tmp = listen
		tmp = strtok(NULL," "); //tmp = portno
		portno = atoi(tmp);

		tmp = strtok(NULL, " "); //tmp = seed;
		char *seeded;
		seeded = removeFirstCharacter(strtok(NULL, "]")); //seeded = listOfFiles

		tmp = strtok(NULL, " ");
		if (tmp[0] == 'l'){
			char *leeched;
			leeched = removeFirstCharacter(strtok(NULL, "]")); //leeched = listOfFiles
		} //else no file leeched.

		storeInfoSeeded(seeded, t, portno);
		free(seeded);
		//TODO store info leeched if leeched == 1
	}
	else if(strcmp(tmp, "look") ==0){
		tmp = removeFirstCharacter(strtok(NULL, "]"));
		char* reply = searchFiles(t, tmp);
		//TODO send reply to client
		free(reply);
	}
	else if(strcmp(tmp,"getfile") ==0){
		tmp = strtok(NULL, " ");
		char* reply = searchSeeders(t, tmp);
		//TODO send reply to client
		free(reply);	
	} else {
		perror("Message non reconnu");
	}
}


void tracker_init(tracker *t, int portno){

	t->sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (t->sockfd < 0)
		error("ERROR opening socket");
	puts("Socket created");

	bzero((char *) &(t->addr), sizeof(t->addr));
	t->addr.sin_family = AF_INET;
    t->addr.sin_addr.s_addr = htonl(INADDR_ANY);
    t->addr.sin_port = htons(portno);

    if (bind(t->sockfd, (struct sockaddr *) &(t->addr), sizeof(t->addr)) < 0)
        error("ERROR on binding");

	puts("Binding done");

	t->seeded_files = list_empty();
	t->seeders = list_empty();

}

void tracker_add_seeder(tracker* t, seeder* s){
	list_add_head(t->seeders , element_init(s));
}

void usage (char *s){
 	fprintf(stderr, "Usage: %s <portno>\n", s);
 	exit(EXIT_FAILURE);
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
