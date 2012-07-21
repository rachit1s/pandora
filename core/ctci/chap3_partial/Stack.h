#ifndef __STACK_H_
#define __STACK_H_

#include "../../cpplib/includelibs.h"
#include "../../cpplib/Node.h"

template<typename T>
class Stack;

template<typename T>
ostream& operator<< (ostream& out, const Stack<T>& stack);

template<typename T>
class Stack
{
  private :
  Node<T>* head;

  public:
  Stack()
  {
    head = NULL;
  }

  //copies the data from given stack to current stack
  Stack( const Stack& stack )
  {
    head = NULL;
    Node<T>* n = &stack.head;
    while( NULL != n )
    {
        push(n->getData());
        n = n->getNext();
    }
  }

  ~Stack()
  {
    Node<T>* curr = head;
    while(NULL != head)
    {
      head = head->getNext();
      delete curr;
      curr = head;
    }

  }
  // puts the element on the top of the stack
  Stack& push( T data ) 
  {
    Node<T>* nNode = new Node<T>(data);
    Node<T>* oldHead = head;
    head = new Node<T>(data) ;
   
    head->setNext(oldHead);

    return *this;
  }

  // removes the top element and return it
  T pop()
  {
    if(isEmpty())
      throw "Stack is Empty.";

    Node<T>* oldHead = head;
    head = head->getNext();

    T data = oldHead->getData();
    delete oldHead;
    return data;
  }

  // true if stack is empty else false
  bool isEmpty()
  {
    if( NULL == head ) return true;

    return false;
  }

  // returns the copy of the top element but does not remove it.
  T top()
  {
    if(isEmpty())
      throw "Stack is empty.";

    return head->getData();
  }

  // removes the top element.
  // return true if the operation was successful 
  // return false if the stack was empty.
  bool removeTop()
  {
    if( isEmpty() ) return false;
    else
    {
      Node<T>* oldHead = head;
      head = head->getNext();
      delete oldHead;
      return true;
    }
  }

  friend ostream& operator<< <>(ostream& out, const Stack<T>& stack);
};

template<typename T>
ostream& operator<<(ostream& out,const Stack<T>&stack)
{
  out << "Stack[";
  Node<T>* curr = stack.head;
  while(NULL != curr)
  {
    out << *curr << ",";
    curr = curr->getNext();
  }
  out << "]" ;

  return out;
}

template<typename T>
void inputStack(Stack<T>*& stack)
{
  T t;
  do{
    cout << "Enter the next value to put into stack (ctrl+d to end) : ";
    cin >> t ;
    if( !cin.eof() )
        stack->push(t);    
  }while(!cin.eof());
}

#endif
