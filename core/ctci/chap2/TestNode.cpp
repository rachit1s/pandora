#include "../../cpplib/includelibs.h"
#include "Node.h"

int main( int argc, char* argv[])
{
  Node<int> node;
  node.setData( 5 );

  cout << node;

  return 0;
}
