#include<iostream>
using namespace std;

class A
{
  public :
    int x; 
    int y;
  virtual int getValue()
    {
      cout << "A.getValue() called." ;
      return x*y;
    }
};

class B : public A
{
  public :

    int getValue()
    {
      cout << "B.getValue() called.";
      return x+y;
    }
};

class C : public B
{
  public : 
    int getValue()
    {
      cout << "C.getValue() called.";
      return x-y;
    }
};
int main()
{
  A a ;
  a.x = 10 ;
  a.y = 2;

  cout << "a.getValue() = " << a.getValue() << endl; // 20

  B b;
  b.x = 10 ;
  b.y = 2;

  cout << "b.getValue() = " << b.getValue() << endl; // 12

  A* aptr = &b;

  cout << "aptr->getValue() = " << aptr->getValue() << endl; // 121212121212121212121212 

  C c;
  c.x = 10;
  c.y = 2;

  cout << "c.getValue() = " << c.getValue() << endl; // 8

  A* acptr = &c ;
   
  cout << "acptr->getValue() = " << acptr->getValue() << endl; // 20

  B* bcptr = &c;
  cout << "bcptr->getValue() = " << bcptr->getValue() << endl; // 12

}

