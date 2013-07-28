/*
 Suppose we have a set S D fa 1 ; a 2 ; : : : ; a n g
of n proposed activities that wish to use a resource, such as a lecture hall, which
can serve only one activity at a time. Each activity a i has a start time s i and a ﬁnish
time f i , where 0  s i < f i < 1. If selected, activity a i takes place during the
half-open time interval Œs i ; f i /. Activities a i and a j are compatible if the intervals
Œs i ; f i / and Œs j ; f j / do not overlap. That is, a i and a j are compatible if s i  f j
or s j  f i . In the activity-selection problem, we wish to select a maximum-size
subset of mutually compatible activities. We assume that the activities are sorted
in monotonically increasing order of ﬁnish time:
*/

#include <iostream>
#include <vector>
#include <algorithm>
#include "../../cpplib/cpputils.h"

using namespace std;

class Activity;

istream& operator>> (istream& in, Activity& a);
ostream& operator<< (ostream& out, const Activity& a);
ostream& operator<< (ostream& out, const Activity* a);

class Activity
{
	private:
	static int ID ;
	int id ;
	int startTime;
	int finishTime;
	void init()
	{
		id = ID++;
		startTime = finishTime = 0 ;
	}
	public:

	Activity()
	{
		init();
	}
	Activity(const int& startTime, const int& finishTime)
	{
		init();
		this->setStartTime(startTime);
		this->setFinishTime(finishTime);
	}
	int getStartTime() const
	{
		return startTime;
	} 
	int getFinishTime() const
	{
		return finishTime;
	}

	int getId() const
	{
		return id;
	}

	void setStartTime(const int& startTime)
	{
		this->startTime = startTime;
	}

	void setFinishTime(const int& finishTime)
	{
		this->finishTime = finishTime;
	}

	friend ostream& operator<< (ostream& out, const Activity& a);
	friend istream& operator>> (istream& in, Activity& a);
	friend ostream& operator<< (ostream& out, const Activity* a);
};
int Activity::ID = 1;
istream& operator>> (istream& in, Activity& a)
{
	int s, f;
	in >> s >> f ;
	a.setStartTime(s);
	a.setFinishTime(f);

	return in; 
}
ostream& operator<< (ostream& out, const Activity& a)
{
	out << "[" << a.getId() << "(" << a.getStartTime() << "," << a.getFinishTime() << ")" << "]";
	return out;
}

ostream& operator<< (ostream& out, const Activity* a)
{
	out << *a ;

	return out;
}

bool finishTimeComparator(const Activity* a1, const Activity* a2)
{
	if( a1->getFinishTime() <= a2->getFinishTime())
		return true;

	return false;
}

void recActivitySelection(list<Activity*>& activities,list<Activity*>::iterator currIter, list<Activity*>& result)
{
	// finish if the iterator is on the end
	if(currIter == activities.end())
	{
		cout << "recActivitySelection was called for list end hence returning..\n" ;
		return;
	}	

	cout << "recActivitySelection called for : " << *currIter << endl;
	// find the next activity with compatible time in greedy sense.
	// see if this activity can be included
	list<Activity*>::iterator lastIter = result.end();
	lastIter--;
	cout << "last activity in result is : " << *lastIter << endl;
	if( (*currIter)->getStartTime() >= (*lastIter)->getFinishTime())
	{
		cout << "Adding Activity : " << *currIter << " into the result." << endl; 
		result.push_back(*currIter);
	}	

	recActivitySelection(activities,++currIter,result);

}
void iterActivitySelection(list<Activity*>&activities,list<Activity*>&result)
{
	list<Activity*>::iterator currIter = activities.begin();
	for( ; currIter != activities.end() ; currIter++)
	{
		list<Activity*>::iterator lastResult = result.end() ;
		lastResult--;
		if( (*currIter)->getStartTime() >= (*lastResult)->getFinishTime())
			result.push_back((*currIter));
	}
}

void activitySelection( list<Activity*>& activities, list<Activity*>& result )
{
	print("Activities before sorting : \n", activities);
	// sort 
	activities.sort(finishTimeComparator);

	print("Activities after sorting : \n", activities);
	// adding an activity in the front with startTime and finishTime = 0 
	Activity* a0 = new Activity(0,0);
	result.push_front(a0);
	// recActivitySelection(activities,activities.begin(),result);
	iterActivitySelection(activities,result);
	result.pop_front();
	delete a0;
}
int main()
{
	int noOfActivities ;
	cout << "Enter number of activities : ";
	cin >> noOfActivities;

	list<Activity*> activities;
	cout << "Enter the start time followed by finish time for " << noOfActivities <<  " activities:\n";
	for( int i = 0 ; i < noOfActivities ; i++ )
	{
		Activity* a = new Activity();
		cin >> *a;
		activities.push_back(a);
	}

	list<Activity*> result;

	activitySelection(activities,result);

	print("The result is : " , result);
	// delete all elements of the list
	list<Activity*>::iterator iter = activities.begin();
	for( ; iter != activities.end() ; iter++ )
		delete (*iter);

	activities.clear();
	return 0;
}