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

import uk.ac.ebi.webservices.WSFasta.*;

public class WSFastaClient {

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
	System.out.println("Use: java WSFastaClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence file");
	System.out.println("     java WSFastaClient [options] --email your@email.com --program PROGNAME --database DBNAME --sequence db:entry_id");
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
	    System.out.println("  Usage: java WSFastaClient [options] --program PROGNAME --database DBNAME --sequence file [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSFastaClient [options] --program PROGNAME --database DBNAME --sequence file --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSFastaClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSFastaClient --polljob --jobid string [--outfile string] [--outformat xml] [--stdout]");
	    System.out.println("");
	    System.out.println(" [ Output files ]");
	    System.out.println("");
	    System.out.println("  .txt	 	: Program output");
	    System.out.println("  .xml		: XML output");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://www.ebi.ac.uk/fasta33/fasta33_help.html");
     return 0;
    }

    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSFastaService service =  new WSFastaServiceLocator();
	    WSFasta wsfasta = service.getWSFasta();
	    String result =  wsfasta.checkStatus(jobid);
	    System.out.println(result);
	    System.err.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.err.println("Use: java WSFastaClient --polljob --jobid "+ jobid + " to see the results");	

        }
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
   }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
	 try {

	    WSFastaService service =  new WSFastaServiceLocator();
	    WSFasta wsfasta = service.getWSFasta();
	    
	    String filename;
		String format = "tooloutput";	
	    if (line.hasOption("outfile")) filename = line.getOptionValue("outfile"); 	 
        else filename = jobid;

	    if (line.hasOption("outformat")) {

	     if (line.getOptionValue("outformat").equals("xml")) {format="toolxml";}
          
	     String result = new String(wsfasta.poll(jobid,format));
	     if (line.hasOption("stdout")){ 
             System.out.println(result);
         } else {
             writeFile( new File(filename), result);
         }

        } else {


 	     WSFile[] results = wsfasta.getResults(jobid);

	      for (int i=0;i<results.length;i++){
		   WSFile file = results[i];
		   String result = new String(wsfasta.poll(jobid,file.getType()));
		   System.out.println("Printing result files: " + filename +"."+file.getExt());
		   writeFile( new File(filename+"."+ file.getExt()), result);
	      }
	     }
	    }  catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
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
 byte[] fileContent = new byte[1024000];

 try {
    params.setMoltype("protein");
    if (line.hasOption("program")) params.setProgram(line.getOptionValue("program")); 	
    if (line.hasOption("database")) params.setDatabase(line.getOptionValue("database"));
    if (line.hasOption("protein")) params.setMoltype("protein");
    if (line.hasOption("rna")) params.setMoltype("rna");
    if (line.hasOption("dna")) params.setMoltype("dna");
    if (line.hasOption("nucleotide")) params.setMoltype("dna");
    if (line.hasOption("matrix")) params.setMatrix(line.getOptionValue("matrix"));
    if (line.hasOption("elower")) params.setElower(new Float(line.getOptionValue("elower")));
    if (line.hasOption("eupper")) params.setEupper(new Float(line.getOptionValue("eupper")));
    if (line.hasOption("scores")) params.setScores(Integer.valueOf(line.getOptionValue("scores"))); 
    if (line.hasOption("ktup")) params.setKtup(Integer.valueOf(line.getOptionValue("ktup"))); 
    if (line.hasOption("alignments")) params.setAlignments(Integer.valueOf(line.getOptionValue("alignments"))); 

    if (line.hasOption("gapext")) params.setGapext(Integer.valueOf(line.getOptionValue("gapext"))); 
    if (line.hasOption("gapopen")) params.setGapopen(Integer.valueOf(line.getOptionValue("gapopen"))); 

    if (line.hasOption("dbrange")) params.setDbrange(line.getOptionValue("dbrange"));
    if (line.hasOption("seqrange")) params.setSeqrange(line.getOptionValue("seqrange"));
    if (line.hasOption("histogram")) params.setHistogram(new Boolean(true));
    if (line.hasOption("topstrand")) params.setTopstrand(new Boolean(true));
    if (line.hasOption("bottomstrand")) params.setBottomstrand(new Boolean(true));

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
        options.addOption(OptionBuilder.withLongOpt("program")
                                        .withDescription("fasta3/fastx3/fasty3/fastf3/fasts3/tfastx3/tfasty3")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("program") );
options.addOption(OptionBuilder.withLongOpt("database")
.withDescription("Protein:\nuniprot/uniref100/uniref90/\nuniref50/uniparc/swissprot/\nipi/prints/sgt/pdb/imgthlap/\nepop/jpop/uspop\n\nNucleotide:\nemfun/eminv/emhum/emmam/\nemorg/emphg/empln/empro/emrod/\nemmus/emsts/emsyn/emunc/emvrl/\nemvrt/emest/emgss/emhtg/empat/\nevec/emnew/emall/imgtligm/imgthla/hgvbase")
.hasArg()
.create("database") );
        options.addOption(OptionBuilder.withLongOpt("histogram")
                                        .withDescription("")
                                        .create("histogram") );
        options.addOption(OptionBuilder.withLongOpt("nucleotide")
                                        .withDescription("")
                                        .create("nucleotide") );
        options.addOption(OptionBuilder.withLongOpt("topstrand")
                                        .withDescription("only for DNA sequence comparison against DNA databanks\nsequence will be searched as it is input into the form")
                                        .create("topstrand") );
        options.addOption(OptionBuilder.withLongOpt("bottomstrand")
                                        .withDescription("only for DNA sequence comparison against DNA databanks\nreverse and complement your input sequence")
                                        .create("bottomstrand") );
       options.addOption(OptionBuilder.withLongOpt("gapopen")
                                        .withDescription("Penalty for the first residue in a gap\n0/-2/-4/-6/-8/-10/-12/-14/-16/-18")
                                        .withValueSeparator('=')
                                        .hasArg()
                                        .create("gapopen"));

        options.addOption(OptionBuilder.withLongOpt("gapext")
                                        .withDescription("Penalty for additional residues in a gap\n0/-4/-6/-8")
                                        .withValueSeparator('=')
                                        .hasArg()
                                        .create("gapext") );
        options.addOption(OptionBuilder.withLongOpt("scores")
                                        .withDescription(" maximum number of reported scores in the output file\n10/20/30/40/50/60/70/80/90/100")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("scores") );
        options.addOption(OptionBuilder.withLongOpt("alignments")
                                        .withDescription("set the maximum number of reported alignments in the output file\n10/20/30/40/50/60/70/80/90/100")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("alignments") );
        options.addOption(OptionBuilder.withLongOpt("ktup")
                                        .withDescription(" limit the word-length the search should use\n1/2")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("ktup") );
        options.addOption(OptionBuilder.withLongOpt("matrix")
                                        .withDescription("To set comparison matrix when searching the database\nBL50/BL62/BL80/P120/P250/M10/M20/M40")
                                        .hasArg()
                                        .withValueSeparator(' ')
                                        .create("matrix") );
        options.addOption(OptionBuilder.withLongOpt("eupper")
                                        .withDescription("1.0/1e-600/1e-300/1e-100/1e-50/1e-10/1e-5/\n0.0001/0.1/1.0/2.0/5.0/10.0/20.0/50")
                                        .hasArg()
                                        .withValueSeparator(' ')
                                        .create("eupper") );
        options.addOption(OptionBuilder.withLongOpt("elower")
                                        .withDescription("1.0/1e-600/1e-300/1e-100/1e-50/1e-10/1e-5/\n0.0001/0.1/1.0/2.0/5.0/10.0/20.0/50")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("elower") );
        options.addOption(OptionBuilder.withLongOpt("dbrange")
                                        .withDescription("sets the sequence range to search within the database")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("dbrange") );
        options.addOption(OptionBuilder.withLongOpt("seqrange")
                                        .withDescription("region within the query sequence should be searched")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("seqrange") );
        options.addOption(OptionBuilder.withLongOpt("outformat")
                                        .withDescription("txt/xml")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("outformat") );
        options.addOption(OptionBuilder.withLongOpt("sequence")
                                        .withDescription("path to a sequence file/string of format database:acc.no")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("sequence") );
        options.addOption( "help","help", false, "help on using this client");
        options.addOption( "async","async",false, "perform an asynchronous job");
        options.addOption( "stdout","stdout",false, "print to standard output");
		options.addOption( "polljob","polljob", false, "poll for the status of an asynchronous job");
	    options.addOption( "status","status", false, "poll for the status of an asynchronous job");
        options.addOption( "protein","protein", false, "sequence is a protein");       
        options.addOption( "dna","dna", false, "sequence is dna");    
        options.addOption( "rna","rna", false, "sequence is rna");
     
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
      } else {

      if (line.hasOption("sequence")||line.hasOption("dbfetch")||line.hasOption("ebifile")) {

       	InputParams params = loadParams(line);
	    Data inputs[] = loadData(line);
	    WSFastaService service =  new WSFastaServiceLocator();
	    WSFasta fasta = service.getWSFasta();
	    String jobid = fasta.runFasta(params,inputs);

	    if (line.hasOption("async")) {
	      System.out.println(jobid);
          System.err.println("This is your job id: "+ jobid);
	      System.err.println("Use: java WSFastaClient --status --jobid "+ jobid + " to see the job status");
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
