#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  List<int>* list = new List<int>();
  createList(list);

  while(true){
  
  int index = 0 ;
  cout << "Enter the element number from last (-1 to abort)  : " ;
  cin >> index;
  
  if( -1 == index )
    break;
  try
  {
    Node<int>* result = getNthFromEndNode(list,index);
  
    cout << "Node : " << *result << endl;
  }
  catch(const char* msg)
  {
    cout << "Error : " << msg<<endl;
  }
  }
  
  delete list;
  return 0;

}
