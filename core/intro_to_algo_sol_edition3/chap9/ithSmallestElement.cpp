/*
coremen : 9.2 Selection in expected linear time
 The algorithm RANDOMIZED-SELECT is modeled after
the quicksort algorithm of Chapter 7. As in quicksort, we partition the input array
recursively. But unlike quicksort, which recursively processes both sides of the
partition, RANDOMIZED-SELECT works on only one side of the partition. This
difference shows up in the analysis: whereas quicksort has an expected running
time of ‚.n lg n/, the expected running time of RANDOMIZED-SELECT is ‚.n/,
assuming that the elements are distinct.
*/

#include <iostream>
#include <algorithm>
#include "../../cpplib/cpputils.h"

template<typename T>
int simplePartition(T* array, int startIndex, int endIndex)
{
	print1DArray("simplePartition : array : " , array , startIndex, endIndex );
	int i = startIndex - 1;
	for( int j = startIndex ; j < endIndex ; j++)
	{
		if( array[j] < array[endIndex] )
		{
			i = i + 1;
			swap(array[i],array[j]);
		}
	}

	swap(array[i+1],array[endIndex]);
	print1DArray("after partition : array : ", array, startIndex,endIndex);
	return i + 1;
}

template<typename T>
int randomPartition(T* array, int startIndex, int endIndex)
{
	print1DArray("randomPartition : array : " , array , startIndex, endIndex );
	srand(time(0));
	int randomIndex = ( rand()% (endIndex - startIndex + 1) ) + startIndex ;
	// swap
	cout << "randomIndex : " << randomIndex << " swaping : " << array[endIndex] << " , " << array[randomIndex] << endl;
	swap(array[endIndex],array[randomIndex]);
	  // T randValue = array[random];
	  // array[random] = array[endIndex];
	  // array[endIndex] = randValue;

	return simplePartition(array,startIndex,endIndex);
}

template<typename T>
T ithSmallestElement(T* array, int startIndex, int endIndex, int i, int (*partition)(T* array, int startIndex,int endIndex))
{
	cout << "ithSmallestElement called with : startIndex " <<  startIndex << ", endIndex : " << endIndex << ", i = " << i << endl;
	print1DArray("ithSmallestElement array : ",array,startIndex,endIndex);
	if( startIndex <= endIndex )
	{
		int pivot = partition(array,startIndex,endIndex);
		cout << "pivot found at : " << pivot << endl;
		if( pivot+1 == i ) return array[pivot];
		else if( pivot+1 < i )
			return ithSmallestElement(array,pivot + 1,endIndex,i,partition);
		else
			return ithSmallestElement(array,startIndex,pivot-1,i,partition);
	}

	return -1;
}

// 9.2-3
// Write an iterative version of RANDOMIZED-SELECT.
template<typename T>
T ithIterativeElement( T* array ,int startIndex , int endIndex , int i, int (*partition)(T* array, int startIndex,int endIndex))
{
	while( startIndex <= endIndex )
	{
		int pivot = partition(array,startIndex,endIndex);
		if( pivot + 1 == i ) return array[pivot];
		else if( pivot + 1 < i )
		{
			startIndex = pivot + 1 ;
		}
		else
		{
			endIndex = pivot-1;
		}
	}

	return -1;
}

template<typename T>
T ithIterativeRandomizedElement( T* array ,int startIndex , int endIndex , int i)
{
	return ithIterativeElement(array,startIndex,endIndex,i,randomPartition);
}

template<typename T>
T ithSmallestElement(T* array, int startIndex, int endIndex, int i)
{
	return ithSmallestElement(array,startIndex,endIndex,i,simplePartition);
}

template<typename T>
T ithRandomizedSmallestElement(T* array, int startIndex, int endIndex, int i )
{
	return ithSmallestElement(array,startIndex,endIndex,i,randomPartition);
}

int main()
{
	int noOfElements ;
	cout << "Enter number of elements : ";
	cin >> noOfElements;

	int* array;
	inputAndCreateArray(array,noOfElements);

	print("The input array is : ", array, noOfElements);

	int i ;
	cout << "index i : ";
	cin >> i ;

	// int x = ithSmallestElement(array,0,noOfElements-1,i);
	// int x = ithRandomizedSmallestElement(array,0,noOfElements-1,i);
	int x = ithIterativeRandomizedElement(array,0,noOfElements-1,i);

	cout << "ithSmallestElement is : " << x << endl;

	print("the array after manipultions is : " ,array,noOfElements);

	return 0;

}