package org.NooLab.somfluid.app.astor.storage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import org.w3c.dom.Node;
import com.iciql.Db;

import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.utilities.datetime.DateTimeValue;
import org.NooLab.utilities.strings.StringsUtil;

import org.NooLab.itexx.storage.DBColumn;
import org.NooLab.itexx.storage.DataBaseCreateCommand;
import org.NooLab.itexx.storage.DbUser;
import org.NooLab.itexx.storage.MetaData;
import org.NooLab.itexx.storage.doco.DocoDbTable;



public class DataBaseHandler  {

	AstorDataBase docoDataBase ;
	Db iDb ;
	MetaData metadata;
	Connection connection;
	DateTimeValue datetimeValue ;
	
	StringsUtil strgutil = new StringsUtil(); 
	
	// ========================================================================
	public DataBaseHandler(AstorDataBase texxDataBase){
		docoDataBase = texxDataBase ;
		connection = docoDataBase.getConnection() ;
		
		iDb = docoDataBase.getIciDb() ;
	}
	// ========================================================================
	

	private int getNumTxtFieldType( String table, String field){
		int rVal=-1;
		int tmpval = 0;
		// we loaded all meta info while opening
		DocoDbTable ptable ;
		DBColumn col;
		
		col = this.metadata.getbyNames(table,field) ;
	
		if (col!=null){
			tmpval = col.datatype;
			
			if (col.isNumerical()>=0){
				rVal=1;
			}
			if (col.isBool()>=0){
				rVal=2;
			}
			if (col.isTextual()>=0){
				rVal=3;
			}
			if (col.isTime()>=0){
				rVal=4;
			}
		}
		
		return rVal;
	}

	public void clearAllTables(){
		
		int n ;
		
		String sql;
		PreparedStatement prep;
		
		try{
			if (connection.isClosed()){ return; }
			n = getTableCount();
			
			
			for( DocoDbTable t : metadata.getTables()){
				
				sql = "DROP TABLE IF EXISTS " + t.getName()+ ";" ;
				
				prep = connection.prepareStatement( sql );
				prep.execute();
			}
			
		}catch(SQLException sx){
			sx.printStackTrace();
		}
	}
	
	public void delete(){
		try{
			
			clearAllTables();
			
			if (connection != null){
				connection.close();
			}
			
			File fil = new File( docoDataBase.getDatabaseFile() ) ;
			
			if(fil.exists()){
				fil.delete();
			}
			
		}catch( SQLException sx){
			sx.printStackTrace();
		}
		// now delete the db file ...
		
	}


