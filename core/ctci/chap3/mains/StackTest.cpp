#include <iostream>
#include "../objects/Stack.h"

using namespace std;

const int PUSH=1;
const int POP=2;
const int TOP=3;
const int REMOVE=4;
const int EXIT=5;

static int testStack()
{
    Stack<int>* stack = new Stack<int>();
//    inputStack(stack);
//    cout << "\nYour inputed Stack = " << *stack;
    
    int i = 0 ;
    bool shouldExit = false;
    do
    {
      cout <<"Choose \n1. push value\n2. pop value\n3. see top value\n4. remove top value\n5. exit\n:" ;
      cin >> i;
      try
      {
      switch(i)
      {
           case PUSH:
            cout << "Enter value to push : " ;
            cin >> i;
            stack->push(i);
            cout << "Stack After push : " << *stack << endl;
            break;
           case POP:
            i = stack->pop();
            cout << "Poped value : " << i << endl;
            cout <<"Stack after pop : " << *stack << endl;
            break;
           case TOP:
            i = stack->top();
            cout << "top value : " << i << endl;
            cout << "Stack after top : " << *stack << endl;
            break;
           case REMOVE:
            stack->removeTop();
            cout << "Stack after remove() : " << *stack << endl;
            break;
           case EXIT:
            cout << "Exiting.." << endl;
            shouldExit = true;
            break;
           default:
            cout << "Illegal Input. Try again." << endl;
      }
      }
      catch( const char* msg )
      {
        cout <<"Caught Exception : " << msg << ". Try again." << endl;
      }

      if( shouldExit == true ) break;
    }while(true);

    delete stack;
    return 0;
}
