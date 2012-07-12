#include "../../cpplib/includelibs.h"

const char* reverseString1(const char* str)
{
	int len = strlen(str);
	char* rev = new char[len+1];
	strcpy(rev,str);
	cout << "after copy rev = " << rev << endl;
	rev[len] = '\0';
	char* low = rev;
	char* high = rev + len - 1 ;
	while(low < high)
	{
		cout << "low:" << *low << " :: high:" << *high << endl;
		char c = *low;
		*low = *high;
		*high = c;
		low++ ;
		high--;		
	}

	return rev;
}

int main(int argc, char* argv[])
{
	if( argc == 1 ) 
	{
		usage(argv[0]," stringToReverse  ");
		return 0;
	}
	const char* str = argv[1];
	const char* (*reverse) (const char* ) = &reverseString1;
	const char* rev = reverse(str);
	cout << "reverse of " << str << ":" << rev << endl;
	delete rev;
	return 0;
}
