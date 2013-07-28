/*
 Problem Statement
Given a number k, find the smallest Fibonacci number f that shares a common factor d( other than 1 ) with it. A number is said to be a common factor of two numbers if it exactly divides both of them. 
Input: T test cases where each contains integer k in [2,1000000]
Output: two separate numbers, f and d, where f is the smallest fibonacci number and d is the smallest number other than 1 which divides both k and f.
*/
#include <iostream>
#include <list>
using namespace std;

int getNextFibonacci()
{
	static int prev = 0 ;
	static int curr = 1;
	int next = curr + prev;

	prev = curr;
	curr = next;
	return next;
}

int currPrime = -1;
list<int> primes;
int nextPrimeSearch = 3;
int nextPrimeIndex;
int nextPrime;
int getNextPrime()
{
	if( nextPrimeIndex < primes.size() )
	{
		//
	}
	if( currPrime == -1 )
	{
		currPrime = 2 ;
		primes.push_back(currPrime);
		return currPrime;
	}	
	else
	{
		int prime = nextPrimeSearch;
		
		while( true )
		{
			list<int>::iterator iter = primes.begin();
			bool isPrime = true;
			for( ; iter != primes.end(); iter++ )
			{
				if( prime % (*iter) == 0 )
				{
					isPrime = false;
					break;
				}
			}
			if( isPrime )
				break;
			else
				prime += 2 ;
		}

		nextPrimeSearch = prime + 2;
		primes.push_back(prime);
		currPrime = prime;	

		return currPrime;
	}
}

int findCommonDivisor(int k,int f)
{

}
void getCommonFactor(int k, int& f , int &d)
{
	while(true)
	{
		f = getNextFibonacci();
		
		d = findCommonDivisor(k,f);

		if( d != -1 )
			return;
	}
}

int main()
{
	for( int i = 0 ; i < 10 ; i++ )
		cout << getNextFibonacci() << "," ;
	cout << endl;

	for( int i = 0 ; i < 10 ; i++ )
		cout << getNextPrime() << "," ;

	cout << endl;
	return 0 ;
}