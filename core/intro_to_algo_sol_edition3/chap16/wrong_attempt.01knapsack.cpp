/*
The 0-1 knapsack problem is the following. A thief robbing a store ﬁnds n
items. The ith item is worth vi dollars and weighs w i pounds, where vi and w i are
integers. The thief wants to take as valuable a load as possible, but he can carry at
most W pounds in his knapsack, for some integer W . Which items should he take?
(We call this the 0-1 knapsack problem because for each item, the thief must either
take it or leave it behind; he cannot take a fractional amount of an item or take an
item more than once.)
*/

#include <iostream>
#include <list>
#include <algorithm>
#include "../../cpplib/cpputils.h"

using namespace std;

/*
The solution can be done with dynamic programming but not through greedy algorithm
*/

class Item
{
	private:
		static int ID;
		int id;
		int weight;
		int price;
		void init()
		{
			setWeight(0);
			setPrice(0);
			id = ID++;
		}
	public:
		Item()
		{
			init();
		}
		Item(const int& weight, const int& price)
		{
			init();
			this->setWeight(weight);
			this->setPrice(price);
		}
		int getId() const
		{
			return id;
		}

		int getWeight() const
		{
			return weight;
		}
		int getPrice() const
		{
			return price;
		}
		void setWeight(const int& weight)
		{
			this->weight = weight;
		}
		void setPrice(const int& price)
		{
			this->price = price;
		}

		friend ostream& operator << ( ostream& out, const Item& item);
		friend ostream& operator << ( ostream& out, const Item*& item);
		friend istream& operator >> (istream& in, Item& item);
};
int Item::ID = 1;

ostream& operator <<( ostream& out, const Item& item)
{
	out << "[" << item.getId() << "(w=" << item.getWeight() << ",p=" << item.getPrice() << ")]";
	return out;
}

ostream& operator <<( ostream& out, const Item* item)
{
	out << (*item);
	return out;
}

istream& operator >> (istream& in, Item& item)
{
	int w, p;
	cin >> w >> p ;
	item.setWeight(w);
	item.setPrice(p);

	return in;
}

list<Item*> knapsack01RecNonOptimal(list<Item*> items,int weight, int& price)
{
	cout << "called knapsack01RecNonOptimal with weight = " << weight << " and Items as : " ;
	print(items);
	cout << endl;
	//return if list is empty
	if(items.empty())
	{
		price = 0 ; 
		return list<Item*>();
	}	

	//1. include first item and see the result.
	//2. exclude first item and see the result.
	Item* first = items.front();
	items.pop_front();
	int including, excluding;
	list<Item*> inResult, exResult;

	if(first->getWeight() > weight ) // then we cannot include it
		including = -1 ; // we cannot include so putting this to minimum so that it is not counted.
	else
		inResult = knapsack01RecNonOptimal(items,weight - first->getWeight(),including);

	 exResult = knapsack01RecNonOptimal(items,weight,excluding);

	 if( including > excluding )
	 {
	 	price = including + first->getPrice();
	 	cout << "including " << first << " resulted in higher price of : " << price << endl;
	 	inResult.push_back(first);
	 	print("returning the result : " , inResult);
	 	return inResult;
	 }	
	 else
	 {
	 	price = excluding;
	 	cout << "excluding " << first << " resulted in higher price of : " << price << endl;
	 	print("returning the result : " , exResult);
	 	return exResult;
	 }
}

int main()
{
	list<Item*> allItems;
	
	int noOfItems, maxWeight;
	cout << "Enter the maximum weight that can be hold : ";
	cin >> maxWeight ;
	cout << "Enter number of items : ";
	cin >> noOfItems;

	cout << "Enter weight and price for " << noOfItems << " items : \n"; 
	for( int i = 0 ; i < noOfItems ; i++ )
	{
		Item* i = new Item();
		cin >> (*i);
		allItems.push_back(i);
	}

	int maxPrice = 0  ;
	list<Item*> resultItems = knapsack01RecNonOptimal(allItems,maxWeight,maxPrice);

	cout << "The maximum price that can be get is : " << maxPrice << endl;

	print("The list of Items for max Price is : ", resultItems);

	// TODO :delete the nodes from the allItems list;

	return 0;
}
