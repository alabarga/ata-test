package com.ata.ie.filter.webproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import monq.net.DistPipeFilter;
import monq.net.PipelineRequest;
import monq.stuff.CharSequenceFeeder;
import monq.stuff.Pipe;
import com.ata.ie.filter.MyServer;
import com.ata.ir.search.Input;
import com.ata.util.CmdLineArgs;
import com.ata.util.conf.PipelineRedezvous;

/**
 * This class is in charge for providing access to the undelying FIRE filter
 * servers. It is used by the FIRE_WebProxy Jetty server but can be used 
 * from any java program or as a stand alone command.
 * <p>
 * From a Java program:
 * <p>
 * <ol>
 *   <li>InputStream is = FIREClient.open("pipelineName", "query String");</li>
 *   <li>FIREClient.copy(is, System.out);</li>
 *   <li>FIREClient.close(is);</li>
 * </ol>
 * From the command line:
 * <p>
 *   echo "text" | java com.ata.ie.filter.webproxy.FIREClient -pipe pipelineName
 * <p>
 * Where text really depends on the servers stated in the pipelineName.pipe file.
 * It the first server is Scotty, then "text" should be a query in the Lucene 2.0
 * syntax. It the first server is any other server then "text" should be in the
 * form:
 * <p>
 * &lt;text&gt;your text goes here &lt;/text&gt;
 * <p>
 * The "&lt;text&gt;" tags are defined at: com.ata.ie.filter.MyServer
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class FIREClient {	
  
  public static final String FILTER_SERVERS_SYSPROP = "filter.servers";

  public static final int BUFFER_SIZE = 1024 * 512;
  public static final String DEFAULT_CONFIG_FILE_PATH = "../conf/filterServers.xml";

  
	private static final DistPipeFilter pipeClient;
	private static final PipelineRedezvous pipelines;
	static {
		try {
      pipeClient = new DistPipeFilter(0, 50);                   
      Runtime.getRuntime().addShutdownHook(new Thread(){
        public void run() { pipeClient.shutdown(); }
      });
      pipeClient.start();
      String filterServersFilePath = System.getProperty(FILTER_SERVERS_SYSPROP);
      if (filterServersFilePath == null || filterServersFilePath.length() == 0){
        System.err.println("You should define the system property -D" + FILTER_SERVERS_SYSPROP);
        System.err.println("Using default path to filter servers config file " + DEFAULT_CONFIG_FILE_PATH);
        filterServersFilePath = DEFAULT_CONFIG_FILE_PATH;
      }
      else {
        System.out.println("Using filter servers configuration from file " + filterServersFilePath);
      }
      pipelines = new PipelineRedezvous(filterServersFilePath);
		}
		catch (Exception e){
			throw new Error("FIREClient", e);
		}
  }
	   

	public static InputStream open (String pipelineName, String queryStr)
	throws IOException{
		PipelineRequest [] request = pipelines.getRequestFor(pipelineName);
		if (request == null || request.length == 0){   
      throw new IOException("Invalid pipeline name " + pipelineName);
    }
	  return pipeClient.open(request, new CharSequenceFeeder(queryStr, MyServer.ENCODING));
	}
	
	
	public static void copy (InputStream in, OutputStream out, boolean printCopyToStderr) throws IOException {
	  if (printCopyToStderr){
      System.err.println("Printing copy to STDERR for debuggin purposes");
    }
	  byte [] buffer = new byte [BUFFER_SIZE];
    for (int bread=-1 ;-1 != (bread=in.read(buffer, 0, BUFFER_SIZE)); ){
      out.write(buffer, 0, bread);
      out.flush();
      if (printCopyToStderr){
        System.err.write(buffer, 0, bread);
        System.err.flush();
      }
      if (System.out.checkError()) break;
    }
    out.flush();
    if (printCopyToStderr){
      System.err.flush();
    }
	}
	
	
	public static void close (InputStream input) throws IOException{
		pipeClient.close(input);
	}	
	
	
	public static final void contact (InputStream in, OutputStream out) throws Exception {
    
	  InputStream input = null;
	  Exception excpt = null;
	  try {
	    UserRequestReader userRequestReader = new UserRequestReader(in);
	    UserRequest userRequest = userRequestReader.getUserRequest();
	    input = open(userRequest.getPipelineName(), userRequest.getQueryStr());
  
	    // Check whether there are results 
	    Input statusChecker = new Input(input); 
	    StringBuffer tmpsb = new StringBuffer();
	    int hitCount = statusChecker.checkStatus(tmpsb);
	    if (hitCount == 0){
	      tmpsb.append("\n").append(userRequest.getQueryStr());
	      throw new IOException(tmpsb.toString());
	    }     
	    out.write(tmpsb.toString().getBytes());

	    // Return results
	    copy(input, out, false);
	  }
	  catch (Exception e){
	    excpt = e;
	  }
	  finally {
      if (input != null) close(input);
      if (excpt != null) throw excpt;
	  }
  }
  
	
	private static final int ARGS_LEN = 2;
  private static final String USAGE = 
    "com.ata.ie.web.proxy -pipe <pipeline name>";

  public static void main (String [] args) throws Exception {
	  
	  CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);
    final String pipeName = cmdLineArgs.getProp("pipe");
    PipelineRequest request [] = pipelines.getRequestFor(pipeName);
    if (request == null || request.length == 0){   
      throw new IOException("Invalid pipeline name " + pipeName);
    }
    Pipe pipe = new Pipe(BUFFER_SIZE);
    pipe.setIn(System.in, false);
    InputStream in = pipeClient.open(request, pipe);
    copy(in, System.out, false);
    pipeClient.close(in);
	}
}
