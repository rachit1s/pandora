/*
infix to postfix
*/

#include <iostream>
#include <stack>
#include <sstream>

using namespace std;

int getPriority(char c)
{
	switch(c)
	{
		case '`':
		case '!':
			return 7;
		case '*':
		case '/':
		case '%':
			return 6;
		case '+':
		case '-':
			return 5;
		case '<':
		case '>':
		// case '<=': // two character operators not supported
		// case '>=':
			return 4;
		// case '==':	// two character operators not supported	
		// case '!=':
		// 	return 3;
		// case '&&':
		// 	return 2;
		// case '||':
		// 	return 1;
	}
}

bool isOperand(char c)
{
	if( c == '+' 
		|| c == '-' || c == '*' || c == '/' || c == '%' 
		|| c == '<' || c == '>' 
		// || c == '!'  // not handling operator which acts on single operand
		// || c == '>=' || c == '&&' || c == '||'  // 2 character operator not supported
		// || c == '>=' || c == '==' || c == '!=' 
		// || c == '`' // ` is used as a unary minus for clear distinguishing
		// c == '(' || c == ')' // should we treat brackets as operators ?
		)
		return false;

	return true;
}

string getPostfix(const string& infix)
{
	stringstream ss;
	stack<char> s;
	// char* curr = infix;
	int index = 0 ;
	while(index != infix.length() )
	{
		char curr = infix[index];
		// cout << "curr = " << curr << endl;

		if( curr == '(')
		{
			// cout << "curr == ( so pushing.\n" ;
			s.push(curr);
		}
		else if( curr == ')')
		{
			// cout << "curr == ) so poping ..\n";
			char op ;
			while( !s.empty() && ( (op = s.top() ) != '('))
			{
				// cout << "popped : " << op << endl;
				ss << op; 
				s.pop();
			}

			if( op != '(')
			{
				// cout << "last op was not ( so throwing exception..\n";
				throw "Illegal Format.";
			}	
			else
			{
				// cout << "poped the (\n" ;
				s.pop();
			}
		}
		else if( isOperand(curr) )
		{
			// cout << "curr is operand ..\n";
			ss << (curr) ;
		}
		else
		{		
			// cout << "curr is operator ...\n";	
			while( !s.empty() )
			{
				char lastOp = s.top();
				// cout << "lastOp = " << lastOp << endl;
				if( lastOp == '(' )
				{
					// cout << "as lastOp == ( so breaking..\n";
					break;
				}
				else
				{
					if( getPriority(lastOp) >= getPriority(curr) )
					{
						// cout << "as getPriority(lastOp) >= getPriority(curr) so poping lastOp\n";
						ss << lastOp ;
						s.pop();
					}
					else
					{
						// cout << "as getPriority(lastOp) < getPriority(curr) so breaking..\n";
						break;
					}
				}
			}

			// cout << "pushing current operator into the stack..\n";
			s.push(curr);
		}

		index++;
	}

	// cout << "input finished... now flusing operators in stack..\n";
	while(!s.empty())
	{
		char currOp = s.top();
		// cout << "currOp = " << currOp << endl;
		ss << currOp ;
		s.pop();
	}

	return ss.str();
}

int main1()
{
	// string infix = "(A+B)*D+E/(F+A*D)+C";
	string infix = "A+B*D+E/F+A*D+C";
	string postfix = getPostfix(infix);

	cout << "post fix : " << postfix << endl;
}