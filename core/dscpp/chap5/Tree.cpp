#ifndef TREE_CPP
#define TREE_CPP

#include <iostream>
#include<queue>
#include<stack>
#include<list>
using namespace std;
template<typename T>
class Tree;

template<typename T>
void deleteTree(Tree<T>* tree);

template<typename T>
void nonRecPreorder(const Tree<T>* tree);

template<typename T>
void nonRecPreorder(const Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void nonRecPostorder(Tree<T>* tree);

template<typename T>
void nonRecPostorder(Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void nonRecPostorder2(Tree<T>* tree);

template<typename T>
void nonRecPostorder2(Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void nonRecPostorder3(const Tree<T>* tree);

template<typename T>
void nonRecPostorder3(const Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void nonRecInorder(const Tree<T>* tree);

template<typename T>
void nonRecInorder(const Tree<T>* tree, void(*visit)(const T& data));


template<typename T>
void inorder(const Tree<T>* tree);

template<typename T>
void inorder(const Tree<T>* tree, void(*visit)(const T& data));


template<typename T>
void iterativeInorder(const Tree<T>* tree);

template<typename T>
void iterativeInorder(const Tree<T>* tree, void(*visit)(const T& data));


template<typename T>
void preorder(const Tree<T>* tree);

template<typename T>
void preorder(const Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void postorder(const Tree<T>* tree);

template<typename T>
void postorder(const Tree<T>* tree, void(*visit)(const T& data));

template<typename T>
void levelorder(const Tree<T>* tree);

template<typename T>
void levelorder(const Tree<T>* tree, void(*visit)(const T& data));


template<typename T>
class Tree
{
  private :
    /*
       only used for iterative algorithms. has nothing to do with Tree as such
       */
    bool visited;
    Tree* leftTree;
    T data;
    Tree* rightTree;

  public:

    Tree( Tree* leftTree, T& data, Tree* rightTree) // keeps a copy of the data
    {
      this->leftTree = leftTree;
      this->rightTree = rightTree;
      this->data = data;
      this->visited = false;
    }

    Tree(T& data)
    {
      this->setRightTree(NULL);
      this->setLeftTree(NULL);
      this->data = data;
      this->visited = false;
    }

    ~Tree()
    {
      // cout << "deleting " << this->getData() << endl;
    }

    Tree * getRightTree() const
    {
      return rightTree;
    }

    Tree * getLeftTree() const
    {
      return leftTree;
    }

    const T& getData()const
    {
      return data;
    }

    void setRightTree(Tree* rightTree)
    {
      this->rightTree = rightTree;
    }

    void setLeftTree(Tree* leftTree)
    {
      this->leftTree = leftTree;
    }

    void setData(T& data)
    {
      this->data = data;
    }

    void setVisited(bool visited)
    {
      this->visited = visited;
    }

    bool getVisited() const
    {
      return visited;
    }


// All these function do not need to be friend function.
    friend void inorder<>(const Tree<T>* tree);
    friend void inorder<>(const Tree<T>* tree, void(*visit)(const T& data));


    /*
       This algorithm uses a bool visited in the tree node.
       But it is possible to do inorder without the visisted tag in tree node see NonRecInorder()
       */
    friend void iterativeInorder<>(const Tree<T>* tree);
    friend void iterativeInorder<>(const Tree<T>* tree, void(*visit)(const T& data));

    friend void nonRecInorder<>(const Tree<T>* tree);
    friend void nonRecInorder<>(const Tree<T>* tree, void(*visit)(const T& data));


    friend void preorder<>(const Tree<T>* tree);
    friend void preorder<>(const Tree<T>* tree, void(*visit)(const T& data));

    friend void nonRecPreorder<>(const Tree<T>* tree);
    friend void nonRecPreorder<>(const Tree<T>* tree, void(*visit)(const T& data));

    friend void postorder<>(const Tree<T>* tree);
    friend void postorder<>(const Tree<T>* tree, void(*visit)(const T& data));

    friend void deleteTree<>(Tree<T>* tree);

    /*
       This algorithm uses a bool visited in the tree node and uses list. There is a better performance
       alternative to this see nonRecPostOrder2
       But it is possible to do postOrder  without the visited tag in tree node see nonRecPostOrder3;
       */
    friend void nonRecPostorder<>(Tree<T>* tree);
    friend void nonRecPostorder<>(Tree<T>* tree, void(*visit)(const T& data));

    /*
       This algorithm uses a bool visited in the tree node and uses Stack.
       But it is possible to do postOrder  without the visited tag in tree node see nonRecPostOrder3;
       */
    friend void nonRecPostorder2<>(Tree<T>* tree);
    friend void nonRecPostorder2<>(Tree<T>* tree, void(*visit)(const T& data));

    friend void nonRecPostorder3<>(const Tree<T>* tree);
    friend void nonRecPostorder3<>(const Tree<T>* tree, void(*visit)(const T& data));

    friend void levelorder<>(const Tree<T>* tree);
    friend void levelorder<>(const Tree<T>* tree, void(*visit)(const T& data));
};

template<typename T>
void print(const T& data)
{
  cout << data << ',';
}

template<typename T>
void nonRecPreorder(const Tree<T>* tree)
{
//  cout << "Calling  iterativeInorder(tree,print); \n" ;
  nonRecPreorder(tree,print);
}
template<typename T>
void nonRecPreorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  stack<const Tree<T>*> nodeStack;

  const Tree<T>* currNode = tree;
  while(true)
  {
    while(NULL != currNode)
    {
//      cout << "pushing into stack : " << currNode->getData() << endl ;
      visit(currNode->getData());
      nodeStack.push(currNode);
      currNode = currNode->getLeftTree();
//    cin.ignore() ;
    }

    if( false == nodeStack.empty() )
    {
        currNode = nodeStack.top();
        nodeStack.pop();
//        cout << "current : " << currNode->getData()  << endl ;
        currNode = currNode->getRightTree();
//        cin.ignore() ;
    }
    else
        {
          break;
        }
//    cin.ignore() ;
  }
}


/*
   We use a prev variable to keep track of the previously-traversed node. Let’s assume curr is the current node that’s on top of the stack. When prev is curr‘s parent, we are traversing down the tree. In this case, we try to traverse to curr‘s left child if available (ie, push left child to the stack). If it is not available, we look at curr‘s right child. If both left and right child do not exist (ie, curr is a leaf node), we print curr‘s value and pop it off the stack.

   If prev is curr‘s left child, we are traversing up the tree from the left. We look at curr‘s right child. If it is available, then traverse down the right child (ie, push right child to the stack), otherwise print curr‘s value and pop it off the stack.

   If prev is curr‘s right child, we are traversing up the tree from the right. In this case, we print curr‘s value and pop it off the stack.
   */

template<typename T>
void nonRecPostorder3(const Tree<T>* tree)
{
  nonRecPostorder3(tree,print);
}

/*
http://www.leetcode.com/2010/10/binary-tree-post-order-traversal.html
   */
template<typename T>
void nonRecPostorder3(const Tree<T>* tree, void(*visit)(const T& data))
{
  if (!tree) return;
        
  stack<const Tree<T>*> s;
  s.push(tree);
  const Tree<T> *prev = NULL;
  while (!s.empty()) {
     const Tree<T> *curr = s.top();
     // we are traversing down the tree
     if (!prev || prev->getLeftTree() == curr || prev->getRightTree() == curr) {
        if (curr->getLeftTree()) {
           s.push(curr->getLeftTree());
        } else if (curr->getRightTree()) {
           s.push(curr->getRightTree());
        } else {
          visit(curr->getData());
           s.pop();
        }
     }
     // we are traversing up the tree from the left
     else if (curr->getLeftTree() == prev) {
            if (curr->getRightTree()) {
               s.push(curr->getRightTree());
             } else {
               visit(curr->getData());
               s.pop();
             }
          }
          // we are traversing up the tree from the right
     else if (curr->getRightTree() == prev) {
       visit(curr->getData());
       s.pop();
     }
    prev = curr;  // record previously traversed node
   }
}
template<typename T>
void nonRecPostorder2(Tree<T>* tree)
{
  nonRecPostorder2(tree,print);
}

/**
  This method modifies the tree
http://en.wikipedia.org/wiki/Post-order_traversal#Iterative_Traversal

psudeocode : 
   iterativePostorder(rootNode)
      nodeStack.push(rootNode)
         while (! nodeStack.empty())
                currNode = nodeStack.peek()
                       if ((currNode.left != null) and (currNode.left.visited == false))
                                  nodeStack.push(currNode.left)
                                         else if ((currNode.right != null) and (currNode.right.visited == false))
                                                    nodeStack.push(currNode.right)
                                                           else
                                                                      print currNode.value
                                                                                 currNode.visited := true
                                                                                            nodeStack.pop()
  */
template<typename T>
void nonRecPostorder2(Tree<T>* tree, void(*visit)(const T& data))
{
    stack<Tree<T>*> nodeStack;

    if( NULL != tree )
        nodeStack.push(tree);

    while( false == nodeStack.empty() )
    {
      Tree<T>* curr = nodeStack.top();
      if( NULL != curr->getLeftTree() && false == curr->getLeftTree()->getVisited() )
      {
        nodeStack.push(curr->getLeftTree());
      }
      else if ( NULL != curr->getRightTree() && false == curr->getRightTree()->getVisited() )
      {
        nodeStack.push(curr->getRightTree());
      }
      else
      {
        visit(curr->getData());
        curr->setVisited(true);
        nodeStack.pop();
      }
    }
}

template<typename T>
void nonRecPostorder(Tree<T>* tree)
{
//  cout << "Calling  iterativeInorder(tree,print); \n" ;
  nonRecPostorder(tree,print);
}

/**
  This method intermittently modifies the tree
  */
template<typename T>
void nonRecPostorder(Tree<T>* tree, void(*visit)(const T& data))
{
  list<Tree<T>* > nodeList;
  Tree<T>* currNode = tree;
  while(true)
  {
    // put the current node and go to its right
    while(NULL != currNode)
    {
      cout << "putting into list : " << currNode->getData() << endl;
      nodeList.push_back(currNode);
      currNode = currNode->getRightTree();
    }

    // find the non marked node from the back.
    typename list<Tree<T>* >::reverse_iterator iter = nodeList.rbegin() ;
    while( iter != nodeList.rend() && ( true == (*iter)->getVisited()) ) 
    {
      cout << "this node is visited so looking for previous : " << (*iter)->getData() << endl ; 
      iter++;
    }

    if( iter != nodeList.rend() )
    {
        (*iter)->setVisited(true);
        cout << "current : " << (*iter)->getData()  << endl ;
        currNode = (*iter)->getLeftTree();
//        cin.ignore() ;
    }
    else
        {
          break;
        }
//    cin.ignore() ;
  }

  typename  list<Tree<T>*>::reverse_iterator iter = nodeList.rbegin();
  while( iter != nodeList.rend() )
  {
    visit((*iter)->getData());
    (*iter)->setVisited(false);
    iter++;
  }
}

template<typename T>
void nonRecInorder(const Tree<T>* tree)
{
//  cout << "Calling  iterativeInorder(tree,print); \n" ;
  nonRecInorder(tree,print);
}
template<typename T>
void nonRecInorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  stack<const Tree<T>*> nodeStack;

  const Tree<T>* currNode = tree;
  while(true)
  {
    while(NULL != currNode)
    {
//      cout << "pushing into stack : " << currNode->getData() << endl ;
      nodeStack.push(currNode);
      currNode = currNode->getLeftTree();
//    cin.ignore() ;
    }

    if( false == nodeStack.empty() )
    {
        currNode = nodeStack.top();
        nodeStack.pop();
//        cout << "current : " << currNode->getData()  << endl ;
        visit(currNode->getData());
        currNode = currNode->getRightTree();
//        cin.ignore() ;
    }
    else
        {
          break;
        }
//    cin.ignore() ;
  }
}

template<typename T>
void iterativeInorder(const Tree<T>* tree)
{
//  cout << "Calling  iterativeInorder(tree,print); \n" ;
  iterativeInorder(tree,print);
}

template<typename T>
void iterativeInorder(const Tree<T>* tree, void(*visit)(const T& data))
{

//  cout << "Inside iterativeInorder \n" ;
  stack<Tree<T>*> nodeStack;
  nodeStack.push(const_cast<Tree<T>*>(tree));
  while(false == nodeStack.empty())
  {
//    cout << "Getting the top element \n";
    Tree<T>* curr = nodeStack.top();
//    cout << "Analyzing curr = " << curr->getData() << endl ;
    if( NULL == curr->getLeftTree() || true == curr->getLeftTree()->getVisited())
    {
//      cout << "left is NULL or is visisted.\n";
      // pop visit and put the right child in stack if not null
      visit(curr->getData());
      curr->setVisited(true);
      nodeStack.pop();
      if( NULL != curr->getRightTree() )
      {
//          cout << "putting into stack : " << curr->getRightTree()->getData() << endl ;
          nodeStack.push(const_cast<Tree<T>*>(curr->getRightTree()));
      }
    }
    else
    {
      // if left child not null then put it into the stack
      if( NULL != curr->getLeftTree() )
      {
//        cout <<"Left is not null and is not visited so puting into the stack : " << curr->getLeftTree()->getData() << endl ;
        nodeStack.push(const_cast<Tree<T>*>(curr->getLeftTree()));
      }
    }
//    cin.ignore();
  }
}

template<typename T>
void inorder(const Tree<T>* tree)
{
  inorder(tree,print);
}
template<typename T>
void inorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  if( NULL != tree ) 
  {
    inorder(tree->getLeftTree());
    visit(tree->getData());
    inorder(tree->getRightTree());
  }
}



template<typename T>
void preorder(const Tree<T>* tree)
{
  preorder(tree,print);
}
template<typename T>
void preorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  if( NULL != tree ) 
  {
    visit(tree->getData());
    preorder(tree->getLeftTree());
    preorder(tree->getRightTree());
  }
}

/*
this is variation of post order.
this could have been done using post-order if the visit fuction had taken the
tree node i.e Tree<T>* instead of data
*/
template<typename T>
void deleteTree(Tree<T>* tree)
{
  if( NULL != tree ) 
  {
    deleteTree(tree->getLeftTree());
    deleteTree(tree->getRightTree());
    delete tree;
  }
}

template<typename T>
void postorder(const Tree<T>* tree)
{
  postorder(tree,print);
}
template<typename T>
void postorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  if( NULL != tree ) 
  {
    postorder(tree->getLeftTree());
    postorder(tree->getRightTree());
    visit(tree->getData());
  }
}


