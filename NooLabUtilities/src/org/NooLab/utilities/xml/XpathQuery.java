package org.NooLab.utilities.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

 
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.strings.StringsUtil;
import org.jdom.Document;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jdom.input.SAXBuilder;

import com.jamesmurty.utils.XMLBuilder;

 



/*
 * http://code.google.com/p/google-guice/
 * http://www.javaprogrammingforums.com/java-programming-tutorials/1540-object-injection.html
 * 
 * 
 * 
 * 
 * see:  google: java xml howto transform object class into xml JaxbTest1
 * 
 * http://www.w3schools.com/xpath/xpath_syntax.asp
 * http://www.exampledepot.com/egs/org.w3c.dom/pkg.html
 * 
 * ATTRIBUTES
 * - Get all e elements directly under element id 3:  <e id="two">	
 *   xpath = "id('two')/e";
 * 
 * - Get all elements where pet equals cat:  <elem1 id="2" pet="cat"/>
 *   String xpath = "//*[@pet='cat']";
 *     
 * - Get all elements where pet contains the string dog
 *   xpath = "//*[contains(@pet,'cat')]"; 
 * 
 * - Get all elem1 elements that have the age attribute
 *   xpath = "//elem1[@age]"; 
 * 
 * 
 * ORDER
 * 
 * - Get the first e element in the document
 *   xpath = "(//e)[1]";
 * 
 * - Get the last e element in the document
 *   xpath = "(//e)[last()]";
 *   
 *   
 * STRUCTURE
 * 
 * - Get all e elements directly under an elem1 element
 *   xpath = "//elem1/e";                       // 10 11
 *   
 * - Get all e elements anywhere under an elem1 element
 *   xpath = "//elem1//e";  
 *   
 *   
 * - Get all elements with at least one child e element
 *   xpath = "//*[e]"; 
 *   
 * - Get all non-e elements in the document
 *   xpath = "//*[name() != 'e']"; 
 *   
 * - Get all elements directly under the root
 *   xpath = "/root/*";
 *   
 * - Get all elements with at least one child e element
 *   xpath = "//*[e]";   
 *   
 *   
 * AXIS  
 *   
 * - Selects all attributes of the current node
 *   attribute::*	
 *   
 * - child::node() 	Selects all children of the current node
 * 
 * - child::book 	Selects all book nodes that are children of the current node
 *   
 */
public class XpathQuery {

	
	org.w3c.dom.Document domDoc ;
	XPath xpath;
	
	String xmlStr = "" ;
	InputStream xmlStream = null;
	
	String uuidStr="$" ;
	
	DFutils fileutil = new DFutils() ;
	StringsUtil strgutil = new StringsUtil() ;
	
	public XpathQuery(){
		
		xpath = XPathFactory.newInstance().newXPath();
		
	}
	

	public String getReplacementSecret( ){
		return uuidStr;
	}
	
	public void setReplacementSecret( String uuidstr){
		uuidStr = uuidstr;
	}
	
	
	public void parseXml(){
		try{
			domDoc = parseXmlStr() ;
		}catch(Exception e){
			System.out.println("parseXml:\n"+xmlStr) ;
			e.printStackTrace() ;
		}
	}
	
	public void setXml( String xmlstr){
		
		int p1,p2 ;
		
		p1 = xmlstr.indexOf("::");
		p2 = xmlstr.indexOf("<?xml");
		
		if ((p1>0) && (p2>p1)){
			xmlstr = xmlstr.substring(p2, xmlstr.length()) ;
			xmlstr = xmlstr.replace(":<", "<");
		}
		
		p1 = xmlstr.trim().indexOf("<?xml") ;
		
		xmlStr = "" ;
		if ((p1>=0) && (p1<=2)){
			xmlStr = xmlstr.trim();
		}
		
		domDoc=null;
	}

