// transfer R G matrixes to M A matrixes

#ifndef RG2MA_h
#define RG2MA_h

#include <vector>
#include <string>
#include <iostream>
#include <algorithm>
#include <iterator>
#include "error_handle/src/error_handle.h"
#include "matrix/src/matrix.h"


using namespace std;

void RG2MA(Matrix R, Matrix G, Matrix & M, Matrix & A) //  return M A  matrix
{
   //  errohandle: judge R's dimensions is equal to G's dimensions
   if (R.dim1()!=G.dim1()){
		throw Error("row dimension of G doesn't match R's");
		
	}
	if(R.dim2()!=G.dim2()){
	   throw Error("column dimension of G doesn't match R's");
		}	

   vector<double> Mvector;
   vector<double> Avector;
   for(unsigned int row_i=0;row_i<R.dim1();row_i++){	
	for ( unsigned int col_i=0;col_i<R.dim2();col_i++){
			if(R[row_i][col_i]<=0)  
			{
				throw Error("there exits negative value(s) in R matrix."); 
			}
			if(G[row_i][col_i]<=0) 
			{
				throw Error("there exits negative value(s) in G matrix."); 
			}
		    
			Mvector.push_back(log(R[row_i][col_i])/log(2.0)-log(G[row_i][col_i])/log(2.0));
			Avector.push_back((log(R[row_i][col_i])/log(2.0)+log(G[row_i][col_i])/log(2.0))/2);
		}
   }
   Mvector.begin();
   Avector.begin();
   copy(Mvector.begin(),Mvector.end(), &M.array_1()[0]);
   copy(Avector.begin(),Avector.end(), &A.array_1()[0]);
}

#endif
