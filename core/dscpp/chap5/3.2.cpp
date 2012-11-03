/*
   Write an algorithm, SwapTree(), that takes a binary tree and swaps the left and the right children of every node.
   */

#include<iostream>
#include"Tree.cpp"

template<typename T>
void swapTree(Tree<T>* root)
{
  // using post order traversal

  Tree<T>* leftTree = root->getLeftTree();
  Tree<T>* rightTree = root->getRightTree();
  if( NULL != leftTree )
      swapTree(leftTree);
  if(NULL != rightTree)
      swapTree(rightTree);

  root->setLeftTree(rightTree);
  root->setRightTree(leftTree);
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

levelorder(tree);

swapTree(tree);

levelorder(tree);

return 0;

}
