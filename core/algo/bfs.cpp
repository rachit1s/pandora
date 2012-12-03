#include <iostream>
#include "Graph.cpp"
#include "MGraph.cpp"

using namespace std;

int main()
{
	int size = 0 ;
	cin >> size; 
	Node* n = new Node[size];

	Graph g(size);

	fillFromMatrix(g);

	print(g);

	// fill(g);

	bfs(g,n,0);

	return 0;
}