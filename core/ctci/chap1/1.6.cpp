#include "../../cpplib/includelibs.h"

static const int N=5;

// rotates square matrix
void rotate90(int ** matrix, const int SIZE)
{
    if( SIZE < 2 )
      return;

    int increment = 0;
    int r = increment;
    int c = increment+1;
    while( r < SIZE )
    {
      while( c < SIZE )
      {
        int temp = matrix[c][r];

        matrix[c][r] = matrix[r][c];
        matrix[r][c] = temp; 
        c++;
      }

      increment++;
      r = increment;
      c = increment+1;
    }
}

int main( int argc, char* argv[])
{
  
  srand(time(0));

  int** matrix = createAndRandomize2DIntArray(N,N);

  print2DArray(matrix,N,N);

  rotate90(matrix,N);

  cout << "\n*********************\n" << endl;
  print2DArray(matrix,N,N);

  del2DArray(matrix,N);

  return 0;
}
