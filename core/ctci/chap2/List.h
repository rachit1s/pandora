#ifndef __LIST_H_
#define __LIST_H_
#include "../../cpplib/Node.h"

template<typename T>
class List;

template<typename T>
Node<T>* getLoopPoint(List<T>* list);

template<typename T>
List<T>* addListsOfReverseNumbersIterative ( const List<T>*const & firstList, const List<T>*const & secondList);

template<typename T> 
Node<T>* getNthFromEndNode (const List<T>* list, const int& n);

template<typename T>
Node<T>* getIfLooped (List<T>* list);

template<typename T>
List<T>* addLists( const List<T>*const & firstList, const List<T>*const & secondList);

template<typename T>
void createLoop(List<T>* list);

template<typename T>
List<T>* createReverse ( const List<T>* list);
template<typename T>
List<T>* nthToLastSubList(const List<T>* list, const int& n);

template<typename T>
Node<T>* getNthNode  ( const List<T>* list, const int& n);

template<typename T>
ostream& operator<<(ostream& out, const List<T>& list)
{
    out << "List[";
    Node<T> * curr = list.head ;
    while( NULL != curr )
    {
      out << *curr << ",";
      curr = curr->getNext();
    }
    out << "]";

    return out;
}

template<typename T>
void removeDuplicates(List<T>* list)
{
  if( NULL == list->head || NULL == list->head->getNext() )
    return;

  Node<T>* curr = list->head;
  Node<T>* next = list->head;
  Node<T>* prev = list->head;
  while(NULL != curr && NULL != curr->getNext())
  {
    // remove all that are equal to curr
    next = curr->getNext();
    prev = curr;
    while(NULL != next)
    {
        if( next->getData() == curr->getData())
        {
          //remove next
          Node<T>* temp = next;
          prev->setNext(next->getNext());
          next = next->getNext();
          delete temp;
        }
        else
        {
          prev = next;
          next = next->getNext();
        }
    }

    curr = curr->getNext();
  }
}

template<typename T>
class List
{
  private :
      Node<T> * head;
  
  public :
      List()
      {
        head = NULL;
      }

      List(const List<T>& list)
      {
        head = NULL;
//        cout << "copy constructure called with : " << list << endl;
        Node<T>* curr = list.head;
//        cout << "curr : " << curr  << endl;
        while( NULL != curr )
        {
//          cout << "coping the data : " << curr->getData() << endl;
          this->add(curr->getData());
          curr = curr->getNext();
        }
      }

      ~List()
      {
        Node<T>* curr = head;
        Node<T>* prev = curr;
        while(NULL != curr)
        {
           curr = curr->getNext();
           delete prev;
           prev = curr;
        }

        head = NULL;
      }

      List& add( const T data )
      {
      //  cout << "called add with data = " << data << endl;
        Node<T>* n = new Node<T>(data);
        if(NULL == head)
        {
          head = n;
        //  cout << "My head was null so setting the head to point to this node : " << *head << endl;
        }
        else
        {
          Node<T>* curr = head;
          //cout << "My Head was not null "<< endl;
          //cout <<": curr : " << *curr << endl; 
          while(NULL != curr->getNext())
          {
            curr = curr->getNext();
          }
          curr->setNext(n);
        }

        return *this;
      }

      List& remove(int index)
      {
      //  cout << "index entered by you : " << index << endl; 
        if( index < 0 ) 
          return *this;

        if(NULL == head) //
          return *this;
        
        if( 0 == index )
        {
          Node<T>* temp = head;
          head = head->getNext();
          temp->setNext(NULL);
          delete temp;

          return *this;
        }

        Node<T>* curr = head;
        for( int i = 0 ; i < index-1 && curr->getNext() != NULL ; i++)
          curr = curr->getNext();
    
        if( NULL != curr->getNext() )
        {
          curr->setNext( curr->getNext()->getNext() );
        }

        return *this;
      }

      void removeNode(const Node<T>* node)
      {
        if( NULL == node ) return;

        Node<T>* curr = head;
        Node<T>* prev = NULL;

        while( NULL != curr && curr != node )
        {
          prev = curr;
          curr = curr->getNext();
        }

        if( NULL != curr ) 
        {
            if( NULL == prev ) // curr == head
            {
             head = head->getNext();
            delete curr;
            }
            else
            {
             prev->setNext(curr->getNext());
             delete curr;
            }
        }
        // else node not found
      }

