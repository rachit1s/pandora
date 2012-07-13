#include "../../cpplib/includelibs.h"

const char oldValue = ' ';
const char* newValue = "%20";

char* replace(const char* str, const char c, const char* rep)
{
  int len = strlen(str);
  int nlen = strlen(rep);
  // count spaces
  const char* cptr = str;
  int count = 0 ;
  while(*cptr)
  {
    if( *cptr == c ) count++;
    cptr++;
  }

  char* nStr = new char[ len + count*nlen +1 ];
  cptr = str;
  char* ncptr = nStr;
  while(*cptr)
  {
    if( *cptr == c ) 
    {
      // replace occurence
      const char * rptr = rep;
      while(*rptr)
      {
        *ncptr = *rptr;
        ncptr++ ;
        rptr++;
      }
    }
    else
    {
      *ncptr = *cptr;
      ncptr++;
    }
    cptr++;
  }
  *ncptr = '\0';

  return nStr;
}

int main(int argc, char* argv[])
{
  if( argc < 2 )
  {
 //   usage(argv[0], " string c nc\n where string='the string in which the replacement happens'\nc='the single character to replace'\nnc='the string to be replaced'");
    usage(argv[0], " string");
    return 0;
  }

  char* str = argv[1];

  char* (*replacer)(const char* str,const char c, const char* r) = &replace;
  char* rep = replacer(str,oldValue,newValue);
  
  cout << str << "-->" << rep << endl;
  delete rep;
  return 0;
}
