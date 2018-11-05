package com.ata.ie.mwt.uniprot;


import java.util.Collections;
import java.util.List;


public class EntryData extends EntryLocation {
	
  private String accessionKey;
  private List <String> accessionKeys;  
  private List <String> names;  
  
  
  public EntryData (List <String> accessionKeys,
					          List <String> names){
      
    Collections.sort(accessionKeys);
    StringBuilder sb = new StringBuilder();
    for (String acc: accessionKeys){
      sb.append(acc).append(Constants.UNIPROT_ID_SEP);
    }
    sb.setLength(sb.length() - Constants.UNIPROT_ID_SEP.length());
    accessionKey = sb.toString();
  	this.accessionKeys = accessionKeys;
  	this.names = names;  	
  }
  
  
  public String getAccessionKey(){ return accessionKey; }
  
  
  public List<String> getAccessionKeys(){ return accessionKeys; }
  
  
  public List<String> getNames(){ return names; }
  
  
  public String toString (){
    StringBuffer sb = new StringBuffer();  
  	sb.append("===========================\n")
  	  .append("Accession Key: ").append(accessionKey)
  	  .append("\nAccession Keys:\n");
  	for (String accession: accessionKeys){
      sb.append("\t").append(accession).append("\n");
    }
    sb.append("\nProtein Names:\n");
    for (String name: names){
      sb.append("\t").append(name).append("\n");
    }         
    return sb.toString();
  } 
}
