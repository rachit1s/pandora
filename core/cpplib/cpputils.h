#ifndef __CPPUTILS_H_INCLUDED__   // if it hasn't been included yet...
#define __CPPUTILS_H_INCLUDED__   //   #define this so the compiler knows it has been included
#include "includelibs.h"
#include <vector>
#include <list>
using namespace std;

/*
void printArray(bool *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}
*/
template<class T>
T* inputAndCreateArray(T*& array, const int& SIZE)
{
  array = new T[SIZE];
  for( int i = 0 ; i < SIZE ; i++ )
    cin >> array[i];

  return array;
}

template<class T>
T* createAndInitArray(T*& array, const int& SIZE, T def)
{
  array = new T[SIZE];
  for( int i = 0 ; i < SIZE ; i++ )
    array[i] = def;

  return array;
}


template <class T>
void print1DArray(const T* array, const int& SIZE)
{
  for( int i = 0 ; i < SIZE ; i++ )
    cout << array[i] << '\t';
}

template <class T>
void print1DArray(const T *array, const int& from, const int& to)
{
  for( int i = from ; i <= to ; i++ )
    cout << array[i] << '\t';
}

template <class T>
void print1DArray(const char* msg, T *array, const int& SIZE)
{
  cout << msg ;
  print1DArray(array,SIZE);
  cout << endl;
}

template <class T>
void print1DArray(const char* msg, T *array, const int& from, const int& to)
{
  cout << msg ;
  print1DArray(array,from,to);
  cout << endl;
}


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
void print(const vector<T>& vec)
{
  typename vector<T>::const_iterator iter = vec.begin();
  while( iter != vec.end() )
  {
    cout << (*iter) << ',';
    iter++;
  }
  cout << endl;
}
template<class T>
void print(const char* msg, const vector<T>& vec)
{
  cout << msg ;
  print(vec);
  cout << endl;
}

template<class T>
void print(const list<T>& l)
{
  typename list<T>::const_iterator iter = l.begin();
  while( iter != l.end() )
  {
    cout << (*iter) << ',';
    iter++;
  }
  cout << endl;
}

template<class T>
void print(const char* msg, const list<T>& l)
{
  cout << msg ;
  print(l);
  cout << endl;
}

template<class T>
void print(const char* msg, T** matrix,const int rows,const int cols)
{
  print2DArray(msg,matrix,rows,cols);
}

template<class T>
void print(T** matrix,const int rows,const int cols)
{
  print2DArray(matrix,rows,cols);
}

template<class T>
void print2DArray(T** matrix,const int rows,const int cols)
{
  for( int r = 0 ; r < rows ; r++)
  {
    for( int c = 0 ; c < cols ; c++ )
    {
        cout << matrix[r][c] << "\t";
    }
    cout << '\n';
  }
}

template<class T>
void print2DArray(const char* msg, T** matrix,const int rows,const int cols)
{
  cout << msg ;
  print2DArray(matrix,rows,cols);
  cout << endl;
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
