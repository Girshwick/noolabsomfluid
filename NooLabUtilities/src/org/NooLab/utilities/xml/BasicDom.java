package org.NooLab.utilities.xml;

/*
http://www.exampledepot.com/
http://www.exampledepot.com/egs/org.w3c.dom/WalkElem.html

http://www.exampledepot.com/egs/org.w3c.dom/xpath_GetElemByAttr.html

*/

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.apache.xalan.*;
import org.apache.xerces.*;
import org.apache.xpath.XPathAPI;

/**
 * 
 * this is not yet ready
 * 
 * 
 */
public class BasicDom {
   
	String strdocument;
	Document xmlDoc;
	boolean validating= false;
	
	Vector<Element> elements = new Vector<Element> (); 
	Vector<String> elementKeys = new Vector<String> ();
	
	Map<String,Vector<String>> elementsAttrContent = new TreeMap<String,Vector<String>>(); 
	
	Map<String,String> attributesMap = new TreeMap<String,String>(); 
	
	 
	
	public BasicDom(){
		
	}
	
	public BasicDom( String filename){
		
		xmlDoc = parseXml( filename, validating);
	}
	
	
	public void setFileInput( String filename ){
		strdocument = filename ;
	}
	
	public void parseXmlFile(){
		xmlDoc = parseXml( strdocument, validating);
	}
	
	public Document getXMLDocument(){
		return xmlDoc;
	}
	
    // Parses an XML file and returns a DOM document.
    // If validating is true, the contents is validated against the DTD
    // specified in the file.
    protected Document parseXml(String filename, boolean validating) {
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);

