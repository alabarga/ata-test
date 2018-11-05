package com.ata.ie.filter.webproxy;


/**
 * The HTTP streamed client contacts the FIRE_WebProxy servlet
 * sending a chunk of text with the following format:
 * <p>
 * pipelineName ; lucene query string
 * <p>
 * This class is a place holder for the first line. Its is instantiated 
 * by the FirtsLineParser class
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com) 
 */
public class UserRequest {
	
  private String pipelineName;
  private String queryStr;
  
  
  public UserRequest (String pipelineName, String queryStr){
  	this.pipelineName = pipelineName;
  	this.queryStr = queryStr;
  }
  
    	
	public String getQueryStr (){ return queryStr; }
	
	public void setQueryStr (String queryStr){ this.queryStr = queryStr; }

	public String getPipelineName (){ return pipelineName; }
	
	public void setPipelineName (String name){ this.pipelineName = name; }
	
	public String toString (){
	  return "PipelineName: " + pipelineName + ", queryStr: " + queryStr;
	}
}
