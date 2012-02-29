package org.NooLab.utilities.xml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import com.jamesmurty.utils.XMLBuilder;

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;

//see : http://code.google.com/p/java-xmlbuilder/wiki/ExampleUsage

// very important:  http://www.exampledepot.com/egs/org.w3c.dom/pkg.html


public class XMLwritebuilder {


	// =================================

	// object references ..............

	// main variables / properties ....

	// constants ......................

	// volatile variables .............

	// helper objects .................
	StringsUtil strgutil = new StringsUtil();

	PrintLog out;

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public XMLwritebuilder(){
		
	}
	
	
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	
	public void test(){
		/*
		
		<?xml version="1.0" encoding="UTF-8"?>
		
		<Projects>
    		<java-xmlbuilder language="Java" scm="SVN">
        		<Location type="URL">http://code.google.com/p/java-xmlbuilder/</Location>
    		</java-xmlbuilder>
    		<JetS3t language="Java" scm="CVS">
        		<Location type="URL">http://jets3t.s3.amazonaws.com/index.html</Location>
    		</JetS3t>
		</Projects>
		
		*/
		
		Properties outputProperties = new Properties();

		// Explicitly identify the output as an XML document
		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");

		// Pretty-print the XML output (doesn't work in all cases)
		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");

		// Get 2-space indenting when using the Apache transformer
		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");

		// Omit the XML declaration header
		// outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		

		
		try {
			XMLBuilder builder = XMLBuilder.create("Projects");
			
			builder.e("java-xmlbuilder")
		        .a("language", "Java")
		        .a("scm","SVN")                    
		        .e("Location")
		            .a("type", "URL")
		            .t("http://code.google.com/p/java-xmlbuilder/")
		        .up()
		    .up()
		    .e("JetS3t")
		        .a("language", "Java")
		        .a("scm","CVS")
		        .e("Location")
		            .a("type", "URL")
		            .a("link","http://jets3t.s3.amazonaws.com/index.html");

			
			
			XMLBuilder firstLocationBuilder = builder.xpathFind("//Location");
			builder.xpathFind("//JetS3t").elem("Location2").attr("type", "Testing");
			

			PrintWriter writer;

			writer = new PrintWriter(new FileOutputStream("D:/dev/java/data/test/stest/xb_projects.xml"));

			builder.toWriter(writer, outputProperties);

			String str = builder.asString(outputProperties);

			
			
			
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

	}
	
	
}
