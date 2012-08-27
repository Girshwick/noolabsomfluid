package org.NooLab.utilities.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.strings.StringsUtil;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.jamesmurty.utils.XMLBuilder;


public abstract class XMessageAbs {

	// =================================

	// object references ..............
	
	protected XpathQuery xpathQuery = new XpathQuery();
	
	// main variables / properties ....

	protected String contentRoot = "transaction" ;
	
	
	// constants ......................

	// volatile variables .............

	
	
	// helper objects .................
	 
	protected PrintLog out = new PrintLog(2,false);
	protected DFutils fileutil = new DFutils() ;
	protected StringsUtil strgutil = new StringsUtil() ;

	String xqueryBasicCondition="";
	String lastErrorState = "";

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public XMessageAbs(){
		
		
	}
	
	
	

	// ======================================================================================================
	
	public String getLastErrorState() {
		return lastErrorState;
	}

	public void clear(){
		xpathQuery.clear() ;
		xqueryBasicCondition = "" ;
	}
	public void reset(){
		clear();
	}



	public String cleanSimple(String rawXmlStr) {

		rawXmlStr = rawXmlStr.trim();
		// cleaning...
		if (rawXmlStr.length()>10){
			int p = rawXmlStr.indexOf("<?xml version=\"1.0");
			if ((p>0) && (p<9)){
				rawXmlStr = rawXmlStr.substring(p,rawXmlStr.length());
			}
		}
		if (rawXmlStr.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")<0){
			rawXmlStr = rawXmlStr.trim() + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" ;
		}
			
		return rawXmlStr;
	}

	
	public String getContentRoot() {
		return contentRoot;
	}




	// ======================================================================================================
	
	public void setContentRoot(String contentroot) {
		this.contentRoot = contentroot;
	}

