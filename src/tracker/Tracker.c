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


/*
initialisation of a tracker given a port number
*/
void tracker_init(tracker *t, int portno){

	t->sockfd = socket(AF_INET, SOCK_STREAM, 0);

	if (t->sockfd < 0)
		error("ERROR opening socket");

	LOG("server : Socket created\n");

	bzero((char *) &(t->addr), sizeof(t->addr));
	t->addr.sin_family = AF_INET;
    t->addr.sin_addr.s_addr = htonl(INADDR_ANY);
    t->addr.sin_port = htons(portno);
    t->portno = ntohs(t->addr.sin_port);

    if (bind(t->sockfd, (struct sockaddr *) &(t->addr), sizeof(t->addr)) < 0)
        error("ERROR on binding");

	LOG("server : Binding done\n");

	t->seeded_files = list_empty();
	t->seeders = list_empty();

}

/*
returns a seeding_file* matching a key
returns NULL if not match is found
key is supposed unique (=> the first match found is returned)
*/
seeded_file* tracker_get_matching_seeded_files(tracker *t, char* key){

	element* current = t->seeded_files->head;
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
	seeded_file* matching_file = tracker_get_matching_seeded_files(t,key);
	char* ret=seeded_file_get_info(matching_file);
	return ret;
}

/*
Adds a seeder to the seeders list of the tracker
*/
void tracker_add_seeder(tracker* t, seeder* s){
	
	int present;
	element* current = t->seeders->head;
	if(!list_is_end_mark(current)){
		present=0;
		while(!list_is_end_mark(current) && !present){

			if(seeder_is_equals(s,current->data))
				present = 1;
			current=current->next;
		}
	}
	if (!present)
		list_add_head(t->seeders , element_init(s));
}

