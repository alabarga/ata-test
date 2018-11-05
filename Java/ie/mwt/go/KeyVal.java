package com.ata.ie.mwt.go;


public class KeyVal {

  public static final char FIELD_DELIM = ':';
  
  private String key, val;
  
  
  public KeyVal (String line) throws Exception {
      int idx = line.indexOf(FIELD_DELIM);
      if (idx == -1){
        throw new Exception("Corrupted GO file");
      }
      key = line.substring(0, idx);
      val = line.substring(idx + 1).trim();
    }
      
  
  public boolean equals (String k){
    return key != null && key.equals(k); 
  }
  
  
  public boolean contains (String k){
    return key != null && key.contains(k); 
  }
  
  
  public String getValue (){ return val; }
  
  
  public boolean isObsolete (){
    return (key.equals(GoTerm.IS_OBSOLETE_KEY) && val.equals(GoTerm.TRUE_VALUE));
  }
}
