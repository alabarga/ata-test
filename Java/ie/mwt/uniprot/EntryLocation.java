package com.ata.ie.mwt.uniprot;

/**
 * Provides a pointer for an entry in a given file. For swissprot, ini gives the 
 * size to the first byte of an "<entry ...>" and size gives the ammount to read 
 * to have the full entry.
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class EntryLocation {
  
  public long ini;
  public int size;
  
  
  public EntryLocation (){
    ini = 0;
    size = 0;
  }
    
  public EntryLocation (long ini, long fin){
    this.ini = ini;
    this.size = (int)(fin - ini);
  }  
  
  public String toString(){
  	return new StringBuffer()
	    .append(ini)
	    .append(" ")
	    .append(size)
      .toString();
  }  
}
