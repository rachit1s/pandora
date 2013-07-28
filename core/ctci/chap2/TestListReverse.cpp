#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  List<int>* list = new List<int>();
  
  createList(list);

  cout << "Original List : " << *list << endl;

  List<int>* revList = createReverse(list);

  cout << "Reversed List : " << *revList << endl;

  cout << "Original List after creating reversed List : " << *list << endl;

  delete revList;
  delete list;
  return 0;
}
