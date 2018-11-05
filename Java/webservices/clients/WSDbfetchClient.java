//
//  DbfetchClient.java
//  
//
//  Created by Sharmila Pillai on Tue Jun 18 2002.
//  Copyright (c) 2003 EBI. All rights reserved.
//
// Version 1.0: The first version
// Version 2.0:	Mon Jun 2 2003
//		Changes to input param syntax. Input now should be dbName:id


import org.apache.axis.client.*;

import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Set;
import java.io.*;

import javax.activation.DataHandler;
import org.apache.axis.AxisFault;

import org.apache.axis.MessageContext; 
import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode; 
import javax.xml.namespace.QName;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.encoding.ser.JAFDataHandlerSerializerFactory;
import org.apache.axis.encoding.ser.JAFDataHandlerDeserializerFactory;

/**
 * Client for WS-Dbfetch webservice.
 *
 * @author Sharmila Pillai <sharmila@ebi.ac.uk>
 *
 */

public class WSDbfetchClient {

    public static void main (String[] args) {

        String format = "default";
        String style = "raw";
        
	try {
	    // prepare the call (the same for all called methods)
	    Call call = (Call) new Service().createCall();
	    call.setTargetEndpointAddress (new java.net.URL ("http://www.ebi.ac.uk/ws/services/Dbfetch"));
            
            if (args.length == 0) {
                printUsage();
                System.exit(0);
            }
            else if ( args[0].equalsIgnoreCase("getSupportedDBs") ) {
                call.setOperationName (new QName("urn:Dbfetch", "getSupportedDBs"));
                call.setReturnType(XMLType.SOAP_ARRAY);
                String[] result = (String[]) call.invoke(new Object[] {});
                for (int count=0; count<result.length; count++)
                    System.out.println(result[count]);
            }
            else if (args[0].equalsIgnoreCase("getSupportedFormats")) {
                call.setOperationName (new QName("urn:Dbfetch", "getSupportedFormats"));
                call.setReturnType(XMLType.SOAP_ARRAY);
                String[] result = (String[]) call.invoke(new Object[] {});
                //for (int count=0; count<result.length; count++) {
                int count =0;
                System.out.println("Database	Formats\n");
                System.out.println("--------	-------\n");
                while (count < result.length) {
                    System.out.println(result[count]+"		"+result[count+1]+"\n");
                    count = count+2;
                }
            }
            else if (args[0].equalsIgnoreCase("getSupportedStyles")) {
                call.setOperationName (new QName("urn:Dbfetch", "getSupportedStyles"));
                call.setReturnType(XMLType.SOAP_ARRAY);
                String[] result = (String[]) call.invoke(new Object[] {});
                for (int count=0; count<result.length; count++)
                    System.out.println(result[count]);            
            }
            else if (args[0].equalsIgnoreCase("fetchData")) {
                if (args.length < 2 ) {
                    printUsage();
                    System.exit(0);
                }
                else {
                    call.setOperationName (new QName("urn:Dbfetch", "fetchData"));
                    call.addParameter( "query", XMLType.XSD_STRING, ParameterMode.IN);
                    call.addParameter( "format", XMLType.XSD_STRING, ParameterMode.IN);
                    call.addParameter( "style", XMLType.XSD_STRING, ParameterMode.IN);
                    call.setReturnType(XMLType.SOAP_ARRAY);
                    
                    if (args.length == 3 )
                        format = args[2];
                    if (args.length == 4) {
                        format = args[2];
                        style = args[3];
                    }
                    
                    String[] result = (String[])call.invoke(new Object[] {args[1], format, style} );
                    
                    if (result.length == 0)
                        System.out.println("hmm...something wrong :-(\n");
                    else {
                        for (int count=0; count<result.length;count++)
                            System.out.println( result[count]);
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("fetchDataFile")) {
                if (args.length < 2 ) {
                    printUsage();
                    System.exit(0);
                }
                else {
                    if (args.length == 3 )
                        format = args[2];
                    if (args.length == 4) {
                        format = args[2];
                        style = args[3];
                    }
                    
                    call.setOperationName (new QName("urn:Dbfetch", "fetchDataFile"));
                    call.addParameter( "query", XMLType.XSD_STRING, ParameterMode.IN);
                    call.addParameter( "format", XMLType.XSD_STRING, ParameterMode.IN);
                    call.addParameter( "style", XMLType.XSD_STRING, ParameterMode.IN);
                    
                    QName qnameAttachment = new QName("urn:Dbfetch", "DataHandler");
                    call.registerTypeMapping(javax.activation.DataSource.class,
                                            qnameAttachment,
                                            JAFDataHandlerSerializerFactory.class,
                                            JAFDataHandlerDeserializerFactory.class);
                                            
                    call.setReturnType(qnameAttachment);
                    
                    Object ret = call.invoke(new Object[] {args[1], format, style} );
                    
                    if (null == ret) {
                        System.out.println("Received null ");
                        throw new AxisFault("", "Received null", null, null);
                    }
                    if ( ret instanceof String) {
                        System.out.println("Received problem response from server: " + ret);
                        throw new AxisFault("", (String) ret, null, null);
                    }
                    if (!(ret instanceof DataHandler)) {
                        //The wrong type of object that what was expected.
                        System.out.println("Received problem response from server:" +
                        ret.getClass().getName());
                        throw new AxisFault("", "Received problem response from server:" +
                        ret.getClass().getName(), null, null);

                    }
                    //Still here, so far so good.
                    DataHandler rdh = (DataHandler) ret;
                    
                    //From here we'll just treat the data resource as file.
                    String receivedfileName = rdh.getName(); //Get the filename.
                    
                    if ( receivedfileName == null) {
                        System.err.println("Could not get the file name.");
                        throw new AxisFault("", "Could not get the file name.", null, null);
                    }
                    if (args[1].equalsIgnoreCase("medline")) {
                        System.out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                        System.out.println("<MedlineSet>");
                        printFiletoScreen(receivedfileName);
                        System.out.println("</MedlineSet>");
                    }
                    else if (args[1].equalsIgnoreCase("interpro")) {
                        System.out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
                        System.out.println("<InterproSet>");
                        printFiletoScreen(receivedfileName);
                        System.out.println("</InterproSet>");
                    }                        
                    else 
                        printFiletoScreen(receivedfileName);
                }
            }
        }
           
	catch (Exception e) {
	    System.err.println ("ERROR:\n" + e.toString());
	    e.printStackTrace();
	}
    }
        
    private static void printFiletoScreen(String receivedfileName) {
        try {
            BufferedReader in = new BufferedReader (new FileReader(receivedfileName));
            String out;
            while ( (out = in.readLine()) != null) 
                //print to screen
                System.out.println(out);
                in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void printUsage() {
        System.out.println("\n USAGE: \n");
        System.out.println( "1. fetchData database name:id or acc.no [output format] [output style] \n");
        System.out.println( "2. getSupportedDBs \n");
        System.out.println( "3. getSupportedFormats \n");
        System.out.println( "4. getSupportedStyles \n");
    }
}
