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
//import java.util.HashTable;

import java.util.Iterator;
import java.util.regex.*;

import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import javax.xml.rpc.encoding.DeserializerFactory;
//import org.apache.soap.encoding.soapenc.MapSerializer;
import java.util.*;


import org.apache.commons.cli.*;
import uk.ac.ebi.webservices.WSEmboss.*;

public class WSEmbossClient {
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

    private static int writeFile(File file, byte[] data) throws IOException {
    
        OutputStream os = new FileOutputStream(file);
	    os.write(data);
        return 0;
    }

	
    private static int usage(Options options)  {
     HelpFormatter formatter = new HelpFormatter();
     System.out.println("Use: 'java WSEmbossClient  --info all' for a list of available programs");
     System.out.println("Use: 'java WSEmbossClient  --info toolname' for a list of options for that tool");
     System.out.println("Use: 'java WSEmbossClient  --tool water --asequence protein1 --bsequence protein2 --email your@email.com' ");
	  
     System.exit(0);
     return 0;
    }


    private static int checkStatus(String jobid) throws IOException {
	 try {

	    WSEmbossService service =  new WSEmbossServiceLocator();
	    WSEmboss emboss = service.getWSEmboss();
	    String result =  emboss.checkStatus(jobid);
	    System.out.println("This is your job id: "+ jobid+ " ( " +result+" )");
	    System.out.println("Use: java WSEmbossClient --polljob --jobid "+ jobid + " to see the results");	

	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }	

    private static int getResults(String jobid, CommandLine line) throws IOException {
	try {

	    WSEmbossService service =  new WSEmbossServiceLocator();
	    WSEmboss emboss = service.getWSEmboss();
	    WSFile[] results = emboss.getResults(jobid);
	    String outfile;
	    if (line.hasOption("outfile")) outfile=line.getOptionValue("outfile");
	    else outfile=jobid;

	    if (line.hasOption("stdout")) {
	      String result = new String(emboss.poll(jobid,"tooloutput"));
	      System.out.println(result.trim());      
        } else {
	       for (int i=0;i<results.length;i++){
		    WSFile file = results[i];
		    System.err.println("Printing result files: " + outfile +"."+file.getExt());
		    if (file.getExt().equals("png")) writeFile( new File(outfile+"."+ file.getExt()), emboss.poll(jobid,file.getType()));
		    else {
		     String result = new String(emboss.poll(jobid,file.getType()));
		     writeFile( new File(outfile+"."+ file.getExt()), result.trim());
		     //if (file.getExt().equals("txt")) System.out.println(result.trim());
            }

	     }
        }
	} catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
    }
	return 0;
}

private static int getTools() throws IOException {
	 try {

	    WSEmbossService service =  new WSEmbossServiceLocator();
	    WSEmboss emboss = service.getWSEmboss();
	    String[] results = emboss.getTools();
	    System.out.println("Available programs: ");		
	    for (int i=0;i<results.length;i++){		
		System.out.println(results[i]); 
	    }
	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }

   private static int getInfo(String tool) throws IOException {
	try {

	    WSEmbossService service =  new WSEmbossServiceLocator();
	    WSEmboss emboss = service.getWSEmboss();
	    String results =  new String( emboss.getInfo(tool) );
	    System.out.println(results);

          if (!tool.equals("all")) {

	    System.out.println("");
     	System.out.println(" [ General options ]");	
	    System.out.println("");
    	System.out.println(" --help    :         : prints this help text");
	    System.out.println(" --async   :         : forces to make an asynchronous query");	
	    System.out.println(" --status  :         : poll for the status of a job");
	    System.out.println(" --polljob :         : poll for the results of a job");
	    System.out.println(" --stdout :          : print results to standard output");
	    System.out.println(" --jobid   : string  : jobid that was returned when an asynchronous job was submitted.");
	    System.out.println(" --outfile : string  : name of the file results should be written to (default is jobid)");
	    System.out.println("");
	    System.out.println(" Synchronous job:");
	    System.out.println("  The results/errors are returned as soon as the job is finished.");
	    System.out.println("  Usage: java WSEmbossClient [options] [--outfile string]");
	    System.out.println("  Returns : saves the results to disk");
	    System.out.println("");
	    System.out.println(" Asynchronous job:");
	    System.out.println("  Use this if you want to retrieve the results at a later time. The results are stored for up to 24 hours. ");
	    System.out.println("  The asynchronous submission mode is recommended when users are submitting batch jobs or large database searches");	
	    System.out.println("  Use: java WSEmbossClient [options] --async");
	    System.out.println("  Returns : jobid");
	    System.out.println("");
	    System.out.println("  Use the jobid to query for the status of the job. ");
	    System.out.println("  Use: java WSEmbossClient --status --jobid string");
	    System.out.println("  Returns : string indicating the status of the job (DONE, RUNNING, NOT_FOUND, ERROR).");
	    System.out.println("");
	    System.out.println("  When done, WSEmbossClient --polljob --jobid string [--outfile string] [--stdout]");
	    System.out.println("");
	    System.out.println("  [ help ]");
	    System.out.println("    For more detailed help information refer to http://emboss.sourceforge.net/docs/#Tutorials");		
         }
	}
        catch (Exception e) {
            System.out.println("ERROR:\n" + e.toString());
            e.printStackTrace();
        }
	return 0;
     }		

private static Data[] loadData(CommandLine line) throws IOException {

 Data[] inputs = new Data[2];

 int cur_in=0;
 int k;
 String[] in = new String[2];
 String[] i = new String[2];

 try {

  Iterator ii = line.iterator();
  while(ii.hasNext()) {
   Option opt = (Option)ii.next();
   Data input= new Data();
   if (opt.hasArg() && (new File(opt.getValue())).exists()){
    input.setType(opt.getLongOpt());
    String fileContent = readFile(new File(opt.getValue()));
	input.setContent(fileContent);
	inputs[cur_in++]=input;
   }
  }
 }
 catch (Exception e) {
   System.out.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }


 try {

    String[] more = line.getArgs();
    int start=1;	
    if (line.hasOption("tool")) start=0;
    if (more.length>start) {
      for (k=start;k<more.length;k++)
       if ((new File(more[k])).exists()){
        Data input= new Data();
	    input.setType("usrfile");
        String fileContent = readFile(new File(more[k]));
	    input.setContent(fileContent);
	    inputs[cur_in++]=input;
       } else {
        Data input= new Data();
	    input.setType("usrinput");
	    input.setContent(more[k]);
	    inputs[cur_in++]=input;
	   }
    } 

 }
 catch (Exception e) {
   System.out.println("ERROR:\n" + e.toString());
   e.printStackTrace();
 }
 return inputs;
}

private static Data[] checkInput(String[] in) throws IOException {

 Data[] inputs = new Data[2];

 try {

  Data input= new Data();
  String fileContent;
  for (int k=0;k<in.length;k++) {
   if ((new File(in[k])).exists()){
	input.setType("sequence");
    fileContent = readFile(new File(in[k]));
	input.setContent(fileContent);
	inputs[k]=input;
   }

   Pattern p = Pattern.compile("([a-zA-Z]*):(\\w*)");
   Matcher m = p.matcher(in[k]);
   if (m.matches()) {
	input.setType("dbfetch");
	input.setContent(in[k]);
	inputs[k]=input;
   }
   Pattern p2 = Pattern.compile("([a-zA-Z]*)-([0-9]*)-([0-9]*)");
   Matcher m2 = p2.matcher(in[k]);
   if (m2.matches()) {
	input.setType("ebifile");
	input.setContent(in[k]);
	inputs[k]=input;
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

if (line.hasOption("blocktype")) params.setBlocktype(line.getOptionValue("blocktype"));
if (line.hasOption("entry")) params.setEntry(line.getOptionValue("entry"));
if (line.hasOption("sequence")) params.setSequence(line.getOptionValue("sequence"));
if (line.hasOption("files")) params.setFiles(line.getOptionValue("files"));
if (line.hasOption("vectorfile")) params.setVectorfile(line.getOptionValue("vectorfile"));
if (line.hasOption("nummismatches")) params.setNummismatches(Integer.valueOf(line.getOptionValue("nummismatches")));
if (line.hasOption("program")) params.setProgram(line.getOptionValue("program"));
if (line.hasOption("minlen")) params.setMinlen(Integer.valueOf(line.getOptionValue("minlen")));
if (line.hasOption("saltconc")) params.setSaltconc(new Float(line.getOptionValue("saltconc")));
if (line.hasOption("primersfile")) params.setPrimersfile(line.getOptionValue("primersfile"));
if (line.hasOption("target")) params.setTarget(line.getOptionValue("target"));
if (line.hasOption("gaplimit")) params.setGaplimit(Integer.valueOf(line.getOptionValue("gaplimit")));
if (line.hasOption("wordsize")) params.setWordsize(Integer.valueOf(line.getOptionValue("wordsize")));
if (line.hasOption("shiftincrement")) params.setShiftincrement(Integer.valueOf(line.getOptionValue("shiftincrement")));
if (line.hasOption("count")) params.setCount(Integer.valueOf(line.getOptionValue("count")));
if (line.hasOption("second")) params.setSecond(line.getOptionValue("second"));
if (line.hasOption("btype")) params.setBtype(line.getOptionValue("btype"));
if (line.hasOption("name")) params.setName(line.getOptionValue("name"));
if (line.hasOption("minpc")) params.setMinpc(new Float(line.getOptionValue("minpc")));
if (line.hasOption("match")) params.setMatch(Integer.valueOf(line.getOptionValue("match")));
if (line.hasOption("estsequence")) params.setEstsequence(line.getOptionValue("estsequence"));
if (line.hasOption("description")) params.setDescription(line.getOptionValue("description"));
if (line.hasOption("winsize")) params.setWinsize(Integer.valueOf(line.getOptionValue("winsize")));
if (line.hasOption("minpallen")) params.setMinpallen(Integer.valueOf(line.getOptionValue("minpallen")));
if (line.hasOption("regions")) params.setRegions(line.getOptionValue("regions"));
if (line.hasOption("emin")) params.setEmin(Integer.valueOf(line.getOptionValue("emin")));
if (line.hasOption("sitelen")) params.setSitelen(Integer.valueOf(line.getOptionValue("sitelen")));
if (line.hasOption("number")) params.setNumber(Integer.valueOf(line.getOptionValue("number")));
if (line.hasOption("threshold")) params.setThreshold(Integer.valueOf(line.getOptionValue("threshold")));
if (line.hasOption("seqall")) params.setSeqall(line.getOptionValue("seqall"));
if (line.hasOption("range")) params.setRange(line.getOptionValue("range"));
if (line.hasOption("frames")) params.setFrames(line.getOptionValue("frames"));
if (line.hasOption("firstset")) params.setFirstset(line.getOptionValue("firstset"));
if (line.hasOption("genomesequence")) params.setGenomesequence(line.getOptionValue("genomesequence"));
if (line.hasOption("to")) params.setTo(Integer.valueOf(line.getOptionValue("to")));
if (line.hasOption("maxrange")) params.setMaxrange(Integer.valueOf(line.getOptionValue("maxrange")));
if (line.hasOption("boutfeat")) params.setBoutfeat(line.getOptionValue("boutfeat"));
if (line.hasOption("from")) params.setFrom(Integer.valueOf(line.getOptionValue("from")));
if (line.hasOption("minrange")) params.setMinrange(Integer.valueOf(line.getOptionValue("minrange")));
if (line.hasOption("pos")) params.setPos(Integer.valueOf(line.getOptionValue("pos")));
if (line.hasOption("overlap")) params.setOverlap(new Boolean(true));
if (line.hasOption("emax")) params.setEmax(Integer.valueOf(line.getOptionValue("emax")));
if (line.hasOption("graphlb")) params.setGraphlb(line.getOptionValue("graphlb"));
if (line.hasOption("dnaconc")) params.setDnaconc(new Float(line.getOptionValue("dnaconc")));
if (line.hasOption("secondset")) params.setSecondset(line.getOptionValue("secondset"));
if (line.hasOption("skip")) params.setSkip(Integer.valueOf(line.getOptionValue("skip")));
if (line.hasOption("exclude")) params.setExclude(line.getOptionValue("exclude"));
if (line.hasOption("gapopen")) params.setGapopen(new Float(line.getOptionValue("gapopen")));
if (line.hasOption("motif")) params.setMotif(line.getOptionValue("motif"));
if (line.hasOption("search")) params.setSearch(line.getOptionValue("search"));
if (line.hasOption("bsequence")) params.setBsequence(line.getOptionValue("bsequence"));
if (line.hasOption("aoutfeat")) params.setAoutfeat(line.getOptionValue("aoutfeat"));
if (line.hasOption("infile")) params.setInfile(line.getOptionValue("infile"));
if (line.hasOption("directory")) params.setDirectory(line.getOptionValue("directory"));
if (line.hasOption("asequence")) params.setAsequence(line.getOptionValue("asequence"));
if (line.hasOption("maxpallen")) params.setMaxpallen(Integer.valueOf(line.getOptionValue("maxpallen")));
if (line.hasOption("orfml")) params.setOrfml(Integer.valueOf(line.getOptionValue("orfml")));
if (line.hasOption("gap")) params.setGap(Integer.valueOf(line.getOptionValue("gap")));
if (line.hasOption("mismatch")) params.setMismatch(Integer.valueOf(line.getOptionValue("mismatch")));
if (line.hasOption("order")) params.setOrder(line.getOptionValue("order"));
if (line.hasOption("posticks")) params.setPosticks(line.getOptionValue("posticks"));
if (line.hasOption("tolerance")) params.setTolerance(new Float(line.getOptionValue("tolerance")));
if (line.hasOption("weight")) params.setWeight(Integer.valueOf(line.getOptionValue("weight")));
if (line.hasOption("minrepeat")) params.setMinrepeat(Integer.valueOf(line.getOptionValue("minrepeat")));
if (line.hasOption("minweight")) params.setMinweight(new Float(line.getOptionValue("minweight")));
if (line.hasOption("windowsize")) params.setWindowsize(Integer.valueOf(line.getOptionValue("windowsize")));
if (line.hasOption("besthits")) params.setBesthits(new Boolean(true));
if (line.hasOption("maxrepeat")) params.setMaxrepeat(Integer.valueOf(line.getOptionValue("maxrepeat")));
if (line.hasOption("word")) params.setWord(Integer.valueOf(line.getOptionValue("word")));
if (line.hasOption("shift")) params.setShift(Integer.valueOf(line.getOptionValue("shift")));
if (line.hasOption("type")) params.setType(line.getOptionValue("type"));
if (line.hasOption("graph")) params.setGraph(line.getOptionValue("graph"));
if (line.hasOption("window")) params.setWindow(Integer.valueOf(line.getOptionValue("window")));
if (line.hasOption("ruler")) params.setRuler(new Boolean(true));
if (line.hasOption("enzymes")) params.setEnzymes(line.getOptionValue("enzymes"));
if (line.hasOption("enzyme")) params.setEnzyme(line.getOptionValue("enzyme"));
if (line.hasOption("compdatafile")) params.setCompdatafile(line.getOptionValue("compdatafile"));
if (line.hasOption("sequences")) params.setSequences(line.getOptionValue("sequences"));
if (line.hasOption("gapextend")) params.setGapextend(new Float(line.getOptionValue("gapextend")));
if (line.hasOption("posblocks")) params.setPosblocks(line.getOptionValue("posblocks"));
if (line.hasOption("letters")) params.setLetters(line.getOptionValue("letters"));
if (line.hasOption("first")) params.setFirst(line.getOptionValue("first"));
if (line.hasOption("atype")) params.setAtype(line.getOptionValue("atype"));
if (line.hasOption("menu")) params.setMenu(line.getOptionValue("menu"));
if (line.hasOption("score")) params.setScore(Integer.valueOf(line.getOptionValue("score")));
if (line.hasOption("mismatchpercent")) params.setMismatchpercent(Integer.valueOf(line.getOptionValue("mismatchpercent")));
if (line.hasOption("graphout")) params.setGraphout(line.getOptionValue("graphout"));
if (line.hasOption("pattern")) params.setPattern(line.getOptionValue("pattern"));
if (line.hasOption("point")) params.setPoint(line.getOptionValue("point"));
if (line.hasOption("format")) params.setFormat(line.getOptionValue("format"));
if (line.hasOption("seqcomp")) params.setSeqcomp(line.getOptionValue("seqcomp"));
if (line.hasOption("block")) params.setBlock(line.getOptionValue("block"));
if (line.hasOption("cfile")) params.setCfile(line.getOptionValue("cfile"));
if (line.hasOption("minoe")) params.setMinoe(new Float(line.getOptionValue("minoe")));


 if (line.hasOption("email")) params.setEmail(line.getOptionValue("email")); 
 if (line.hasOption("async")) params.setAsync(new Boolean(true));

 String[] more = line.getArgs();
 if (line.hasOption("tool")) params.setTool(line.getOptionValue("tool"));
 else params.setTool(more[0]);

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
       
 options.addOption(OptionBuilder.withLongOpt("excludedregion").withDescription("Excluded region(s)")
                                .withValueSeparator(' ').hasArg().create("excludedregion") );
 options.addOption(OptionBuilder.withLongOpt("files").withDescription("Comma separated file list")
                                .withValueSeparator(' ').hasArg().create("files") );
 options.addOption(OptionBuilder.withLongOpt("date").withDescription("Index date")
                                .withValueSeparator(' ').hasArg().create("date") );
 options.addOption(OptionBuilder.withLongOpt("explode").withDescription("Use the expanded group names")
                                .withValueSeparator(' ').create("explode") );
 options.addOption(OptionBuilder.withLongOpt("maxsize").withDescription("Maximum nucleotide size of ORF to report")
                                .withValueSeparator(' ').hasArg().create("maxsize") );
 options.addOption(OptionBuilder.withLongOpt("program").withDescription("Program to search for")
                                .withValueSeparator(' ').hasArg().create("program") );
 options.addOption(OptionBuilder.withLongOpt("aahydropathy").withDescription("Hydropathy data filename")
                                .withValueSeparator(' ').hasArg().create("aahydropathy") );
 options.addOption(OptionBuilder.withLongOpt("dbname").withDescription("Database name")
                                .withValueSeparator(' ').hasArg().create("dbname") );
 options.addOption(OptionBuilder.withLongOpt("minlen").withDescription("Minimum length")
                                .withValueSeparator(' ').hasArg().create("minlen") );
 options.addOption(OptionBuilder.withLongOpt("primersfile").withDescription("Primer file")
                                .withValueSeparator(' ').hasArg().create("primersfile") );
 options.addOption(OptionBuilder.withLongOpt("docolour").withDescription("Colour residues by table oily, amide etc.")
                                .withValueSeparator(' ').create("docolour") );
 options.addOption(OptionBuilder.withLongOpt("saltconc").withDescription("Enter salt concentration (mM)")
                                .withValueSeparator(' ').hasArg().create("saltconc") );
 options.addOption(OptionBuilder.withLongOpt("prokaryote").withDescription("Use prokaryotic cleavage data")
                                .withValueSeparator(' ').create("prokaryote") );
 options.addOption(OptionBuilder.withLongOpt("target").withDescription("Sequence section to match")
                                .withValueSeparator(' ').hasArg().create("target") );
 options.addOption(OptionBuilder.withLongOpt("protofile").withDescription("Full pathname of PROTO file")
                                .withValueSeparator(' ').hasArg().create("protofile") );
 options.addOption(OptionBuilder.withLongOpt("showscore").withDescription("Print residue scores")
                                .withValueSeparator(' ').hasArg().create("showscore") );
 options.addOption(OptionBuilder.withLongOpt("gaplimit").withDescription("Enter maximum gap between repeated regions")
                                .withValueSeparator(' ').hasArg().create("gaplimit") );
 options.addOption(OptionBuilder.withLongOpt("forwardinput").withDescription("Forward input primer sequence to check")
                                .withValueSeparator(' ').hasArg().create("forwardinput") );
 options.addOption(OptionBuilder.withLongOpt("wordsize").withDescription("Word size")
                                .withValueSeparator(' ').hasArg().create("wordsize") );
 options.addOption(OptionBuilder.withLongOpt("boxcolval").withDescription("Colour to be used for background. (GREY)")
                                .withValueSeparator(' ').hasArg().create("boxcolval") );
 options.addOption(OptionBuilder.withLongOpt("alternatives").withDescription("Number of alternative matches")
                                .withValueSeparator(' ').hasArg().create("alternatives") );
 options.addOption(OptionBuilder.withLongOpt("addlast").withDescription("Force the sequence to end with an asterisk")
                                .withValueSeparator(' ').create("addlast") );
 options.addOption(OptionBuilder.withLongOpt("collision").withDescription("Allow collisions in calculating consensus")
                                .withValueSeparator(' ').create("collision") );
 options.addOption(OptionBuilder.withLongOpt("count").withDescription("Number of times to perform the mutation operations")
                                .withValueSeparator(' ').hasArg().create("count") );
 options.addOption(OptionBuilder.withLongOpt("trim").withDescription("Trim trailing X's and *'s")
                                .withValueSeparator(' ').create("trim") );
 options.addOption(OptionBuilder.withLongOpt("datafile").withDescription("Scoring matrix")
                                .withValueSeparator(' ').hasArg().create("datafile") );
 options.addOption(OptionBuilder.withLongOpt("anglesfile").withDescription("Angles data file")
                                .withValueSeparator(' ').hasArg().create("anglesfile") );
 options.addOption(OptionBuilder.withLongOpt("btype").withDescription("Type of second feature")
                                .withValueSeparator(' ').hasArg().create("btype") );
 options.addOption(OptionBuilder.withLongOpt("scorefile").withDescription("Comparison matrix file")
                                .withValueSeparator(' ').hasArg().create("scorefile") );
 options.addOption(OptionBuilder.withLongOpt("name").withDescription("Name of the sequence")
                                .withValueSeparator(' ').hasArg().create("name") );
 options.addOption(OptionBuilder.withLongOpt("minpc").withDescription("Minimum percentage")
                                .withValueSeparator(' ').hasArg().create("minpc") );
 options.addOption(OptionBuilder.withLongOpt("estsequence").withDescription("EST sequence(s)")
                                .withValueSeparator(' ').hasArg().create("estsequence") );
 options.addOption(OptionBuilder.withLongOpt("equivalences").withDescription("Create prototype equivalence file")
                                .withValueSeparator(' ').create("equivalences") );
 options.addOption(OptionBuilder.withLongOpt("regions").withDescription("Regions to extract (eg: 4-57,78-94)")
                                .withValueSeparator(' ').hasArg().create("regions") );
 options.addOption(OptionBuilder.withLongOpt("thermo").withDescription("Thermodynamic calculations")
                                .withValueSeparator(' ').hasArg().create("thermo") );
 options.addOption(OptionBuilder.withLongOpt("translate").withDescription("Regions to translate (eg: 4-57,78-94)")
                                .withValueSeparator(' ').hasArg().create("translate") );
 options.addOption(OptionBuilder.withLongOpt("emin").withDescription("Minimum number of elements per fingerprint")
                                .withValueSeparator(' ').hasArg().create("emin") );
 options.addOption(OptionBuilder.withLongOpt("title").withDescription("Do not display the title")
                                .withValueSeparator(' ').create("title") );
 options.addOption(OptionBuilder.withLongOpt("box").withDescription("Display prettyboxes")
                                .withValueSeparator(' ').create("box") );
 options.addOption(OptionBuilder.withLongOpt("minsize").withDescription("Minimum nucleotide size of ORF to report")
                                .withValueSeparator(' ').hasArg().create("minsize") );
 options.addOption(OptionBuilder.withLongOpt("hwindow").withDescription("Window size for hydropathy averaging")
                                .withValueSeparator(' ').hasArg().create("hwindow") );
 options.addOption(OptionBuilder.withLongOpt("threshold").withDescription("Threshold")
                                .withValueSeparator(' ').hasArg().create("threshold") );
 options.addOption(OptionBuilder.withLongOpt("seqall").withDescription("")
                                .withValueSeparator(' ').hasArg().create("seqall") );
 options.addOption(OptionBuilder.withLongOpt("idformat").withDescription("ID line format")
                                .withValueSeparator(' ').hasArg().create("idformat") );
 options.addOption(OptionBuilder.withLongOpt("lastorf").withDescription("ORF at the end of the sequence")
                                .withValueSeparator(' ').create("lastorf") );
 options.addOption(OptionBuilder.withLongOpt("frames").withDescription("Select one or more values")
                                .withValueSeparator(' ').hasArg().create("frames") );
 options.addOption(OptionBuilder.withLongOpt("bsource").withDescription("Source of second feature")
                                .withValueSeparator(' ').hasArg().create("bsource") );
 options.addOption(OptionBuilder.withLongOpt("operator").withDescription("Enter the logical operator to combine the sequences")
                                .withValueSeparator(' ').hasArg().create("operator") );
 options.addOption(OptionBuilder.withLongOpt("genomesequence").withDescription("Genomic sequence")
                                .withValueSeparator(' ').hasArg().create("genomesequence") );
 options.addOption(OptionBuilder.withLongOpt("sticky").withDescription("Allow sticky end cutters")
                                .withValueSeparator(' ').create("sticky") );
 options.addOption(OptionBuilder.withLongOpt("maxrange").withDescription("The maximum distance between the features")
                                .withValueSeparator(' ').hasArg().create("maxrange") );
 options.addOption(OptionBuilder.withLongOpt("residuesperline").withDescription("Number of residues to be displayed on each line")
                                .withValueSeparator(' ').hasArg().create("residuesperline") );
 options.addOption(OptionBuilder.withLongOpt("showall").withDescription("Show all potential EMBOSS data files")
                                .withValueSeparator(' ').hasArg().create("showall") );
 options.addOption(OptionBuilder.withLongOpt("stricttags").withDescription("Only display the matching tags")
                                .withValueSeparator(' ').create("stricttags") );
 options.addOption(OptionBuilder.withLongOpt("wordlen").withDescription("Word length for initial matching")
                                .withValueSeparator(' ').hasArg().create("wordlen") );
 options.addOption(OptionBuilder.withLongOpt("listoptions").withDescription("Display the date and options used")
                                .withValueSeparator(' ').create("listoptions") );
 options.addOption(OptionBuilder.withLongOpt("csimilarity").withDescription("Colour to display similar residues (GREEN)")
                                .withValueSeparator(' ').hasArg().create("csimilarity") );
 options.addOption(OptionBuilder.withLongOpt("tolower").withDescription("Change masked region to lower-case")
                                .withValueSeparator(' ').hasArg().create("tolower") );
 options.addOption(OptionBuilder.withLongOpt("maxscore").withDescription("Maximum score of feature to extract")
                                .withValueSeparator(' ').hasArg().create("maxscore") );
 options.addOption(OptionBuilder.withLongOpt("matchtag").withDescription("Tag of feature to display")
                                .withValueSeparator(' ').hasArg().create("matchtag") );
 options.addOption(OptionBuilder.withLongOpt("sort").withDescription("Sort features by Type, Start or Source, Nosort (don't sort - use input order) or join coding regions together and leave other features in the input order")
                                .withValueSeparator(' ').hasArg().create("sort") );
 options.addOption(OptionBuilder.withLongOpt("length").withDescription("Window size")
                                .withValueSeparator(' ').hasArg().create("length") );
 options.addOption(OptionBuilder.withLongOpt("secondset").withDescription("")
                                .withValueSeparator(' ').hasArg().create("secondset") );
 options.addOption(OptionBuilder.withLongOpt("gapdist").withDescription("Gap separation distance")
                                .withValueSeparator(' ').hasArg().create("gapdist") );
 options.addOption(OptionBuilder.withLongOpt("skip").withDescription("Number of sequences to skip at start")
                                .withValueSeparator(' ').hasArg().create("skip") );
 options.addOption(OptionBuilder.withLongOpt("fields").withDescription("Display 'fields' column")
                                .withValueSeparator(' ').create("fields") );
 options.addOption(OptionBuilder.withLongOpt("identity").withDescription("Required number of identities at a position")
                                .withValueSeparator(' ').hasArg().create("identity") );
 options.addOption(OptionBuilder.withLongOpt("motif").withDescription("Protein motif to search for")
                                .withValueSeparator(' ').hasArg().create("motif") );
 options.addOption(OptionBuilder.withLongOpt("filenames").withDescription("Wildcard database filename")
                                .withValueSeparator(' ').hasArg().create("filenames") );
 options.addOption(OptionBuilder.withLongOpt("bsequence").withDescription("")
                                .withValueSeparator(' ').hasArg().create("bsequence") );
 options.addOption(OptionBuilder.withLongOpt("aaproperties").withDescription("Amino acid properties filename")
                                .withValueSeparator(' ').hasArg().create("aaproperties") );
 options.addOption(OptionBuilder.withLongOpt("infile").withDescription("Full pathname of file aaindex1")
                                .withValueSeparator(' ').hasArg().create("infile") );
 options.addOption(OptionBuilder.withLongOpt("gapv").withDescription("Multiple alignment: Gap extension penalty")
                                .withValueSeparator(' ').hasArg().create("gapv") );
 options.addOption(OptionBuilder.withLongOpt("matrix").withDescription("Similarity scoring Matrix file")
                                .withValueSeparator(' ').hasArg().create("matrix") );
 options.addOption(OptionBuilder.withLongOpt("ambiguity").withDescription("Allow ambiguous matches")
                                .withValueSeparator(' ').create("ambiguity") );
 options.addOption(OptionBuilder.withLongOpt("maxpallen").withDescription("Enter maximum length of palindrome")
                                .withValueSeparator(' ').hasArg().create("maxpallen") );
 options.addOption(OptionBuilder.withLongOpt("primer").withDescription("Pick PCR primer(s)")
                                .withValueSeparator(' ').hasArg().create("primer") );
 options.addOption(OptionBuilder.withLongOpt("bsense").withDescription("Sense of second feature")
                                .withValueSeparator(' ').hasArg().create("bsense") );
 options.addOption(OptionBuilder.withLongOpt("refseq").withDescription("The number or the name of the reference sequence")
                                .withValueSeparator(' ').hasArg().create("refseq") );
 options.addOption(OptionBuilder.withLongOpt("includedregion").withDescription("Included region(s)")
                                .withValueSeparator(' ').hasArg().create("includedregion") );
 options.addOption(OptionBuilder.withLongOpt("order").withDescription("Sort order of results")
                                .withValueSeparator(' ').hasArg().create("order") );
 options.addOption(OptionBuilder.withLongOpt("weight").withDescription("Whole sequence molwt")
                                .withValueSeparator(' ').hasArg().create("weight") );
 options.addOption(OptionBuilder.withLongOpt("tag").withDescription("Tag of feature to extract")
                                .withValueSeparator(' ').hasArg().create("tag") );
 options.addOption(OptionBuilder.withLongOpt("rangetype").withDescription("Specify position")
                                .withValueSeparator(' ').hasArg().create("rangetype") );
 options.addOption(OptionBuilder.withLongOpt("eightyseven").withDescription("Use the old (1987) weight data")
                                .withValueSeparator(' ').create("eightyseven") );
 options.addOption(OptionBuilder.withLongOpt("windowsize").withDescription("Enter window size")
                                .withValueSeparator(' ').hasArg().create("windowsize") );
 options.addOption(OptionBuilder.withLongOpt("besthits").withDescription("Show only the best hits (minimise mismatches)?")
                                .withValueSeparator(' ').create("besthits") );
 options.addOption(OptionBuilder.withLongOpt("blastversion").withDescription("Blast index version")
                                .withValueSeparator(' ').hasArg().create("blastversion") );
 options.addOption(OptionBuilder.withLongOpt("star").withDescription("Trim off asterisks")
                                .withValueSeparator(' ').create("star") );
 options.addOption(OptionBuilder.withLongOpt("commercial").withDescription("Only enzymes with suppliers")
                                .withValueSeparator(' ').create("commercial") );
 options.addOption(OptionBuilder.withLongOpt("value").withDescription("Value of feature tags to extract")
                                .withValueSeparator(' ').hasArg().create("value") );
 options.addOption(OptionBuilder.withLongOpt("seqtype").withDescription("Sequence type")
                                .withValueSeparator(' ').hasArg().create("seqtype") );
 options.addOption(OptionBuilder.withLongOpt("full").withDescription("Show all EMBOSS version information fields")
                                .withValueSeparator(' ').create("full") );
 options.addOption(OptionBuilder.withLongOpt("infdat").withDescription("Name of prosite directory")
                                .withValueSeparator(' ').hasArg().create("infdat") );
 options.addOption(OptionBuilder.withLongOpt("atag").withDescription("Tag of first feature")
                                .withValueSeparator(' ').hasArg().create("atag") );
 options.addOption(OptionBuilder.withLongOpt("word").withDescription("Word size to consider (e.g. 2=dimer)")
                                .withValueSeparator(' ').hasArg().create("word") );
 options.addOption(OptionBuilder.withLongOpt("shift").withDescription("Window shift increment")
                                .withValueSeparator(' ').hasArg().create("shift") );
 options.addOption(OptionBuilder.withLongOpt("type").withDescription("Type of feature to extract")
                                .withValueSeparator(' ').hasArg().create("type") );
 options.addOption(OptionBuilder.withLongOpt("sd").withDescription("Standard Deviation value")
                                .withValueSeparator(' ').hasArg().create("sd") );
 options.addOption(OptionBuilder.withLongOpt("annotation").withDescription("Regions to mark (eg: 4-57 promoter region 78-94 first exon)")
                                .withValueSeparator(' ').hasArg().create("annotation") );
 options.addOption(OptionBuilder.withLongOpt("ruler").withDescription("Add a ruler")
                                .withValueSeparator(' ').create("ruler") );
 options.addOption(OptionBuilder.withLongOpt("minsd").withDescription("Minimum SD")
                                .withValueSeparator(' ').hasArg().create("minsd") );
 options.addOption(OptionBuilder.withLongOpt("aminscore").withDescription("Minimum score of first feature")
                                .withValueSeparator(' ').hasArg().create("aminscore") );
 options.addOption(OptionBuilder.withLongOpt("enzyme").withDescription("Restriction enzyme name")
                                .withValueSeparator(' ').hasArg().create("enzyme") );
 options.addOption(OptionBuilder.withLongOpt("after").withDescription("Amount of sequence after feature to extract")
                                .withValueSeparator(' ').hasArg().create("after") );
 options.addOption(OptionBuilder.withLongOpt("compdatafile").withDescription("Compseq output file to use for expected word frequencies")
                                .withValueSeparator(' ').hasArg().create("compdatafile") );
 options.addOption(OptionBuilder.withLongOpt("firstorf").withDescription("ORF at the beginning of the sequence")
                                .withValueSeparator(' ').create("firstorf") );
 options.addOption(OptionBuilder.withLongOpt("boxcol").withDescription("Colour the background in the boxes")
                                .withValueSeparator(' ').create("boxcol") );
 options.addOption(OptionBuilder.withLongOpt("intronpenalty").withDescription("Intron penalty")
                                .withValueSeparator(' ').hasArg().create("intronpenalty") );
 options.addOption(OptionBuilder.withLongOpt("maxlabels").withDescription("Maximum number of labels")
                                .withValueSeparator(' ').hasArg().create("maxlabels") );
 options.addOption(OptionBuilder.withLongOpt("asense").withDescription("Sense of first feature")
                                .withValueSeparator(' ').hasArg().create("asense") );
 options.addOption(OptionBuilder.withLongOpt("sequences").withDescription("")
                                .withValueSeparator(' ').hasArg().create("sequences") );
 options.addOption(OptionBuilder.withLongOpt("gapextend").withDescription("Gap extension penalty")
                                .withValueSeparator(' ').hasArg().create("gapextend") );
 options.addOption(OptionBuilder.withLongOpt("letters").withDescription("Residue letters")
                                .withValueSeparator(' ').hasArg().create("letters") );
 options.addOption(OptionBuilder.withLongOpt("consensus").withDescription("Display the consensus")
                                .withValueSeparator(' ').create("consensus") );
 options.addOption(OptionBuilder.withLongOpt("mean").withDescription("Mean value")
                                .withValueSeparator(' ').hasArg().create("mean") );
 options.addOption(OptionBuilder.withLongOpt("prune").withDescription("Ignore simple patterns")
                                .withValueSeparator(' ').create("prune") );
 options.addOption(OptionBuilder.withLongOpt("pattern").withDescription("Regular expression pattern")
                                .withValueSeparator(' ').hasArg().create("pattern") );
 options.addOption(OptionBuilder.withLongOpt("block").withDescription("Types of block mutations to perform")
                                .withValueSeparator(' ').hasArg().create("block") );
 options.addOption(OptionBuilder.withLongOpt("product").withDescription("Prompt for product values")
                                .withValueSeparator(' ').hasArg().create("product") );
 options.addOption(OptionBuilder.withLongOpt("poliii").withDescription("Select probes for Pol III expression vectors")
                                .withValueSeparator(' ').create("poliii") );
 options.addOption(OptionBuilder.withLongOpt("minoe").withDescription("Minimum observed/expected")
                                .withValueSeparator(' ').hasArg().create("minoe") );
 options.addOption(OptionBuilder.withLongOpt("cfile").withDescription("")
                                .withValueSeparator(' ').hasArg().create("cfile") );
 options.addOption(OptionBuilder.withLongOpt("minlength").withDescription("Display matches equal to or above this length")
                                .withValueSeparator(' ').hasArg().create("minlength") );
 options.addOption(OptionBuilder.withLongOpt("slow").withDescription("Do you want to carry out slow or fast pairwise alignment")
                                .withValueSeparator(' ').hasArg().create("slow") );
 options.addOption(OptionBuilder.withLongOpt("mstart").withDescription("ORF start with an M")
                                .withValueSeparator(' ').create("mstart") );
 options.addOption(OptionBuilder.withLongOpt("aadata").withDescription("Amino acid data file")
                                .withValueSeparator(' ').hasArg().create("aadata") );
 options.addOption(OptionBuilder.withLongOpt("entry").withDescription("ID or Accession number")
                                .withValueSeparator(' ').hasArg().create("entry") );
 options.addOption(OptionBuilder.withLongOpt("asource").withDescription("Source of first feature")
                                .withValueSeparator(' ').hasArg().create("asource") );
 options.addOption(OptionBuilder.withLongOpt("sequence").withDescription("")
                                .withValueSeparator(' ').hasArg().create("sequence") );
 options.addOption(OptionBuilder.withLongOpt("percent").withDescription("Percent threshold of ambiguity in window")
                                .withValueSeparator(' ').hasArg().create("percent") );
 options.addOption(OptionBuilder.withLongOpt("maxcuts").withDescription("Maximum cuts per RE")
                                .withValueSeparator(' ').hasArg().create("maxcuts") );
 options.addOption(OptionBuilder.withLongOpt("vectorfile").withDescription("Are your vector sequences in a file?")
                                .withValueSeparator(' ').hasArg().create("vectorfile") );
 options.addOption(OptionBuilder.withLongOpt("table").withDescription("Code to use")
                                .withValueSeparator(' ').hasArg().create("table") );
 options.addOption(OptionBuilder.withLongOpt("onlydend").withDescription("Only produce dendrogram file")
                                .withValueSeparator(' ').hasArg().create("onlydend") );
 options.addOption(OptionBuilder.withLongOpt("maxgroups").withDescription("Maximum number of groups")
                                .withValueSeparator(' ').hasArg().create("maxgroups") );
 options.addOption(OptionBuilder.withLongOpt("nummismatches").withDescription("Number of mismatches allowed")
                                .withValueSeparator(' ').hasArg().create("nummismatches") );
 options.addOption(OptionBuilder.withLongOpt("cother").withDescription("Colour to display other residues (BLACK)")
                                .withValueSeparator(' ').hasArg().create("cother") );
 options.addOption(OptionBuilder.withLongOpt("mismatches").withDescription("Number of contiguous mismatches allowed in a tail")
                                .withValueSeparator(' ').hasArg().create("mismatches") );
 options.addOption(OptionBuilder.withLongOpt("aa").withDescription("Select only regions that start with AA")
                                .withValueSeparator(' ').create("aa") );
 options.addOption(OptionBuilder.withLongOpt("similarcase").withDescription("Show similar residues in lower-case")
                                .withValueSeparator(' ').create("similarcase") );
 options.addOption(OptionBuilder.withLongOpt("calcfreq").withDescription("Calculate expected frequency from sequence")
                                .withValueSeparator(' ').create("calcfreq") );
 options.addOption(OptionBuilder.withLongOpt("shiftincrement").withDescription("Enter Shift Increment")
                                .withValueSeparator(' ').hasArg().create("shiftincrement") );
 options.addOption(OptionBuilder.withLongOpt("prefer").withDescription("Use the first sequence when there is a mismatch")
                                .withValueSeparator(' ').create("prefer") );
 options.addOption(OptionBuilder.withLongOpt("second").withDescription("")
                                .withValueSeparator(' ').hasArg().create("second") );
 options.addOption(OptionBuilder.withLongOpt("match").withDescription("Match score")
                                .withValueSeparator(' ').hasArg().create("match") );
 options.addOption(OptionBuilder.withLongOpt("ccolours").withDescription("Colour residues by their consensus value.")
                                .withValueSeparator(' ').create("ccolours") );
 options.addOption(OptionBuilder.withLongOpt("bangle").withDescription("Beta sheet angle (degrees)")
                                .withValueSeparator(' ').hasArg().create("bangle") );
 options.addOption(OptionBuilder.withLongOpt("description").withDescription("Description of the sequence")
                                .withValueSeparator(' ').hasArg().create("description") );
 options.addOption(OptionBuilder.withLongOpt("strict").withDescription("Trim off all ambiguity codes, not just N or X")
                                .withValueSeparator(' ').create("strict") );
 options.addOption(OptionBuilder.withLongOpt("winsize").withDescription("Window size")
                                .withValueSeparator(' ').hasArg().create("winsize") );
 options.addOption(OptionBuilder.withLongOpt("minpallen").withDescription("Enter minimum length of palindrome")
                                .withValueSeparator(' ').hasArg().create("minpallen") );
 options.addOption(OptionBuilder.withLongOpt("avalue").withDescription("Value of first feature's tags")
                                .withValueSeparator(' ').hasArg().create("avalue") );
 options.addOption(OptionBuilder.withLongOpt("btag").withDescription("Tag of second feature")
                                .withValueSeparator(' ').hasArg().create("btag") );
 options.addOption(OptionBuilder.withLongOpt("sitelen").withDescription("Minimum recognition site length")
                                .withValueSeparator(' ').hasArg().create("sitelen") );
 options.addOption(OptionBuilder.withLongOpt("showdel").withDescription("Output deleted mwts")
                                .withValueSeparator(' ').create("showdel") );
 options.addOption(OptionBuilder.withLongOpt("source").withDescription("Source of feature to display")
                                .withValueSeparator(' ').hasArg().create("source") );
 options.addOption(OptionBuilder.withLongOpt("width").withDescription("Window size")
                                .withValueSeparator(' ').hasArg().create("width") );
 options.addOption(OptionBuilder.withLongOpt("numreturn").withDescription("Number of results to return")
                                .withValueSeparator(' ').hasArg().create("numreturn") );
 options.addOption(OptionBuilder.withLongOpt("number").withDescription("The number of the sequence to output")
                                .withValueSeparator(' ').hasArg().create("number") );
 options.addOption(OptionBuilder.withLongOpt("plurality").withDescription("Plurality check value")
                                .withValueSeparator(' ').hasArg().create("plurality") );
 options.addOption(OptionBuilder.withLongOpt("range").withDescription("Range(s) to translate")
                                .withValueSeparator(' ').hasArg().create("range") );
 options.addOption(OptionBuilder.withLongOpt("gaplength").withDescription("Gap length penalty")
                                .withValueSeparator(' ').hasArg().create("gaplength") );
 options.addOption(OptionBuilder.withLongOpt("shade").withDescription("Shading")
                                .withValueSeparator(' ').hasArg().create("shade") );
 options.addOption(OptionBuilder.withLongOpt("frame").withDescription("Frame of word to look at (0=all frames)")
                                .withValueSeparator(' ').hasArg().create("frame") );
 options.addOption(OptionBuilder.withLongOpt("find").withDescription("Type of output")
                                .withValueSeparator(' ').hasArg().create("find") );
 options.addOption(OptionBuilder.withLongOpt("firstset").withDescription("")
                                .withValueSeparator(' ').hasArg().create("firstset") );
 options.addOption(OptionBuilder.withLongOpt("size").withDescription("Size to split at")
                                .withValueSeparator(' ').hasArg().create("size") );
 options.addOption(OptionBuilder.withLongOpt("to").withDescription("End of region to delete")
                                .withValueSeparator(' ').hasArg().create("to") );
 options.addOption(OptionBuilder.withLongOpt("html").withDescription("Format output as an HTML table")
                                .withValueSeparator(' ').create("html") );
 options.addOption(OptionBuilder.withLongOpt("resbreak").withDescription("Residues before a space")
                                .withValueSeparator(' ').hasArg().create("resbreak") );
 options.addOption(OptionBuilder.withLongOpt("from").withDescription("Start of region to delete")
                                .withValueSeparator(' ').hasArg().create("from") );
 options.addOption(OptionBuilder.withLongOpt("minrange").withDescription("The minimum distance between the features")
                                .withValueSeparator(' ').hasArg().create("minrange") );
 options.addOption(OptionBuilder.withLongOpt("maxdiv").withDescription("Cut-off to delay the alignment of the most divergent sequences")
                                .withValueSeparator(' ').hasArg().create("maxdiv") );
 options.addOption(OptionBuilder.withLongOpt("pos").withDescription("Position to insert after")
                                .withValueSeparator(' ').hasArg().create("pos") );
 options.addOption(OptionBuilder.withLongOpt("overlap").withDescription("Overlap between split sequences")
                                .withValueSeparator(' ').hasArg().create("overlap") );
 options.addOption(OptionBuilder.withLongOpt("cidentity").withDescription("Colour to display identical residues (RED)")
                                .withValueSeparator(' ').hasArg().create("cidentity") );
 options.addOption(OptionBuilder.withLongOpt("emax").withDescription("Maximum number of elements per fingerprint")
                                .withValueSeparator(' ').hasArg().create("emax") );
 options.addOption(OptionBuilder.withLongOpt("release").withDescription("Release number")
                                .withValueSeparator(' ').hasArg().create("release") );
 options.addOption(OptionBuilder.withLongOpt("bottom").withDescription("Display the reference sequence at the bottom")
                                .withValueSeparator(' ').create("bottom") );
 options.addOption(OptionBuilder.withLongOpt("dnaconc").withDescription("Enter DNA concentration (nM)")
                                .withValueSeparator(' ').hasArg().create("dnaconc") );
 options.addOption(OptionBuilder.withLongOpt("database").withDescription("Name of a single database to give information on")
                                .withValueSeparator(' ').hasArg().create("database") );
 options.addOption(OptionBuilder.withLongOpt("single").withDescription("Force single site only cuts")
                                .withValueSeparator(' ').create("single") );
 options.addOption(OptionBuilder.withLongOpt("blunt").withDescription("Allow blunt end cutters")
                                .withValueSeparator(' ').create("blunt") );
 options.addOption(OptionBuilder.withLongOpt("matrixfile").withDescription("Matrix file")
                                .withValueSeparator(' ').hasArg().create("matrixfile") );
 options.addOption(OptionBuilder.withLongOpt("bmaxscore").withDescription("Maximum score of second feature")
                                .withValueSeparator(' ').hasArg().create("bmaxscore") );
 options.addOption(OptionBuilder.withLongOpt("plasmid").withDescription("Allow circular DNA")
                                .withValueSeparator(' ').create("plasmid") );
 options.addOption(OptionBuilder.withLongOpt("sense").withDescription("Sense of feature to extract")
                                .withValueSeparator(' ').hasArg().create("sense") );
 options.addOption(OptionBuilder.withLongOpt("reverseinput").withDescription("Reverse input primer sequence to check")
                                .withValueSeparator(' ').hasArg().create("reverseinput") );
 options.addOption(OptionBuilder.withLongOpt("nlabel").withDescription("Number DNA sequence")
                                .withValueSeparator(' ').create("nlabel") );
 options.addOption(OptionBuilder.withLongOpt("gapopen").withDescription("Gap opening penalty")
                                .withValueSeparator(' ').hasArg().create("gapopen") );
 options.addOption(OptionBuilder.withLongOpt("exclude").withDescription("Sequence names to exclude")
                                .withValueSeparator(' ').hasArg().create("exclude") );
 options.addOption(OptionBuilder.withLongOpt("search").withDescription("Program to search for")
                                .withValueSeparator(' ').hasArg().create("search") );
 options.addOption(OptionBuilder.withLongOpt("amaxscore").withDescription("Maximum score of first feature")
                                .withValueSeparator(' ').hasArg().create("amaxscore") );
 options.addOption(OptionBuilder.withLongOpt("plabel").withDescription("Number translations")
                                .withValueSeparator(' ').create("plabel") );
 options.addOption(OptionBuilder.withLongOpt("aangle").withDescription("Alpha helix angle (degrees)")
                                .withValueSeparator(' ').hasArg().create("aangle") );
 options.addOption(OptionBuilder.withLongOpt("bminscore").withDescription("Minimum score of second feature")
                                .withValueSeparator(' ').hasArg().create("bminscore") );
 options.addOption(OptionBuilder.withLongOpt("tt").withDescription("Select only regions that end with TT")
                                .withValueSeparator(' ').create("tt") );
 options.addOption(OptionBuilder.withLongOpt("directory").withDescription("CUTG directory")
                                .withValueSeparator(' ').hasArg().create("directory") );
 options.addOption(OptionBuilder.withLongOpt("asequence").withDescription("")
                                .withValueSeparator(' ').hasArg().create("asequence") );
 options.addOption(OptionBuilder.withLongOpt("matchvalue").withDescription("Value of feature tags to display")
                                .withValueSeparator(' ').hasArg().create("matchvalue") );
 options.addOption(OptionBuilder.withLongOpt("step").withDescription("Stepping value")
                                .withValueSeparator(' ').hasArg().create("step") );
 options.addOption(OptionBuilder.withLongOpt("pair").withDescription("Values to represent identical similar related")
                                .withValueSeparator(' ').hasArg().create("pair") );
 options.addOption(OptionBuilder.withLongOpt("mismatch").withDescription("Mismatch score")
                                .withValueSeparator(' ').hasArg().create("mismatch") );
 options.addOption(OptionBuilder.withLongOpt("gap").withDescription("Gap penalty")
                                .withValueSeparator(' ').hasArg().create("gap") );
 options.addOption(OptionBuilder.withLongOpt("orfml").withDescription("Minimum ORF Length to report")
                                .withValueSeparator(' ').hasArg().create("orfml") );
 options.addOption(OptionBuilder.withLongOpt("minscore").withDescription("Minimum accepted score")
                                .withValueSeparator(' ').hasArg().create("minscore") );
 options.addOption(OptionBuilder.withLongOpt("tolerance").withDescription("Ppm tolerance")
                                .withValueSeparator(' ').hasArg().create("tolerance") );
 options.addOption(OptionBuilder.withLongOpt("minrepeat").withDescription("Minimum repeat size")
                                .withValueSeparator(' ').hasArg().create("minrepeat") );
 options.addOption(OptionBuilder.withLongOpt("bvalue").withDescription("Value of second feature's tags")
                                .withValueSeparator(' ').hasArg().create("bvalue") );
 options.addOption(OptionBuilder.withLongOpt("minweight").withDescription("Minimum weight")
                                .withValueSeparator(' ').hasArg().create("minweight") );
 options.addOption(OptionBuilder.withLongOpt("matchsource").withDescription("Source of feature to display")
                                .withValueSeparator(' ').hasArg().create("matchsource") );
 options.addOption(OptionBuilder.withLongOpt("before").withDescription("Amount of sequence before feature to extract")
                                .withValueSeparator(' ').hasArg().create("before") );
 options.addOption(OptionBuilder.withLongOpt("casesensitive").withDescription("Do a case-sensitive search")
                                .withValueSeparator(' ').create("casesensitive") );
 options.addOption(OptionBuilder.withLongOpt("show").withDescription("What to show")
                                .withValueSeparator(' ').hasArg().create("show") );
 options.addOption(OptionBuilder.withLongOpt("polybase").withDescription("Allow regions with 4 repeats of a base")
                                .withValueSeparator(' ').create("polybase") );
 options.addOption(OptionBuilder.withLongOpt("matchtype").withDescription("Type of feature to display")
                                .withValueSeparator(' ').hasArg().create("matchtype") );
 options.addOption(OptionBuilder.withLongOpt("gapc").withDescription("Multiple alignment: Gap opening penalty")
                                .withValueSeparator(' ').hasArg().create("gapc") );
 options.addOption(OptionBuilder.withLongOpt("maxrepeat").withDescription("Maximum extent of repeats")
                                .withValueSeparator(' ').hasArg().create("maxrepeat") );
 options.addOption(OptionBuilder.withLongOpt("clean").withDescription("Change all *'s to X's")
                                .withValueSeparator(' ').create("clean") );
 options.addOption(OptionBuilder.withLongOpt("hybridprobe").withDescription("Pick hybridization probe")
                                .withValueSeparator(' ').hasArg().create("hybridprobe") );
 options.addOption(OptionBuilder.withLongOpt("reverse").withDescription("Write the reverse complement when poly-T is removed")
                                .withValueSeparator(' ').create("reverse") );
 options.addOption(OptionBuilder.withLongOpt("gappenalty").withDescription("Gap penalty")
                                .withValueSeparator(' ').hasArg().create("gappenalty") );
 options.addOption(OptionBuilder.withLongOpt("separate").withDescription("Write regions to separate sequences")
                                .withValueSeparator(' ').create("separate") );
 options.addOption(OptionBuilder.withLongOpt("portrait").withDescription("Set page to Portrait")
                                .withValueSeparator(' ').create("portrait") );
 options.addOption(OptionBuilder.withLongOpt("window").withDescription("Window")
                                .withValueSeparator(' ').hasArg().create("window") );
 options.addOption(OptionBuilder.withLongOpt("enzymes").withDescription("Comma separated enzyme list")
                                .withValueSeparator(' ').hasArg().create("enzymes") );
 options.addOption(OptionBuilder.withLongOpt("uppercase").withDescription("Regions to put in uppercase (eg: 4-57,78-94)")
                                .withValueSeparator(' ').hasArg().create("uppercase") );
 options.addOption(OptionBuilder.withLongOpt("endgaps").withDescription("Use end gap separation penalty")
                                .withValueSeparator(' ').create("endgaps") );
 options.addOption(OptionBuilder.withLongOpt("matchsense").withDescription("Sense of feature to display")
                                .withValueSeparator(' ').hasArg().create("matchsense") );
 options.addOption(OptionBuilder.withLongOpt("splicepenalty").withDescription("Splice site penalty")
                                .withValueSeparator(' ').hasArg().create("splicepenalty") );
 options.addOption(OptionBuilder.withLongOpt("alternative").withDescription("Use alternative collisions routine")
                                .withValueSeparator(' ').hasArg().create("alternative") );
 options.addOption(OptionBuilder.withLongOpt("maxnamelen").withDescription("Margin size for the sequence name.")
                                .withValueSeparator(' ').hasArg().create("maxnamelen") );
 options.addOption(OptionBuilder.withLongOpt("highlight").withDescription("Regions to colour in HTML (eg: 4-57 red 78-94 green)")
                                .withValueSeparator(' ').hasArg().create("highlight") );
 options.addOption(OptionBuilder.withLongOpt("mincuts").withDescription("Minimum cuts per RE")
                                .withValueSeparator(' ').hasArg().create("mincuts") );
 options.addOption(OptionBuilder.withLongOpt("first").withDescription("")
                                .withValueSeparator(' ').hasArg().create("first") );
 options.addOption(OptionBuilder.withLongOpt("atype").withDescription("Type of first feature")
                                .withValueSeparator(' ').hasArg().create("atype") );
 options.addOption(OptionBuilder.withLongOpt("menu").withDescription("Select number")
                                .withValueSeparator(' ').hasArg().create("menu") );
 options.addOption(OptionBuilder.withLongOpt("score").withDescription("CpG score")
                                .withValueSeparator(' ').hasArg().create("score") );
 options.addOption(OptionBuilder.withLongOpt("nucleic").withDescription("Display nucleic acid databases")
                                .withValueSeparator(' ').create("nucleic") );
 options.addOption(OptionBuilder.withLongOpt("mismatchpercent").withDescription("Allowed percent mismatch")
                                .withValueSeparator(' ').hasArg().create("mismatchpercent") );
 options.addOption(OptionBuilder.withLongOpt("setcase").withDescription("Define a threshold above which the consensus is given in uppercase")
                                .withValueSeparator(' ').hasArg().create("setcase") );
 options.addOption(OptionBuilder.withLongOpt("protein").withDescription("Display protein databases")
                                .withValueSeparator(' ').create("protein") );
 options.addOption(OptionBuilder.withLongOpt("format").withDescription("Display format")
                                .withValueSeparator(' ').hasArg().create("format") );
 options.addOption(OptionBuilder.withLongOpt("seqcomp").withDescription("Overlap sequence")
                                .withValueSeparator(' ').hasArg().create("seqcomp") );
 options.addOption(OptionBuilder.withLongOpt("point").withDescription("Types of point mutations to perform")
                                .withValueSeparator(' ').hasArg().create("point") );


//****************************************************************************
// common options for Emboss clients 
//****************************************************************************

 options.addOption(OptionBuilder.withLongOpt("tool")
                                        .withDescription("Emboss program to run")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("tool") );
	
 options.addOption(OptionBuilder.withLongOpt("info")
                                        .withDescription("Information about EMBOSS programs")
                                        .withValueSeparator(' ')
                                        .hasArg()
                                        .create("info") );        

//****************************************************************************
// common options for EBI clients 
//****************************************************************************
	
    options.addOption( "help","help",false, "help on using this client");
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
                                        .withDescription("save results to outfile")
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
      else if (line.hasOption("info")) {
	  if (line.getOptionValue("info").equals("all")) {
           getTools();
          } else { 
	   getInfo(line.getOptionValue("info"));
	  }
     }
      else {

      String[] more = line.getArgs();

      if (line.hasOption("tool")|| line.hasOption("asequence")|| line.hasOption("bsequence")|| line.hasOption("sequence")||line.hasOption("dbfetch")||line.hasOption("ebifile")|| (more.length>1)) {

	
            InputParams params = loadParams(line);

	    Data inputs[] = loadData(line);

	    WSEmbossService service =  new WSEmbossServiceLocator();
	    WSEmboss emboss = service.getWSEmboss();
	    String jobid = emboss.run(params,inputs);

	  if (line.hasOption("async")) {
             System.out.println("This is your job id: "+ jobid);
	     System.out.println("Use: java WSEmbossClient --status --jobid "+ jobid + " to see the job status");
	  } else {             
	     getResults(jobid, line);            
	  }	
      }
       else {		    
         usage(options);
         System.exit(0);
      }
     }
    }
    catch (ParseException exp) {
      System.out.println("Parsing failed. Reason: " + exp.getMessage());
      usage(options);
    }
    catch (Exception e) {
      System.out.println("ERROR:\n" + e.toString());
      e.printStackTrace();
    }
  }
}
