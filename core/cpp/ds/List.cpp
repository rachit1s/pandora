#include<iostream>
#include<string>
#include<sstream>

using namespace std;

template<typename T>
class Node
{
  public:
    T data;
    Node* next;
    Node(T data, Node* next)
    {
      this->data = data;
      this->next = next;
    }
};

template<typename T>
class List;

template<typename T>
ostream& operator<< (ostream& out, const List<T>& list);

template<typename T>
class List
{
  private :
    Node<T>* first;
  public :
    List<T>();
    ~List<T>();
    void insert(T& data);
    void insertAfter(T& data, int index);
    void remove(int index);
    T* get(int index, T& t);
    
    string toString() const;
    friend ostream& operator<< <>(ostream& out, const List<T>& list);
};

template<typename T>
List<T>::List()
{
  first = NULL;
}

template<typename T>
List<T>::~List()
{
}


template<typename T>
void List<T>::insert(T& data)
{
  Node<T>* nptr = new Node<T>(data,NULL);
  if( NULL == first )
  {
    first = nptr;
    cout << "set the first to nptr" << endl;
  }
  else
  {
    // insert at end
    Node<T>* ptr = first;
    while(NULL != ptr->next ) ptr = ptr->next;

    ptr->next = nptr;
  }
}

template<typename T>
void List<T>::insertAfter(T& data, int index)
{
    Node<T>* nptr = new Node<T>(data,NULL);
    Node<T>* ptr = first;
    if(0>index)
    {
      nptr->next= ptr;
      first= nptr;
      return;
    }
    while(NULL != ptr && 0 != index )
    {
      ptr = ptr->next;
      index--;
    }
    
    if( NULL == ptr )
    {
      cout << "Index out of bound exception." << endl;
      return;
    }

    nptr->next = ptr->next;
    ptr->next = nptr;
//nptr->next = tmp;
 }

template<typename T>
void List<T>::remove(int index)
{
    Node<T>* ptr = first;
    if(0==index && 0!=ptr)
    {
      first = ptr->next;
      delete ptr;
      return;
    }
    if(0>index)
    {
      cout<<"invalid index"<<endl;
      return;
    }
    while(NULL != ptr && 0 != (index-1))
    {
    ptr = ptr->next;
    index--;
    }
     if( NULL == ptr )
    {
    cout << "Index out of bound exception." << endl;
    return;
    }

    Node<T>* temp = ptr->next;
    ptr->next= ptr->next->next;
    delete temp;
}

template<typename T>
T* List<T>::get(int index, T& t)
{
    if( index < 0 ) 
    {
      cout << "index out of bound.";
      return NULL;
    }
    
    Node<T>* ptr = first;
    while( NULL != ptr && 0 != index)
    {
      ptr = ptr->next;
      index--;
    }

    if( NULL == ptr ) 
    {
      cout <<"index out of bound" << endl;
      return NULL;
    }

    t = ptr->data;

    return &t;
}

template<typename T>
string List<T>::toString()const
{
  stringstream ss;
  ss << "List[" ;
  Node<T>* ptr = first;
  while(NULL != ptr )
  {
    ss << ptr->data << "," ;
    ptr = ptr->next;
  }

  ss << "]";
  return ss.str();
}

template<typename T>
ostream& operator<< (ostream& out, const List<T>& list)
{
  out << list.toString();
  return out;
}

int main()
{
  int i ;
  List<int> list;

  List<double> list2;

  List<string>list3;
  List<string*>list4;
  do
  {
    cout << "Enter an operation :\n"
         << "1. insert.\n"
         << "2. insertAfter.\n"
         << "3. remove.\n"
         << "4. get.\n"
         << "5. print.\n";
    cin >> i ;

    if( cin.eof() )
      break;

    int data ;
    int index;
    int* dptr = NULL;
    switch(i)
    {
      case 1 : 
        cout << "Enter the value to insert : ";
        cin >> data;
        list.insert(data);

        break;
      case 2 : 
        cout << "Enter the index after which to insert : " ;
        cin >> index;
        cout << "Enter the value to insert : ";
        cin >> data;
        list.insertAfter(data,index);
        break;

      case 3 :
        cout << "Enter the index to remove : ";
        cin >> index;
        list.remove(index);
        break;

      case 4: 
        cout << "Enter the index to get : ";
        cin >> index;
        dptr = list.get(index,data);
        if( NULL != dptr ) 
          cout << "The value at " << index << " is : " << data;
        
        break;
       
      case 5 :
        cout << list ;
        break;

      defalt : cout << "Illegal operation. !"; 
    }
    cout << endl ;
  }while( !cin.eof());

  return 0;
}
