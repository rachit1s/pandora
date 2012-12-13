/*
8 6  Implement the “paint fill” function that one might see on many image editing pro -
grams   That is, given a screen (represented by a 2 dimensional array of Colors), a 
point, and a new color, fill in the surrounding area until you hit a border of that col-
or ’ 
*/

#include <iostream>
#include "../../cpplib/cpputils.h"
using namespace std;

template<typename T>
void fill(T** matrix, const int& rows, const int& cols, const int& r, const int& c, const T currentValue, const T finalValue )
{
	cout << "fill: rows = " << rows << ", cols = " << cols << ", r = " << r << ", c = " << c << ", currentValue = " << currentValue << ", finalValue = " << finalValue << endl; 
	if( r < 0 || r >= rows || c < 0 || c >= cols ) // out of range
		return;

	if( matrix[r][c] == finalValue || matrix[r][c] != currentValue ) // if not current value of already final value.
		return;

	matrix[r][c] = finalValue;

	fill( matrix, rows, cols, r, c+1,currentValue,finalValue);
	fill( matrix, rows, cols, r, c-1,currentValue,finalValue);
	fill( matrix, rows, cols, r+1, c,currentValue,finalValue);
	fill( matrix, rows, cols, r-1, c,currentValue,finalValue);
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

	int x, y;
	cout << "Enter x : " ;
	cin >> x ;
	cout << "Enter y : " ;
	cin >> y;

	int finalValue ;
	cout << "Enter finalValue : " ;
	cin >> finalValue;

	fill(matrix,rows,cols,x,y,matrix[x][y],finalValue);

	cout << "Final matrix : " << endl;
	print2DArray(matrix,rows,cols);

	return 0;
}