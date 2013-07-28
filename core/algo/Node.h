#ifndef Node_h
#define Node_h

class Node
{
	private: 
		static int ID ;
		int id;
		int status;
	public:
		static  const int status_unvisited = 1;
		static const int status_visited = 2;
		static const int status_explored = 3;
		Node()
		{
			id = ID++;
			status = status_unvisited;
		}

		int getStatus()
		{
			return status;
		}

		void setStatus(int status)
		{
			this->status = status;
		}
};

int Node::ID = 0 ;

#endif