#ifndef __CPPUTILS_H_INCLUDED__   // if it hasn't been included yet...
#define __CPPUTILS_H_INCLUDED__   //   #define this so the compiler knows it has been included
#include "includelibs.h"
using namespace std;


void printArray(bool *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}
void printArray(int *array, long SIZE, char delimiter)
{
	for( long i = 0 ; i < SIZE ; i++)
		cout << i << ". " << array[i] << delimiter;
}

void printArray(bool *array, long SIZE)
{
	printArray(array,SIZE,'\n');
}
void printArray(int *array, long SIZE)
{
	printArray(array,SIZE,'\n');
}


void usage(const char* progName,const char* params)
{
	cout << "Usage : " << progName << " " << params << endl;
}



#endif
