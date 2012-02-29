package org.NooLab.utilities.xml;

/*
 Java, XML, and Web Services Bible
 Mike Jasnowski
 ISBN: 0-7645-4847-6
 */



import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.* ;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.NooLab.utilities.logging.PrintLog;
import org.apache.xerces.parsers.*;
 
import org.apache.xerces.dom.NodeImpl;
 


public class XmlFileRead{
	

	Vector<XmlSection> sections = new Vector<XmlSection>() ; 
	
	int count;
	
	String filename ;

	// volatile variables .............
	
	
	// helper objects .................
	
	XmlTree xmltree ; 
	// DefaultMutableTreeNode root ;
	XTreeNode root;
	Vector<Object> nodeDataObj = new Vector<Object>();
	
	PrintLog out  ;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 
	public XmlFileRead(){
		
	}
	
	public XmlFileRead(String filname){
		
		
		filename = filname ;
		 
		// filename = "D:/dev/java/data/test/a.xml";
		
		xmltree = new XmlTree( filename ) ;
		
		root = xmltree.getTreeRoot();
		
		
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	public String getXmlTagData( String nodelabel, String taglabel){
		String content="" ;
		
		
		return content;
	}
	
	
	@SuppressWarnings("rawtypes")
	
	public String getXmlTagData( String nodelabel1, String nodelabel2, String taglabel){
		
		int level , d,k ;
		boolean closing = false, lastC, tagmatches=false;
		
		String content="", str = "" , sp,treepath, parentLabel="",sectionlevelStr="";
		Enumeration enumNodes = null ;
		TreeNode[] xpath;
		// DefaultMutableTreeNode root ;
		// DefaultMutableTreeNode node;
		DefaultMutableTreeNode dnode;
		XTreeNode node ;
		
		XNodeData nodeContent = null ;
		Object obj;
		
		String[] currentHierarchy = new String[16]; 
		
		
		d = 0; 
		dnode = root;
		while (dnode != null ){
			
			 
			dnode = dnode.getNextNode() ; 
			
			if (dnode != null){
				
				xpath = dnode.getPath() ; xpath[0] = null ;
				
				if (xpath.length>1){
					str = (xpath[xpath.length-1]).toString() ;
				}
				// nodeDataObj.add( node.getUserObject() );
				obj = dnode.getUserObject();
				str = obj.toString();
				
				if (str.contains(".XNodeData@")){
					
					level = dnode.getLevel()-1 - d  ;
					sp="";
					for (int i=0;i<level;i++){
						sp=sp+"  ";
					}
					
					nodeContent = (XNodeData)( obj ); 
				 
					// System.out.println( sp+"    tag "+nodeContent.index+":  "+nodeContent.name + " = " + nodeContent.value);
					
					if ((taglabel.length()>0) && (taglabel.contentEquals(nodeContent.name))){
						tagmatches = true;
					} else {
						tagmatches = false;
					}
					
				} else{
					
					lastC = closing;
					
					
					if (str.contentEquals("//")){
						closing = true ;
						
						if((closing) && (lastC)){
							  d++;
							} else {
								d = 0;

							}
					} else {
						closing = false ;
						d=0;
					}

					level = dnode.getLevel()-1 - d  ;
					sp="";
					for (int i=0;i<level;i++){
						sp=sp+"  ";
					}
					// System.out.println(sp+""+str+"  (L"+level+")");
					
					if (closing==false){
						currentHierarchy[level] = str;
					}else{
						for (k=level;k<16;k++){
							currentHierarchy[k] = "" ;
						}
					}
				}
				
				if ((tagmatches) || (taglabel.length()==0)){
					k=0;
					sectionlevelStr = currentHierarchy[level-1] ; // "-1" ... ?
					parentLabel = dnode.getParent().toString();
					if (parentLabel.contentEquals(sectionlevelStr)){
						sectionlevelStr = currentHierarchy[level-2] ; // "-1" ... ?
						parentLabel = dnode.getParent().getParent().toString();
						if (parentLabel.contentEquals(sectionlevelStr)){
							content = nodeContent.value ;
							break ;
						}
					}
				}
				// nodelabel1, nodelabel2, taglabel
			} // node ?
			//   
	
		}

		
		return content;
	}
	
    
	private String getTreePath( DefaultMutableTreeNode node){
		String tpath="", str, hs1, hs2;
		int p,d=0; 
		TreeNode[] pc;
		
		pc = node.getPath();
		p = pc.length ;
		
		hs1 = node.getParent().toString()+"//" ;
		hs2 = node.getUserObject().toString() ;
		
		if (hs1.contentEquals(hs2)){
			d=2;
		}
		
		for (int i=1;i<p-d;i++){
			str = pc[i].toString() ;
			
			tpath = tpath+str;
			
			if (i<p-1){
				tpath=tpath+".";
			}
		}
		
		return tpath ;
	}

	 
	public Vector<Vector<String>> getsimpleXMLElementData( String text){
		Vector<Vector<String>>  eleSections = null;

		
		return eleSections;
	}

} // main class XmlFileRead

 



class XmlTree {
 

