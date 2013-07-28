#include<iostream>
#include"Tree.cpp"

template<typename T>
void assign( Tree<T>* from, Tree<T>*& to)
{
// using preorder
	if( NULL == from )
		return;
	cout << "current " << from->getData() << endl;
	if(NULL == to) // this will be the case only for the root
	{
		cout << "the to was null " << endl;
		T y = from->getData();
		to = new Tree<T>(NULL,y,NULL);
		cout << "assigned to root -> " << y << endl;
	}

	Tree<T>* right = NULL;
	Tree<T>* r = from->getRightTree();
	if( NULL != r)
	{
		T y = r->getData();
		right = new Tree<T>(NULL,y,NULL);
		to->setRightTree(right);
		cout << "assigned to right : " << y << endl;
	}

	Tree<T>* left = NULL;
	Tree<T>* l = from->getLeftTree();
	if(NULL != l )
	{
		T y = l->getData();
		left = new Tree<T>(NULL,y,NULL);
		to->setLeftTree(left);
		cout << "assigned to left :  "<< y << endl;
	}

	assign(l,left);
	assign(r,right);
}

int main()
{
int x = 1;
Tree<int>* tree = new Tree<int>(NULL,x,NULL);
x = 2;
Tree<int>* r = new Tree<int>(NULL,x,NULL);
x = 3 ;
Tree<int>* l = new Tree<int>(NULL,x,NULL);
x = 4 ;
Tree<int>* ll = new Tree<int>(NULL,x,NULL);
x = 5;
Tree<int>* rl = new Tree<int>(NULL,x,NULL);

x = 6;
Tree<int>* lr = new Tree<int>(NULL,x,NULL);
x = 7;
Tree<int>* rr = new Tree<int>(NULL,x,NULL);

r->setLeftTree(rl);
l->setLeftTree(ll);

r->setRightTree(rr);
l->setRightTree(lr);
tree->setRightTree(r);
tree->setLeftTree(l);

Tree<int>* newTree = NULL;
assign(tree,newTree);

cout << "Level Order Original" << endl;
levelorder(tree);

cout << "Level Order Duplicate" << endl;
levelorder(newTree);

return 0;
}