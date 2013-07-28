#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  List<int>* list = new List<int>();
  
  createList(list);
  removeFromList(list);

  removeDuplicates(list);

  cout << "List after removing duplicates : " << *list << endl;

  delete list;
  return 0;
}
