#ifndef MGRAPH_H
#define MGRAPH_H

#include "MGraph.h"
#include "Graph.h"
#include <iostream>
#include <list>
using namespace std;


MGraph::MGraph(int noOfVertices)
		{
			if( noOfVertices < 1 )
				throw "noOfVertices of graph cannot be less than 1.";

			this->noOfVertices = noOfVertices;
			edges = new bool*[noOfVertices];
			for( int i = 0 ; i < noOfVertices ; i++ )
			{
				edges[i] = new bool[noOfVertices];
			}

			clear();
		}

MGraph::~MGraph()
{
	for( int i = 0 ; i < noOfVertices ; i++ )
	{
		delete [] edges[i];
	}

	delete [] edges;
}


bool* MGraph::getEdgesOf(int vertex)
		{
			if( vertex < 0 || vertex > noOfVertices  - 1)
				throw "Vertex index out of bounds exception." ;

			return edges[vertex];
		}
void MGraph::clear()
{
	//fill with false
	for( int i = 0 ; i < noOfVertices ; i++)
	{
		for( int j = 0 ; j < noOfVertices ; j++ )
		{
			edges[i][j] = false;
		}
	}
}

void fillFromMatrix(MGraph &g)
{
	int value = 0 ;
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		for( int j = 0 ; j < g.getNoOfVertices(); j++ )
		{
			cin >> value ;
			
			g.getEdges()[i][j] = ((value == 0) ? false :true);
			
		}
	}
}

void fill(MGraph &g)
{
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		cout << "Enter the edges from vertex " << i << endl;
		int to;
		
		while(true)
		{
			cout << i << "--->";
			cin >> to ; 
			if( cin.eof() )
				break;

			if( to < 0 || to > g.getNoOfVertices()-1)
			{
				cout << "You entered illegal index of vertex : " << to << ". Try again :" << endl;
				continue;
			}

			g.getEdges()[i][to] = true;
		}

		cin.clear();
	}
}

void print(MGraph& g)
{
	for( int k = 0 ; k < g.getNoOfVertices() ; k++ )
	{
		cout << '\t' << k ;
	}
	cout << endl;
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		cout << i ;
		for( int j = 0 ; j < g.getNoOfVertices() ; j++ )
		{
			cout << '\t' << g.getEdges()[i][j] ;
		}
		cout << endl;
	}
}

void transpose(MGraph& g, MGraph& t)
{
	if( g.getNoOfVertices() != t.getNoOfVertices() )
		throw "The transpose graph does not have same number of vertices as of original graph.";

	// clear the t
	t.clear();

	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		for( int j = 0 ; j < g.getNoOfVertices() ; j++ )
		{
			t.getEdges()[j][i] = g.getEdges()[i][j];
		}
	}
}

class Graph;

void getGraphFromMGraph(MGraph& m, Graph& g)
{
	if( g.getNoOfVertices() != m.getNoOfVertices() )
		throw "Both graphs do not have same number of vertices.";

	for( int i = 0 ; i < m.getNoOfVertices() ; i++ )
	{
		list<int>& l = g.getEdgesOf(i);
		l.clear();
		for( int j = 0 ; j < m.getNoOfVertices() ;j++)
		{
			if( m.getEdges()[i][j] )
				l.push_back(j);
		}
	}
}

#endif