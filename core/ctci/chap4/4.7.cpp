#include <iostream>
#include "Tree.cpp"
using namespace std;

/*
checks if the tree first is exactly same as second in terms of 
values 
*/
template<typename T>
bool isEqual( Tree<T>* first, Tree<T>* second)
{
	if( NULL == first && NULL == second )
		return true;
	// check node value and recurse for right and left values
	if( (NULL != first && NULL == second)
		|| (NULL != second && NULL == first)
		|| ( first->getData() != second->getData() )
	  )
	return false;

	return (isEqual(first->getLeftTree(),second->getLeftTree()) && isEqual(first->getRightTree(),second->getRightTree()));

}

/*
ignores any extra nodes in first
*/
template<typename T>
bool similar(Tree<T>* first, Tree<T>* second)
{
	if(NULL == second )
		return true;

	if( NULL != first )
	{
		if(first->getData() != second->getData())
			return false;

		return (similar(first->getLeftTree(),second->getLeftTree()) && similar(first->getRightTree(),second->getRightTree()));
	}
	
	return true;
}

template<typename T>
bool isSubTree(Tree<T>* source, Tree<T>* sub, bool (*equalityFunction)(Tree<T>* first, Tree<T>* second) )
{
	// find the root of sub and see if all right and left satisfy
	// do level order iteration and find the root of sub

	queue<Tree<T>*> treeQueue;
	treeQueue.push(source);
	while(!treeQueue.empty())
	{
		Tree<T>* top = treeQueue.front();
		if(top->getData() == sub->getData())
		{
			// found the root data no compare
			if( equalityFunction(top,sub) )
			{
				// found the sub tree 
				return true;
			}
		}

		treeQueue.pop();
		if( NULL != top->getLeftTree() )
		{
			treeQueue.push(top->getLeftTree());	
		}

		if( NULL != top->getRightTree())
		{
			treeQueue.push(top->getRightTree());
		}
	}

}

int main()
{
	Tree<int>* super;
	Tree<int>* sub;

	// for exact sub tree 
	if( isSubTree(super,sub,isEqual) )
	{
		cout << "The tree sub is exact subtree of super.";
	}

	// for similarity 
	if( isSubTree(super,sub,similar) )
	{
		cout << "The tree sub is similar to subtree of super.";
	}

	return 0;
}