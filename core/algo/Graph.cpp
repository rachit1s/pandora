#ifndef GRAPH_CPP
#define GRAPH_CPP

#include "Graph.h"
#include "MGraph.h"
#include "Node.h"
#include "DFSNode.h"
#include <iostream>
#include <stack>
#include <list>
#include <deque>
using namespace std;



/*
removes all the edges.
*/
void Graph::clear()
{
	for( int i = 0 ; i < noOfVertices ; i++ )
	{
		edges[i].clear();
	}
}

Graph::Graph(int noOfVertices)
{
	if( noOfVertices < 1 )
		throw "Graph vertices cannot be less than 1.";

	this->noOfVertices = noOfVertices;
	this->edges = new list<int>[this->noOfVertices];
}

void fillFromMatrix(Graph &g)
{
	int value = 0 ;
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		for( int j = 0 ; j < g.getNoOfVertices(); j++ )
		{
			cin >> value ;
			
			if( value != 0 )
			{
				g.getEdgesOf(i).push_back(j);	
			}
		}
	}
}

void fill(Graph &g)
{
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		list<int>& l = g.getEdgesOf(i);
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

			l.push_back(to);
		}

		cin.clear();
	}
}

void print(Graph& g)
{
	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		list<int>& l = g.getEdgesOf(i);
		list<int>::iterator iter = l.begin();

		cout << i << " ---> " ;
		while( iter != l.end() )
		{
			cout << (*iter) << '\t' ;
			iter++;
		}

		cout << endl;
	}
}

void transpose(Graph& g, Graph& t)
{
	if( g.getNoOfVertices() != t.getNoOfVertices() )
		throw "The transpose graph does not have same number of vertices as of original graph.";

	// clear the t
	t.clear();

	for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	{
		list<int>& l = g.getEdges()[i]; // get edges of i
		// make them the edges of respective counter part 
		list<int>::iterator  iter = l.begin() ;
		while( iter != l.end() )
		{
			t.getEdges()[(*iter)].push_back(i);
			iter++;
		}
	}
}

class MGraph;

void getMGraphFromGraph( Graph& g, MGraph& m)
{
	if( g.getNoOfVertices() != m.getNoOfVertices() )
		throw "Both graphs do not have same number of vertices.";

	m.clear();

	for( int i = 0 ; i < g.getNoOfVertices() ; i++ )
	{
		list<int>& l = g.getEdges()[i];
		list<int>::iterator iter = l.begin();

		while(iter != l.end())
		{
			m.getEdges()[i][*iter] = true;
			iter++;
		}
	}
}


/*
n : is the array of Nodes which contains the actual values of the graphs.
*/
void bfs(Graph& g, Node* n, int startPoint)
{
	cout << "bfs: " ;
	deque<int> q ;
	q.push_back(startPoint);
	n[startPoint].setStatus(Node::status_visited);

	while( !q.empty() )
	{
		int curr = q.front();
		q.pop_front();
		cout << curr ;
		// find all the children of current and put in the q
		list<int> children = g.getEdgesOf(curr);
		list<int>::iterator iter = children.begin();

		for( ; iter != children.end(); iter++ )
		{
			int c = *iter;
			if( n[c].getStatus() == Node::status_unvisited )
			{
				q.push_back(c);
				n[c].setStatus(Node::status_visited);
			}
		}

		n[curr].setStatus(Node::status_explored);
	}
}

bool ctci4_2DFS(Graph& g, DFSNode* n, const int& startPoint,const int& endPoint)
{
	stack<int> s;
	s.push(startPoint);
	foundEndPoint = false;
	while(!s.empty())
	{
		int curr = s.top();
		list<int> children = g.getEdgesOf(curr);
		list<int>iterator iter = children.begin();
		bool foundUnvisited = false;
		for( ; iter != children.end() ; iter++ )
		{
			int child = *iter ;
			if( endPoint == child )
			{
				foundEndPoint = true;
				break;
			}

			if( n[child].getStatus() == DFSNode::Status_UnVisited )
			{
				foundUnvisited = true;
				n[child].setStatus(DFSNode::Status_Explored);
				s.push(child);
				break;				
			}
		}

		if( true == foundEndPoint )
		{
			break;
		}

		if( false == foundUnvisited)
		{
			// visit this node
			n[child].setStatus(DFSNode::Status_Visited);
			s.pop();
		}
	}
}
bool ctci4_2BFS(Graph& g, Node* n, int startPoint, int endPoint)
{
	deque<int> q;
	deque<int> path;

	q.push_back(startPoint);
	n[startPoint].setStatus(Node::status_visited);
	bool found = false;
	while(!q.empty())
	{
		int curr = q.front();
		q.pop_front();
		list<int> children = g.getEdgesOf(curr);
		list<int>::iterator iter = children.begin();

		for( ; iter != children.end(); iter++)
		{
			int child = *iter;
			if( n[child].getStatus() == Node::status_unvisited) 
			{
				q.push_back(child);
				n[child].setStatus(Node::status_visited);
				if( child == endPoint )
				{
					found = true;
					break;
				}
			}
		}

		if( found == true )
		{
			break;
		}

		n[curr].setStatus(Node::status_explored);
	}

	return found;
}

/*
n : is the array of Nodes which contains the actual values of the graphs.
*/
void dfs(Graph& g, DFSNode* n, int startPoint)
{
	stack<int> s;
	s.push(startPoint);

	while(!s.empty())
	{
		int curr = s.top();
		list<int> children = g.getEdgesOf(curr);

		list<int>::iterator iter = children.begin();
		bool foundUnvisited = false;
		for( ; iter != children.end() ; iter++)
		{
			int child = *iter;
			if( n[child].getStatus() == DFSNode::Status_UnVisited)
			{
				foundUnvisited = true;
				// explore the node
				s.push(child);
				n[child].setStatus(DFSNode::Status_Explored);
				cout << "Explored : " << child << endl;
				break;
			}
		}

		if( foundUnvisited == false )
		{
			// no more depth. so visit.
			cout << "Visited : " << curr << endl;
			n[curr].setStatus(DFSNode::Status_Visited);
			s.pop();
			continue;
		}
	}
}

#endif