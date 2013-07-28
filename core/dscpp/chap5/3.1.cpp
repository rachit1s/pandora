#include<iostream>
#include"Tree.cpp"

template<typename T>
void countLeafNode(const Tree<T>* root, int & count)
{
	if(NULL != root)
	{
		cout << "Analyzing " << root->getData() << endl;
		if(NULL == root->getLeftTree() && NULL == root->getRightTree())
		{
			count++;
			cout << "increasing count.." ;
		}	
		
		if( NULL != root->getLeftTree())
			countLeafNode(root->getLeftTree(),count);
		if( NULL != root->getRightTree())
			countLeafNode(root->getRightTree(),count);
	}
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

	int count =0 ;
	countLeafNode(tree,count);

	cout << "number of leaf nodes = " << count << endl;

	return 0;

}