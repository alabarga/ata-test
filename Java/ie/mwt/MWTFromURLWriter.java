package com.ata.ie.mwt;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import monq.jfa.AbstractFaAction;
import monq.jfa.CallbackException;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.ReaderCharSource;
import com.ata.ie.filter.MyServer;

/**
 * Class used to obtain a vocabulary from a set of URLs which comply to certain rules
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class MWTFromURLWriter {

  
  protected String urlRoot;
  protected String urlTrail;
  protected String tagName;
  protected String idxUrl;
  protected PrintStream out;
  protected Dfa dfa;
  protected Map<String, Set<String>> namesMap;
  

  /**
   * The URL is built as:
   * <p>
   * URL = urlRoot + idxUrl + [A..Z] + urlTrail
   * <p>
   * For all URLs [A..Z] the pointed page is downloaded and parsed. In particular
   * entries within the page matching regExp are looked up. These entries are
   * broken down into {name} = [id1, id2, ..., idN], ie. typically such entries 
   * will look like:
   * <p>
   * &lt;a href="some/url/ID.html"&gt;name&lt;/a&gt;
   * <p>
   * At the end of the parsing of a the complete set of URLs a map indexed by
   * name and containing the list of IDs found (in the complete set of URLs for that
   * name) is produced and written out to a MWT file. The tagName defines the named
   * entity within the MWT file. The parameter idStartPositionClue gives a clue on
   * where the ID might start (eg. in the above example it would be /)
   * 
   * @param urlRoot
   * @param urlTrail
   * @param regExp
   * @param idStartPositionClue
   * @param tagName
   * @param idxUrl
   * @param outputFileName
   * @throws Exception
   */
  public MWTFromURLWriter (String urlRoot, 
                           String urlTrail, 
                           String regExp, 
                           final char idStartPositionClue,
                           String tagName, 
                           String idxUrl, 
                           String outputFileName)
  throws Exception {
    
    this.urlRoot = urlRoot;
    this.urlTrail = urlTrail;
    this.tagName = tagName;
    this.idxUrl = idxUrl;
    namesMap = new HashMap<String, Set<String>>();
    out = new PrintStream(new FileOutputStream(outputFileName));
    
    Nfa nfa = new Nfa(regExp, new AbstractFaAction(){
      public void invoke(StringBuffer yytext, int start, DfaRun runner)
      throws CallbackException {
        String match = yytext.substring(start);
        int sIdIdx = match.indexOf(idStartPositionClue) + 1;
        int eIdIdx = match.indexOf('.', sIdIdx);
        String id = match.substring(sIdIdx, eIdIdx);
        int gtIdx = match.indexOf('>', eIdIdx + 1) + 1;
        String name = match.substring(gtIdx, match.indexOf('<', gtIdx));
        
        Set<String> idSet = namesMap.get(id);
        if (idSet == null){
          idSet = new HashSet<String>();
        }
        idSet.add(id);
        namesMap.put(name, idSet);
        yytext.setLength(start);
      }
    });
    dfa = nfa.compile(DfaRun.UNMATCHED_DROP);
  }
  
  
  /**
   * Writes te MWT file
   * @throws Exception
   */
  public void writeFile () throws Exception {
    
    StringBuilder sb = new StringBuilder(urlRoot).append(idxUrl);
    int baseUrlLen = sb.length();
    
    for (char c='A'; c <= 'Z'; c++){
      sb.setLength(baseUrlLen);
      String urlName = sb.append(c).append(urlTrail).toString();      
      InputStream in = null;
      try {
        in = new URL(urlName).openStream();
      }
      catch (FileNotFoundException e){
        System.err.println("Failed to process: " + urlName);
        continue;
      }
      System.out.println("Processing: " + urlName);
      new DfaRun(dfa, new ReaderCharSource(in, MyServer.ENCODING)).filter();      
      in.close();
    }
    
    StringBuilder mwtLine = new StringBuilder();
    out.print(HeadersAndFooters.getMwtHeader(tagName));
    for (String name: namesMap.keySet()){
      mwtLine.setLength(0);
      mwtLine.append("<t p1=\"");
      Set<String> idSet = namesMap.get(name);
      for (String id: idSet){
        mwtLine.append(id).append(",");
      }
      mwtLine.setLength(mwtLine.length() - 1);
      mwtLine.append("\">").append(name).append("</t>");
      out.println(mwtLine.toString());
    }
    out.print(HeadersAndFooters.getMwtFooter());
    out.flush();
    out.close();
  }
}
