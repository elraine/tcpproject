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


//returns a seeding_file* matching a key
//returns NULL if not match is found
//key is supposed unique (=> the first match found is returned)
seeded_file* tracker_get_matching_seeded_files(tracker *t, char* key){
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
char* tracker_search_seeders(tracker *t, char* key){
	seeded_file* match = malloc(sizeof(seeded_file));
	match = tracker_get_matching_seeded_files(t,key);

	char* ret=seeded_file_get_info(match);
	return ret;
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
    t->portno = ntohs(t->addr.sin_port);

    if (bind(t->sockfd, (struct sockaddr *) &(t->addr), sizeof(t->addr)) < 0)
        error("ERROR on binding");

	puts("Binding done");

	t->seeded_files = list_empty();
	t->seeders = list_empty();

}

void tracker_add_seeder(tracker* t, seeder* s){
	list_add_head(t->seeders , element_init(s));
}


int tracker_store_info_seeded(char *files, tracker *t, int portno){          //probably replace portno by seeder.
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
Parses messages mess sent by the client, and stores the information in tracker t.
Current version assumes correct syntax in the messages sent.
*/
char* tracker_parse_message(char* mess, tracker* t){

	printf("PARSE : %s\n",mess);

	char* reply;

	char* message_parsed = malloc(strlen(mess));
    strcpy(message_parsed, mess);

	char *tmp= strtok(message_parsed," ");

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
			//TODO decommenter lignes en dessous (fuck le warning de "set but not used")
			//char *leeched;
			//leeched = removeFirstCharacter(strtok(NULL, "]")); //leeched = listOfFiles
		} //else no file leeched.

		tracker_store_info_seeded(seeded, t, portno);
		free(seeded);
		//TODO store info leeched if leeched == 1
		reply = "OK";
	}
	else if(strcmp(tmp, "look") ==0){
		tmp = removeFirstCharacter(strtok(NULL, "]"));
		reply = tracker_search_files(t, tmp);
		//TODO send reply to client
		//free(reply);
	}
	else if(strcmp(tmp,"getfile") ==0){
		tmp = strtok(NULL, " ");
		reply = tracker_search_seeders(t, tmp);
		//TODO send reply to client
		//free(reply);	
	} else {
		reply = "Message non reconnu";
	}
	return reply;
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
char *tracker_search_files(tracker *t, char *criteria){
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

void tracker_free(tracker *t){

	list_free(t->seeded_files);
	list_free(t->seeders);

}