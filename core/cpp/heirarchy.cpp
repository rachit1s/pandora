#include<iostream>

using namespace std;

class A
{
  private :
    int apriv;
  protected :
    int aprot;
    int getAPriv()const
    {
      return apriv;
    }

  public :
    int apub;

    int getAProt() const
    {
      return aprot;
    }

    int getAPub()const
    {
      return apub;
    }

    int getValue()const
    {
      return apub * aprot * apriv ;
    }
    
};

class B : private A
{
  public:
    int getMyAProt()
    {
      return aprot;
    }
};

class C : public B
{
//  public :
//    int accessAProt()
//    {
//        return aprot;
 //   }
};

int main()
{
  A a ; //always correct 
  B b; // always correct


  // a's private
//  b.apriv;
  // a's protected
//  b.aprot;
  // a's public
//  b.apub;

  a.getValue();
  a.apub;
  a.getAPub();
  a.getAProt();
 // a.aprot;
 // a.getAPriv();


  A* aptr = &a ; // correct 
  B* bptr = &b; // correct

  aptr = &b;  // right
//  bptr = &a;  // wrong
 
  
  A & aref = a; // right
  B & bref = b; // right

  A& aaref = b; // right
//  B& bbref = a; // wrong

  return 0;
}
