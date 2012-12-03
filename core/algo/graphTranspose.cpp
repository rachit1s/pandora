/*
The transpose of a directed graph G = (V, E) is the graph G T  = (V, E T ), where E T  = {(v, u)  
V × V : (u, v)  E}. Thus, G T  is G with all its edges reversed. Describe efficient algorithms for 
computing G T  from G, for both the adjacency-list and adjacency-matrix representations of G. 
Analyze the running times of your algorithms. 
*/
#include "Graph.cpp"
#include "MGraph.cpp"
using namespace std;

int main()
{
	cout << "Enter the number of vertices of the graph : " ;
	int size;
	cin >> size;
	Graph g(size);

	fill(g);

	cout << "Printing the graph:\n";
	print(g);

	Graph t(g.getNoOfVertices());

	transpose(g,t);

	cout << "Printing the transpose:\n";
	print(t);

	cout << "Working with MGraph now.\n" ;

	MGraph m(size);

	getMGraphFromGraph(g,m);

	cout << "Printing the m Graph:\n";
	print(m);

	MGraph tm(m.getNoOfVertices());

	transpose(m,tm);

	cout << "Printing the transpose:\n";
	print(tm);

	return 0;
}