/*
Given a binary tree, print boundary nodes of the binary tree Anti-Clockwise starting from the root. 
For example, boundary traversal of the following tree is “20 8 4 10 14 25 22″
*/


#include <iostream>
using namespace std;

template<typename T>
class Tree
{
	private:
	Tree<T>* left;
	Tree<T>* right;
	T data;
	bool tag;
	public:
	Tree(Tree<T>* left , T data , Tree<T>* right)
	{
		this->left = left;
		this->data = data;
		this->right = right;
		tag = false;
	}

	Tree(T data)
	{
		this->left = NULL;
		this->data = data;
		this->right = NULL;
		tag = false;
	}

	Tree<T>* getLeft() const
	{
		return left ;
	}

	Tree<T>* getRight() const
	{
		return right;
	}

	T getData() const
	{
		return data;
	}

	void setLeftTree(Tree<T>* left)
	{
		this->left = left;
	}

	void setRightTree(Tree<T>* right)
	{
		this->right = right;
	}

	void setData(const T data)
	{
		this->data = data;
	}

	void setTag(bool tag)
	{
		this->tag = tag;
	}

	bool getTag() const
	{
		return tag;
	}
};

template<typename T>
void visit(Tree<T>* tree)
{
	cout << tree->getData() << "," ;
}

bool partFinished = false;
bool secondPartStart = false;


// does not give correct result if the tree is left linear 
template<typename T>
void boundaryTraversal(Tree<T>* root)
{
	static Tree<T>* myRoot = root;

	if( myRoot->getLeft() == NULL )
	{
		partFinished = true;
	}

	if( partFinished == false && (root->getLeft() != NULL || root->getRight() != NULL))
	{
		visit(root);
	}

	if( partFinished == false && root->getLeft() == NULL && root->getRight() == NULL )
	{
		partFinished = true;
	}

	if( root->getLeft() == NULL && root->getRight() == NULL )
		visit(root);

	if( NULL != root->getLeft())
	{
		boundaryTraversal(root->getLeft());
	}	


	if( root == myRoot )
	{
		secondPartStart = true;
		myRoot->setTag(true);
	}

	if(NULL != root->getRight())
	{
		if( secondPartStart == true )
		{
			if( root->getTag() == true )
				root->getRight()->setTag(true);
		}
		boundaryTraversal(root->getRight());
	}

	if( root->getTag() == true && root != myRoot)
	{
		visit(root);
	}
}


// template<typename T>
// void printLeftBoundary(Tree<T>* root)
// {
// 	while( root != NULL )
// 	{
// 		visit(root);
// 		root = root->getLeft();
// 	}
// }
// template<typename T>
// void printRightBoundary(Tree<T>* root)
// {
// 	if( NULL != root )
// 		printRightBoundary(root->getRight());

// 	visit(root);
// }
// void printLeaves(Tree<T>* root)
// {
// 	if( NULL == root->getLeft() && NULL == root->getRight() )
// 		visit(root);
// }
// template<typename T>
// void boundaryTraversal2(Tree<T>* root)
// {
// 	printLeftBoundary(root);
// 	printLeaves(root);
// 	printRightBoundary(root);
// }

int main()
{
	// Tree<int>* tree14 = new Tree<int>(14);
	// Tree<int>* tree11 = new Tree<int>(tree14,11,NULL);

	// Tree<int>* tree10 = new Tree<int>(10);
	// Tree<int>* tree7 = new Tree<int>(tree10,7,tree11);

	// Tree<int>* tree6 = new Tree<int>(6);
	// Tree<int>* tree3 = new Tree<int>(tree6,3,tree7);

	// Tree<int>* tree13 = new Tree<int>(13);
	// Tree<int>* tree12 = new Tree<int>(12);

	// Tree<int>* tree9 = new Tree<int>(tree12,9,tree13);

	// Tree<int>* tree8 = new Tree<int>(8);
	// Tree<int>* tree5 = new Tree<int>(tree8,5,tree9);

	
	// Tree<int>* tree16 = new Tree<int>(16);
	// Tree<int>* tree15 = new Tree<int>(NULL,15,tree16);
	// Tree<int>* tree4 = new Tree<int>(NULL,4,tree15);
	// // Tree<int>* tree4 = new Tree<int>(4);
	// Tree<int>* tree2 = new Tree<int>(tree4,2,tree5);

	// Tree<int>* tree1 = new Tree<int>(tree2,1,tree3);

	
	// Tree<int>* tree2 = new Tree<int>(tree4,2,NULL);
	// Tree<int>* tree1 = new Tree<int>(tree2,1,NULL);

	// This test case : only left linear tree gives wrong result
	// Tree<int>* tree11 = new Tree<int>(11);
	// Tree<int>* tree7 = new Tree<int>(NULL,7,tree11);
	// Tree<int>* tree3 = new Tree<int>(NULL,3,tree7);
	// Tree<int>* tree1 = new Tree<int>(NULL,1,tree3);

	// Tree<int>* tree1 = new Tree<int>(1);
	boundaryTraversal(tree1);

	return 0;

}