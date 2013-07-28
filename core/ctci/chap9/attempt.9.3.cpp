/*
9 3  Given a sorted array  of  n  integers  that has been rotated an unknown number  of 
times, give an O(log n) algorithm that finds an element in the array    You may assume 
that the array was originally sorted in increasing order 
EXAMPLE:
Input: find 5 in array (15 16 19 20 25 1 3 4 5 7 10 14)
Output: 8 (the index of 5 in the array)
*/

/*
this algo is not efficient and gives in correct results some times.
*/

#include <iostream>
using namespace std;

template<typename T>
void printArray( T* array, const int& from, const int& to)
{
	for(int i = from ; i <= to ; i++ )
		cout << array[i] << '\t';
}

template<typename T>
void printArray(const char* msg, T* array, const int& from, const int& to)
{
	cout << msg  << " : array from " << from  << " to " << to << endl;
	printArray(array,from,to);
	cout << endl;
}

template<typename T>
void swap( T* array, int from, int to )
{
	while( to > from )
	{
		T tmp = array[to];
		array[to] = array[from];
		array[from] = tmp;
		to-- ;
		from++;
	}
}

template<typename T>
void rotateArray( T* array, const int& size, const int& times)
{
	if( times <= 0 )
		return ;

	int high = size - times;
	swap(array,0,high-1);
	swap(array,high,size-1);
	swap(array,0,size-1);
}

int quickSearch(int* array, int num, int low, int high)
{
	cout << "quickSearch called with low : " << low << ", high : " << high << endl;
	printArray("Array = ",array,low,high);
	if( low > high )
		return -1 ; // not found 

	int mid = (low + high)/2;

	if( array[mid] == num)
		return mid; // found

	if( num < array[mid] )
		return quickSearch(array, num,low,mid-1);
	else
		return quickSearch(array,num,mid+1,high);
}

int modifiedQuickSearch(int* array, int num, int low, int high)
{
	cout << "modifiedQuickSearch called with low : " << low << ", high : " << high << endl;
	printArray("Array = ",array,low,high);

	if( low > high )
		return -1 ; // not found

	int mid = (low + high)/2;

	if( array[mid] == num)
		return mid; // found

	int low1 = low;
	int high1 = mid - 1 ;
	int low2 = mid+1;
	int high2 = high;
	// check if this part is rotated
	if( array[low1] >= array[high1]) // yes it is rotated.
	{
		if( num <= array[low1] && num <= array[high1] )
			return modifiedQuickSearch(array,num,low1,high1);
	}	
	else
	{
		if( num >= array[low1] && num <= array[high1] )
			return quickSearch(array,num,low1,high1);
	}

	if( array[low2] >= array[high2]) // yes it is rotated.
	{
		if( num <= array[low2] && num <= array[high2] )
			return modifiedQuickSearch(array,num,low2,high2);
	}	
	else
	{
		if( num >= array[low2] && num <= array[high2] )
			return quickSearch(array,num,low2,high2);
	}
		

	return -1;
}

int main()
{
	int size;
	cout << "Enter size : " ;
	cin >> size;
	int* array = new int[size];

	cout << "Enter numbers : " ;
	for( int i = 0 ; i < size ; i++ )
		cin >> array[i];

	printArray("You entered Array : ", array, 0 , size-1);

	cout << "Enter the number of times to rotate : ";
	int times;
	cin >> times;

	rotateArray(array,size,times);

	printArray("\nRotated Array is : ",array,0,size-1);

	cout << "Enter the number to search : ";
	int num;
	cin >> num;

	int index = modifiedQuickSearch(array,num,0,size-1);
	if( -1 == index)
	{
		cout << "Number not found.";
	}
	else
		cout << "Number found at : " << index << endl;

	return 0;

}