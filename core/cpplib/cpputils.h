#ifndef __CPPUTILS_H_INCLUDED__   // if it hasn't been included yet...
#define __CPPUTILS_H_INCLUDED__   //   #define this so the compiler knows it has been included
#include "includelibs.h"
#include <vector>
#include <stack>
#include <utility>
#include <list>
#include <cstdlib>
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
T* createAndInitArray(T*& array, const int& SIZE, T def = NULL)
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
void print(const T* array, const int& SIZE)
{
  print1DArray(array,SIZE);
}

template <class T>
void print1DArray(const T *array, const int& from, const int& to)
{
  for( int i = from ; i <= to ; i++ )
    cout << array[i] << '\t';
}

template <class T>
void print(const T *array, const int& from, const int& to)
{
  print1DArray(array,from,to);
}

template <class T>
void print1DArray(const char* msg, T *array, const int& SIZE)
{
  cout << msg ;
  print1DArray(array,SIZE);
  cout << endl;
}

template <class T>
void print(const char* msg, T *array, const int& SIZE)
{
  print1DArray(msg,array,SIZE);
}

template <class T>
void print1DArray(const char* msg, T *array, const int& from, const int& to)
{
  cout << msg ;
  print1DArray(array,from,to);
  cout << endl;
}

template <class T>
void print(const char* msg, T *array, const int& from, const int& to)
{
  print1DArray(msg,array,from,to);
}

template <class T>
void printArray(T *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}

template <class T>
void print(T *array, long SIZE, char delimiter)
{
  printArray(array,SIZE,delimiter);
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
void delete2DArray(T** mat, const int rows)
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

template < class T>
int normalPartition(T* array, int startIndex, int endIndex)
{
  T pivotValue = array[endIndex];
  int i = startIndex - 1;
  for( int j = startIndex; j < endIndex ; j++ )
  {
    if( array[j] <= pivotValue)
    {
      i = i + 1;
      // swap
      T t = array[j];
      array[j] = array[i];
      array[i] = t;
    }
  }

  // put the pivot in position
  array[endIndex] = array[i+1] ;
  array[i+1] = pivotValue;

  return i+1;
}


template< class T>
int hoarePartition(T* array, int startIndex, int endIndex)
{

  cout << "hoarePartition called with : startIndex = " << startIndex << ", endIndex = " << endIndex << endl;
  if( startIndex == endIndex )
    return startIndex ;

  T pivotValue = array[endIndex] ;
  int i = startIndex;
  int j = endIndex;
  while(true)
  {
    while( (  array[j] > pivotValue ) && j > startIndex )
    {
      j = j - 1;
    }

    while( ( array[i] <= pivotValue ) && i < endIndex )
    {
      i = i + 1;
    }

    cout << "hoarePartition : i = " << i << ", j = " << j << endl;
    if( i < j )
    {
      T iValue = array[i];
      array[i] = array[j];
      array[j] = iValue;
    }
    else return j;
  }
}


template< class T>
int randomizedPartition(T* array, int startIndex ,int endIndex )
{
  srand(time(0));
  int random = ( rand() % (endIndex - startIndex + 1) ) + startIndex;
  T randValue = array[random];
  array[random] = array[endIndex];
  array[endIndex] = randValue;

  return partition(array,startIndex,endIndex);
}


template <class T>
void quickSort(T* array, int startIndex, int endIndex)
{
  quickSort(array,startIndex,endIndex,normalPartition);
}

template <class T>
void quickSort(T* array, int startIndex, int endIndex, int (*partition)(T* array, int startIndex, int endIndex))
{
  if( startIndex < endIndex )
  {
    int pivot = partition(array,startIndex,endIndex);
    quickSort(array,startIndex,pivot - 1,partition);
    quickSort(array,pivot + 1, endIndex, partition);
  }
}

template< class T>
void iterativeQuickSort( T* array, int startIndex , int endIndex , int (*partition)(T* array, int startIndex, int endIndex))
{
  stack<pair<int,int> > callStack;
  pair<int,int> init(startIndex,endIndex);
  callStack.push(init);
  while( !callStack.empty() )
  {
    pair<int,int> p = callStack.top();
    callStack.pop();
    if( p.first < p.second )
    {
      int pivot = partition(array,p.first,p.second);
      pair<int,int> p1(p.first,pivot-1);
      callStack.push(p1);
      pair<int,int> p2(pivot+1,p.second);
      callStack.push(p2);
    }
  }
}

template< class T>
void iterativeQuickSort( T* array, int startIndex , int endIndex )
{
  iterativeQuickSort( array, startIndex , endIndex , normalPartition);
}

template< class T>
void tailRecursiveQuickSort(T* array, int startIndex, int endIndex, int (*partition)(T* array, int startIndex, int endIndex) )
{
  while( startIndex < endIndex )
  {
    int pivot = partition(array,startIndex,endIndex);
    tailRecursiveQuickSort(array,startIndex,pivot - 1,normalPartition);
    startIndex = pivot + 1;
  }
}

template <class T>
void tailRecursiveQuickSort(T* array, int startIndex, int endIndex)
{
  tailRecursiveQuickSort(array,startIndex,endIndex,normalPartition);
}

template <class T>
void randomizedQuickSort(T* array, int startIndex,int endIndex )
{
  quickSort(array,startIndex,endIndex,randomizedPartition);
}

template < class T>
void hoareQuickSort(T* array, int startIndex , int endIndex)
{
  quickSort(array,startIndex,endIndex,hoarePartition);
}

#endif
