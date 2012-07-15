#include <iostream>
using namespace std;

class A
{
  public:
    A(){
      cout << "A() called" << endl;
    }
    A(int i):A()
    {
      cout <<"A(int) called" << endl;
    }
}

int main()
{
  A a = A();

  return 0;
}
