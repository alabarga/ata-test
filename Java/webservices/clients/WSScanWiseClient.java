import java.io.*;
import org.apache.axis.AxisFault;
import org.apache.axis.client.*;
import org.apache.axis.MessageContext; 
import org.apache.axis.encoding.XMLType;
import org.apache.axis.transport.http.HTTPConstants;

import javax.activation.DataHandler;
import javax.xml.rpc.ParameterMode; 
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.*;


import org.apache.commons.cli.*;

import uk.ac.ebi.webservices.WSScanWise.*;

public class WSScanWiseClient {
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
	System.out.println("Use: java WSScanWiseClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence file");
	System.out.println("Use: java WSScanWiseClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence db:entry_id");
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
	    System.out.println(" --jobid   : string  : jobid that was returned when an asynchronous job was submitted.");
	    System.out.println(" --outfile : string  : name of the file results should be written to (default is jobid)");
	    System.out.println(" --stdout :          : send results to standard output");
	    System.out.println(" --jobid   : string  : jobid that was returned when an asynchronous job was submitted.");
	    System.out.println("");
	    System.out.println(" Synchronous job:");
	    System.out.println("  The results/errors are returned as soon as the job is finished.");
	    System.out.println("  Usage: java WSScanWiseClient [options] --sequence file [--outformat txt/xml]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSScanWiseClient [options] --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSScanWiseClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSScanWiseClient --polljob --jobid string [--outfile string]");
	    System.out.println("");
	    System.out.println(" [ Output files ]");
	    System.out.println("");
	    System.out.println("  .txt	 	: Program output");
	    System.out.println("  .xml		: XML output");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://www.ebi.ac.uk/scanwise/scanwise_help.html");
     return 0;
    }
    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSScanWiseService service =  new WSScanWiseServiceLocator();
	    WSScanWise scanwise = service.getWSScanWise();
	    String result =  scanwise.checkStatus(jobid);
	    System.out.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.out.println("Use: java WSScanWiseClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {
			
	     WSScanWiseService service =  new WSScanWiseServiceLocator();
	     WSScanWise scanwise = service.getWSScanWise();

	    String filename = jobid;	
	    String format="tooloutput";
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	 

	    if (line.hasOption("ids")) {

        String[] ids = scanwise.getIds(jobid);

        for (int i=0;i<ids.length;i++){
			System.out.println(ids[i]);
	    } 
	     return 0;	
	    }
	
	    if (line.hasOption("outformat")) {

            byte[] resultbytes = scanwise.poll(jobid,format);
			String result = new String(resultbytes);
			if (line.hasOption("stdout")){ 
              System.out.println(result);
             } else {
              writeFile( new File(filename+"."+line.getOption("outformat")), result);
             }   
        } else {

 	     WSFile[] results = scanwise.getResults(jobid);
	     for (int i=0;i<results.length;i++){
			WSFile file = results[i];
			byte[] resultbytes = scanwise.poll(jobid,file.getType());
			String result = new String(resultbytes);		  
			System.out.println("Printing result files: " + filename +"."+file.getExt());
			writeFile( new File(filename +"."+ file.getExt()), result); 
	     }
	    }
	}
    catch (Exception e) {
        System.out.println("ERROR:\n" + e.toString());
        e.printStackTrace();
    }
	return 0;
}	

