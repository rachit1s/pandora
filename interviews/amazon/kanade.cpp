/*
http://en.wikipedia.org/wiki/Maximum_subarray_problem
*/

#include <iostream>
using namespace std;

int sum(int* array, int len)
{
	int sum = 0;
	int startIndex = -1;
	int endIndex = -1;

	int lastSum = 0;
	int lastStartIndex = -1;
	int lastEndIndex = -1;

	for( int i = 0 ; i < len ; i++)
	{
		sum += array[i];
		if( sum < 0 )
		{
			sum = 0 ;
			startIndex = -1;
			endIndex = -1;
		}
		else
		{
			if( startIndex == -1 )
				startIndex = i ;

			endIndex = i;
		}
		
		cout << "current Sum : " << sum << " from " << startIndex << " to " << endIndex << endl;
		
		if( sum > lastSum )
		{
			lastSum = sum;
			lastStartIndex = startIndex;
			lastEndIndex = i;
			cout << "changing  lastSum : " << lastSum << " from " << lastStartIndex << " to " << lastEndIndex << endl;
		}
	}

	cout << "max sum : " << lastSum << " from " << lastStartIndex << " to " << lastEndIndex << endl;

	return lastSum;
}

int main()
{
	int array [] = {-2,1,-3,4,-1,2,1,-5,4};

	int mysum = sum(array,9);

	return 0;
}