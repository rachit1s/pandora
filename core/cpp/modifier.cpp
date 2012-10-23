#include<iostream>

using namespace std;

class A
{
  private :
    int priv;
    virtual int getPriv()
    {
      return priv;
    }
  protected :
    int prot;
    int getProt()
    {
      return prot;
    }
  public :
    int pub;
    int getPub()
    {
      return pub;
    }
    void setPriv(int p)
    {
      priv = p;
    }
      

};

class C : public A
{
  private :
    int getProt()
    {
      return prot;
    }
  protected :
    int getPub()
    {
      return pub;
    }
  public :

    int getPriv()
    {
      return 210;
    }
};

class B : public A
{
  public : 
    int pub;
};

int main()
{
  B b;
  b.A::pub = 10 ;
  b.pub = 20 ;

  cout << b.A::pub << endl;
  cout << b.pub << endl;

  C c;
  c.setPriv(20);
  cout << "c.getPriv() =" << c.getPriv() << endl;
//  cout << "c.A::getPriv() =" << c.A::getPriv() << endl;

  A* aptr = &c;

  cout << "aptr->getPriv() = " << aptr->getPriv()  << endl;

  return 0;
}
