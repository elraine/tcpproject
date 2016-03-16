#ifndef SEEDED_FILE_H
#define SEEDED_FILE_H
#include "Seeder.h"

typedef struct{
	char * file_name;
	unsigned int file_length;
	unsigned int piece_size;
	char * key;
	seeder* seeders;
}seeded_file;


#endif
