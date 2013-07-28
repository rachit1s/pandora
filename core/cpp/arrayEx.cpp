#include<iostream>

using namespace std;

int main()
{
  int a[3][4] = { {1,2,3,4} , {5,6,7,8}, {9,10,11,12 } };

  int count = 12 ;
  /*
  char* x = a;

  for( int i = 0 ; i < count ; i++ )
  {
    cout << *x << endl ;
    x = x + 1;

  }


  */
cout << "sy =" << *(*a+6) << endl;
  for( int i = 0 ; i < count ; i++ )
  {
    cout << *(*a+i) << endl ;
  }



  for( int i = 0 ; i < count ; i++ )
  {
    cout << *(a[0]+i) << endl ;
  }

  return 0;


}
