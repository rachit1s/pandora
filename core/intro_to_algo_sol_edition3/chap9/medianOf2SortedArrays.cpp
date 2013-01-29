/*
9.3-8
Let X and Y be two arrays, each containing n numbers already in
sorted order. Give an O.lg n/-time algorithm to ﬁnd the median of all 2n elements
in arrays X and Y .
*/
#include <iostream>
using namespace std;

int medianOf2SortedArrays(int* x, int xStart, int xEnd, int* y, int yStart, int yEnd)
{
	int length = xEnd - xStart + 1;
	bool isEvenLength = ((length % 2 ) == 0);
	int xMedianIndex = (xEnd - xStart + 1)/2;
	int xMedian = x[xMedianIndex];

	int yMedianIndex = (yEnd - yStart + 1)/2;
	int yMedian = y[yMedianIndex];

	if( x)
}
int medianOf2SortedArrays(int* x, int* y, int size) // where size is the size of both the arrays
{
	return medianOf2SortedArrays(x,0,size-1,y,0,size-1);
}

int main()
{

}