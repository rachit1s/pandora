/*
9 7  A circus is designing a tower routine consisting of people standing atop one anoth-
er’s shoulders  For practical and aesthetic reasons, each person must be both shorter 
and lighter than the person below him or her  Given the heights and weights of each 
person in the circus, write a method to compute the largest possible number of peo-
ple in such a tower 
EXAMPLE:
Input (ht, wt): (65, 100) (70, 150) (56, 90) (75, 190) (60, 95) (68, 110)
Output: The  longest  tower  is  length  6  and  includes  from  top  to  bottom:  (56,  90) 
(60,95) (65,100) (68,110) (70,150) (75,190)
*/

#include <iostream>
#include <list>
#include "../../cpplib/cpputils.h"

using namespace std;
class Person
{
	private: 
		static int ID;
		int id ;
		int height;
		int weight;
		void init()
		{
			id = ID++;
			height = weight = 0 ;
		}
	public:
		Person()
		{
			init();
		}
		Person( const int& h, const int& w)
		{
			this->setHeight(h);
			this->setWeight(w);
		}
		int getId() const
		{
			return id;
		}
		void setHeight(const int& height)
		{
			this->height = height;
		}
		void setWeight(const int& weight)
		{
			this->weight = weight;
		}

		int getHeight() const
		{
			return height;
		}

		int getWeight() const{
			return weight;
		}

		friend ostream& operator << ( ostream& out, const Person& person);
		friend istream& operator >> ( istream& in , Person& person);
		friend ostream& operator << (ostream& out, const Person* person);
};

int Person::ID = 1 ;

ostream& operator<<(ostream& out, const Person& person)
{
	out << "[id=" << person.getId() << ",height=" << person.getHeight() << ",weight=" << person.getWeight() << "]";

	return out;
}

istream& operator >> ( istream& in , Person& person)
{
	int h, w;
	in >> h >> w ;
	person.setWeight(w);
	person.setHeight(h);

	return in ;
}
ostream& operator << (ostream& out, const Person* person)
{
	out << (*person) ;

	return out;
}

bool heightComparator(Person*& p1, Person*& p2)
{
	return (p1->getHeight() < p2->getHeight());
}
bool weightComparator(Person*&p1, Person*& p2)
{
	return (p1->getWeight() < p2->getWeight());
}

void longestCommonSubSequence(int* list1, int* list2, int noOfPerson, list<int>& resultIds)
{
	cout << "Called longestCommonSubSequence for following parameters : \n";
	print("list1 : " , list1, noOfPerson+1);
	print("list2 : ", list2, noOfPerson+1);

	const int INCLUDED = 1 ;
	const int DEC_I = 2 ;
	const int DEC_J = 3;
	const int FINISH = 0 ;

	// sizeMatrix
	int** sizeMatrix = create2DArray(noOfPerson+1,noOfPerson+1,0);
	int** resultMatrix = create2DArray(noOfPerson+1,noOfPerson+1,FINISH);

	for( int i = 1 ; i <= noOfPerson ; i++ )
	{
		for( int j = 1 ; j <= noOfPerson ; j++ )
		{
			if( list1[i] == list2[j] )
			{
				sizeMatrix[i][j] = sizeMatrix[i-1][j-1] + 1;
				resultMatrix[i][j] = INCLUDED;
			}
			else if( sizeMatrix[i][j-1] < sizeMatrix[i-1][j] )
			{
				// set the result to i,j-1
				sizeMatrix[i][j] = sizeMatrix[i-1][j];
				resultMatrix[i][j] = DEC_I;
			}
			else 
			{
				sizeMatrix[i][j] = sizeMatrix[i][j-1];
				resultMatrix[i][j] = DEC_J;
			}
		}
	}

	print("sizeMatrix is : \n",sizeMatrix, noOfPerson+1,noOfPerson+1);
	print("resultMatrix is :\n", resultMatrix, noOfPerson+1,noOfPerson+1);

	for( int i = noOfPerson, j = noOfPerson ; i >= 1 && j >= 1 ; )
	{
		if( INCLUDED == resultMatrix[i][j] )
		{
			resultIds.push_back(list1[i]);
			i = i-1;
			j = j-1;
		}
		else if( DEC_I == resultMatrix[i][j] )
		{
			i = i-1;
		}
		else if( DEC_J == resultMatrix[i][j])
		{
			j = j-1;
		}
		else
		{
			cout << "you have reached the end of one of the array .. although the for loop will break before this.";
		}
	}

	delete2DArray(sizeMatrix,noOfPerson+1);
	delete2DArray(resultMatrix,noOfPerson+1);

	print("resultant ids are : " ,resultIds);
}
/*
solution taken from : http://tianrunhe.wordpress.com/2012/04/01/circus-tower-sorting-problem/
*/
list<Person*> findMaxTower(list<Person*>& persons)
{
	// persons : original list 
	// list1 = sorted by height
	// list2 = sorted by weight
	// list3 = longest common subsequence of( list1 and list2) based on ids

	int noOfPerson = persons.size();

	list<Person*> heightSorted;
	heightSorted.assign(persons.begin(),persons.end());
	heightSorted.sort(heightComparator);

	print("Height Sorted Persons are : ", heightSorted);

	list<Person*> weightSorted;
	weightSorted.assign(persons.begin(),persons.end());
	weightSorted.sort(weightComparator);

	print( "WeightSorted Persons are : ", weightSorted );

	int* hids = new int[noOfPerson + 1];
	int* wids = new int[noOfPerson + 1];

	list<Person*>::iterator iter = heightSorted.begin();

	for( int i = 1 ; iter != heightSorted.end() ; i++, iter++ )
	{
		hids[i] = (*iter)->getId();
	}

	iter = weightSorted.begin();

	for( int i =1 ; iter != weightSorted.end() ; i++, iter++ )
	{
		wids[i] = (*iter)->getId();
	}

	list<int> lcsIds;

	longestCommonSubSequence(hids,wids,noOfPerson,lcsIds);

	list<int>::iterator idsIter = lcsIds.begin();

	list<Person*> resultPersons;

	for( ; idsIter != lcsIds.end() ; idsIter++ )
	{
		// find the id in the persons list
		for( iter = persons.begin(); iter != persons.end() ; iter++)
		{
			if((*iter)->getId() == (*idsIter))
			{
				resultPersons.push_back(*iter);
				break;
			}
		}
	}

	delete hids;
	delete wids;

	return resultPersons;
}

int main()
{
	int noOfPerson;
	cout << "Enter number of Persons : ";
	cin >> noOfPerson;

	list<Person*> persons;
	cout << "Enter the height of each person followed by its weight for " << noOfPerson << " persons : \n";
	for( int i = 0 ; i < noOfPerson ;i++ )
	{
		Person* p = new Person();
		cin >> (*p) ;
		persons.push_back(p);
	} 

	print("You have entered following persons : ", persons);

	list<Person*> resultPersons = findMaxTower(persons);
	resultPersons.sort(heightComparator);

	print("Longest tower can be constructed using following persons :\n", resultPersons);

	return  0; 

}