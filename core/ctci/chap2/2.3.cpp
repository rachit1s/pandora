#include "../../cpplib/includelibs.h"
#include "List.h"

void deleteNode_Inefficient(Node<int>* node)
{
    // shift the values of all the data into list
    Node<int>* curr = node->getNext(); // note it is given that the node is not the last node
    Node<int>* prev = node;
    while(NULL != curr->getNext())
    {
      prev->setData(curr->getData());
      prev = curr;
      curr = curr->getNext();
    }

    prev->setData(curr->getData());
    prev->setNext(NULL);

    delete curr;
}

void deleteNode(Node<int>* node)
{
    if( NULL == node || NULL == node->getNext() )
      throw "Illegal parameters.";

    Node<int>* curr = node->getNext(); // note it is given that the node is not the last node
    Node<int>* prev = node;
    prev->setData(curr->getData());

    prev->setNext(curr->getNext());

    delete curr;
}

int main()
{
  List<int>* list = new List<int>();
  createList(list);

    int index = -1;
  while(true)
  {
    cout << "Enter the index of the node in the middle of the list to remove  (-1 to end)  : " ;
    cin >> index;

    if( -1 == index ) 
      break;

    Node<int>* node = getNthNode(list,index);
   
    cout << "Will now remove node : ";
    if( NULL == node )
      cout << "NULL" << endl;
    else
      cout << *node << endl;

    deleteNode(node);

    cout << "List after removing :" << *list << endl; 
  }

  delete list;
}
