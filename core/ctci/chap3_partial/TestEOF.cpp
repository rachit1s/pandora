#include "../../cpplib/includelibs.h"

int main()
{
    int i = 0 ; 
    do{
      cout << "Enter Integer : ";
      cin >> i;
      if(!cin.eof())
        cout << "Your input was : " << i  << endl ;
      else
        cout << "You entered eof. I will no abort." << endl;
    }
    while( !cin.eof() );

    return 0;
}
