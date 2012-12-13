/*
15.2 Matrix-chain multiplication
*/

#include <iostream>
#include <climits>
#include <cstdlib>
#include "../../cpplib/cpputils.h"

using namespace std;


/*
O(exponential)
*/
int matrixChainRecNonOptimal(int * p, int from , int to,int** optimalBreak)
{
	cout << "matrixChainRecNonOptimal called for : from = " << from << ", to : " << to << endl;
	cin.get();
	if( from == to )
		return 0;

	int minimum = INT_MAX;
	for( int l = 0 ; l < (to-from) ; l++ )
	{
		cout << "calculating for length : " << l << endl; 
		int left = matrixChainRecNonOptimal(p,from,from+l,optimalBreak);
		int right = matrixChainRecNonOptimal(p , from+l+1,to,optimalBreak);
		int operations = left + right + p[from-1] * p[from+l] * p[to];
		if( operations < minimum )
		{
			minimum = operations;
			optimalBreak[from][to] = from + l;
		}
	}

	return minimum;
}
/**
O(n3)
bottom up approach.
p[0] contains the rows in first matrix. number of elements in p = number of matrix + 1 
matrix i = p[i-1] x p[i] -> as all matrices in that order are compatible for multiplication
optimalBreak is a [numberOfMatrices+1] x [numberOfMatrices+1] matrix
minimumOperation is a [numberOfMatrices+1] x [numberOfMatrices+1] matrix
**/
void matrixChainMultiplication(int* p,int numberOfMatrices,int** optimalBreak, int** minimumOperation)
{
	for( int i = 0 ; i < numberOfMatrices+1 ; i++ )
	{
		minimumOperation[i][i] = 0 ; // single matrix no break or operation required.
	}

	for( int l = 2 ; l <= numberOfMatrices ; l++ ) // for group length of matrix from 2 to all-1
	{
		cout << "Iterating for length : " << l << endl;
		for( int i = 1; i <= numberOfMatrices-l+1;i++)
		{
			int j = i + l - 1 ;
			cout << "Finding minimum break point between i = " << i << ", j = " << j << endl;
			// for each break
			int minimumOperations = INT_MAX;
			int ob = 0 ; // just to print the output
			for( int k = i ; k < j ; k++ ) // break after kth matrix
			{
				int operations = minimumOperation[i][k] + minimumOperation[k+1][j] + p[i-1] * p[k] * p[j];
				if( operations < minimumOperations)
				{
					minimumOperations = operations;
					optimalBreak[i][j] = ob = k;
				}
			}

			cout << "minimumOperations = " << minimumOperations << " at break point : " << ob << endl << endl;
			minimumOperation[i][j] = minimumOperations;
		}
	}
}

string itoa(int i)
{
	stringstream ss;
	ss << i ;
	return ss.str();
}

string getMatrixMultiplicationString(int ** breakPoints,int from, int to)
{
	cout << "getMatrixMultiplicationString called for from : " << from << ", to : " << to << endl; 
	if( from == to )
	{
		string str = itoa(from);
		return str ;
	}	
	int breakAt = breakPoints[from][to];
	return " ( " + getMatrixMultiplicationString(breakPoints,from,breakAt) + " " + getMatrixMultiplicationString(breakPoints,breakAt+1,to) + " ) ";
}
void printMatrixMultiplication(int** breakPoints, int numberOfMatrices)
{
	string s = getMatrixMultiplicationString(breakPoints,1,numberOfMatrices);
	cout << "mutiplication = " << s << endl;
}

int main()
{
	int numberOfMatrices, *p, **optimalBreak, **minimumOperation;
	cout << "Enter the number of matrices : ";
	cin >> numberOfMatrices;

	cout << "Enter the sizes of matrices such that matrix i is of p[i-1] x p[i] dimension :\n";
	inputAndCreateArray(p,numberOfMatrices+1);

	print1DArray("You entered sizes : ", p, numberOfMatrices+1);

	optimalBreak = create2DArray(numberOfMatrices+1,numberOfMatrices+1,0);
	minimumOperation = create2DArray(numberOfMatrices+1,numberOfMatrices+1,INT_MAX);

	
	// matrixChainMultiplication(p,numberOfMatrices,optimalBreak,minimumOperation);
	// cout << "The minimumOperations for i = 1 to i = numberOfMatrices is : " << minimumOperation[1][numberOfMatrices] << endl ;
	// print2DArray("The minimumOperation Matrix is : \n", minimumOperation , numberOfMatrices+1, numberOfMatrices+1 );
	

	int min = matrixChainRecNonOptimal(p,1,numberOfMatrices,optimalBreak);
	cout << "The minimumOperationations for i = 1 to i = numberOfMatrices is : " << min << endl ;
	

	print2DArray("The optimalBreak Matrix is : \n", optimalBreak , numberOfMatrices+1, numberOfMatrices+1 );

	

	printMatrixMultiplication(optimalBreak,numberOfMatrices);

	return 0;
}