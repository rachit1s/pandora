// make groups of half
#include <iostream>
#include <list>
using namespace std;

list< list<int> > first;
list< list<int> > second;

void divide( int* array, int currentIndex, int* firstHalf, int currentFirstIndex, int firstLength, int* secondHalf, int currentSecondIndex, int secondLength)
{
	if( currentFirstIndex >= firstLength && currentSecondIndex >= secondLength )
	{
		// put both the combination in the respective lists
		list<int> tempFirst;
		list<int> tempSecond;
		for( int i = 0 ; i < firstLength ; i++ )
			tempFirst.push_back(firstHalf[i]);
		for( int i = 0 ; i < secondLength ; i++ )
			tempSecond.push_back(secondHalf[i]);

		first.push_back(tempFirst);
		first.push_back(tempSecond);
	}

	if( currentFirstIndex < firstLength )
	{
		firstHalf[currentFirstIndex] = array[currentIndex];	
	}
}
void divideArrayIntoHalf(int* array, int length, int*& firstHalf, int*& secondHalf)
{
	int firstLen = length/2;
	int secondLen = length - firstLen ;

	firstHalf = new int[firstLen];
	secondHalf = new int[secondLen];

	divide(array,0,firstHalf,0, firstLen,secondHalf,0, secondLen);
}
int main()
{

}