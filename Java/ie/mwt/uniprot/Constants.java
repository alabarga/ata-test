package com.ata.ie.mwt.uniprot;


public interface Constants {
	
  public static final int SWISSPROT_SIZE = 300000; // Entries roughly
  public static final int MAX_THREADS = 5;
  public static final int FIFO_MAX_SIZE = SWISSPROT_SIZE / MAX_THREADS;
  
     
  public static final String STAG = "<entry";	
  public static final String ETAG = "</entry>";
  public static final String UNIPROT_ID_SEP = ",";
  public static final String ENTRY     = "entry";	
  public static final String ACCESSION = "accession";
  public static final String PROTEIN   = "protein";
  public static final String ORGANISM  = "organism";
  public static final String GENE      = "gene";
  public static final String NAME      = "name";
  public static final String COMMENT   = "comment";  
}
