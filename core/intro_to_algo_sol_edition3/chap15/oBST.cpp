/*
optimal binary search tree
*/
#include <iostream>
#include <list>
#include "../cpplib/cpputils.h"

/*
let us assume that the Values to be put in the BST are sorted and then numbered from 1....n
now we just have give the representation of BST in terms of number i.e. which element will appear as root or right and left nodes.
let probabilities has length noOfValues + 1 with first element i.e. probabilities[0] be a dummy value for convenience of programming
let costMatrix be of size [noOfValues+1]*[noOfValues+1] : it will contain the cost of sub trees. 
our answer will be costMatrix[1][noOfValues]
and let rootMatrix be of size [noOfValues+1]*[noOfValues+1] : it will contain the roots of sub trees for the purpose of reconstruction 
*/
void obstIterBottomUp(int* probabilities, int noOfValues, int** costMatrix, int** rootMatrix)
{
	for( int i = 0 ; i < noOfValues ; i++ )
		costMatrix[i][i] = 
}