#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  List<int>* list = new List<int>();
  createList(list);

    int index = -1;
  while(true)
  {
    cout << "Enter the index of the node to remove  (-1 to end)  : " ;
    cin >> index;

    if( -1 == index ) 
      break;

    Node<int>* node = getNthNode(list,index);
   
    cout << "Will now remove node : ";
    if( NULL == node )
      cout << "NULL" << endl;
    else
      cout << *node << endl;

    list->removeNode(node);

    cout << "List after removing :" << *list << endl; 
  }

  delete list;
}
