/*
http://cs.stackexchange.com/questions/439/which-combinations-of-pre-post-and-in-order-sequentialisation-are-unique
*/

#include <iostream>
#include "../cpplib/cpputils.h"
#include "../dscpp/chap5/Tree.cpp"

using namespace std;

/*
this assumes the every node in the tree has unique value. It consturcts from the pre-order and in-order output of tree traversal
if not then we can use a id in the node. Get the outputs of pre and post as ids instead of data.
and from the hash-map of id,value we can reconstruct the tree with values 
*/
template <typename T>
Tree<T>* reconstructTreeUsingPreOrderAndInOrderOutput( T* preOrder, int preStart, int preEnd, T* inOrder, int inStart, int inEnd)
{
	if( preEnd < preStart ) // check other tests for invalid trees like .. not matching values in and pre order etc.
		return NULL;
	if(inEnd < inStart )
		return NULL;

	T rootData = preOrder[preStart];
	Tree<T> * root = new Tree<T>(rootData); // this is bound to be the root of the tree

	// find the preOrder[preStart] value in the inOrder array.
	int inRootIndex = -1;
	for( int i = inStart ; i <= inEnd ; i++ )
	{
		if( inOrder[i] == rootData )
		{
			inRootIndex =  i ;
			break;
		}
	}

	if( inRootIndex == -1 )
		throw "Invalid Tree";

	Tree<T>* leftTree = reconstructTreeUsingPreOrderAndInOrderOutput(preOrder,preStart+1,preStart + (inRootIndex - inStart),inOrder,inStart,inRootIndex -1 );

	Tree<T>* rightTree = reconstructTreeUsingPreOrderAndInOrderOutput(preOrder,preStart + (inRootIndex - inStart) + 1,preEnd,inOrder,inRootIndex+1,inEnd );

	cout << endl << root->getData() << " has left Tree : " << ( NULL != leftTree ? leftTree->getData() : -1 ) ;
	root->setLeftTree(leftTree);

	cout << " and right Tree : " << (NULL != rightTree ? rightTree->getData() : -1 )  ;
	root->setRightTree(rightTree);

	return root;
}

/*
this assumes the every node in the tree has unique value. It consturcts from the post-order and in-order output of tree traversal
if not then we can use a id in the node. Get the outputs of pre and post as ids instead of data.
and from the hash-map of id,value we can reconstruct the tree with values 
*/
template <typename T>
Tree<T>* reconstructTreeUsingPostOrderAndInOrderOutput( T* postOrder, int postStart, int postEnd, T* inOrder, int inStart, int inEnd)
{
	cout << "\npost Order (" << postStart << ","<< postEnd << ")  : ";
	print1DArray(postOrder,postStart, postEnd);
	cout << "\nin Order : (" << inStart << ","<< inEnd << ") ";
	print1DArray( inOrder,inStart, inEnd);//

	if( postEnd < postStart ) // check other tests for invalid trees like .. not matching values in and pre order etc.
		return NULL;
	if(inEnd < inStart )
		return NULL;

	T rootData = postOrder[postEnd];
	cout << "Root chosen is : " << rootData << endl;
	Tree<T> * root = new Tree<T>(rootData); // this is bound to be the root of the tree

	// find the preOrder[preStart] value in the inOrder array.
	int inRootIndex = -1;
	for( int i = inStart ; i <= inEnd ; i++ )
	{
		if( inOrder[i] == rootData )
		{
			inRootIndex =  i ;
			break;
		}
	}

	int leftLength = inRootIndex - inStart;
	int rightLength = inEnd - inRootIndex;

	cout << "leftLength : " << leftLength << ", rightLength = " << rightLength << endl;
	if( inRootIndex == -1 )
		throw "Invalid Tree";

	Tree<T>* leftTree = reconstructTreeUsingPostOrderAndInOrderOutput(postOrder,postStart,postStart + leftLength -1 ,inOrder,inStart,inRootIndex -1 );

	Tree<T>* rightTree = reconstructTreeUsingPostOrderAndInOrderOutput(postOrder,postStart + leftLength,postEnd-1,inOrder,inRootIndex+1,inEnd );

	cout << endl << root->getData() << " has left Tree : " << ( NULL != leftTree ? leftTree->getData() : -1 ) ;
	root->setLeftTree(leftTree);

	cout << " and right Tree : " << (NULL != rightTree ? rightTree->getData() : -1 ) ;
	root->setRightTree(rightTree);

	return root;
}

