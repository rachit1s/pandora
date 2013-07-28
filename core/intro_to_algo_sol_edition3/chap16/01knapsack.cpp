/*
The 0-1 knapsack problem is the following. A thief robbing a store ﬁnds n
items. The ith item is worth vi dollars and weighs w i pounds, where vi and w i are
integers. The thief wants to take as valuable a load as possible, but he can carry at
most W pounds in his knapsack, for some integer W . Which items should he take?
(We call this the 0-1 knapsack problem because for each item, the thief must either
take it or leave it behind; he cannot take a fractional amount of an item or take an
item more than once.)
*/

#include <iostream>
#include "../../cpplib/cpputils.h"

using namespace std;

class Result01
{
	public:
		int x;
		int y;
		bool included;
		Result01()
		{
			x = y = 0 ;
			included = false;
		}

		friend ostream& operator << (ostream& out, const Result01& r);
};

ostream& operator << (ostream& out, const Result01& r)
{
	out << "["<< r.x << "," << r.y << "," << (r.included == true ? "i" : "e") <<  "]";

	return out;
}

int knapsack01(int* weights, int* prices, int noOfItems, int maxWeight, int** priceMatrix, Result01** resultMatrix)
{
	for( int i = 0 ; i <= maxWeight ; i++)
		priceMatrix[0][i] = 0 ;
	for( int i = 0 ; i <= noOfItems ; i++ )
		priceMatrix[i][0] = 0;

	for( int w = 1 ; w <= maxWeight ; w++ ) // iterate over all possible weight combinations
	{
		for( int i = 1 ; i <= noOfItems ; i++ ) // iterate over how many items we can include
		{
			for( int cw = w ; cw <= maxWeight ; cw++ )
			{
				if(weights[i] > cw) // cannot include this weight
				{
					priceMatrix[i][cw] = priceMatrix[i-1][cw]; // take the price from one item less
					resultMatrix[i][cw].x=i-1;
					resultMatrix[i][cw].y=cw;
					resultMatrix[i][cw].included = false;
				}
				else
				{
					// include the weight
					int including = prices[i] + priceMatrix[i-1][cw-weights[i]];
					int excluding = priceMatrix[i-1][cw];
					if( including > excluding )
					{
						priceMatrix[i][cw] = including;
						resultMatrix[i][cw].x=i-1;
						resultMatrix[i][cw].y=cw-weights[i];
						resultMatrix[i][cw].included = true;
					}
					else
					{
						priceMatrix[i][cw] = excluding;
						resultMatrix[i][cw].x=i-1;
						resultMatrix[i][cw].y=cw;
						resultMatrix[i][cw].included = false;
					}
				}
			}
		}
	}
}

void printResult(Result01** resultMatrix, int x, int y)
{
	if( x < 1 || y < 1 )
		return ;

	if( resultMatrix[x][y].included == true )
		cout << x << ",";

	printResult(resultMatrix,resultMatrix[x][y].x, resultMatrix[x][y].y);
}
int main()
{
	int maxWeight ;
	cout << "Enter the max weight : " ;
	cin >> maxWeight;

	int noOfItems;
	cout << "Enter number of Items : " ;
	cin >> noOfItems;

	int* weights;
	cout << "Enter weights of Items (please put dummy 0 for first Item): " ;
	inputAndCreateArray(weights,noOfItems+1);

	int* prices;
	cout << "Enter prices of Items (please put dummy 0 for first Item) : " ;
	inputAndCreateArray(prices,noOfItems+1);

	int** priceMatrix = create2DArray(noOfItems+1,maxWeight+1, 0);
	Result01 r;

	Result01** resultMatrix = create2DArray(noOfItems+1,maxWeight+1, r);

	knapsack01(weights,prices,noOfItems,maxWeight,priceMatrix,resultMatrix);

	print("The price Matrix is : \n",priceMatrix,noOfItems+1,maxWeight+1);

	cout << "\n\n So for max price we have to include : " ;
	printResult(resultMatrix,noOfItems,maxWeight);
	cout << "\n\n";

	print("\nPrinting complete resultMatrix : \n",resultMatrix,noOfItems,maxWeight);

	return 0;
}
