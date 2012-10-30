#include<iostream>

using namespace std;

void reverseString(char* str)
{
  char* end = str ;
  while( '\0' != *end ) end++ ;
  end--;

  char* start = str;
  while( start < end )
  {
    char tmp = *start;
    *start = *end;
    *end= tmp;
    start++;
    end--;
  }
}

int main()
{
  while(true)
  {
  char* s = new char[100] ;
  cout << "Enter a string : " ;
  cin >> s;

  cout << "You entered : " << s << endl;

  reverseString(s);

  cout << "reversed String : " << s << endl ;
  }

  return 0;
}
