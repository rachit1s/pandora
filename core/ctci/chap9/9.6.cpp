/*
9 6  Given a matrix in which each row and each column is sorted, write a method to find 
an element in it 
*/


#include <iostream>
#include "../../cpplib/cpputils.h"
using namespace std;

bool findInSortedMatrix( int** matrix, const int& ROWS, const int& COLS, const int& endRow , const int& startCol, const int& num, int& x, int& y)
{
	cout << "called with endRow = " << endRow << " and startCol = " << startCol << endl;
	if( endRow == -1 || startCol == COLS ) // not found
	{
		x = y = -1 ;
		return false;
	}	

	if( matrix[endRow][startCol] == num )
	{
		x = endRow;
		y = startCol ;
		return true;
	}
	else if( num < matrix[endRow][startCol] )
		return findInSortedMatrix(matrix,ROWS,COLS,endRow-1,startCol,num,x,y);
	else if( num > matrix[endRow][startCol] )
		return findInSortedMatrix(matrix,ROWS,COLS,endRow,startCol+1,num,x,y);

	return false ;
}

bool findInSortedMatrixIterative( int** matrix, const int& ROWS, const int& COLS, int endRow , int startCol, const int& num, int& x, int& y)
{
	cout << "called with endRow = " << endRow << " and startCol = " << startCol << endl;
	while(endRow != -1 && startCol != COLS )
	{
		if( matrix[endRow][startCol] == num )
		{
			x = endRow;
			y = startCol ;
			return true;
		}
		else if( num < matrix[endRow][startCol] )
			endRow--;
		else
			startCol++;
	}

	return false ;
}

int main()
{
	int rows, cols;
	cout << "Enter number of rows : " ;
	cin >> rows;
	cout << "Enter number of cols : " ;
	cin >> cols;

	int** matrix ;
	createAndInput2DArray( matrix, rows, cols);

	cout << "you entered matrix : " << endl; 
	print2DArray(matrix,rows,cols);

	int num;

	cout << "Enter value to search : " ;
	cin >> num;

	int x , y;
	// bool found = findInSortedMatrix(matrix,rows,cols,rows-1,0,num,x,y);
	bool found = findInSortedMatrixIterative(matrix,rows,cols,rows-1,0,num,x,y);

	if( false == found )
		cout << " Value not found.";
	else
		cout << "Value found at x = " << x << ", y = " << y << endl;

	return 0;
}

