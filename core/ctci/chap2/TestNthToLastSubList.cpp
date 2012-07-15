#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  List<int>* list = new List<int>();
  createList(list);

  while(true){
  
  int index = 0 ;
  cout << "Enter the start index of sub-list (-1 to abort)  : " ;
  cin >> index;
  
  if( -1 == index )
    break;
  
  List<int>* nList = nthToLastSubList(list,index);
  cout << "Sub List : " << *nList << endl;
  delete nList;

  }
  
  delete list;
  return 0;

}
