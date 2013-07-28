/*
 * SetOfStacks.cpp
 *
 *  Created on: Jul 21, 2012
 *      Author: nitiraj
 */

#include "SetOfStacks.h"
#include <iterator>
using namespace std;

template<typename T>
SetOfStacks<T>::SetOfStacks(unsigned int sizeOfEachStack) {
	this->sizeOfEachStack = sizeOfEachStack;
}

template<typename T>
SetOfStacks<T>::~SetOfStacks() {
	if(!isEmpty())
	{
		typedef typename vector< ArrayStack<T>* >::iterator VectorIterator;
		VectorIterator iter = stackOfStacks.begin();
		while(iter != stackOfStacks.end() )
		{
			ArrayStack<T> * ast = *(iter);
			cout << "AST to be deleted : " << *ast << endl;
			stackOfStacks.erase(iter);
			delete ast;
//			iter++;
		}
	}
}

template<typename T>
bool SetOfStacks<T>::isEmpty() {
	return stackOfStacks.empty();
}

template<typename T>
void SetOfStacks<T>::push(T& t) {
	if (isEmpty() || (stackOfStacks.back())->isFull()) {
		// put a new ArrayStack on the back
		ArrayStack<T>* ast = new ArrayStack<T>(sizeOfEachStack);
		ast->push(t);
		stackOfStacks.push_back(ast);
	} else {
		ArrayStack<T>*& ast = stackOfStacks.back();
		ast->push(t);
	}
}

template<typename T>
T& SetOfStacks<T>::top() {
	if (isEmpty())
		throw "Stack is empty.";

	return stackOfStacks.back()->top();
}

template<typename T>
T SetOfStacks<T>::pop() {
	if (isEmpty())
		throw "Stack is empty.";

	ArrayStack<T>* ast = stackOfStacks.back();
	T t = ast->pop();
	if (ast->isEmpty())
	{
		stackOfStacks.pop_back();
		delete ast;
	}

	return t;
}

template<typename T>
T SetOfStacks<T>::popAt(size_t stackNumber)
{
	if(stackNumber > stackOfStacks.size()-1)
		throw "stackNumber Out Of Bounds.";

	typedef typename vector< ArrayStack<T>* >::iterator VectorIterator;
	VectorIterator iter;
	for( iter = stackOfStacks.begin(); stackNumber > 0 && iter != stackOfStacks.end() ; iter++, stackNumber--);

	ArrayStack<T> * ast = *(iter);
	T t = ast->pop();
	if( ast->isEmpty() )
	{
		stackOfStacks.erase(iter);
		delete ast;
	}
	else
	{
		// we can shift data from next stacks recursively and see if last stack becomes empty and delete it
		// but not doing that
	}

	return t;
}

template<typename T>
ostream& operator<<(ostream& out, const SetOfStacks<T>& sos) {
	out << "SOS[";
	typedef typename vector<ArrayStack<T>*>::const_iterator VectorPointer;

	for ( VectorPointer ii = sos.stackOfStacks.begin() ; ii != sos.stackOfStacks.end(); ii++) {
		out << *(*ii) << ",";
	}

	out << "]";

	return out;
}

