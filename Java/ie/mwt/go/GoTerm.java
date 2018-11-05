package com.ata.ie.mwt.go;


import java.util.ArrayList;
import java.util.List;


public class GoTerm {
  
  public static final String ID_KEY = "id";
  public static final String NAME_KEY = "name";
  public static final String NAMESPACE_KEY = "namespace";
  public static final String SYNONYM_KEY = "synonym";
  public static final String ISA_KEY = "is_a";
  public static final String IS_OBSOLETE_KEY = "is_obsolete";
  
  public static final String TRUE_VALUE = "true";

  
  private String id;
  private String namespace; // Ontology branch
  private List<String> isAList;
  private String name;
  private List<String> synonymList;

  
  public GoTerm (List<KeyVal> attrList) throws Exception {
    for (KeyVal kv: attrList){
      if (kv.equals(ID_KEY)){
        id = kv.getValue();
      }
      else if (kv.equals(NAMESPACE_KEY)){
        namespace = kv.getValue();
      }
      else if (kv.equals(NAME_KEY)){
        name = kv.getValue();
      }
      else if (kv.equals(ISA_KEY)){
        addIsA(kv.getValue());
      }
      else if (kv.contains(SYNONYM_KEY)){
        addSynonym(kv.getValue());
      }
    }
    
    if (id == null || name == null){
      throw new Exception("Corrupted GO file");
    } 
  }
  
  public String toString (){
    StringBuilder sb = new StringBuilder();
    sb.append("ID: ").append(id)
      .append("\nName: ").append(name)
      .append("\nNameSpace: ").append(namespace);
    if (synonymList != null){
      sb.append("\nSynonyms:");
      for (String s: synonymList){
       sb.append("\n\t").append(s); 
      }
    }
    if (isAList != null){
      sb.append("\nIsA:");
      for (String isa: isAList){
       sb.append("\n\t").append(isa); 
      }
    }
    return sb.toString();
  }
  
  public String getId (){ return id; }
  
  public String getNamespace (){ return namespace; }
  
  public String getName (){ return name; }
  
  private void addIsA (String isA){
    if (isAList == null){
      isAList = new ArrayList<String>();
    }
    int idx = isA.indexOf('!');
    if (idx != -1){
      isA = isA.substring(0, idx).trim();
    }
    isAList.add(isA);
  }
  
  public List<String> getIsAList (){ return isAList; }
  
  private void addSynonym (String synonym){
    if (synonymList == null){
      synonymList = new ArrayList<String>();
    }
    synonymList.add(synonym);
  }
  
  public List<String> getSynonym (){ return synonymList; }
}
