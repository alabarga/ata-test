package com.ata.ie.filter;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import monq.jfa.DfaRun;
import monq.jfa.ReaderCharSource;
import monq.net.Service;


public abstract class MyServer implements Service {
  
  public static final String ROI_PROP = "roi";
  public static final String ENCODING = "UTF-8";
  public static final int MAX_CONCURRENTS = 100;

  public static String ROI = null;
  static {
    String roiProp = System.getProperty(ROI_PROP);
    if (roiProp == null || roiProp.length() <= 0){
      throw new Error(
        "You need to define (java -D...) a system property '" + ROI_PROP + "' " +
        "containing the region of interest tag delimiter (i.g.text)"
      );
    }
    ROI = roiProp;
  }
    
  protected final int port;
  protected InputStream in;
  protected OutputStream out;
  protected Exception exception;
  
  
  public abstract DfaRun createDfaRun() throws Exception;
  
    
  public Exception getException (){ 
    return exception; 
  }
    
    
  public MyServer (final int port, InputStream in, OutputStream out){
    this.port = port;
    this.in = in; 
    this.out = out; 
  }
               

  public void run (){   
    PrintStream outpw = null;
    try {
      outpw = new PrintStream(out, false, ENCODING);
    }
    catch (UnsupportedEncodingException e){
      outpw = new PrintStream(out, false);
    }
    
    if (!in.markSupported()){
      in = new BufferedInputStream(in);
    }
    ReaderCharSource charSource = null;   
    try {
      charSource = new ReaderCharSource(in, ENCODING);
    }
    catch (UnsupportedEncodingException e){
      charSource = new ReaderCharSource(in);
    }
    
    try {
      DfaRun dfaRun = createDfaRun();
      dfaRun.setIn(charSource);
      dfaRun.filter(outpw);                  
    }
    catch (Exception e){
      StringBuilder buffer = new StringBuilder();
      buffer.append("<MyServer port=\"")
        .append(port)
        .append("\">");      
      buffer.append(e.getMessage());
      for (int i=0; i<buffer.length(); i++){
        if (buffer.charAt(i) == '\n') buffer.replace(i, i+1, "<br />");        
      }                     
      buffer.append("</MyServer>\n");
      outpw.print(buffer);
      outpw.flush();
      exception = new Exception(buffer.toString(), e); 
      e.printStackTrace();
    }
  }  
}
