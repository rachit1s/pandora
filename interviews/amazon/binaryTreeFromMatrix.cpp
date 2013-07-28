/*
http://discuss.joelonsoftware.com/default.asp?interview.11.623860.10
2. A tree is represented in a matrix form such that A[i,j] = 1 if j is the ancestor of i. Otherwise A[i,j] = 0.
Given this construct a normal binary search tree.
*/

#include <iostream>
#include "../../core/cpplib/cpputils.h"
#include "../../core/dscpp/chap5/Tree.cpp"

using namespace std;

// assuming that root is properly set in the matrix.
// Tree should be properly deleted by caller
Tree<int>* createBinarySearchTree(int** matrix, const int& SIZE)
{
	Tree<int>* root = NULL;
	Tree<int>** treeNodes ;
	createAndInitArray(treeNodes,SIZE);
	
	for( int index = 0 ; index < SIZE ; index++)
	{
		cout << "finding parent of : " << index << endl;
		Tree<int>* currNode = treeNodes[index];
		if(NULL == currNode)
		{
			currNode = new Tree<int>(index);
			treeNodes[index] = currNode;
		}

		bool foundParent = false;
		for( int i = 0 ; i < SIZE ; i++ )
		{
			if(matrix[index][i] != 0)
			{
				cout << "found parent of " << index << " as " << i << endl;
				foundParent = true;
				// this is the parent
				Tree<int>* parent = treeNodes[i];
				if( NULL == parent )
				{
					parent = new Tree<int>(i);
					treeNodes[i] = parent;
				}

				if( index < i )
				{
					// left node
					parent->setLeftTree(currNode);
				}
				else
				{
					parent->setRightTree(currNode);
				}
			}
		}

		if( foundParent == false )
		{
			root = currNode;
			cout << "found root as : " << root->getData() << endl;
		}
	}

	cout << "Printing treeNodes : \n";
	for( int i = 0 ; i < SIZE ; i++ )
	{
		cout << treeNodes[i]->getData() << "," ;
	}

	cout << endl;

	// cout << "returning root : " << (*root) << endl;
	return root;
}

int main()
{
	int SIZE = 0 ;
	cout << "Enter the noOfNodes in the Tree : " ;
	cin >> SIZE ;

	cout << "Enter the values of matrix : ";
	int ** matrix; 
	createAndInput2DArray(matrix,SIZE,SIZE);


	Tree<int>* tree = createBinarySearchTree(matrix,SIZE);

	cout << "\n\n";
	preorder(tree);
	cout << "\n\n";

	cout << "\n\n";
	postorder(tree);
	cout << "\n\n";

	cout << "\n\n";
	inorder(tree);
	cout << "\n\n";

	deleteTree(tree);
	delete2DArray(matrix,SIZE);

}