/*
this assumes the every node in the tree has unique value. It consturcts from the post-order and pre-order output of tree traversal.
The case of any node having one child is ambiguous and the only child is assumed to be the left child.
if not then we can use a id in the node. Get the outputs of pre and post as ids instead of data.
and from the hash-map of id,value we can reconstruct the tree with values 
*/
template <typename T>
Tree<T>* reconstructTreeUsingPostOrderAndPreOrderOutput( T* postOrder, int postStart, int postEnd, T* preOrder, int preStart, int preEnd)
{

	cout << "\npost Order (" << postStart << ","<< postEnd << ")  : ";
	print1DArray(postOrder,postStart, postEnd);
	cout << "\npre Order : (" << preStart << ","<< preEnd << ") ";
	print1DArray( preOrder,preStart, preEnd);//

	if( preEnd < preStart )
		return NULL;
	if( postEnd < postStart )
		return NULL;

	// get the root as first element of preOrder
	T rootData = preOrder[preStart];

	// just confirm this should be last element of the post order
	if(postOrder[postEnd] != rootData )
		throw "Illegal Tree. Type1";

	Tree<T>* root = new Tree<T>(rootData);
	Tree<T>* left = NULL, *right = NULL;
	T *leftData = NULL, *rightData = NULL;

	// now we have to find the right and left of root.
	// root->left = preOrder[preStart+1] and root->right = postOrder[postEnd-1]
	if( preStart+1 <= preEnd )
	{
		leftData = &preOrder[preStart+1];
	}
	if( postEnd-1 >= postStart)
	{
		rightData = &postOrder[postEnd-1];
	}
	
	if( (NULL == rightData) && (NULL == leftData) )
		return root;

	if( ( (NULL != rightData) && (NULL == leftData) ) || ( (NULL == rightData) && (NULL != leftData) ) )
		throw "Illegal Tree. Type2";

	// it was assumed the it has unique values
	if( *rightData == *leftData ) // this is the case of single child
	{
		// make this the left child
		left = reconstructTreeUsingPostOrderAndPreOrderOutput( postOrder, postStart, postEnd-1, preOrder,preStart+1,preEnd);
		root->setLeftTree(left);
		return root;
	}

	// find leftData in post order and rightData in preOrder
	int leftIndex = -1, rightIndex = -1 ;
	for( int i = postStart; i <= postEnd - 1 ; i++)
	{
		if( postOrder[i] == *leftData )
		{
			leftIndex = i ;
			break;
		}	
	}

	for( int i = preStart+1 ; i <= preEnd ; i++ )
	{
		if( preOrder[i] == *rightData )
		{
			rightIndex = i ;
			break;
		}
	}
	
	if( -1 == rightIndex || -1 == leftIndex )
		throw "Illegal Tree. Type3";

	left = reconstructTreeUsingPostOrderAndPreOrderOutput( postOrder, postStart, leftIndex, preOrder,preStart+1,rightIndex-1);
	right = reconstructTreeUsingPostOrderAndPreOrderOutput( postOrder, leftIndex+1, postEnd-1, preOrder,rightIndex,preEnd);

	root->setLeftTree(left);
	root->setRightTree(right);

	return root;
}

template<typename T>
int putInArray(Tree<T>* tree, T* array, int currIndex)
{
	if(NULL == tree)
		return currIndex;
	array[currIndex++] = tree->getData();

	return currIndex;
}

template<typename T>
int getPreorder(Tree<T>* tree, T* array, int nextIndex, int (*visit)(Tree<T>* t, T* array, int currIndex))
{
	if( NULL == tree )
		return nextIndex; // nothing stored
	nextIndex = visit(tree,array,nextIndex);
	nextIndex = getPreorder(tree->getLeftTree(),array,nextIndex,visit);
	nextIndex = getPreorder(tree->getRightTree(),array,nextIndex,visit);

	return nextIndex;
}

template<typename T>
int getPostorder(Tree<T>* tree, T* array, int nextIndex, int (*visit)(Tree<T>* t, T* array, int currIndex))
{
	if( NULL == tree )
		return nextIndex; // nothing stored
	nextIndex = getPostorder(tree->getLeftTree(),array,nextIndex,visit);
	nextIndex = getPostorder(tree->getRightTree(),array,nextIndex,visit);
	nextIndex = visit(tree,array,nextIndex);

	return nextIndex;
}

template<typename T>
int getInorder(Tree<T>* tree, T* array, int nextIndex, int (*visit)(Tree<T>* t, T* array, int currIndex))
{
	if( NULL == tree )
		return nextIndex; // nothing stored
	nextIndex = getInorder(tree->getLeftTree(),array,nextIndex,visit);
	nextIndex = visit(tree,array,nextIndex);
	nextIndex = getInorder(tree->getRightTree(),array,nextIndex,visit);

	return nextIndex;
}

