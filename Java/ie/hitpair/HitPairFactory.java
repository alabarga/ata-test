package com.ata.ie.hitpair;

import java.util.List;
import java.util.Map;

/**
 * This class takes care of building the HitPairs.
 * 
 * @see HitPair
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HitPairFactory {

  /**
   * The unique keys of the individual Hits comprising the HitPair are sorted
   * lexicographically and concatenated to produce the HitPair's unique key.
   * 
   * @param h1
   * @param h2
   * @return A unique key representing the HitPair
   */
  
  public static String buildKey (Hit h1, Hit h2){
    StringBuffer keyBuffer = new StringBuffer(100);
    if (h1.compareTo(h2) <= 0){ // h1 precedes h2 lexicographically
      keyBuffer.append(h1.getKey()).append(h2.getKey());      
    }
    else {
      keyBuffer.append(h2.getKey()).append(h1.getKey());
    }
    return keyBuffer.toString();    
  }
        
  
  /**
   * @param hitKeyList A list containing the unique keys of all the individual 
   *                   Hits that have been found in a sentence. The list can 
   *                   have duplicates, which means that a particular Hit is
   *                   found more than once in the sentence. The order is 
   *                   relevant and mirrors the order in which the Hits are
   *                   found in the sentence
   * @param hitHash    The Map which contains the Hits, as found thoughout the
   *                   corpus of Medline abstracts retrieved by FIRE. It is
   *                   addressed by the Hits' unique keys
   * @param hitPairHash The Map which contains the HitPairs, as found thoughout 
   *                    the corpus of Medline abstracts retrieved by FIRE. 
   *                    It is addressed by the HitPairs' unique keys
   * @param pmid        A particular HitPair is found in one or many Abstracts
   *                    and sentences throughout the corpus retrieved by
   *                    FIRE. Thus each one of the PMIDs of the abstracts
   *                    are stored together with the sentences in the HitPair
   * @param sentId      The sentence id 
   * @param sentTxt     The text of the sentence
   */
  public static void buildHitPairs (List<String> hitKeyList,
                                    Map<String,Hit> hitHash,
                                    Map<String, HitPair> hitPairHash, 
                                    String pmid,
                                    String sentId, 
                                    String sentTxt){
    
    int size = hitKeyList.size(); // List of Hits in a sent
    for (int i=0; i<size-1; i++){
      for (int j=i+1; j<size; j++){       
        Hit h1 = hitHash.get(hitKeyList.get(i));
        Hit h2 = hitHash.get(hitKeyList.get(j));
        if (!h1.equals(h2)){ 
          // No sense in having a Hit co-occurring with itself
          String hitPairkey = buildKey(h1, h2);        
          HitPair hp = hitPairHash.get(hitPairkey);
          if (hp == null){
            hp = new HitPair(hitPairkey, h1, h2);                 
          }     
          hp.addSent(pmid, sentId, sentTxt); 
          hitPairHash.put(hitPairkey, hp);
        }                                           
      }                           
    }       
  }   
}