	/**
	 * 
	 * 
	 * 
	 * inserting a complete XML tagged node section into an existing tag  ;
	 * if the embedding tag does not exist, it will be created;
	 * 
	 * this works even if the initial string is empty.
	 * however, the embedding tag must be unique, the first one will always be taken... 
	 * 
	 * @param xmlstr
	 * @param embeddingTag
	 * @param insertionXml
	 * @return
	 */
	public String insertXmlStrToXmlStr( String xmlstr, String embeddingTag, String insertionXml){
		
		String xmlout , insertionStr="";
		int p1,p2;
		
		
		if (embeddingTag.length()==0){
			return xmlstr;
		}
		
		p1 =  xmlstr.indexOf(embeddingTag);

		if (p1<0){
			xmlstr = xmlstr +"  ";
			p1=xmlstr.indexOf(">")+1;
			
			xmlstr = strgutil.insert( xmlstr, "\n<"+embeddingTag+" />\n", p1);
		}
			
		xmlout = xmlstr;
			
		p1 = xmlout.indexOf("</"+embeddingTag);
		if (p1>=0){
			
			xmlout = strgutil.insert(xmlout, insertionXml, p1);
		}else{
			
			p1 = xmlout.indexOf("<"+embeddingTag);
			p1 = xmlout.indexOf("/>",p1);
			
			xmlout = strgutil.remove(xmlout, p1, p1+1);
			
			insertionStr = "\n"+insertionXml+"</"+embeddingTag+">";
			xmlout = strgutil.insert(xmlout, insertionStr, p1);
		}

		return xmlout; 
	}
	
	
	public String insertXmlToXml( String xmlstr, String embeddingTag, String insertionXml){
		
		String xmlout = xmlstr, embeddingtag="";
		
		String ruid, str ;
		int p1,p2;
		boolean ok;
		
		try{
			
			embeddingtag = embeddingTag;
			
			if (embeddingtag.startsWith("//")==false){
				embeddingtag = "//"+embeddingtag;
				embeddingtag = embeddingtag.replace("///", "//");
			}
			
			xpathQuery.ensureXmlDoc( xmlstr ) ;
			Thread.yield(); out.delay(2);
			
			ruid = GUID.randomvalue() ;
			ruid = ruid.replace("-", "");
			ruid = ruid.substring(0, 10) + ruid.substring(ruid.length()-9, ruid.length());
			

			int z=0; ok=false;
			while ((z<3) && (ok==false)){
				xmlout = xpathQuery.insertNode( embeddingtag, ruid, "a", "99");
				
				Thread.yield();
				out.delay(5); // as if SAX would need a commit....
			// without these 2 commands, the node will NOT appear in the xml till the next line !!!
			
				if ((xmlout.length()==0) || (xmlout.indexOf(ruid)<0)){
					xmlout = xmlstr;
					z++;
				}else{
					ok=true;
				}
			}
				
			if ((xmlout.length()==0) || (xmlout.indexOf(ruid)<0)){
				out.print(2, "xpathQuery.insertNode() failed, xml reset to original, and trying ordinary string processing... ");
				xmlout = xmlstr;
				
				// fallback to string processing
				xmlout = insertXmlStrToXmlStr( xmlstr, embeddingTag, insertionXml) ;
				
				if ((xmlout.length()==0) || (xmlout.indexOf(ruid)<0)){
					out.printErr(2, "xpathQuery.insertNode() failed, xml reset to original. ");
					xmlout = xmlstr;
				}
			}else{
				
				p1 = xmlout.indexOf("<"+ruid);
				p2 = xmlout.indexOf("99", p1); p2 = xmlout.indexOf("/>", p2)+2;

				if ((p1>0) && (p2>p1)){
					str = xmlout.substring(p1,p2);
					xmlout = xmlout.replace(str, insertionXml);
				}

			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		return xmlout;
	}

	public String insertRawToXml( String attrvalue, String domainLabel, String attrLabel, String xmlstr, String insertionMarker, int mode ){ 
								//  originatorID "originator","id", xstr, "</messageboard>",1){ 
		
		String str;
		
		if (attrvalue.length()>0){
			str = "   <"+domainLabel+" "+attrLabel+"=\""+attrvalue+"\" />\n\r";
			xmlstr = xmlstr.replace(insertionMarker, str+insertionMarker) ;
			 
		}
		return xmlstr ;
	}
	

	private String _prepareNumOpsAttrValueStr( String valueStr, String attrValue ) {
		
		String str;
		double attrNumVValue;
		int attrNumValue = 0 ;
		
		valueStr = valueStr.trim() ;
		
		if (attrValue.length()==0){
			attrValue = "0" ;
		}
		
		if (strgutil.isNumericX(attrValue)){
			attrNumValue = Integer.parseInt(attrValue) ;
		}
		
		if ((valueStr.startsWith("+")) || (valueStr.startsWith("-"))){
			str = valueStr.replace("+", "").replace("-", "");
			if (str.indexOf(".")>=0){
				double v = Double.parseDouble(str) ;
				if (valueStr.indexOf("+")>=0){
					attrNumVValue = attrNumValue+v;
				}else{
					attrNumVValue = attrNumValue-v;
				}
				valueStr = ""+attrNumVValue; 
			}else{
				int vi = Integer.parseInt(str) ;
				if (valueStr.indexOf("+")>=0){
					attrNumValue = attrNumValue+vi;
				}else{
					attrNumValue = attrNumValue-vi;
				}
				valueStr = ""+attrNumValue; 
			}
			
		}
		
		
		return valueStr;
	}
	
	
	public String changeAddSegment( String rawXmlMsg, String anchorXPath, String domainXPath, String attr, String valueStr ){ 
			                 //             "//transaction","/relay/hopcount","value",rxstr){
		
		boolean entryExists, hb;
		int  attrNumValue=-9999;
		
		String completeXPath, str, indent="", steppedanchor, anchorTag ,guidStr, attrValue;
		String[] leveledTags = new String[1] ;
		 
		try{
			
			domainXPath = strgutil.trimm(domainXPath, "/");
			
			completeXPath = anchorXPath + "/"+ domainXPath;
			// does it exist ?

			str = getSpecifiedInfo(rawXmlMsg, completeXPath, attr);
			if (str.length()==0){
				str = getSpecifiedInfo(rawXmlMsg, "//"+domainXPath, attr);
			}
			
			attrValue = str;
			entryExists = (str != null) && (str.length() > 0);

			xpathQuery.ensureXmlDoc(rawXmlMsg);

			guidStr = GUID.randomvalue() ;
			
			if (entryExists) {
				
				valueStr = _prepareNumOpsAttrValueStr(valueStr,attrValue ) ;
				
				xpathQuery.setAttributesValue(anchorXPath, domainXPath, attr, valueStr);
				
				rawXmlMsg = xpathQuery.getXMLString() ;
				
			} else {
				// create it from scratch
				
				leveledTags[0] = domainXPath;
				anchorTag = anchorXPath;

				if (domainXPath.indexOf("/") > 0) {
					leveledTags = domainXPath.split("/");

					for (int i = 0; i < leveledTags.length - 1; i++) {
						indent = indent + "   ";
						str = leveledTags[i];
						steppedanchor = anchorTag + "/"+ str;
						 
						hb = ( xmlNodeExists( rawXmlMsg, steppedanchor ));
						
						if (hb== false){
							
							xpathQuery.setReplacementSecret( guidStr );
							rawXmlMsg = xpathQuery.insertNode(anchorTag, str, "", "");
							
						}
						anchorTag = anchorTag + "/" + str;
					}
					// <relay/>
					// <hopcount value="2"/>
				}
				 
				domainXPath = leveledTags[leveledTags.length - 1];
				
				valueStr = _prepareNumOpsAttrValueStr(valueStr,attrValue ) ;
				
				 
				rawXmlMsg = xpathQuery.insertNode(anchorTag, domainXPath, attr, valueStr);
				rawXmlMsg = rawXmlMsg.replace("!-- "+guidStr+" --!", "\n");
			} // if - else :: entryExists does not exist ?
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return rawXmlMsg;
	}
	
	// originatorID = 
	public String getInfoFromProlog( String rawXmlMsg, String domainLabel, String attrLabel, String cleanBy){
		  // "origin","id"
		
		String resultStr="" , str="";
		int p;
		
		if (rawXmlMsg==null){
			return "";
		}
		
		rawXmlMsg = rawXmlMsg.replace("\n\r", "\n");
		rawXmlMsg = rawXmlMsg.replace("::\n<", "::<");
		rawXmlMsg = rawXmlMsg.replace(">\n::", "::<");
		rawXmlMsg = rawXmlMsg.replace("> ::", "::<");
		rawXmlMsg = rawXmlMsg.replace(":: <", "::<");
		
		p = rawXmlMsg.indexOf("::<?xml");
		
		if (p>0){
			str = rawXmlMsg.substring(0, p);
			
			if ((str.indexOf( domainLabel)>=0) && (str.indexOf(attrLabel)>=0)){
				str = str.replace("<" + domainLabel + "=", "");
				str = str.replace(">", "");
				str = str.replace("\"","");
			} else{
				str="";
			}
			// e.g. a dot
			if (cleanBy.length()>0){
				p = str.lastIndexOf(".") ;
				str = str.substring(p+1, str.length()) ;
				// e.g.  "org.NooLab.glue.net.http.rest.resources.RegistrationsResource$MessageSupply@17d5d2a"
				// e.g.  "RegistrationsResource$MessageSupply@17d5d2a"
			}
			resultStr = str;
 		}
		
		return resultStr;
	 	
	}
	
	// rawXmlMsg = xmsg.removePrologFromXml(rawXmlMsg) ;
	public String removePrologFromXml( String rawXmlMsg){

		int p;
		
		if (rawXmlMsg==null){
			return "" ;
		}
		rawXmlMsg = rawXmlMsg.replace("\n\r", "\n");
		rawXmlMsg = rawXmlMsg.replace("::\n<", "::<");
		rawXmlMsg = rawXmlMsg.replace(">\n::", "::<");
		rawXmlMsg = rawXmlMsg.replace("> ::", "::<");
		rawXmlMsg = rawXmlMsg.replace(":: <", "::<");
		
		p = rawXmlMsg.indexOf(">::");
		if (p>0){

			rawXmlMsg = rawXmlMsg.substring(p+3, rawXmlMsg.length()) ;
		}
		
		return rawXmlMsg;
	}
	 
	

	public String createRestletRoutingPrefix( String xml, String route, String actionDescriptor ){ // "/register", "new"); // new -> post will be used
		
		String xmlstr = "";
		XMLBuilder builder ;

		
		try{

			builder = getXmlBuilder( "meta" ) ; // the embracing root
				
				
				builder.e("route").a("value", route).up()
						.e("action").a("value", actionDescriptor) ;
				
				xmlstr = getXmlStr( builder, false ) ;
				
				
				xmlstr = (xmlstr + " :: "+ xml).trim() ;
				
				xmlstr = xmlstr.replace("</meta>\n", "</meta>");
				xmlstr = xmlstr.replace("</meta>\r\n", "</meta>");
				
		}catch(Exception e){
		}

			
		return xmlstr;
		
	}
	
	/**
	 * this is a natural companion for getNodeList();
	 * 
	 * @param string
	 * @param strIn
	 * @return
	 */
	public String[] getAttrValuesForNode( Node node , String nodename, String[] strIn) {
		
		int an = strIn.length;
		
		String[] stringsOut = new String[ an ] ;
		
		try{

			// -> LOOP
			for (int i=0;i<an;i++){
				if ((strIn[i]!=null) && (strIn[i].length()>0)){
					stringsOut[i] = getNodeInfo(node, nodename, strIn[i]);
				}
				
			}// i->
			
		}catch(Exception e){
			
		}
		
		return stringsOut;
	}

	
	public Vector<Object> getNodeList( String rawXmlMsg, String domainSpecs, String itemSpecs){
		
		Vector<Object> list;
		String xQuery = "";
		
		String root;
    	
    	// Object getMatchingXmlNode( String xmlpath, String nodeName, String attrName  )
    	
		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
		
    	root = this.contentRoot ;
    	
    	domainSpecs = domainSpecs.trim();
    	itemSpecs = itemSpecs.trim();
    	
    	if ((domainSpecs.endsWith("/")==false) && (itemSpecs.startsWith("/")==false) && (itemSpecs.length()>0)){
    		itemSpecs = "/"+itemSpecs;
    	}
    	domainSpecs = domainSpecs + itemSpecs;
    	
    	if (xpathQuery.domDoc==null){
    		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    	}
    	
		list = xpathQuery.getMatchingXmlNodes( domainSpecs );
		
		return list;
		 
	}
	  

	   
    /**
     * 
     * example: </br> 
     *  &lt;exclude&gt;&lt;type&gt;</br> 
          &nbsp;&nbsp;&lt;item name="image"/&gt;</br>   
          &nbsp;&nbsp;&lt;item name="video"/&gt;</br> 
          &nbsp;&nbsp;&lt;item name="sound"/&gt;</br> 
          &nbsp;&nbsp;&lt;item name="program"/&gt;</br> 
      	&lt;/type&gt;&lt;/exclude&gt;  </br></br>
       the query would be:</br>
       &nbsp;&nbsp;getItemsList(rawXmlStr, ":RootElement:"/exclude/type", "item", "name")</br> 	
	   this will return then a list of objects containing image, video, sound, program;</br></br>
	   if you want to get the list of nodes instead you may use getNodeList(rawXmlStr, "/exclude/type", "item") ; 
     *  
     * 
     * @param rawXmlMsg
     * @param domainSpecs
     * @param itemSpecs
     * @param idSpecs
     * @return
     */
    public Vector<Object> getItemsList( String rawXmlMsg, String domainSpecs, String itemSpecs, String attrSpecs){
    												 	// "properties", "property", "id" ){
	  /*
		<properties>
			<property id="0" label="keywords" values="a;b;c"/>
		</properties>
	  */
    	Vector<Object> listItems = new Vector<Object>() ;
    	String root;
    	
    	// Object getMatchingXmlNode( String xmlpath, String nodeName, String attrName  )
    	
    	root = this.contentRoot ;
    	
    	domainSpecs = domainSpecs.trim();
    	itemSpecs = itemSpecs.trim();
    	
    	if ((domainSpecs.endsWith("/")==false) && (itemSpecs.startsWith("/")==false) && (itemSpecs.length()>0)){
    		itemSpecs = "/"+itemSpecs;
    	}
    	domainSpecs = domainSpecs + itemSpecs;
    	
    	if (xpathQuery.domDoc==null){
    		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    	}
    	
    	listItems = xpathQuery.getAttributesValues( domainSpecs , itemSpecs, attrSpecs );
    	// Vector<Integer> listItems
    	
    	
    	return listItems;
    }
 
    /**
     * ( rawXmlMsg, "//properties/property" ,"id",i, "items" );
     * 
     * @param rawXmlMsg 
     * @param domainSpecs  the xml path e.g. "//properties/property" 
     * @param attrLabel    the attribute serving as criterion  e.g., "id"
     * @param attrValue    that attribute's value, e.g. "1"
     * @param listlabel    the element label that we should collect, e.g. "items" 
     * @return
     */
    public Vector<Object> selectListFromSpecifiedItem( 	String rawXmlMsg, 
    													String domainSpecs, 
    													String attrLabel, 
    													String attrValue, 
    													String listlabel){
    	/*
			  <properties id="32092fea-8abf-4e3f-a954-b33ff3f76d09" type="1">
      				<property id="1" label="keywords" >
      					<items type="incl" values="a;b;c;d"/>
      					<items type="excl" values="x"/>
      				</property>    	
    	*/
    	
    	Vector<Object> nodelist = new Vector<Object>() ;

    	Vector<Object> containedNodes;
    	
		String infoStr="",rootpath="",itemSpecs="";
		String  root,secondpart="",str ;
		Node node;
		int p;
		
		try{
			// str = domainSpecs;
			
			if ((domainSpecs.length()==0) || (attrLabel.length()==0)){
				return null;
			}
			xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
			
			if (domainSpecs.startsWith("//")){
				domainSpecs = domainSpecs.replace("//", "");
				secondpart="";
				
				if (domainSpecs.contentEquals("t")==false){
					p=0;
				}
				p= domainSpecs.indexOf("/");
				if (p>0){
					secondpart = domainSpecs.substring(p+1, domainSpecs.length()) ;
					domainSpecs = domainSpecs.substring(0, p) ;
				}else{
					secondpart = domainSpecs ;
				}
				
				rootpath= "//"+domainSpecs;
				domainSpecs = secondpart ;
				 
			}else{
				if (contentRoot.length()==0){
					rootpath= "//transaction";
				}else{
					rootpath = "//"+contentRoot;
				}
			}
			if (domainSpecs.startsWith("/")==false){
				domainSpecs = "/"+domainSpecs;
			}
			
			if ( rootpath.contains( "/"+domainSpecs)==false){
				root = rootpath +domainSpecs;
			}else{
				root = rootpath ;
			}
			
			
			
			// infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );

			// get all tags described by domainSpecs
			Vector<Object> listItemsObj;

			// 
			listItemsObj = getItemsList(rawXmlMsg,root, "", "") ;
			 
			for (int i=0;i<listItemsObj.size();i++){
				
				node = (Node) listItemsObj.get(i) ;
				
				str = getSpecifiedItemInfo(node, attrLabel);
				
				if (str.contentEquals(attrValue)){
					
					
					containedNodes = getSubtagsFromNode( node, listlabel, "" ,"");
					// further conditions here ?
					
					// 
					nodelist = containedNodes;
					break;
				}
				
			}// i->
			
			
		if ((infoStr.length()==0) || (infoStr.contentEquals("-1"))){
			p=0;
		}
			
		}catch(Exception ex){
			out.print(2, "ERROR on message \n\r"+rawXmlMsg) ;
			ex.printStackTrace();
		}
		
    	return nodelist;
    }
    /**
     * 
     * Get all elements where pet equals cat<br/>
     * "//*[@pet='cat']";
     *  
     *  
     */
    public Object selectSpecifiedItem( String rawXmlMsg, String xpath, String domainSpecs, String attrLabel, String attrValue){
    	Object xmlNodeObj = null;
    	
    	String xquery , startmarker = "//";
    	// xquery = "//*[@pet='cat']";
    	
    	
    	try{
    		domainSpecs = domainSpecs.trim();
    		
    		if (domainSpecs.startsWith("//")){
    			startmarker="" ;
    		}
        	if (domainSpecs.length()==0){
        		domainSpecs="*";
        	}
        	
        	xquery = startmarker + domainSpecs+"[@"+attrLabel+"='"+attrValue+"']";
        		 //   //property[@id='1'] 
        	
        	if ((domainSpecs.length()==0) || (attrLabel.length()==0)){
    			return xmlNodeObj;
    		}
        	
        	
    		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    		
        	xmlNodeObj = xpathQuery.getMatchingXmlNode( xquery ) ;
        	
    	}catch(Exception e){
    		
    	}
    	
    	return xmlNodeObj;
    }
    
    public Object selectSpecifiedItem( String rawXmlMsg, String domainSpecs, String attrSpecs, String attrValue){
    	Object xmlNodeObj = null;
    	
    	String infoStr="",rootpath="";
    	
		String  root,secondpart="" ;
		int p;
		
		try{
			// str = domainSpecs;
			
			if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
				return xmlNodeObj;
			}
			xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
			
			if (domainSpecs.startsWith("//")){
				domainSpecs = domainSpecs.replace("//", "");
				secondpart="";
			
				p= domainSpecs.indexOf("/");
				if (p>0){
					secondpart = domainSpecs.substring(p+1, domainSpecs.length()) ;
					domainSpecs = domainSpecs.substring(0, p) ;
				}else{
					secondpart = domainSpecs ;
				}
				
				rootpath= "//"+domainSpecs;
				domainSpecs = secondpart ;
				 
			}else{
				rootpath = "//"+contentRoot;
			}

			if (domainSpecs.startsWith("/")==false){
				domainSpecs = "/"+domainSpecs;
			}
			
			if ( rootpath.contains( "/"+domainSpecs)==false){
				root = rootpath +domainSpecs;
			}else{
				root = rootpath ;
			}
			
			// Get all e elements directly under element id 3
			// xpath = "id('two')/e";   
			// "//*[@pet='cat']";

			// infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );
			xmlNodeObj = xpathQuery.getMatchingXmlNode( root , domainSpecs, attrSpecs, attrValue );
			// Get all elements where pet equals cat
			// String xpath = "//*[@pet='cat']"; 
			
			 
				 
			 
			
		}catch(Exception ex){
				out.print(2, "ERROR on message \n\r"+rawXmlMsg) ;
				ex.printStackTrace();
		}
		
		
    	return xmlNodeObj;
    }
	
    
    public boolean xmlNodeExists( String rawXmlMsg, String domainSpecs ){
    	boolean rB=false;
    	Node node;
    	String str="" ;
    	
    	try{
    	
    		if ((domainSpecs==null) || (domainSpecs.length()==0)){
				return false;
			}
    		
			xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    		
			node = (Node) xpathQuery.getXmlNodeByName( domainSpecs );
			
			if(node!=null ){
				str = node.getNodeName() ;
			}
		
			rB = (node!=null) ;
			
    	}catch(Exception e){
    		
    	}
    	
    	return rB;
    }
    
    
	public Vector<Object> getSubtagsFromNode( Node node, String elementLabel, String attrSpecs , String attrValue){
		
		Vector<Object> nodelist = new Vector<Object>() ;
		
		Vector<Object> completelist;
		
		completelist = xpathQuery.getNodesByName( node, elementLabel) ;
		
		if (attrSpecs.length()>0){
			// further selection
			
			// ....
			
		}else{
			nodelist = completelist;
		}
		
		return nodelist;
	}
 
	public Vector<Object> getSubtagFromNode( Node node, String elementLabel, String attrSpecs , String attrValue){
		
		Vector<Object> nodelist = new Vector<Object>() ;
		
		Vector<Object> completelist;
		
		completelist = xpathQuery.getNodesByName( node, elementLabel) ;
		
		if (attrSpecs.length()>0){
			// further selection
			
			// ....
			
		}else{
			nodelist = completelist;
		}
		
		return nodelist;
	}
	
	public void setConditionalXPath(String xpath) {
		 
		 xpathQuery.setQueryPrefix(xpath) ;
		xqueryBasicCondition = xpath ;
		
	}
	
	public String getConditionalXPath() {
		
		// TODO Auto-generated method stub
		String xstr = xpathQuery.getQueryPrefix() ;
		String str = xqueryBasicCondition ;
		return xstr;
	}
	public String getTextDataFromNode( String rawXmlMsg, Object xmlNodeObj, String xpath) {

		String resultInfo="";
		Node node;
		
		if (xmlNodeObj==null){
			return resultInfo;
		}

		node = (Node)xmlNodeObj;
		// resultInfo = xpathQuery.readNode( xmlNodeObj, attrSpecs ) ;
		
		// get full path for node
		// resultInfo = node.getTextContent();
		
		String npath = node.getBaseURI();
		
		
		// npath = node.getLocalName() ;
		if (npath!=null){
			int z = npath.length();
		}
		
		resultInfo = getTextData( rawXmlMsg, xpath);
		
		return resultInfo;
	}

	public String getNodeInfo(Object xmlNodeObj, String domainSpecs, String attrSpecs){
		
		String resultInfo="";
		String  rootpath="";
    	
		String  root,secondpart="" ;
		int p;
		
		if (xmlNodeObj==null){
			return resultInfo;
		}
		 
		 
		try{
			 
			 
			resultInfo = xpathQuery.readNode( xmlNodeObj, attrSpecs ) ;
			
		if ((resultInfo.length()==0) || (resultInfo.contentEquals("-1"))){
			p=0;
		}
			
		}catch(Exception ex){
			out.printErr(2, "ERROR while accessing node \n\r") ;
			ex.printStackTrace();
		}
		
		
		return resultInfo;
	}
	
	public String getSpecifiedItemInfo( Object xmlNodeObj, String attrLabel){
		String resultStr="";
		
		if (xmlNodeObj==null){
			return resultStr;
		}
		
		resultStr = xpathQuery.readNode( xmlNodeObj, attrLabel );
		
		return resultStr;
	}

 
	/**
	 * <b>not yet implemented !!</b>
	 * use instead:  setBasicConditionLocation(), then getItemsList
	 * 
	 * @param rawXmlMsg
	 * @param domainSpecs
	 * @param condAttrSpecs
	 * @param condition
	 * @param dataTagSpecs
	 * @return
	 * @throws Exception 
	 */
	public Vector<Object> getSpecifiedConditionalNode(	String rawXmlMsg, 
															String domainSpecs, 
															String condAttrSpecs,
															String condition, 
															String dataTagSpecs ) throws Exception {
		
		String subtag1 ="", previousQueryPrefix, previousXqueryCondition = "" ;
		
		clearBasicConditionLocation();
		previousQueryPrefix = xpathQuery.getQueryPrefix();
		previousXqueryCondition = xqueryBasicCondition ;
		xqueryBasicCondition = "" ;
		
		subtag1 = dataTagSpecs;
		int p= subtag1.indexOf("/",3) ;
		if (p>0){
			subtag1 = dataTagSpecs.substring(0,p) ;
			
			dataTagSpecs = dataTagSpecs.substring(p+1,dataTagSpecs.length());
			dataTagSpecs = strgutil.trimm(dataTagSpecs, "/");
			
			if (subtag1.startsWith("//")){
				subtag1 = subtag1.substring(2, subtag1.length()) ;
			}
			if (subtag1.startsWith("/")){
				subtag1 = subtag1.substring(1, subtag1.length()) ;
			}
		}
		
		int r = setBasicConditionLocation(rawXmlMsg, domainSpecs, condAttrSpecs, condition, subtag1);
		
		if (r!=0){
			throw(new Exception("Requested node in xml not found (r="+r+")."));
		}
		Vector<Object> nodelist = getItemsList(rawXmlMsg, "//"+dataTagSpecs, "", "");
		
		xpathQuery.setQueryPrefix(previousQueryPrefix) ;
		xqueryBasicCondition = previousXqueryCondition;
		
		return nodelist;
	}
	
	
	public String getTextDataFromConditionalSection( String rawXmlMsg, 
													String domainSpecs, 
													String condAttrSpecs,
													String condition, 
													String dataTagSpecs){

		String resultInfo = "";
		Node pkgNode;
		
		// similar to 
		// pkgNode = (Node)selectSpecifiedItem(rawXmlMsg, "//sompackages/packages", "package", "name", condition);
		
		
		Object xmlNodeObj = null;
    	
    	String xquery , startmarker = "//";
    	// xquery = "//*[@pet='cat']";
    	
    	
    	try{
    		domainSpecs = domainSpecs.trim();
    		
    		if (domainSpecs.startsWith("//")){
    			startmarker="" ;
    		}
        	if (domainSpecs.length()==0){
        		domainSpecs="*";
        	}
    		if ((domainSpecs.startsWith("//")==false) && (domainSpecs.startsWith("/"))){
    			domainSpecs = domainSpecs.substring(1,domainSpecs.length()) ;
    		}
        	
    		
        	xquery = startmarker + domainSpecs+"[@"+condAttrSpecs+"='"+condition+"']/"+dataTagSpecs;
        		 //   //property[@id='1'] 

    		if (xquery.startsWith("///")){
    			xquery = xquery.substring(1,xquery.length()) ;
    		}

    		
        	xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    		 
    			lastErrorState = "";
    			xmlNodeObj = xpathQuery.getMatchingXmlNode( xquery ) ;

    			// resultInfo = xpathQuery.readNode( xmlNodeObj,dataAttrSpecs ) ;
    		
    			resultInfo = ((Node)xmlNodeObj).getTextContent();
    			
    			
    	}catch(Exception e){
    		
    	}
		return resultInfo;
		
	}
	
	/**
	 * this creates queries like  
	 * 		"locations/vendor/location[@id = 'store102']//street"  
	 * where the "street" is a tag inside the conditional tag = selected on condition fulfilled by an attribute</br></br>
	 * 
	 * example:</br>
	 * there is a tag &lt;table name= [str]&gt; for which we have identified the value of the "name" attribute before.
	 * this table defines a section, which contains a nested &lt;create&gt;&lt;drop active="1" /&gt; </br></br>
	 * The call is then like this.... </br>
	 * (rawXmlMsg, "/table", "name", str, "/create/drop", "active") ;
	 * 
	 * 
	 */
	public String getSpecifiedConditionalInfo(	String rawXmlMsg, 
												String domainSpecs, 
												String condAttrSpecs,
												String condition, 
												String dataTagSpecs,
												String dataAttrSpecs ) {
		String resultInfo = "";
		Node pkgNode;
		
		// similar to 
		// pkgNode = (Node)selectSpecifiedItem(rawXmlMsg, "//sompackages/packages", "package", "name", condition);
		
		
		Object xmlNodeObj = null;
    	
    	String xquery , startmarker = "//";
    	// xquery = "//*[@pet='cat']";
    	
    	
    	try{
    		domainSpecs = domainSpecs.trim();
    		
    		if (domainSpecs.startsWith("//")){
    			startmarker="" ;
    		}
        	if (domainSpecs.length()==0){
        		domainSpecs="*";
        	}
    		if ((domainSpecs.startsWith("//")==false) && (domainSpecs.startsWith("/"))){
    			domainSpecs = domainSpecs.substring(1,domainSpecs.length()) ;
    		}
        	
    		
        	xquery = startmarker + domainSpecs+"[@"+condAttrSpecs+"='"+condition+"']/"+dataTagSpecs;
        		 //   //property[@id='1'] 

    		if (xquery.startsWith("///")){
    			xquery = xquery.substring(1,xquery.length()) ;
    		}

    		
        	xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    		 
    			lastErrorState = "";
    			xmlNodeObj = xpathQuery.getMatchingXmlNode( xquery ) ;
        		resultInfo = xpathQuery.readNode( xmlNodeObj,dataAttrSpecs ) ;
    		
    		
    	}catch(Exception e){
    		
    	}
		return resultInfo;
	}
	
	public void clearBasicConditionLocation(){
		xpathQuery.setQueryPrefix("") ;
		xqueryBasicCondition = "";
	}
	 
	/**
	 * any further query will be conditional to this location as a base !!!</br></br>
	 * 
	 * Example: </br>
	 * let us assume that there is a tag  &lt;table name="abc"&gt;.</br>
	 * within this tag we find a section defined by the tag    &lt;astor&gt; ... &lt;/astor&gt;. </br></br> 
	 * The query would look like this (tnStr refers to "abc"): </br></br>
	 * &nbsp;&nbsp;&nbsp; setBasicConditionLocation( xmlstr,  "//table", "name", tnStr , "/astor");</br></br>
	 *   
	 * Next, let us assume, that inside &lt;astor&gt; there is a section &lt;core&gt; which itself contains a list of
	 * items</br>
	 * &nbsp;&nbsp;&lt;item id="1" ... /&gt; </br>
	 * &nbsp;&nbsp;&lt;item id="2" ... /&gt; </br>
	 * &nbsp;&nbsp;&lt;item id="3" ... /&gt; </br></br> 
	 * 
	 * In this case we would be interested in the nodes described by the "item" tag. </br>
	 * Given the conditional anchor as defined above we could access them by </br></br>
	 * &nbsp;&nbsp;&nbsp;xMsg.getItemsList(xmlstr, "//core", "item", "");</br></br>
	 *   
	 * As a result, we can access any kind of list of tags relative to (=inside of) </br>
	 * a tag that contains a particular value for one of its attributes.  
	 * 
	 * @param rawXmlMsg   the xml string
	 * @param domainSpecs   the name of the tag containing the attribute that shall serve as condition
	 * @param condAttrSpecs   the attribute that shall serve as condition
	 * @param condition       the value of the attribute = the value of the condition
	 * @param dataTagSpecs    the sub tag inside the conditionally selected tag domainSpecs
	 * @return
	 */
	public int setBasicConditionLocation( String rawXmlMsg, 
										     String domainSpecs, 
										     String condAttrSpecs,
										     String condition,
										     String dataTagSpecs
										      ) {
		
		String resultInfo = "";
		Node pkgNode;
		int result=-1;
		 	
		
		Object xmlNodeObj = null;
    	
    	String xquery , startmarker = "//";
    	// xquery = "//*[@pet='cat']";
    	xqueryBasicCondition = "" ;
    	
    	try{
    		domainSpecs = domainSpecs.trim();
    		
    		if (domainSpecs.startsWith("//")){
    			startmarker="" ;
    		}
        	if (domainSpecs.length()==0){
        		domainSpecs="*";
        	}
        	
        	xquery = startmarker + domainSpecs+"[@"+condAttrSpecs+"='"+condition+"']";
        	if (dataTagSpecs.length()>0){
        		xquery = xquery + "/" + dataTagSpecs;
        	}
        		 //   //property[@id='1'] 
        	
        	
    		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
    		
        	xmlNodeObj = xpathQuery.getMatchingXmlNode( xquery ) ;
        	result = -2;
        	
        	if (xmlNodeObj!=null){
        		
        		String nodename = ((Node)xmlNodeObj).getNodeName() ;
        		
        		if ((nodename!=null) && (nodename.length()>0)){

            		xpathQuery.setQueryPrefix(xquery) ;
            		xqueryBasicCondition = xquery;
            		
            		if (dataTagSpecs.contains(nodename)){
            			result =0;
            		}else{
            			result =-3;
            		}

        		}
        	}else{
        		result=-7;
        	}
        	
        	
        	
    	}catch(Exception e){
    		
    	}
		
		return result;
	}
	
	
	/**
	 * 
	 * example: from several "property", we want that with id=1, and from that
	 *          the value of "label"
	 *          &lt;property id="1" label="keywords" /&gt;</br></br>
	 * (do no include the xml root, as for any method of the  "getSpecifiedInfo()" group!)
	 * 
	 * 
	 * @param rawXmlMsg
	 * @param domainSpecs
	 * @param attrSpecs
	 * @param attrValue
	 * @param requAttr
	 * @return
	 */
	public String getSpecifiedInfo( String rawXmlMsg, String domainSpecs, String attrSpecs, String attrValue, String requAttr) { // "type") ;
	
		String infoStr="",rootpath="",itemSpecs="";
		String  root,secondpart="",str ;
		Node node;
		int p;
		
		try{
			// str = domainSpecs;
			
			if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
				return infoStr;
			}
			xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
			
			if (domainSpecs.startsWith("//")){
				domainSpecs = domainSpecs.replace("//", "");
				secondpart="";
				
				if (domainSpecs.contentEquals("t")==false){
					p=0;
				}
				p= domainSpecs.indexOf("/");
				if (p>0){
					secondpart = domainSpecs.substring(p+1, domainSpecs.length()) ;
					domainSpecs = domainSpecs.substring(0, p) ;
				}else{
					secondpart = domainSpecs ;
				}
				
				rootpath= "//"+domainSpecs;
				domainSpecs = secondpart ;
				 
			}else{
				if (contentRoot.length()==0){
					rootpath= "//transaction";
				}else{
					rootpath = "//"+contentRoot;
				}
			}
			if (domainSpecs.startsWith("/")==false){
				domainSpecs = "/"+domainSpecs;
			}
			
			if ( rootpath.contains( "/"+domainSpecs)==false){
				root = rootpath +domainSpecs;
			}else{
				root = rootpath ;
			}
			
			
			
			// infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );

			// get all tags described by domainSpecs
			Vector<Object> listItemsObj;

			// 
			listItemsObj = getItemsList(rawXmlMsg,root, "", "") ;
			 
			for (int i=0;i<listItemsObj.size();i++){
				
				node = (Node) listItemsObj.get(i) ;
				
				str = getSpecifiedItemInfo(node, attrSpecs);
				
				if (str.contentEquals(attrValue)){
					infoStr = getSpecifiedItemInfo(node, requAttr);
					break;
				}
				
			}// i->
			
			
		if ((infoStr.length()==0) || (infoStr.contentEquals("-1"))){
			p=0;
		}
			
		}catch(Exception ex){
			out.print(2, "ERROR on message \n\r"+rawXmlMsg) ;
			ex.printStackTrace();
		}
		
    	return infoStr;
	}
	 
	
    public String getSpecifiedInfo( String rawXmlMsg, String domainSpecs,String attrSpecs) { // "type") ;
    	String infoStr="",rootpath="";
    	
		String  root,secondpart="", zpath= domainSpecs;
		int p;
		
		try{
			// str = domainSpecs;
			
			if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
				return infoStr;
			}
			xpathQuery.ensureXmlDoc( rawXmlMsg ) ; // this does not delete the domdoc !
			
			if (domainSpecs.startsWith("//")){
				domainSpecs = domainSpecs.replace("//", "");
				secondpart="";
				
				if (domainSpecs.contentEquals("t")==false){
					p=0;
				}
				p= domainSpecs.lastIndexOf("/");
				if (p>0){
					secondpart = domainSpecs.substring(p+1, domainSpecs.length()) ;
					domainSpecs = domainSpecs.substring(0, p) ;
				}else{
					secondpart = domainSpecs ;
				}
				
				rootpath= "//"+domainSpecs;
				domainSpecs = secondpart ;
				 
			}else{
				if (contentRoot.length()==0){
					rootpath= "//transaction";
				}else{
					rootpath = "//"+contentRoot;
				}
			}
			
			 
			
			
			if (domainSpecs.startsWith("/")==false){
				domainSpecs = "/"+domainSpecs;
			}
			
			if ( rootpath.contains( "/"+domainSpecs)==false){
				root = rootpath +domainSpecs;
			}else{
				root = rootpath ;
			}
			if (xqueryBasicCondition.length()>0){
				String freeaccessMark ="";
				if (domainSpecs.indexOf("//")==0){
					freeaccessMark ="//";
				}
				domainSpecs = strgutil.trimm(domainSpecs, "//");
				domainSpecs = strgutil.trimm(domainSpecs, "/");
				String xpathAddon = ""; 
				p = domainSpecs.lastIndexOf("/");
				if (p>1){
					xpathAddon =  domainSpecs.substring(0,p);
					xpathAddon = strgutil.trimm(xpathAddon, "/");
					zpath = domainSpecs.substring(p+1,domainSpecs.length());
					xpathAddon = "/"+xpathAddon;
				}
				
				String xRoot = xqueryBasicCondition + xpathAddon + "/"+zpath;
				infoStr = xpathQuery.getConditionalAttributesValue( xRoot , zpath, attrSpecs );
			}else{
				domainSpecs = strgutil.trimm(domainSpecs, "//");
				domainSpecs = strgutil.trimm(domainSpecs, "/");
				p = domainSpecs.lastIndexOf("/") ;
				if (p>1){
					String xpathAddon =  domainSpecs.substring(0,p);
					xpathAddon = strgutil.trimm(xpathAddon, "/");
					zpath = domainSpecs.substring(p+1,domainSpecs.length());
					xpathAddon = "/"+xpathAddon;
					if (root.indexOf(xpathAddon)<0){
						root = root + xpathAddon;
					}
					domainSpecs = zpath;
				}
				infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );
			}
			
		if ((infoStr.length()==0) || (infoStr.contentEquals("-1"))){
			p=0;
		}
			
		}catch(Exception ex){
			out.print(2, "ERROR on message \n\r"+rawXmlMsg) ;
			ex.printStackTrace();
		}
		
