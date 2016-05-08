#ifndef LIST_H
#define LIST_H

#define ENDMARK NULL


typedef struct element{
	void *data;
	struct element *next;
}element;


typedef struct list{
	element *head;
}list;

element* element_init(void* data);
list* list_empty();
int list_is_end_mark(element const *el);
void list_add_head(list *l, element *cell);
element* list_remove_head(list *l);
void list_free(list *l);
element* list_find(list* l, void* data);
element* list_remove_data(list* l, void* data);

#endif
