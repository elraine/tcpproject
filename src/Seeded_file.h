#ifndef SEEDED_FILE_H
#define SEEDED_FILE_H
#include "Seeder.h"
#include "List.h"

typedef struct{
	char * file_name;
	unsigned int file_length;
	unsigned int piece_size;
	char * key;
	list* seeders;
}seeded_file;

seeded_file* seeded_file_init(char* name, unsigned int length, unsigned int piece_size, char* key);

void seeded_file_add_seeder(seeded_file* f, seeder* s);
void seeded_file_remove_seeder(seeded_file* f, seeder* s);
int seeded_file_look(seeded_file* f,char* name, int filesize);


#endif

