package com.ata.ie.mwt.disease;

import com.ata.ie.mwt.MWTFromURLWriter;

// http://www.healthcentral.com/ency/408/{ID}.html

/**
 * Class used to obtain the disease vocabulary from: http://www.healthcentral.com/ency/408/{ID}.html
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public final class MWTDiseaseWriter extends MWTFromURLWriter {

  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "disease";
  
  /**
   * Base URL where to find entries for this semantic type
   */
  public static final String URL_ROOT = "http://www.healthcentral.com/ency/408/";
  
  /**
   * The information about a particular semantic type can be obtained by fetching
   * the contents of the URL formed as: URL_ROOT/{ID}URL_TRAIL
   */
  public static final String URL_TRAIL = ".html";
  
  /**
   * Regular expression which defines an entry within the URL
   */
  public static final String ENTRY_REGEX = "<a href=\"/ency/408/[0-9]+\\.html\"(.*</a>)!";
  
  /**
   * @see com.ata.ie.mwt.MWTFromURLWriter
   */
  public static final char CLUE = '"';
  
  
  /**
   * @see com.ata.ie.mwt.MWTFromURLWriter
   * @param tagName
   * @param idxUrl
   * @param outputFileName
   * @throws Exception
   */
  public MWTDiseaseWriter (String tagName, String idxUrl, String outputFileName)
  throws Exception {
    super(URL_ROOT, URL_TRAIL, ENTRY_REGEX, CLUE, tagName, idxUrl, outputFileName);
  }
  
  
  /**
   * Syntax: com.ata.ie.mwt.disease.MWTDiseaseWriter diseaseMWTFileName
   * <p>
   * Produces the MWT file for the disease filter server
   * @param args
   * @throws Exception
   */
  public static void main (String [] args) throws Exception {

    if (args.length != 1){
      System.err.println("Syntax: com.ata.ie.mwt.disease.MWTDiseaseWriter diseaseMWTFileName");
      System.exit( -1 );
    }
    
    // Disease
    new MWTDiseaseWriter(TAGNAME, "diseidx", args[0]).writeFile();
  }
}

