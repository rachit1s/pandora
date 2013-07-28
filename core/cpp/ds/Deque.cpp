#include<iostream>
#include<sstream>
#include<string>

using namespace std;

template<typename T>
class Deque;

template<typename T>
ostream& operator<<(ostream& out, const Deque<T>& q);

template<typename T>
class Deque
{
  private:
   int front;
   int rear;
   int maxSize;
   T* array;

  public:
   Deque(int maxSize)
   {
    this->maxSize = maxSize;
    array = new T[maxSize+1];
    front = rear = 0;
   }
   void full()
   {
     cout << "Deque is full.\n" ;
   }
   void empty()
   {
     cout << "Deque is empty.\n" ;
   }

   bool isEmpty();
   bool isFull();

   void add(T& t, bool isFront);
   T* remove(T& t, bool isFront);
   void addFront(T&t);
   void addBack(T&t);
   T* removeFront(T&t);
   T* removeBack(T&t);
   string toString()const;
   friend ostream& operator<< <>(ostream& out, const Deque<T>& q);
};

template<typename T>
string Deque<T>::toString()const
{
  stringstream ss;
  ss << "Deque[maxSize=" << maxSize << ",front="<<front<< ",rear=" << rear << ",array=[";
  for( int i = 0 ; i < maxSize ; i++ )
  {
    ss << array[i] << ",";
  }

  ss << "]]";

  return ss.str();
}

template<typename T>
ostream& operator<< (ostream& out, const Deque<T>& q)
{
  out << q.toString();
  return out;
}

template<typename T>
bool Deque<T>::isFull()
{
  if( ((rear+1)%maxSize) == front ) return true;
  return false;
}

template<typename T>
bool Deque<T>::isEmpty()
{
  if( front == rear ) return true;
  return false;
}

template<typename T>
void Deque<T>::add(T& t, bool isFront)
{
    if( isFront == true ) 
        addFront(t);
    else
        addBack(t);
}

template<typename T>
T* Deque<T>::remove(T& t, bool fromFront)
{
  if( fromFront == true )
    return    removeFront(t);
  else
    return  removeBack(t);
}

template<typename T>
void Deque<T>::addFront(T& t)
{
  if( isFull() ){
    full();
    return;
  }

  array[front] = t ;
  if( 0 == front ) front = maxSize -1;
  else
     front = front-1;
}

template<typename T>
void Deque<T>::addBack(T&t)
{
  if( isFull() ){
    full();
    return ;
  }

  rear = (rear+1)%maxSize;
  array[rear] = t;
}

template<typename T>
T* Deque<T>::removeFront(T&t)
{
  if(isEmpty()){
    empty();
    return NULL;
  }

  front=(front+1)%maxSize;
  t = array[front];
  return &t;
}

template<typename T>
T* Deque<T>::removeBack(T&t)
{
    if(isEmpty()){
      empty();
      return NULL;
    }

    t = array[rear];
    if(rear == 0) rear = maxSize -1 ;
    else rear = rear -1 ;
    return &t;
}

