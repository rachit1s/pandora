// Describe how you could use a single array to implement three stacks.
#include "../../cpplib/includelibs.h"

// maintains N stacks in a single array
class StackN
{
  private:
    int* array;
    int* tails[];
    int* heads[];
    int n;

  public:
    // totalsize should be greater than n
    // each stack will have atleast one 
    StackN(int n, int totalSize)
    {
      this->n = n;
      array = new int[totalSize];
      tails = new int*[n];
      heads = new int*[n];

      for(int i = 0 ; i < n ; i++ )
      {
        tails[i] = heads[i] = NULL;
      }
    }



};
