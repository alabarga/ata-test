#ifndef Two_normalizeBetweenArrays_h
#define Two_normalizeBetweenArrays_h

#include "matrix/src/matrix.h"
#include "utils_normBetweenArray.h"
#include "RG2MA.h"
#include <iostream>
#include <string>
#include "datasheet/src/datasheet.h"
#include "boost/regex.hpp"
#include "normalizeMedianAbsValues.h"
#include "na_lable.h"

// Svilen

using namespace std;

// for two_channel input data
void Two_normalize_BetweenArray(Datasheet & mydata)  
{
  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 0%"));
	TASKSITE->SetProgress(0.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  // 0. transfer matrix & R,matrix & G to matirx M and matrix A, ensure data is complete
  Matrix R;
  Matrix G;
  Matrix M;
  Matrix A;
  
  // 1. get NA values' index
  vector<int>index1;
  vector<int>index2;  
  vector<int>index3;
  vector<int>index4; 

  if(mydata.validSuffixName("R")&&mydata.validSuffixName("G"))
  {
	  
	  R=mydata.matrixbySuffixName ("R");
	  G=mydata.matrixbySuffixName("G");
	  M=R;
	  A=G;
	  //  judge if there are enough data in one column
      isColEnough(M);
      isColEnough(A);
	  //  get na lables
	  na_lable(M,index1,index2);
	  na_lable(A,index3,index4);
	  RG2MA(R,G,M,A);	  
	  
  }
  else if(mydata.validSuffixName("M")&&mydata.validSuffixName("A"))
  {
	  
	  M=mydata.matrixbySuffixName ("M");
	  A=mydata.matrixbySuffixName ("A");
	  //  judge if there are enough data in one column
      isColEnough(M);
      isColEnough(A);
	  //  get na lables
	  na_lable(M,index1,index2);
	  na_lable(A,index3,index4);
  }
  else 
	  throw Error("Data incomplete. ");	 

  
  // 2. normalize between arrays using normalizeMedianAbsValues
#ifdef CONFIG_H // Svilen
	TASKSITE->SetStatus(_T("Normalizing... 10%"));
	TASKSITE->SetProgress(10.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  get_normalizeMedianAbsValues(M); 
  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 20%"));
	TASKSITE->SetProgress(20.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  get_normalizeMedianAbsValues(A);

#ifdef CONFIG_H
  // Svilen
	TASKSITE->SetStatus(_T("Normalizing... 30%"));
	TASKSITE->SetProgress(30.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  // 3. transfer to NA values
  
  for(unsigned int index_i=0;index_i<index1.size();index_i++)
  {
	  //cout<<"NA values:"<<endl;
	  M[index1[index_i]][index2[index_i]]=NA_value;
	 
  }

  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 40%"));
	TASKSITE->SetProgress(40.);

	if (TASKSITE->IsCancelled())
		return;
#endif  
  for(unsigned int index_j=0;index_j<index3.size();index_j++)
  {
	  A[index3[index_j]][index4[index_j]]=NA_value;
  }

#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 50%"));
	TASKSITE->SetProgress(50.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  // 4. construct output data
  if(mydata.validSuffixName("R")&&mydata.validSuffixName("G"))
   {
		   //1. replace suffix name R G to M A
	    mydata.matrixbySuffixName("R") = M;
        mydata.matrixbySuffixName("G") = A;	
		string str1("(.+)(\\.)(R)");
		string str2("$1$2M");
		mydata.headerNames()= normBetweenArray_replace_v_string(mydata.headerNames(), str1,str2);
		string str3("(.+)(\\.)(G)");
		string str4("$1$2A");
        mydata.headerNames()= normBetweenArray_replace_v_string(mydata.headerNames(), str3,str4);
   }   
  
  else{
   mydata.matrixbySuffixName("M") = M;
   mydata.matrixbySuffixName("A") = A;
   
   //cout<<"step3: "<<endl;
  }
   //mydata.header_names_=sort(header);

#ifdef CONFIG_H
  // Svilen
	TASKSITE->SetStatus(_T("Normalizing... 100%"));
	TASKSITE->SetProgress(100.);
#endif
}
#endif
