#include<iostream>
using namespace std;
#include"Tree.cpp"

void testGeneral()
{
  int x = 10;
  Tree<int>* right = new Tree<int>(NULL,x,NULL);
  
  x = 11;
  Tree<int>* left = new Tree<int>(NULL,x,NULL);
  
  x = 12;
  Tree<int> root = Tree<int>(left,x,right);

  x = 20;
  Tree<int>* rr = new Tree<int>(NULL,x,NULL);

  right->setRightTree(rr);

  x = 21;
  Tree<int>* rl = new Tree<int>(NULL,x,NULL);

  right->setLeftTree(rl);

  x = 22 ;
  Tree<int>* lr = new Tree<int>(NULL,x,NULL);

  left->setRightTree(lr);

  x=23;
  Tree<int>* ll = new Tree<int>(NULL,x,NULL);

  left->setLeftTree(ll);

  const Tree<int>* r = root.getRightTree();
  const int& y = r->getData();

  cout <<"nonRecPostorder3 Traversal ....\n";
  nonRecPostorder3(&root);

  cout << "\n\n\n" ;

//  y++ ;
 // cout << "y = " << y << "\t r data = " << r->getData() << endl; 

//  cout <<"IN ORDER Traversal ....\n";
//  inorder(&root);

//  cout << "\n\n\n" ;


//  cout <<"PRE ORDER Traversal ....\n";
//  preorder(&root);

//  cout << "\n\n\n" ;

//  cout <<"POST ORDER Traversal ....\n";
//  postorder(&root);

//  cout << "\n\n\n" ;

//  cout <<"LEVEL ORDER Traversal ....\n";
//  levelorder(&root);

//  cout << "\n\n\n" ;

//  cout << "Iterative Inorder traversal....\n";
//  iterativeInorder(&root);

//  cout <<"\n\n\n\n" ;


//  cout << "Non Recursive Inorder traversal....\n";
//  nonRecInorder(&root);

//  cout <<"\n\n\n\n" ;

//  cout << "Non Recursive Preorder traversal....\n";
//  nonRecPreorder(&root);

//  cout <<"\n\n\n\n" ;

//  cout << "Non Recursive Postorder traversal....\n";
//  nonRecPostorder(&root);

//  cout <<"\n\n\n\n" ;

//  cout << "Non Recursive Postorder 2  traversal....\n";
//  nonRecPostorder2(&root);

//  cout <<"\n\n\n\n" ;
}

int main()
{
  testGeneral();
  return 0;
}