    	return infoStr;
    }
   
	// public <T> T getSpecifiedValue( String rawXmlMsg, String domainSpecs,String attrSpecs, Class<T> typeClass){
    public long getSpecifiedValue( String rawXmlMsg, String domainSpecs,String attrSpecs){
    	
    	// T typedObject = null;
    	// Class<T> typeClass
    	int p;
    	String infoStr="",rootpath="", root,secondpart;
    	long value=-1;
		 
		
		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
		 
		if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
			return value;
		}
		xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
		
		if (domainSpecs.startsWith("//")){
			domainSpecs = domainSpecs.replace("//", "");
			secondpart="";
			
			if (domainSpecs.contentEquals("t")==false){
				p=0;
			}
			p= domainSpecs.indexOf("/");
			if (p>0){
				secondpart = domainSpecs.substring(p+1, domainSpecs.length()) ;
				domainSpecs = domainSpecs.substring(0, p) ;
			}else{
				secondpart = domainSpecs ;
			}
			
			rootpath= "//"+domainSpecs;
			domainSpecs = secondpart ;
			 
		}else{
			if (contentRoot.length()==0){
				rootpath= "//transaction";
			}else{
				rootpath = "//"+contentRoot;
			}
		}
		if (domainSpecs.startsWith("/")==false){
			domainSpecs = "/"+domainSpecs;
		}
		
		if ( (rootpath+"$").contains( "/"+domainSpecs+"$")==false){
			root = rootpath +domainSpecs;
		}else{
			root = rootpath ;
		}
		
		p = domainSpecs.lastIndexOf("/");
		if (( p>1) && ( p<domainSpecs.length() )){
			domainSpecs = domainSpecs.substring(p, domainSpecs.length()).trim() ;
		}
		// root: //transaction/confirmation/state   domainSpecs: /confirmation/state but it must be /state !
		infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );
		 
    			  if (infoStr.length()>0){
    				 /* try {
    					// not this way ...  = typedObject.getDeclaredConstructor().newInstance();
    					typedObject = Class.getDeclaredConstructor(typeClass).newInstance("HERESMYARG");
    					// yet this does not add anything to explicit constructions based on the (switched) class - literal 
						value = getLong(infoStr,-1);
					} catch (Exception e) { }
					*/
    				  
    				value = Long.parseLong(infoStr) ;
    			  } 
    			  // typedObject = (T)((Object)value); 
    	
    	return value;
    }

    
    public String getTextData( String rawXmlMsg, String xpath){ 
	
    	/*
	 // Get the text node
Text text1 = (Text)element.getFirstChild();
String string = text1.getData();
	 */
    	String textData = "";
    	NodeList nodes =null ;
    	Node node = null;

    	
    	try{
    		

        	xpathQuery.ensureXmlDoc( rawXmlMsg ) ;
        	
        	Object nodeObj = xpathQuery.getMatchingXmlNode(xpath) ;
        	
        	if (nodeObj!=null){
        		String cn = nodeObj.getClass().getSimpleName() ;  
        		// "DTMNodeList"
        	
        		if (cn.toLowerCase().contains("nodelist")){
        			nodes = (NodeList)nodeObj ;
        			if (nodes.getLength()==0){
        				nodes=null;
        			}
        		}else{
        			node = (Node)nodeObj ;	
        		}
        	
        	
        		Text xnText = (Text)(node.getFirstChild() );
        		textData = xnText.getData();
        	}	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return textData;
	}
	
	public boolean rawIndicatorsCheck( String rawXmlMsg, String[] rawIndicators){
		boolean rB=false;
		int isum=0;
		
		int[] p = new int[rawIndicators.length] ;
		
		for (int i=0;i<rawIndicators.length;i++){
			p[i] = rawXmlMsg.indexOf( rawIndicators[i]);
			
			if (p[i]>=0){
				isum = isum+ 1;
			}
		}
		
		rB = (isum == p.length) ;
		
		return rB;
	}
	
	 
	public boolean tagExists(String rawXmlMsg, String xpath) {
		
		boolean rB=false;
		
		xpathQuery.ensureXmlDoc(  rawXmlMsg ) ;
    	
    	Object nodeObj = xpathQuery.getMatchingXmlNode(xpath) ;
		
		rB = nodeObj != null; 
		
		return rB;
	}
	
	public String listContainsAny( String strList1, String strList2 ){
		
		String matchingStr="";
		String[] strings1, strings2;
		
		strings1 = strList1.split(";");
		strings2 = strList2.split(";");
		
		for (int i=0;i<strings1.length;i++){			
			for (int k=0;k<strings2.length;k++){
				if (strings1[i].contentEquals(strings2[k])){
					matchingStr = strings1[i];
					return matchingStr;
				}
			}	
		}
		
		return matchingStr;
	}
	
	
    // simple check, not considering W3C pre-amble, xml comments, etc...
    public boolean isXML( String str ){
    	boolean rB=false;
    	int p;
    	
    	if (str==null){
    		return rB;
    	}
    	
    	str = str.trim();
    	p =str.indexOf("<?xml version=");
    	
    	rB = (p==0);
    	
    	return rB;
    }
    
    /**
     * serializes a 1-dimensional ArrayList into a xml compatible string
     * 
     * @param list
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String serializeMonoList( ArrayList list){
    	String xliststr="";
    	Object obj;
    	
    	
    	if ((list!=null) && (list.size()>0)){
    		
    		obj = list.get(0) ;
    		
    		String cn = obj.getClass().getSimpleName().toLowerCase();
    		if (cn.startsWith("double")){
    			return serializeListDouble(list) ;
    		}
    		if (cn.startsWith("float")){
    			return serializeListFloat(list) ;
    		}
    		if (cn.startsWith("int")){
    			return serializeListInt(list) ;
    		}
    		if (cn.startsWith("str")){
    			return serializeListStr(list) ;
    		}
    	} // !null? >0 ?
    	
    	
    	return xliststr;
    }

    private String serializeListStr(ArrayList<String> list) {
		String str,xstr=""; 
		
    	for (int i=0;i<list.size();i++){
    		str = list.get(i).trim();

    		if (str.length()>0){
    			xstr = xstr + str;
        		if (i<list.size()-1){
        			xstr = xstr + ";" ;
        		}
    		}
		}
    	return xstr;
	}




	private String serializeListInt(ArrayList<Integer> list) {
		String xstr=""; 
		
    	for (int i=0;i<list.size();i++){
    		
    		if (list.get(i) != null){
    			xstr = xstr + ""+list.get(i);
        		if (i<list.size()-1){
        			xstr = xstr + ";" ;
        		}
    		}
		}
    	return xstr;
	}




	private String serializeListFloat(ArrayList<Float> list) {
		String xstr=""; 
		
    	for (int i=0;i<list.size();i++){
    		
    		if (list.get(i) != null){
    			xstr = xstr + ""+ String.format("%.7f", list.get(i));
        		if (i<list.size()-1){
        			xstr = xstr + ";" ;
        		}
    		}
		}
    	return xstr;
	}




	private String serializeListDouble(ArrayList<Double> list) {
		String xstr=""; 
		
    	for (int i=0;i<list.size();i++){
    		
    		if (list.get(i) != null){
    			xstr = xstr + ""+ String.format("%.7f", list.get(i));
        		if (i<list.size()-1){
        			xstr = xstr + ";" ;
        		}
    		}
		}
    	return xstr;
	}




	public String serialize( String[] strarray){
    	String xliststr="";
    	
    	
    	
    	return xliststr;
    }
    
    public String serialize( int[] intarray){
    	String xliststr="";
    	
    	
    	
    	return xliststr;
    }
    
    public String serialize( double[] numarray){
    	String xstr="";
    	 
    	if (numarray  != null){
    		for (int i=0;i<numarray.length;i++){
    		 
    			xstr = xstr + ""+ String.format("%.7f", numarray[i] );
        		if (i<numarray.length-1){
        			xstr = xstr + ";" ;
        		}
    		 
    		}
    	} 
    	
    	return xstr;
    }
    
    /**
     * 
     * creates a string which looks like
     * 
     *       <items index="0" "">;
     * 
     * @param num2Darray
     * @return
     */
    public String serialize( double[][] num2Darray){
    	String rowstr,xliststr="";
    	
    	XMLBuilder aBuilder ;
		int n=0,m=0;
		 
		
		aBuilder = getXmlBuilder( "array" );
		
    	if ((num2Darray!=null) && (num2Darray.length>0)){
    		 
    		aBuilder = aBuilder.a("count", ""+num2Darray.length) ;
    		
    		
    		for (int i=0;i<num2Darray.length;i++){

							n = num2Darray[i].length;
							rowstr = serialize( num2Darray[i] );

    			aBuilder = aBuilder.e("row")
    									.a("count", ""+n)
    									.a("items", rowstr).up();
    		} // i->
    		
    		aBuilder = aBuilder.up();
    	}
    	
    	aBuilder = aBuilder.up();
    	
    	xliststr = getXmlStr(aBuilder, false);
    	
    	return xliststr;
    }

     
	
    public String numerize( float value, int digits){
		String numstr="";
	
		numstr = String.format("%."+digits+"f", value);
		
		numstr = StringsUtil.trimTrailingZeroes(numstr);
		
		return numstr;
	}
    
    public String numerize( double value, int digits){
		String numstr="";
	
		numstr = String.format("%."+digits+"f", value);
		
		numstr = StringsUtil.trimTrailingZeroes(numstr);
		
		return numstr;
	}
	
    public String booleanize( boolean flag){
		String bstr="";
		
		if (flag){ bstr="true";}else{bstr="false";}
		
		return bstr;
	}

    // ....................................................
    
    public Object getArray1D(String str) {
    	Object obj = null;
    	
    	
    	return obj;
    }
 
    public Object getArray2D(String str) {
    	Object obj = null;
    	
    	
    	return obj;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList getListFromXmlStr( String str, Class clz) {
    	ArrayList gList=null;
    	String cname = clz.getSimpleName() ;
    	String listr;
    	int vi;
    	double v;
    	String[] stritems ;

    	str = str.trim() ;
    	if (str.length()==0){
    		stritems = new String[0];
    	}else{
    		stritems = str.split(";");
    	}
    	
		if (cname.toLowerCase().startsWith("doub")){
			gList = new ArrayList<Double>();
			if (stritems.length>0){
				for (int i=0;i<stritems.length;i++){
					listr = stritems[i];
					if (strgutil.isNumericX(listr)){
						v = Double.parseDouble(listr);
						gList.add(((Double)v));
					}
				}
			}
		}
		if (cname.toLowerCase().startsWith("int")){
			gList = new ArrayList<Integer>();
			if (stritems.length>0){
				for (int i=0;i<stritems.length;i++){
					listr = stritems[i];
					if (strgutil.isNumericX(listr)){
						vi = Integer.parseInt(listr);
						gList.add(((Integer)vi));
					}
				}
			}
		}
		if (cname.toLowerCase().startsWith("str")){
			gList = new ArrayList<String>();
			if (stritems.length>0){
				gList = new ArrayList<String>(Arrays.asList(stritems));
			}
		}
 
		return gList;
	}

    
    
    public boolean getBool(String str, boolean defaultVal) {
		boolean rB=defaultVal;

		str = str.toLowerCase();
		
		if ((str.length()==0) || (str.startsWith("n")) || (str.startsWith("0")) || (str.startsWith("f"))){
			rB=false;
		}
		if ((str.length()>0) && (str.length()<=4) && 
				( (str.startsWith("j")) || (str.startsWith("y")) || (str.startsWith("d")) || (str.startsWith("w")) ||
				  (str.startsWith("s")) || (str.startsWith("o")) || (str.startsWith("1")) || (str.startsWith("t"))) ){
			rB=true;
		}
		
		return rB;
	}

    public double getNum(String str, double defaultVal) {
		double num=defaultVal;
		
		if (strgutil.isNumericX(str)){
			num = Double.parseDouble(str);
		}
		return num;
	}

    public int getInt(String str, int defaultVal) {
		int vi=defaultVal;
		
		if (strgutil.isNumericX(str)){
			vi = Integer.parseInt(str);
		}
		
		return vi;
	}
	
    

	/**
	 * this is generically/type-free usable for lists on primitives, as it 
	 * detects the type of the collected primitive
	 *
	 * @param list
	 */
	public String digestList(ArrayList<?> list) {

		return serializeMonoList(list) ;
		/*
		String xstr = "";
		
		if ((list==null) || (list.size()==0)){
			return xstr;
		}
		
		Object obj = list.get(0) ;
		
		String cn = obj.getClass().getSimpleName() ;
		
		
		if ((cn.toLowerCase().startsWith("int")) && (cn.contains("[]"))){
			
			return xstr;
		}
		if ((cn.toLowerCase().startsWith("doub")) && (cn.contains("[]"))){
			
			return xstr;
		}
		if ((cn.toLowerCase().startsWith("str")) && (cn.contains("[]"))){

			return xstr;
		}
		
		if (cn.toLowerCase().startsWith("int")){
			xstr = digestIntList(   (ArrayList<Integer>) list);
			return xstr;
		}
		if (cn.toLowerCase().startsWith("doub")){
			xstr = digestNumList( (ArrayList<Double>) list);
			return xstr;
		}
		if (cn.toLowerCase().startsWith("str")){
			xstr = digestStringList((ArrayList<String>) list);
			return xstr;
		}
		
		return xstr;
		*/
		
	}
	
	public String digestStringList( ArrayList<String> strlist){
		String str;
		String xstr = "";
		
		if ((strlist==null) || (strlist.size()==0)){
			return xstr;
		} 
		 
		for (int i=0;i<strlist.size();i++){
			str = strlist.get(i);
			xstr = xstr+str;
			if (i<strlist.size()-1){
				xstr = xstr+";" ;
			}
		}
		
		return xstr;
	}
	
	public String digestNumList( ArrayList<Double> numlist){
		
		String str,xstr = "";
		if ((numlist==null) || (numlist.size()==0)){
			return xstr;
		} 
		
		
		for (int i=0;i<numlist.size();i++){
			double v = numlist.get(i);
			str = String.format("%.7f", v);
			xstr = xstr+str;
			if (i<numlist.size()-1){
				xstr = xstr+";" ;
			}
		}
		
		return xstr;
	}
	
	public String digestIntList( ArrayList<Integer> ilist){
		
		String xstr = "";
		if ((ilist==null) || (ilist.size()==0)){
			return xstr;
		} 
		
		
		for (int i=0;i<ilist.size();i++){
			int vi = ilist.get(i);
			xstr = xstr+""+vi;
			if (i<ilist.size()-1){
				xstr = xstr+";" ;
			}
		}
		
		return xstr;
	}
	
	// ------------------------------------------
	// ------------------------------------------

	protected String getXmlStr( XMLBuilder builder, boolean fullXML ){
		String xmlstr="";
		Properties outputProperties ;
		
		try {

			outputProperties = new Properties();

			outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
			outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");
			outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "3");

			// Omit the XML declaration header, creating just a xml snippet as output...
			if (fullXML == false) {
				outputProperties.put( javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes" );
			}

			xmlstr = builder.asString(outputProperties);
			int p = xmlstr.indexOf("UTF-8");
			if ((p>0) && (p<40)){
				xmlstr = xmlstr.replace("encoding=\"UTF-8\"?>","encoding=\"UTF-8\"?>\n\n");
			}
			xmlstr = xmlstr.trim() ;
		} catch (TransformerException e) {

			e.printStackTrace();
		}
		return xmlstr;
	}
	
 
	
	protected XMLBuilder getXmlBuilder( String XFrame ){
		
		String str;
		
		XMLBuilder builder = null ;
		 
		try {
		
			
			builder = XMLBuilder.create( XFrame ) ;// e.g. "MessageBoard"

		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} 
		
		return builder;
		
	}
	 

	
	 
	
	// ======================================================================================================

	protected int getInteger(String str, int defaultvalue) {
		int result = defaultvalue;

		try {
			if ((str.length() > 0) && (strgutil.isNumericX(str))) {
				result = Integer.parseInt(str);
			}
		} catch (Exception e) {
		}
		return result;
	}
	
	protected long getLong(String str, long defaultValue) {
		long result = defaultValue;

		try {
			if ((str.length() > 0) && (strgutil.isNumericX(str))) {
				result = Long.parseLong(str);
			}
		} catch (Exception e) {
		}
		return result;
	}
	 
	protected double getDouble(String str, double defaultvalue) {
		double result = defaultvalue;

		try {
			if ((str.length() > 0) && (strgutil.isNumericX(str))) {
				result = Double.parseDouble(str);
			}
		} catch (Exception e) {
		}
		return result;
	}

}
