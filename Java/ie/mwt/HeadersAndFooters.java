package com.ata.ie.mwt;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The MWT files of the filter servers need a header and a footer (proper XML files do).
 * This class provides the methods to build such structures
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HeadersAndFooters {
  
  /**
   * This header is common to all filter servers. It defines a name space for the company
   */
  public static final String COMMON_HEADER = 
    "<?xml version='1.0' encoding='UTF-8'?>\n" +
    "<mwt xmlns:a=\"http://www.atalab.com/a\">\n";
  
  
  /**
   * This is the part of the header of any MWT file which is dependent on the filter server
   */
  private static final String STANDARD_MWTHEADER =
    COMMON_HEADER +  
    "<template><a:TAG_NAME ids=\"%1\">%0</a:TAG_NAME></template>\n\n";
  
  
  /**
   * This header is common to all filter servers. It provides the means to avoid the
   * multiplicity of tags for a named entity which belongs in two of the vocabularies
   * defined by two filter servers. If you cascade two filter servers, say protein
   * annotator and species annotator, each containing the same entry, say RAT (as in
   * Protein RAT and Species RAT), the order of the filter servers will determine
   * which category the named entity will be assigned. This footer ensures that a
   * subsequent filter server will not add tags around a named entity which has already
   * been tagged
   */
  private static final String STANDARD_MWTFOOTER = 
    "\n\n<!-- E X C E P T I O N :    I G N O R E -->\n" +
    "<template>%0</template>\n" +
    "<r>&lt;a:[^&gt;]*&gt;(.*&lt;/a)!:[^&gt;]*&gt;</r>\n" +
    "<r>&lt;[^&gt;]*&gt;</r>\n\n" +
    "</mwt>\n";  
  
  
  
  /**
   * @param tagName Name of the tag as in &lt;a:tagName&gt;Named entity&lt;/a:tagName&gt;
   * @return A personalized MWT file header suitable for a particular semantic type
   */
  public static final String getMwtHeader (String tagName){
    StringBuilder sb = new StringBuilder(STANDARD_MWTHEADER);       
    // Replace TAG_NAME with tagNames
    String tagTpt = "TAG_NAME";
    int tagTptLen = tagTpt.length();
    int tag1 = sb.indexOf(tagTpt, 0);
    sb.replace(tag1, tag1 + tagTptLen, tagName);
    int tag2 = sb.indexOf(tagTpt, tag1 + tagName.length());
    sb.replace(tag2, tag2 + tagTptLen, tagName);    
    // Add time stamp and creator
    String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());        
    sb.append("<!-- Created by: ATA Spa. Copyright 2007. All rights reserved -->\n");
    sb.append("<!-- Created on: ").append(timeStamp).append(" -->\n\n");
    return sb.toString();
  }

  
  /**
   * @return A standard MWF file footer
   */
  public static final String getMwtFooter (){ return STANDARD_MWTFOOTER; }
  
}
