package com.ata.ie.mwt;

/**
 * Classes implementing this interface are supposed to be Runnables which method
 * run throws an IOException captured within a catch block and assigned 
 * to an internal variable accessible via the method getIoe
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public interface ICrashable {
	
  /**
   * @return The IOException
   */
  public java.io.IOException getIoe ();
	  	 
}
