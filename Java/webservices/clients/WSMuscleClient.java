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

import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import javax.xml.rpc.encoding.DeserializerFactory;
import java.util.*;


import org.apache.commons.cli.*;

import uk.ac.ebi.webservices.WSMuscle.*;


public class WSMuscleClient {
  
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
	System.out.println("Use: java WSMuscleClient [options] --email your@email.com --sequence file");

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
	    System.out.println("");
	    System.out.println(" Synchronous job:");
	    System.out.println("  The results/errors are returned as soon as the job is finished.");
	    System.out.println("  Usage: java WSMuscleClient [options] --sequence file [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSMuscleClient [options] --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSMuscleClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSMuscleClient --polljob --jobid string [--outfile string]");
	    System.out.println("");
	    System.out.println(" [ Output files ]");
	    System.out.println("");
	    System.out.println("  .txt	 	: Program output");
	    System.out.println("  .xml		: XML output");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://www.ebi.ac.uk/muscle/muscle_frame.html");
     return 0;
    }

    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSMuscleService service =  new WSMuscleServiceLocator();
	    WSMuscle wsmuscle = service.getWSMuscle();
	    String result =  wsmuscle.checkStatus(jobid);
	    System.out.println(result);
	    System.err.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.err.println("Use: java WSMuscleClient -polljob -jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.err.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {

	    WSMuscleService service =  new WSMuscleServiceLocator();
	    WSMuscle wsmuscle = service.getWSMuscle();

	   if (line.hasOption("stdout")) {
		byte[] resultbytes = wsmuscle.poll(jobid,"tooloutput");
		String result = new String(resultbytes);		  
		System.out.println(result.trim());
       } else {
	    WSFile[] results = wsmuscle.getResults(jobid);
			
	    String filename;	
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	 
        else filename = jobid; 
		
	    for (int i=0;i<results.length;i++){
		WSFile file = results[i];
		byte[] resultbytes = wsmuscle.poll(jobid,file.getType());
		String result = new String(resultbytes);		  
		System.err.println("Printing result files: " + filename +"."+file.getExt());
		writeFile( new File(filename +"."+ file.getExt()), result.trim()); 
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
	input.setType("dbfetch");
	input.setContent(line.getOptionValue("dbfetch"));
	inputs[0]=input;
     } else {
     if ((new File(line.getOptionValue("sequence"))).exists()){
	Data input= new Data();
	input.setType("sequence");
	System.err.println("Reading file: "+line.getOptionValue("sequence"));
    String fileContent = readFile(new File(line.getOptionValue("sequence")));
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

    if (line.hasOption("outputtree")) params.setOutputtree(line.getOptionValue("outputtree"));
    if (line.hasOption("output")) params.setOutput(line.getOptionValue("output")); 
    else params.setOutput("clw"); 
    if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 
    if (line.hasOption("async")) params.setAsync(new Boolean(true)); 


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
        
        options.addOption(OptionBuilder.withLongOpt("output")
					.withDescription("output format ('clw' (default),'fasta','clwstrict', 'msf', 'phyi', 'phys')")
					.hasArg()
					.create("output") );
        
        options.addOption(OptionBuilder.withLongOpt("outputtree")
                                        .withDescription("specify iteration tree: tree1, tree2 ")
                                        .hasArg()
                                        .withValueSeparator(' ')
                                        .create("outputtree") );
        
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
	WSMuscleService service =  new WSMuscleServiceLocator();
	WSMuscle wsmuscle = service.getWSMuscle();
	String jobid = wsmuscle.runMuscle(params,inputs);

	if (line.hasOption("async")) {
         System.out.println(jobid);
         System.err.println("This is your job id: "+ jobid);
	 System.err.println("Use: java WSMuscleClient --status --jobid "+ jobid + " to see the job status");
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
