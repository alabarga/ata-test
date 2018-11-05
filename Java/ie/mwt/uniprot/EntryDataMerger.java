package com.ata.ie.mwt.uniprot;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ata.ie.mwt.FIFO;
import com.ata.ie.mwt.ICrashable;
import com.ata.ie.mwt.Interruptible;


public class EntryDataMerger extends Interruptible implements Runnable, ICrashable {
	
  private FIFO [] fifoArray;
  private Map<String, List<String>> map; // {accession1,accession2,...} -> List<String>
  private IOException ioe;
  
  
  public EntryDataMerger (FIFO [] fifoArray){
  	this.fifoArray = fifoArray;
  	map = new HashMap<String,List<String>>(); 
  	ioe = null;
  }   
  
  
  protected final boolean stillDataInFifos(){     
    for (int j=0; j < Constants.MAX_THREADS; j++){             
      if (!fifoArray[j].isProducerDone() || fifoArray[j].hasNext()){
        return true;
      }
    }      	    
    return false;
  }
  
  
  public void run (){
    long fifoRRSelector=0; 
    while (!isInterrupted() || stillDataInFifos()){
      // Select source FIFO, Round Robin
      int fifoIdx = 0;      	      
      int j = 0;      	
      for ( ; j<Constants.MAX_THREADS; j++){
        // Choose the next RR FIFO which has contents      	    
        fifoIdx = (int)((fifoRRSelector ++) % Constants.MAX_THREADS);      	  
        if (fifoArray[fifoIdx].hasNext()) break;      	    
      }      	      	
      if (j >= Constants.MAX_THREADS && !isInterrupted()){
        try { Thread.sleep(200); } catch (InterruptedException ie){ /**/ }
        continue;
      }      

      EntryData entry = (EntryData)fifoArray[fifoIdx].next();        
      if (entry != null){                      
        String accKey = entry.getAccessionKey();
        List<String> names = map.get(accKey);
        if (names == null){
          map.put(accKey, entry.getNames());
        }
        else {
          for (String name: entry.getNames()){
            if (!names.contains(name)) names.add(name);
          }
        }
      }      	  	  
    }
  }
    
  
  public Map<String, List<String>> getEntryMap (){ return map; }  

  
  public IOException getIoe (){ return ioe; }
}
