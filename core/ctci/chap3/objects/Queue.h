/*
 * Queue.h
 *
 *  Created on: Jul 22, 2012
 *      Author: nitiraj
 */

#ifndef QUEUE_H_
#define QUEUE_H_
#include "Stack.h"

template<typename T>
class Queue;

template<typename T>
ostream& operator<<(ostream& out, const Queue<T>& queue);

template<typename T>
class Queue {
	Stack<T> getStack;
	Stack<T> putStack;
	Stack<T>* nowStack;
public:
	Queue();
	virtual ~Queue();
	T get();
	void put(const T& t);
	friend ostream& operator<< <>(ostream& out, const Queue<T>& queue);
};

#endif /* QUEUE_H_ */
