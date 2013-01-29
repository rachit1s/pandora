/*
find the pivot in a increaasingly sorted rotated array
*/

#include <iostream>
using namespace std;

int findPivotPoint(int* a, int len)
{
	int start = 0 ;
	int end = len-1;
	int mid = (start+end)/2;
	cout << endl;
	while( mid > start )
	{
		cout << "start = " << start << " end = " << end << " mid = " << mid << endl;
		if( a[mid] > a[end] && 
		    a[mid] > a[start]
		)
		{
			start = mid;
		}
		else if( a[mid] < a[end] && 
			     a[mid] < a[start]
			)
		{
			end = mid;
		}

		mid = (start+end)/2;
	}

	cout << "start = " << start << " end = " << end << " mid = " << mid << endl;

	return mid;
}

int main()
{
	int len;
	cout << "Enter length : ";
	cin >> len;

	cout << "Enter array elements : ";
	int* a = new int[len];
	for( int i =  0 ; i < len ; i++)
	{
		cin >> a[i];
	}

	int pivotPoint = findPivotPoint(a,len);
	cout << "pivotPoint = " << pivotPoint << endl;

	return 0;
}