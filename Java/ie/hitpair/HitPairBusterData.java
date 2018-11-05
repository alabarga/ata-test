package com.ata.ie.hitpair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import monq.jfa.Xml;

/**
 * This class is a helper for the HitPairBuster
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

class HitPairBusterData {	 
  
  protected boolean pmidFound = false;
  protected int startPmid, startSent, startZ, startZName, startCitation, 
                totalHits, processedHits;        
  protected String zId, zType, zGOnto, zName, sentId, sentPm, sentTxt, pmid;
  
  private Map<String, Hit> hitHash;
  private Map<String, HitPair> hitPairHash;
  private List<String> hitKeyList;
  private Map<String, String> attrs;
  
  
  public HitPairBusterData (Map<String,Hit> hitHash, 
                            Map<String,HitPair> hitPairHash){
    totalHits = 0;
    processedHits = 0;
    this.hitHash = hitHash;    
    this.hitPairHash = hitPairHash;        
    hitKeyList = new ArrayList<String>();                  
    attrs = new HashMap<String,String>();   
  }
     

  public void resetCitation (){
    pmidFound   = false;
    startPmid   = 0;    
    startSent   = 0;  
    startZ      = 0;
    startZName  = 0;        
    zId         = "";
    zType       = "";
    zGOnto      = "";         
    zName       = "";       
    sentId      = "";           
    sentPm      = "";
    sentTxt     = "";            
    pmid        = "";
  }  
  
  
  public void attrsPopulate (StringBuffer text, int start){
  	attrs.clear();
  	Xml.splitElement(attrs, text, start);
  }
     
  
  public String attrsGet (String key){  	
  	return attrs.get(key);
  }
  
    
  public void clearHitKeyList (){ hitKeyList.clear(); }    
  
  
  public void buildHit (){  	
  	String tmpKey = HitFactory.buildHit(hitHash, zId, zType, zGOnto, zName);         
    hitKeyList.add(tmpKey);
  }
  
  
  public void buildHitPairs (){  	
  	HitPairFactory.buildHitPairs(hitKeyList,
                                 hitHash,
		                             hitPairHash, 
			                           pmid,
			                           sentId, 
			                           sentTxt);
  }         
}      
