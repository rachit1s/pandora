/*
KMP String Matching
*/
#include <iostream>
#include "../../cpplib/cpputils.h"

using namespace std;

/*
assume first letter of pattern is useless
*/
int* computePrefixFunction(char* pattern, int length )
{
	int * pi ;
	createAndInitArray(pi,length,0);

	pi[1] = 0 ;
	int k = 0 ;
	for( int q = 2 ; q < length ; q++ )
	{
		cout << "q = " << q << endl;
		while( k > 0 && pattern[k+1] != pattern[q])
		{
			cout << "k =" << k << "\npi[k]" << pi[k] << endl;
			k = pi[k];
		}	

		if( pattern[k+1] == pattern[q])
		{
			cout << "pattern["<< k+1 << "] == pattern[" << q << "]\n so setting k = k+1 \n"; 
			k = k + 1 ;
		}	

		cout << "finally : k = " << k << " and setting : pi[q] = pi[" << q << "] = k = " << k << endl; 
		pi[q] = k;
	}

	return pi;
}


/*
assume first letter of str and pattern is useless
*/
int * kmpMatcher(char* str, int strLen, char* pattern, int patternLen)
{
	int * pi = computePrefixFunction(pattern,patternLen);
	int * shifts;
	createAndInitArray(shifts,strLen-patternLen+1,-1);

	int q = 0 ;
	for( int i = 1 ; i < strLen ; i++ )
	{
		while( q > 0 && pattern[q+1] != str[i])
		{
			// cout << q << 
			q = pi[q];
		}

		if( pattern[q+1] == str[i])
		{
			q = q + 1;
		}

		if( q == patternLen )
		{
			shifts[q] = 1;
			cout << "found a pattern for shift " << (i - patternLen) << endl;
			q = pi[q];
		}
	}

	delete pi;

	return shifts;
}


/*
wiki
algorithm kmp_table:
    input:
        an array of characters, W (the word to be analyzed)
        an array of integers, T (the table to be filled)
    output:
        nothing (but during operation, it populates the table)

    define variables:
        an integer, pos ← 2 (the current position we are computing in T)
        an integer, cnd ← 0 (the zero-based index in W of the next 
character of the current candidate substring)

    (the first few values are fixed but different from what the algorithm 
might suggest)
    let T[0] ← -1, T[1] ← 0

    while pos is less than the length of W, do:
        (first case: the substring continues)
        if W[pos - 1] = W[cnd], 
          let cnd ← cnd + 1, T[pos] ← cnd, pos ← pos + 1

        (second case: it doesn't, but we can fall back)
        otherwise, if cnd > 0, let cnd ← T[cnd]

        (third case: we have run out of candidates.  Note cnd = 0)
        otherwise, let T[pos] ← 0, pos ← pos + 1
*/
// pattern begins at 0th position itself
int* computePiFuction(char* W, int len)
{
	int * T ;
	createAndInitArray(T,len,-1);

	T[0] = -1;
	T[1] = 0;
	int pos = 2 ,cnd = 0 ;

	while( pos < len)
	{
		cout << "considering pos = " << pos << " and cnd = " << cnd << endl;
		cout << "W[pos-1] = " << W[pos-1] << " and W[cnd] = " << W[cnd] << endl;

		if( W[pos - 1] == W[cnd])
		{
			cout << "since W[pos-1] == W[cnd] " << endl;
			cnd = cnd + 1;
			T[pos] = cnd;
			cout << "cnd = " << cnd << " and T[pos] = " << T[pos] ;
			pos = pos + 1;
			cout << " and pos = " << pos << endl;
		}
		else if( cnd > 0 )
		{
			cout << "as W[pos-1] != W[cnd ] but cnd > 0  i.e. cnd = " << cnd << endl;
			cnd = T[cnd];

			cout << "Therefore cnd = T[cnd] = " << cnd << endl;
		}
		else
		{
			cout << "as W[pos-1] != W[cnd] and cnd is not > 0. \n";
			T[pos] = 0 ;
			cout << "T[pos] = " << T[pos] << endl;
			pos = pos + 1;
			cout << " and incremented pos : " << pos << endl;
		}
	}

	return T;
}

int main()
{
	int length;
	cout << "Enter length of pattern string : ";
	cin >> length;
	cout << "Enter your pattern : ";
	char* pattern = new char[length];
	cin >> pattern ;

	cout << "You entered pattern : " << "'" << pattern << "'" << endl;

	// int * pi = computePrefixFunction(pattern,length);
	int * pi = computePiFuction(pattern,length);

	print("the pi function : " , pi , length );

	return 0 ;
}