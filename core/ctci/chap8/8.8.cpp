/*
8 8  Write an algorithm to print all ways of arranging eight queens on a chess board so 
that none of them share the same row, column or diagonal 
*/

#include <iostream>
using namespace std;

int columnInRow[8] = {-1};

bool checkRow(int row)
{
	for( int i = 0 ; i < row ; i++ )
	{
		int diff = columnInRow[i] - columnInRow[row];
		diff = (diff < 0 ? -diff : diff );

		if( diff == 0 || diff == row - i ) return false;
	}

	return true;
}

void printBoard()
{
	cout << "Printing Board : \n";
	for( int i = 0 ; i < 8 ; i++ )
	{
		for( int j = 0 ; j < 8 ; j++ )
		{
			if( columnInRow[i] == j )
				cout << "Q\t";
			else
				cout << "_\t";
		}

		cout << endl;
	}

	cout << "\n\n";
}

void placeInRow(int row)
{
	if( row == 8 )
	
{		// finished placing
		printBoard();
	}
	else
	{
		for( int i = 0 ; i < 8 ; i++ )
		{
			columnInRow[row] = i ;
			if( checkRow(row) )
			{
				placeInRow(row+1);
			}
		}
	}
}

int main()
{
	placeInRow(0);
}
