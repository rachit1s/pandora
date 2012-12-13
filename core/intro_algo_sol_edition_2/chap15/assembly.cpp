/*
15.1 Assembly-line scheduling 
*/

#include <iostream>
#include "../../cpplib/cpputils.h"
using namespace std;


void findAssebly(int n , int** a, int** e, int** f, int** l )
{
	f[0][0] = a[0][0] + e[0][0];
	f[1][0] = a[1][0] + e[1][0];

	for( int i = 1; i < n ; i++ )
	{
		// for assembly 1
		int f0i0 = f[0][i-1] + a[0][i];
		int f0i1 = f[1][i-1] + e[1][i-1] + a[0][i];
		if(f0i0 <= f0i1)
		{
			f[0][i] = f0i0;
			l[0][i] = 0;
		}
		else
		{
			f[0][i] = f0i1 ;
			l[0][i] = 1;
		}

		int f1i0 = f[0][i-1] + e[0][i-1] + a[1][i];
		int f1i1 = f[1][i-1] + a[1][i];

		if( f1i1 <= f1i0 )
		{
			f[1][i] = f1i1;
			l[1][i] = 1;
		}
		else
		{
			f[1][i] = f1i0;
			l[1][i] = 0;
		}
	}
}
int main()
{
	int n = 0 ; 
	cout << "Enter n : " ;
	cin >> n;

	int **a, **e, **f, **l;
	createAndInput2DArray(a,2,n);	// the actual time taken at station ith of two lines
	createAndInput2DArray(e,2,n);	// stores the time taken to switch from ith to (i+1)th of opposite line
	f = create2DArray(2,n,0);	 // stores the least time taken to clear the assebly's ith station
	l = create2DArray(2,n,0);	 // the previous station was from line 0 or line 1 for the optimal path

	findAssebly(n,a,e,f,l);

	cout << "The calculated functions are : \n";
	print2DArray(f,2,n);
	cout << endl;

	cout << "The calculated lines are : \n";
	print2DArray(l,2,n);
	cout << endl;

	// find the shortest path.
	if( f[0][n-1] < f[1][n-1] )
	{
		cout << "The smallest path is the exit through line 0 " ;
	}
	else
		cout << "The smallest path is the exit through line 1 " ;
}