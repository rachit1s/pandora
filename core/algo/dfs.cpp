#include <iostream>
#include "Graph.cpp"
#include "MGraph.cpp"

using namespace std;

int main()
{
	int size = 0 ;
	cout << "Enter size of graph : " ; 
	cin >> size; 
	DFSNode* n = new DFSNode[size];

	Graph g(size);
	cout << "Enter graph in matrix form : " ;
	fillFromMatrix(g);

	cout << "Enter the start point : ";
	int startPoint;
	cin >> startPoint;
	
	print(g);

	dfs(g,n,startPoint);
	
	return 0;
}