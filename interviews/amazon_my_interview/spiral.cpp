/*
spiral 
*/
#include <iostream>
#include <queue>
#include <stack>

using namespace std;
class TreeNode{
			public:
           int val;
           TreeNode *left, *right;
           TreeNode()
           {
           	left = right = NULL;
           }
       };

 class      DLLNode {
 public:
           int val;
           DLLNode *prev, *next;
           DLLNode()
           {
           	prev = next = NULL;
           }
       };
DLLNode* getSpiralDLLFromBST(TreeNode *root){
    /* For your reference:
       TreeNode{
           int val;
           TreeNode *left, *right;
       };

       DLLNode {
           int val;
           DLLNode *prev, *next;
       };
    */

    DLLNode* head = NULL;
    DLLNode* last = NULL;

    TreeNode* delimiterNode = new TreeNode();
    queue<TreeNode*> q;
    q.push(root);
    q.push(delimiterNode);

    stack<TreeNode*> s;

    bool insertLeft = true;
    bool wasLastDelimiter = false;
    while( !q.empty() )
    {

    	TreeNode* tn = q.front();
    	cout << "curr value = " << tn->val << endl;
    	q.pop();
    	if(tn == delimiterNode)    		
    	{
    		if( wasLastDelimiter )
    			break;
    		
    		wasLastDelimiter = true;
    		while(!s.empty())
    		{
    			TreeNode* tns = s.top();
    			s.pop();
				if( insertLeft )
		    	{
		    		if( NULL != tns->left )
		    		{
		    			q.push(tns->left);
		    		}
		    		if(NULL != tns->right)
		    		{
		    			q.push(tns->right);
		    		}
		    	}
		    	else
		    	{
		    		if(NULL != tns->right)
		    		{
		    			q.push(tns->right);
		    		}
		    		if( NULL != tns->left )
		    		{
		    			q.push(tns->left);
		    		}
		    	}
			}

			insertLeft = !(insertLeft);

    		q.push(delimiterNode); // finished all children
    		continue;
    	}
    	else
    	{
    		wasLastDelimiter = false;
    	}

    	DLLNode* curr = new DLLNode();
    	curr->prev = curr->next = NULL;

    	curr->val = tn->val;
    	curr->prev = last;

    	if( last != NULL )
    	{
    		last->next = curr;
    	}
    	last = curr;

    	if(head == NULL)
    	{
    		head = curr;
    	}
    	
    	s.push(tn);
    }

    delete delimiterNode;
    return head;
}

int main()
{
	TreeNode* tn1 = new TreeNode();
	tn1->val = 5;

	TreeNode* tn2 = new TreeNode();
	tn2->val = 2;

	TreeNode* tn3 = new TreeNode();
	tn3->val = 8;

	TreeNode* tn4 = new TreeNode();
	tn4->val = -4;

	TreeNode* tn5 = new TreeNode();
	tn5->val = 4;

	TreeNode* tn6 = new TreeNode();
	tn6->val = 6;

	TreeNode* tn7 = new TreeNode();
	tn7->val = 9;

	tn1->left = tn2;
	tn1->right = tn3;
	tn2->left= tn4;
	tn2->right = tn5;
	tn3->left = tn6;
	tn3->right = tn7;

	DLLNode* head = getSpiralDLLFromBST(tn1);

	while( head != NULL)
	{
		cout << (head->val) << endl;
		head = head->next;
	}

	return 0;

}