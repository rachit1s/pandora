#include <iostream>
using namespace std;

#include "CImg.h"
using namespace cimg_library;

int main()
{
      CImg<unsigned char> src("test.jpg");
          int width = src.width();
              int height = src.height();
                  cout << width << "x" << height << endl;
                      for (int r = 0; r < height; r++)
                                for (int c = 0; c < width; c++)
                                              cout << "(" << r << "," << c << ") ="
                                                                 << " R" << (int)src(c,r,0,0)
                                                                                  << " G" << (int)src(c,r,0,1)
                                                                                                   << " B" << (int)src(c,r,0,2) << endl;
                          return 0;
}

