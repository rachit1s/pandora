/*
 * SetOfStacks.h
 *
 *  Created on: Jul 21, 2012
 *      Author: nitiraj
 */

#ifndef SETOFSTACKS_H_
#define SETOFSTACKS_H_
#include <vector>
#include "ArrayStack.h"

using namespace std;


template<typename T>
class SetOfStacks;

template<typename T>
ostream& operator<< (ostream& out, const SetOfStacks<T>& sos);

template <typename T>
class SetOfStacks {
private :
	unsigned int sizeOfEachStack;
	vector< ArrayStack<T>* > stackOfStacks;
public:
	SetOfStacks(unsigned int sizeOfEachStack);
	virtual ~SetOfStacks();
	T pop();
	void push(T& t);
	T& top();
	bool isEmpty();
	T popAt(size_t stackNumber);
	friend ostream& operator<< <>(ostream& out, const SetOfStacks<T>& sos);
};

#endif /* SETOFSTACKS_H_ */
