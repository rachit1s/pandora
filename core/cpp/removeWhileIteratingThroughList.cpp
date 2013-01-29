#include <iostream>
#include <list>

using namespace std;

int main()
{
	list<int> ints;
	for( int i = 0 ; i < 10 ; i++ )
		ints.push_back(i);


	list<int>::iterator iter = ints.begin();
	cout << "after insertion : list.size() : " << ints.size() << endl;
	for( ; iter != ints.end() ; iter++ )
		cout << (*iter) << ",";

	iter = ints.begin();

	while(iter != ints.end())
	{
		if( (*iter) % 2 == 0)
		{
			cout << "removing : " << (*iter) << endl ;
			ints.erase(iter++);
		}
		else
			iter++;
	}

	iter = ints.begin();

	cout << "\nafter removal list.size() : " << ints.size() << endl;
	for( ; iter != ints.end() ; iter++ )
		cout << (*iter) << ",";

	return  0; 

}