package com.ata.ie.hitpair;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import com.ata.ie.filter.MyServer;
import com.ata.ie.filter.Sentenciser;
import com.ata.ie.mwt.go.MWTGoWriter;
import com.ata.ir.search.Output;

import monq.jfa.Nfa;
import monq.jfa.CallbackException;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.AbstractFaAction;
import monq.jfa.ReaderCharSource;
import monq.jfa.Xml;
import monq.jfa.actions.Drop;

/**
 * This class is in charge for collecting the incoming XML from the FIRE
 * server and for analyzing it to extract the Hits and HitPairs.
 * 
 * @see HitPairBusterData
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HitPairBuster {   
  
  /* **** M E D L I N E C I T A T I O N S E T **** */
  private static AbstractFaAction action_start_set=new AbstractFaAction(){                               
    public void invoke(StringBuffer yytext,int start,DfaRun runner)       
    throws CallbackException {
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      hitPairBusterData.attrsPopulate(yytext, start);            
      hitPairBusterData.totalHits = Integer.parseInt(hitPairBusterData.attrsGet(Output.HITS_ATTR));
    }
  };
  
  
  /* **** M E D L I N E C I T A T I O N **** */
  private static AbstractFaAction action_start_citation=new AbstractFaAction(){                               
    public void invoke(StringBuffer yytext,int start,DfaRun runner)       
    throws CallbackException {
      // At the start of a citation we clear the client data's internal variables
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      hitPairBusterData.resetCitation();
      /* **
       * Here we tell the DfaRun to start collecting the incoming text
       * in its internal buffer. We need to remember to set the flag to
       * false or we run the risk of overflowing.
       */
      runner.collect = true;      
      hitPairBusterData.startCitation = start;
    }
  };
  private static AbstractFaAction action_end_citation = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {                                               
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      // We stop collecting the citation and reset the buffer to a normal size
      runner.collect = false;      
      yytext.setLength(hitPairBusterData.startCitation);
      hitPairBusterData.processedHits ++;
    }
  };
  
  
  /* **** P M I D **** */
  private static AbstractFaAction action_start_pmid=new AbstractFaAction() {                               
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {    
      /* **
       * A Medline citation is identified by a unique id called a PMID or
       * PubMed Id. The first <PMID>id</PMID> found in the citation is the
       * one we are looking for. But be aware that within the reminder of the
       * citation you could encounter other <PMID>id</PMID> like structures 
       * which intention is to cross reference related citations. This is why
       * a flag is required to indicate that the main PMID has been found.
       */
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;                                                  
      if (!hitPairBusterData.pmidFound){
        // Offset of the PMID within the citation
        hitPairBusterData.startPmid = yytext.length();      
      }
    }
  };
  private static AbstractFaAction action_end_pmid = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {                                               
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;                              
      if (!hitPairBusterData.pmidFound){
        hitPairBusterData.pmid 
          = yytext.substring(hitPairBusterData.startPmid, start);      
        hitPairBusterData.pmidFound = true;
      }
    }
  };
    
      
  /* **** SENT **** */
  private static AbstractFaAction action_start_sent = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {                                                   
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      /* **
       * The matched SENT segion looks like this:
       * 
       * <SENT sid="0" pm=".">...
       * 
       * start points to the opening "<"
       */
      hitPairBusterData.attrsPopulate(yytext, start);            
      hitPairBusterData.sentId = hitPairBusterData.attrsGet(Sentenciser.SID_ATTR);
      hitPairBusterData.startSent = yytext.length();
      hitPairBusterData.clearHitKeyList();       
    }
  };      
  private static AbstractFaAction action_end_sent = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {                                         
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      hitPairBusterData.sentTxt = yytext.substring(hitPairBusterData.startSent, start); 
      hitPairBusterData.buildHitPairs();                                          
    }
  };    
      
      
  /* **** Z **** */
  private static AbstractFaAction action_start_z = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {                
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;
      hitPairBusterData.attrsPopulate(yytext, start);
      String tmpZType = hitPairBusterData.attrsGet(Xml.TAGNAME); // z:go, ...            
      hitPairBusterData.zType = tmpZType.substring(2);      
      hitPairBusterData.zId = hitPairBusterData.attrsGet("ids");             
      hitPairBusterData.zGOnto = hitPairBusterData.attrsGet(MWTGoWriter.ONTOLOGY_ATTR);     
      hitPairBusterData.startZName = yytext.length();            
      hitPairBusterData.startZ = start;      
    }
  };        
  private static AbstractFaAction action_end_z = new AbstractFaAction() {
    public void invoke(StringBuffer yytext,int start,DfaRun runner)
    throws CallbackException {           
      HitPairBusterData hitPairBusterData = (HitPairBusterData)runner.clientData;                         
      hitPairBusterData.zName 
		    = yytext.substring(hitPairBusterData.startZName, start);        
      hitPairBusterData.buildHit();                                      
    }        
  };
  
  
  private static Dfa dfa;      
  static {
    try {
      Nfa nfa = new  
        Nfa(Xml.STag("MedlineCitationSet"), action_start_set)
        .or(Xml.STag("MedlineCitation"), action_start_citation)
        .or(Xml.ETag("MedlineCitation"), action_end_citation)
        .or(Xml.STag("PMID"), action_start_pmid)
        .or(Xml.ETag("PMID"), action_end_pmid)
        .or(Xml.STag("Sentence"), action_start_sent)
        .or(Xml.ETag("Sentence"), action_end_sent)
        .or("<plain>",  Drop.DROP)
        .or("</plain>", Drop.DROP)
        .or(Xml.STag("a:[a-zA-Z0-9]+"), action_start_z)
        .or(Xml.ETag("a:[a-zA-Z0-9]+"), action_end_z);
      dfa = nfa.compile(DfaRun.UNMATCHED_COPY);
    } 
    catch (Exception e){      
      throw new Error("HitPairBuster failure!", e);
    }
  }
  
  
  private HitPairBusterData hitPairBusterData; 
  private DfaRun dfaRun;
  
  
  /**
   * The class processes the incoming XML and fills the Hit and HitPair Hashes
   * 
   * @param in The incoming stream of XML as delivered by FIRE
   * @param hitHash
   * @param hitPairHash
   */
  public HitPairBuster (InputStream in,
                        Map<String,Hit> hitHash,
                        Map<String,HitPair> hitPairHash){
                        
    ReaderCharSource charSource = null;
    try {
      charSource = new ReaderCharSource(in, MyServer.ENCODING);
    }
    catch (UnsupportedEncodingException e){
      charSource = new ReaderCharSource(in);
    }
    
    dfaRun = new DfaRun(dfa, charSource);
    hitPairBusterData = new HitPairBusterData(hitHash, hitPairHash);
    dfaRun.clientData = hitPairBusterData;           
  }  
  
  
  /**
   * It starts the filtering process
   * 
   * @return The number of hits delivered by FIRE
   * @throws IOException
   */
  public int filter () throws IOException  { 
    dfaRun.filter();
    return hitPairBusterData.totalHits;
  }
}
