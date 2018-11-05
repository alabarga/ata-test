package com.ata.ie.hitpair;


import java.util.List;
import java.util.ArrayList;


/**
 * This class represents a named entity, a Hit, such as a Swissprot protein or
 * gene name, or a cellular component from the Gene Ontology, or a Drug from
 * DrugBank. Hits are identified by a unique key composed of the primary id in
 * a punblic domain database and the semantic type:
 * <p>
 * Magnesium Sulfate would yield the key <b>APRD01080drug</b> and would be
 * linked to DrugBank via the link
 * <p>
 * http://redpoll.pharmacy.ualberta.ca/drugbank/cgi-bin/getCard.cgi?CARD=APRD01080.txt
 * <p>
 * A Hit can be found in the literature with a variation of names, synonyms, in
 * which case all the findings are kept in the names attribute.
 * 
 * @see HitFactory
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */


public class Hit implements Comparable<Hit> {

  private String key;   // id + type
  private String id;    // primary key in a database, URL + id gives the link
  private String type;  // type (semantic), uniprot, drug, disease ...
  private List<String> names;
  
  
  public Hit (String key, String id, String type){
    this.id    = id;
    this.type  = type;
    this.key   = key;
    names = new ArrayList<String>();
  }
  
  // Bean accessor methods
  public void setKey (String key){ this.key = key; }
  public String getKey (){ return key; }
  
  public void setId (String id){ this.id = id; }
  public String getId (){ return id; }
  
  public void setType (String type){ this.type = type; }
  public String getType (){ return type; }
  
  public String getName (){
  	StringBuffer strBuffer = new StringBuffer();
  	for (String name: names){
      strBuffer.append(name)
        .append(" or "); // Length == 4        
    }
    strBuffer.setLength(strBuffer.length() - 4);
    strBuffer.append(" (").append(type).append(")");
  	return strBuffer.toString(); 
  }
  
  public void addName (String name){    
    if (!names.contains(name)) names.add(name);
  }
  
  public boolean equals (Hit h){ return key.equals(h.key); }
  
  public int compareTo (Hit h){ return key.compareTo(h.key); } 
  
  
  public void printHit (){
    System.out.println(type + "/" + id + ": " + getName());
    System.out.println("--------------------------------");
  }
}