            // Create the builder and parse the file
            Document doc = factory.newDocumentBuilder().parse(new File(filename));
            return doc;
        } catch (SAXException e) {
            // A parsing error occurred; the xml input is not valid
        } catch (ParserConfigurationException e) {
        } catch (IOException e) {
        }
        return null;
    }
    
     
    private void getAllElements(){
    	NodeList list ;
    	Element element ;
    	Vector<String> attrs = new Vector<String>();
    	NamedNodeMap nnmAttrs;
    	Node node;
    	int n;
    	String str;
    	
    	
    	// Get a list of all elements in the document
    	list = xmlDoc.getElementsByTagName("*");
    	
    	for (int i=0; i<list.getLength(); i++) {
    		attrs.clear();
    		
    	    // Get element
    	   element = (Element)list.item(i);
    	   elements.add(element) ;
    	   
    	   elementsAttrContent.put("", attrs);
    	   
    	   nnmAttrs = element.getAttributes();
    	   
    	   n = nnmAttrs.getLength();
    	   for (int a=0;a<n;a++){
    		   node = nnmAttrs.item(a);
    		   str = node.getNodeName() ;
    		   str = node.getNodeValue() ;
    		   str = node.getTextContent() ;
    		   str = node.getLocalName();
    		   str = node.getBaseURI() ;
    	   }
    	   elementKeys.add("");
    	}
    }

    public Element getElement( String eNameID ){
    	Element element ;
    	
    	// Retrieve the element using id
        element = xmlDoc.getElementById( eNameID );
        
        return element ;
    }

    public void getAttribute( Element element, String attrName ){
    	String attrValue;
    	
    	// Get the element's attribute
        attrValue = element.getAttribute(attrName); 
        
        attributesMap.put( attrName, attrValue) ;
    }

	// Parses a string containing XML and returns a DocumentFragment
	// containing the nodes of the parsed XML.
	public static DocumentFragment parseXml(Document doc, String fragment) {
		
		// Wrap the fragment in an arbitrary element
		fragment = "<fragment>" + fragment + "</fragment>";
		try {
			// Create a DOM builder and parse the fragment
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			Document d = factory.newDocumentBuilder().parse( new InputSource(new StringReader(fragment)));

			// Import the nodes of the new document into doc so that they
			// will be compatible with doc
			Node node = doc.importNode(d.getDocumentElement(), true);

			// Create the document fragment node to hold the new nodes
			DocumentFragment docfrag = doc.createDocumentFragment();

			// Move the nodes into the fragment
			while (node.hasChildNodes()) {
				docfrag.appendChild(node.removeChild(node.getFirstChild()));
			}

			// Return the fragment
			return docfrag;
		} catch (SAXException e) {
			// A parsing error occurred; the xml input is not valid
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		}
		return null;
	}
 
    public Document createDomDocument() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
        }
        return null;
    }
    

    public Vector<Element> getElementbyXPathLocation( String xpathExpression){
		Element element = null;
		Vector<Element> elements = new Vector<Element>();
		NodeList nodelist ;
		
		try {
			
			// Get the matching elements
			nodelist = XPathAPI.selectNodeList(xmlDoc, xpathExpression);

			// Process the elements in the nodelist
			for (int i = 0; i < nodelist.getLength(); i++) {
				// Get element
				element = (Element) nodelist.item(i);
				elements.add(element) ;
			}
			
		} catch (javax.xml.transform.TransformerException e) {
		}
		return elements;
	}
    
    public Vector<Element> getElementbyXPathContent( String xpathExpression){
		Element element = null;
		Vector<Element> elements = new Vector<Element>();
		String xpath ="";
		
		/*
		 // Get all elements that equal the string cat
			String xpath = "//*[.='cat']";                       // 2 6

		// Get all elements that equal the string dog
			xpath = "//*[.='dog']";                              // (none)
		// Note that element #3 does not match because its
		// content is " dog " rather than "dog"

		// Get all elements that contain the string cat
			xpath = "//*[contains(.,'cat')]";                    // 1 2 4 5 6

		// Get all elem3 elements that contain the string cat
			xpath = "//elem3[contains(.,'cat')]";                // 6

		// Get all elements that contain the string cat,
		// ignoring the contents of any subelements
			xpath = "//*[contains(child::text(),'cat')]";        // 2 4 6

		// Get all elements without subelements and whose contents contains the string cat
			xpath = "//*[count(*)=0 and contains(.,'cat')]";     // 2 6 
		 */
		
		return getElementbyXPathLocation(xpath);
    }
    
    public Vector<Element> getElementbyXPathID( String xpathExpression){
		 
		String xpath ="";
		
		/*
		  // Get element id 3
			String xpath = "id('3')";                // 3

		// Get all e elements directly under element id 3
			xpath = "id('two')/e";                   // 3 4 6

		// Get elements with id='two', id='3', or id='seven'
			xpath = "id('two 3 seven the fifth')";   // two 3 seven
		// Note that this method of finding elements does not work
		// if the id value contains a space

		// Get a non-existent element
			xpath = "id('100')";                     // (none)
		 */
		
		return getElementbyXPathLocation(xpath);
    }
    
    public Vector<Element> getElementbyXPathAttributes( String xpathExpression){
		Element element = null;
		Vector<Element> elements = new Vector<Element>();
		String xpath ="";
		
		/*
		  
		  EXAMPLE XML Doc
		  
<?xml version="1.0" encoding="UTF-8"?>
<root id="1">
    <elem1 id="2" pet="cat"/>
    <elem1 id="3" pet=" dog " age="8"/>
    <elem1 id="4" pet="Cat" >
        <elem2 id="5">
            <elem3 id="6" age="10"/>
        </elem2>
    </elem1>
    <elem1 id="7" pet="dog"/>
</root>
		  
		// Get all elements where pet equals cat
			String xpath = "//*[@pet='cat']";                // 2

		// Get all elements where pet equals dog
			xpath = "//*[@pet='dog']";                       // 7
		// Note that element #3 does not match because the attribute
		// value is " dog " rather than "dog"

		// Get all elements where pet contains the string dog
			xpath = "//*[contains(@pet,'dog')]";             // 3 7

		// Get all elements that have the age attribute
			xpath = "//*[@age]";                             // 3 6

		// Get all elem1 elements that have the age attribute
			xpath = "//elem1[@age]";                         // 3

		// Get all elements that have both pet and age attributes
			xpath = "//*[@pet and @age]";                    // 1 3
 
		 */
		
		return getElementbyXPathLocation(xpath);
    }
    
    /*
    
    contents of an attribute
    
    You can access the contents of an attribute of a node selected like this as follows:
	Given the XML from the example, the following will print the "pet" attributes of all :

	XPath xpath ;
	XPathExpression expr;
	NodeList elem1List;
	
	
	xpath = XPathFactory.newInstance().newXPath();
	expr = xpath.compile("/root/elem1");
	elem1List = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	expr = xpath.compile("@pet");
	
	for (int i = 0; i < elem1List.getLength(); i++) {
		System.out.println(expr.evaluate(elem1List.item(i), XPathConstants.STRING));
	}

	In this case all are under ???. If, e.g., some are under root 
	and others are in other places, and you want to select them all, use "//elem1" instead of "/root/elem1".
    
    */
    
    
    
    
    
    public void selectFromChildElements( String xpath){
    	
    	/*
    	  
    	 	EXAMPLE XML doc
    	  
<?xml version="1.0" encoding="UTF-8"?>
<root id="1">
    <elem1 id="2">
        <elem2 id="3">
            <e id="4"/>
            <elem3 id="5">
                <e id="6"/>
            </elem3>
            <elem3 id="7"/>
        </elem2>
    </elem1>
    <elem1 id="8">
        <elem2 id="9"/>
        <e id="10"/>
        <e id="11"/>
    </elem1>
    <e id="12"/>
</root>
    	  
    	  
    	    /book/chapter[2]/section[3]
    	       
		// Get the first element under the root
			String xpath = "/STAR/STAR[1]";            // 2

        // Get the second elem1 element under the root
			xpath = "/root/elem1[2]";            // 8

		// Get all first-born e elements in the document; that is, for all
		// e elements with e element siblings, include only the first sibling
			xpath = "//e[1]";                    // 4 6 10 12  
    	      
    	       
    	// Get the first e element in the document
			xpath = "(//e)[1]";                  // 4

		// For all e elements with e element siblings, include only
		// the first 3 siblings
			xpath = "//e[position() <= 3]";      // 4 6 10 11 12

		// Get all last-born e elements in the document; that is, for all
		// e elements with e element siblings, include only the last sibling
			xpath = "//e[last()]";               // 4 6 11 12

		// Get the last e element in the document
			xpath = "(//e)[last()]";             // 12
    	*/
    }
    
    
} // end of class BasicDom

