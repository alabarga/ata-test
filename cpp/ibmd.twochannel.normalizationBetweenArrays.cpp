// ibmd.twochannel.normalizationBetweenArrays.cpp : Defines the entry point for the console application.
//
#include "utils/src/config.h"
#ifdef CONFIG_H 

#include "StdAfx.h"
#endif
#include <vector>
#include <iostream>
#include <string>
#include "read_write.h"
#include <fstream>
#include <boost/timer.hpp>

//#include "boost/progress.hpp"
using namespace std;
using namespace boost;

int main(int argc, char* argv[])
{
//boost::progress_timer t;
   timer t0;
    cout<<"Normalize Between Arrays."<<endl;   
	char * filename="single_input.txt";
	char * outfile="output.txt";
	read_write(filename,outfile,",");	
	printf( "\n 100*10: totally %f seconds \n", t0.elapsed() );
	system("pause");
	return 0;
	    

}

