/*
http://www.cplusplus.com/forum/general/21716/
   How to find all Boolean permutations of length n
   [0,0]
   [1,0]
   [0,1]
   [1,1]
   */

   #include <iostream>
   #include <cmath>
   using namespace std;


   void PrintAll(int n = 2)
   {
    if (n <= 0)
    {
        return;
    }

    //if n = 2, nTemp = 3
    //if n = 3, nTemp = 7
    //if n = 4, nTemp = 15
    //.... 

    int nTemp = (int)pow(2, n) - 1;

    for (int i = 0; i <= nTemp; i++)
    {
        cout<<"[";
        for (int k = 0; k < n; k++)
        {
            if ((i >> k) & 0x1)
            {
                cout<<"1";
            }
            else
            {
                cout<<"0";
            }

            if (k != n - 1)
            {
                cout<<",";
            }
        }

        cout<<"]"<<endl;
    }
   }

int main(int, char *[])
{
    PrintAll(4);
        return 0;
}
