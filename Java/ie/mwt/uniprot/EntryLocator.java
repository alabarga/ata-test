package com.ata.ie.mwt.uniprot;

import java.io.IOException;
import com.ata.ie.mwt.FIFO;
import com.ata.ie.mwt.ICrashable;
import com.ata.ie.mwt.IReporter;
import com.ata.util.ByteSource;

/**
 * Given an input file it goes through it and looks for the offsets from the 
 * beginning of the file for the tags "startTag" and "endTag" (passed as 
 * parameters). 
 * 
 * The result is a list of newline separated pairs of longs written to the 
 * default output stream. Each pair represents an "entry" element in the original
 * file. The first long is the offset from the start of the file to the alluded 
 * tag (first byte). The second is the size of the entry in bytes, so that added 
 * to the first value tells the offset to the end of the alluded 
 * tag (last byte).
 * 
 * Input: startTag endTag file path/name.
 * Output format: (long long\n)*
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class EntryLocator implements Runnable, ICrashable, IReporter {     
	
  protected ByteSource bs;
  protected byte [] stag;
  protected byte [] etag;
  protected long entryCount;
  protected IOException ioe;
  protected FIFO [] fifoArray;
  
	
  public EntryLocator (String fileName, 
  		                 String sTag, 
                       String eTag,
                       FIFO [] fifoArray)
  throws IOException {
  	
  	bs = new ByteSource(fileName);  	
  	stag = sTag.getBytes();
  	etag = eTag.getBytes();
  	this.fifoArray = fifoArray;
  	entryCount = 0;
  	ioe = null;  	
  }
  
	
  public void run (){
  	
  	int stagCount = 0;
  	int etagCount = 0;
  	long startByte = 0;
  	long endByte = 0;
  	byte currentByte = 0;
  	int tagIdx = 0;  	
  	
  	try {
  	  while (-1 != currentByte){  	
        // Find stag
  	    tagIdx = 0;  	    
        while (-1 != (currentByte=bs.read())){
  	      if (currentByte == stag[tagIdx]){  	  	
  	        tagIdx++;
  	  	    if (tagIdx == stag.length){  	  	  
  	  	      startByte = bs.getOffset() - stag.length + 1;
  	  	      stagCount ++;
  	  	      break;
  	  	    }
  	      }
  	      else tagIdx = 0;
  	    }
  	  	    
  	    // Find etag
        tagIdx = 0;
  	    while (-1 != (currentByte=bs.read())){
  	      if (currentByte == etag[tagIdx]){  	  	
  	        tagIdx++;
  	  	    if (tagIdx == etag.length){  	  	  
  	  	      endByte = bs.getOffset() + 1;
  	  	      // Select FIFO round robin
  	  	      int fifoIdx = etagCount % Constants.MAX_THREADS;
  	  	      fifoArray[fifoIdx].add(new EntryLocation(startByte, endByte));  	  	      
  	  	      etagCount ++;
  	  	      break;
  	  	    }
  	      }
  	      else tagIdx = 0;
  	    }
  	  }  	
  	  bs.close();
  	}
  	catch (IOException ioe){
  	  this.ioe = ioe;
  	}
  	finally {
  	  if (ioe != null) return;  	
  	  if (stagCount != etagCount){
  	    ioe = new IOException("stagCount != etagCount.");  	  
  	  }  	    	  	
  	  entryCount = stagCount;
  	}
  }
  
  
  public IOException getIoe (){ return ioe; }
  
  
  public void printReport(){    
    System.err.println("EntryLocator Report: " + entryCount + " entries.");    
  }  
}
