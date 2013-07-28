#include<iostream>

using namespace std;

class Shape
{
  public :
    virtual void draw() = 0;
    virtual void redraw()const = 0;
};

class Rectangle : public Shape
{
  public:
    virtual void draw()
    {
        cout << "drawing rectangle." << endl;
    }

    virtual void redraw()const
    {
      cout << "redrawing rectangle." << endl;
    }
};
int main()
{
//  Shape s;
  Rectangle r; 
  r.draw();
  r.redraw();

  Shape* sptr = &r;
  sptr->draw();
  sptr->redraw();

  return 0;
}
