#ifndef Graph_h
#define Graph_h

#include <list>
using namespace std;

class Graph
{
	private:
		int noOfVertices; // number of vertices 
		list<int>* edges; // edges represent as array of lists
	public:
		Graph(int noOfVertices);
		~Graph()
		{
			delete [] edges;
		}

		int getNoOfVertices()
		{
			return noOfVertices;
		}

		list<int>* getEdges()
		{
			return edges;
		}

		list<int>& getEdgesOf( int vertex )
		{
			if( vertex >= noOfVertices || vertex < 0)
				throw "Vertex out of bounds.";

			return edges[vertex];
		}

	void clear();
};

#endif