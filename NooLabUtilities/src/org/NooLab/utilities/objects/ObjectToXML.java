package org.NooLab.utilities.objects;


import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.output.ByteArrayOutputStream;


/**
 * taken from:
 * http://sanjaal.com/java/tag/simple-tutorial-for-java-xml-serialization/
 * 
 *
 */
public class ObjectToXML {

	/**
     * This method saves (serializes) any java bean object into xml file
     */
    public String serializeObjectToXML( Object objectToSerialize) throws Exception {
    	
    	String xmlStr = "" ;
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // FileOutputStream os = new FileOutputStream(xmlFileLocation);
    	
        XMLEncoder encoder = new XMLEncoder(bos);
        encoder.writeObject(objectToSerialize);
        encoder.close();
        
        xmlStr = bos.toString();
        return xmlStr;
    }
 
    /**
     * Reads Java Bean Object From XML File
     */
    public Object deserializeXMLToObject( String xmlStr) throws Exception {
    	
    	ByteArrayInputStream bis;
    	InputStream is = new ByteArrayInputStream( xmlStr.getBytes( Charset.defaultCharset() ) );
        // FileInputStream os = new FileInputStream(xmlFileLocation);
         
        XMLDecoder decoder = new XMLDecoder(is);
        Object deSerializedObject = decoder.readObject();
        decoder.close();
 
        return deSerializedObject;
    }
 
    /**
     * Testing.
     * 1. Creates and Object.
     * 2. Serializes Object To XML
     * 3. Deserializes Object From XML
     * 4. Prints The values hold in Object
     */
    public static void tester() throws Exception {
 
        /* Location of XML File */
        String XMLLocation = "C:/myXMLFile.xml";
 
        ObjectToXML serializer = new ObjectToXML();
 
        /* Creating and filling a bean object */
        /*MyBeanToSerialize obj = new MyBeanToSerialize();
        
        obj.setFirstName("Johnny");
        obj.setLastName("Depp");
        obj.setAge(45);
 
        /* Serialzing Object to XML  
        System.out.println("Starting Serialization...");
        serializer.serializeObjectToXML(XMLLocation, obj);
        System.out.println("Serialized Object: " + obj.getClass().getName());
        System.out.println("Destination XML: " + XMLLocation);
 
        /* Reading the object from serialized XML  
        System.out.println("\n\nStarting De-Serialization...");
        System.out.println("Source XML: " + XMLLocation);
        MyBeanToSerialize deserializedObj = (MyBeanToSerialize) serializer.deserializeXMLToObject(XMLLocation);
        System.out.println("De-serialized Object: "
                + deserializedObj.getClass().getName());
        System.out.println("\nChecking For Values In De-Serialized Object");
        System.out.println("...First Name: " + deserializedObj.getFirstName());
        System.out.println("...Last Name: " + deserializedObj.getLastName());
        System.out.println("...Age: " + deserializedObj.getAge());
        */
    }
 
}
 
