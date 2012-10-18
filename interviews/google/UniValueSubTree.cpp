/*
   google-interview-questions : http://www.careercup.com/question?id=14754709 

   Write a program to count the number of uni-value sub-trees in a given tree. Explain reasoning, and implementation decisions.
   */

#include<iostream>
#include<stack>
using namespace std;

class node
{
  public :
    static int id_seq;
    node()
    {
        id = id_seq++;
    }
    int id;
    node* right;
    node* left;
    char data;
};

int node::id_seq = 0 ;

int UniValueSubTrees(node *root, int &count)
{
      if(!root)
                return 0;
          
          int L_C = UniValueSubTrees(root->left, count);
              int R_C = UniValueSubTrees(root->right, count);
                  
                  int temp = 1, L_data=0, R_data=0;
                      
                      if(root->left)
                                L_data = root->left->data;
                          if(root->right)
                                    R_data = root->right->data;
                              
                              if((root->data == L_data) && (root->data == R_data))
                                    {
                                              temp = L_C + R_C + (L_C * R_C) + 1;
                                                      count +=  temp;
                                                          }
                                  else if(root->data == L_data)
                                        {
                                                  temp = L_C + 1;
                                                          count += temp;
                                                              }
                                      else if(root->data == R_data)
                                            {
                                                      temp = R_C + 1;
                                                              count += temp;
                                                                  }
                                          else
                                                    count += temp;
                                                  
                                              return temp;

}


int main()
{
  stack<node> nodeStack;

  char c ;
  
  cin >> c ;

}
