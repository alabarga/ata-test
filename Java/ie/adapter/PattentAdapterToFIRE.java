package com.ata.ie.adapter;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import com.ata.ie.filter.MyServer;
import com.ata.ie.filter.MyServerSocket;
import com.ata.ie.filter.Sentenciser;
import com.ata.util.CmdLineArgs;
import monq.jfa.AbstractFaAction;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.Xml;
import monq.jfa.actions.Embed;
import monq.net.FilterServiceFactory;
import monq.net.Service;
import monq.net.ServiceCreateException;
import monq.net.ServiceFactory;
import monq.net.TcpServer;


public class PattentAdapterToFIRE extends MyServer {

  private static final String OPEN_ROI = "<" + ROI + ">";
  private static final String CLOSE_ROI = Sentenciser.getSentenceEndTag() + "</" + ROI + ">";
  
  
  public static final String [] ToAnnotate = {
    "p", "title", "claim-text"
  };
  
  
  private static AbstractFaAction open = new AbstractFaAction(){
    public void invoke(StringBuffer yytext, int start, DfaRun runner){
      int sentNumber = ((Integer)runner.clientData).intValue();
      yytext.append(OPEN_ROI).append(Sentenciser.getSentenceStartTag(sentNumber));
      runner.clientData = new Integer(sentNumber + 1);
    }
  };
  

  private static Dfa dfa = null;
  static {
    try {
    	Nfa nfa = new Nfa(Nfa.NOTHING);
    	for (String roi: ToAnnotate){
    	  nfa.or(Xml.STag(roi), /*new Embed("", OPEN_ROI)*/ open)
        .or(Xml.ETag(roi), new Embed(CLOSE_ROI, ""));
    	}
      dfa = nfa.compile(DfaRun.UNMATCHED_COPY);
    } 
    catch (Exception e){      
      throw new Error("PattentAdapterToFIRE error", e);
    }
  }
  
  
  public DfaRun createDfaRun () throws Exception {
    DfaRun dfaRun = new DfaRun(dfa);
    dfaRun.clientData = new Integer(0);
    return dfaRun;
  }


  public PattentAdapterToFIRE (int port, InputStream in, OutputStream out){
    super(port, in, out);      	
  }

  
  private static final int ARGS_LEN = 2; 
  private static final String USAGE =
    "Usage: com.ata.ie.adapter.PattentAdapterToFIRE -port <port for inbound connections>";
  
  
  public static void main (String [] args) throws Exception {

    CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);  
    final int port = Integer.parseInt(cmdLineArgs.getProp("port"));

    FilterServiceFactory fsf = 
      new FilterServiceFactory(new ServiceFactory (){
        public Service createService (InputStream in, OutputStream out, Object params)
        throws ServiceCreateException {
          return new PattentAdapterToFIRE(port, in, out);
        }
      });

    MyServerSocket mySvrSkt = new MyServerSocket(port, MAX_CONCURRENTS, InetAddress.getLocalHost());
    TcpServer svr = new TcpServer(mySvrSkt, fsf, MAX_CONCURRENTS);
    svr.setLogging(System.out);
    System.out.println("PattentAdaterToFIRE port " + port);
    svr.serve();          
  }
}
