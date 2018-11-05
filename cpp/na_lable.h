#ifndef na_lable_h
#define na_lable_h

#include <vector>
#include <algorithm>
#include <iterator>
#include "error_handle/src/error_handle.h"
#include "matrix/src/matrix.h"
#include "utils_normBetweenArray.h"


using namespace std;

//const double NA_value=88.00; 

void na_lable(Matrix  x, vector<int> & index1, vector<int> & index2)
{
   
	for(unsigned int i=0;i<x.dim1();i++)
	{  
		for(unsigned int j=0;j<x.dim2();j++)
		{
			if(x[i][j]==NA_value)
			{
				index1.push_back(i); // for row index
				index2.push_back(j); // for column index
							
			}
		}
		
	}
}



#endif