	// DefaultMutableTreeNode rootNode;
	XTreeNode rootNode;
	
	private SAXTreeBuilder saxTree = null;

	
	public XmlTree(String filename) {
		SAXParser saxParser;
		// DefaultMutableTreeNode top;

		// top = new DefaultMutableTreeNode( filename );
		// DefaultMutableTreeNode top = new DefaultMutableTreeNode("XML Document");

		
		XTreeNode top;

		top =  (new XTreeNode( filename )) ;
		// (DefaultMutableTreeNode)
		
		
		saxTree = new SAXTreeBuilder(top);
		 

		try {
			saxParser = new SAXParser();
			saxParser.setContentHandler(saxTree);
			
			saxParser.parse(new InputSource(new FileInputStream( filename )));
			
		} catch (Exception ex) {
			top.add(new DefaultMutableTreeNode(ex.getMessage()));
		}

		rootNode = saxTree.getTree();

	}
	
	// public DefaultMutableTreeNode getTreeRoot(){
	public XTreeNode getTreeRoot(){
		
		return rootNode;
	}

}


class SAXTreeBuilder extends DefaultHandler {
	/*
	private DefaultMutableTreeNode currentNode = null;
	private DefaultMutableTreeNode previousNode = null;
	private DefaultMutableTreeNode rootNode = null;
	*/
	
	private XTreeNode currentNode = null;
	private XTreeNode previousNode = null;
	private XTreeNode rootNode = null;

	
	public SAXTreeBuilder(XTreeNode root){ // DefaultMutableTreeNode root) {
		rootNode =  root;
	}

	public void startDocument() {
		currentNode = rootNode;
	}

	public void endDocument() {
		
	}

	public void characters(char[] data, int start, int end) {
		
		String str = new String(data, start, end);
		if (!str.equals("") && Character.isLetter(str.charAt(0))){
			
			// currentNode.add(new DefaultMutableTreeNode(str));
			currentNode.add(new XTreeNode(str));
		}
	}

	public void startElement( String uri, String qName, 
							  String lName,
							  Attributes atts) {
		
		previousNode = currentNode;
		
		// currentNode = new DefaultMutableTreeNode(lName);
		currentNode = new XTreeNode(lName);
		currentNode.level = currentNode.getLevel() ;
		// content = id suvID 
		// Add attributes as child nodes //
		// Attributes  org.apache.xerces.util.XMLAttributesImpl 
		  
		
		attachAttributeList(currentNode, atts);
		
		previousNode.add(currentNode);
	}

	public void endElement(String uri, String qName, String lName) {
		XTreeNode parentnode;
		
		if (currentNode.getUserObject().equals(lName)){
			// currentNode = (DefaultMutableTreeNode) currentNode.getParent();
			parentnode = (XTreeNode) currentNode.getParent();
			currentNode = parentnode ;
			currentNode.level = parentnode.getLevel() ;
		}
		
		
		// previousNode.add(new DefaultMutableTreeNode(qName+"//")) ; // lName+"//"
		previousNode.add(new DefaultMutableTreeNode("//")) ;  
	}

	// public DefaultMutableTreeNode getTree() {
	public XTreeNode getTree() {
		return rootNode;
	}

	// private void attachAttributeList( DefaultMutableTreeNode node,
	private void attachAttributeList( XTreeNode node,
	  		                          Attributes atts) {
		
		String str, value = "",name = "",typestr ;
		// DefaultMutableTreeNode subnode ;
		XTreeNode subnode;
		int k ;
		XNodeData  nodedata  ;
		
		 
		
		for (int i = 0; i < atts.getLength(); i++) {
			nodedata = new XNodeData();
			
			nodedata.uri = atts.getURI(i) ;
			nodedata.qName = atts.getQName(i) ;
			
			nodedata.name = atts.getLocalName(i);
			nodedata.value = atts.getValue(nodedata.name);
		
			name = nodedata.name; 
			value = nodedata.value; 
			
			nodedata.typestr = atts.getType(i) ;
		
			nodedata.index = atts.getIndex(nodedata.qName) ;
			
			 
			
 
			// subnode = new DefaultMutableTreeNode(nodedata) ; // name + " = " + value) ;
			 
			subnode = new XTreeNode(nodedata) ;
			 
			node.add( subnode);
			 
		}
	}

}

class XNodeData{
	
	public int index;
	public String typestr ;
	public String qName  ;
	public String name ;
	public String value;
	public String uri ;
	
	
	public XNodeData(){
		
	}
	
}



class XTreeNode extends DefaultMutableTreeNode{
	
	int id;
	int level ;
	
	public XTreeNode(Object obj){
		super(obj);
		
	}

	
	public XTreeNode _getNextNode(){
		DefaultMutableTreeNode nnode ;
		
		nnode = super.getNextNode();
		
		return (XTreeNode)nnode;
	}
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLev() {
		return level;
	}

	public void setLev(int level) {
		this.level = level;
	}
	
	
	
}

