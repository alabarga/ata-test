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

import uk.ac.ebi.www.*;

public class WSNCBIBlastClient {
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
     HelpFormatter formatter = new HelpFormatter();
	 System.out.println("Use: java WSNCBIBlastClient --email your@email.com --database uniprot --program blastp --sequence protein1");
	 formatter.printHelp("blast", options);
     return 0;
    }

    private static int generalOptions()  {
    
	 System.out.println("Use: java WSNCBIBlastClient --email your@email.com --database uniprot --program blastp --sequence protein1");
     return 0;
    }


    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSNCBIBlastService service =  new WSNCBIBlastServiceLocator();
	    WSNCBIBlast blast = service.getWSNCBIBlast();
	    String result =  blast.checkStatus(jobid);
	    System.out.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.out.println("Use: java WSNCBIBlastClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid) throws IOException {
	 try {

	    WSNCBIBlastService service =  new WSNCBIBlastServiceLocator();
	    WSNCBIBlast blast = service.getWSNCBIBlast();
		
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
		
			WSFile[] results = blast.getResults(jobid);
				
			for (int i=0;i<results.length;i++){
				WSFile file = results[i];
				byte[] resultbytes = blast.poll(jobid,file.getType());
				String result = new String(resultbytes);		  
				System.out.println("Printing result files: " + jobid+"."+file.getExt());
				writeFile( new File(jobid+"."+ file.getExt()), result); 
			}
		}
	}
        catch (Exception e) {
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
	input.setType("dbfetch");
	input.setContent(line.getOptionValue("dbfetch"));
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
    if (line.hasOption("exp")) params.setExp(new Float(line.getOptionValue("exp"))); 
    if (line.hasOption("opengap")) params.setOpengap(Integer.valueOf(line.getOptionValue("opengap"))); 
    if (line.hasOption("extendgap")) params.setExtendgap(Integer.valueOf(line.getOptionValue("extendgap"))); 
    if (line.hasOption("dropoff")) params.setDropoff(Integer.valueOf(line.getOptionValue("dropoff"))); 
    if (line.hasOption("align")) params.setAlign(Integer.valueOf(line.getOptionValue("align"))); 
    if (line.hasOption("scores")) params.setScores(Integer.valueOf(line.getOptionValue("scores"))); 
    if (line.hasOption("numal")) params.setNumal(Integer.valueOf(line.getOptionValue("numal"))); 
    if (line.hasOption("gapalign")) params.setGapalign("true"); 
    if (line.hasOption("filter")) params.setFilter("true");

    
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
               .withDescription("blosum62/blosum100/pam10/pam30/pam70")
               .withValueSeparator(' ')
                                        .hasArg()
                                        .create("matrix") );
        options.addOption(OptionBuilder.withLongOpt("exp")
                                        .withDescription("0.001-1000 (default 10)")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("exp") );
        options.addOption(OptionBuilder.withLongOpt("filter")
                                        .withDescription("default false")
                                        .create("filter") );
        options.addOption(OptionBuilder.withLongOpt("gapalign")
                                        .withDescription("default false")
                                        .create("gapalign") );
        options.addOption(OptionBuilder.withLongOpt("align")
                                        .withValueSeparator(' ')
                                        .withDescription("0-4 (default 0)")
                                        .hasArg()
                                        .create("align") );
        options.addOption(OptionBuilder.withLongOpt("scores")
                                        .withValueSeparator(' ')
                                        .withDescription("5-500 default 100")
                                        .hasArg()
                                        .create("scores") );
        options.addOption(OptionBuilder.withLongOpt("numal")
                                        .withValueSeparator(' ')
                                        .withDescription("5-500 default 50")
                                        .hasArg()
                                        .create("numal") );
        
        options.addOption(OptionBuilder.withLongOpt("opengap")
                                        .withValueSeparator(' ')
                                        .withDescription("0-10 default 0")
                                        .hasArg()
                                        .create("opengap") );
        options.addOption(OptionBuilder.withLongOpt("extendgap")
                                        .withValueSeparator(' ')
                                        .withDescription("0-2 default 0")
                                        .hasArg()
                                        .create("extendgap") );

        //****************************************************************************
	// common options for EBI clients 
	//****************************************************************************
	
    options.addOption( "help","help",false, "help on using this client");
    options.addOption( "async","async",false, "perform an asynchronous job");
    options.addOption( "polljob","polljob", false, "poll for the status of an asynchronous job and get the results");
    options.addOption( "status","status", false, "poll for the status of an asynchronous job");   
    options.addOption( "ids","ids",false, "retrieve only identifiers");
    options.addOption( "stdout","stdout",false, "print to standard output");
	
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
    options.addOption(OptionBuilder.withLongOpt("outformat")
                                        .withValueSeparator(' ')
                                        .withDescription("txt/xml")
                                        .hasArg()
                                        .create("outformat") );
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
 		getResults(jobid);
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
	    WSNCBIBlastService service =  new WSNCBIBlastServiceLocator();
	    WSNCBIBlast blast = service.getWSNCBIBlast();
	    String jobid = blast.runNCBIBlast(params,inputs);

	  if (line.hasOption("async")) {
             System.out.println("This is your job id: "+ jobid);
	     System.out.println("Use: java WSNCBIBlastClient --status --jobid "+ jobid + " to see the job status");
	  } else {
             getResults(jobid);
	  }	
      } else {

        usage(options);
		generalOptions();	
	    	
 		    
        System.exit(0);
      }
     }
    }
        catch (ParseException exp) {
            System.out.println( "Parsing failed. Reason: " + exp.getMessage() );
            usage(options);
	    generalOptions();
        }
        catch (Exception e) {
             System.err.println ("ERROR:\n" + e.toString());
	    e.printStackTrace();
	}
    }
}
