#include "../../cpplib/includelibs.h"
#include "List.h"


int main()
{
    List<int>* firstList = new List<int>();
    cout << "Input values for firstList";
    createList(firstList);

    List<int>* secondList = new List<int>();
    cout <<"Input values for secondList";
    createList(secondList);

    try
    {
        List<int>* addedList = addLists(firstList,secondList);
        cout << "AddedList = " << *addedList << endl;

        delete addedList ;
    }
    catch(...)
    {
      cout << "exception occured" <<endl;
    }

    try
    {
      List<int>* revAddedList = addListsOfReverseNumbersIterative(firstList,secondList);
      cout << "Added list of reverse numbers : " << *revAddedList << endl;

      delete revAddedList;
    }
    catch(...)
    {
      cout << "exception occured" << endl;
    }

    delete firstList;
    delete secondList;
    return 0;
}
