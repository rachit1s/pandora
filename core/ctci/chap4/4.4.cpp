/*
Given a binary search tree, design an algorithm which creates a linked list of all the 
nodes at each depth (i e , if you have a tree with depth D, you’ll have D linked lists) 
*/
# include <iostream>
# include "Tree.cpp"

/*
valid for binary search tree
*/
template<typename T>
Tree<T>* maximum(Tree<T>* tree)
{
	Tree<T>* max = tree;
	while( NULL != max->getRightTree() )
		max = max->getRightTree();

	return max;
}

template<typename T>
Tree<T>* minimum(Tree<T>* tree)
{
	Tree<T>* min = tree;
	while( NULL != min->getLeftTree())
		min = min->getLeftTree();

	return min;
}

/*
the minimum of right sub tree
or first ancestor which is not right child of parent
*/
template<typename T>
Tree<T>* successor(Tree<T>* tree)
{
	if( NULL != tree->getRightTree() )
		return minimum(tree->getRightTree());

	Tree<T>* x = tree;
	Tree<T>* y = x->getParent();
	while(NULL != y && y->getRightTree() == x)
	{
		x = y ;
		y = y->getParent();
	}

	if( x == tree ) // this was the root with no right
		return NULL ;

	return y;
}

int main()
{
	
}