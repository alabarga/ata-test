package com.ata.ie.mwt.drug;

import com.ata.ie.mwt.MWTFromURLWriter;

// http://www.nlm.nih.gov/medlineplus/druginfo/drug_Aa.html

/**
 * Class used to obtain the drug vocabulary from: http://www.nlm.nih.gov/medlineplus/druginfo/drug_Aa.html
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public final class MWTDrugWriter extends MWTFromURLWriter {
  
  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "drug";

  /**
   * Base URL where to find entries for this semantic type
   */
  public static final String URL_ROOT = "http://www.nlm.nih.gov/medlineplus/druginfo/";
  
  /**
   * The information about a particular semantic type can be obtained by fetching
   * the contents of the URL formed as: URL_ROOT/{ID}URL_TRAIL
   */
  public static final String URL_TRAIL = "a.html";
  
  /**
   * Regular expression which defines an entry within the URL
   */
  //public static final String ENTRY_REGEX = "<A href=\"medmaster/.?[0-9]+\\.html\"(.*</A>)!";
  public static final String ENTRY_REGEX = "<a href=\"/medlineplus/druginfo/meds/.?[0-9]+\\.html\"(.*</a>)!";
  /**
   * @see com.ata.ie.mwt.MWTFromURLWriter
   */
  public static final char CLUE = '/';
  
  
  /**
   * @see com.ata.ie.mwt.MWTFromURLWriter
   * @param tagName
   * @param idxUrl
   * @param outputFileName
   * @throws Exception
   */
  public MWTDrugWriter (String tagName, String idxUrl, String outputFileName)
  throws Exception {
    super(URL_ROOT, URL_TRAIL, ENTRY_REGEX, CLUE, tagName, idxUrl, outputFileName);
  }
  
  /**
   * Syntax: com.ata.ie.mwt.drug.MWTDrugWriter drugMWTFileName
   * <p>
   * Writes the MWT file for the drug filter server
   * @param args
   * @throws Exception
   */
  public static void main (String [] args) throws Exception {

    if (args.length != 1){
      System.err.println("Syntax: com.ata.ie.mwt.drug.MWTDrugWriter drugMWTFileName");
      System.exit( -1 );
    }
    
    new MWTDrugWriter(TAGNAME, "drug_", args[0]).writeFile();
  }
}
//Eof - Alberto Labarga (alberto.labarga@gmail.com)
