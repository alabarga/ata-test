/*
 *
 *
 * Copyright (c) 2009
 * jiayuehua   jiayuehua@omcisengineering.com
 *  
 * This material is provided "as is", with absolutely no warranty expressed
 * or implied. Any use is at your own risk.
 *
 * Permission to use or copy this software for any purpose is hereby granted
 * without fee, provided the above notices are retained on all copies.
 * Permission to modify the code and to distribute modified code is granted,
 * provided the above notices are retained, and a notice that the code was
 * modified is included with the above copyright notice.
 *
 *
 */
#ifndef ISTWOCHANNEL_H
#define ISTWOCHANNEL_H



#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <algorithm>
#include <functional>

#include "boost/regex.hpp"


#include "error_handle/src/error_handle.h"
#include "utils/src/tempfun.h"


class SuffixMatch
{
private:
 boost::regex reg_suffix;
public:
 SuffixMatch(boost::regex reg):reg_suffix(reg){}
 bool operator ()(string str)
 {
	return  boost::regex_match(str,reg_suffix);
 }
};



bool isTwoChannel( fstream &  fin)
{
	vector<string> suffix_names_;
	suffix_names_.push_back("A");
	suffix_names_.push_back("M");

	vector<string> source_str1 = open_file(fin);
	string reg_str1 = string("(.+)")  +string ("(\\.)")+string("(") + "A" + string(")");
	boost::regex reg_suffix1(reg_str1,boost::regex::icase);
	
	string reg_str2 = string("(.+)")  +string ("(\\.)")+string("(") + "M" + string(")");
	boost::regex reg_suffix2(reg_str2,boost::regex::icase);

	int A_size = count_if(source_str1.begin(), source_str1.end(), SuffixMatch(reg_suffix1) );
		int M_size = count_if(source_str1.begin(), source_str1.end(), SuffixMatch(reg_suffix2) );
		if(A_size){
			return A_size == M_size;
		}
		else{
			return false;
		}
}

#endif