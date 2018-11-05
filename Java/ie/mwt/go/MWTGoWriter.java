package com.ata.ie.mwt.go;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.ata.ie.mwt.HeadersAndFooters;

/**
 * Class used to obtain the go vocabulary from: ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/UNIPROT/gene_association.goa_uniprot.gz
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class MWTGoWriter {
  
  public static final String CC = "cellular_component";
  public static final String BP = "biological_process";
  public static final String MF = "molecular_function";

  
  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "go";
  public static final String ONTOLOGY_ATTR = "onto";
  public static final int BUFFER_SIZE = 1024 * 8;
  
  
  public static void main (String [] args) throws Exception {
    
    final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out), BUFFER_SIZE);

    // Header
    String header= new StringBuilder()
      .append(HeadersAndFooters.COMMON_HEADER)
      .append("<template><a:")
      .append(TAGNAME)
      .append(" ids=\"%1\" ")
      .append(ONTOLOGY_ATTR)
      .append("=\"%2\">%0</a:")
      .append(TAGNAME)
      .append("></template>\n\n")
      .toString();
    out.write(header);
    
    // Get terms from GO OBO file
    List<GoTerm> goTermList = GoOboParser.parse(System.in);
    
    // Merge term ids by name then by onto: {name} = {{onto} = IdSet}
    Map<String,Map<String,Set<String>>> nameMap = new HashMap<String,Map<String,Set<String>>>();
    for (GoTerm goTerm: goTermList){
      String name = goTerm.getName();
      Map<String,Set<String>> ontoMap = nameMap.get(name);
      if (ontoMap == null){
        ontoMap = new HashMap<String,Set<String>>();
        nameMap.put(name, ontoMap);
      }
      String onto = goTerm.getNamespace();
      Set<String> idList = ontoMap.get(onto);
      if (idList == null){
        idList = new HashSet<String>();
        ontoMap.put(onto, idList);
      }
      idList.add(goTerm.getId());
      List<String> ids = goTerm.getIsAList();
      if (ids != null) idList.addAll(ids);
    }   
    
    // Display
    StringBuilder line = new StringBuilder();
    for (String name: nameMap.keySet()){
      Map<String, Set<String>> ontoMap = nameMap.get(name);
      for (String onto: ontoMap.keySet()){
        line.setLength(0);
        line.append("<t p1=\"");
        for (String id: ontoMap.get(onto)){
          line.append(id).append(",");
        }
        line.setLength(line.length() - 1);
        line.append("\" p2=\"").append(onto).append("\">").append(name).append("</t>\n");
        out.write(line.toString());
      }
    }
    
    out.write(HeadersAndFooters.getMwtFooter());
    out.flush();
    out.close();
  }
}
