#include "../../cpplib/includelibs.h"
#include "List.h"

int main()
{
  try
  {
    List<int>* list = new List<int>();

    createList(list);

     createLoop(list);

     Node<int>* loopHead = getLoopPoint(list);

     if( NULL != loopHead )
         cout << "found loop head to be : " << *loopHead << endl;
     else cout << "No loop detected." << endl ;
  }
  catch( const char* msg)
  {
    cout << "Exception occured : " << msg ;
  }

  // deleting looped list will also result in segmentation fault
  return 0;
}
