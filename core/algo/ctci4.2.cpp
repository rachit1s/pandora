#include <iostream>
#include "Graph.cpp"
#include "MGraph.cpp"

using namespace std;

int main()
{
	int size = 0 ;
	cout << "Enter size of graph : " ; 
	cin >> size; 
	Node* n = new Node[size];

	Graph g(size);
	cout << "Enter graph in matrix form : " ;
	fillFromMatrix(g);

	cout << "Enter the start point : ";
	int startPoint;
	cin >> startPoint;
	cout << "Enter the end point : " ;
	int endPoint; 
	cin >> endPoint;
	print(g);

	// fill(g);

//	BSF(g,n,0);

	bool found = ctci4_2BFS(g,n,startPoint,endPoint);
	if( found == true )
	{
		cout << "path exists.";
	}
	else
	{
		cout << "path does not exists.";
	}

	return 0;
}