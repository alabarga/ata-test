package com.ata.ie.filter.webproxy;


import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.thread.BoundedThreadPool;
import com.ata.util.CmdLineArgs;

/**
 * This class is a Jetty server containing a servlet that provides access
 * to FIRE using streamed HTTP. This is the server side which needs to be
 * exposed to the external world via a URL (http://www.your_domain.com:port)
 * outside the firewall. There will be a number of threads to respond to user
 * requests in the form:
 * <p>
 * pipelineName ; query ()
 * <p>
 * The client side comes implemented in the clasee HttpFIREClient
 * 
 * @see HttpFIREClient
 * @see FIREClient
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class FIRE_WebProxy {
  
  private static final int ARGS_LEN = 4;
  private static final String USAGE =
    "Usage: com.ata.ie.web.proxy.FIRE_WebProxy -port <port for inbound connections> " +
    "-maxthreads <max number of threads to process requests>";
  
  
  /**
   * Usage: com.ata.ie.web.proxy.FIRE_WebProxy 
   *   -port <port for inbound connections>
   *   -maxthreads <max number of threads to process requests>
   * <p>
   * Pretty self explanatory
   * 
   * @param args
   * @throws Exception
   */
  public static void main (String [] args) throws Exception {
    
    CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);
    final int port = Integer.parseInt(cmdLineArgs.getProp("port"));
    final int maxthreads = Integer.parseInt(cmdLineArgs.getProp("maxthreads"));
    
    Server server = new Server();
 
    // Thread Pool
    BoundedThreadPool threadPool = new BoundedThreadPool();
    threadPool.setMaxThreads(maxthreads);
    server.setThreadPool(threadPool);
    
    // Connector
    Connector connector = new SocketConnector();
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    
    // Handler
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping("com.ata.ie.filter.webproxy.FIRE_WebProxyServlet", "/");
    server.setHandler(handler);
    
    server.start();
    server.join();
  }
}
