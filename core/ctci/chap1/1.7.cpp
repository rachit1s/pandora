#include "../../cpplib/includelibs.h"

static const int N=3;
static const int M=4;

void makeZero(int ** matrix, const int rows, const int cols)
{
  int * cs = new int[cols];
  int * rs = new int[rows];

    fill(cs,cs+cols-1,0);
    fill(rs,rs+rows-1,0);

  for( int r = 0 ; r < rows ; r++ )
  {
    for( int c = 0 ; c < cols ; c++ )
    {
      if( matrix[r][c] == 0 )
      {
        cs[c] = 1 ;
        rs[r] = 1 ;
      }
    }
  }

  for(int r = 0 ; r < rows ; r++ )
  {
    if( 1 == rs[r] ) 
    {
      for( int c = 0 ; c < cols ; c++ )
        matrix[r][c] = 0 ;
    }
  }

  for( int c = 0 ; c < cols ; c++ )
  {
    if( 1 == cs[c] ) 
    {
      for( int r = 0 ; r < rows ; r++ )
        matrix[r][c] = 0 ;
    }
  }
}

int main( int argc, char* argv[])
{
  srand(time(0));
  
  int** matrix;
  createAndInput2DArray(matrix,N,M); 
  
  print2DArray(matrix,N,M);

  makeZero(matrix,N,M);

  cout << "\n***********************\n" << endl;
  print2DArray(matrix,N,M);

  del2DArray(matrix,N);

  return 0;
}
