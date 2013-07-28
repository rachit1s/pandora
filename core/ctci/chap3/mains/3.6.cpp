/*
 * 3.6.cpp
 *
 *  Created on: Jul 23, 2012
 *      Author: nitiraj
 */
#include <iostream>
#include "../objects/Stack.h"
using namespace std;

/*
 * Write a program to sort a stack in ascending order. You should not make any assumptions about how the stack is implemented. The following are the only functions that should be used to write this program: push | pop | peek | isEmpty.
 */
void static sortStack(Stack<int>& stack)
{
	Stack<int> temp ;
	// pop from stack till you get smaller then current an put them into temp or empty
	// pop through temp push them in stack placing the current value in correct position.
	// do above process till you get empty in stack.
	int *last = NULL;
	int lastValue = 0 ;
	while(!stack.isEmpty())
	{
		if( NULL == last || *last <= stack.top())
		{
			lastValue = stack.pop();
			last = &lastValue;
			temp.push(lastValue);
		}
		else
		{
			lastValue = stack.pop();
			last = &lastValue;
			while(!temp.isEmpty())
			{
				int t = temp.pop();
				if( NULL != last && *last >= t )
				{
					stack.push(*last);
					stack.push(t);
					last = NULL;
				}
				else
				{
					stack.push(t);
				}
			}
			if( NULL != last )
			{
				stack.push(*last);
				last = NULL;
			}
		}
	}

	// now temp contains sorted descending elements
	while(!temp.isEmpty())
	{
		stack.push(temp.pop());
	}
}

void static simpleSortStack(Stack<int>& stack)
{
	Stack<int> temp ;
	// pop from stack till you get smaller then current an put them into temp or empty
	// pop through temp push them in stack placing the current value in correct position.
	// do above process till you get empty in stack.
	while(!stack.isEmpty())
	{
		if( temp.isEmpty() == true || temp.top() <= stack.top())
		{
			temp.push( stack.pop() );
		}
		else
		{
			int hold = stack.pop();
			bool found = false;
			while(!temp.isEmpty())
			{
				if(found == false && temp.top() <= hold )
				{
					stack.push(hold);
					stack.push(temp.pop());
					found = true;
				}
				else
				{
					stack.push(temp.pop());
				}
			}
			if( found == false )
			{
				stack.push(hold);
			}
		}
	}

	// now temp contains sorted descending elements
	while(!temp.isEmpty())
	{
		stack.push(temp.pop());
	}
}

void static sortStack()
{
	Stack<int> stack;
	int i = 0 ;
	cout << "Enter the values in stack." << endl;
	do
	{
		cout << "Enter next value (-1 to end) : " ;
		cin >> i;
		if( i != -1 )
		{
			stack.push(i);
		}
	}while(i != -1);

	cout << "Stack before sorting : " << stack << endl;
	simpleSortStack(stack);
	cout <<"Stack after sort : " << stack << endl;
}
