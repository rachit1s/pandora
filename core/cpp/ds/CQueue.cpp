// The circular fixed size queue

#include<iostream>
#include<string>
#include<sstream>

using namespace std;

template<typename T>
class CQueue;

template<typename T>
ostream& operator<<(ostream& out,const CQueue<T>& queue);

template<typename T>
class CQueue
{
  private:
    T* array;
    int front;
    int rear;
    int maxSize;
    void full();
    void empty();
  public:
    CQueue(int maxSize);
    ~CQueue();
    bool isFull();
    bool isEmpty();
    void add(T& t);
    T* remove(T& t);
    string toString()const;
    friend ostream& operator<< <>(ostream& out,const CQueue<T>& queue);
};

template<typename T>
ostream& operator<<(ostream& out, const CQueue<T>& queue)
{
  out << queue.toString();
  return out;
}

template<typename T>
string CQueue<T>::toString() const
{
    stringstream ss;
    ss << "CQueue[" << "maxSize=" << maxSize << ",front=" << front << ",rear=" << rear << ",array[";
    for( int i = 0 ; i < maxSize ; i++ )
    {
      ss << array[i] << ",";
    }
    ss << "]]";
    return    ss.str();
}
template<typename T>
CQueue<T>::CQueue(int maxSize):maxSize(maxSize)
{
  array = new T(maxSize);
  front = rear = 0;
}

template<typename T>
CQueue<T>::~CQueue()
{
  delete [] array;
}

template<typename T>
inline void CQueue<T>::full()
{
  cout << "Queue is full.\n";
}

template<typename T>
inline void CQueue<T>::empty()
{
  cout << "Queue is empty.\n";
}

template<typename T>
bool CQueue<T>::isFull()
{
  if( front == ( (rear+1) % maxSize ) ) return true;
  return false;
}

template<typename T>
bool CQueue<T>::isEmpty()
{
  if(front == rear) return true;
  return false;
}

template<typename T>
void CQueue<T>::add(T& t)
{
  if( isFull() ){
    full();
    return;
  }

  rear = (rear+1) % maxSize;
  array[rear] = t;
}

template<typename T>
T* CQueue<T>::remove(T& t)
{
    if( isEmpty() ){
      empty();
      return NULL;
    }

    front = (front+1)%maxSize;
    t = array[front];
    return &t;
}

 
