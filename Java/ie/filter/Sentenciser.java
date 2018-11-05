package com.ata.ie.filter;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import monq.jfa.AbstractFaAction;
import monq.jfa.Dfa;
import monq.jfa.DfaRun;
import monq.jfa.Nfa;
import monq.jfa.Xml;
import monq.jfa.actions.Copy;
import monq.jfa.actions.Replace;
import monq.jfa.actions.SwitchDfa;
import monq.net.FilterServiceFactory;
import monq.net.Service;
import monq.net.ServiceCreateException;
import monq.net.ServiceFactory;
import monq.net.TcpServer;
import com.ata.util.CmdLineArgs;


public class Sentenciser extends MyServer {
  
  public static final String SID_ATTR = "sid";
  private static final String START_SENTENCE = "<Sentence";
  private static final String END_SENTENCE = "</Sentence>";
  
  
  private static AbstractFaAction startText = new AbstractFaAction(){
    public void invoke (StringBuffer yytext, int start, DfaRun runner){
      runner.clientData = new Integer(0);
      // Delete optional trailing blanks (<text>  THESE WHITES  sent1. sent2 ... </text>)
      int inputLen = yytext.length() - 1;
      for ( ; yytext.charAt(inputLen) != '>' ; inputLen --);
      yytext.setLength(inputLen + 1);
      // unskip one blank to trigger the action 'blank' as an
      // artificial start of sentence together with collect=false;
      yytext.append(" ");
      runner.unskip(yytext, inputLen + 1);
    }
  };


  private static AbstractFaAction lastSentence = new AbstractFaAction(){
    public void invoke (StringBuffer yytext, int start, DfaRun runner) {
      // Insert end of sentence. This is only necessary if we are in
      // collect mode, because otherwise we found the end tag right after
      // the start tag with nothing in between.
      if (runner.collect){
        char pm = yytext.charAt(start);
        int insertPos = start + ((pm == '.' || pm == '?' || pm == '!')? 1 : 0);
        yytext.insert(insertPos, END_SENTENCE);
        runner.collect = false;
      }
    }
  };


  private static AbstractFaAction trySentence = new AbstractFaAction(){
    public void invoke(StringBuffer yytext, int start, DfaRun runner){
      int inputLen = yytext.length();
      runner.collect = false;
      // after the punctuation mark there is at least one blank. We
      // get rid of all blanks except one, which triggers
      // r.collect=true 
      int i = start + 2;
      for ( ; i<inputLen && Character.isWhitespace(yytext.charAt(i)); i++ );
      runner.unskip(yytext, i - 1);
      yytext.setLength(start + 1); // Leave the pm in the output
      yytext.append(END_SENTENCE);
    }
  };


  public static String getSentenceStartTag (int sentNumber){
    return new StringBuilder(START_SENTENCE)
      .append(" ")
      .append(SID_ATTR)
      .append("=\"")
      .append(sentNumber)
      .append("\">")
      .toString();
  }
  
  
  public static final String getSentenceEndTag (){ return END_SENTENCE; }
  
  
  private static AbstractFaAction blank = new AbstractFaAction(){
    public void invoke(StringBuffer yytext, int start, DfaRun runner){
      yytext.setLength(start); // delete all blanks, we will leave just one
      if (!runner.collect){
        // The very first blank found (triggers start of Sentence)
        runner.collect = true;
        Integer sid = (Integer)runner.clientData;
        yytext.append(START_SENTENCE)
          .append(" ")
          .append(SID_ATTR)
          .append("=\"")
          .append(sid.intValue())
          .append("\">");
        runner.clientData = new Integer(sid.intValue() + 1);
      }
      else {
        yytext.append(" ");
      }
    }
  };
  
  
  private static class NoEnd extends AbstractFaAction {
    
    private int toklen;
    
    public NoEnd (int toklen){
      this.toklen = toklen;
    }
    
    public void invoke (StringBuffer yytext, int start, DfaRun runner){
      // need to push back trailing context
      runner.unskip(yytext, start + toklen);
    }
  }


  private static Dfa dfa;
  static {
    try {
      SwitchDfa detectBoundaries = new SwitchDfa(lastSentence);
      SwitchDfa doWork = new SwitchDfa(startText);
      
      Nfa boundariesNfa = new Nfa(Xml.STag(ROI) + "[\r \n\t]*", doWork)
        .or("[^<\n\r]+", Copy.COPY);
      Dfa boundariesDfa = boundariesNfa.compile(DfaRun.UNMATCHED_COPY);
      detectBoundaries.setDfa(boundariesDfa);
      
      Nfa doWorkNfa = new Nfa("[.?!][\r \n\t]+[a-z]*[\\[\\]()A-Z0-9\\-]", trySentence)
        // an XML start tag is a good start for a sentence.
        .or("[.?!][\r \n\t]+" + Xml.STag(), trySentence)
        // when seeing ROI tag our work ends
        .or("[.?!]?[\r \n\t]*" + Xml.ETag(ROI), detectBoundaries)
        // non-sentence endings
        .or("(Dr|vs|cf)[.]", new NoEnd(3))
        .or("(e[.]g|i[.]e|c[.]f)[.]", new NoEnd(4))
        .or("[Ff]ig[.][\r \n\t]+[0-9IiVv]", new NoEnd(4))
        .or("[Nn]o[.][\r \n\t]+[0-9IiVv]", new NoEnd(3))
        // typical species names
        .or("[A-Z][.][\r \n\t]+[a-z]", new NoEnd(2))
        // normalize white space to just one blank
        .or("[\r \n\t]+", blank)
        // speed things up a bit. The following two are split into two
        // so that plain alpha strings are tokens on their
        // own. Thereby the alpha tokens ending in a dot above are
        // slightly longer and can take precedence.
        .or("[^.?!<>& \r\n\tA-Za-z]+", Copy.COPY)
        .or("[A-Za-z]+", Copy.COPY)
        .or(Xml.Reference, Copy.COPY)
        .or("<", new Replace("&lt;"))
        .or(">", new Replace("&gt;"))
        .or("&", new Replace("&amp;"))
        ;
      Dfa work = doWorkNfa.compile(DfaRun.UNMATCHED_COPY);
      doWork.setDfa(work);
      
      dfa = boundariesDfa;
    }
    catch (Exception e){
      throw new Error("Sentenciser error", e);
    }
  }

  
  public Sentenciser (final int port, InputStream in, OutputStream out){
    super(port, in, out);
  }
  
  
  public DfaRun createDfaRun () throws Exception {
    DfaRun dfaRun = new DfaRun(dfa);
    dfaRun.clientData = new Integer(0);
    return dfaRun;
  }


  private static final int ARGS_LEN = 2; 
  private static final String USAGE =
    "Usage: com.ata.ie.filter.Sentenciser -port <port for inbound connections>";


  public static void main (String [] args) throws Exception {

    CmdLineArgs cmdLineArgs = new CmdLineArgs(USAGE, ARGS_LEN, args);  
    final int port = Integer.parseInt(cmdLineArgs.getProp("port"));

    FilterServiceFactory fsf = 
      new FilterServiceFactory(new ServiceFactory (){
        public Service createService (InputStream in, OutputStream out, Object params)
        throws ServiceCreateException {
          return new Sentenciser(port, in, out);
        }
      });

    MyServerSocket mySvrSkt = new MyServerSocket(port, MAX_CONCURRENTS, InetAddress.getLocalHost());
    TcpServer svr = new TcpServer(mySvrSkt, fsf, MAX_CONCURRENTS);
    svr.setLogging(System.out);
    System.out.println("Sentenciser port " + port);
    svr.serve();          
  }
}
