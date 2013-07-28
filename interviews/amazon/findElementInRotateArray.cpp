/*
Finding an element in rotated sorted array..
*/

#include <iostream>
using namespace std;

int findElement2(int* array, int len, int element)
{
	int low = 0, high = len-1, mid = 0;

	while( low <= high )
	{
		mid = ( low + high )/2;
		cout << "low = " << low << ", high = " << high << ", mid = " << mid << endl;
		if( element == array[mid])
			return mid;
		else
		{
			if( array[mid] > array[high] )
			{
				if( element > array[mid] )
				{
					low = mid + 1;
				}
				else
				{
					if( element >= array[low])
					{
						high = mid - 1 ;
					}
					else
					{
						low = mid + 1;
					}
				}
			}
			else
			{
				if( element < array[mid])
				{
					high = mid -1 ;
				}
				else
				{
					if( element <= array[high])
					{
						low = mid + 1;
					}
					else
					{
						high = mid -1 ;
					}
				}
			}
		}
	}

	return -1;
}
// this is wrong implementation : the point is that we should first compare mid with high or low 
// before comparing with element
int findElement(int* array, int len, int element)
{
	int low = 0, high = len-1, mid = 0;
	

	while( low <= high )
	{
		mid = (low + high)/2;
		cout << "low = " << low << ", high = " << high << ", mid = " << mid << endl;
		if( array[mid] == element )
			return mid;

		if(element > array[mid])
		{
			cout << "element=" << element << "  >  mid=" << array[mid] << endl;
			if( array[mid] > array[high])
			{
				low = mid + 1;
			}	
			else 
			{
				if( element > array[high])
				{
					high = mid;
				}
				else
				{
					low = mid + 1;
				}
			}
		}
		else
		{
			cout << "element=" << element << "   < mid=" << array[mid] << endl; 
			if( element > array[high])
			{
				high = mid;
			}
			else
			{
				low = mid +1 ;
			}
		}
	}

	return -1;
}

int main()
{
	int array[] = {7,8,1,2,3,4,5,6};

	int index = findElement2(array,8,10);

	cout << "index = " << index << endl;

	return 0;
}