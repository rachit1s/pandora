/*
 * TestQueue.cpp
 * Created on: Jul 22, 2012
 * Author: nitiraj
 */
#include <iostream>
#include "../objects/Queue.cpp"

using namespace std;

static void testQueue()
{
	Queue<int> q;

	const int PUT = 1;
	const int GET = 2;
	const int QUIT = 3;

	int i ;
	bool quit = false;
	do{
		try
		{
			cout << "Enter operation number :\n1. put into queue.\n2. get from queue.\n3.quit\n: ";
			cin >> i ;
			if(!cin.eof())
			{
				switch(i)
				{
					case PUT:
						cout << "Enter a value to queue : " ;
						cin >> i ;
						if(cin.eof()) break;
						q.put(i);
						cout << "Queue after put : " << q << endl;
						break;
					case GET:
						i = q.get();
						cout << "The value is : " << i << endl;
						cout << "Queue after get : " << q << endl;
						break;
					case QUIT:
						cout << "Jumped to quit." << endl;
						quit = true;
						break;
					default:
						cout <<"You entered illegal value : " << endl;
						break;
				}
			}
		}
		catch(const char* msg)
		{
			cout << "Exception occured : " << msg << endl << "Try Again.\n";
		}
	}while(!cin.eof() && !quit);
}


