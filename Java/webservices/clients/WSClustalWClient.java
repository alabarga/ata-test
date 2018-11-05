import java.io.*;

import org.apache.axis.AxisFault;
import org.apache.axis.client.*;
import org.apache.axis.MessageContext; 
import org.apache.axis.encoding.XMLType;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;

import javax.activation.DataHandler;

import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.ParameterMode; 
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.*;
import java.util.*;

import org.apache.commons.cli.*;

import uk.ac.ebi.webservices.WSClustalW.*;

public class WSClustalWClient {
  
    private static String readFile(File file) throws IOException {
    
        InputStream is = new FileInputStream(file);
        
        long length = file.length();
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while(offset < bytes.length && (numRead=is.read(bytes,offset,bytes.length-offset)) >= 0 ) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("...");
        }
        is.close();
        return new String(bytes);
    }
    
    private static int writeFile(File file, String data) throws IOException {
    
        OutputStream os = new FileOutputStream(file);
        PrintStream p = new PrintStream( os );
		
        p.println (data);

        p.close();
        return 0;
    }
	
    private static int usage(Options options)  {
	System.out.println("Use: java WSClustalWClient [options] --email your@email.com --sequence test");
	System.out.println("");
	System.out.println(" [ options ] ");
	System.out.println("");        
        
	Iterator ii = options.getOptions().iterator();
 	while(ii.hasNext()) {
   	Option opt = (Option)ii.next();
	 System.out.println(" --" + opt.getLongOpt() + " : " + opt.getDescription());
	}


	    System.out.println("");
     	System.out.println(" [ General options ]");
	    System.out.println("");
    	System.out.println(" --help    :         : prints this help text");
	    System.out.println(" --async   :         : forces to make an asynchronous query");	
	    System.out.println(" --status  :         : poll for the status of a job");
	    System.out.println(" --polljob :         : poll for the results of a job");
	    System.out.println(" --stdout :          : send results to standard output");
	    System.out.println(" --jobid   : string  : jobid that was returned when an asynchronous job was submitted.");
	    System.out.println(" --outfile : string  : name of the file results should be written to (default is jobid)");
	    System.out.println(" --jobid   : string  : jobid that was returned when an asynchronous job was submitted.");
	    System.out.println("");
	    System.out.println(" Synchronous job:");
	    System.out.println("  The results/errors are returned as soon as the job is finished.");
	    System.out.println("  Usage: java WSClustalWClient [options] --sequence file [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSClustalWClient [options] --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSClustalWClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, use the jobid to retrieve the status of the job. ");
	    System.out.println("  Use: java WSClustalWClient --polljob --jobid string [--outfile string]");
	    System.out.println("");
	    System.out.println(" [ Output files ]");
	    System.out.println("");
	    System.out.println("  .txt	 	: Program output");
	    System.out.println("  .aln		: ClustalW alignment");
	    System.out.println("  .dnd		: Tree guide");
	    System.out.println("  .nj .ph .dst	: Tree file");
	    System.out.println("  .errors 	: Errors file");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://www.ebi.ac.uk/ClustalW/ClustalW_help.html");
     return 0;
    }

    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSClustalWService service =  new WSClustalWServiceLocator();
	    WSClustalW wsClustalW = service.getWSClustalW();
	    String result =  wsClustalW.checkStatus(jobid);
	    System.err.println(result);
	    System.err.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.err.println("Use: java WSClustalWClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.err.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
        
	try {

	    WSClustalWService service =  new WSClustalWServiceLocator();
	    WSClustalW wsClustalW = service.getWSClustalW();

	    String filename;
		if (line.hasOption("outfile")) filename = line.getOptionValue("outfile");
        else filename = jobid;


	   String format = "toolaln";
	   if (line.hasOption("stdout")) { 
	     if (line.hasOption("outputtree")) {
	      if (line.getOptionValue("outputtree").equals("nj"))	
               format ="toolnj";
	      if (line.getOptionValue("outputtree").equals("dist"))	
               format ="tooldst";
	      if (line.getOptionValue("outputtree").equals("phylip"))	
               format ="toolph";
	     } else if (line.hasOption("tree"))  format ="toolnj";

	      String result = new String(wsClustalW.poll(jobid,format));
	      System.out.println(result.trim());  
    
       } else {

        WSFile[] results = wsClustalW.getResults(jobid);
	    for (int i=0;i<results.length;i++){

		WSFile file = results[i];
		byte[] resultbytes = wsClustalW.poll(jobid,file.getType());
		String result = new String(resultbytes);

		System.err.println("Printing result files: " + filename +"."+file.getExt());
		writeFile( new File(filename+"."+ file.getExt()), result.trim()); 
	    }
	   }
	 }
         catch (Exception e) {
            System.err.println("ERROR:\n" + e.toString());
            e.printStackTrace();
         }

	return 0;
    }	

