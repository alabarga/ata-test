package com.ata.ie.mwt.uniprot;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class EntryParser extends DefaultHandler	{
	  	  
  protected XMLReader parser;
  protected List <String> accessionKeys;  
  protected List <String> names;  
  protected StringBuilder tmpSb;
  protected boolean inAccession;
  protected boolean inProtein;
  protected boolean inOrganism;
  protected boolean inComment;
  protected boolean inName;
  protected boolean inEntry;
  protected boolean parsedAlready;
  protected EntryData entryData;
  
  
  
  public EntryParser () 
  throws SAXException, ParserConfigurationException, IOException {
  	super();  	
  	SAXParserFactory parserFactory = SAXParserFactory.newInstance();
  	SAXParser saxParser = parserFactory.newSAXParser();  	
  	parser = saxParser.getXMLReader();  	
  	parser.setContentHandler(this);
  	parser.setErrorHandler(this);
  	parser.setDTDHandler(this);
  	parser.setEntityResolver(this);
  	parser.setFeature("http://xml.org/sax/features/validation", false);
  	accessionKeys = new ArrayList<String>();
  	names = new ArrayList<String>();  	
  	tmpSb = new StringBuilder();  	
  	    
  	parsedAlready = false;
    inEntry = false;
    inAccession = false;    
    inProtein = false;     
    inOrganism = false;
    inComment = false;
    inName = false;
    entryData = null;
  }
  
  
  public EntryData parse (InputStream is, String encoding) 
  throws SAXException, IOException {
    if (!parsedAlready){
  	  InputSource source = new InputSource(is);
  	  source.setEncoding(encoding); 
  	  parser.parse(source);
  	  parsedAlready = true;
    }
  	return entryData;
  }
    

  public void startElement (String uri, 
  		                      String localName,
	                          String rawName, 
							              Attributes attr){
  	
  	// ENTRY element -> get date
    if (rawName.equals(Constants.ENTRY) && !inEntry){      
      inEntry = true;  
    }    
    // ACCESSION element -> transition to collect id
    else if (rawName.equals(Constants.ACCESSION) && !inAccession && inEntry){
      inAccession = true;
      tmpSb.setLength(0);
    }
    // PROTEIN element -> transition to collect names
    else if ((rawName.equals(Constants.PROTEIN) || rawName.equals(Constants.GENE)) 
                && 
             !inProtein && inEntry){
      inProtein = true;      
    }
    // ORNANISM element
    else if (rawName.equals(Constants.ORGANISM) && !inOrganism && inEntry){
      inOrganism = true;      
    }
    // COMMENT element
    else if (rawName.equals(Constants.COMMENT) && !inComment && inEntry){
      inComment = true;      
    }
    // NAME element -> transition to collect name
    else if (rawName.equals(Constants.NAME) && 
             !inName && (inProtein || !inOrganism || !inComment) && inEntry){
      inName = true;
      tmpSb.setLength(0);
    }
  }  
  
  
  public void characters (char [] chars, int start, int length){
    // ACCESSION || NAME element
  	if ((inAccession || ((inProtein || (!inOrganism && !inComment)) && inName)) && inEntry){
  	  String name = new String(chars, start, length).trim();
  	  if (name.length() > 0){
  	    tmpSb.append(name);
  	  }
  	}  	  
  }
  
  
  public void endElement (String uri, 
  		                    String localName, 
						              String rawName){
  	
    // ACCESSION element -> transition out of collect id
    if (rawName.equals(Constants.ACCESSION) && inAccession && inEntry){
      inAccession = false;
      accessionKeys.add(tmpSb.toString());
    }
    // NAME element -> transition out of collect name
    else if (rawName.equals(Constants.NAME) && (inProtein || (!inOrganism && !inComment)) && inName && inEntry){			 
      inName = false;
      String name = tmpSb.toString();
      if (name != null && name.length() > 0 && !names.contains(name)) names.add(name);
    }
    // PROTEIN element -> transition out of collect names
    else if ((rawName.equals(Constants.PROTEIN) || rawName.equals(Constants.GENE))
                && inProtein && inEntry){
      inProtein = false;      
    }
    // ORGANISM element
    else if (rawName.equals(Constants.ORGANISM) && inOrganism && inEntry){
      inOrganism = false;      
    }
    // COMMENT element
    else if (rawName.equals(Constants.COMMENT) && inComment && inEntry){
      inComment = false;      
    }
    // ENTRY
    else if (rawName.equals(Constants.ENTRY) && inEntry){
      inEntry = false;      
      entryData = new EntryData(accessionKeys, names);
    }
  }     

  
  public static void main (String [] args) throws Exception {      
    if (args.length < 1){
      System.err.println("Syntax: SynonymFinder inputFile1 .. inputFile2");
      System.exit( -1 );
    }  
      
  	EntryParser parser = new EntryParser();
  	for (int i=0; i<args.length; i++){
  	  System.out.println("File " + args[i] + ":");
	    EntryData d = parser.parse(new FileInputStream(args[i]), "UTF-8");
	    System.out.println(d);
  	}	
  }
}
