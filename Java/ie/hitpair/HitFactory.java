package com.ata.ie.hitpair;


import java.util.Map;
import com.ata.ie.mwt.chebi.MWTChebiWriter;
import com.ata.ie.mwt.disease.MWTDiseaseWriter;
import com.ata.ie.mwt.drug.MWTDrugWriter;
import com.ata.ie.mwt.go.MWTGoWriter;
import com.ata.ie.mwt.symptom.MWTSymptomWriter;
import com.ata.ie.mwt.taxonomy.MWTTaxonomyWriter;
import com.ata.ie.mwt.uniprot.MWTUniprotWriter;

/**
 * This class takes care of building the Hits from the pieces of text coming 
 * from FIRE. If you need to add any semantic type this is the place. In
 * particular a Hit belongs to a semantic type which has a name such as 
 * uniprot, drug, chemical, chebi, species and can be found in the annotated
 * text preceeded by "a:" such in "a:uniprot". Additionaly a semantic type can
 * be linked (or should be linkable for that matter) to a URL which acts as a
 * front end to a database containing further information about the particular
 * instance of the semantic type.
 * 
 * @see Hit
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HitFactory {

  public static final String UNIPROT  = MWTUniprotWriter.TAGNAME;  
  public static final String GO       = MWTGoWriter.TAGNAME;  
  public static final String GO_CC    = MWTGoWriter.CC;
  public static final String GO_BP    = MWTGoWriter.BP;
  public static final String GO_MF    = MWTGoWriter.MF;
  public static final String CHEBI    = MWTChebiWriter.TAGNAME;
  public static final String DRUG     = MWTDrugWriter.TAGNAME;
  public static final String SPECIES  = MWTTaxonomyWriter.TAGNAME;
  public static final String DISEASE  = MWTDiseaseWriter.TAGNAME;
  public static final String SYMPTOM  = MWTSymptomWriter.TAGNAME;
  
  
  // http://www.ebi.uniprot.org/uniprot-srv/elSearch.do?querytext={@ids}
  public static final String UNIPROT_URL_START 
    = "http://www.ebi.uniprot.org/uniprot-srv/elSearch.do?querytext=";
  public static final String UNIPROT_URL_END = ""; 
  
  // href="http://www.ebi.ac.uk/ego/DisplayGoTerm?selected={@ids}"
  public static final String GO_URL_START   
    = "http://www.ebi.ac.uk/ego/DisplayGoTerm?selected=";  
  public static final String GO_URL_END = "";
   
  // href="http://www.nlm.nih.gov/medlineplus/druginfo/uspdi/{@ids}.html"
  public static final String DRUG_URL_START   
    = "http://www.nlm.nih.gov/medlineplus/druginfo/uspdi/";
  public static final String DRUG_URL_END = ".html";
  
  // href="http://www.healthcentral.com/ency/408/{@ids}.html"
  public static final String DISEASE_URL_START   
    = "http://www.healthcentral.com/ency/408/";
  public static final String DISEASE_URL_END = ".html";
  
  // href="http://www.healthcentral.com/ency/408/{@ids}.html"
  public static final String SYMPTOM_URL_START   
    = "http://www.healthcentral.com/ency/408/";
  public static final String SYMPTOM_URL_END = ".html";

  // http://www.ncbi.nih.gov/Taxonomy/Browser/wwwtax.cgi?id={@ids}&lvl=0
  public static final String UMLS_URL_START   
    = "http://www.ncbi.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=";
  public static final String UMLS_URL_END = "&lvl=0";    
     

  /**
   * Builds the unique key for the Hit
   * 
   * @param id
   * @param type
   * @return id + type
   */
  public static String buildKey (String id, String type){ 
    return id + type; 
  }

  
  /**
   * Utility private method to create a Hit. A Hit is only created once, 
   * therefore the need for a public buildHit method which checks whether the
   * Hit already exists in a Hit HashMap.
   *  
   * @param key  The concatenation of id + type
   * @param id   Primary key in a public domain database
   * @param type The semantic type, such as uniprot 
   * @param onto For the particular case of the Gene Ontology, which generic 
   *             type is "go" but is divided in three sub branches, 
   *             "Cellular Component", "Molecular Function" and 
   *             "Biological Process"
   * @return The Hit
   */  
  private static Hit buildHit (String key, String id, String type, String onto){
    String theType = type.equals(GO)? onto : type;    
    return new Hit(key, id, theType);
  }     

  
  /**
   * The Hit is only built if it is not present already in the hitHash. All
   * synonims of the Hit are kept in a list.
   * 
   * @param hitHash Map addressed by the Hit's unique key and of base type Hit
   * @param zId     Unique id of the Hit in a public domain database
   * @param zType   The semantic type
   * @param zGOnto  The sub branch in the gene ontology
   * @param zName   The particular instance of the name found in the text
   * @return        The unique key of the Hit
   */
  public static String buildHit (Map<String,Hit> hitHash, 
  		                           String zId, 
								                 String zType,
							 	                 String zGOnto, 
								                 String zName){
  	
  	String tmpKey = buildKey(zId, zType);    
    Hit hit = hitHash.get(tmpKey);    
    if (hit == null){        	
      hit = buildHit(tmpKey, zId, zType, zGOnto);                            
      hitHash.put(tmpKey, hit);
    }            
    hit.addName(zName);         
    return tmpKey;
  }
}