private static Data[] loadData(CommandLine line) throws IOException {

 Data[] inputs = new Data[1];

 try {
   
    if (line.hasOption("sequence")) {

     if ((new File(line.getOptionValue("sequence"))).exists()){
		Data input= new Data();
		input.setType("sequence");
		String fileContent = readFile(new File(line.getOptionValue("sequence")));
		input.setContent(fileContent);	
		inputs[0]=input;
      
     } else {
       System.err.println("Please enter valid filename/input sequence");
       System.exit(0);
     }
    
   } else {

    String[] more = line.getArgs();
    if (more.length>0) {
      if ((new File(more[0])).exists()){
        Data input= new Data();
		input.setType("sequence");
		String fileContent = readFile(new File(more[0]));
		input.setContent(fileContent);
		inputs[0]=input;
       } else {
        System.err.println("Please enter valid filename/input sequence");
        System.exit(0);
       } 
    }
   }

 }
 catch (Exception e) {
   System.err.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }
 return inputs;
}

private static InputParams loadParams(CommandLine line) throws IOException {

 InputParams params = new InputParams();
 try {

    if (line.hasOption("alignment")) params.setAlignment(line.getOptionValue("alignment")); 	

    if (line.hasOption("align")) params.setAlignment("full");
    if (line.hasOption("quicktree")) params.setAlignment("fast");
    if (line.hasOption("tree")) params.setOutputtree("nj");
    if (line.hasOption("outputtree")) params.setOutputtree(line.getOptionValue("outputtree"));

    if (line.hasOption("pairgap")) params.setPairgap(Integer.valueOf(line.getOptionValue("pairgap"))); 
    if (line.hasOption("gapdist")) params.setGapdist(Integer.valueOf(line.getOptionValue("gapdist"))); 
    if (line.hasOption("gapext")) params.setGapext(new Float(line.getOptionValue("gapext"))); 
    if (line.hasOption("gapopen")) params.setGapopen(Integer.valueOf(line.getOptionValue("gapopen"))); 
    if (line.hasOption("ktup")) params.setKtup(Integer.valueOf(line.getOptionValue("ktup"))); 
    if (line.hasOption("topdiags")) params.setTopdiags(Integer.valueOf(line.getOptionValue("topdiags"))); 
    if (line.hasOption("matrix")) params.setMatrix(line.getOptionValue("matrix")); 
    if (line.hasOption("output")) params.setOutput(line.getOptionValue("output")); 
    if (line.hasOption("score")) params.setScores(line.getOptionValue("score")); 
    if (line.hasOption("outorder")) params.setOutorder(line.getOptionValue("outorder")); 

    if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 
    if (line.hasOption("async")) params.setAsync(new Boolean(true)); 

    if (line.hasOption("kimura")) params.setKimura(new Boolean(true));
    if (line.hasOption("tossgaps")) params.setTossgaps(new Boolean(true));

 }
 catch (Exception e) {
   System.err.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }
 return params;
}