/*
Given a string of files and a seeder, stores the files into the tracker
*/
int tracker_store_info_seeded(char *files, tracker *t, seeder* seed){
	if (files[0] != '\0'){
		char* tmp;
		tmp = strtok(files," ");

		while(tmp != NULL){
			//file is a temporary seeded_file, used to check if the files uploaded by the user already exists in the database
			char* tmp_name = tmp;
			int tmp_file_length = atoi(strtok(NULL," "));
			int tmp_piece_size = atoi(strtok(NULL," "));
			char* tmp_key = strtok(NULL," ");

			seeded_file* file = seeded_file_init(tmp_name, tmp_file_length,tmp_piece_size, tmp_key);

			element* search = seeded_file_find(t->seeded_files, file);

			if (search != NULL){
				seeded_file* sf = search->data;
				//si le seeder n'a pas ce fichier, on rajoute le seeder dans sf->seeders
				element* seeder_search = seeded_file_seeder_find(sf->seeders,seed);

				if(seeder_search == NULL)
					seeded_file_add_seeder(sf,seed);

			} else {
				file->seeders = list_empty();
				seeded_file_add_seeder(file,seed);
				element* el = element_init(file);
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
char* tracker_parse_message(char* mess, tracker* t, seeder* s){

	LOG("server message recu : %s\n",mess);
	char* reply;
	char* message_parsed = malloc(strlen(mess));
    strcpy(message_parsed, mess);

	char *tmp= strtok(message_parsed," ");
	if(strcmp(tmp, "announce")==0){

		tmp = strtok(NULL," "); //tmp = listen
		tmp = strtok(NULL," "); //tmp = portno
		s->portnoListen = atoi(tmp);
		tmp = strtok(NULL," "); //tmp = seed;

		char *seeded = removeFirstCharacter(strtok(NULL, "]")); //seeded = listOfFiles
		tracker_store_info_seeded(seeded, t, s);

		reply = "ok";
	}
	else if(strcmp(tmp, "look") ==0){
		tmp = removeFirstCharacter(strtok(NULL, "]"));
		reply = tracker_search_files(t, tmp);
	}
	else if(strcmp(tmp,"getfile") ==0){
		tmp = strtok(NULL, " ");
		reply = tracker_search_seeders(t, tmp);
	} else {
		reply = "error";
	}
	//free(message_parsed);
	return reply;
}

/**
Returns a string containing all seeded files verifying the listed criteria.
Authorised criteria are :
filename="..."
filesize>"..." | filesize<"..."           One of the two or none.
piecesize>"..." | piecesize<"..."		  One of the two or none.
**/
char *tracker_search_files(tracker *t, char *criteria){

	char* delimiteur;
	asprintf(&delimiteur,"%c",'"');

	char* crit[3];
	int nb_criteres=0;

	char *name = NULL;
	int fileSize=0;
	int pieceSize=0;
	int isBiggerFile=0;
	int isBiggerPiece=0;


	char *tmp_criteria = strtok(criteria," "); //tmp = filename1

	while(tmp_criteria != NULL){
		crit[nb_criteres]=malloc(strlen(tmp_criteria));
		strcpy(crit[nb_criteres],tmp_criteria);
		nb_criteres++;
		tmp_criteria = strtok(NULL," ");
	}

	char* tmp_criteria_field;

	for(int i=0; i<nb_criteres; i++){

		tmp_criteria_field = strtok(crit[i], "\"");

		if (strcmp(tmp_criteria_field, "filename=") ==0){           //then criteria is filename
			name = strtok(NULL, "\"");
		} else if (strcmp(tmp_criteria_field, "filesize<") ==0){   	//then criteria is filesize<
			isBiggerFile = -1;
			fileSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp_criteria_field, "filesize>") ==0){    //then criteria is filesize>
			isBiggerFile = 1;
			fileSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp_criteria_field, "piecesize<") ==0){   //then criteria is piecesize<
			isBiggerPiece = -1;
			pieceSize = atoi(strtok(NULL, "\""));
		} else if (strcmp(tmp_criteria_field, "piecesize>") ==0){   //then criteria is piecesize<
			isBiggerPiece = 1;
			pieceSize = atoi(strtok(NULL, "\""));
		}
	}

	/*
	Establishes list of files matching criteria
	*/
	list *matchingFiles = list_empty();
	element *current = malloc(sizeof(element*));
	current = t->seeded_files->head;

	while(!list_is_end_mark(current)){
		if( ((name!=NULL)?(strcmp(getSfFilename(current->data), name) ==0):1)
				&& (isBiggerInt(getSfFilesize(current->data), fileSize, isBiggerFile))
				&& (isBiggerInt(getSfPiecesize(current->data), pieceSize, isBiggerPiece))){
			
			seeded_file* sf = (seeded_file*)current->data;
			list_add_head(matchingFiles, element_init(sf));
		}
		current = current->next;
	}
	

	/*
	Creates a string ret = list[fileinfo1 fileinfo2]
	*/
	
	current = matchingFiles->head;
	char* ret;
	asprintf(&ret,"list [");
	while(!list_is_end_mark(current)){
		seeded_file* sf = (seeded_file*)current->data;
		asprintf(&ret,"%s%s %d %d %s ",ret,sf->file_name,sf->file_length,sf->piece_size,sf->key);
		current=current->next;
	}

	ret[strlen(ret)-1]=']';

	return ret;
}

/*
Frees the data of the tracker
*/
void tracker_free(tracker *t){
	
	list_free(t->seeders);
	list_free(t->seeded_files);	
}

/*
Displays the data contained into the tracker
*/
void tracker_display_seeded_files(tracker* t){

	element *current = t->seeded_files->head;
	printf("Seeded_files du tracker : \n");
	
	while(!list_is_end_mark(current)){
		printf("\t%s\n",seeded_file_to_string((seeded_file*)(current->data)));
		element *current_seeder = ((seeded_file*)(current->data))->seeders->head;
		while(!list_is_end_mark(current_seeder)){
			printf("\t\t%s\n",seeder_to_string((seeder*)(current_seeder->data)));
			current_seeder=current_seeder->next;
		}
		current = current->next;
	}

	element *currseed = t->seeders->head;
	printf("Seeders du tracker : \n");
	while(!list_is_end_mark(currseed)){
		printf("\t%s\n",seeder_get_info((seeder*)(currseed->data)));
		currseed = currseed->next;
	}
}
/*
Disconnect a seeder from the tracker -> remove the seeder from the DB
*/
void tracker_disconnect_seeder(tracker* t, seeder* s){

	element *currsf = t->seeded_files->head;
	element *prev = NULL;
	//pour chaque fichier	
	while(!list_is_end_mark(currsf)){

		seeder_remove_from_list(((seeded_file*)(currsf->data))->seeders,s);

		if(list_is_end_mark(((seeded_file*)(currsf->data))->seeders->head)){
			if(prev==NULL){
				t->seeded_files->head=currsf->next;
				prev=NULL;
				currsf = currsf->next;	
			}
			else{
				prev->next=currsf->next;
				prev= currsf;
				currsf = currsf->next;	
			}
		}
		else{
			prev= currsf;
			currsf = currsf->next;	
		}		
	}

	element* supprSeeder = seeder_remove_from_list(t->seeders,s);
		if(supprSeeder != NULL){
			seeder_free(supprSeeder->data);
			free(supprSeeder);
	}
}











