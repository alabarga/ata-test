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

import uk.ac.ebi.webservices.WSWUBlast.*;

public class WSWUBlastClient {
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
	System.out.println("Use: java WSWUBlastClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence file");
	System.out.println("Use: java WSWUBlastClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence db:entry_id");
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

	    System.out.println("");
	    System.out.println(" Synchronous job:");
	    System.out.println("  The results/errors are returned as soon as the job is finished.");
	    System.out.println("  Usage: java WSWUBlastClient [options] --program PROGNAME --database DBNAME --sequence file [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSWUBlastClient [options] --program PROGNAME --database DBNAME --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSFastaClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSWUBlastClient --polljob --jobid string [--outfile string]");
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

	    WSWUBlastService service =  new WSWUBlastServiceLocator();
	    WSWUBlast wublast = service.getWSWUBlast();
	    String result =  wublast.checkStatus(jobid);
	    System.out.println(result);
	    System.err.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.err.println("Use: java WSWUBlastClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {
		
	     WSWUBlastService service =  new WSWUBlastServiceLocator();
	     WSWUBlast wublast = service.getWSWUBlast();

	    String filename = jobid;	
	    String format="tooloutput";
		
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	     

	    if (line.hasOption("ids")) {

         String[] ids = wublast.getIds(jobid);

         for (int i=0;i<ids.length;i++){
			 System.out.println(ids[i]);
	     } 
	     return 0;	
	    }
	
	    if (line.hasOption("outformat")) {

	     if (line.getOptionValue("outformat").equals("xml")) {format="toolxml";}
          
	     String result = new String(wublast.poll(jobid,format));
	     if (line.hasOption("stdout")){ 
              System.out.println(result);
             } else {
              writeFile( new File(filename), result);
             }   

        } else {


 	     WSFile[] results = wublast.getResults(jobid);

	     for (int i=0;i<results.length;i++){
		WSFile file = results[i];
		
		String result = new String(wublast.poll(jobid,file.getType()));		  
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

	    WSWUBlastService service =  new WSWUBlastServiceLocator();
	    WSWUBlast wublast = service.getWSWUBlast();
	    String[] results = wublast.getIds(jobid);
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

    if (line.hasOption("program")) params.setProgram(line.getOptionValue("program")); 	
    if (line.hasOption("database")) params.setDatabase(line.getOptionValue("database"));
    if (line.hasOption("matrix")) params.setMatrix(line.getOptionValue("matrix"));
    if (line.hasOption("sensitivity")) params.setSensitivity(line.getOptionValue("sensitivity"));
    if (line.hasOption("filter")) params.setFilter(line.getOptionValue("filter"));
    if (line.hasOption("sort")) params.setSort(line.getOptionValue("sort"));
    if (line.hasOption("stats")) params.setStats(line.getOptionValue("stats"));  
    if (line.hasOption("strand")) params.setStrand(line.getOptionValue("strand"));  
    if (line.hasOption("outformat")) params.setOutformat(line.getOptionValue("outformat")); 
    if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 

    if (line.hasOption("async")) params.setAsync(new Boolean(true)); 
    if (line.hasOption("echofilter")) params.setEchofilter(new Boolean(true));
    if (line.hasOption("exp")) params.setExp(new Float(line.getOptionValue("exp")));
    if (line.hasOption("scores")) params.setScores(Integer.valueOf(line.getOptionValue("scores"))); 
    if (line.hasOption("numal")) params.setNumal(Integer.valueOf(line.getOptionValue("numal"))); 
    if (line.hasOption("topcombon")) params.setTopcombon(Integer.valueOf(line.getOptionValue("tomcombon"))); 

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
        options.addOption(OptionBuilder.withLongOpt("program")
                                        .withDescription("name of the blast program to use blastn/blastp/blastx/tblastn/tblastx")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("program") );
options.addOption(OptionBuilder.withLongOpt("database")
                                .withDescription("uniprot/uniref100/uniref90/uniref50/\nuniparc/swissprot/ipi/prints/sgt/pdb/imgthlap")
                                .withValueSeparator(' ')
                                .hasArg()
                                .create("database") ); 
        options.addOption(OptionBuilder.withLongOpt("matrix")
                                        .withDescription("blosum62/blosum30/blosum35/.../blosum100/\nGONNET/pam10/pam20...pam500")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("matrix") );
        options.addOption(OptionBuilder.withLongOpt("exp")
                                        .withDescription("1.0/10/100/1000")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("exp") );
        options.addOption(OptionBuilder.withLongOpt("echoFilter")
                                        .withDescription("")
                                        .create("e") );
        options.addOption(OptionBuilder.withLongOpt("echofilter")
                                        .withValueSeparator(' ')
                                        .withDescription("seg/xnu/seg+xnu,none")
                                        .hasArg()
                                        .create("filter") );
        options.addOption(OptionBuilder.withLongOpt("numal")
                                        .withValueSeparator(' ')
                                        .withDescription("Number of alignments: 5-500")
                                        .hasArg()
                                        .create("numal") );
        options.addOption(OptionBuilder.withLongOpt("scores")
                                        .withValueSeparator(' ')
                                        .withDescription("5/10/20/50/100/150/200/250/300/350/400/450/500")
                                        .hasArg()
                                        .create("scores") );
        options.addOption(OptionBuilder.withLongOpt("sensitivity")
                                        .withValueSeparator(' ')
                                        .withDescription("low/medium/normal/high")
                                        .hasArg()
                                        .create("sensitivity") );
        options.addOption(OptionBuilder.withLongOpt("sort")
                                        .withValueSeparator(' ')
                                        .withDescription("pvalue/count/highscore/totalscore")
                                        .hasArg()
                                        .create("sort") );
        options.addOption(OptionBuilder.withLongOpt("stats")
                                        .withValueSeparator(' ')
                                        .withDescription("sump/poisson/kap")
                                        .hasArg()
                                        .create("stats") );
        options.addOption(OptionBuilder.withLongOpt("strand")
                                        .withDescription("")
                                        .hasArg()
                                        .create("strand") );
        options.addOption(OptionBuilder.withLongOpt("topcombon")
                                        .withValueSeparator(' ')
                                        .withDescription("1/2/3/4/5/50/100/1000/1e9")
                                        .hasArg()
                                        .create("topcombon") );

                
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

      if (line.hasOption("sequence")) {

        InputParams params = loadParams(line);
	    Data inputs[] = loadData(line);
	    String inputSeq = "";
	    WSWUBlastService service =  new WSWUBlastServiceLocator();
	    WSWUBlast wublast = service.getWSWUBlast();
	    String jobid = wublast.runWUBlast(params,inputs);

	    if (line.hasOption("async")) {
		   System.out.println(jobid); 
           System.err.println("This is your job id: "+ jobid);
	       System.err.println("Use: java WSWUBlastClient --status --jobid "+ jobid + " to see the job status");
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