int main()
{
	int noOfNodes; // get output from inputTreeMethod about the number of nodes in the tree
	Tree<int>* root = inputTree(noOfNodes);

	cout << "noOfNodes : " << noOfNodes  << endl ;

	int* preOrderOutput;
	createAndInitArray(preOrderOutput,noOfNodes,-1);

	int* postOrderOutput;
	createAndInitArray(postOrderOutput,noOfNodes,-1);

	int* inOrderOutput;
	createAndInitArray(inOrderOutput,noOfNodes,-1);

	getPreorder(root,preOrderOutput,0,putInArray);

	getPostorder(root,postOrderOutput,0,putInArray);

	getInorder(root,inOrderOutput,0,putInArray);

	print("\nPreorder traversal : ", preOrderOutput,noOfNodes);
	cout << "Preorder : \n";
	preorder(root);
	cout << endl;

	print("\nPostorder traversal : ", postOrderOutput,noOfNodes);
	cout << "Postorder : \n";
	postorder(root);
	cout << endl;

	print("\nInorder traversal : ", inOrderOutput,noOfNodes);
	cout << "inorder : \n" ;
	inorder(root);
	cout << endl;

	Tree<int>* newTree = reconstructTreeUsingPreOrderAndInOrderOutput(preOrderOutput,0,noOfNodes-1,inOrderOutput,0,noOfNodes-1);
	{
		int* preOrderOutput;
		createAndInitArray(preOrderOutput,noOfNodes,-1);

		int* postOrderOutput;
		createAndInitArray(postOrderOutput,noOfNodes,-1);

		int* inOrderOutput;
		createAndInitArray(inOrderOutput,noOfNodes,-1);

		getPreorder(newTree,preOrderOutput,0,putInArray);

		getPostorder(newTree,postOrderOutput,0,putInArray);

		getInorder(newTree,inOrderOutput,0,putInArray);

		print("\nPreorder traversal : ", preOrderOutput,noOfNodes);
		cout << "Preorder : \n";
		preorder(newTree);
		cout << endl;

		print("\nPostorder traversal : ", postOrderOutput,noOfNodes);
		cout << "Postorder : \n";
		postorder(newTree);
		cout << endl;

		print("\nInorder traversal : ", inOrderOutput,noOfNodes);
		cout << "inorder : \n" ;
		inorder(newTree);
		cout << endl;
	}

	{
		cout << "\n\n+++++++++++++++++++++++++ reconstruction from post and in order +++++++++++++++++++++++++++++++++\n\n";
		Tree<int>* newTree = reconstructTreeUsingPostOrderAndInOrderOutput(postOrderOutput,0,noOfNodes-1,inOrderOutput,0,noOfNodes-1);
		{
			int* preOrderOutput;
			createAndInitArray(preOrderOutput,noOfNodes,-1);

			int* postOrderOutput;
			createAndInitArray(postOrderOutput,noOfNodes,-1);

			int* inOrderOutput;
			createAndInitArray(inOrderOutput,noOfNodes,-1);

			getPreorder(newTree,preOrderOutput,0,putInArray);

			getPostorder(newTree,postOrderOutput,0,putInArray);

			getInorder(newTree,inOrderOutput,0,putInArray);

			print("\nPreorder traversal : ", preOrderOutput,noOfNodes);
			cout << "Preorder : \n";
			preorder(newTree);
			cout << endl;

			print("\nPostorder traversal : ", postOrderOutput,noOfNodes);
			cout << "Postorder : \n";
			postorder(newTree);
			cout << endl;

			print("\nInorder traversal : ", inOrderOutput,noOfNodes);
			cout << "inorder : \n" ;
			inorder(newTree);
			cout << endl;
		}

	}

	{
		cout << "\n\n+++++++++++++++++++++++++ reconstruction from post and pre order +++++++++++++++++++++++++++++++++\n\n";
		Tree<int>* newTree = NULL;
		try
		{
		  newTree = reconstructTreeUsingPostOrderAndPreOrderOutput(postOrderOutput,0,noOfNodes-1,inOrderOutput,0,noOfNodes-1);
		}
		catch(const char* msg)
		{
			cout << "Exception occured : " << msg << endl;
			return 1;
		}
		{
			int* preOrderOutput;
			createAndInitArray(preOrderOutput,noOfNodes,-1);

			int* postOrderOutput;
			createAndInitArray(postOrderOutput,noOfNodes,-1);

			int* inOrderOutput;
			createAndInitArray(inOrderOutput,noOfNodes,-1);

			getPreorder(newTree,preOrderOutput,0,putInArray);

			getPostorder(newTree,postOrderOutput,0,putInArray);

			getInorder(newTree,inOrderOutput,0,putInArray);

			print("\nPreorder traversal : ", preOrderOutput,noOfNodes);
			cout << "Preorder : \n";
			preorder(newTree);
			cout << endl;

			print("\nPostorder traversal : ", postOrderOutput,noOfNodes);
			cout << "Postorder : \n";
			postorder(newTree);
			cout << endl;

			print("\nInorder traversal : ", inOrderOutput,noOfNodes);
			cout << "inorder : \n" ;
			inorder(newTree);
			cout << endl;
		}
	}


	// cout << "Postorder : \n";
	// postorder(root);
	// cout << endl;

	// cout << "inorder : \n" ;
	// inorder(root);
	// cout << endl;

	// cout << "levelorder : \n";
	// levelorder(root);
	// cout << endl; 


	// Tree<int>
	deleteTree(root);
	deleteTree(newTree);

	return 0; 
}