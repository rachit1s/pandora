#include<iostream>
#include "cpputils.h"

using namespace std;

int main()
{
	int SIZE = 100;
	int array[SIZE];
	cout << "Array after just creation.\n";
	printArray(array,SIZE);
//	std::fill_n(array,SIZE,-1);
	std::fill(array, array+SIZE, -1);
	cout << "Array after filling with -1.\n";
	printArray(array,SIZE);

	return 0;
}
