package com.ata.ie.mwt.chebi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import com.ata.ie.mwt.HeadersAndFooters;

/**
 * Class used to obtain the go vocabulary from: ftp://ftp.ebi.ac.uk/pub/databases/chebi/Flat_file_tab_delimited/compounds.csv 
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class MWTChebiWriter {

  /**
   * Semantic type for this filter server (&lt;a:TAGNAME&gt;Named entity&lt;/a:TAGNAME&gt;)
   */
  public static final String TAGNAME = "chebi";


  public static final int BUFFER_SIZE = 1024 * 8;
  public static final String SEPARATOR = "\t";
  public static final int ID_FIELD = 2;
  public static final int NAME_FIELD = 5;


  public static void main (String [] args) throws Exception {

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in), BUFFER_SIZE);
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out), BUFFER_SIZE);

    // Header
    out.write(HeadersAndFooters.getMwtHeader(TAGNAME));

    StringBuilder mwtLine = new StringBuilder(); 

    for (String line=null; null != (line=in.readLine()); ){

      String [] fields = line.split(SEPARATOR);

      if (fields.length < NAME_FIELD) continue;
      String id = fields[ID_FIELD];
      String name = fields[NAME_FIELD];
      if (id == null || name == null || name.equals("null")) continue;
      mwtLine.setLength(0);
      mwtLine.append("<t p1=\"")
        .append(id)
        .append("\">")
        .append(name)
        .append("</t>\n");
      out.write(mwtLine.toString());
    }

    out.write(HeadersAndFooters.getMwtFooter());
    in.close();
    out.flush();
    out.close();
  }
}
//Eof - Alberto Labarga (alberto.labarga@gmail.com)