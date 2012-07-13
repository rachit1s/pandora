#include "../../cpplib/includelibs.h"

static const int N=5;
void initMat(int** &mat, const int rows, const int cols)
{
  int n = rows*cols;
  mat=new int*[rows];
  for( int i = 0 ; i < rows ; i++)
  {
    mat[i] = new int[cols];
    for( int j = 0 ; j < cols ; j++ )
    {
        mat[i][j] = rand()%n;
    }
  }
}

void delMat(int** mat, const int rows, const int cols)
{
    for( int i = 0 ; i < rows ; i++ )
      delete [] mat[i];

    delete [] mat;
}

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

  int** matrix;

  initMat(matrix,N,N);

  print2DArray(matrix,N,N);

  rotate90(matrix,N);

  cout << "\n*********************\n" << endl;
  print2DArray(matrix,N,N);

  delMat(matrix,N,N);

  return 0;
}
