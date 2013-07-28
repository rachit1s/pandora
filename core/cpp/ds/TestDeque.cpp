#include<iostream>
using namespace std;

#include "Deque.cpp"

template<typename T>
void testDeque(Deque<T>& q)
{
  int c ;
  do
  {
    cout << "1. insert value into front of queue.\n"
         << "2. insert value into back of queue.\n"
         << "3. remove value from the front of queue.\n"
         << "4. remove value from the back of queue.\n"
         << "5. print queue.\n"
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
        q.addFront(t);
        break;
      case 2:
        cin >> t;
        q.addBack(t);
        break;
      case 3:
        tptr = q.removeFront(t);
        if( NULL != tptr ) 
            cout << "out value is : " << t << endl;
        break;
      case 4:
        tptr = q.removeBack(t);
        if( NULL != tptr )
          cout << "out value is : " << t << endl;
        break;
      case 5:
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
  cout << "Enter max-size of the Deque: ";
    int size ;
  cin >> size ;
    Deque<int> intQ(size+1);
    testDeque(intQ);

    return 0;
}
