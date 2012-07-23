#include<iostream>
#include "../objects/SetOfStacks.cpp"

using namespace std;

static void testSetOfStacks()
{
  SetOfStacks<int>* as = new SetOfStacks<int>(3);

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
}

static void testSetOfStacksPopAt()
{
  SetOfStacks<int>* as = new SetOfStacks<int>(3);

  try
  {
    for( int i = 0 ; i < 20 ; i++ )
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

  // removing the data from 3nd stack for 5 time
  for( int i = 0 ; i < 5 ; i++)
  {
	  int p = as->popAt(2);
	  cout << "poped int : " << p << endl;
	  cout << "SOS after pop : " << *as << endl;
  }

  delete as;
}
