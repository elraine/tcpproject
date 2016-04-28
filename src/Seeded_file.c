#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <strings.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "Seeded_file.h"

seeded_file* seeded_file_init(char* name, unsigned int length, unsigned int piece_size, char* key){
	
	seeded_file* sf = malloc(sizeof(seeded_file*));
	
	sf->file_name = name;
	sf->file_length = length;
	sf->piece_size = piece_size;
	sf->key = key;
	
	sf->seeders = list_empty();
	return sf;
}

void seeded_file_add_seeder(seeded_file* sf, seeder* s){
	
	list_add_head(sf->seeders,element_init(s));
}

void seeded_file_remove_seeder(seeded_file* sf, seeder* s){
	
	list_remove_data(sf->seeders,s);
	//free ?
}

int seeded_file_look(seeded_file* sf,char* name, int filesize){
	int matching = 0;
	printf("name : %s\n",name);
	printf("file_name : %s\n",sf->file_name);
	if(name != NULL){
		
		printf("ploopppppp\n");
	}
	if(name != NULL && !strcmp(sf->file_name,name))
		matching = 1;
	/*if(filesize!=(-1) && filesize == sf->file_length)
		matching = 1;*/
	return matching;
}


char* getSfFilename(seeded_file *sf){
	return sf->file_name;
}

int getSfFilesize(seeded_file *sf){
	return sf->file_length;
}

int getSfPiecesize(seeded_file *sf){
	return sf->piece_size;
}

char* getSfKey(seeded_file *sf){
	return sf->key;	
}

int sfSize(seeded_file *sf){
	int size = stringSize(sf->file_name) + stringSize(itoa(sf->file_length)) + stringSize(itoa(sf->piece_size)) + stringSize(sf->key);
   	size+=4;                          //3spaces +\0 or 4 spaces
   	return size;
}

char* sfToChar(seeded_file *sf){
   int size = stringSize(sf->file_name) + stringSize(itoa(sf->file_length)) + stringSize(itoa(sf->piece_size)) + stringSize(sf->key);
   size+=4;                         //3spaces + \0

   char* filesize = itoa(sf->file_length);
   char* piecesize = itoa(sf->piece_size);
   char* ret= malloc(size*sizeof(char));

   strcpy(ret, sf->file_name);
   strcat(ret, " ");
   strcat(ret, filesize);
   strcat(ret, " ");
   strcat(ret, piecesize);
   strcat(ret, " ");
   strcat(ret, sf->key);

   return ret;
}



element* list_sf_find(list *l, seeded_file *sf){
	char* key = getSfKey(sf);

	element* e = malloc(sizeof(element));
	e = l->head;

	while(!list_is_end_mark(e)){
		if(strcmp(getSfKey(e->data), key) ==0)
			return e;
		e = e->next;
	}
	return NULL;
}
