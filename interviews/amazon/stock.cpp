/*
b. Set of stocks for each day have been given. Need to find the days on which I buy and sell share to earn max profit, alongwith finding the max profit.
*/

#include <iostream>
using namespace std;

int stocks(int* prices ,int len)
{
	// int diff= 0 ;
	int maxDiff = 0 ;
	int buy = 0 ;
	int sell = 0 ;
	int min = 0 ;
	for( int i= 0 ;i < len ;i++)
	{
		cout << "current price: " << prices[i] << endl;
		if( prices[i] < prices[min])
		{
			cout << "current price : " << prices[i] << " is less then min price :" << prices[min] << endl;
			min = i ;
		}


		int diff= prices[i] - prices[min];
		cout << "current diff : " << diff << endl;
		if( maxDiff < diff)
		{
			cout << "current diff " << diff << " is more than maxDiff :" << maxDiff << endl;
			buy = min;
			sell = i ;
			maxDiff = diff;
			cout << "buy = " << buy << ", sell= " << sell << endl;
		}
	}

	return maxDiff;
}

int main()
{
	int prices[] = {3,2,4,11,1,5,13,3,12,1};

	int profit = stocks(prices,10);

	cout << "profit : " << profit << endl;

	return 0 ;

}