template<typename T>
void levelorder(const Tree<T>* tree)
{
  levelorder(tree,print);
}
template<typename T>
void levelorder(const Tree<T>* tree, void(*visit)(const T& data))
{
  cout << "Inside level order..\n";
  queue<Tree<T>*> nodeQueue;
  nodeQueue.push(const_cast<Tree<T>*>(tree));

  while( false == nodeQueue.empty() )
  {
    Tree<T>* curr = nodeQueue.front();
    visit(curr->getData());
    nodeQueue.pop();
    if( NULL != curr->getLeftTree() )
        nodeQueue.push(const_cast<Tree<T>*>(curr->getLeftTree()));

    if( NULL != curr->getRightTree() )
        nodeQueue.push(const_cast<Tree<T>*>(curr->getRightTree()));
  }
}

// construct an integer tree from input. where -1 value denotes NULL sub-tree
Tree<int>* inputTree(int& noOfNodes)
{
  noOfNodes = 0 ;
  int data;
  cout << "Enter value of root : ";
  cin >> data;
  if(cin.eof() || -1 == data)
    return NULL;
  
  Tree<int>* root = new Tree<int>(data);
  queue<Tree<int>*> values ;
  values.push(root);
  noOfNodes++;
  
  while(!values.empty())
  {
    Tree<int>* top = values.front();
    values.pop();
    cout << "Enter Left Child of " << top->getData() << " (-1 for null) : " ;
    cin >> data;
    if( -1 != data )
    {
      noOfNodes++;
      Tree<int>* left = new Tree<int>(data);
      top->setLeftTree(left);
      values.push(left);
    } 

    cout << "Enter Right Child of " << top->getData() << " (-1 for null) : " ;
    cin >> data;
    if( -1 != data )
    {
      noOfNodes++;
      Tree<int>* right = new Tree<int>(data);
      top->setRightTree(right);
      values.push(right);
    }      
  }

  return root;
}

Tree<int>* inputTree()
{
  int noOfNodes;
  return inputTree(noOfNodes);
}

#endif
