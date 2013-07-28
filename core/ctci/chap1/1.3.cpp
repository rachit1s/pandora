#include "../../cpplib/includelibs.h"

// shifting chars algo
void removeDuplicates(char* str)
{
	if( str == NULL )
		return ;
                        
	char* curr= str;
	for(char* curr= str ; *curr; curr++)
	{
		char* now = curr + 1;
		char* loc = NULL;
		// find the first of the repeating char
		for(;*now && (*now != *curr); now++);
		// found repeating char
		if( *now )
		{
			loc = now;
			*loc = '\0'; // put the null here
			now++;
			for(;*now;now++)
			{
				if( *now == *curr) {
					*now = '\0';
				}
				else {
					*loc = *now;
					loc++;
					*now='\0'; // to make sure that the end of string is always null terminated
				}
			}
		}
	}
}

int main(int argc, char* argv[])
{
	if( argc == 1 )
	{
		usage(argv[0], " string");
		return 0;
	}

	void (*remDup)(char *) = &removeDuplicates;
	char *str = argv[1];
	cout << str;
	remDup(str);
	cout << " : after removing duplicates : " << str<<endl;

	return 0; 
}

