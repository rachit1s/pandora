/*
let the graph be represented by a matrix where the pointers are from 0th index to 1 index.
ie. a[0][1] means a directed line from 0--->1 and a[1][0] means a directed line 1--->0
*/

#include <iostream>
#include <list>
using namespace std;

bool findPath( int** mat, int length, int from, int to, list<int>& path);
// recursive
bool findPath( int** mat, int length, int from, int to)
{
	list<int> path;
	path.push_back(from);
	bool found = findPath(mat,length,from,to,path);
	if(found)
	{
		cout << "Found the path :\n"; 
		list<int>::iterator iter = path.begin();

		for( ; iter != path.end() ; iter++ )
			cout << (*iter) << "---->" ;
	}
	else
	{
		cout << "No path found from " << from << " to " << to << endl;
	}
}

bool findPath( int** mat, int length, int from, int to, list<int>& path)
{
	for( int i = 0 ; i < length ; i++ )
	{
		// if there is a path		
		if( mat[from][i] == 1 )
		{			
			if( i == to )
			{
				cout << "reached to " << to << endl;
				path.push_back(i);
				return true;
			}
			// ignore the values in path
			bool alreadyPresent = false;
			list<int>::iterator iter = path.begin();
			for( ;iter != path.end(); iter++)
			{
				if((*iter) == i)
				{
					alreadyPresent = true;
					break;
				}
			}

			if( !alreadyPresent )
			{
				cout << "found new path from " << from << " to " << i << endl;
				path.push_back(i);
				bool found = findPath(mat,length,i,to,path);
				if( found )
					return true;
				else
					path.pop_back();
			}

		}
	}

	return false;
}

int ** getMatrix( int SIZE )
{
	int ** matrix = new int*[SIZE];
	for( int i = 0 ; i < SIZE ; i++)
	{
		matrix[i] = new int[SIZE];
	}
	return matrix;
}
void inputMatrix( int** matrix, int SIZE)
{
	for( int i = 0 ; i < SIZE ; i++ )
	{
		for(int j = 0 ; j < SIZE ; j++ )
		{
			cout << "matrix[" << i << "," << j << "]=";
			cin >> matrix[i][j];
		}
	}
}

void printMatrix(int** matrix , int SIZE)
{
	for( int i = 0 ; i< SIZE ;i++)
	{
		for( int j = 0 ;j < SIZE ;j++)
			cout << matrix[i][j] << '\t' ;

		cout << '\n' ;
	}
}

int main()
{	
	int SIZE;
	cout << "Input size of matrix : " ;
	cin >> SIZE;
	cout << "You entered : " << SIZE << endl;	
	int ** matrix = getMatrix(SIZE);
	cout << "Input the matrix..\n";
	inputMatrix(matrix ,SIZE);
	cout << "Your matrix.. ";
	printMatrix(matrix,SIZE);	
	int to = 0, from=0;
	cout << "Input index of source : " ;
	cin >> from;
	cout << "Input index of destination : " ;
	cin >> to;

	findPath(matrix,SIZE,from,to);

	return 0;
}
