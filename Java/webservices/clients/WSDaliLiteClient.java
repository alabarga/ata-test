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

import uk.ac.ebi.webservices.WSDaliLite.*;

public class WSDaliLiteClient {
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
	System.out.println("Use: java WSDaliLiteClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence file");
	System.out.println("Use: java WSDaliLiteClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence db:entry_id");
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
	    System.out.println("  Usage: java WSDaliLiteClient --pdb1 (PDBID or PDB file) [--chain1 ID] --pdb2 (PDBID or PDB file) [--chain2 ID]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSDaliLiteClient --pdb1 (PDBID or PDB file) [--chain1 ID] --pdb2 (PDBID or PDB file) [--chain2 ID] --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSFastaClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSDaliLiteClient --polljob --jobid string [--outfile string]");
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

	    WSDaliLiteService service =  new WSDaliLiteServiceLocator();
	    WSDaliLite dalilite = service.getWSDaliLite();
	    String result =  dalilite.checkStatus(jobid);
	    System.out.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.out.println("Use: java WSDaliLiteClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {
			
	     WSDaliLiteService service =  new WSDaliLiteServiceLocator();
	     WSDaliLite dalilite = service.getWSDaliLite();

	    String filename;	
	    String format="tooloutput";
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	 
            else filename = jobid; 

	    if (line.hasOption("ids")) {

             String[] ids = dalilite.getIds(jobid);

             for (int i=0;i<ids.length;i++){
		System.out.println(ids[i]);
	     } 
	     return 0;	
	    }
	
	    if (line.hasOption("outformat")) {

	     if (line.getOptionValue("outformat")=="xml") {format="toolxml";}
             byte[] resultbytes = dalilite.poll(jobid,format);
	     String result = new String(resultbytes);
	     if (line.hasOption("stdout")){ 
              System.out.println(result);
             } else {
              writeFile( new File(filename), result);
             }   

            } else {


 	     WSFile[] results = dalilite.getResults(jobid);

	     for (int i=0;i<results.length;i++){
		WSFile file = results[i];
		byte[] resultbytes = dalilite.poll(jobid,file.getType());
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

	    WSDaliLiteService service =  new WSDaliLiteServiceLocator();
	    WSDaliLite dalilite = service.getWSDaliLite();
	    String[] results = dalilite.getIds(jobid);
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

    if (line.hasOption("dbfetch")) {
	Data input= new Data();
	input.setType("dbfetch");
	input.setContent(line.getOptionValue("dbfetch"));
	inputs[0]=input;
    }

    if (line.hasOption("ebifile")) {
	Data input= new Data();
	input.setType("ebifile");
	input.setContent(line.getOptionValue("ebifile"));
	inputs[0]=input;
    }

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
    if (line.hasOption("pdb1")) params.setSequence1(line.getOptionValue("pdb1")); 	
    if (line.hasOption("pdb2")) params.setSequence2(line.getOptionValue("pdb2")); 
    if (line.hasOption("chain1")) params.setChainid1(line.getOptionValue("chain1")); 	
    if (line.hasOption("chain2")) params.setChainid2(line.getOptionValue("chain2")); 	
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
        

        options.addOption(OptionBuilder.withLongOpt("pdb1")
                                        .withValueSeparator(' ')
                                        .withDescription("PDB ID or PDF format file (.pdb,.ent,.dat,.brk)")
                                        .hasArg()
                                        .create("pdb1") );
        options.addOption(OptionBuilder.withLongOpt("chain1")
                                        .withValueSeparator(' ')
                                        .withDescription("Chain ID for structure 1")
                                        .hasArg()
                                        .create("chain1") );
        options.addOption(OptionBuilder.withLongOpt("pdb2")
                                        .withValueSeparator(' ')
                                        .withDescription("PDB ID or PDF format file (.pdb,.ent,.dat,.brk)")
                                        .hasArg()
                                        .create("pdb2") );
        options.addOption(OptionBuilder.withLongOpt("chain2")
                                        .withValueSeparator(' ')
                                        .withDescription("Chain ID for structure 2")
                                        .hasArg()
                                        .create("chain2") );
                
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

      if (line.hasOption("pdb1")&line.hasOption("pdb2")) {

        InputParams params = loadParams(line);
	    Data inputs[] = loadData(line);
	    String inputSeq = "";
	    WSDaliLiteService service =  new WSDaliLiteServiceLocator();
	    WSDaliLite dalilite = service.getWSDaliLite();
	    String jobid = dalilite.runDaliLite(params);

	    if (line.hasOption("async")) {
               System.out.println("This is your job id: "+ jobid);
	       System.out.println("Use: java WSDaliLiteClient --status --jobid "+ jobid + " to see the job status");
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
