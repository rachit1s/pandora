#include<iostream>
#include "CImg.h" // Just a single header - didn't need to install anything else on my machine
using namespace cimg_library;
using namespace std;

int main()
{
     CImg<unsigned char> image("hills.jpg");
        CImgDisplay main_disp(image);
          std::cin.ignore();
}
