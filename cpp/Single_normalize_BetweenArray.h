#ifndef single_normalizeBetweenArrays_h
#define single_normalizeBetweenArrays_h

#include "matrix/src/matrix.h"
#include "utils_normBetweenArray.h"
#include <iostream>
#include <string>
#include "datasheetSingleMatrix/src/Datasheetsinglematrix.h"
#include "boost/regex.hpp"
#include "normalizeMedianAbsValues.h"
#include "na_lable.h"

// Svilen

using namespace std;

// for single_channel input data
void Single_normalize_BetweenArray(Datasheetsinglematrix & mydata)  
{
  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 0%"));

	TASKSITE->SetProgress(0.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  // 1. get the index of nas
  vector<int>index1;
  vector<int>index2;  
  Matrix & single=mydata.matrix();
 //  judge if there are enough data in one column
  isColEnough(single); 
  // get na lables
  na_lable(single, index1,index2);
  
  // 2. normalize between arrays using normalizeMedianAbsValues

  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 10%"));
	TASKSITE->SetProgress(10.);

	if (TASKSITE->IsCancelled())
		return;
#endif
  get_normalizeMedianAbsValues(single); 

  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 20%"));

	TASKSITE->SetProgress(20.);

	if (TASKSITE->IsCancelled())
		return;

#endif
  // 3. transfer to NA values
  
  for(unsigned index_i=0;index_i<index1.size();index_i++)
  {
	  //cout<<"NA values:"<<endl;
	  single[index1[index_i]][index2[index_i]]=NA_value;
	 
  }

  // Svilen
#ifdef CONFIG_H
	TASKSITE->SetStatus(_T("Normalizing... 40%"));
	TASKSITE->SetProgress(40.);

	if (TASKSITE->IsCancelled())
		return;
 
  // 4. construct output data
//	mydata.matrix=single; 

  // Svilen
	TASKSITE->SetStatus(_T("Normalizing... 100%"));
	TASKSITE->SetProgress(100.);

#endif
}

#endif
