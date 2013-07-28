#include<iostream>
using namespace std;

class A
{
  //public:
    /*
    A()
    {
      cout << "A constructor" << endl;
    }
    */
/*    int A()
    {
      cout << "A method" << endl;
      return 1;
    }
    */
};

int A(int x)
{
  return x;
}

int main()
{
  A a = A();
//  a.A();

//  int y = A(10);
//  cout << y << endl; 
  return 0;
}
