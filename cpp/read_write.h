#ifndef read_write_h
#define read_write_h

#include "Single_normalize_BetweenArray.h"
#include "Two_normalize_BetweenArray.h"
#include <iostream>
#include <string>
#include "datasheet/src/datasheet.h"
#include "datasheetSingleMatrix/src/Datasheetsinglematrix.h"
#include "boost/regex.hpp"
#include "isTwoChannel.h"
#include <boost/timer.hpp>


//
// Svilen

using namespace std;
using namespace boost;

// deal with two kinds of input(single or two channel ), used to input the source file and output the result file
void read_write(char * pfin, char * output,const string & num_dilimiter = ",")
{
#ifdef CONFIG_H	// Svilen
	TASKSITE = new _ConsoleUnmanagedTaskSite();
#endif
	// read
	 timer t1;

	 fstream fin(pfin);
	 bool is_double=isTwoChannel(fin);
     if(is_double){
	 vector<string> suffixname;
	 suffixname.push_back("M");
	 suffixname.push_back("A");
     Datasheet mydata(pfin,suffixname,num_dilimiter);
     printf( "\n 100*10: totally %f seconds \n", t1.elapsed() );

	 timer t2;
	 Two_normalize_BetweenArray(mydata); 
     printf( "\n 100*10: totally %f seconds \n", t2.elapsed() );

// write

	 timer t3;
#ifdef CONFIG_H
	if (!TASKSITE->IsCancelled())
#endif
	{	
		ofstream fout(output);
		fout<<mydata;
	}
	printf( "\n 100*10: totally %f seconds \n", t3.elapsed() );
   }
   else {
      
	   timer t4;
	   Datasheetsinglematrix mydata(pfin, string("Row_index"),num_dilimiter);
      printf( "\n 100*10: totally %f seconds \n", t4.elapsed() );
   
	  timer t5;
   Single_normalize_BetweenArray(mydata) ;
   printf( "\n 100*10: totally %f seconds \n", t5.elapsed() );

    // write
   timer t6;
#ifdef CONFIG_H
 
if (!TASKSITE->IsCancelled())
#endif
	{	
		ofstream fout(output);
		fout<<mydata;
	}
    printf( "\n 100*10: totally %f seconds \n", t6.elapsed() ); 
   }  
   
   // Svilen
	//delete TASKSITE;


}

#endif














