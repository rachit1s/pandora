#include<iostream>
using namespace std;

#include "CQueue.cpp"

template<typename T>
void testCQueue(CQueue<T>& q)
{
  int c ;
  do
  {
    cout << "1. insert value into queue.\n"
         << "2. remove value from the queue.\n"
         << "3. print queue.\n"
         << ":" ;
    cin >> c;
  
    if( cin.eof() )
      break;

    T t;
    T * tptr = NULL;
    switch(c)
    {
      case 1:
        cin >> t;
        q.add(t);
        break;
      case 2:
        tptr = q.remove(t);
        if( NULL != tptr ) 
            cout << "out value is : " << t << endl;
        break;
      case 3:
        cout << q << endl;
        break;
      default :
        cout <<"Invalid choice. Try Again." << endl;
        break;
    }
  }while(true);
}

int main()
{
  cout << "Enter max-size of the Queue: ";
    int size ;
  cin >> size ;
    CQueue<int> intQ(size+1);
    testCQueue(intQ);

    return 0;
}
