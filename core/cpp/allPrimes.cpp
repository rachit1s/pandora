#include "../cpplib/includelibs.h"

void allPrimes( long lastNumber )
{
	bool* nums = new bool[lastNumber] ;
//	cout << "Initial values :" << endl; 
//	printArray(nums,lastNumber);

	for(long l = 2 ; l < lastNumber ; l++ )
	{
		if( nums[l-1] == 0 )
		{
			// mark all number divisible by this
			long i = l ;
			while( (i = i + l) <= lastNumber )
			{
				nums[i-1] = 1;
			}
		}
	}
	cout << "All primes are" << endl;
	for( long l = 0 ; l < lastNumber ; l++ )
	{
		if( nums[l] == 0 )
			cout << l+1 << ",";
	}
	cout << endl;
}
int main(int argc, char* argv[])
{
	if( argc == 1 )
	{
		usage(argv[0], " lastNumberToCheck  ");
		return 0;
	}

	long lastNumber = atol(argv[1]);
	allPrimes( lastNumber  );
	return 0;
}

