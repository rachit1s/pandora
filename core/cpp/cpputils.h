#include<iostream>

using namespace std;

void printArray(int *array, int SIZE, char delimiter)
{
	for( int i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}

void printArray(int *array, int SIZE)
{
	printArray(array,SIZE,'\n');
}
