// Normalization funciton: method= "scale" 
#ifndef get_normalizeMedianAbsValues_h
#define get_normalizeMedianAbsValues_h

#include <string>
#include "matrix/src/matrix.h"
#include "utils_normBetweenArray.h"
#include "error_handle/src/error_handle.h"

using namespace std;

int get_normalizeMedianAbsValues(Matrix & x)
{
	unsigned int narrays=x.dim2();
	if(narrays==1)
		return 0;
	vector<double>  cmed;
	normBetweenArray_get_cmed(x, cmed);
	for(unsigned int xcol_i=0;xcol_i<x.dim2();xcol_i++ )
	{
		double cmed_i=cmed[xcol_i];
		for(unsigned int xrow_i=0;xrow_i<x.dim1();xrow_i++)
		{
			x[xrow_i][xcol_i]=x[xrow_i][xcol_i]/cmed_i;
		}
	}
    return 1;
}

#endif