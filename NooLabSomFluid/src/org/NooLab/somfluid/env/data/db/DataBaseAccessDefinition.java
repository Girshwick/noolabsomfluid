package org.NooLab.somfluid.env.data.db;

import java.io.Serializable;
import java.util.Vector;

import org.NooLab.itexx.storage.DataBaseAccessDefinitionIntf;
import org.NooLab.somfluid.util.XmlStringHandling;



/**
 *   this class reads definition resources/files (xml) for organizing
 *   the access to databases 
 *   
 *   The info is presented as a "sijo bean"
 * 
 *
 */
public class DataBaseAccessDefinition 
										implements 
													Serializable,
													DataBaseAccessDefinitionIntf {

	String databaseName="" ;
	XColumns xColumns = new XColumns();
	
	transient private XColumn xColumn;
	
	String user="", password="";
	
	
	//=========================================================================
	public DataBaseAccessDefinition(){
		
	}
	//=========================================================================
	
	// <texx><database name="randomwords">
	public void getDatabaseDefinitionInfo(String xmlstr, String dbname) throws Exception{
		 
		XmlStringHandling xMsg = new XmlStringHandling(); 
		Vector<Object> nodelist,nodeDbObjs ;
		Object nodeObj, dbNodeObj;
		String str, tablename,tnStr;
		
		xMsg.setContentRoot("texx");
		
		nodeDbObjs = xMsg.getNodeList( xmlstr, "texx/database" , "") ;
		nodeDbObjs = xMsg.getNodeList( xmlstr, "texx" , "database") ;
		dbNodeObj = null;
		
		if (nodeDbObjs==null){
			return ;
		}
		for (int i=0;i<nodeDbObjs.size();i++){
			str = xMsg.getNodeInfo( nodeDbObjs.get(i), "database", "name") ;
			if ((str==null) || (str.length()==0)){
				//databaseUrl
				throw(new Exception("Requested name for database is empty."));
			}
			/*
			   <texx> == root
			     <database name="randomwords">
			       <access>
	                 <user name="RG" />
			 */

			if (str.contentEquals( dbname )){// e.g."randomwords"
				dbNodeObj = nodeDbObjs.get(i);
			
				databaseName = dbname;
				user = xMsg.getSpecifiedConditionalInfo(xmlstr, "//database", "name", "randomwords", "/access/user", "name");
				password = xMsg.getSpecifiedConditionalInfo(xmlstr, "//database", "name", "randomwords", "/access/user", "password");
				// str = xMsg.getSpecifiedConditionalInfo(xmlstr, "/texx/database", "name", "randomwords", "/access/user", "name"); these two versions also work! 
				// str = xMsg.getSpecifiedConditionalInfo(xmlstr, "/texx/database", "name", "randomwords", "access/user", "name");
				
				// level 1 table, level 2 core, extension
				// get name of table
				extractFromTable( xMsg, xmlstr, "contexts");
				
				extractFromTable( xMsg, xmlstr, "adhocdocuments");
			}
			// 
		}// i->

		
		
		
	}


	private void extractFromTable(XmlStringHandling xMsg, String xmlstr,String tnStr) {

		
		
		int r = xMsg.setBasicConditionLocation( xmlstr,     /*  any further query will be conditional to this location as a base !!! */
												"//table", 
												"name", 			  
												tnStr ,			       
												"/astor");
		
		_extractXColumns( xMsg, xmlstr, tnStr, "core");
		_extractXColumns( xMsg, xmlstr, tnStr, "extension");
		
	}
	
	private void _extractXColumns(XmlStringHandling xMsg, String xmlstr, String tablename, String domain) {
		
		Vector<Object> nodelist;
		Object nodeObj ;
		
		// nodelist = xMsg.getItemsList(xmlstr, "//core", "item", "id"); // returns the content of id="[1,2,3,4,5]"
		nodelist = xMsg.getItemsList(xmlstr, "//core", "item", "");   // returns the nodes 

		if ((nodelist!=null) && (nodelist.size()>0)){
			for (int n=0;n<nodelist.size();n++){
				/*
	              <item id="1" name="id" quality="id" sema="cid" /> 
	              <item id="4" name="wordlabel" quality="label" sema="word" />
	             */ 
				nodeObj = nodelist.get(0) ;
				
				XColumn xc = new XColumn() ;
				
				xc.table   = tablename;
				xc.domain  = domain;
				xc.id      = xMsg.getNodeInfo(nodeObj, "item", "id");
				xc.name    = xMsg.getNodeInfo(nodeObj, "item", "name");
				xc.quality = xMsg.getNodeInfo(nodeObj, "item", "quality");
				xc.sema    = xMsg.getNodeInfo(nodeObj, "item", "sema");
				
				xColumns.items.add(xc) ;
				
			}// n->
		
			
		}
		
		
	}
	public XColumns getxColumns() {
		return xColumns;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getDatabaseName() {
		return databaseName;
	}


	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	
}
