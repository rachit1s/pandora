#include<iostream>
using namespace std;

class A
{
  public:
    void print()
    {
      cout << "A.print()" << endl;
    }
};

int main()
{
  class A a;
  a.print();

  return 0;
}
