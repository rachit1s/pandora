/*
8 8  Write an algorithm to print all ways of arranging eight queens on a chess board so 
that none of them share the same row, column or diagonal 
*/

//////////// NOTE : This is the shittiest program I have ever written .........
#include <iostream>
using namespace std;

class ChessBoard
{
	public:
		static const int  SIZE = 8 , ROWS = 8 ,COLS = 8 ;
	private:
		static const int OCCUPIED = 1, FREE = 0, TRIED = 2, QUEEN = 3 ;
		int board[ROWS][COLS];
		int numberOfQueens;
		// int queensX[SIZE/2];
		// int queensY[SIZE/2];
	public:
		ChessBoard()
		{
			for(int i = 0 ; i < ROWS ; i++ )
				for( int j = 0 ; j < COLS ; j++ )
					board[i][j] = FREE; 

			numberOfQueens = 0 ; 
			// for( int i = 0 ; i < SIZE/2 ; i++ )
			// {
			// 	queensX[i] = -1 ; 
			// 	queensY[i] = -1 ;
			// }
		}
		ChessBoard(const ChessBoard& chessBoard)
		{
			copy(chessBoard);
		}

		void copy(const ChessBoard& chessBoard)
		{
			for(int i = 0 ; i < ROWS ; i++ )
				for( int j = 0 ; j < COLS ; j++ )
					this->board[i][j] = chessBoard.board[i][j]; 

			this->numberOfQueens = chessBoard.numberOfQueens ; 
			// for( int i = 0 ; i < SIZE/2 ; i++ )
			// {
			// 	this->queensX[i] = chessBoard.queensX[i] ; 
			// 	this->queensY[i] = chessBoard.queensY[i] ;
			// }
		}

		void operator=( const ChessBoard& chessBoard )
		{
			cout << "operator= called.";
			copy(chessBoard);
		}

		int getNumberOfQueens() const
		{
			return numberOfQueens;
		}

		bool isFree(const int& x, const int& y) const
		{
			return (board[x][y] == FREE);
		}

		void setTried(const int& x, const int& y)
		{
			board[x][y] = TRIED;
		}

		bool getNextFree(int& x, int& y) const
		{
			for( int i = 0 ; i < ROWS ; i++ )
			{
				for( int j = 0 ; j < COLS ; j++ )
					if(board[i][j] == FREE)
					{
						x = i;
						y = j;

						return true;
					}
			}

			return false;
		}

		void putQueeen(const int& x , const int& y)
		{
			// queensX[numberOfQueens] = x;
			// queensY[numberOfQueens] = y;
			board[x][y] = QUEEN;
			numberOfQueens++;
			// horizontal
			for( int i = 0 ; i < COLS ; i++ )
			{
				if( board[x][i] == FREE )
					board[x][i] = OCCUPIED ;
			}
			// vertical
			for( int i = 0 ; i < ROWS ; i++ )
			{
				if( board[i][y] == FREE )
					board[i][y] = OCCUPIED ;
			}
			// diagonal 1
			int i = x+1, j = y+1;
			while(i < ROWS && j < COLS)
			{
				if( board[i][j] == FREE )
					board[i][j] = OCCUPIED ;
				i = i+1;
				j = j+1;
			}
			// diagonal 2
			i = x+1;
			j = y-1;
			while(i < ROWS && j > 0)
			{
				if( board[i][j] == FREE )
					board[i][j] = OCCUPIED ;
				i = i+1;
				j = j-1;
			}
			// diagonal 3
			i = x-1;
			j = y-1;
			while(i > 0 && j > 0)
			{
				if( board[i][j] == FREE )
					board[i][j] = OCCUPIED ;
				i = i-1;
				j = j-1;
			}
			// diagonal 4
			i = x-1;
			j = y+1;
			while(i > 0 && j < COLS)
			{
				if( board[i][j] == FREE )
					board[i][j] = OCCUPIED ;
				i = i-1;
				j = j+1;
			}
		}

		void print()
		{
			for( int i = 0 ; i < ROWS ; i++ )
			{
				for( int j = 0 ; j < COLS ; j++ )
				{
					if( board[i][j] == QUEEN )
						cout << "\tQ";
					else
						cout << "\t_";
				}

				cout << "\n";
			}
		}

};

bool arrangeQueens(const ChessBoard& cb, ChessBoard*& result)
{
	ChessBoard current = cb;
	if( current.getNumberOfQueens() == ChessBoard::SIZE )
	{
		result = new ChessBoard(current);
		return true;
	}	

	int x, y;
	while(current.getNextFree(x,y))
	{
		current.putQueeen(x,y);
		bool found = arrangeQueens(current,result);
		if( found )
			return true;
		else
		{
			current = cb;
			current.setTried(x,y);
		}
	}
	
	return false;
}

void findOne()
{
	// this finds only one of the arrangements
	ChessBoard cb;
	ChessBoard* result;
	if( arrangeQueens(cb,result) )
	{
		cout << "Arrangement : \n";
		result->print();
		delete result;
	}
	else
		cout << "No Arrangement Found.\n";
}

int main()
{
	findOne();
}
