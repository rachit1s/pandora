#include "../../cpplib/includelibs.h"

int main()
{
    stringstream strstream;
    const char* name = "Nitiraj Singh";
    strstream << "My Name is : " <<name ;

    cout << strstream.str() << endl;

    return 0;

}

