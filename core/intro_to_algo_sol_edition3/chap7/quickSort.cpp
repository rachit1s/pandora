#include <iostream>
#include "../../cpplib/cpputils.h"

using namespace std;

int main()
{
	int size; 
	cout << "Enter no. of elements : ";
	cin >> size;

	int* array;
	inputAndCreateArray(array, size);

	print("Your Input : ",array,size);

	// quickSort(array,0,size-1);
	// randomizedQuickSort(array,0,size-1);
	// hoareQuickSort(array,0,size-1);

	// tailRecursiveQuickSort(array,0,size -1);
	iterativeQuickSort(array,0,size -1);

	print("Sorted Array : ", array, size);

	return 0;
}