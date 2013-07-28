#include<iostream>

using namespace std;

class A
{
  public:
  int x;
  A()
  {
    cout << "1. A()";
  }
  A(const A& a )
  {
    cout << "2. A (a)";
  }

  void operator= (const A& a)
  {
    cout << "3. operator = " ;
  }
};

void print(A a)
{
  cout << "print(a)" ;
}

void print1(A& a)
{
  cout << "print1(a)" ;
}


int main()
{
  A a ;
  cout << "..." << endl;
  A a1 = a;
  cout << "..." << endl;
  A a2(a);
  cout << "..." << endl;
  A a3();

  a3();

  cout << "..." << endl;
  A * aptr = new A;
  cout << "..." << endl;
  A * aptr2 = new A();
  cout << "..." << endl;
  A* aptr3 = new A(a);
  cout << "..." << endl;
  A a4 = A();
  cout << "..." << endl;
  A a5 = A(a);
  cout << "..." << endl;

  a5 = a4;
  cout << "..." << endl;

  print(a);
  cout << "..." << endl;
  print1(a);
  cout << "..." << endl;
  
  a.x ;
  return 0 ;
}

A a3()
{
  cout << "a3 called";
  return A();
}
