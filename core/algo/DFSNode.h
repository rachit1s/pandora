#ifndef DFSNode_h
#define DFSNode_h

class DFSNode
{	
	private:
		static int ID;
		int id;
		int status;
	public:
		static const int Status_UnVisited = 0 ; // initial set to unvisited.
		static const int Status_Explored = 1 ; // first a node get explored when its children are put in the stack
		static const int Status_Visited = 2; // next the node itself get visited when its poped from the stack
		DFSNode()
		{
			id = ID++;
			status = DFSNode::Status_UnVisited;
		}
		int getStatus(){
			return status;
		}
		void setStatus(int status)
		{
			this->status = status;
		}
		int getId()
		{
			return id;
		}
};

int DFSNode::ID = 0 ;

#endif