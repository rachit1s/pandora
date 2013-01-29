/*
find an element in rotated sortead array. assume increasingly sorted initially
*/
#include <iostream>
using namespace std;

int wrongFindElement( int* a, int len, int element)
{
	int start = 0 ;
	int end = len-1;
	int mid = (start+end)/2;

	while( mid >= start )
	{
		cout << "\nstart = " << start << "	end = " << end << "	  mid = " << mid;
		if( a[mid] == element )
			return mid;
		else if( a[mid] < a[end])
		{
			if( element < a[mid])
			{
				end = mid - 1;
			}
			else
			{
				if( element > a[start])
					end = mid - 1;
				else
					start = mid + 1;
			}
		}
		else  // a[mid] > a[end] 
		{
			if( element > a[mid])
			{
				start = mid + 1;
			}
			else if ( element > a[start])
			{
				end = mid - 1;
			}
			else
			{
				start = mid + 1;
			}
		}

		mid = (end+start)/2;
	}

	return -1;
}

int findElement(int a[], int l, int u, int x) { 
    while (l <= u) { 
      int m = (l + u) / 2; 
      if (x == a[m]) {
        return m; 
      } else if (a[l] <= a[m]) {
        if (x > a[m]) {
          l = m+1; 
        } else if (x >=a [l]) {
          u = m-1; 
        } else {
         l = m+1;
        }
      } 
      else if (x < a[m]) u = m-1; 
      else if (x <= a[u]) l = m+1; 
      else u = m - 1; 
    }
    return -1; 
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

	int element;
	cout << "Find the element : ";
	cin >> element;

	int index = findElement(a,0,len-1,element);
	cout << "index = " << index << endl;

	return 0;
}