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
