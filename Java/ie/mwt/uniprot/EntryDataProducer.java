package com.ata.ie.mwt.uniprot;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.ata.ie.mwt.FIFO;
import com.ata.ie.mwt.ICrashable;
import com.ata.ie.mwt.Interruptible;


public class EntryDataProducer extends Interruptible implements Runnable, ICrashable {	  
	
  protected int id;  
  protected String encoding;
  protected FIFO inFifo;
  protected FIFO outFifo;
  protected volatile boolean isDone;
  protected IOException ioe;
  protected RandomAccessFile raf;  

	
  public EntryDataProducer (int id,
                            String fileName, 
  		                      String encoding,
								            FIFO inFifo,
								            FIFO outFifo)
  throws SAXException, ParserConfigurationException, IOException {
  	  	
  	this.id = id;  	
  	this.encoding = encoding;
  	this.raf = new RandomAccessFile(new File(fileName), "r");  	
  	this.inFifo  = inFifo;
  	this.outFifo = outFifo;  	
  	isDone = false;  
  	ioe = null;
  }  
  
  
  public void run (){  	
  	try {  	    	
  	  while (!isInterrupted() || inFifo.hasNext()){
  	    EntryLocation entry = (EntryLocation)inFifo.next();
  	    if (entry != null){
  	      byte [] buffer = new byte[entry.size]; 
          raf.seek(entry.ini);
          int readCount = raf.read(buffer, 0, entry.size);
          ByteArrayInputStream bais 
            = new ByteArrayInputStream(buffer, 0, readCount);          
          EntryData entryData = new EntryParser().parse(bais, encoding);
          entryData.ini = entry.ini;
          entryData.size = entry.size;
          outFifo.add(entryData);          
  	    }
  	  	Thread.yield();  	  	
  	  }
  	}  	  
  	catch (IOException ioe){
      this.ioe = ioe;
    }
  	catch (SAXException saxe){
  	  this.ioe = new IOException(saxe.getMessage());
  	}  	
  	catch (ParserConfigurationException pce){
  	  this.ioe = new IOException(pce.getMessage());
  	}
  	finally {
  	  synchronized (this){
	      isDone = true;	  
  	  }
  	}
  }    
     
  
  public IOException getIoe (){ return ioe; }
  
  
  public synchronized boolean isDone (){ return isDone; }
      
  
  public void close () throws IOException { raf.close(); }
}
