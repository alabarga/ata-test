package com.ata.ie.filter;


import java.io.InputStream;
import com.ata.util.CmdLineArgs;
import monq.net.DistPipeFilter;
import monq.net.PipelineRequest;
import monq.stuff.Pipe;


public class SimpleSVRClient {	
  
  public static final int BUFFER_SIZE = 1024 * 512;

  
	private static final DistPipeFilter pipeClient;
	static {
		try {
      pipeClient = new DistPipeFilter(0, 50);                   
      Runtime.getRuntime().addShutdownHook(new Thread(){
        public void run() { pipeClient.shutdown(); }
      });
      pipeClient.start(); 
		}
		catch (Exception e){
			throw new Error("FIREClient", e);
		}
  }
	
	
	private static final int ARGS_LEN = 4;
	private static final String USAGE = 
	  "SimpleEXAMPLEClient -host <host name> -port <port>";
  
	
	public static void main (String [] args) throws Exception {
	  
	  CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);
	  final String host = cmdLineArgs.getProp("host");
    final int port = Integer.parseInt(cmdLineArgs.getProp("port"));
    
    PipelineRequest request [] = {new PipelineRequest("Example", host, port)};
	  Pipe pipe = new Pipe(BUFFER_SIZE);
	  pipe.setIn(System.in, false);
    InputStream in = pipeClient.open(request, pipe);
    byte [] buffer = new byte [BUFFER_SIZE];
    for (int bread=-1 ;-1 != (bread=in.read(buffer, 0, BUFFER_SIZE)); ){
      System.out.write(buffer, 0, bread);
      System.out.flush();
      if (System.out.checkError()) break;
    }
    pipeClient.close(in);
	}
}
