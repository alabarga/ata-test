package com.ata.ie.mwt.uniprot;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ata.ie.mwt.HeadersAndFooters;

/**
 * Class used to obtain the go vocabulary from: ftp://ftp.ebi.ac.uk/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.xml.gz
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class MWTUniprotWriter {
  
  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "swissprot";
  
  private String fileName;
  private Set<String> nameSet;

  
  public MWTUniprotWriter (String fileName){
    this.fileName = fileName;
    nameSet = new HashSet<String>();
  }
  
  
  public void writeFile(Map <String, List<String>> map) throws FileNotFoundException {
    long nameCount = 0;
    PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
    pw.print(HeadersAndFooters.getMwtHeader(TAGNAME));
    for (String acc: map.keySet()){
      for (String name: map.get(acc)){
        if (nameSet.contains(name)){
          continue;
        }
        nameSet.add(name);
        pw.println("<t p1=\"" + acc + "\">" + name + "</t>");
        nameCount ++;
      }
    }
    pw.println(HeadersAndFooters.getMwtFooter());
    pw.flush();
    pw.close();
    System.err.println("Created file " + fileName + " with " + nameCount + " names.");
  }
}
