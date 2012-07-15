#include "../../cpplib/includelibs.h"

static const int N=3;
static const int M=4;

int main( int argc, char* argv[])
{
  srand(time(0));
  
  int** matrix;
  createAndInput2DArray(matrix,N,M); 
  
  print2DArray(matrix,N,M);

  del2DArray(matrix,N);

  return 0;
}
