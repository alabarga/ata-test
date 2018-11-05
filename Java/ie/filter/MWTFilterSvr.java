package com.ata.ie.filter;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import com.ata.util.CmdLineArgs;
import monq.jfa.DfaRun;
import monq.net.FilterServiceFactory;
import monq.net.Service;
import monq.net.ServiceCreateException;
import monq.net.ServiceFactory;
import monq.net.TcpServer;
import monq.programs.DictFilter;


public class MWTFilterSvr extends MyServer {
  
  private final DictFilter dictFilter;
    
    
  public MWTFilterSvr (final int port, InputStream in, OutputStream out, final DictFilter dictFilter){
    super(port, in, out);
    this.dictFilter = dictFilter;
  }
  
  
  public DfaRun createDfaRun() throws Exception {
    return dictFilter.createRun();
  }
               

  private static final int ARGS_LEN = 4; 
  private static final String USAGE =
    "Usage: com.ata.ie.filter.MWTFilter -port <port for inbound connections> " +
    "-mwtfile <MWT file name>";

  
  public static void main (String [] args) throws Exception {
     
    CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);  
    final int port = Integer.parseInt(cmdLineArgs.getProp("port"));
    final String mwtFileName = cmdLineArgs.getProp("mwtfile");
    
    System.out.println("Reading MWT file: " + mwtFileName);
    BufferedReader mwtFile = new BufferedReader(new FileReader(mwtFileName));
    System.out.println("Loading dictionary in memory, this will take some time...");
    final DictFilter dictFilter = new DictFilter(mwtFile, "elem", MyServer.ROI, false);
    mwtFile.close();
    dictFilter.setInputEncoding(ENCODING);
    dictFilter.setOutputEncoding(ENCODING);
    
    FilterServiceFactory fsf = 
      new FilterServiceFactory(new ServiceFactory (){
        public Service createService (InputStream in, OutputStream out, Object params)
        throws ServiceCreateException {
          return new MWTFilterSvr(port, in, out, dictFilter);
      }
    });

    MyServerSocket mySvrSkt = new MyServerSocket(port, MAX_CONCURRENTS, InetAddress.getLocalHost());
    TcpServer svr = new TcpServer(mySvrSkt, fsf, MAX_CONCURRENTS);
    svr.setLogging(System.out);
    System.out.println("MWTFilter port " + port + ": " + mwtFileName);
    svr.serve();          
  }
}
