package com.ata.ie.mwt;

import java.util.ArrayList;
import java.util.List;
import com.ata.ie.mwt.uniprot.Constants;

/**
 * A FIFO (First In First Out) queue to which one producer writes and from which
 * many consumers read. The writer is a high performance process that takes almost
 * no time to produce an element. The readers usually need to do some analysis tasks
 * over the data and hence take a long time, this being the reason to need many
 * consumers to keep the system performant. This class is mostly used by the process
 * that creates the MWT file for the Swissprot filter server 
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */


public final class FIFO implements IReporter {
	              
  protected List fifoList;  
  protected String fifoName;
  protected int fifoUniqueId;
  protected boolean isProducerDone;
  protected int highWaterMark;
  protected long entryCount;
   
  /**
   * @param name Name of the producer
   * @param id ID of the queue
   */
  public FIFO (String name, int id){
    this.fifoName = name;
    this.fifoUniqueId = id;
  	fifoList = new ArrayList();
  	highWaterMark = 0;
  	entryCount = 0;
  	isProducerDone = false;
  }
  
  
  /**
   * Sets whether the producer is done
   * @param v True or False
   */
  public synchronized void setIsProducerDone (boolean v){ 
    isProducerDone = v; 
  }
  
  
  /**
   * @return Whether the producer is done
   */
  public synchronized final boolean isProducerDone (){ 
    return isProducerDone; 
  }
    
  
  /**
   * @return Whether there is an element available to be read from the FIFO
   */
  public synchronized boolean hasNext (){ 
  	return fifoList.size() > 0; 
  }
  

  /**
   * Prints a report on the usage of the FIFO
   */
  public void printReport (){
  	System.err.println("FIFO " + fifoName + "_" + fifoUniqueId + " report:");
  	System.err.println("\t" + entryCount + " total throughput.");
  	System.err.println("\t" + highWaterMark + " max entries at once.");
  }
          
  
  /**
   * @param obj The object to be added to the FIFO
   */
  public synchronized void add (Object obj){
  	int fifoSize = fifoList.size();
  	while (fifoSize >= Constants.FIFO_MAX_SIZE){  	  
  	  try { wait(); } catch (InterruptedException e){ /**/ }
  	}
  	fifoList.add(obj);
  	entryCount ++;	
  	if (++fifoSize > highWaterMark) highWaterMark = fifoSize;
  	notifyAll();
  }
  
  
  /**
   * @return The first available element from the FIFO
   */
  public synchronized Object next (){  	  	 
  	Object obj = null;
  	if (fifoList.size() > 0) obj = fifoList.remove(0);
  	notifyAll();
  	return obj;
  }
}
