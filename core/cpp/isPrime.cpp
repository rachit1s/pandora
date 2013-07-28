#include "../cpplib/includelibs.h"


bool isPrimeOptimized( long number)
{
	long limit = sqrt(number) + 1;
	cout << "iterating till : " << limit <<endl;
	for( long l = 2 ; l < limit ; l++ )
	{
		if( number%l == 0 )
		{
			return false;
		}
	}

	return true;
}

bool isPrime( long number)
{
	for( long l = 2 ; l < number/2 ; l++ )
	{
		if( number%l == 0 )
		{
			return false;
		}
	}

	return true;
}

int main( int argc, char* argv[] )
{
	if( argc == 1 )
	{
		usage(argv[0], " longNumber ");
		return 0 ;
	}
	
	long number = atol(argv[1]);
	bool (*ip)(long)  = isPrimeOptimized;
 	cout << number << " is " << ( ip(number) == false? "not ": "" ) << "a prime." << endl;
	return 0;
}

