package com.ata.ie.mwt.taxonomy;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import com.ata.ie.mwt.HeadersAndFooters;


/**
 * Class used to obtain the go vocabulary from: ftp://ftp.ncbi.nih.gov/pub/taxonomy/taxdmp.zip 
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class MWTTaxonomyWriter {
  
  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "species";

  private static final Set<String> ACCEPTED_NAME_TYPES = new HashSet<String>();
  static {
    // Available: acronym, anamorph, blast name, common name, equivalent name
    // genbank acronym, genbank anamorph, genbank common name, genbank synonym
    // in-part, includes, misnomer, misspelling, scientific name, synonym, teleomorph
    ACCEPTED_NAME_TYPES.add("common name");
    ACCEPTED_NAME_TYPES.add("equivalent name");
    ACCEPTED_NAME_TYPES.add("scientific name");
  }
  
  public static final int BUFFER_SIZE = 1024 * 8;
  public static final String SEPARATOR = "#"; // substitution of \t by #
  public static final int ID_FIELD = 0;       // GO:0000123
  public static final int NAME_FIELD = 1;     // Actual name (term in the Taxonomy vocabulary)
  public static final int NAMETYPE_FIELD = 3;

  
  public static void main (String [] args) throws Exception {
    
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in), BUFFER_SIZE);
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out), BUFFER_SIZE);

    // Header
    out.write(HeadersAndFooters.getMwtHeader(TAGNAME));
    
    StringBuilder mwtLine = new StringBuilder(); 
    Set<String> nameSetForId = new HashSet<String>();
    
    // Individual lines grouped by Name
    for (String prevId=null, line=null; null != (line=in.readLine()); ){
      String [] fields = line.replaceAll("\\s*\\|\\s*", SEPARATOR).split(SEPARATOR);
      if (fields.length < NAMETYPE_FIELD) continue;
      String nameType = fields[NAMETYPE_FIELD];
      if (nameType == null || !ACCEPTED_NAME_TYPES.contains(nameType)) continue;
      String id = fields[ID_FIELD];
      String name = fields[NAME_FIELD];
      if (id == null || name == null) continue;
      if (prevId == null || id.equals(prevId)){
        nameSetForId.add(name);
      }
      else if (prevId != null && !id.equals(prevId)){
        mwtLine.setLength(0);
        for (String n: nameSetForId){
          mwtLine.append("<t p1=\"")
                 .append(prevId)
                 .append("\">")
                 .append(n)
                 .append("</t>\n");
        }
        out.write(mwtLine.toString());
        nameSetForId.clear();
      }
      prevId = id;
    }
    
    out.write(HeadersAndFooters.getMwtFooter());
    in.close();
    out.flush();
    out.close();
  }
}
