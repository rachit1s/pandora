#ifndef __ARRAYSTACK_H__
#define __ARRAYSTACK_H__

#include "../../cpplib/includelibs.h"

template<typename T>
class ArrayStack;

template<typename T>
ostream& operator<< (ostream& out, const ArrayStack<T>& as);


template<typename T>
class ArrayStack
{
  private:
    int size;
    T** array;
    int topIndex ;
  public:
    ArrayStack(int size)
    {
      this->size = size;
      array = new T*[size];
      topIndex = -1;
    }

    ~ArrayStack()
    {
      delete [] array;
    }
    
    void push(T& const t)
    {
      if(isFull())
        throw "Stack is full";
      array[++topIndex] = &t;
    }

    T& pop()
    {
      T& value = top();
      array[topIndex--] = NULL;
      return value;
    }

    bool isFull()
    {
      if(topIndex==size-1)
        return true;

      return false;
    }

    bool isEmpty()
    {
      if(-1 == topIndex)
        return true;

      return false;
    }

    T& top()
    {
      if(isEmpty())
        throw "Stack is empty";
      return *array[topIndex];
    }

    friend ostream& operator<< <><><><>(ostream& out, const ArrayStack<T>& as);
};

template<typename T>
ostream& operator<< (ostream& out, const ArrayStack<T>& as)
{
    out << "[ArrayStack=(size=" << as.size << "),(topIndex=" << as.topIndex << "),array=[";
    for( int i = 0; i <= as.topIndex ; i++)
    {
      out << *(as.array[i]) << ",";
    }
    out << "]]";

    return out;
}

#endif
