/*
8 3  Write a method that returns all subsets of a set 
*/

#include <iostream>
#include <list>

using namespace std;

void printList(list<int>& l)
{
	list<int>::iterator iter = l.begin();

	for( ; iter != l.end() ; iter++ )
	{
		cout << (*iter) << ",";
	}
}

void printListOfList( list< list<int> >& ll )
{
	typedef list< list<int> >::iterator Iter;

	Iter iter = ll.begin();

	for( ; iter != ll.end() ; iter++)
	{
		cout << "[ ";
		printList((*iter));
		cout << "]\n";
	}
}

list<list<int> > subSets( list<int>& set)
{
	// cout << "list passed is : " ;
	// printList(set);

	cout << endl;

	if( set.empty() )
	{
		list< list<int> > ll ;
		list<int> l ;
		ll.push_back(l);
		return  ll;
	}	

	int curr = set.front();
	set.pop_front();
	
	// cout << "curr : " << curr << endl;

	list<list<int> > subsets = subSets(set);

	// cout << "Intermediat subsets : size = " << subsets.size() << " : " ;
	// printListOfList(subsets);
	// cout << endl;

	typedef list<list<int> >::iterator Iter;
	Iter iter = subsets.begin();
	list< list<int> > newSubSets ; no need to create a copy
	for( ; iter != subsets.end() ; iter++ )
	{
		list<int> subSet = *iter;
		
		// cout << "for : subSet = ";
		// printList(subSet);
		// cout << endl;

		newSubSets.push_back(subSet); 
		// list<int> moreSubSet = subSet; // no need to create a copy
		subSet.push_back(curr);

		newSubSets.push_back(subSet);

		// cout << "for : newSubSets = ";
		// printListOfList(newSubSets);
		// cout << endl;

	}

	// cout << "Final Subsets : ";
	// printListOfList(newSubSets);
	// cout << endl;

	return newSubSets;
}

int main()
{
	cout << "Enter elements : ";
	list<int> set ;

	int i = 0 ;
	cin >> i;
	while( !cin.eof() )
	{
		set.push_back(i);
		cin >> i;
	}

	cout << "You entered : ";
	printList(set);

	cout << endl;

	list< list<int> > subsets = subSets(set);
	cout << "Subsets are : ";
	printListOfList(subsets);

	return 0;
}