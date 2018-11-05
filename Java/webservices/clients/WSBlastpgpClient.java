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

import uk.ac.ebi.webservices.WSBlastpgp.*;

public class WSBlastpgpClient {
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
	System.out.println("Use: java WSBlastpgpClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence file");
	System.out.println("Use: java WSBlastpgpClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence db:entry_id");
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
	    System.out.println("  Usage: java WSBlastpgpClient [options] --program PROGNAME --database DBNAME --sequence file [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSBlastpgpClient [options] --program PROGNAME --database DBNAME --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSFastaClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSBlastpgpClient --polljob --jobid string [--outfile string]");
	    System.out.println("");
	    System.out.println(" [ Output files ]");
	    System.out.println("");
	    System.out.println("  .txt	 	: Program output");
	    System.out.println("  .xml		: XML output");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://www.ebi.ac.uk/blast2/WU-Blast2_help.html");
     return 0;
    }
    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSBlastpgpService service =  new WSBlastpgpServiceLocator();
	    WSBlastpgp blastpgp = service.getWSBlastpgp();
	    String result =  blastpgp.checkStatus(jobid);
	    System.out.println(result);
	    System.err.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.err.println("Use: java WSBlastpgpClient --polljob --jobid "+ jobid + " to see the results");	

	}
    catch (Exception e) {
        System.out.println("ERROR:\n" + e.toString());
        e.printStackTrace();
    }
	return 0;
}	

