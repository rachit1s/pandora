/*
   • Given an 4n X 4n Matrix, where n is a positive integer taken as input. Imagine the matrix consisting of two interleaved coils whose centers are at the centre of the matrix. Implement a java program which takes an integer (n) as input and prints the two coils in two seperate lines.

   Please have a look at the below examples to get a sense of what the two coils are :
   • Example 1:
   • Input: 1
   • Matrix:
   01 02 03 04
   05 06 07 08
   09 10 11 12
   13 14 15 16

   • Output the Two Coils as:
   - Coil1: 10 06 02 03 04 08 12 16
   - Coil2: 07 11 15 14 13 09 05 01

   • Example 2:
   • Input: 2
   • Matrix:
   01 02 03 04 05 06 07 08
   09 10 11 12 13 14 15 16
   17 18 19 20 21 22 23 24
   25 26 27 28 29 30 31 32
   33 34 35 36 37 38 39 40
   41 42 43 44 45 46 47 48
   49 50 51 52 53 54 55 56
   57 58 59 60 61 62 63 64

   • Output the Two Coils as:
   - Coil1: 36 28 20 21 22 30 38 46 54 63 52 51 50 42 34 26 18 10 02 03 04 05 06 07 08 16 24 32 40 48 56 64
   - Coil2: 29 37 45 44 43 35 27 19 11 12 13 14 15 23 31 39 47 55 63 62 61 60 59 58 57 49 41 33 25 17 09 01
*/
#include <iostream>
using namespace std;

int ** getMatrix( int SIZE )
{
   int ** matrix = new int*[SIZE];
   for( int i = 0 ; i < SIZE ; i++)
   {
      matrix[i] = new int[SIZE];
   }
   return matrix;
}
void inputMatrix( int** matrix, int SIZE)
{
   for( int i = 0 ; i < SIZE ; i++ )
   {
      for(int j = 0 ; j < SIZE ; j++ )
      {
         cout << "matrix[" << i << "," << j << "]=";
         cin >> matrix[i][j];
      }
   }
}

void printMatrix(int** matrix , int SIZE)
{
   for( int i = 0 ; i< SIZE ;i++)
   {
      for( int j = 0 ;j < SIZE ;j++)
         cout << matrix[i][j] << '\t' ;

      cout << '\n' ;
   }
}

void printCoil(int **matrix,int SIZE, int startX, int startY, int x[4], int y[4])
{
   int i = startX, j = startY, count = SIZE-1, lastCount = SIZE-1 ;
   int turn = 1;
   int currX = 0, currY = 0 ;
   
   int totalCounts = (SIZE*SIZE)/2;
   while( totalCounts-- )
   {
      cout << matrix[i][j] << "," ;
      //cout << "(" << i << "," << j << ")," ;
   
      i = i + x[currX]; // set to next pointers
      j = j + y[currY];
   
      cout << "count =" << count << "  turn = " << turn << endl;

      count--;
      if( 0 == count )
      {
         turn++;
         if(turn == 2 )
         {
            count = lastCount - 1;
            lastCount = lastCount - 1;
         }
         else 
         if( turn % 2 == 0)
         {

            count = lastCount - 2 ;
            lastCount = count;            
         }
         else
         {
            count = lastCount ;
         }
         

      //   cout << "next count = " << count << "\t lastCount = " << lastCount << endl;

         currX = (currX + 1)%4;
         currY = (currY + 1)%4;

       //  cout << "next currX = " << currX << "\t next currY = " << currY << endl;
      }
   }

}



int main()
{
   int n ;
   cout << "Input the N for 4N x 4N matrix : " ;
   cin >> n ;
   int SIZE = 4*n;

   int ** matrix = getMatrix(SIZE);
   inputMatrix(matrix,SIZE);
   cout << "\n";
   printMatrix(matrix,SIZE);

   int x[] = {1,0,-1,0};
   int y[] = {0,1,0,-1};

   cout << "Coil 1 : ";
   printCoil(matrix,SIZE,0,0,x,y);

   int x1[] = {-1,0,1,0};
   int y1[] = {0,-1,0,1};

   cout <<"\nCoil 2 : " ;
   printCoil(matrix,SIZE,SIZE-1,SIZE-1,x1,y1);

   return 0;
}
