import java.util.*;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.cli.*;
import uk.ac.ebi.webservices.WSWhatizit.*;
import uk.ac.ebi.cdb.webservice.proxy.*;

public class WSWhatizitClient {

   static Document dom;


   private static byte[] readFile(File file) throws IOException {
    
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
        return bytes;
    }

	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

    public static void main(String[] args) {

	
    if (args.length<1){
     System.out.println("WSWhatizit: annotate biological entities in text");
     System.out.println("");
     System.out.println("Use java -jar WSWhatizit.jar MEDLINEXML");
     System.out.println("Use java -jar WSWhatizit.jar PMID  (ex. 15978345)");
     System.exit(1);
    }

    try {
	String filters="filters=ifplain;sentenciser;swissprot;go;nesummary;ofhtml";
	byte[] fileContent = new byte[1024000];

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	String text;

	try {

	//Using factory get an instance of document builder
	DocumentBuilder db = dbf.newDocumentBuilder();
	
	if ((new File(args[0])).exists()){
          //fileContent = readFile(new File(args[0]));
	  //parse using builder to get DOM representation of the XML file
	  dom = db.parse(args[0]);
	} else {
	 WSCitationProxy citProxy = new WSCitationProxy();
      	 // example - get medline citation with pubmed id 15978345
         text  = citProxy.getMedlineXML(new Integer(args[0]));	
 	 dom = db.parse(new ByteArrayInputStream(text.getBytes()));	
	}


	}catch(ParserConfigurationException pce) {
		pce.printStackTrace();
	}catch(SAXException se) {
		se.printStackTrace();
	}catch(IOException ioe) {
		ioe.printStackTrace();
	}

	Element docEle = dom.getDocumentElement();

	//get a nodelist of  elements
	NodeList nl = docEle.getElementsByTagName("Abstract");
	String abstracts = filters; 
	if(nl != null && nl.getLength() > 0) {
	for(int i = 0 ; i < nl.getLength();i++) {
		//get the employee element
		Element el = (Element)nl.item(i);

		abstracts = abstracts + "\n"+ getTextValue(el,"AbstractText");
	}
	}

	System.out.println(abstracts);
	
 	WSWhatizitServerService service =  new WSWhatizitServerServiceLocator();
	WSWhatizitServer whatizit = service.getWSwhatizit();
	
	String response = whatizit.markup(abstracts,filters);

	System.out.println(response);
    }
    catch (Exception e) {
      System.err.println("ERROR:\n" + e.toString());
      e.printStackTrace();
    }
    }

}
