#ifndef __CPPUTILS_H_INCLUDED__   // if it hasn't been included yet...
#define __CPPUTILS_H_INCLUDED__   //   #define this so the compiler knows it has been included
#include "includelibs.h"
using namespace std;

/*
void printArray(bool *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}
*/

template <class T>
void printArray(T *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}

/*
void printArray(T *array, long SIZE)
{
	printArray(array,SIZE,'\n');
}
*/

template <class T>
void printArray(T *array, long SIZE)
{
	printArray(array,SIZE,'\n');
}


void usage(const char* progName,const char* params)
{
	cout << "Usage : " << progName << " " << params << endl;
}

template<class T>
void print2DArray(T** matrix,const int rows,const int cols)
{
  for( int r = 0 ; r < rows ; r++)
  {
    for( int c = 0 ; c < cols ; c++ )
    {
        cout << matrix[r][c] << '\t';
    }
    cout << '\n';
  }
}

template <class T>
T ** create2DArray(const int rows, const int cols, const T defaultValue)
{
  T ** mat = new T*[rows];
  for( int i = 0 ; i < rows ; i++ )
  {
    mat[i] = new T[cols];
    for( int j = 0 ; j < cols ; j++ )
    {
        mat[i][j] = defaultValue;
    }
  }

  return mat;
}

int** createAndRandomize2DIntArray(const int rows, const int cols)
{
    int n = rows*cols;
    int**  mat=new int*[rows];
        for( int i = 0 ; i < rows ; i++)
            {
                  mat[i] = new int[cols];
                  for( int j = 0 ; j < cols ; j++ )
                  {
                      mat[i][j] = rand()%n;
                  }
           }
    return mat;
}

template <class T>
void del2DArray(T** mat, const int rows)
{
      for( int i = 0 ; i < rows ; i++ )
              delete [] mat[i];

          delete [] mat;
}

template <class T>
void createAndInput2DArray( T**&matrix, const int rows, const int cols)
{
  matrix = new T*[rows];
  for( int i = 0 ; i < rows ; i++ )
  {
    matrix[i] = new T[cols];
    for( int j = 0 ; j < cols ; j++ )
    {
      cout << "enter value for matrix[" << i << "][" << j << "] : " ;
      cin >> matrix[i][j];
    }
  }
}
#endif
