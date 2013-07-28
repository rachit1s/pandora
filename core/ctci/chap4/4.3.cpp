# include <iostream>
# include "Tree.cpp"

using namespace std;
/*
Given a sorted (increasing order) array, write an algorithm to create a binary tree with 
minimal height 
*/

/*
including from, including to
*/
Tree<int>*  createTree( int* intArray, int low, int high)
{
	if( high < low )
		return NULL;

	int mid = (low + high)/2;

	Tree<int>* t = new Tree<int>(NULL,intArray[mid],NULL);
	Tree<int>* left = createTree(intArray, low, mid-1);
	Tree<int>* right = createTree(intArray, mid+1,high);

	t->setLeftTree(left);
	t->setRightTree(right);

	return t;
}

int main()
{
	int size = 0 ;
	cout << "Enter the size of array : ";
	cin >> size ;

	int* a = new int[size];

	int count = 0 ; 
	cout << "Enter a sorted array values : ";
	while( count < size )
	{
		cin >> a[count++];
	}

	Tree<int>* tree = createTree(a,0,size-1);

	cout << "\nIterativeInorder :" ;
	iterativeInorder(tree);
	cout << "\ninorder :" ;
	inorder(tree);

	return 0 ;
}