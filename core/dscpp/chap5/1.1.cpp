/*
Q.   Write a function that reads in a tree represented as a list and creates its internal representation using nodes with three fields:
   tag, data and link

A. I did not understand the meaning of tag. But the below solution uses a node which has a data field and list of nodes which represents its childs.
   A temporary list is used for the algorithm

  */

#include<iostream>
#include<list>

using namespace std;

template<typename T>
class Node;

list<Node<char>*> toBeDeleted;

template<typename T>
class Node
{
  private:
    T data;
    list<Node<T>*> children;
  public :
    Node(T data)
    {
      this->data = data;
    }
    ~Node()
    {

    }
    const T& getData()
    {
      return data;
    }
    void addChild(Node<T>* node)
    {
      children.push_back(node);
    }

    const list<Node<T>*>& getChildren() const
    {
      return children;
    }
};

void createList(const char* string,Node<char>* root)
{
  char LEFT_BRACKET = '(' ;
  char RIGHT_BRACKET = ')' ;
  Node<char>* leftBracket = new Node<char>(LEFT_BRACKET);
  Node<char>* rightBracket = new Node<char>(RIGHT_BRACKET);
  
  toBeDeleted.push_back(leftBracket);
  toBeDeleted.push_back(rightBracket);

//  Node<char>* lastList = root;
  list<Node<char>*> nodeStack;
  
  int i = 0 ;
  char currChar;
  while( (currChar = string[i++]) != '\0' )
  {
    cout << "Analysing input char : " << currChar << endl ;
    switch(currChar)
    {
      case '(':
        nodeStack.push_back(leftBracket);
        break;
      case ')':
        // pop till the last left bracket and 1 character beyond

        cout << "char is ) so popping." << endl;
        while(!nodeStack.empty() && nodeStack.back()->getData() != LEFT_BRACKET)
        {
          cout <<"popping .. " << nodeStack.back()->getData() << endl; 
          nodeStack.pop_back();
        }

        if( nodeStack.empty() )
        {
          cout << "Tree formation completed and still data not completed.";
          return;
        }
        else
        {
          // pop the left bracket and also pop the next character
          cout <<"popping .. " << nodeStack.back()->getData() << endl; 
          nodeStack.pop_back();
          if( nodeStack.empty() )
          {
            cout << "The tree formation finished successfully.";
            return;
          }
          else
          {
//            lastList = nodeStack.back();
            cout <<"popping .. " << nodeStack.back()->getData() << endl; 
            nodeStack.pop_back();
          }
        }
        break;
      case ',':
        break; // do nothing in case of comma
      default : // all others
        // add the letter into the list
        {
          Node<char> * node = new Node<char>(currChar);
          toBeDeleted.push_back(node);
          nodeStack.push_back(node);

          // find the parent
          /*
              list<int>::reverse_iterator rit;
                for ( rit=mylist.rbegin() ; rit != mylist.rend(); ++rit )
                    cout << " " << *rit;
                    */
              cout << "Finding the last Parent .." << endl;
              list<Node<char>*>::reverse_iterator rit = nodeStack.rbegin();
              for(; rit != nodeStack.rend(); rit++)
              {
                   cout << "Iterator at : " << (*rit)->getData() << endl; 
                   if( (*rit)->getData() == LEFT_BRACKET )
                   {
                     cout << "found (" << endl;
                     rit++; // go one more back
                     if( rit == nodeStack.rend() )
                     {
                       // insert into the root
                       cout << "But this is end so adding to root." << endl;
                       root->addChild(node);
                       break;
                     }
                     else
                     {
                       cout << "adding to " << (*rit)->getData() << endl; 
                        (*rit)->addChild(node);
                        break;
                     }
                   }
              }
          }
        }
  }
}

void printList(Node<char>* root)
{
  // print the node and call printList on all the childs
  //cout << root->getData() << "" << endl;
  list<Node<char>*>::const_iterator iter ; 
  iter = root->getChildren().begin() ;
    cout << "Printing children of ... " << root->getData()  << " : " << endl;
  for( ; iter != root->getChildren().end() ; iter++ )
  {
    cout << (*iter)->getData() << '\t' ;
  }

  cout << endl;

  iter = root->getChildren().begin() ;
  for( ; iter != root->getChildren().end() ; iter++ )
  {
    printList(*iter);
  }
}
int main()
{

  Node<char>* root = new Node<char>(' ');
  toBeDeleted.push_back(root);
  const char* str = "(A(B(E(K,L),F),C(G),D(H(M),I,J)))";
  createList(str,root);
  printList(root);
  return 0;
}