	public int createDataTablesByResource( ArrayList<DataBaseCreateCommand> ccs){
		
		String sqlStatement;
		int z=0,result = -1;
		
		if (ccs.size()==0){
			return result;
		}
		
		for ( DataBaseCreateCommand cc: ccs){
			sqlStatement ="";
			
			if (cc.getDomain().toLowerCase().startsWith("users")){
				continue;
			}
			if ((cc.getDropCmd()!=null) && (cc.getDropCmd().length()>0)){
				sqlStatement = cc.getDropCmd()+";" ; 
			}
			sqlStatement = sqlStatement  +"\n"+ cc.getCreateCmd()+";" ; ;
			
			docoDataBase.getOut().print(2, "\n"+sqlStatement+"\n") ;
			
			try {
				/*
				Connection c = iDb.getConnection();
				if (c.isClosed()){
					// docoDataBase.connect(dbname, filepath)
					// Db.open(connection) ;
					
				}
				 */
				connection = docoDataBase.getConnection() ;
				
				if ((connection.isClosed()==false) &&
					(connection.isReadOnly()==false)){
						Statement statement = connection.createStatement();
						
						statement.execute( sqlStatement );  
			        
						// connection.commit() ;
						statement.close();
					
						// PreparedStatement prep = connection.prepareStatement( sqlStatement );
						// prep.execute();

						// prep.close();
						
					metadata = new MetaData(connection, docoDataBase.getDatabaseName() );
					metadata.retrieveMetaData();
					
					docoDataBase.setDbMetaData( connection.getMetaData() ) ;

					int n = metadata.getTableNames(1).size() ;
					docoDataBase.getOut().print(2, "tables : "+n) ;
				}
				
				
				// createDataTableExplicit( 0, "ABC", new String[]{});
				result = z;
				z++ ;
			} catch (SQLException e) {
				 
				docoDataBase.getOut().printErr(1, "SQL (index:"+z+") failed : \n"+ sqlStatement+"\n\n");
				result = -100-z;
				// e.printStackTrace();
				break;
			}
			
			
		} // cc -> ccs
		
		if (result>0){
			result=0;
		}
		
		return result;
	}
	


	
	public String getDataBaseNameFromResource(String xmlstr, String appnameShortStr, String pattern) throws Exception {
		String   nameextx="",dbName = "" , extension ;
		String[] nameext = new String[]{""}; // we need 1 empty entry 

		XmlStringHandling xMsg = new XmlStringHandling();
		String ccStr,str, dstr, tnStr;
		boolean hb;
		appnameShortStr = appnameShortStr.toLowerCase() ;
		xMsg.clear();
		xMsg.setContentRoot(appnameShortStr) ; // TODO: should be constant in 
		PersistenceSettings ps ;
		
		Vector<Object> nodeObjs ,nodeDbObjs, nobj;
		Object dbNodeObj;
		
		// enumerate the create sections
		nodeDbObjs = xMsg.getNodeList(xmlstr, appnameShortStr , "database") ;
										// sth like "randomgraph", "texx" ...		
		dbNodeObj = null;		
		
		if (nodeDbObjs==null){
			return "";
		}
		
		for (int i=0;i<nodeDbObjs.size();i++){
			
			str = xMsg.getNodeInfo( nodeDbObjs.get(i), "database", "name") ;
			if ((str==null) || (str.length()==0)){
				throw(new Exception("Requested name for database is empty."));
			}
			
			dbName ="" ;
			nameextx = xMsg.getNodeInfo( nodeDbObjs.get(i), "database", "extensions") ;
			nameext = nameextx.split(";");
			/*
			 *   <texx><database name="randomwords">
			 */
			hb = pattern.length()==0;
			
			if (hb==false){
				for (int n=0;n<nameext.length;n++){
					extension = nameext[n] ;
					if (extension.length()>0){
						hb = StringsUtil.matchSimpleWildCard( str+extension , pattern) ;
					}else{
						hb = str.toLowerCase().contentEquals( pattern);
					}
					if (hb){
						if (pattern.contains("*")){
							dbName = str; // or the extended name ??
						}else{
							dbName = pattern;
						}
						return dbName;
					}
				}
			}
			if ((hb==false) && (pattern.contains("*"))){
				hb = StringsUtil.matchSimpleWildCard( pattern, str) ;
			}
			if ((hb) && (dbName.length()==0)){
				dbName = str;
				return dbName;
			}
			// 
		}// i->
		if (dbNodeObj==null){
			return "";
		}
		
		return "";
	}

	
	public String removeCommentaries(String inStr) {
		int z = 0;
		String ccStr = inStr;
		while ((ccStr.indexOf("**") > 0) && (z < ccStr.length())) {
			String[] rstr = strgutil.substringsBetweenEmbracingPair(ccStr, "**", "**", 0);

			for (int s = 0; s < rstr.length; s++) {
				ccStr = ccStr.replace(rstr[s], " ");
			}
			if (strgutil.frequencyOfStr(ccStr, "**") == 1) {
				ccStr = StringsUtil.replaceall(ccStr, " ** ", " ").trim();
			}
			z++;
		}
		return ccStr;
	}
	
