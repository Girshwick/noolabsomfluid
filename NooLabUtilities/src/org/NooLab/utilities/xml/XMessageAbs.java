package org.NooLab.utilities.xml;

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
	 
	protected PrintLog out;
	protected DFutils fileutil = new DFutils() ;
	protected StringsUtil strgutil = new StringsUtil() ;

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
	public XMessageAbs(){
		
		
	}
	
	
	

	// ======================================================================================================
	
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
			
			xpathQuery.setXml( xmlstr ) ;
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

			xpathQuery.setXml(rawXmlMsg);

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
     * 
     * if idSpecs="" then this will return a list of nodes !
     * if idSpecs is defined, it will return the value of the attribute 
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
    	
    	domainSpecs = domainSpecs + itemSpecs;
    	
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
			xpathQuery.setXml( rawXmlMsg ) ;
			
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
        	
        	
    		xpathQuery.setXml( rawXmlMsg ) ;
    		
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
			xpathQuery.setXml( rawXmlMsg ) ;
			
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
    		
			xpathQuery.setXml( rawXmlMsg ) ;
    		
			node = (Node) xpathQuery.geXmlNodeByName( domainSpecs );
			
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
 
	
	public String getSpecifiedItemInfo( Object xmlNodeObj, String attrLabel){
		String resultStr="";
		
		if (xmlNodeObj==null){
			return resultStr;
		}
		
		resultStr = xpathQuery.readNode( xmlNodeObj, attrLabel );
		
		return resultStr;
	}
	
	/**
	 * 
	 * example: from several "property", we want that with id=1, and from that
	 *          the value of "label"
	 *          &lt;property id="1" label="keywords" /&gt;
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
			xpathQuery.setXml( rawXmlMsg ) ;
			
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
    	
		String  root,secondpart="" ;
		int p;
		
		try{
			// str = domainSpecs;
			
			if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
				return infoStr;
			}
			xpathQuery.setXml( rawXmlMsg ) ;
			
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
			
			infoStr = xpathQuery.getAttributesValue( root , domainSpecs, attrSpecs );
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
		 
		
		xpathQuery.setXml( rawXmlMsg ) ;
		 
		if ((domainSpecs.length()==0) || (attrSpecs.length()==0)){
			return value;
		}
		xpathQuery.setXml( rawXmlMsg ) ;
		
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
	
	// ------------------------------------------
	// ------------------------------------------
	
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

			xmlstr = xmlstr.replace("encoding=\"UTF-8\"?>",
					"encoding=\"UTF-8\"?>\n\n");

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
