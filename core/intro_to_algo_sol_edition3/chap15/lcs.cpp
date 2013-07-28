/*
15.4 Longest common subsequence
*/

#include <iostream>
#include <string>
#include "../../cpplib/cpputils.h"

using namespace std;

/*
O(xLength * yLength)
bottoms-up
lcsSizes[xLength+1][yLength+1] 
seq[xLength][yLength]
*/
static const int EQUAL = 1 ;
static const int GoOneXLess = 2;
static const int GoOneYLess = 3;

void LCSIter(string x, string y, int** lcsSizes, int** seq )
{
	int xLength = x.length();
	int yLength = y.length();

	for( int i = 0 ; i < xLength ; i++ )
		lcsSizes[i][0] = 0 ;
	for( int i = 0 ; i < yLength ; i++ )
		lcsSizes[0][i] = 0 ;

	for( int i = 1 ; i <= xLength ; i++ )
	{
		for( int j = 1 ; j <= yLength ; j++ )
		{
			if( x[i-1] == y[j-1] )
			{
				lcsSizes[i][j] = lcsSizes[i-1][j-1] + 1;
				seq[i-1][j-1] = EQUAL;
			}
			else if( lcsSizes[i-1][j] < lcsSizes[i][j-1])
			{
				lcsSizes[i][j] = lcsSizes[i][j-1];
				seq[i-1][j-1] = GoOneYLess;
			}
			else
			{
				lcsSizes[i][j] = lcsSizes[i-1][j];
				seq[i-1][j-1] = GoOneXLess;
			}
		}
	}
}

int main()
{
	string x, y;
	cout << "Enter X : " ;
	cin >> x ;
	cout << "Enter Y : " ;
	cin >> y ;

	int** lcsSizes = create2DArray(x.length()+1,y.length()+1,0);
	int** seq = create2DArray(x.length(),y.length(),0);

	LCSIter(x,y,lcsSizes,seq);

	print2DArray("seq is : \n", seq, x.length(),y.length());

	cout << "lcs size is : " << lcsSizes[x.length()][y.length()] << endl;
}