/*
     Example XML DOc
     
     <?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE root [ <!ELEMENT e (e*) >
                 <!ATTLIST e  id    ID    #REQUIRED>
]>
<root>
    <e id="1">
        <e id="two">
            <e id="3"/>
            <e id="4">
                <e id="the fifth"/>
            </e>
            <e id="6"/>
        </e>
    </e>
    <e id="seven"/>
</root>

 
 
 
 */
/* XPath expressions

STAR = *  !!!!


// Get the root element (without specifying its name)
String xpath = "/*";                       // 1

// Get the root element (using its name)
xpath = "/root";                           // 1

// Get all elements directly under the root
xpath = "/root/*";                         // 2 8 12

// Get all e elements directly under the root
xpath = "/root/e";                         // 12

// Get all e elements in the document
xpath = "//e";                             // 4 6 10 11 12

// Get all non-e elements in the document
xpath = "//*[name() != 'e']";              // 1 2 3 5 7 8 9

// Get all e elements directly under an elem1 element
xpath = "//elem1/e";                       // 10 11

// Get all e elements anywhere under an elem1 element
xpath = "//elem1//e";                      // 4 6 10 11

// Get all elements with at least one child element
xpath = "//*[*]";                          // 1 2 3 5 8

// Get all elements without a child element
xpath = "//*[not(*)]";                     // 4 6 7 9 10 11 12

// Get all elements with at least one child e element
xpath = "//*[e]";                          // 1 3 5 8

// Get all elements with more than one child e elements
xpath = "//*[count(e)>1]";                 // 8

// Get all non-e elements without an e child element
xpath = "//*[not(e) and name() != 'e']";   // 2 7 9

// Get all level-4 e elements (the root being at level 1)
xpath = "/STAR/STAR/STAR/e";                        // 4

// Get all elements with more than one child e elements
xpath = "//*[count(e)>1]";                 // 8



// XPath 1.0 does not support regular expressions to match element names. However, it is possible to perform some very simple matches on element names.
 

// Get all elements whose name starts with el
xpath = "//*[starts-with(name(), 'el')]";  // 2 3 5 7 8 9

// Get all elements whose name contains with lem1
xpath = "//*[contains(name(), 'lem1')]";   // 2 8



//Sets of elements can also be combined using the union operator |
 

// Get all e elements directly under either the root or an elem2 element
xpath = "/STAR/e | //elem2/e";                // 4 12
     


*/


