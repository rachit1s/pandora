#include <iostream>
#include "Tree.cpp"
#include <queue>
#include <list>

using namespace std;

template<typename T>
void findPath(list<list<Tree<T>*>>& l, Tree<T>* tree,int value)
{
	if( NULL == tree)
		return NULL;
	l.push_back(tree);
	value = value - tree->getData();
	if( 0 == value )
		return l;
	else if( value < 0 )
		return NULL;

	return

}

template<typename T>
list<list<Tree<T>*>> findAllPaths(Tree<T>* root, int value)
{
	// level order visit each node and try to find the solution
	queue<Tree<T>*> queue;
	if( NULL != root )
		queue.push(root);

	while( !queue.empty())
	{
		// start from current node in queue
		Tree<T>* curr = queue.front();
		list<Tree<T>*> list = findPath(curr,value);


	}
}
int main()
{

}