private static String[] getIds(String jobid) throws IOException {

	try {

	    WSScanWiseService service =  new WSScanWiseServiceLocator();
	    WSScanWise scanwise = service.getWSScanWise();
	    String[] results = scanwise.getIds(jobid);
	    return results;
	
	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	
	return null;

}	

private static Data[] loadData(CommandLine line) throws IOException {

 Data[] inputs = new Data[1];

 try {

    if (line.hasOption("sequence")) {
     Pattern p = Pattern.compile("([a-zA-Z]*):(\\w*)");
     Matcher m = p.matcher(line.getOptionValue("sequence"));
     if (m.matches()) {
		Data input= new Data();
		input.setType("sequence");
		input.setContent(line.getOptionValue("sequence"));
		inputs[0]=input;
    } else {
     if ((new File(line.getOptionValue("sequence"))).exists()){
		Data input= new Data();
		input.setType("sequence");
	
		String fileContent = readFile(new File(line.getOptionValue("sequence")));
		input.setContent(fileContent);
		inputs[0]=input;
      
     } else {
       System.out.println("Please enter valid filename/input sequence");
       System.exit(0);
     }
    }
   }
 }
 catch (Exception e) {
   System.out.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }
 return inputs;
}
private static InputParams loadParams(CommandLine line) throws IOException {

 InputParams params = new InputParams();
 try {
    if (line.hasOption("database")) params.setDatabase(line.getOptionValue("database"));
    if (line.hasOption("hspscan_minhsp")) params.setHspscan_Minhsp(Integer.valueOf(line.getOptionValue("hspscan_minhsp"))); 
    if (line.hasOption("hspscan_maxres")) params.setHspscan_Maxres(Integer.valueOf(line.getOptionValue("hspscan_maxres")));  
    if (line.hasOption("hspscan_worddepth")) params.setHspscan_Worddepth(Integer.valueOf(line.getOptionValue("hspscan_worddepth")));  
    if (line.hasOption("hspscan_numb")) params.setHspscan_Numb(Integer.valueOf(line.getOptionValue("hspscan_numb")));
    if (line.hasOption("hspscan_minword")) params.setHspscan_Minword(Integer.valueOf(line.getOptionValue("hspscan_minword")));

    if (line.hasOption("outformat")) params.setOutformat(line.getOptionValue("outformat")); 
    if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 
    if (line.hasOption("async")) params.setAsync(new Boolean(true)); 
   

 }
 catch (Exception e) {
   System.out.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }
 return params;
}

    public static void main (String[] args) {
    
        //create the Options
        Options options = new Options();
        

        options.addOption(OptionBuilder.withLongOpt("database")
                                .withDescription("uniprot")
                                .withValueSeparator(' ')
                                .hasArg()
                                .create("database") ); 
        

        options.addOption(OptionBuilder.withLongOpt("hspscan_maxres")
                                        .withValueSeparator(' ')
                                        .withDescription("The maximum results returned by scan:50-300 (100)")
                                        .hasArg()
                                        .create("hspscan_maxres") );
        options.addOption(OptionBuilder.withLongOpt("hspscan_numb")
                                        .withValueSeparator(' ')
                                        .withDescription("When a word is hit by this number or more in the database, supresses HSP generation from this word. This handles low complexity sequences. Due to an interplay of high scoring HSPs with maxresults, lower numb levels can improve sensitivity: 50-5000 (1000)")
                                        .hasArg()
                                        .create("hspscan_numb") );
        options.addOption(OptionBuilder.withLongOpt("hspscan_worddepth")
                                        .withValueSeparator(' ')
                                        .withDescription(" How many amino acids considered to be different from the original words: 0-2 (2)")
                                        .hasArg()
                                        .create("hspscan_worddepth") );
        options.addOption(OptionBuilder.withLongOpt("hspscan_minword")
                                        .withValueSeparator(' ')
                                        .withDescription(" Minimum score for 2-away words used (all identical and 1-away words are used currently). The higher this number the faster the search, but the less sensitive. Use 24 when looking for 80%> identical matches only: 12-24 (14)")
                                        .hasArg()
                                        .create("hspscan_minword") );
        options.addOption(OptionBuilder.withLongOpt("hspscan_minhsp")
                                        .withValueSeparator(' ')
                                        .withDescription(" Minimum score for hsps: 22-35 (14)")
                                        .hasArg()
                                        .create("hspscan_minhsp") );
        
                
        //****************************************************************************
	// common options for EBI clients 
	//****************************************************************************
	
    options.addOption( "help","help", false, "help on using this client");
    options.addOption( "async","async",false, "perform an asynchronous job");
    options.addOption( "stdout","stdout",false, "print to standard output");
    options.addOption( "ids","ids",false, "retrieve only identifiers");
    options.addOption( "polljob","polljob", false, "poll for the status of an asynchronous job and get the results");
    options.addOption( "status","status", false, "poll for the status of an asynchronous job");        
  
    options.addOption(OptionBuilder.withLongOpt("outformat")
                                        .withValueSeparator(' ')
                                        .withDescription("txt/xml")
                                        .hasArg()
                                        .create("outformat") );
    options.addOption(OptionBuilder.withLongOpt("email")
                                        .withDescription("Your email address")
					.hasArg()
                                        .create("email") );
    options.addOption(OptionBuilder.withLongOpt("jobid")
                                        .withDescription("Jobid at EBI queue system")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("jobid") );
    options.addOption(OptionBuilder.withLongOpt("sequence")
                                        .withDescription("path to a sequence file")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("sequence") );
	options.addOption(OptionBuilder.withLongOpt("dbfetch")
                                        .withDescription("db:id input for dbfetch")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("dbfetch") );
	options.addOption(OptionBuilder.withLongOpt("ebifile")
                                        .withDescription("tool:jobid:filetype from previous results")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("ebifile") );
	options.addOption(OptionBuilder.withLongOpt("outfile")
                                        .withDescription("file name to save the results")
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

      if (line.hasOption("sequence")||line.hasOption("dbfetch")||line.hasOption("ebifile")) {

        InputParams params = loadParams(line);
	    Data inputs[] = loadData(line);
	    String inputSeq = "";
	    WSScanWiseService service =  new WSScanWiseServiceLocator();
	    WSScanWise scanwise = service.getWSScanWise();
	    String jobid = scanwise.runScanWise(params,inputs);

	    if (line.hasOption("async")) {
               System.out.println("This is your job id: "+ jobid);
	       System.out.println("Use: java WSScanWiseClient --status --jobid "+ jobid + " to see the job status");
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
          System.out.println( "Parsing failed. Reason: " + exp.getMessage() );
          usage(options);
        }
        catch (Exception e) {
            System.err.println ("ERROR:\n" + e.toString());
	    e.printStackTrace();
	}
    }
}
