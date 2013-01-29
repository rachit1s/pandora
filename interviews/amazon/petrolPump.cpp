/*
http://discuss.joelonsoftware.com/default.asp?interview.11.623860.10
1. There are n petrol bunks arranged in circle. Each bunk is separated from the rest by a certain distance. You choose some mode of travel which needs 1litre of petrol to cover 1km distance. You can't infinitely draw any amount of petrol from each bunk as each bunk has some limited petrol only. But you know that the sum of litres of petrol in all the bunks is equal to the distance to be covered.
ie let P1, P2, ... Pn be n bunks arranged circularly. d1 is distance between p1 and p2, d2 is distance between p2 and p3. dn is distance between pn and p1.Now find out the bunk from where the travel can be started such that your mode of travel never runs out of fuel.
*/
#include <iostream>
#include "../../core/cpplib/cpputils.h"

using namespace std;

list<int> findStartPoints(int* petrols , int* distances, int noOfPumps)
{
	print("petrols are : \t", petrols, noOfPumps);
	print("distances are : \t",distances,noOfPumps);

	list<int> validStartPoints;
	// find the complete paths validity 
	// i.e. before reaching any station j the sum of petrols till that point should be more than or equal to the distance covered till that
	// let x be the start point p[x] + p[x+1] ... + p[j-1] >= d[x] + d[x+1] + ..... d[j-1] 
	// where  x < j <= (x-1) such that if x > noOfPumps then x resets to 1

	for( int pumpId = 0 ; pumpId < noOfPumps ; pumpId++ )
	{
		cout << "checking for start point : " << pumpId << endl;
		int pv = 0 ;
		int distance = 0;
		int i;
		cout << "The last pump would be : " << (pumpId + noOfPumps - 1) % noOfPumps << endl ;
		for( i = pumpId ; i != (pumpId + noOfPumps - 1) % noOfPumps ; i = (i + 1) % noOfPumps )
		{
			pv += petrols[i];
			distance += distances[i];
			cout << "from " << i << " to " <<  (i + 1) % noOfPumps << " petrol = " << pv << " and distance = " << distance << endl;
			if( pv < distance )
				break;
		}

		if( i == ( (pumpId + noOfPumps - 1) % noOfPumps ) )
		{
			cout << "So this is a valid start point.\n" ;
			validStartPoints.push_back(pumpId);
		}	
		else
			cout << "This is not a valid start point \n";
	}

	return validStartPoints;
}

int main()
{
	int noOfPumps;
	cout << "Enter number of pumps : ";
	cin >> noOfPumps;

	cout << "Enter amount of petrol at each pump followed by its distance to the next pump for each pump :\n";

	int* petrols = new int[noOfPumps];
	// inputAndCreateArray(petrols,noOfPumps);

	int* distances = new int[noOfPumps];
	// inputAndCreateArray(distances,noOfPumps);

	for( int i= 0 ; i < noOfPumps ; i++ )
	{
		cin >> petrols[i];
		cin >> distances[i];
	}

	list<int> validStartPoints = findStartPoints(petrols,distances,noOfPumps);

	print("Valid start points are : ", validStartPoints );

	delete petrols;
	delete distances;

	return 0;

}