      friend ostream& operator<< <> ( ostream& out, const List<T>& list);
      friend void removeDuplicates <> (List<T>* list);
      friend List<T>* nthToLastSubList <> (const List<T>* list, const int& n);
      friend Node<T>* getNthNode <> ( const List<T>* list, const int& n);
      friend List<T>* createReverse <> ( const List<T>* list);
      // will only be valid when the (/int,+,%,==, = integer, >int)  operations on T are defined
      // assuming the numbers are stored in normal order i.e. one's digit is at the tail of the list
      friend List<T>* addLists <> ( const List<T>*const & firstList, const List<T>*const & secondList);
      

      // assuming the numbers are stored in reverse order i.e. one's digit is at the head of the list
      friend List<T>* addListsOfReverseNumbersIterative <> ( const List<T>*const & firstList, const List<T>*const & secondList);
      
      friend void createLoop<> (List<T>* list);

      // returns the node point of meeting of the slow and fast pointers
      // NULL if no loop
      friend Node<T>* getIfLooped<> (List<T>* list);

      // returns the actual start point of the loop
      // NULL if no loop present
      friend Node<T>* getLoopPoint<> (List<T>* list);
      friend Node<T>* getNthFromEndNode<> (const List<T>* list, const int& n);
};

template<typename T>
Node<T>* getLoopPoint (List<T>* list)
{
  Node<T>* ptr1 = getIfLooped(list);
  if( NULL == ptr1 ) return NULL;

  Node<T>* ptr2 = list->head;

  while( ptr1 != ptr2)
  {
    ptr1 = ptr1->getNext();
    ptr2 = ptr2->getNext();
  }

  return ptr1;
}

template<typename T>
List<T>* addListsOfReverseNumbersIterative ( const List<T>*const & firstList, const List<T>*const & secondList)
{
    List<T>* retList = new List<T>();
    Node<T>* f = firstList->head;
    Node<T>* s = secondList->head;

    T carry = 0;
    while( NULL != f || NULL != s )
    {
      T data = carry ;
      if( NULL != f )
      {
        data = data + f->getData();
        f = f->getNext();
      }
      
      if( NULL != s )
      {
        data = data + s->getData();
        s = s->getNext();
      }
      
      T value = data % 10 ;
      carry = data/10;
    
      retList->add(value);
    }

    if( 0 != carry ) 
      retList->add(carry);

    return retList;

}

template<typename T>
Node<T>* getNthFromEndNode(const List<T>* list, const int& n)
{
  Node<T>* nth = getNthNode(list,n-1);
  if( NULL == nth ) throw "No such element";

  Node<T>* nlast = list->head;
  while( NULL != nth->getNext() )
  {
    nlast = nlast->getNext();
    nth = nth->getNext();
  }

  return nlast;
}
template<typename T>
Node<T>* getIfLooped(List<T>* list)
{
  if( NULL == list->head || NULL == list->head->getNext() ) // no loop
    return NULL;

  Node<T>* slow = list->head;
  Node<T>* fast = list->head;

  while( NULL != fast && NULL != slow ) // if their is a loop then slow and fast will eventually become equal at the loop-head
  {
    fast = fast->getNext();
    if( NULL == fast ) return NULL; // their is no loop
    else
      fast = fast->getNext();

    slow = slow->getNext();
    
    if( fast == slow ) break;

  }

  if( NULL == slow || NULL == fast )
    return NULL;
  else return slow ;
}

template<typename T>
void createLoop(List<T>* list)
{
  int index = 0 ;
  cout << "Enter the index where you want to loop last node to : " ;
  cin >> index;

  Node<T> * curr = list->head ;
  if( NULL == curr )
    throw "Head of list is null.";

  Node<T>* nthNode = getNthNode(list,index);

  if( NULL == nthNode )
    throw "cannot find the node at given index "; 

  while(NULL != curr->getNext())
  {
    curr = curr->getNext();
  }

  curr->setNext(nthNode);

  cout << "Loop created" << endl;
}