	public ArrayList<DataBaseCreateCommand> getCreateFromResource(String rawXmlMsg, String appshortname , String databaseName) throws Exception  {
		
		ArrayList<DataBaseCreateCommand> ccs = new ArrayList<DataBaseCreateCommand>(); 
		DataBaseCreateCommand cc = null;
		 
		
		/*
	    <documentobserver>  the name of the database
  	       <config>
  	            <separator name="::" />
  	       </config>
  
  	  <access>
	     <users>
	        <user id="1" name="rg" password="rg" properties="" operation="RW" />
	        <user id="2" name="astor" password="astor"  properties="" operation="RW" />
	        <user id="3" name="texx" password="texx"  properties="" operation="RW" />
	        <user id="4" name="sf" password="sf"  properties="" operation="RW" />
	     </users>
	  </access>

  	         <table name="documents" >
  	            <create>
       	          <drop active="1" cmd="DROP TABLE IF EXISTS ::tablename::" />
       	          <text>
  	               CREATE TABLE IF NOT EXISTS ::tablename:: (  
  	       				  id            BIGINT primary key,
  	       				  guid          VARCHAR(40), 
  	       				  location      VARCHAR, 
  	       				  filename      VARCHAR(120),
  	       				  isremote      int, 
  	       				  changedate    int, ** CURRENT_TIMESTAMP() 
  	       				  status        int, 
  	       				  mime          VARCHAR(20) 
		          </text>
  	            </create>
  	         </table>

  	    </documentobserver>
	 */
		
		XmlStringHandling xMsg = new XmlStringHandling();
		String ccStr,str, dstr, tnStr, _user ,_pwd;

		String   nameextx="",dbName = "" , matchingDbName="", extension ;
		String[] nameext = new String[]{""}; // we need 1 empty entry 
		boolean hb;
		
		
		
		xMsg.clear( );
		xMsg.setContentRoot( appshortname) ; // TODO: should be constant in PersistenceSettings !
		
		Vector<Object> nodeObjs ,nodeDbObjs, nobj;
		Object dbNodeObj;
		
		// enumerate the create sections
		nodeDbObjs = xMsg.getNodeList(rawXmlMsg, appshortname , "database") ;
		dbNodeObj = null;
		
		if (nodeDbObjs==null){
			return ccs;
		}
		for (int i=0;i<nodeDbObjs.size();i++){
			
			str = xMsg.getNodeInfo( nodeDbObjs.get(i), "database", "name") ;
			

			dbName ="" ; hb=false;
			nameextx = xMsg.getNodeInfo( nodeDbObjs.get(i), "database", "extensions") ;
			nameext = nameextx.split(";");

			
			if ((str==null) || (str.length()==0)){
				//databaseUrl
				throw(new Exception("Requested name for database is empty."));
			}
			
			
			for (int n=0;n<nameext.length;n++){
				extension = nameext[n] ;
				if (extension.length()>0){
					hb = StringsUtil.matchSimpleWildCard( str+extension , databaseName) ;
				}else{
					hb = str.contentEquals(databaseName) ;
				}
				if (hb){
					if (databaseName.contains("*")){
						matchingDbName = str; // or the extended name ??
					}else{
						matchingDbName = str;
					}
					break;
				}
			}

			if (hb){
				
				dbNodeObj = nodeDbObjs.get(i);
				
				break;
			}
			// 
		}// i->
		
		if (dbNodeObj==null){
			return ccs;
		}
		
		
		
		nodeObjs = xMsg.getSubtagFromNode( (Node)dbNodeObj, "table", "", "");

		
		if (nodeObjs==null){
			return ccs;
		}
		
		for (int i=0;i<nodeObjs.size();i++){
			Object node = nodeObjs.get(i);
		
			tnStr = xMsg.getNodeInfo( node, "/table", "name") ;
			// tnStr = xMsg.getNodeInfo(nodeObjs.get(i), "/", "name") ; would work as well
			
			// str  = xMsg.getNodeInfo( node, "/create/drop", "active") ;
			// dstr = xMsg.getNodeInfo( node, "/create/drop", "cmd");
			
			str  = xMsg.getSpecifiedConditionalInfo(rawXmlMsg, "/table", "name", tnStr, "/create/drop", "active") ;
			dstr = xMsg.getSpecifiedConditionalInfo(rawXmlMsg, "/table", "name", tnStr, "/create/drop", "cmd");
			
			ccStr = xMsg.getTextDataFromConditionalSection(rawXmlMsg, "/table", "name", tnStr, "/create/text") ;

			
			if (ccStr.length()>0){
				cc = new DataBaseCreateCommand() ;
				
				ccStr = removeCommentaries(ccStr);
				
				ccStr = StringsUtil.replaceall(ccStr, " ** ", " ").trim() ;
				ccStr = StringsUtil.replaceall(ccStr, "\t", " ").trim() ;
				ccStr = StringsUtil.replaceall(ccStr, "  ", " ").trim() ;
				ccStr = StringsUtil.replaceall(ccStr, "( ", "(") ;
				
				dstr = dstr.replace("::tablename::", tnStr); 
				ccStr = ccStr.replace("::tablename::", tnStr);
				
				cc.setTableName(tnStr);
				cc.setDrop(dstr); 
				cc.setCreate(ccStr) ;
				
				ccs.add(cc);
			}
			
		} // all table sections
		
		// users
		/*
		   	  <access>
	     	    <users>
	               <user id="1" name="rg" password="rg" properties="" operation="RW" />
 
		 */
		Vector<Object> usernodes;
		
		tnStr = xMsg.getNodeInfo( (Node)dbNodeObj, "/table", "name") ;
		
		 try{
			 // that's experimental ...
			 // usernodes = xMsg.getSpecifiedConditionalNode(rawXmlMsg, "//database", "name", databaseName, "/access/users/user");
		 }catch(Exception e){
			 
		 }
		
		
		// instead we use ...
		int r = xMsg.setBasicConditionLocation( rawXmlMsg, "//database", "name", matchingDbName, "/access");
												// note that "databaseName" could be "matchingDbName" + "[extension]"
												// yet, here we need the dbname as it is defining the xml node
		if (r!=0){
			throw(new Exception("Requested node in xml not found (expected node: database::name::"+matchingDbName+"::access , r="+r+")."));
		}
		usernodes = xMsg.getItemsList(rawXmlMsg, "//users", "user", "");
		
		
		
		if ((usernodes != null) && (usernodes.size()>0)){
			
			cc = new DataBaseCreateCommand("USERS") ;
			ccs.add(cc) ;
			
			for (int i=0;i<usernodes.size();i++){
				Object node = usernodes.get(i);
				
				
				_user = xMsg.getNodeInfo( node, "user", "name");
				_pwd  = xMsg.getNodeInfo( node, "user", "password");
				str = xMsg.getNodeInfo( node, "user", "operation");
				
				if ((_user.length()>0) && (_pwd.length()>0)){
					DbUser dbu = new DbUser();
					dbu.setUsername(_user) ;
					dbu.setPassword(_pwd) ;
					cc.getUsers().add(dbu) ;
					
				}
			}
			
		}
		  
			
		return ccs;
	}

	
	/**
	 * 
	 * @param keep <ul>
	 *             0=overwrite the table if it already exists;<br/>
	 *             1=using the provided name of the table as a basic name, <br/>
	 *             stuffing it with a serial enumeration "00000" and the  <br/>
	 *             current date-time;<br/>
	 *             2=append, if the column headers match, else fall back to keep=1;<br/>
	 *             </ul><br/>
	 * @param tablename <ul>the basic name of the table to be created;<br/>
	 * if keep=1, the table name will be changed
	 * </ul>
	 * @param columnheaders the column names for the table to be created
	 * @return the actual name of the opened table, which will be different<br/>
	 * from the requested one, if keep=1;<br/> 
	 */
	public String createDataTableExplicit( int keep , String tablename, String[] columnheaders){
		/*
		 
		  	DROP TABLE IF EXISTS Data;
			CREATE TABLE Data(ID INT PRIMARY KEY, timedist VARCHAR(255), A VARCHAR(255) , B VARCHAR(255)   );
			INSERT INTO Data VALUES( 11, '964528128' , '29.86'  , ' 226.8');
			SELECT * FROM Data ORDER BY ID;
		 */
		PreparedStatement prep;
		ResultSet resultset ;
		Statement statement;
		String sql, dropp="";
		String dtstr="";
		String name_of_createdtable="",hs1;
		boolean result;
		
		tablename = tablename.toUpperCase();
		
		if (keep==0){
			dropp = "DROP TABLE IF EXISTS "+tablename+"; ";
		}
		if (keep==1){ // use provided tablename as a basis
			// change tablename
			
			int n = getTableCount(tablename);
			// possible name: DATA_xyz_0001_2342342
			
			 
			dtstr = datetimeValue.get(); // according to defined format
			
			hs1 = String.format("%05d", n);
			
			tablename = tablename + "_"+hs1+"_" + dtstr;
			
		}	
	
		
		sql = dropp +
			  "CREATE TABLE "+tablename+" (ID INT PRIMARY KEY, timedist VARCHAR(255), A VARCHAR(255) , B VARCHAR(255)   ); "+
			  "";
			  // "INSERT INTO "+tablename+" VALUES( 1, '1' , '29.86'  , ' 226.8'); " ;
			  // "SELECT * FROM Data ORDER BY ID;"; " ;
		
		try{
			// prep = connection.prepareStatement( sql );
			// prep.execute();
			
			if (keep<2){ // append mode 
				statement = connection.createStatement();  
				statement.execute(sql);  
	        
				statement.close();
			}
			
	        name_of_createdtable = tablename;

			metadata = new MetaData(connection, docoDataBase.getDatabaseName() );
			docoDataBase.setDbMetaData( connection.getMetaData() ) ;

		}catch(SQLException sx){
			name_of_createdtable = "";
			sx.printStackTrace();
		}
		
		
		return name_of_createdtable;
	}

