#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "List.h"

list* list_empty(){
	list *l = malloc(sizeof(list));
	l->head = ENDMARK;
	return l;
}

element* element_init(void* data){
	element* e = malloc(sizeof(element));
	e->data = data;
	e->next = ENDMARK;
	return e;
}

int list_is_end_mark(element const *el){
    return (el == ENDMARK);
}

void list_add_head(list *l, element *cell){
	cell->next = l->head;
	l->head = cell;
}

element* list_remove_head(list *l){
	assert( l != NULL );
	element* ret = l->head;
	if(!list_is_end_mark(ret)){
		l->head = ret->next;
		ret->next = NULL;
	}
	return ret;
}

void list_free(list *l){
	element * el;
	el = l->head;
	while(!list_is_end_mark(l->head)){
		el = list_remove_head(l);
		free(el);
	}
	free(l);
}

element* list_find(list *l, void* data){
	
	element* e = l->head;

	while(!list_is_end_mark(e)){
		if(e->data == data)
			return e;
		e = e->next;
	}
	return NULL;
}

element* list_remove_data(list* l, void* data){
	element* e = l->head;
	element* prev= NULL;
	while(!list_is_end_mark(e)){
		if(e->data == data){
			if(prev==NULL){
				l->head=e->next;
				return e;
			}
			else{
				prev->next=e->next;
				return e;
			}
		}
		prev = e;
		e = e->next;	
	}
	return NULL;
}


// ============ Some functions useful to other files, not directly related to list.

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

int stringSize(char* a){
	int size=0;
	while (a[size] != '\0'){
		size++;
	}
	return size;
}

int isBiggerInt(int a, int b, int isBigger){
	if (isBigger == 1){
		return (a>=b);
	} else if (isBigger == -1){
		return (a<=b);
	} else return 1;                                //if isBigger is unexpected returns true.
}

/*
Removes first character of string t.
Used to remove '[' from the list of files in parser
*/
char* removeFirstCharacter(char* t){
   int size =1;
   char tmp;
   tmp = t[0];
   while(tmp != '\0'){     //get size of t
      tmp=t[size];
      size++;
   }

   char* ret=malloc(sizeof(char)*size);
   for (int i=0; i<size; i++){
      ret[i]=t[i+1];
   }

   return ret;
}