template<typename T>
List<T>* addLists(const List<T>*const & firstList, const List<T>*const & secondList)
{
//    cout<< "AddedLIsts called with \nfirst : " << *firstList << "\nsecond : " << *secondList << endl;
    List<T>* addedList = new List<T>();
    
    List<T>* first = createReverse(firstList);
    List<T>* second = createReverse(secondList);

  //  cout << "First list reversed :" << *first << endl;
  //  cout << "Second list reversed : " << *second << endl;
    
    Node<T>* fc = first->head;
    Node<T>* sc = second->head;

    T rem = 0 ;
    while(NULL != fc || NULL != sc)
    { 
      T fcurr = 0;
      T scurr = 0;
      // if non null get curr values
      if( NULL != fc ) fcurr = fc->getData();
      if( NULL != sc ) scurr = sc->getData();

    //  cout << "Considering : fcurr " << fcurr << " : and scurr : " << scurr << endl ;

      if( ( fcurr < 0 || fcurr > 9 ) )
      {
        delete first;
        delete second;
        delete addedList;
        throw "Error : illegal value in first list";
      }
      if( ( scurr < 0 || scurr > 9 ) )
      {
        delete first;
        delete second;
        delete addedList;
        throw "Error : illegal value in second list";
      }

      T sum = fcurr + scurr + rem ;
      T value = sum % 10 ;
      rem = sum/10  ;
      //cout << "sum = " << sum << " : and rem = " << rem  << " : and value = " << value << endl;  

      addedList->add(value);

      if( NULL != fc ) fc = fc->getNext();
      if( NULL != sc ) sc = sc->getNext();
    }
    delete first;
    delete second;
    
  //  cout << "Added list before adding final remainder : " << *addedList << endl; 

    if( rem != 0 ) addedList->add(rem);

   // cout << "Added list after adding final remainder : " << *addedList << endl; 

    List<T>* addedRevList = createReverse(addedList);

  //  cout << "Final Result : " << *addedRevList << endl ;

    delete addedList;
    
    return addedRevList;
}

template<typename T>
List<T>* createReverse(const List<T>* list)
{
//  cout << "createReverse called with :" << *list << endl;
  List<T>* nList = new List<T>(*list);

//  cout << "createReverse : after duplicating :  " << *nList << endl; 

  if( NULL == nList->head || NULL == nList->head->getNext()) return nList;

  Node<T>* prev = nList->head;
  Node<T>* curr = prev->getNext();
  Node<T>* next = curr->getNext();
  while(NULL != next)
  {
//    cout << "createReverse : curr = " << *curr << endl; 
    curr->setNext(prev);
    prev = curr;
    curr = next;
    next = next->getNext();
  }

  curr->setNext(prev);
  nList->head->setNext(NULL);
  nList->head = curr;

  return nList;
}

template<typename T>
Node<T>* getNthNode( const List<T>* list, const int& n)
{
  if( n < 0 ) 
    return NULL;

  Node<T>* nthNode = list->head;
  int i = 0;
  while(i<n && NULL != nthNode) 
  {
    nthNode = nthNode->getNext();
    i++;
  }

  return nthNode;
}

template<typename T>
List<T>* nthToLastSubList (const List<T>* list, const int& n)
{
    List<T>* nList = new List<T>();
    if(n < 0 )
      return nList;
//    if( NULL == list->head )
//      return nList;

    // get nth node
    int i = 0 ;
    Node<T>* nthNode = list->head;
    while(i<n && NULL != nthNode) 
    {
      nthNode = nthNode->getNext();
      i++;
    }

    if( NULL == nthNode )
      return nList;

    while(NULL != nthNode)
    {
      nList->add(nthNode->getData());
      nthNode = nthNode->getNext();
    }

    return nList;
}

 void createList(List<int>* list )
 {
   int value = -1 ;
   cout << "Enter a value to put into list ( -1 to end input) : ";
   cin >> value;
   while( -1 != value )
   {
     list->add(value);
     cout << "List after adding your value : " << *list << endl;
     cout << "Enter another value to put into list ( -1 to end input) : " ;
     cin >> value;
   }
 
   cout << "List after all your input : " << *list << endl;
 }
 
 void removeFromList(List<int>* list)
 {
   int i = 0 ;
 
   cout << "Enter the index to remove (-1 to end): " ;
   cin >> i;
   while(-1 != i)
   {
      list->remove(i);
      cout << "list after removal : " << *list << endl ;
      cout <<"Enter another index to remove (-1 to end): " ;
      cin >> i;
   }
 
   cout << "List after finishing deletin operations : " << *list  << endl ;
 
 }


#endif
