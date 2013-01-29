/*
postfix to tree to infix
*/

#include <iostream>
#include <string>
#include "infix2postfix.cpp"

using namespace std;

class Tree
{
	public:
	Tree(char data)
	{
		this->data = data;
		left = NULL;
		right = NULL;
	}
	Tree(Tree* left, char data, Tree* right)
	{
		this->data = data;
		this->left = left;
		this->right = right;
	}
	char data;
	Tree* left;
	Tree* right;
};

Tree* postfix2Tree(string postfix)
{
	stack<Tree*> s;
	int index = 0 ; 
	while( index < postfix.length() )
	{
		char curr = postfix[index];
		// cout << "considering char : " << curr << endl;
		if( isOperand(curr) )
		{
			Tree* t = new Tree(curr);
			s.push(t);
		}
		else
		{
			// assuming all operators acts on 2 operands
			if( s.empty() )
			{
				throw "Illegal Format.";
			}
			else
			{				
				Tree* secondOperand = s.top();
				s.pop();
				if( s.empty() )
				{
					throw "Illegal Format." ;
				}
				else
				{
					Tree* firstOperand = s.top();
					s.pop();
					Tree* t = new Tree(firstOperand,curr,secondOperand);
					s.push(t);
				}
			}
		}

		index++;
	}

	Tree* root = s.top();
	s.pop();
	if( s.empty() == false )
	{
		// TODO: delete all tree elements.
		throw "Illegal Format.";
	}	
	else
	{
		return root;
	}
}

void postorder(Tree* tree, stringstream& ss)
{
	if( NULL != tree )
	{
		if( NULL != tree->left )
		{
			if( !isOperand(tree->left->data) 
				&& !isOperand(tree->data)
				&& (getPriority(tree->data) > getPriority(tree->left->data))
				 )
				ss << '(';
			
			postorder(tree->left,ss);

			// if( !isOperand(tree->left->data) )
			if( !isOperand(tree->left->data) 
				&& !isOperand(tree->data)
				&& (getPriority(tree->data) > getPriority(tree->left->data))
				 )
				ss << ')';
		}

		ss << tree->data;
		
		if( NULL != tree->right)
		{
			// if( !isOperand(tree->right->data) )
			if( !isOperand(tree->right->data) 
				&& !isOperand(tree->data)
				&& (getPriority(tree->data) > getPriority(tree->right->data))
				 )
				ss << '(';

			postorder(tree->right,ss);

			// if( !isOperand(tree->right->data) )
			if( !isOperand(tree->right->data) 
				&& !isOperand(tree->data)
				&& (getPriority(tree->data) > getPriority(tree->right->data))
				 )
				ss << ')';
		}
	}
}
string tree2Infix(Tree* tree)
{
	stringstream ss;
	postorder(tree,ss);
	return ss.str();
}

int main()
{
	string infix = "(((A+B))*D)+(E/F)+(A*D+C)";
	string postfix = getPostfix(infix);

	cout << "initial infix : " << infix << endl;
	cout << "postfix : " << postfix << endl;

	Tree* tree = postfix2Tree(postfix);
	string infix2 = tree2Infix(tree);

	cout << "final infix : " << infix2 << endl;

	return 0 ;
}
