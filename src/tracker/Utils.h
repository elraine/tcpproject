#ifndef UTILS_H
#define UTILS_H

#define LOGFILE "server.log"
FILE *mylog;
#define LOG(args...) do { fprintf(mylog, args); fflush(mylog); } while (0)

void error(const char *msg);
void usage (char *s);
char* itoa(int a);
int number_of_digits(int a);
int isBiggerInt(int a, int b, int isBigger);
char* removeFirstCharacter(char* t);

#endif
