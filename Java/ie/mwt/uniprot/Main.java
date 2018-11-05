package com.ata.ie.mwt.uniprot;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.ata.ie.mwt.FIFO;


public class Main {

  public static void main (String [] args) throws Exception {  	
	  	
  	if (args.length != 2){
      System.err.println("Syntax: Main iFile(uniprot_sprot.xml) oFile(swissprot.mwt)");
      System.exit( -1 );
    }
	  	
  	// ========= F I F O S =========  	
  	FIFO [] entryLocationFifos = new FIFO [Constants.MAX_THREADS];  	
  	FIFO [] entryDataFifos = new FIFO [Constants.MAX_THREADS];
  	for (int i=0; i < Constants.MAX_THREADS; i++){
  	  entryLocationFifos[i] = new FIFO("EntryLocator", i);  	  
	    entryDataFifos[i] = new FIFO("EntryData", i);	    
  	}  	  	   	
  	
  	// ========    E N T R Y    L O C A T O R:    P R O D U C E R ========
  	// Thread that fills up the FIFOs entryLocationFifos
  	EntryLocator entryLocator 
	    = new EntryLocator(args[0], 
	  	                   Constants.STAG, 
						             Constants.ETAG, 
						             entryLocationFifos); // <== an array of fifos
  	Thread entryLocatorThread = new Thread(entryLocator,"EntryLocator");
  	entryLocatorThread.setDaemon(true);
  	entryLocatorThread.setPriority(Thread.MIN_PRIORITY);
  	entryLocatorThread.start();
  	//System.err.println("[EntryLocator running.]");
		  	 
  	// = P R O C E S S O R S:    C O N S U M E R S    /    C O N S U M E R S = 
  	// The processors read from the entryLocationFifos FIFOs
  	EntryDataProducer [] entryDataProducerArray 
	    = new EntryDataProducer[Constants.MAX_THREADS];
  	for (int i=0; i < Constants.MAX_THREADS; i++){
  		entryDataProducerArray[i] = new EntryDataProducer(i, args[0], 
  	  		                                              "UTF-8", 
											                                  entryLocationFifos[i], 
											                                  entryDataFifos[i]);  	  
  	  Thread processorThread 
	      = new Thread(entryDataProducerArray[i], "EntryDataProducer" + i);
  	  processorThread.setDaemon(true);
  	  processorThread.setPriority(Thread.NORM_PRIORITY);
  	  processorThread.start();  	
  	  //System.err.println("[EntryDataProducer" + i + " running.]");
  	}
  	
  	// ========= E N T R Y D A T A M E R G E R:    C O N S U M E R  =========
  	EntryDataMerger entryDataMerger = new EntryDataMerger(entryDataFifos); 
  	Thread entryDataMergerThread 
	    = new Thread(entryDataMerger, "EntryDataMerger");
  	entryDataMergerThread.setDaemon(true);
  	entryDataMergerThread.setPriority(Thread.MAX_PRIORITY);
  	entryDataMergerThread.start();
  	//System.err.println("[EntryDataMerger running.]");
  	  	
  	System.err.println("[System running. Depending on your machine it can take over 10 min.]");
  	  	
  	
  	// ======== W A I T    U N T I L    D O N E ========
  	// The EntryLocator needs to finish before anything
  	entryLocatorThread.join();
  	//System.err.println("[EntryLocator finished.]");
  	IOException ioe = entryLocator.getIoe();
  	if (ioe != null) ioe.printStackTrace();
  	entryLocator.printReport();
  	entryLocator = null;
  	
  	// Tell the EntryDataProducers that the E is done
  	for (int i=0; i < Constants.MAX_THREADS; i++){
  	  entryDataProducerArray[i].setInterrupted(true);
  	  //System.err.println("[EntryDataProducer" + i + " interrupted.]");
  	}  	  	
  	// Wait for the processors to finish
  	int entryDataProducerAliveCount = Constants.MAX_THREADS;  	
  	while (entryDataProducerAliveCount > 0){
  	  for (int i=0; i < Constants.MAX_THREADS; i++){
  	  	if (entryDataProducerArray[i] != null && entryDataProducerArray[i].isDone()){
  	  	  //System.err.println("[EntryDataProducer" + i + " finished.]");
  	  	  entryDataProducerArray[i].close();  	  	   	  	    	  	   	  	   	  	  
  	  	  ioe = entryDataProducerArray[i].getIoe();
  	  	  if (ioe != null) ioe.printStackTrace();
  	  	  entryDataFifos[i].setIsProducerDone(true);  	  	  
  	  	  entryDataProducerArray[i] = null;
  	  	  entryDataProducerAliveCount --;
  	  	}  	    
  	  }
  	  try { Thread.sleep(200); } catch (InterruptedException e){ /**/ }
  	}
  	
  	// Tell the EntryDataMerger that the processors are done
  	entryDataMerger.setInterrupted(true);
  	//System.err.println("[EntryDataMerger interrupted.]");  	
  	// Wait for the EntryDataMerger to finish
  	entryDataMergerThread.join();
  	//System.err.println("[EntryDataMerger finished.]");
  	ioe = entryDataMerger.getIoe();
  	if (ioe != null) ioe.printStackTrace();  	  

  	
  	// ======== P R O C C E S S    N A M E S    M A P ========
    // {accession1,accession2,...} = List<String>
  	Map<String, List<String>> map = entryDataMerger.getEntryMap();
  	new MWTUniprotWriter(args[1]).writeFile(map);
  }
}
