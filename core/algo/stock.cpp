/*
    Say you have an array for which the ith element is the price of a given stock on day i.
    If you were only permitted to buy one share of the stock and sell one share of the stock,
    design an algorithm to find the best times to buy and sell.
*/

// bool stock( int* stockPrices, int length, int& buy, int& sell)
// {
// 	if( length < 2 )
// 		false;
// 	buy = 0 ;
// 	sell = 0;
// 	int nextBuy = 0 ;
// 	int nextSell = 0 ;


// 	int diff = stockPrices[sell] - stockPrices[buy];
// 	for( int i = 1 ; i < length ; i++ )
// 	{
// 		if( stockPrices[i] < stockPrices[buy] )
// 		{
// 			nextBuy = i ;
// 		}
// 		else if( stockPrices[sell] < stockPrices[i])
// 		{
// 			nextSell = i ;
// 		}

// 		if( nextSell > nextBuy )
// 		{
// 			int nextDiff = stockPrices[nextSell] - stockPrices[nextBuy];
// 			if( diff < nextDiff )
// 			{
// 				buy = nextBuy ;
// 				sell = nextSell;
// 				diff = nextDiff;
// 			}
// 		}
		// diff = stockPrices[sell] - stockPrices[buy];
		// if( stockPrices[i] < stockPrices[buy])
		// {
		// 	int nextDiff = stockPrices[sell] - stockPrices[i];
		// 	if( nextDiff > diff )
		// 	{
		// 		buy = i;
		// 	}
		// }
		// else if( stockPrices[i] > stockPrices[sell])
		// {
		// 	int nextDiff = stockPrices[i] - stockPrices[buy];
		// 	if( nextDiff > diff )
		// 	{
		// 		sell = i ;
		// 	}
		// }
	// }

	// if( )
// }

    void getBestTime(int stocks[], int sz, int &buy, int &sell) {
  int min = 0;
  int maxDiff = 0;
  buy = sell = 0;
  for (int i = 0; i < sz; i++) {
    if (stocks[i] < stocks[min])
      min = i;
    int diff = stocks[i] - stocks[min];
    if (diff > maxDiff) {
      buy = min;
      sell = i;
      maxDiff = diff;
    }
  }
}

int main()
{
	
}