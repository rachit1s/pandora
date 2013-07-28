#include<iostream>
#include<ostream>

using namespace std;

template<typename T>
class ThreadedNode;

template<typename T>
class ThreadedTree;

template<typename T>
ostream& operator<<(ostream& out, const ThreadedNode<T>& node);

template<typename T>
ostream& operator<< (ostream& out, const ThreadedTree<T>& tree);

template<typename T>
class ThreadedNode
{
  static int ID;
  private :
    bool leftThread;
    bool rightThread;
    ThreadedNode<T>* leftNode;
    ThreadedNode<T>* rightNode;
    T data;

    // not required in tree's specs but for programmatic use
    int id;

  public :
    ThreadedNode()
    {
      ID++;
      this->id = id;
    }
    ThreadedNode(const T data) : ThreadedNode()
    {
      this->data = data;
    }

    int getId()
    {
      return id;
    }

    bool getLeftThread() const
    {
      return this->leftThread;
    }

    void setLeftThread(bool leftThread) 
    {
      this->leftThread = leftThread;
    }

    bool getRightThread() const
    {
      return this->rightThread;
    }

    void setRightThread(bool rightThread)
    {
      this->rightThread = rightThread;
    }

    ThreadedNode<T>* getLeftNode() const
    {
      return this->lefThreadedNode;
    }

    void setLeftNode(const ThreadedNode<T>* leftNode) 
    {
      this->leftNode = leftNode;
    }

    ThreadedNode<T>* getRightNode() const
    {
      return this->rightNode;
    }

    void setRightNode(const ThreadedNode<T>* rightNode)
    {
      this->rightNode = rightNode;
    }

    T& getData() const
    {
      return this->data;
    }

    void setData(const T& t)
    {
      this->data = t;
    }

    friend ostream& operator<< <>(ostream& out, const ThreadedNode<T>& node);
};

template<typename T>
ostream operator<<(ostream& out, const ThreadedNode<T>& node)
{
  out << "[ id=" << node->getId() << ",data=" << node->getData() << "]";
  return out;
}

template<typename T>
class ThreadedTree
{
  private :
    ThreadedNode<T>* root;
  public : 
    ThreadedTree()
    {
      this->root = new ThreadedNode<T>();
      root->setRightNode(root);
      root->setLeftNode(root);
      root->setRightThread(true);
      root->setLeftThread(true);
    }
    ThreadedNodeT>* getRoot()const
    {
      return this->root;
    }
    void setRoot(ThreadedNode<T>*root)
    {
      this->root = root;
    }

  friend ostream& operator<< <>(ostream& out, const ThreadedTree<T>& tree);
  template<typename S>
  friend class ThreadedInorderIterator<S>;
};

template<typename T>
int ThreadedNode<T>::ID = 0 ;

template<typename T>
class ThreadedInorderIterator
{
  private :
    ThreadedTree<T>* t;
    ThreadedNode<T>* currentNode;
  public :
    ThreadedInorderIterator(ThreadedTree<T>* tree):t(tree)
    {
      currentNode = t.getRoot();
    }
    ThreadedNode<T>* next();
};

template<typename T>
ostream& operator<< (ostream& out, const ThreadedTree<T>& tree)
{
  ThreadedInorderIterator<T> iter = new ThreadedInorderIterator<T>(tree);
  ThreadedNode<T>* curr = iter.next() ;
  out << "ThreadedTree[";
  while(NULL != curr && tree.getRoot() != curr)
  {
    out << (*curr) << "," ; 
  }
  out << "]";
  return out;
}


template<typename T>
ThreadedNode<T>* ThreadedInorderIterator<T>::next()
{
  ThreadedNode<T> * temp = currentNode->getRightNode();
  if( !currentNode->getRightThread() )
    while(!temp->getLeftThread()) temp = temp->getLeftNode();

    currentNode = temp;
    if(currentNode == t.getRoot()) return NULL;
    else return currentNode;
}

template<typename T>
ThreadedNode<T>* getSuccessor( ThreadedTree<T>* ttree, ThreadedNode<T>* node)
{
  ThreadedNode<T> * temp = node->getRightNode();
  if( !node->getRightThread() )
    while(!temp->getLeftThread()) temp = temp->getLeftNode();

    node = temp;
    if(node == ttree.getRoot()) return NULL;
    else return node;
}

template<typename T>
void insert(ThreadedTree<T>* tree, ThreadedNode<T>* treeNode, bool onRight, T& data)
{
  ThreadedNode<T>* node = new ThreadedNode<T>(data);
  if(onRight)
  {
    node->setRightNode(treeNode->getRightNode());
    node->setRightThread(treeNode->getRightThread());
    node->setLeftNode(treeNode);
    node->setLeftThread(true);
    if( false == treeNode->getRightThread() )
    {
      ThreadedNode<T> * suc = getSuccessor(tree,treeNode);
      suc->setLeftNode(node);
    }
    treeNode->setRightNode(node);
    treeNode->setRightThread(false);
  }
  else // on left...
  {
    node->setRightNode(treeNode);
    node->setRightThread(false);
    node->setLeftNode(treeNode->getLeftNode());
    node->setLeftThread(treeNode->getLeftThread());
    if(false == treeNode->getLeftThread())
    {
      ThreadedNode<T>* temp = treeNode->getLeftNode();
      while(!temp->getRightThread()) temp = temp->getRightNode();

      temp->setRightNode(node);
    }
    treeNode->setLeftNode(node);
    treeNode->setLeftThread(false);
  }
}

int main()
{
  ThreadedTree<char>* tt = new ThreadedTree<char>();
  insert(tt, tt.getRoot(), false, 'A');
  cout << (*tt) << endl ;

  return 0;
}
