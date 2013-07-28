
/*
19 7  You are given an array of integers (both positive and negative)  Find the continuous 
sequence with the largest sum  Return the sum  
EXAMPLE
Input: {2, -8, 3, -2, 4, -10}
Output: 5 (i e , {3, -2, 4} )
*/

#include <iostream>
#include <climits>
#include "../../cpplib/cpputils.h"
using namespace std;

// assumes that the largest means largest positive sum
void largestSum( int* values, int numberOfValues )
{
	// int* starts = create1DArray(numberOfValues,-1);
	// int* ends = create1DArray(numberOfValues,-1);

	int start = 0,end = -1, overAllMax = INT_MIN, currStart = 0 , currEnd = -1, currMax = INT_MIN, currValue = 0 ;
	for(int i = 0 ; i < numberOfValues ; i++ )
	{
		currValue = currValue + values[i];
		if( currValue > currMax )
		{
			currMax = currValue;
			currEnd = i;
		}

		if( currValue < 0 )
		{
			// see if the currMax is more than overAllMax -- store it
			if( currMax > overAllMax )
			{
				start = currStart;
				end = currEnd;
				overAllMax = currMax;
			}
			// reset the start
			currValue = 0 ;
			currMax = INT_MIN;
			currStart = i+1;
			currEnd = -1;
		}
		
	}

	// see if the currMax is more than overAllMax -- store it
	if( currMax > overAllMax )
	{
		start = currStart;
		end = currEnd;
		overAllMax = currMax;
	}

	cout << "overAllMax = " << overAllMax << ", start = " << start << ", end = " << end << endl;
}

int main()
{
	int numberOfValues;
	cout << "Enter number of vlaues :" ;
	cin >> numberOfValues;
	int* array ;
	inputAndCreateArray(array,numberOfValues);

	largestSum(array,numberOfValues);

	return 0;
}