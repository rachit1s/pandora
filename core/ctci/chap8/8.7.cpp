/*
8 7  Given an infinite number of quarters (25 cents), dimes (10 cents), nickels (5 cents) and 
pennies (1 cent), write code to calculate the number of ways of representing n cents 
*/

#include <iostream>
#include <list>

using namespace std;


void printCoins( int coin25, int coin10, int coin5, int coin1 , const int& sum)
{
	int currSum = 25*coin25 + 10*coin10 + 5*coin5 + 1*coin1;
	if( currSum == sum )
	{
		cout << "coin25 = " << coin25 << ", coin10 = " << coin10 << ", coin5 = " << coin5 << ", coin1 = " << coin1 << endl;
		return;
	}
	else if( currSum > sum )
	{
		return;
	}

	printCoins(coin25+1,coin10,coin5,coin1,sum);
	printCoins(coin25,coin10+1,coin5,coin1,sum);
	printCoins(coin25,coin10,coin5+1,coin1,sum);
	printCoins(coin25,coin10,coin5,coin1+1,sum);
}

int main()
{
	cout << "Enter n : ";
	int n ;
	cin >> n ;

	printCoins(0,0,0,0,n);

	return 0;
}