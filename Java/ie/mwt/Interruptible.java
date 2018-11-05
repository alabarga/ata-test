package com.ata.ie.mwt;

/**
 * Class used to control the beahior of Runnables. The main loop within the
 * run method can be controlled with the "interrupted" flag. The name does not
 * imply any call to Thread.interrupt (I may just asl well called this flag
 * dontContinue)
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class Interruptible {

  private volatile boolean interrupted;
  
  
  public Interruptible (){ interrupted = false; }
  
  public synchronized void setInterrupted (boolean v){ interrupted = v; }
  
  public synchronized boolean isInterrupted (){ return interrupted; }
  
}
