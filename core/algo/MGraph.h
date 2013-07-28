#ifndef MGraph_h
#define MGraph_h
/*
The matrix based graph representation
*/

class MGraph
{
	private:
	bool** edges;
	int noOfVertices ;

	public:
		MGraph(int noOfVertices);
		
		~MGraph();

		int getNoOfVertices()
		{
			return noOfVertices;
		}

		bool** getEdges()
		{
			return edges;
		}

		
		bool* getEdgesOf(int vertex);

		void clear();

};

#endif