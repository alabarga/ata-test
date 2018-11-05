#ifndef Utils_normBetweenArray_h
#define Utils_normBetweenArray_h

#include <vector>
#include <string>
#include <iostream>
#include <sstream>
#include <algorithm>
#include <iterator>
#include "error_handle/src/error_handle.h"
#include "matrix/src/matrix.h"
#include "boost/regex.hpp"
#include <map>
#include <boost/lexical_cast.hpp>
using namespace std;

const double NA_value=8.8888888888888888888; 

string int2String(int t)
{
return boost::lexical_cast<string>(t);
}


 vector<string> normBetweenArray_replace_v_string(const vector<string>&vs, string str1, string str2)
{
	vector<string> vs_result;
	boost::regex reg(str1);
	for(vector<string>::const_iterator c_i_s = vs.begin(); c_i_s != vs.end(); c_i_s++)
	{
		if(boost::regex_match(*c_i_s,reg))
		{
			string s=boost::regex_replace(*c_i_s, reg, str2);
			vs_result.push_back(s);
		}
		else
		{
			vs_result.push_back(*c_i_s);
		}
	}
	return vs_result;
}




// Slice_iter<double> m_c0 = m.column(0);
// reject na values of a column and return a vector with valid values
void normBetweenArray_get_valid_vector(Matrix x, Slice_iter<double> m_c, vector<double> & valid_vector,int ncol)
{
	for(unsigned int r_i=0;r_i<m_c.size();r_i++)
	{
		if(x[r_i][ncol]!=NA_value) 
		{
			valid_vector.push_back(fabs(x[r_i][ncol]));
		}
	}
}



double normBetweenArray_median_of_vector_double(vector<double> v_vector) {

       sort(v_vector.begin(), v_vector.end());
       int mid=v_vector.size();
       double medianValue;
       if (mid%2==0){
                        mid=mid/2;
                        medianValue=v_vector[mid-1]/2+v_vector[mid]/2;
       }
       else{
                        mid=int(mid/2);
                        medianValue=v_vector[mid]/2;
       }
       return medianValue;
}




double normBetweenArray_mean_of_vector_double(vector<double> median_vector){

	double sum=0;
	for(unsigned int m_i=0;m_i<median_vector.size();m_i++)
	{
		sum=median_vector[m_i]+sum;
	}
	double meanValue=sum/median_vector.size();
	return meanValue;

}


void normBetweenArray_get_cmed(Matrix  x, vector<double> & cmed) 
{ 
	//x=abs(x);	
	for(unsigned int col_i=0; col_i<x.dim2();col_i++)
	{
		Slice_iter<double> x_col=x.column(col_i);
		vector<double> valid_vector;
		normBetweenArray_get_valid_vector(x,x_col, valid_vector,col_i );
        double medianValue= normBetweenArray_median_of_vector_double(valid_vector);
		//cout<<"median: "<<medianValue<<endl;
		double cm=log(medianValue);
		cmed.push_back(cm);
		
	}
   double meanValue=normBetweenArray_mean_of_vector_double(cmed);
   //cout<<"cmed_value: "<<endl;
   for(unsigned int cm_i=0;cm_i<cmed.size();cm_i++)
   {
	   cmed[cm_i]=exp(cmed[cm_i]-meanValue);
	   //cout<<cmed[cm_i]<<" ";
   }

   cout<<endl;
}


void isColEnough(Matrix x)
{
for(unsigned int na_i=0;na_i<x.dim2();na_i++)
{
	int col_na=0;
	for(unsigned int na_j=0;na_j<x.dim1();na_j++)
	{
	  if(x[na_j][na_i]==NA_value)
		  col_na++;      
    }
	//cout<<"col_na"<<col_na<<endl;
	if(x.dim1()-col_na<=2)
		{
			throw Error("There is less than two values in column "+int2String(na_i+1)+".");
		}
	
}




}






























#endif