	public void setXmlFromFile( String filename ){
		  
		try {
		
			if (fileutil.fileexists(filename)) {

				xmlStr = fileutil.readFile2String(filename);
				domDoc=null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void setXml( InputStream instream){
		
		xmlStream = instream ;
		domDoc=null;
	}
	
	
	public String getElementsIncValue( String xmlpath ) { // e.g.  "//transaction/data/values" 
		String xvalue="";
		
		String str="  ";
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;
		Element element;
		
		if (domDoc==null){
			domDoc = parseXmlStr( ) ;
		}
		
		if (domDoc==null){
			return "" ;
		}
		
		try{
			
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			if (nodes.getLength() > 0) {

				node = nodes.item(0);
				
				if (node!=null){
					str = node.getNodeName();
			
					xvalue = node.getTextContent() ;
				   
					// preventing null return
					if (xvalue==null){
						xvalue="" ;
					}
					
				} // node really existent ?
			} // such a node found ?
			
		}catch(Exception e){
			
		}
		
		 
		
		return xvalue;
	}
	
	public String getXMLString(){
		
		String xmlstr ="" ;
		
		XMLBuilder builder ;
		 
		Transformer transformer = null ;
		StreamResult sr ;
		DOMSource source ;
		
		
		try{
			if (domDoc==null){
				domDoc = parseXmlStr( ) ;
			}

			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			sr = new StreamResult(new StringWriter());
			source = new DOMSource(domDoc);
			
			transformer.transform(source, sr);

			xmlstr = sr.getWriter().toString();
			 
		}catch(Exception e){
			
		}
		 
		return xmlstr;
	}
	
	
 
	
	public void addChildTagElement( String xmlpath, String tagname  ){ // e.g.  "//transaction", "request" 
	
		addChildTagElement( xmlpath, tagname ,0 ) ;
	}
	
	
	
	@SuppressWarnings("static-access")
	public void addChildTagElement( String xmlpath, String tagname , int separateClosing ){	
		String str="" ;
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, hostingNode ;
		
		Node newNode ;
		
		newNode = domDoc.createElement( tagname );
		
		if (separateClosing>0){
			newNode.setTextContent("!-- "+uuidStr+" --!");
		}
		
		try{
			
			
			expr = xpath.compile( xmlpath );
			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			
			Thread.currentThread().yield();
			Thread.currentThread().sleep(0,200);
			
			nodes = (NodeList) xresult;

			if (nodes.getLength() > 0) {

				hostingNode = nodes.item(0);

				// canada.setTextContent("ca");
				hostingNode.appendChild( newNode);
				
				Thread.currentThread().yield();
				Thread.currentThread().sleep(1);
				
				str =  getXMLString();
				
				str= str+"";
			} // xml path exists

			
		}catch(Exception e){
			
		}
			
		
		
		
	}
	
	public String removeElement( String xmlpath ) { // e.g.  "//transaction/name"
		/*
		
		add an attribute to the earth node:

			Node earth = doc.getFirstChild();
			NamedNodeMap earthAttributes = earth.getAttributes();
			Attr galaxy = doc.createAttribute("galaxy");
			galaxy.setValue("milky way");
			earthAttributes.setNamedItem(galaxy);
		*/
		
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, pn;
		NamedNodeMap nodeAttr;
		
		String xvalue, str="", nodeName;
		
		try{
			
			if (domDoc==null){
				domDoc = parseXmlStr( ) ;
			}
			
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			str =  getXMLString();
			
			if (nodes.getLength() > 0) {

				node = nodes.item(0);
				
				pn = node.getParentNode();
				
				str = pn.getNodeName() ;
				str = node.getNodeName() ;
				
				pn.removeChild( node );

				str =  getXMLString();
				
				str= str+"";
			} // xml path "//register//name" exists

			
		}catch(Exception e){
			
		}
			
		return str ;
	}
	
	
	public void removeAttribute(  String xmlpath, String attrName ){ // "//transaction/request", "request"

		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;
		
		Attr newAttribute ;
		
		String xvalue, str=""   ;
		
		try{
			
			
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			if (nodes.getLength() > 0) {

				node = nodes.item(0);
				 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					if (nodeAttr != null) {
				
						nodeAttr.removeNamedItem(attrName) ;
					}
					 
				str =  getXMLString();
				
				str= str+"";
			} // xml path "//register//name" exists

			
		}catch(Exception e){
			
		}
			
		
		
	}
	
	public void addAttribute( String xmlpath, String attrName , String attrValue){
	 
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;
		
		Attr newAttribute ;
		
		String xvalue, str=""   ;
		
		try{
			
			
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			if (nodes.getLength() > 0) {

				node = nodes.item(0);
				 
				 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					if (nodeAttr != null) {
				
						newAttribute = domDoc.createAttribute( attrName );  
						newAttribute.setValue(attrValue);
					
						nodeAttr.setNamedItem(newAttribute);
					}
					 
				str =  getXMLString();
				
				str= str+"";
			} // xml path "//register//name" exists

			
		}catch(Exception e){
			
		}
		
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * @param xmlpath
	 * @param nodeName
	 * @param attrName
	 * @param attrValue
	 */
	public void setAttributesValue( String xmlpath, String nodeName, String attrName , String attrValue){
		 
		String str="", xvalue="";
		int n,nn;

		
		String nodestr; 
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;

		
		nodeName = strgutil.trimm(nodeName, "/");
		if (nodeName.indexOf("/")>0){
			int p = nodeName.lastIndexOf("/");
			if (p>0){
				str = nodeName.substring(0, p);
				nodestr = nodeName.substring(p+1, nodeName.length()) ;
				
				xmlpath = xmlpath+ "/" +str+ "/" +nodestr;
				nodeName = nodestr ;
			}
		}
		
		if (domDoc==null){
			domDoc = parseXmlStr( ) ;
		}
		
		if (domDoc==null){
			return    ;
		}
		
		try {
 
			// expr = xpath.compile("//register//name");
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			
			nn = nodes.getLength();
			
			if (nn > 0) {

				node = nodes.item(0);
				str = node.getNodeName();

				if (str.contentEquals( nodeName)) { // e.g. "name" 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					if (nodeAttr != null) {
						for (int ac = 0; ac < nodeAttr.getLength(); ac++) {
							anode = nodeAttr.getNamedItem(attrName); // e.g. "value" 

							if (anode != null) {
								str = anode.getNodeName(); // "value"
								xvalue = anode.getNodeValue(); // the value of the
															// tag [name]
								
								// if (attrName.contentEquals(str)) 
								{// make the change
									anode.getFirstChild().setNodeValue( attrValue ); 
								}
								break;
							} // anode != null ?
						} // ac ->
					} // nodeAttr != null ?
					
				} // node == nodeName ?

			} // xml path exists

			
		}catch(Exception e){
			
		}
		 
		str =  getXMLString();
		str = str + "" ;
	}
	
	
	public String readNode( Object xmlNodeObj, String attrLabel ){
		
		String resultStr="", str ;
		
		Node node, anode;
		NamedNodeMap nodeAttr;
		 
		
		node = (Node)xmlNodeObj;
		
		nodeAttr = node.getAttributes(); // attributes are tags !!!

		if (nodeAttr != null) {
			//for (int ac = 0; ac < nodeAttr.getLength(); ac++) {
			anode = nodeAttr.getNamedItem( attrLabel );
			
			if (anode!=null){
				str = anode.getNodeName(); // "value"
				resultStr = anode.getNodeValue();
			}
			
			
		} // nodeAttr != null ?
			
		return resultStr;
	}
	
	public Object getMatchingXmlNode( String xpathQuery ){
		
		Object xresult = null;
		int nk;
		
		XPathExpression expr;
		NodeList nodes;
		
		
		
		try{
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return null;
			}
			
			
			expr = xpath.compile( xpathQuery );
			
			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			if (nodes.getLength() > 0) {
				nk = nodes.getLength() ;
				
				for (int k=0;k<nk;k++){
					
					
					
				}// k->
				
				xresult = nodes.item(0) ;
				
			} // nodes len > 0 ?
			
		}catch(Exception e){
			
		}
		
		
		
		return xresult;
	}
	
	
	public Object geXmlNodeByName( String fullxmlpath){
		Node node=null , resultnode=null;
		String nodeName, str ;
		int p,nn;
		XPathExpression expr;
		Object xresult;
		NodeList nodes ;
		
		str = strgutil.trimm( fullxmlpath, "//") ;
		str = strgutil.trimm( str, "/") ;
		p = str.lastIndexOf("/");
		if (p>0){
			str = str.substring(p+1, str.length()) ;
		}
		nodeName = str;
		
		
		try{
			
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return null;
			}
			
			expr = xpath.compile( fullxmlpath );
			
			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			nn = nodes.getLength();
			
			if (nn > 0) {
				node = nodes.item(0); 
				
				str = node.getNodeName();

				if (str.contentEquals( nodeName)) {
					
					resultnode = node;
				}
				
			}
			
			
		}catch(Exception e){
			
		}
		
		return resultnode;
	}
	/**
	 * 
	 * this works much as the next method "getAttributesValue()", the difference being just that 
	 * it returns the node object... this can be used for a kind of caching
	 * 
	 * 
	 * @param xmlpath
	 * @param nodeName
	 * @param attrName
	 * @return
	 */
	public Object getMatchingXmlNode( String xmlpath, String nodeName, String attrName, String attrValue  ){
		Node node=null, resultnode=null;
	
		String str="", xvalue="";
		int n,nn;

		 
		// Get all elements where pet equals cat
		// String xpath = "//*[@pet='cat']"; 

		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node anode;
		NamedNodeMap nodeAttr;

		try {
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return null;
			}
		
		

			if (nodeName.length()==0){
				str = xmlpath;
				int p = str.lastIndexOf("/");
				nodeName = str.substring(p+1,str.length()) ;
			}
			nodeName = nodeName.replace("//", "") ; nodeName = nodeName.replace("/", "") ;

			// expr = xpath.compile("//register//name");
			
			expr = xpath.compile( xmlpath );

			 
			 
			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			nn = nodes.getLength();
			
			if (nn > 0) {
				
				{
				// loop ? 
				node = nodes.item(0);
				str = node.getNodeName();

				if (str.contentEquals( nodeName)) { // e.g. "name" 
					nodeAttr = node.getAttributes(); // attributes are tags !!!
					resultnode = node;
					
					if ((nodeAttr != null) && (attrName.length()>0)) {
						for (int ac = 0; ac < nodeAttr.getLength(); ac++) {
							anode = nodeAttr.getNamedItem(attrName); // e.g. "value" 

							if (anode != null) {
								str = anode.getNodeName(); // "value"
								xvalue = anode.getNodeValue(); // the value of the
															// tag [name]
								resultnode = node; 
								break;
							} // anode != null ?
						} // ac ->
					} // attrName.length() > 0 ?
					// break; // in case of a loop only the first occurrence .... 
				} // getNodeName == nodeName ?

			  } // ?? ->
			} // xml path "//register//name" exists
			
		} catch (XPathExpressionException e) {
			System.out.println("Exception in XpathQuery : node:"+nodeName+", attribute"+attrName+"\r\n"+xmlStr) ;
			e.printStackTrace();
		}
		
		return resultnode;
	}
	
	
	/**
	 * 
	 * here we do not provide an attribute, which causes the objects in the collection being 
	 * a String[], containing the values, while the very first element in the collection contains
	 * the attribute labels<br/>
	 * as a consequence we represent is as a kind of a table...
	 *  
	 * 
	 * @param xmlpath
	 * @param nodeName
	 * @return
	 * 
	 * This is not quite ready!!
	 */
	public Vector<Object> getAttributesValuesAsTable( String xmlpath, String nodeName){
		Vector<Object> listItems = new Vector<Object>(); 
		String[] taggyrow;

		String str="", attrlabel,xvalue="";
		int n,k = 0;
		boolean firstrow;
		
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;

		try {
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return listItems;
			}
		
		

			if (nodeName.length()==0){
				str = xmlpath;
				int p = str.lastIndexOf("/");
				nodeName = str.substring(p+1,str.length()) ;
			}
			nodeName = nodeName.replace("//", "") ; nodeName = nodeName.replace("/", "") ;

			// expr = xpath.compile("//register//name");
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			k = nodes.getLength();
			
			if (nodes.getLength() > 0) {
				
				for (int nk=0;nk<k;nk++){
				 
				node = nodes.item( nk );
				str = node.getNodeName();
				firstrow  = false;
				
				if (str.contentEquals( nodeName)) { // e.g. "name" 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					if (nodeAttr != null) {
						int na = nodeAttr.getLength();
						
						
							if (listItems.size()==0){
								firstrow  =true;
							
								taggyrow = new String[na];
								listItems.add(taggyrow) ;
							}
						
							taggyrow = new String[na];
							listItems.add(taggyrow) ;
							
							for (int ac = 0; ac < na; ac++) {
							
								anode = nodeAttr.item(ac);
								
								if (anode != null) {
									attrlabel = anode.getNodeName(); // "value"	
									if (firstrow){
										((String[])listItems.get(0))[ac] = attrlabel;
									}
									
									xvalue = anode.getNodeValue(); // the value of the
											
									taggyrow[ac] = attrlabel;
									
								} // anode != null ?
							} // ac->
							listItems.add(taggyrow) ;	
					} // nodeAttr != null ??
						
				} // requested nodeName ?
					
				} // nk->
			} // xml path "//register//name" exists
			
		} catch (XPathExpressionException e) {
			System.out.println("Exception in XpathQuery : node:"+nodeName+" \r\n"+xmlStr) ;
			e.printStackTrace();
		}
		k=k+0;
		
		
		
		return listItems;
	}
	
	
	public Vector<Object> getAttributesValues( String xmlpath, String nodeName ){
		Vector<Object> listItems = new Vector<Object>(); 
 
		String str="", xvalue="";
		int n,k = 0;

		Map<String,String> taggyRow  ; 
		
		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;

		try {
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return listItems;
			}
		
		

			if (nodeName.length()==0){
				str = xmlpath;
				int p = str.lastIndexOf("/");
				nodeName = str.substring(p+1,str.length()) ;
			}
			nodeName = nodeName.replace("//", "") ; nodeName = nodeName.replace("/", "") ;

			// expr = xpath.compile("//register//name");
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			k = nodes.getLength();
			if (nodes.getLength() > 0) {

				for (int nk=0;nk<k;nk++){
					 
				node = nodes.item( nk );
				str = node.getNodeName();

					if (str.contentEquals( nodeName)) { // e.g. "name" 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					  if (nodeAttr != null) {
						int na = nodeAttr.getLength();
						
						taggyRow = new HashMap<String,String>();
						
						for (int ac = 0; ac < na; ac++) {
							
							anode = nodeAttr.item(ac);
							
							if (anode != null) {
								str = anode.getNodeName(); // "value"
								xvalue = anode.getNodeValue(); // the value of the
								taggyRow.put(str, xvalue) ;
							} // anode != null ??
							
						} // ac->
					  	
					  	listItems.add(taggyRow) ;
					  }// nodeAttr != null
					  
					} // == requested nodeName ?
				} // nk->
				
			} // xml path "//register//name" exists
			
		} catch (XPathExpressionException e) {
			System.out.println("Exception in XpathQuery : node:"+nodeName+" \r\n"+xmlStr) ;
			e.printStackTrace();
		}
		k=k+0;
		
		
		return listItems;
		
	}
	
	public Vector<Object> getNodesByName( Node node, String nodeName){
		
		Vector<Object> listItems = new Vector<Object>(); 
		
		NodeList nodes;
		int k,ntyp;
		String str;
		
		if (node==null){
			return listItems;
		}
		nodes = node.getChildNodes();
		
		k = nodes.getLength();
		if (nodes.getLength() > 0) {

			for (int nk = 0; nk < k; nk++) {

				node = nodes.item(nk);
				
				ntyp = node.getNodeType(); // 3=#text 1=element
				str = node.getNodeName();

				if ((ntyp==1) && (str.contentEquals(nodeName))) { // e.g. "name"

					listItems.add(node) ;
				} // == requested nodeName ?
			} // nk->

		} // xml path "//register//name" exists

		
		return listItems;
	}
	/**
	 * 
	 * if we did not provide an attribute to look for (its value), we will return the whole node !!
	 * 
	 * 
	 * @param xmlpath
	 * @param nodeName
	 * @param attrName
	 * @return
	 */
	public Vector<Object> getAttributesValues( String xmlpath, String nodeName, String attrName){
		Vector<Object> listItems = new Vector<Object>(); 
		

		String str="", xvalue="";
		int n,k = 0;

		

		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;

		try {
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return listItems;
			}
		
		

			if (nodeName.length()==0){
				str = xmlpath;
				int p = str.lastIndexOf("/");
				nodeName = str.substring(p+1,str.length()) ;
			}
			nodeName = nodeName.replace("//", "") ; nodeName = nodeName.replace("/", "") ;

			// expr = xpath.compile("//register//name");
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;
			k = nodes.getLength();
			if (nodes.getLength() > 0) {

				for (int nk = 0; nk < k; nk++) {

					node = nodes.item(nk);
					str = node.getNodeName();

					if (str.contentEquals(nodeName)) { // e.g. "name"

						// if we did not provide an attribute to look for (its value), we will rturn the whole node !!
						if (attrName.length() > 0) {
							nodeAttr = node.getAttributes(); // attributes are
																// tags !!!

							if (nodeAttr != null) {
								int na = nodeAttr.getLength();

								for (int ac = 0; ac < na; ac++) {

									anode = nodeAttr.getNamedItem(attrName); // e.g.
																				// "value"

									anode = nodeAttr.item(ac);

									if (anode != null) {
										str = anode.getNodeName(); // "value"
										xvalue = anode.getNodeValue(); 
										// the value of the

										if (str.contentEquals(attrName)) {
											listItems.add(xvalue);
											break;
										}
									} // anode != null ??

								} // ac->
							}// nodeAttr != null
						} else {
							listItems.add(node);
						}

					} // == requested nodeName ?
				} // nk->

			} // xml path "//register//name" exists

		} catch (XPathExpressionException e) {
			System.out.println("Exception in XpathQuery : node:"+nodeName+", attribute"+attrName+"\r\n"+xmlStr) ;
			e.printStackTrace();
		}
		k=k+0;
		
		
		return listItems;
	}
	
	
	
	
	public Vector<Object> getMatchingXmlNodes( String xpathQuery ){
		
	
		Vector<Object> xresult = null;
		int nk;
		
		XPathExpression expr;
		NodeList nodes;
		Object evobj;
		
		
		try{
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}
	
			if (domDoc == null) {
				return null;
			}
			
			
			expr = xpath.compile( xpathQuery );
			
			evobj = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) evobj;
	
			if (nodes.getLength() > 0) {
				nk = nodes.getLength() ;
				
				for (int k=0;k<nk;k++){
					
					xresult.add( nodes.item(k) ) ;
					
				}// k->
				
			} // nodes len > 0 ?
			
		}catch(Exception e){
			
		}
		 
		return xresult;
	}

	public String getAttributesValue( String xmlpath, String nodeName, String attrName  ){
		
		String str="", xvalue="";
		int n,i;

		

		XPathExpression expr;
		Object xresult;
		NodeList nodes, nodelist;
		Node node, anode, childnode;
		NamedNodeMap nodeAttr;

		try {
			
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}

			if (domDoc == null) {
				return "";
			}
		
		

			if (nodeName.length()==0){
				str = xmlpath;
				int p = str.lastIndexOf("/");
				nodeName = str.substring(p+1,str.length()) ;
			}
			nodeName = nodeName.replace("//", "") ; nodeName = nodeName.replace("/", "") ;

			// expr = xpath.compile("//register//name");
			expr = xpath.compile( xmlpath );

			xresult = expr.evaluate(domDoc, XPathConstants.NODESET);
			nodes = (NodeList) xresult;

			int nn = nodes.getLength();
			
			if (nn > 0) {

			  node = nodes.item(0);
			  str = node.getNodeName();
			  for (i=0;i<nn;i++){
					
				  node = nodes.item(i);
				  
				if (str.contentEquals( nodeName)) { // e.g. "name" 
					nodeAttr = node.getAttributes(); // attributes are tags !!!

					if (nodeAttr != null) {
						for (int ac = 0; ac < nodeAttr.getLength(); ac++) {
							anode = nodeAttr.getNamedItem(attrName); // e.g. "value" 

							if (anode != null) {
								str = anode.getNodeName(); // "value"
								xvalue = anode.getNodeValue(); // the value of the
															// tag [name]
							}
						}
					} // nodeAttr ?
				} // nodeName ?
			  }// i->
			} // xml path "//register//name" exists
			
		} catch (XPathExpressionException e) {
			System.out.println("Exception in XpathQuery : node:"+nodeName+", attribute"+attrName+"\r\n"+xmlStr) ;
			e.printStackTrace();
		}
		return xvalue;
	}
	
	private org.w3c.dom.Document parseXmlStr( InputStream instream){
		
		DocumentBuilderFactory dbf;
		DocumentBuilder db ;
		
		try {
			
			dbf = DocumentBuilderFactory.newInstance();
			//Using factory get an instance of document builder
			db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
				domDoc =  db.parse(instream) ;
		
		/*	
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			
			se.printStackTrace();
		*/
		}catch (Exception e) {
			// e.printStackTrace();
		}

		return domDoc ;
	}

	public org.w3c.dom.Document parseXmlStr( ){
		  
		
		try {
			xmlStream = null;
			if (xmlStr.length()>2){
				xmlStream = new ByteArrayInputStream( xmlStr.getBytes("UTF-8") );
			}
			// opposite direction is using a java obj
			// StreamToString sts = new StreamToString();
			
			if ((xmlStream!=null) && (xmlStream.available()>0)){
				domDoc = parseXmlStr(xmlStream) ;
			}
			
			Thread.yield(); // tribute to SAX...
			
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return domDoc ;
	}

	public org.w3c.dom.Document getDomDoc() {
		return domDoc;
	}

	public String changeAtrributeValue( String parentTagName, String attr, String valueStr) {
		
		String outStr = "";
		
		/*
		// update salary value
		staff.getChild("salary").setText("7000");
 
		// remove firstname element
		staff.removeChild("firstname");
		
		 */
		
		
		
		return outStr ;
	}
	
	
	public String insertNode( String parentTagName, String newTagNode, String attr, String valueStr ) {

		String outStr = "", completeXPath;
		int sc=0;
		boolean hb=false;
		Node node;
		
		try{
		
			if (domDoc == null) {
				domDoc = parseXmlStr();
			}
			completeXPath = parentTagName + "/" +newTagNode ;
			
			if (attr.length()==0){
				sc=1;
			}
			// addChildTagElement( "//transaction", "relay" ) ;
			
			node = (Node) geXmlNodeByName( completeXPath );
			
			hb = (node!=null );
			
			if (hb==false){
				addChildTagElement( parentTagName, newTagNode, sc) ;
				Thread.yield(); Thread.sleep(1);
			}
			if (attr.length()>0){
				addAttribute(completeXPath, attr, valueStr ) ;
				Thread.yield(); Thread.sleep(1);
			}
			
			outStr = getXMLString() ;
			
		}catch(Exception e){
			
		}
		return outStr;
	}
	
}
