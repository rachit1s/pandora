/*
 * Queue.cpp
 *
 *  Created on: Jul 22, 2012
 *      Author: nitiraj
 */

#include "Queue.h"

template<typename T>
Queue<T>::Queue() {
	nowStack = &putStack;
}

template<typename T>
Queue<T>::~Queue() {

}

template<typename T>
T Queue<T>::get()
{
	if( nowStack == &putStack )
	{
		nowStack = &getStack;
		// transfer from put to get
		while(!putStack.isEmpty())
		{
			nowStack->push(putStack.pop());
		}
	}

	if( nowStack->isEmpty() )
		throw "Queue is empty.";
	else return nowStack->pop();
}

template<typename T>
void Queue<T>::put(const T& t)
{
	if( nowStack == &getStack)
	{
		nowStack = &putStack;
		while(!getStack.isEmpty())
		{
			nowStack->push(getStack.pop());
		}
	}

	nowStack->push(t);
}

template<typename T>
ostream& operator<<(ostream& out, const Queue<T>& queue)
{
	out << "Queue[nowStack=" << ((queue.nowStack == &(queue.getStack))? "getStack" : "putStack" ) << ","
			<< " getStack=" << queue.getStack << " putStack=" << queue.putStack ;
	return out;
}
