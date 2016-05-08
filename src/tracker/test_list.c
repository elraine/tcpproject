#include <stdio.h>
#include <stdlib.h>
#include "List.h"
#include <assert.h>

void test_list_empty(){
	
  list *l = list_empty();
  assert(list_is_end_mark(l->head));
  
  list_free(l);
  
  printf("Les tests de list_empty sont réussis!\n");
}

void test_list_is_end_mark (void)
{
  list *l = list_empty();
  assert(list_is_end_mark(l->head));

  char* c1 = "plop";
  list_add_head(l, element_init(c1));
  
  assert(!list_is_end_mark(l->head));
  assert(list_is_end_mark(l->head->next));
  
  list_free(l);
  
  printf("Les tests de list_is_end_mark sont réussis!\n");
}

void test_list_add_head (void)
{
  list *l = list_empty();

  char* c1 = "plop";
  char* c2 = "plap";
  
  list_add_head(l, element_init(c1));
  assert(l->head->data == c1);

  list_add_head(l, element_init(c2));
  assert(l->head->data == c2);
  
  list_free(l);
  
  printf("Les tests de list_add_head sont réussis!\n");
}

void test_list_remove_head (void)
{
  list *l = list_empty();
  assert(list_remove_head(l) == NULL);
  element *tmp = NULL;
  
  element* e1 = element_init("plop");
  list_add_head(l,e1);
  tmp = list_remove_head(l);
  assert(tmp == e1);

  element* e2 = element_init("plap");
  list_add_head(l, e2);
  list_add_head(l, e1);
  
  tmp = list_remove_head(l);
  assert(l->head == e2);
  free(tmp);

  list_free(l);
  
  printf("Les tests de list_remove_head sont réussis!\n");
}

void test_list_find (void)
{
  list *l = list_empty();
  assert(list_remove_head(l) == NULL);
  element *tmp = NULL;
  
  element* e1 = element_init("plop");
  element* e2 = element_init("plap");
  
  list_add_head(l,e1);
  assert(list_find(l,"plop") == e1);

  list_add_head(l, e2);
  assert(list_find(l,"plap") == e2);
  assert(list_find(l,"plup") == NULL);
 
  tmp = list_remove_head(l);
  assert(list_find(l,"plap") == NULL);
  free(tmp);

  list_free(l);
  
  printf("Les tests de list_find sont réussis!\n");
}

void test_list_remove_data (void)
{
  list *l = list_empty();
  assert(list_remove_head(l) == NULL);
  element *tmp = NULL;
  
  element* e1 = element_init("plop");
  element* e2 = element_init("plap");
  element* e3 = element_init("plup");
  
  list_add_head(l,e1);
  list_add_head(l,e2);
  assert(list_remove_data(l,"plup") == NULL);
  list_add_head(l,e3);
  
  tmp = list_remove_data(l,"plup");
  assert( tmp == e3);
  assert(l->head == e2);
  free(tmp);

  tmp = list_remove_data(l,"plop");
  assert( tmp == e1);
  assert(l->head == e2);
  assert(e2->next == NULL);
  
  free(tmp);
  list_free(l);
  
  printf("Les tests de list_remove_data sont réussis!\n");
}

int main()
{
  test_list_empty();
  test_list_is_end_mark();
  test_list_add_head();
  test_list_remove_head();
  test_list_find();
  test_list_remove_data();

  return 0;
}