	public DocoDbTable getTable(String tablename){
		DocoDbTable table=null;
		 
		int ix=-1;
		
		int tn = metadata.getTables().size();
		  
		for (DocoDbTable t : metadata.getTables()){
			ix++;
			if (tablename.contentEquals(t.getName())){
				table = t;
				break;
			}
		}
	  
		return table;
	}
	
	
	public String[] getTables(){
		String[] tablenames = null;
		int ix=-1;
		
		int tn = metadata.getTables().size();
		tablenames = new String[tn];
		
		for (DocoDbTable t : metadata.getTables()){
			ix++;
			tablenames[ix] = t.getName();
		}
		
		return tablenames;
	}
	
	public int getTableCount(){
		return getTableCount("*") ;
	}
	
	public int getTableCount( String namesnip){
		int z,rVal=-1;
		ResultSet resultset;
		String sql,approx;
		
		// SELECT  TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE ='TABLE' AND TABLE_NAME LIKE 'DATA%'
		
		approx = " AND TABLE_NAME LIKE '%"+namesnip+"%';" ;
				 
		if (namesnip.contentEquals("*")){
			approx="";
		}
		else{
			if (namesnip.contains("*")){
				namesnip = namesnip.replace("*","%");
				approx = " AND TABLE_NAME LIKE '"+namesnip+"';" ;
			}	
			else{
				if (namesnip.length()>0){
					
				}
			}
		}
			
		sql = "SELECT  TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE ='TABLE' " +approx;
		
		try{
			PreparedStatement prep = connection.prepareStatement( sql );
			resultset = prep.executeQuery();
			 
			z=0;
			while (resultset.next()){
				z++;
			}
			rVal = z;
			resultset.close();

		} catch( SQLException sx ){
			sx.printStackTrace();
		} finally{
			 
		}
		
		return rVal;
	}
	
