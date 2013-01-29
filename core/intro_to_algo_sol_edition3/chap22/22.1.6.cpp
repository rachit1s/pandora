/*
22.1-6
Most graph algorithms that take an adjacency-matrix representation as input re-
quire time .V 2 /, but there are some exceptions. Show how to determine whether
a directed graph G contains a universal sink—a vertex with in-degree jV j  1 and
out-degree 0—in time O.V /, given an adjacency matrix for G.
*/
#include <iostream>
#include "../../algo/MGraph.cpp"

int findUniversalSink(MGraph& g)
{
	int j = 0,i = 1;
	int noOf1s = 0 ;
	int count = 0 ;
	while( (noOf1s != g.getNoOfVertices() - 1) && j < g.getNoOfVertices())
	{
		cout << "i = " << i << ", j = " << j << ", noOf1s = " << noOf1s << endl;
		count++ ;
		if( g.getEdgesOf(i)[j] == 1 )
		{
			noOf1s++;
		}
		else
		{
			if( i > j )
				j = i ;
			else 
				j = j + 1;
			noOf1s = 0 ;
		}

		i = (i+1)% g.getNoOfVertices();
	}

	cout << "number of times the loop executed : " << count << endl;

	if( noOf1s == g.getNoOfVertices() -1) 
	{
		count++ ;
		// perform the final check
		for( int j = 0 ; j < g.getNoOfVertices() ; j++ )
		{
			if( g.getEdgesOf(i)[j] != 0 )
				return -1 ;
		}
		return i;
	}
		
	else
		return -1;
}

int findUniversalSink2(MGraph& g)
{
	//pseudo code
//M -> adjacency matrix
int a=0;
for(int i=1;i<g.getNoOfVertices();++i)
{
  if(g.getEdgesOf(a)[i]) a=i;
}

//check that a is sink by reading out 2V entries from the matrix
for( int j = 0 ; j < g.getNoOfVertices() ;j++)
	if(g.getEdgesOf(a)[j] != 0 )
		return -1;

for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	if( i != a && g.getEdgesOf(i)[a] != 1)
		return -1;

return a; //if a is a sink, otherwise -1

}

int findUniversalSink3(MGraph& g)
{
	//pseudo code
//M -> adjacency matrix
int a=0;
for(int i=1;i<g.getNoOfVertices();++i)
{
	cout << "i = " << i << ", a = " << a << endl;
  if(!g.getEdgesOf(i)[a])
  {
   a=i;
   cout << "setting a = " << a << endl;
  }
}

//check that a is sink by reading out 2V entries from the matrix
for( int j = 0 ; j < g.getNoOfVertices() ;j++)
	if(g.getEdgesOf(a)[j] != 0 )
		return -1;

for( int i = 0 ; i < g.getNoOfVertices() ; i++)
	if( i != a && g.getEdgesOf(i)[a] != 1)
		return -1;

return a; //if a is a sink, otherwise -1

}

int main()
{
	int noOfVertices ;
	cout << "Enter number of vertices : ";
	cin >> noOfVertices;

	MGraph g(noOfVertices);
	fillFromMatrix(g);

	cout << "Your Graph is : \n";
	print(g);

	// int sink = findUniversalSink(g);

	// int sink = findUniversalSink2(g);
	int sink = findUniversalSink3(g);

	cout << "universal sink is : " << sink << endl;

	return 0;

}
