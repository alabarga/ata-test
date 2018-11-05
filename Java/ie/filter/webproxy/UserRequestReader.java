package com.ata.ie.filter.webproxy;

import java.io.IOException;
import java.io.InputStream;

/**
 * The HTTP streamed client contacts the FIRE_WebProxy servlet
 * sending a chunk of text with the following format:
 * <p>
 * pipelineName;lucene query string
 * <p>
 * This class is the parser/validator for the first line
 * 
 * @see UserRequest
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class UserRequestReader {
	
  /**
   * End of Line character
   */
  public static final char EOL = '\n';
  
  /**
   * The name of a pipeline will never exceed 1Mb (or you need to consult your doctor)
   */
	public static final int NAME_SIZE = 1024 * 1024 * 1; // 1 Mb	
	
	
	private InputStream in;
	private StringBuilder tmpsb;
	
	
	/**
	 * This class will read a line from in and attempt to return a UserRequest
	 * @param in
	 */
	public UserRequestReader (InputStream in){
	  this.in = in;
	  tmpsb = new StringBuilder(NAME_SIZE);
	}
	
	
	/**
	 * Will read a line in the form "pipelineName ; the query to be given to the search engine"
	 * @return
	 * @throws IOException
	 */
	public UserRequest getUserRequest () throws IOException {
		// Read the entire first line
	  int cnt=0;	  	  
	  for (int c=-1; -1 != (c=in.read()) && -1 != EOL && cnt < NAME_SIZE; cnt++){
		  tmpsb.append((char)c);
		}	  	 	  
	  if (cnt == 0){
			throw new IOException("Pipeline name expected but not received.");
		}
	  if (cnt >= NAME_SIZE){
			throw new IOException("First line too big. Only " + NAME_SIZE + " characters expected.");
		}		
		
		// Find the ";" separator
		int idxSC = tmpsb.indexOf(";");		
		String pipelineName = (idxSC == -1)? tmpsb.toString().trim() : tmpsb.substring(0, idxSC).trim();
		String queryStr = null;
		if (idxSC != -1){
			if ((idxSC+1) >= tmpsb.length()){				
				throw new IOException("A query is expected in the first line.");
			}			
			queryStr = tmpsb.substring(idxSC+1).trim();			
			if (queryStr == null || queryStr.length() == 0){				
				throw new IOException("A query is expected in the first line.");
			}			
		}
    return new UserRequest(pipelineName, queryStr);				                  
	}
}