	private boolean indexvalueExists(String table, String id_field, int indexvalue){
		String sql;
		boolean rb=false;
		ResultSet resultset;
		int r=-1,z=0;
		
		sql = "SELECT "+id_field+" FROM "+table+ " WHERE "+id_field+"="+indexvalue +";";
		
		try{
			if (connection.isClosed()){
				return false;
			}
			
			PreparedStatement prep = connection.prepareStatement( sql );
			resultset = prep.executeQuery();
			
			while (resultset.next()){
				r = resultset.getInt(id_field);
				z++;
			}
			if (z>0){
				rb=true;
			}
		}catch(SQLException sx){
			sx.printStackTrace();
		}
		return rb;
	}
	
	

	public void sampleCreateTable(){
		String tablename = "Employee" ;
		
		String sqlCmd = 	
					"CREATE TABLE "+tablename+"(  \n"+" "+
					"  id            int, \n"+
					"  first_name    VARCHAR(15),\n"+
					"  last_name     VARCHAR(15),\n"+
					"  start_date    DATE,\n"+
					"  end_date      DATE,\n"+
					"  salary        FLOAT(8,2),\n"+
					"  city          VARCHAR(10),\n"+
					"  description   VARCHAR(15)\n"+
					"  );  ";
			     
	}

	/**
	 * 
	 * This runs a select query (insert and update queries are treated separately)
	 * So far, it is not run capable to tun complicated joins across
	 * several tables
	 * 
	 * 
	 * @param table the name of the table in the opened database
	 * @param sql the sql command
	 * @param params the params for the sql command
	 */
	public void query( String table, String[] fields, String[] params){
		
		String field_list="", _condition ;
			
		PreparedStatement prep ;
		ResultSet resultset ;
		
		// SELECT * FROM KDATA 
		
		// variables from java program : (X INT=?) 
		
		// sql = "SELECT * FROM TABLE(X INT=?) T INNER JOIN TEST ON T.X=TEST.ID" ;
		// PreparedStatement prep = connection.prepareStatement( sql );
		// prep.setObject(1, new Object[] { "1", "2" });
		
		// SET @TOTAL = NULL;
		// SELECT X, SET(@TOTAL, IFNULL(@TOTAL, 1.) * X) F FROM SYSTEM_RANGE(1, 50);
	
		// exporting a csv file
		// CALL CSVWRITE('test.csv', 'SELECT * FROM TEST');
	
		// create fieldlist
		
		
		// create condition
		
		_condition = " WHERE " ;
		
		try{
			// 
			
			String sql_cmd =
				" SELECT "+ field_list +" FROM "+table+
				_condition
				 ;
			
			
			prep = connection.prepareStatement( sql_cmd );
			resultset = prep.executeQuery();
			
			
		}catch(SQLException sx){
			sx.printStackTrace();
		}
		
	
	}

