#include <iostream>

using namespace std;

int main()
{
	int end = 5 ;
	for( int i = 0 ; i != end ; i++ )
	{
		cout << "i = " << i;
		cout << "end = " << end++ ;
	}

	return 0 ;
}