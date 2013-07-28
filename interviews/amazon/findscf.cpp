 /*All test cases have passed.*/

#include<iostream>
#include<algorithm>
using namespace std;
typedef unsigned long long int big;

big findSCF(big a, big b) {
  if(a%2==0 && b%2==0) return 2;
  for(big i = 3; i <= b; i+=2)
      if(a%i==0 && b%i==0) return i;
  return 1;
}

int main()
{

int k,t;
cin >> t;
while(t--)
{
cin >> k;
big f = 2, prev = 1, temp, d=1;
while(f<=k) {
  d=findSCF(k,f);
  if(d>1) break;
  temp=prev;
  prev=f;
  f+=temp;
}
if(d > 1)
cout << f << " " << d << endl;
else {
while(f%k!=0) {
   temp=prev;
   prev=f;
   f+=temp;
}
cout << f << " " << k << endl;
}
}
}

/* 
Since k can be 10^6, f can be as large as 10^18, so i have used unsigned long long int as variable type of 'f' 
*/
