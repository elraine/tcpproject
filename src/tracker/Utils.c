#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "Utils.h"
#include <math.h>


void error(const char *msg) {
    perror(msg);
    exit(1);
}

void usage (char *s){
 	fprintf(stderr, "Usage: %s <portno>\n", s);
 	exit(EXIT_FAILURE);
}

char* itoa(int a){
	int tmp=a;
	int size =1;
	while ((tmp/10) >0){
		size++;
		tmp = tmp/10;
	}
	char* ret=malloc((size+1)*sizeof(char));
	ret[size] ='\0';
	size--;
	tmp =a;
	while(size >=0){
		ret[size]=(tmp%10) + '0';
		tmp = tmp/10;
		size--;
	}
	return ret;
}

int number_of_digits(int a){
	return floor (log10 (abs (a))) + 1;
}


int isBiggerInt(int a, int b, int isBigger){
	if (isBigger == 1){
		return (a>=b);
	} else if (isBigger == -1){
		return (a<=b);
	} else return 1; //if isBigger is unexpected returns true.
}

/*
Removes first character of string t.
Used to remove '[' from the list of files in parser
*/
char* removeFirstCharacter(char* t){

   int size = strlen(t);
   char* ret=malloc(sizeof(char)*size);
   for (int i=0; i<size; i++){
      ret[i]=t[i+1];
   }
   return ret;
}