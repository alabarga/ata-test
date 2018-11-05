package com.ata.ie.hitpair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class representing a collection of two Hits as found in a relationship of
 * co-occurrence in a sentence of some Medline abstract delivered by FIRE.
 * <p>
 * A HitPair is identified by a unique key compossed by the concatenation of the
 * Hits' unique keys in alphabetical sorting. The Hits of a Hit pair are not
 * assigned any logical order, which means that H1H2 is equivalent to H2H1.
 * <p> 
 * A HitPair holds an internal data strucutre containing the PMIDs of the 
 * documents and the comprised sentences in which it appears. The data
 * structure is
 * <p>
 * <b>Map&lt;String, Map&lt;String, String&gt;&gt;</b>
 * <p>
 * Where 
 * <p>
 * <b>Map{"PMID"}{"SentenceID"}</b> contains the text of a <b>"sentence"</b>
 * 
 * @see HitPairFactory
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HitPair {  

  private String key;
  private Hit h1, h2;    
  private Map<String, Map<String, String>> abstracts; 
  // abstracts{"pmid"}{"sentId"} -> sentStr
  

  public HitPair (String key, Hit h1, Hit h2){
    this.key  = key;
    this.h1   = h1;
    this.h2   = h2;            
    abstracts = new HashMap<String, Map<String, String>>();
  }       
  
  // Bean accessor methods
  public void setKey (String key){ this.key = key; }
  public String getKey (){ return key; }
  
  public void setH1 (Hit h1){ this.h1 = h1; }
  public Hit getH1 (){ return h1; }
  
  public void setH2 (Hit h2){ this.h2 = h2; }
  public Hit getH2 (){ return h2; }

  
  /**
   * @return The data structure pointing to the PMIDs and sentences in which 
   *         the HitPair is found Map&lt;String, Map&lt;String, String&gt;&gt;
   */
  public Map<String, Map<String, String>> getAbstracts (){ return abstracts; }
  
  
  /**
   * Method to add a sentence to a HitPair
   * 
   * @param pmid
   * @param sentId
   * @param sent
   */
  public void addSent (String pmid, String sentId, String sent){                
    Map<String, String> sentMap = abstracts.get(pmid);
    if (sentMap == null){      
      sentMap = new HashMap<String, String>();      
      abstracts.put(pmid, sentMap);
    }             
    if (!sentMap.containsKey(sentId)) sentMap.put(sentId, sent);
  }    
  
  
  public boolean equals (HitPair hp){ return key.equals(hp.key); } 
  
  
  // ===============================================================
  // Convenience methods used for debugging
  
  /**
   * Prints out the contents of a particular HitPair. Used for debugging 
   * purposes.
   */
  public void printAbstracts (){
    System.out.println("HitPair with ");
    System.out.println("- H1: " + h1.getName());
    System.out.println("- H2: " + h2.getName());
    printAbstracts(abstracts); 
  }
  
  
  private static void printAbstracts (Map <String, Map<String, String>> abstracts){
    StringBuffer buf = new StringBuffer();
    Iterator<String> pmidIterator = abstracts.keySet().iterator();    
    while (pmidIterator.hasNext()){
      String pmid = (String)pmidIterator.next();
      System.out.print("- PMID " + pmid + ": { ");
      Map<String, String> sents = abstracts.get(pmid);
      buf.setLength(0);
      Iterator<String> sentIdIterator = sents.keySet().iterator();
      while (sentIdIterator.hasNext()){
        String sentId = (String)sentIdIterator.next();
        System.out.print(sentId + " ");
        buf.append("- ").append(sentId).append(": ")
           .append(sents.get(sentId)).append("\n");
      }
      System.out.println("}");
      System.out.print(buf.toString());
    }
    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
  }
}
