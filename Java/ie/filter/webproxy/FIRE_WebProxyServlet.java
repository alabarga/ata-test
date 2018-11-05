package com.ata.ie.filter.webproxy;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is a servlet that provides access
 * to FIRE using streamed HTTP. This is the server side which needs to be
 * exposed to the external world via a URL (http://www.your_domain.com:port)
 * outside the firewall. There will be a number of threads to respond to user
 * requests in the form:
 * <p>
 * pipelineName ; query ()
 * <p>
 * The client side comes implemented in the clasee HttpFIREClient
 * <p>
 * For this to work on Tomcat, the conf and dtds folders need to be accessible 
 * by the FIREClient class. The easiest is to create a couple of links from the
 * root of the Tomcat installation:
 * <p>
 * <ol>
 * <li>cd /path/to/apache-tomcat-A.B.C</li>
 * <li>ln -s  /path/to/atalab/conf conf</li>
 * <li>ln -s  /path/to/atalab/dtds dtds</li>
 * </ol>
 * 
 * @see HttpFIREClient
 * @see FIREClient
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class FIRE_WebProxyServlet extends HttpServlet {
  
  public static final String CONTENT_TYPE = "text/plain; charset=UTF-8";
  
   
  /**
   * HTTP GET method
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {  	
  	try {
    	response.setContentType(CONTENT_TYPE);    	
    	response.setHeader("Transfer-Encoding", "chunked");
    	response.setHeader("HTTP-Version", "HTTP/1.1"); 
    	response.setBufferSize(FIREClient.BUFFER_SIZE);
    	response.setStatus(HttpServletResponse.SC_OK);
      FIREClient.contact(request.getInputStream(),response.getOutputStream());
  	}
  	catch (Exception e){
  		response.setContentType("text/xml; charset=UTF-8");
    	response.setHeader("Transfer-Encoding", "text/xml");
    	response.setStatus(HttpServletResponse.SC_OK);
 			ServletOutputStream out = response.getOutputStream();
 			out.println("<StackTrace>");
 			out.println("<message>" + e + "</message>");
 			out.println("</StackTrace>");
 			e.printStackTrace();
  	}    
  }
 
  
  /**
   * HTTP POST method
   */
  public void doPost (HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {
    doGet(request, response);
  }
}
