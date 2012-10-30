#include<iostream>
#include<algorithm>
#include<stack>
using namespace std;

void printMatrix(int** matrix, int rows, int cols )
{
  for( int i = 0 ; i < rows ; i++ )
  {
    for( int j = 0 ; j < cols ; j++ ) 
    {
      cout << matrix[i][j] << "\t" ;
    }

    cout << "\n";
  }
}


  struct offset{
    int x,y;
  };

class point;
ostream& operator << (ostream& out, const point& p);


class point{
  public:
  int x, y;
  point(int x, int y)
  {
    this->x = x ;
    this->y = y ;
  }
 friend ostream&  operator << (ostream& out, const point& p);
};

ostream& operator<<(ostream& out, const point& p)
{
  out << "(" << p.x << "," << p.y << ")" ;
  return out;
}

  int UP = 0 , UP_RIGHT = 1 , RIGHT = 2 , DOWN_RIGHT = 3 , DOWN = 4 , DOWN_LEFT = 5 , LEFT = 6 , UP_LEFT = 7;

  offset steps [8];

point getNext(point curr, int d )
{
  
  point next(curr.x + steps[d].x, curr.y + steps[d].y);
  return next;
}

void ratInMaze(int ** matrix, int rows, int cols)
{
  
  steps[UP].x = 0 ; 
  steps[UP].y = 1 ;
  steps[UP_RIGHT].x = 1 ;
  steps[UP_RIGHT].y = 1 ;
  steps[RIGHT].x = 1 ;
  steps[RIGHT].y = 0;
  steps[DOWN_RIGHT].x = 1;
  steps[DOWN_RIGHT].y = -1 ;
  steps[DOWN].x = 0;
  steps[DOWN].y = -1;
  steps[DOWN_LEFT].x = -1 ;
  steps[DOWN_LEFT].y = -1;
  steps[LEFT].x = -1;
  steps[LEFT].y = 0 ;
  steps[UP_LEFT].x = -1;
  steps[UP_LEFT].y = 1 ;
  
  point final(rows-2,cols-2);

  cout << "have to reach to : " << final.x << "," << final.y << endl;

  stack<point> path;

  // check for zero on 1,1 before this
  path.push(point(1,1));

  
  while(path.empty() == false)
  {
    point now = path.top();
          matrix[now.x][now.y] = 2;
          cout << "current point : " << now << " : " <<  matrix[now.x][now.y]  << endl; 
        if( now.x == final.x && now.y == final.y ) 
        {
          break;
        }
    bool foundAWay = false;
    for( int i = UP ; i <= UP_LEFT ; i++ ) // a little inefficient as it tries for all directions everytime.
    {
        point next = getNext( now,i ) ;
        cout << " checkout out zero at : " << next << endl;
        if( 0 == matrix[next.x][next.y] )
        {
          foundAWay = true;
          cout << "found the zero at : " << next << endl;
          path.push(next);
          break;
        }
    }

    if( !foundAWay )
    {
      path.pop(); // go back to the last point.
    }
  }

  if( path.empty() == true ) 
  {
    cout << "sorry no path." << endl;
  }
  else
  {
    cout << "The path is  : " ;
    while( path.empty() == false )
    {
      point p = path.top();
      cout << "\t(" << p.x << "," << p.y << ")" ; 
      path.pop();
    }
  }
}

void inputMatrix(int**& matrix, int& rows, int& cols)
{
  int n,m;

  cout << "Enter rows : ";
  cin >> n ;
  cout << "Enter cols : ";
  cin >> m ;

  rows = n + 2 ;
  cols = m + 2 ;

  matrix = new int*[rows];
  for( int i = 0 ; i < rows ; i++ )
  {
     matrix[i] = new int[cols];
     fill_n(matrix[i],cols,1);
  }


  for( int i = 1 ; i < rows - 1; i++ )
  {
    for( int j = 1 ; j < cols - 1; j++ )
    {
      cout << "Enter matrix[" << i << "][" << j << "] = ";
      cin >> matrix[i][j] ;
    }
  }

}

void deleteMatrix(int**& matrix, const int & rows, const int& cols)
{
  for( int i = 0 ; i < rows ; i++ )
  {
    delete [] matrix[i];
  }

  delete [] matrix;
}

void createMatrix5By4(int**& matrix)
{
  int rows =7;
  int cols =6;

  matrix = new int*[rows];
  for( int i = 0 ; i < rows ; i++ )
  {
     matrix[i] = new int[cols];
     fill_n(matrix[i],cols,1);
  }

  matrix[1][1] = 0;
  matrix[1][2] = 0;
  matrix[1][3] = 1;
  matrix[1][4] = 0;
  matrix[2][1] = 1;
  matrix[2][2] = 0;
     matrix[2][3] = 0;
     matrix[2][4] = 1;
     matrix[3][1] = 1;
     matrix[3][2] = 1;
     matrix[3][3] = 1;
     matrix[3][4] = 1;
     matrix[4][1] = 0;
     matrix[4][2] = 1;
     matrix[4][3] = 0;
     matrix[4][4] = 1;
     matrix[5][1] = 1;
     matrix[5][2] = 2;
     matrix[5][3] = 1;
     matrix[5][4] = 0;
}
int main()
{ 
  int rows = 7;
  int cols = 6;

  int** matrix;

  createMatrix5By4(matrix);

//  inputMatrix(matrix,rows,cols);

  printMatrix(matrix,rows,cols);

  ratInMaze(matrix,rows,cols);

  deleteMatrix(matrix,rows,cols);

  return 0;
}
