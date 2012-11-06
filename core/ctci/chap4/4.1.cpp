#include<iostream>
#include<algorithm>
#include"Tree.cpp"
using namespace std;

/*
book's solution
*/

template<typename T>
int maxDepth(Tree<T>* root) {
   if (root == NULL) {
     return 0;
   }
   return 1 + max(maxDepth(root->getLeftTree()), maxDepth(root->getRightTree()));
 }
    
template<typename T>  
 int minDepth(Tree<T>* root) {
   if (root == NULL) {
      return 0;
    }
    return 1 + min(minDepth(root->getLeftTree()), minDepth(root->getRightTree()));
  }
     
template<typename T>
  bool isBalanced(Tree<T>*root){
   return (maxDepth(root) - minDepth(root) <= 1);
  }

// this is wrong as it compares only the max heights but we have to compare the min with the max 
// template<typename T>
// int height(Tree<T>*t)
// {
//     if(NULL != t)
//     {
//       int lh = height(t->getLeftTree());
//       int rh = height(t->getRightTree());
//       if( -1 == lh ||  -1 == rh  )
//       {
//         return -1; // the left or right tree at this node is unbalanced
//       }
//       if( lh > rh + 1 || rh > lh + 1)
//       {
//          cout << "The left and right tree of this node are unbalanced : " << t->getData() << endl ;
//          return - 1 ;
//       } 
//       else
//       {
//         return (lh > rh ? lh + 1 : rh + 1)  ;
//       }
//     }
//     else
//       return 0;
// }

template<typename T>
int minMaxHeight(Tree<T>*t, int& min, int& max)
{
    if(NULL != t)
    {
      int rmin = 0 , rmax = 0 , lmin = 0 , lmax = 0;
      int lret = minMaxHeight(t->getLeftTree(),lmin, lmax);
      int rret = minMaxHeight(t->getRightTree(),rmin,rmax);
      if( -1 == lret || -1 == rret )
      {
        return -1; // stops further comparison
      }
      if( lmax > rmin + 1 )
      {
        cout << "The left is bigger than right at " << t->getData() << endl;
        return -1; // the left or right tree at this node is unbalanced
      }
      if( rmax > lmin + 1 )
      {
        cout << "The right is bigger than left at " << t->getData() << endl;
        return -1; // the left or right tree at this node is unbalanced
      }
      // set the mins and maxs
      if( lmax > rmax )
        max = lmax + 1;
      else
        max = rmax + 1;
      if( lmin < rmin )
        min = lmin + 1;
      else
        min = rmin + 1;
      return 0;
    }
    else
      return 0;
}


int main()
{
  int x = 1;
Tree<int>* tree = new Tree<int>(NULL,x,NULL);
x = 3;
Tree<int>* r = new Tree<int>(NULL,x,NULL);
x = 2 ;
Tree<int>* l = new Tree<int>(NULL,x,NULL);
x = 4 ;
Tree<int>* ll = new Tree<int>(NULL,x,NULL);
x = 6;
Tree<int>* rl = new Tree<int>(NULL,x,NULL);
x = 5;
Tree<int>* lr = new Tree<int>(NULL,x,NULL);
x = 7;
Tree<int>* rr = new Tree<int>(NULL,x,NULL);
x= 8 ;
Tree<int>* lll = new Tree<int>(NULL,x,NULL);
ll->setLeftTree(lll);

addChilds(r,10,20);
addChilds(r->getRightTree(),30,40);
addChilds(r->getLeftTree(),50,60);
// addChilds(r->getRightTree()->getLeftTree(), 70,80);
// addChilds(r->getRightTree()->getRightTree(),90,100);
// addChilds(r->getLeftTree()->getLeftTree(),110,120);
// addChilds(r->getLeftTree()->getRightTree(),130,140);
// x=9;
// Tree<int>* llll = new Tree<int>(NULL,x,NULL);
// lll->setLeftTree(llll);

// r->setLeftTree(rl);
// l->setLeftTree(ll);

// r->setRightTree(rr);
// l->setRightTree(lr);
tree->setRightTree(r);
tree->setLeftTree(l);

// book answer
if( isBalanced(tree))
{
  cout << "The tree is balanced.\n" ;
}
else
  cout << "The tree is unbalanced.\n";

int min = 0 , max = 0 ;
int h = minMaxHeight(tree, min, max);
if(-1 == h)
{
  cout <<"The tree is unbalanced.";
}
else
{
  cout << "The tree is balanced.";
}

return 0;
}
