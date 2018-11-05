package com.ata.ie.mwt.go;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class GoOboParser {

  public static final int BUFFER_SIZE = 1024 * 1024 * 8;
  public static final String TERM_START = "[Term]";
  
  
  public static List<GoTerm> parse (InputStream input) throws Exception {
    
    BufferedReader in = new BufferedReader(new InputStreamReader(input), BUFFER_SIZE);
  
    List<GoTerm> goTermList = new ArrayList<GoTerm>();
    boolean inTerm = false;
    boolean isObsolete = false;
    List<KeyVal> attrList = new ArrayList<KeyVal>();
    
    for (String line=null; (line=in.readLine()) != null; ){
      line = line.trim();
      
      if (line.equals(TERM_START)){
        inTerm = true;
        isObsolete = false;
        attrList.clear();
      }
      else if (inTerm && line.length() == 0){
        inTerm = false;
        if (!isObsolete){
          goTermList.add(new GoTerm(attrList));
        }
      }
      else if (inTerm){
        KeyVal kv = new KeyVal(line);
        isObsolete = isObsolete || kv.isObsolete();
        if (!isObsolete){
          attrList.add(kv);
        }
      }
    }
    
    if (inTerm && !isObsolete && attrList.size() > 0){
      goTermList.add(new GoTerm(attrList));
    }
    
    in.close();
    
    return goTermList;
  }
}