public static void main (String[] args) {

        //create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("alignment")
                                        .withDescription("Alignment type: fast/full")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("alignment") );
        options.addOption(OptionBuilder.withLongOpt("output")
					.withDescription("Output format: aln1/aln2/gcg/phyip/pir/gde")
					.hasArg()
					.create("output") );
        options.addOption(OptionBuilder.withLongOpt("matrix")
                                        .withDescription("Matrix: blosum/gonnet/id/pam")
                                        .create("matrix") );
        options.addOption(OptionBuilder.withLongOpt("outorder")
                                        .withDescription("Output order: input/aligned")
                                        .create("r") );
		options.addOption(OptionBuilder.withLongOpt("ktup")
                                        .withDescription(" limit the word-length the search should use: 1-5")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("ktup") );
        options.addOption(OptionBuilder.withLongOpt("gapopen")
                                        .withDescription("Penalty for the first residue in a gap: 1-100")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("gapopen") );
        options.addOption(OptionBuilder.withLongOpt("gapext")
                                        .withDescription("Penalty for additional residues in a gap: 0.5-10.0")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("gapext") );
        options.addOption(OptionBuilder.withLongOpt("gapdist")
                                        .withDescription("Penalty for gap separation: 1-10")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("gapdist") );
        options.addOption(OptionBuilder.withLongOpt("score")
                                        .withDescription("Score type: percent/absolute")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("score") );
        options.addOption(OptionBuilder.withLongOpt("cpu")
                                        .withDescription("CPU mode: single/multiple")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("cpu") );
        options.addOption(OptionBuilder.withLongOpt("outputtree")
                                        .withDescription("Tree Type: nj/phylip/dist")
                                        .hasArg()
                                        .withValueSeparator(' ')
                                        .create("outputtree") );
        options.addOption(OptionBuilder.withLongOpt("tossgaps")
                                        .withDescription("Ignore gaps in aligment")
                                        .hasArg()
                                        .withValueSeparator(' ')
                                        .create("tossgaps") );
        options.addOption(OptionBuilder.withLongOpt("pairgap")
                                        .withDescription("Gap penalty")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("pairgap") );
        options.addOption(OptionBuilder.withLongOpt("topdiags")
                                        .withDescription("Number of top diagonals")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("topdiags") );
        options.addOption(OptionBuilder.withLongOpt("title")
                                        .withDescription("Job name")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("title") );
                                        
 	options.addOption( "align","align", false, "do full alignment");
	options.addOption( "quicktree","quicktree", false, "do fast alignment");
	options.addOption( "tree","tree", false, "nj tree");
	options.addOption( "kimura","kimura", false, "Kimura correction");
	
	//****************************************************************************
	// common options for EBI clients 
	//****************************************************************************
	
    options.addOption( "help","help", false, "help on using this client");
    options.addOption( "async","async",false, "perform an asynchronous job");
    options.addOption( "polljob","polljob", false, "poll for the status of an asynchronous job and get the results");
    options.addOption( "status","status", false, "poll for the status of an asynchronous job");   
    options.addOption( "stdout","stdout", false, "send output to STDOUT"); 
     
    options.addOption(OptionBuilder.withLongOpt("jobid")
                                        .withDescription("Jobid at EBI queue system")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("jobid") );
    options.addOption(OptionBuilder.withLongOpt("email")
										.hasArg()
                                        .withDescription("Your email address")
                                        .create("email") );
    options.addOption(OptionBuilder.withLongOpt("sequence")
                                        .withDescription("path to a sequence file")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("sequence") );
    
    options.addOption(OptionBuilder.withLongOpt("outfile")
                                        .withDescription("base file name for results")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("outfile") );

    CommandLineParser parser = new PosixParser();    //create the command line parser    
    
	
    try {
            
      CommandLine line = parser.parse(options,args);
           
      if (line.hasOption("polljob") && line.hasOption("jobid")) {    
		String jobid = line.getOptionValue("jobid");            
		getResults(jobid,line);
      } 
      else if (line.hasOption("status") && line.hasOption("jobid")) {
		String jobid = line.getOptionValue("jobid");
		checkStatus(jobid);
      }
      else {

      String[] more = line.getArgs();
      if (line.hasOption("sequence")||(more.length>0)) {

        InputParams params = loadParams(line);
		Data inputs[] = loadData(line);
		WSClustalWService service =  new WSClustalWServiceLocator();
		WSClustalW wsClustalW = service.getWSClustalW();
		String jobid = wsClustalW.runClustalW(params,inputs);
		if (line.hasOption("async")) {
         System.out.println(jobid);
         System.err.println("This is your job id: "+ jobid);
         System.err.println("Use: java WSClustalWClient --status --jobid "+ jobid + " to see the job status");
		} else {
         getResults(jobid,line);	
        }   
      } else {

        usage(options);		    
        System.exit(0);
      }
     }
    }
    catch (ParseException exp) {
      System.err.println("Parsing failed. Reason: " + exp.getMessage());
      usage(options);
    }
    catch (Exception e) {
      System.err.println("ERROR:\n" + e.toString());
      e.printStackTrace();
    }
  }
}
