/*
Fuzzy sorting of intervals
Suppose We have n closed intervals given by the array I, that is I =
[ab bl], [a2, b2], . . . , [am bn]. The algorithm for fuzzy sorting is as follows,
Fuzzysort takes as input the interval array, beginning index, p and the
end index T‘ of the sub-array which is to be sorted, in other words We
sort I[p...r]. When Fuzzysort returns, the array I[p...1“] will be
sorted.
Fuzzysort uses a procedure called Fuzzypartition, Which again takes
as input I,p,1", considers I[1"] as the pivot, and rearranges I as fol-
lows: Fuzzypartition returns a two-numbered value (ql, qT). The re-
arrangement will be such that I[p. . .ql] and I[qr . . 4"] have to be sorted,
and I[ql+1 . . .q,_1] is in the right place.

*/

#include <iostream>
#include "../../cpplib/cpputils.h"
#include <utility>

ostream& operator << (ostream& out, const pair<int,int>& p)
{
	cout << "(" << p.first << "," << p.second << ")";
	return out;
}

istream& operator >> (istream& in, pair<int,int>& p)
{
	cin >> p.first;
	cin >> p.second;
}

// template<typename pair<int,int>>
void swap(pair<int,int>& t1, pair<int,int>& t2)
{
	cout << "swapping : " << t1 << " and " << t2 << endl;
  pair<int,int> tmp = t1;
  t1 = t2;
  t2 = tmp;
}
	
pair<int,int> fuzzyPartition(pair<int,int>* array, int startIndex, int endIndex)
{
	cout << "fuzzyPartition called with : startIndex : " << startIndex << ", endIndex : " << endIndex << endl;
	int x = array[endIndex].first;

	int i = startIndex - 1;

	cout << "pivot : " << array[endIndex] << " and x = " << x << endl;
	
	for( int j = startIndex; j < endIndex ; j++ )
	{
		cout << "first loop for " << array[j] << endl;
		if( array[j].first < x )
		{
			i = i + 1;

			// swap i and j
			swap(array[i],array[j]);
			// cout << "swapping : " << array[i] << " and " << array[j] << endl;
			// pair<int,int> iValue = array[i];
			// array[i] = array[j];
			// array[j] = iValue;
		}
	}

	swap(array[endIndex],array[i+1]);
	cout << "i = " << i << endl;
	print1DArray("array after first loop : ", array, startIndex, endIndex);
	// pair<int,int> tmp = array[endIndex];
	// array[endIndex] = array[i+1];
	// array[i+1] = tmp;

	// .. find the overlaping partitions
	int q = startIndex -1 ;
	for( int k = startIndex; k <= i ; k++ )
	{
		cout << "second loop for : k = " << k << " and value=" << array[k] << endl ;
		if( array[k].second >= x )
		{
			q = q + 1;
			swap(array[k],array[q]);
			// pair<int,int> kValue = array[k];
			// array[k] = array[q];
			// array[q] = array[k];
		}
	}

	print1DArray("array after second loop : " , array, startIndex,endIndex);

	cout << "finally : q = " << q << endl;  
	for( int l = startIndex ; l <= q ; l++ )
	{
		cout << "third loop for : l = " << l << endl ;
		cout << "swaping indexes : " << l << "," << (i - l + startIndex) << endl; 
		// swap all elements
		swap(array[l],array[i-l+startIndex]);
		// pair<int,int> lValue = array[l];
		// array[l] = array[i-l+ startIndex];
		// array[i-l+startIndex] = lValue;
	} 

	print1DArray("array after third loop : " , array, startIndex,endIndex);

	pair<int,int> p(i-q+startIndex-1,i+2);

	cout << "returning : " << p << endl;
	return p;
}

void fuzzySort(pair<int,int>* array, int startIndex, int endIndex)
{
	cout << "fuzzySort called with : startIndex : " << startIndex << " , endIndex : " << endIndex << endl;
	if( startIndex < endIndex )
	{
		pair<int,int> pivotRange = fuzzyPartition(array,startIndex,endIndex);

		fuzzySort(array,startIndex,pivotRange.first);
		fuzzySort(array,pivotRange.second, endIndex);
	}
}

int main()
{
	// pair<int,int> p1(1,2);
	// pair<int,int> p2(2,1);
	// p2 = p1 ;
	// cout << p2 << endl; 

	int noOfIntervals;
	cout << "Enter the number of intervals : ";
	cin >> noOfIntervals;

	cout << "Enter " << noOfIntervals << " intervals x followed by y : \n";
	pair<int,int>* array;
	inputAndCreateArray(array,noOfIntervals);

	print1DArray("Initial Array : ", array,0,noOfIntervals - 1);

	fuzzySort(array,0,noOfIntervals-1);

	print1DArray("final answer : ", array, 0, noOfIntervals-1);

	delete array;
	
}