#include <iostream>
#include "../objects/ArrayStack.h"

static int testArrayStack()
{
  ArrayStack<int>* as = new ArrayStack<int>(9);

  try
  {
    for( int i = 0 ; i < 10 ; i++ )
    {
      as->push(i);
      cout << "Stack after push : " << *as << endl;
    }
  }
  catch(const char* msg)
  {
    cout << "Exception occured : " << msg << endl;
    cout << "Stack at exeception : " << *as << endl;
  }

  try
  {
    for( int i = 0 ; i < 10 ; i++ )
    {
      int x = as->pop();
      cout << "poped x : " << x << endl; 
      cout << "Stack after pop : " << *as << endl;
    }
  }
  catch(const char* msg)
  {
    cout << "Exception occured : " << msg << endl;
    cout << "Stack after exception : " << *as << endl ;
  }

  delete as;

  return 0;
}
