#include "../../cpplib/includelibs.h"

const int SIZE = 256 ; // if the strings are assumed to be ascii

bool anagrams( const char* str1, const char* str2)
{
    int count[SIZE] = {0}; // init by zero

    for( ;*str1 && *str2; str1++,str2++)
    {
        count[int(*str1)]++; // let both compensate
        count[int(*str2)]--; // for each other.
    }

    if( (*str1 && !*str2) || (*str2 && !*str1) )
    {
      cout << "they are of different size." << endl;
      return false; // end here only if they are diff length
    }

    // else check if they compensate completely
    for(int i = 0 ; i < SIZE ; i++)
    {
      if( count[i] != 0 ) return false;
    }

    return true;
}

int main(int argc, char* argv[])
{
  if( argc < 3 ) 
  {
    usage( argv[0], " string1 string2");
    return 0;
  }

  char* str1 = argv[1];
  char* str2 = argv[2];

  bool (*areAnagrams)(const char* str1, const char* str2) = &anagrams;
  cout << str1 << " is " << ( areAnagrams(str1,str2) == false ? "not ": "" ) << "anagram of " << str2 << endl;
  return 0;
}