	// TODO  better using a String[] and a map, this ensures appropriate matches
	public void insert(String table, String[] fields, String[] values){
		
		String field_list="", values_list="",v="";
		int i,k, fieldtype, _sensor_positions;
		
		
		try{
			
			for ( i=0;i<fields.length;i++){
				field_list = field_list + fields[i];
				
				if (i<fields.length-1){
					field_list = field_list + "," ;
					 
				}
			}
			field_list = "";
			
			_sensor_positions = values.length ;
			if ( fields.length < _sensor_positions){
				_sensor_positions = fields.length ;
			}
			
			for ( i=0;i< _sensor_positions;i++){
				v ="";
				// we need the type information !!
				// looking up type for 
				
				fieldtype = getNumTxtFieldType(table, fields[i]) ;
				if (fieldtype == 1){
					v = values[i] ;
				}
				if (fieldtype == 2){ // bool
					v = values[i] ;  // we expect that data are supplied as strings "true" or "false" 
				}
	
				if (fieldtype == 3){
					v = "'"+ values[i]+"'";	
				}
				if (fieldtype == 4){
					v = values[i] ;
				}
				
				if (v.length()>0){
					values_list = values_list + v;
					field_list = field_list + fields[i];
					
					if (i<values.length-1){
						values_list = values_list + "," ;
						field_list  = field_list  + "," ;
					}
				}
	
			} // i->
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String sql_cmd =
						" INSERT INTO "+table+" "+
						" ("+ field_list +") "+
						" VALUES ("+ values_list +") ; "+
						"  ";
		sql_cmd = sql_cmd.replace(",)", ")");
		sql_cmd = sql_cmd.replace("(,", "(");
		try{
			if (sql_cmd.indexOf("()")<0){
				PreparedStatement prep = connection.prepareStatement( sql_cmd );
				prep.execute();
				connection.commit();
			}
		}catch(SQLException sx){
			sx.printStackTrace();
			
			// int recordid;
			// recordid = Integer.parseInt( values[0]);
			// indexvalueExists(table, "ID", recordid);
		}
		 // (id,first_name, last_name, start_date, end_Date,   salary,  City,       Description)
	     //               values (1,'Jason',    'Martin',  '19960725',  '20060725', 1234.56, 'Toronto',  'Programmer');
	}

	public void exportCSV(String tablename, String filename){
		String sql;
		Statement statement ;
		
		try{
			sql = "CALL CSVWRITE('"+filename+"', 'SELECT * FROM "+tablename+"');" ;
	
			statement = connection.createStatement();  
			statement.execute(sql);  
	    
			statement.close();
			
			 
		}catch(SQLException sx){
			 
			sx.printStackTrace();
		}
	}


	public void setiDb(Db iDb) {
		this.iDb = iDb;
	}

	/**
	 * creates a new instance of MetaData  
	 */

	
	public void updateMetaData(Connection c) {
		
		connection = c;
		metadata = new MetaData(c, docoDataBase.getDatabaseName() );
		metadata.retrieveMetaData() ;
		
	}

	/**
	 * creates a new if necessary 
	 */
	public MetaData getMetadata() {
		if (metadata==null){
			updateMetaData(connection) ;
		}
		return metadata;
	}


	public Connection getConnection() {
		return connection;
	}







	

}