private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {
			
	     WSBlastpgpService service =  new WSBlastpgpServiceLocator();
	     WSBlastpgp blastpgp = service.getWSBlastpgp();

	    String filename= jobid; 	
	    String format="tooloutput";
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	 

	    if (line.hasOption("ids")) {

         String[] ids = blastpgp.getIds(jobid);
         for (int i=0;i<ids.length;i++){
			System.out.println(ids[i]);
	     } 
	     return 0;	
	    }
	
	    if (line.hasOption("outformat")) {

	     if (line.getOptionValue("outformat")=="xml") {format="appxmlfile";}
         byte[] resultbytes = blastpgp.poll(jobid,format);
	     String result = new String(resultbytes);
	     if (line.hasOption("stdout")){ 
            System.out.println(result);
         } else {
            writeFile( new File(filename), result);
         }   

        } else {

 	     WSFile[] results = blastpgp.getResults(jobid);
	     for (int i=0;i<results.length;i++){
			WSFile file = results[i];
			byte[] resultbytes = blastpgp.poll(jobid,file.getType());
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

	    WSBlastpgpService service =  new WSBlastpgpServiceLocator();
	    WSBlastpgp blastpgp = service.getWSBlastpgp();
	    String[] results = blastpgp.getIds(jobid);
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

    if (line.hasOption("mode")) params.setMode(line.getOptionValue("mode")); 	
    if (line.hasOption("database")) params.setDatabase(line.getOptionValue("database"));
    if (line.hasOption("matrix")) params.setMatrix(line.getOptionValue("matrix"));
    if (line.hasOption("filter")) params.setFilter(line.getOptionValue("filter"));
    if (line.hasOption("gapalign")) params.setGapalign(line.getOptionValue("gapalign"));
    if (line.hasOption("exp")) params.setExp(new Float(line.getOptionValue("exp")));
    if (line.hasOption("expmulti")) params.setExpmulti(new Float(line.getOptionValue("expmulti")));
    if (line.hasOption("align")) params.setAlign(Integer.valueOf(line.getOptionValue("align"))); 
    if (line.hasOption("extendgap")) params.setExtendgap(Integer.valueOf(line.getOptionValue("extendgap"))); 
    if (line.hasOption("opengap")) params.setOpengap(Integer.valueOf(line.getOptionValue("opengap"))); 
    if (line.hasOption("scores")) params.setScores(Integer.valueOf(line.getOptionValue("scores"))); 
    if (line.hasOption("maxpasses")) params.setMaxpasses(Integer.valueOf(line.getOptionValue("maxpasses")));
    if (line.hasOption("dropoff")) params.setDropoff(Integer.valueOf(line.getOptionValue("dropoff")));  
    if (line.hasOption("finaldropoff")) params.setFinaldropoff(Integer.valueOf(line.getOptionValue("finaldropoff")));  
    if (line.hasOption("startregion")) params.setStartregion(Integer.valueOf(line.getOptionValue("startregion")));  
    if (line.hasOption("endregion")) params.setEndregion(Integer.valueOf(line.getOptionValue("endregion")));
    if (line.hasOption("patern")) params.setPattern(line.getOptionValue("pattern"));
    if (line.hasOption("usagemode")) params.setUsagemode(line.getOptionValue("usagemode"));  
    if (line.hasOption("async")) params.setAsync(new Boolean(true)); 
    if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 
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
        
        ///blosum70/blosum75/blosum80/blosum85/blosum90/blosum40/\nblosum45/blosum50/
        options.addOption(OptionBuilder.withLongOpt("mode")
                                        .withDescription("name of the program to be used: [PSI-Blast, PHI-Blast]")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("mode") );
		options.addOption(OptionBuilder.withLongOpt("database")
                                .withDescription("name of the database (only protein databases)")
                                .withValueSeparator(' ')
                                .hasArg()
                                .create("database") ); 
        options.addOption(OptionBuilder.withLongOpt("matrix")
                                        .withDescription("name of a file containing an alternate scoring matrix [blosum62, blosum45, blosum80, pm30, pam70]")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("matrix") );
        options.addOption(OptionBuilder.withLongOpt("exp")
                                        .withDescription("Expectation value")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("exp") );
        options.addOption(OptionBuilder.withLongOpt("expmulti")
                                        .withDescription("threshold (multipass model)")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("expmulti") );
        options.addOption(OptionBuilder.withLongOpt("filter")
                                        .withValueSeparator(' ')
                                        .withDescription("filter query sequence with SEG [T,F]")
                                        .hasArg()
                                        .create("filter") );
        options.addOption(OptionBuilder.withLongOpt("gapalign")
                                        .withValueSeparator(' ')
                                        .withDescription("Gapped [T,F]")
                                        .hasArg()
                                        .create("gapalign") );
        options.addOption(OptionBuilder.withLongOpt("scores")
                                        .withValueSeparator(' ')
                                        .withDescription("number of scores to be reported")
                                        .hasArg()
                                        .create("scores") );
        options.addOption(OptionBuilder.withLongOpt("maxpasses")
                                        .withValueSeparator(' ')
                                        .withDescription("number of iterations")
                                        .hasArg()
                                        .create("maxpasses") );
        options.addOption(OptionBuilder.withLongOpt("opengap")
                                        .withValueSeparator(' ')
                                        .withDescription("cost of opening a gap")
                                        .hasArg()
                                        .create("opengap") );    
        options.addOption(OptionBuilder.withLongOpt("extendgap")
                                        .withValueSeparator(' ')
                                        .withDescription("cost of extending a gap")
                                        .hasArg()
                                        .create("extendgap") );   
        options.addOption(OptionBuilder.withLongOpt("align")
                                        .withValueSeparator(' ')
                                        .withDescription("alignment view option  [ 0 - pairwise, 1 - M/S identities , 2 - M/S non-identities, 3 - Flat identities, 4 - Flat non-identities]")
                                        .hasArg()
                                        .create("align") );  
 
        options.addOption(OptionBuilder.withLongOpt("dropoff")
                                        .withValueSeparator(' ')
                                        .withDescription("Dropoff")
                                        .hasArg()
                                        .create("dropoff") ); 
 
        options.addOption(OptionBuilder.withLongOpt("finaldropoff")
                                        .withValueSeparator(' ')
                                        .withDescription("Final Dropoff")
                                        .hasArg()
                                        .create("finaldropoff") ); 
 
        options.addOption(OptionBuilder.withLongOpt("startregion")
                                        .withValueSeparator(' ')
                                        .withDescription("Start of required region in query")
                                        .hasArg()
                                        .create("startregion") ); 
 
        options.addOption(OptionBuilder.withLongOpt("startregion")
                                        .withValueSeparator(' ')
                                        .withDescription("End of required region in query (negative number)")
                                        .hasArg()
                                        .create("startregion") ); 

          options.addOption(OptionBuilder.withLongOpt("pattern")
                                        .withValueSeparator(' ')
                                        .withDescription("Hit File or pattern (PHI-BLAST only)")
                                        .hasArg()
                                        .create("pattern") ); 
        options.addOption(OptionBuilder.withLongOpt("usagemode")
                                        .withValueSeparator(' ')
                                        .withDescription("Program option [blastpgp,patseedp,seedp] (PHI-BLAST only)")
                                        .hasArg()
                                        .create("usagemode") );               
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

      if (line.hasOption("sequence")) {

        InputParams params = loadParams(line);
	    Data inputs[] = loadData(line);
	    String inputSeq = "";
	    WSBlastpgpService service =  new WSBlastpgpServiceLocator();
	    WSBlastpgp blastpgp = service.getWSBlastpgp();
	    String jobid = blastpgp.runBlastpgp(params,inputs);

	    if (line.hasOption("async")) {
		   System.out.println(jobid); 
           System.err.println("This is your job id: "+ jobid);
	       System.err.println("Use: java WSBlastpgpClient --status --jobid "+ jobid + " to see the job status");
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
