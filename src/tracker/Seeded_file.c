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
#include "Seeder.h"
#include "Utils.h"

seeded_file* seeded_file_init(char* name, unsigned int length, unsigned int piece_size, char* key){
	
	seeded_file* sf = malloc(sizeof(seeded_file*));
	
	sf->file_name = malloc(strlen(name));
	strcpy(sf->file_name,name);

	sf->file_length = length;
	sf->piece_size = piece_size;

	sf->key = malloc(strlen(key));
	strcpy(sf->key,key);

	sf->seeders = list_empty();
	return sf;
}

void seeded_file_add_seeder(seeded_file* sf, seeder* s){
	
	list_add_head(sf->seeders,element_init(s));
}

void seeded_file_remove_seeder(seeded_file* sf, seeder* s){
	
	list_remove_data(sf->seeders,s);
}

char *seeded_file_get_info(seeded_file *s){

	char* ret;
	asprintf(&ret,"peers %s [",s->key);
	element *current = s->seeders->head;
	while(!list_is_end_mark(current)){
		asprintf(&ret,"%s%s ",ret,seeder_get_info(current->data));
		current=current->next;
	}
	ret[strlen(ret)-1]=']';
	return ret;
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

char* seeded_file_to_string(seeded_file *sf){

   char* buffer;
   asprintf(&buffer,"%s %d %d %s",sf->file_name,sf->file_length,sf->piece_size,sf->key);
   return buffer;
}

element* seeded_file_seeder_find(list *l, seeder *s){
	
	element* e = l->head;

	while(!list_is_end_mark(e)){
		if(e->data == s)
			return e;
		e = e->next;
	}
	return NULL;
}


element* seeded_file_find(list *l, seeded_file *sf){

	char* key = getSfKey(sf);

	element* e = l->head;

	while(!list_is_end_mark(e)){
		if(strcmp(getSfKey(e->data), key) ==0)
			return e;
		e = e->next;
	}
	return NULL;
}

void seeded_file_free(seeded_file* sf){

	free(sf->key);
	free(sf->file_name);

}