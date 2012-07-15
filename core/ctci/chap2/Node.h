#ifndef __NODE_H
#define __NODE_H
#include "../../cpplib/includelibs.h"

template <typename T>
class Node;


template <class T>
ostream& operator<<( ostream& out, const Node<T>& n)
{
    out<< "Node[ data=" << n.data << " ]"; 
    return out;
}

template <typename T>
class Node
{
  private :
  T data;
  Node<T>* next;
  public :

  Node()
  {
    next = NULL;
  }

  Node( const T data )
  {
    this->data = data;
    next = NULL;
  }

  Node(T data, Node<T> const* next)
  {
    this->data = data;
    this->next = next;
  }

  void setData(const T& data)
  {
    this->data = data;
  }

  T getData()
  {
    return data;
  }

  Node<T>* getNext()
  {
    return next;
  }

  void setNext(Node<T> * const node)
  {
    this->next = node;
  }

 friend ostream& operator<< <> (ostream& out, const Node<T>& r);

};



#endif
