#include <iostream>
#include <string.h>
using namespace std;

bool hucBubble(char* s)
{
	if( NULL == s ) 
		return false;
	
	int length = strlen(s);
	for( int i = 0 ; i < length-1 ; i++ )
	{
		cout << "start checking for " << s[i] << endl ;
		for( int j = i+1 ; j < length ; j++ )
		{
			if( s[i] == s[j] )
			{
				return false;
			}
		}
	}

	return true;
}

bool hucHashed(char* s)
{
	 if( NULL == s )
                return false;
	
	bool* occupied = new bool[256];
	cout << "Print my bool array." << endl;
	for( int i = 0 ; i < 256 ; i++ )
	{
		cout << i << " = " << occupied[i] << endl;
	}
        int length = strlen(s);
        for( int i = 0 ; i < length ; i++ )
	{
		cout << "checking for index = " << int(s[i]) << endl; 
		if(true == occupied[ int(s[i])  ])
		{
			return false;
		}
		else {
			occupied[ int(s[i])  ] = true; // make it filled
		}
	}
	return true;
}
/*
does sorting of characters and then iterate for duplicate characters adjacent to each other
*/
bool hucBinary(char *s )
{
	
}

void usage(char* progname)
{
	cout << "usage : " << progname << " string_to_check" << endl;
}
int main( int argc, char* argv[] )
{
//	for( int i = 0 ; i <= argc ; i++)
//	{
//		cout << i << ":" << argv[i] << endl;
//	}
//	cout << "calling the function" << endl;
	bool (*hasUniqueChars)(char*) = &hucHashed;
	//bool (*hasUniqueChars)(char*) = &hucBubble;
	if( argc == 1 )
	{
		usage(argv[0]);
		return 1;
	}
	char* s = argv[1];
	if( hasUniqueChars(s))
	{
		cout << s << " : has unique characters." << endl;
	}
	else
	{
		cout << s << " : does not have unique characters." << endl;
	}
	// checking output of char[]
	char* defaults = new char[10];
	for( int i = 0 ; i < 10 ; i++ )
	{	
		cout << i << " = " << defaults[i] << ((NULL==defaults[i])? "its null." : "its not null") << ". And ascii value is " << int(defaults[i]) << endl;
	}
}
