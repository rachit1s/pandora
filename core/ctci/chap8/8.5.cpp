/*
8 5  Implement an algorithm to print all valid (e g , properly opened and closed) combi-
nations of n-pairs of parentheses 
EXAMPLE:
input: 3 (e g , 3 pairs of parentheses)
output: ()()(), ()(()), (())(), ((()))
*/
#include <iostream>
#include <list>
#include <string>

using namespace std;

template<typename T>
void printList(list<T>& l)
{
	// template<typename T>
	typename list<T>::iterator iter = l.begin();

	for( ; iter != l.end() ; iter++ )
	{
		cout << (*iter) << ", ";
	}
}

template<typename T>
void printList(const char* message, list<T>& l)
{
	cout  << endl << message << " list : size = " << l.size() << " : " ;
	printList(l);
	cout << endl;
}

int getOpen(string& s)
{
	int count = 0 ;
	for( int i = 0 ; i < s.length() ; i++)
	{
		if( '(' == s[i] )
		{
			count++;
		}
	}

	return count;
}
list<string> findParenthesis(int n)
{
	list<string> l;
	if( n < 1 )
		return l;

	string init;
	l.push_back(init);

	for( int i = 0 ; i < 2*n ; i++ )
	{
		printList("currently in l : ",l);

		list<string> l1 = l ;
		l.clear();
		for( list<string>::iterator iter = l1.begin(); iter != l1.end() ; iter++ )
		{
			cout << "current string : " << (*iter) << endl;
			int open = getOpen(*iter);
			int closed = (*iter).length() - open;

			if( closed < open )
			{
				l.push_back((*iter) + ")");
			}

			if( open < n )
			{
				l.push_back((*iter) + "(");
			}
		}
	}
}

int main()
{
	cout << "Enter a number : " ;
	int n ;
	cin >> n;

	list<string> p = findParenthesis(n);

	printList("Final Result :  " ,p);

	return 0;
}