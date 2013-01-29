/*
Card Shuffling (30 Points)

Here is an algorithm for shuffling N cards:

1) The cards are divided into K equal piles.
2) The bottom N / K cards belong to pile 1 in the same order (so the bottom card of the initial pile is the bottom card of pile 1).
3) The next N / K cards from the bottom belong to pile 2, and so on.
4) Now the top card of the shuffled pile is the top card of pile 1. The next card is the top card of pile 2,..., the Kth card of the shuffled pile is the top card of pile K. Then (K + 1)th card is the card which is now at the top of pile 1, the (K + 2)nd is the card which is now at the top of pile 2 and so on.

For example, if N = 6 and K = 3, the order of a deck of cards "ABCDEF" (top to bottom) when shuffled once would change to "ECAFDB".

Given N and K, what is the least number of shuffles needed after which the pile is restored to its original order?

Input:
The first line contains the number of test cases T. The next T lines contain two integers each N and K.

Output:
Output T lines, one for each test case containing the minimum number of shuffles needed. If the deck never comes back to its original order, output -1.

Constraints:

K will be a factor of N.
T <= 10000
2 <= K <= N <= 10^9

Sample Input:
3
6 3
4 2
5 5

Sample Output:
6
4
2
*/


#include <iostream>
#include <set>

using namespace std;

int noOfIterations(int n, int k)
{
	//
	int i = 1 ;
	int initialIndex = 0;
	int index = initialIndex;
	set<int> indexes ;
	//cout << "initial index : " << initialIndex << endl;
	while(true)
	{
		index = (index % (n/k)) * k + ( k - (index / (n/k)) -1  ) ;
		//cout << "index = " << index << endl;
		if( index == initialIndex )
			return i;
		set<int>::iterator iter = indexes.find(index);
		if( iter != indexes.end() ) // already reached this index once so it will start the repetition of cycle and will never reach
			return -1;						// initial index
		else
			indexes.insert(index);


		i++;

	}
}

// int main()
// {
// 	int noOfTestCases;
// 	cin >> noOfTestCases;

// 	int n , k;
// 	for( int i = 0 ; i < noOfTestCases ; i++ )
// 	{
// 		cin >> n ;
// 		cin >> k ;
// 		cout << noOfIterations(n,k) << endl;
// 	}
// 	return 0 ;
// }

int main()
{
	int noOfTestCases;
	cin >> noOfTestCases;
	
	int* n = new int[noOfTestCases], *k = new int[noOfTestCases];
	for( int i = 0 ; i < noOfTestCases ; i++ )
	{
		cin >> n[i] ;
		cin >> k[i] ;
	}

	for( int i = 0 ; i < noOfTestCases ; i++ )
	{
		cout << noOfIterations(n[i],k[i]) << endl;
	}
	return 0 ;
}