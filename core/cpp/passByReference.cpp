#include<iostream>
using namespace std;

class A
{
  public :
    int value;
    A(int value)
    {
      cout << "normal constructor called."<< endl;
      this->value = value;
    }

    A(const A& a)
    {
      cout <<"copy constructor called." << endl;
      this->value = a.value;
    }

    A& operator=(const A& a);
};

A& A::operator=(const A& a)
{
  cout << "overloaded operator= called." << endl;
  this->value = a.value;

  return *this;
}

void changeValue( int& y )
{
  y = 20;
}

A* changeValue(A& a)
{
  A b(20);
  a = b;
  return &a;
}

int main()
{
  int x = 10 ;
  int &ref = x;
  changeValue(ref);

  cout << "x = " << x << "\t ref = " << ref ;

  A a(10);

  cout << "a.value = " << (a.value) << "\t &a = " << (&a) << endl; 
  changeValue(a);

  cout << "a.value = " << (a.value) << "\t &a = " << (&a) << endl; 
  return 0;
}
