/*
Write a method to compute all permutations of a string 
*/
#include <iostream>
#include <list>

using namespace std;

template<typename T>
void printList(list<T>& l)
{
	// template<typename T>
	typename list<T>::iterator iter = l.begin();

	for( ; iter != l.end() ; iter++ )
	{
		cout << (*iter) ;
	}
}

template<typename T>
void printList(const char* message, list<T>& l)
{
	cout << message << " list : size = " << l.size() << " : " ;
	printList(l);
	cout << endl;
}

template<typename T>
void printListOfList( list< list<T> >& ll )
{
	typename list< list<T> >::iterator iter = ll.begin();

	for( ; iter != ll.end() ; iter++)
	{
		cout << "[";
		printList((*iter));
		cout << "]\n";
	}
}

template<typename T>
void printListOfList( const char* message, list< list<T> >& ll )
{
	cout << message;
	printListOfList(ll);
	cout << endl;
}

template<typename T>
list< list<T> > permutations( list<T> str )
{
	printList("list passed : " , str );

	if(str.size() == 1)
	{
		list< list<T> > ll ;
		ll.push_back(str);
		printListOfList("returning single sized list : " , ll);
		return ll;
	}

	
	
	T front = str.front();
	
	cout << "front : " << front << endl;

	list< list<T> > allPerms;
	typename list<T>::iterator iter = str.begin();
	for( ; iter != str.end() ; iter++ )
	{
		//swap front...	
		T tmp = *iter;
		*iter = front;
		list<T> tempList = str;
		tempList.pop_front();

		printList("finding permutations of : ",tempList);
		list< list<T> > perms = permutations(tempList);

		printListOfList("Found permutations to be : ",perms);

		typename list< list<T> >::iterator liter = perms.begin();

		for( ; liter != perms.end() ; liter++ )
		{
			(*liter).push_front(tmp);
			allPerms.push_back((*liter));
		}

		printListOfList("Permutations after appending the character : ",perms);
		*iter = tmp;
	}

	return allPerms;
}

int main()
{
	cout << "Enter a string : " ;
	string str ;
	cin >> str;

	list<char> chars;
	for( int i = 0 ; i < str.length() ; i++ )
	{
		chars.push_back(str[i]);
	}

	cout << "Your char list : " ;
	printList(chars);
	cout << endl;

	list< list<char> > perms = permutations(chars);

	printListOfList("Resultant Permutations : " ,perms);

	return 0;

}