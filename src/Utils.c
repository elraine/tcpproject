#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "Utils.h"

void error(const char *msg) {
    perror(msg);
    exit(1);
}

void usage (char *s){
 	fprintf(stderr, "Usage: %s <portno>\n", s);
 	exit(EXIT_FAILURE);
}