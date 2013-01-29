
#include <iostream>
#include <cmath>
#include <list>
using namespace std;

int binaryToInt( const char* num )
{
	char* str = const_cast<char*>(num) ;
	if( NULL == str || *str == '\0')
		return -1 ;

	int ans = 0 ; 
	int len = 1 ;
	while( (*(str + 1)) != '\0' ) 
	{
		// cout << "*str =" << (*str)<< endl;
		str++;
		len++;
	}

	int pow2 = 1;
	// cout << "length = " << len << endl;
	for( int i = 0 ; i < len ; i++)
	{
		ans += ((*str) - '0')*pow2;
		// cout << "ans = " << ans << endl;
		pow2 *= 2;
		str--;
	}

	return ans;
}

void printBinary( unsigned int n)
{
	list<char> rep;
	while(n != 0)
	{	
		char c = ( (n & 1) == 0 ) ? '0' : '1';
		// cout << "inserting c = " << c << endl;
		rep.push_back( c );
		n = n >> 1;
		// cout << "n = " <<  n << endl; 
	}
 
	list<char>::reverse_iterator revIter = rep.rbegin();
	for( ; revIter != rep.rend() ; revIter++ )
	{
		cout << (*revIter) ;
	}
}
void printBinary(const char* msg , int n)
{
	cout << msg;
	printBinary(n);
	cout << endl;
}
int insertMinN( int N, int M, int i , int j)
{
	unsigned int all1 = ~0;
	
	printBinary("all1 : ",all1);

	unsigned int x = all1 << (i+1) ;

	printBinary("x : ",x);

	unsigned int y = pow(2,j) - 1;

	printBinary("y : ",y);

	unsigned int mask = x | y ;

	printBinary("mask : ",mask);

	unsigned int maskedN = N & mask ;

	printBinary("maskedN : ",maskedN);

	unsigned int shiftedM = M << j ;

	printBinary("shiftedM : ",shiftedM);

	unsigned int ans = maskedN | shiftedM ;

	printBinary("ans : ",ans);

	return ans;
}

int main()
{
	// printBinary(4);
	const char* n = "10000000000";
	const char* m = "10011";
	int N = binaryToInt(n);
	int M = binaryToInt(m);

	cout << "N = " << N << " M = " << M << endl;

	int i = 6;
	int j = 2;
	int ans = insertMinN(N,M,i,j);

	printBinary("ans is : ", ans);

	return 0;
}