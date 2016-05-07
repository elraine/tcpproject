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

