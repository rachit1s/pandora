/*
test stl stack
*/
#include <iostream>
#include <stack>
#include <exception>

using namespace std;

int main()
{
	stack<char> s;
	try
	{
		char c = s.top();
	}
	catch(exception e)
	{
		cout << e.what() << endl;
	}

	return 0;
}