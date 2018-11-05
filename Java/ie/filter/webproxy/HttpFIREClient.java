package com.ata.ie.filter.webproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.ata.ie.hitpair.Hit;
import com.ata.ie.hitpair.HitPair;
import com.ata.ie.hitpair.HitPairBuster;

/**
 * Client for the FIRE_WebProxy Jetty server that provides streamed HTTP access to FIRE from
 * outside the firewall. The response is analyzed and the Hits/hitPairs are extracted
 * (this is Molule III)
 * <p>
 * Note: To invoque the client set the "http.keepAlive" parameter to false: 
 * <p>
 * -Dhttp.keepAlive=false
 * <p>  
 * 
 * @author Alberto Labarga (alberto.labarga@gmail.com)
 */

public class HttpFIREClient {
	
  /**
   * This needs to be changed once the definitive architecture has been set
   */
  public static final String SERVER_URL = "http://localhost:8081";     
  
  
  protected byte [] buffer;
  protected boolean uploadDone;
  protected boolean downloadDone;
  protected URL url;
  protected HttpURLConnection conn;
  
  
  
  public HttpFIREClient () throws MalformedURLException, IOException {
  	this(SERVER_URL);
  }
  
  
  public HttpFIREClient (String urlStr) throws MalformedURLException, IOException {
  	uploadDone = false;
    downloadDone = false;
    buffer = new byte [FIREClient.BUFFER_SIZE];
  	this.url = new URL(urlStr);  	
  	conn = (HttpURLConnection)this.url.openConnection();  	  
    conn.setRequestMethod("POST");
    conn.setUseCaches(false);    
    conn.setDoInput(true);
    conn.setDoOutput(true);                   
    conn.setRequestProperty("Content-Type", FIRE_WebProxyServlet.CONTENT_TYPE);
    conn.setRequestProperty("Transfer-Encoding", "chunked");     
    conn.setChunkedStreamingMode(FIREClient.BUFFER_SIZE);
    conn.connect();
  }    
  
    
  public void upload (InputStream in) throws IOException {  	
  	if (uploadDone) throw new IOException("Upload done already.");
  	OutputStream out = conn.getOutputStream();
  	
  	FIREClient.copy(in, out, false);
    out.close();
    uploadDone = true;  	
  }
  
  
  public void download (OutputStream out) throws IOException {
  	if (downloadDone) throw new IOException("Download done already.");
  	InputStream in = conn.getInputStream();
  	FIREClient.copy(in, out, false);
    in.close();
    downloadDone = true;
  }
  
  
  public int download (Map<String, Hit> hitHash, Map<String, HitPair> hitPairHash) 
  throws IOException {
    
    if (downloadDone) throw new IOException("Download done already.");
    InputStream in = conn.getInputStream();
    HitPairBuster hitPairBuster = new HitPairBuster(in, hitHash, hitPairHash);
    int documentCount = hitPairBuster.filter();
    in.close();
    downloadDone = true;
    return documentCount;
  }
  
  
  public void close (){
  	conn.disconnect();
  }
  
  
  public static void main (String [] args) throws IOException {  	
    String serverUrl = (args.length == 1)? args[0] : SERVER_URL;
    HttpFIREClient client = new HttpFIREClient(serverUrl);  	
    client.upload(System.in);
    client.download(System.out);
    
    Map<String, Hit> hitHash = new HashMap<String, Hit>();
    Map<String, HitPair> hitPairHash = new HashMap<String, HitPair>();
    int documentCount = client.download(hitHash, hitPairHash);
    client.close();

    // See results
    System.out.println("Document count: " + documentCount);
    System.out.println("Hit count: " + hitHash.size());
    System.out.println("HitPair count: " + hitPairHash.size());
    for (Hit hit: hitHash.values()){
      hit.printHit();
    }
    for (HitPair hitPair: hitPairHash.values()){
      hitPair.printAbstracts();
    }
  }
}
