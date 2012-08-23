package org.NooLab.somfluid.env.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.h2.jdbc.JdbcSQLException;
import org.h2.server.Service;
import org.h2.tools.Server;
import com.iciql.Db;


import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.resources.ResourceContent;
import org.NooLab.utilities.resources.ResourceLoader;


import org.NooLab.docserver.main.DocumentObserverIntf;
import org.NooLab.docserver.storage.db.iciql.Folders;
import org.NooLab.itexx.storage.ConnectorClientIntf;
import org.NooLab.itexx.storage.DatabaseMgmt;
import org.NooLab.itexx.storage.DbConnector;
import org.NooLab.itexx.storage.DbLogin;
import org.NooLab.itexx.storage.DbUser;
import org.NooLab.itexx.storage.randomwords.iciql.Contexts;
import org.NooLab.itexx.storage.store.DataBaseCreateCommand;


import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.structures.randomgraph.RawEntityPosition;
 
 
 


/**
 * 
 * 
 *
 */
public class _obs_TexxDataBase implements ConnectorClientIntf{

	String cfgResourceJarPath = "";
	// in factory, it is set to persistence settings, from where we fetch it here
	// "org/NooLab/texx/resources/sql/" ; // trailing / needed !!   
	
	SomFluidProperties texxProperties ;
	PersistenceSettings ps;
	
	private boolean dbFileExists;
	private boolean isOpen;
	boolean dbRecreated = false;
	
	
	String databaseName = "";
	String configFile = "";
	String storageDir = "" ; 
	
	String databaseUrl="";

	String accessMode = "tcp"; // "http" = external server via 8082, "file" 
	Server server;
	Connection connection;
	Db iciDb;
	DatabaseMetaData jdbMetaData ;
	String dbCatalog ;
	MetaData metaData ; 
	
	DbConnector dbConnector ;
	DataBaseHandlerIntf dbHandler ;
	
	String user = "";
	String password = "" ;
	
	Class parent;
	String databaseFile="";
	
	DFutils fileutil = new DFutils();
	PrintLog out = new PrintLog(2,false);

	
	StringedObjects strObj = new StringedObjects();
	protected String internalCfgStoreName;
	
	// ========================================================================
	public _obs_TexxDataBase( SomFluidProperties props ){
		texxProperties = props ;
		
		ps = props.getPersistenceSettings();
		
		// sth like "rg-fingerprints";
		databaseName =  ps.getDatabaseName();
		user = ps.getDbUser() ;
		password = ps.getDbpassword() ;
		
		String cfgJarPath = ps.getConfigSqlResourceJarPath() ;
		if ((cfgJarPath!=null) && (cfgJarPath.length()>0)){
			cfgResourceJarPath = cfgJarPath ;
		}
		
		internalCfgStoreName = ps.getInternalSqlCfgStoreName(); 
		if (internalCfgStoreName.length()==0){
			internalCfgStoreName = "create-db-sql-xml" ;
		}
		
		// String configFile = databaseName+".h2.db-config.xml";
		
		
		getStorageDir();
		
		dbConnector = new DbConnector( (ConnectorClientIntf)this);

		out.setPrefix("[TEXX-DB]");
	}

	public _obs_TexxDataBase(){

		out.setPrefix("[rgraph-db]");
	}

	// ========================================================================
	
	public boolean dbFileExists(){
		return dbFileExists;
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	
	private boolean removeLock(){
		boolean rB=false;
		
		try{
		

			if (connection==null){
				ArrayList<String> locks = fileutil.listOfFiles("lock",".db", storageDir);
				for (int i=0;i<locks.size();i++){
					String fname = locks.get(i);
					if (DFutils.fileExists( fname)){
						fileutil.deleteFile( fname) ;
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return rB;
	}

	public boolean prepareDatabase( String expected) throws Exception {
		boolean rB=false;
		String appnameShortStr;
		 
		try {
			// remove any lock 
			if (connection==null){
				removeLock();
			}else{
				return true;
			}
			
			if ((databaseName==null) || (databaseName.length()==0)){
				
				//             e.g. resourceName = "create-db-sql-xml" ;
				String xmlstr;
				
				xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, internalCfgStoreName ) ;
				
				appnameShortStr = texxProperties.getPersistenceSettings().getAppNameShortStr() ;
				
				dbHandler = new DataBaseHandler( this ) ;
				databaseName = dbHandler.getDataBaseNameFromResource( xmlstr,  appnameShortStr, expected ) ;
			}
			
			int servermodeFlag=0;
			if (accessMode.contentEquals("tcp")){
				// if in server mode
				String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR) ;
				h2Dir = DFutils.createPath(h2Dir, "storage/") ;
				
				String dbn  = databaseName;
				int r = createServer( databaseName,h2Dir) ;
				if (r!=0){
					if ((databaseName==null) || (databaseName.length()==0)){
						dbn = "???";
						String str = "creating or connnecting to server for database name <"+dbn+"> failed in mehod <prepareDatabase()>." ;
						throw(new Exception(str)) ;
					}
					// ... let us proceed and look whether we can connect...
				}
				servermodeFlag=1;
			}else{
				if (accessMode.contentEquals("http")){
					servermodeFlag=1;
				}
			}
			//  
			String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR) ;
			h2Dir = DFutils.createPath(h2Dir, "storage/") ;

			databaseFile = connect( databaseName, h2Dir, servermodeFlag ) ;
			
			
			
			rB = databaseFile.length()>0;
			
			if (rB){
				
				dbHandler = new DataBaseHandler( this ) ;
		
		// returns falsely false for randomwords		
				if ((checkStructure(connection,"randomwords") != 0) || (dbRecreated)){ 
					// connection.close();
					createDatabaseStructure( databaseName ) ;// usually, it should be taken from the XML
				}else{
					metaData = dbHandler.getMetadata();
					metaData.retrieveMetaData();
					
					String str = ArrUtilities.arr2Text( metaData.getTableNames(1),";");

					out.printErr(2, "Tables : "+str) ;
				}
				
			}
			
			if ((connection!=null) && (connection.isClosed()==false)){
				connection.close() ;
			}
			
		} catch (FileNotFoundException e) {
			rB=false;
			e.printStackTrace();
		}
		 
		return rB;
	}

	//             e.g. resourceName = "create-db-sql-xml" ;
	

	
	private void createDatabaseStructure(String dbname) throws Exception {
		// loading the  "create-db-sql-xml"  
		// from package "org.NooLab.docserver.resources.sql"
		boolean rB;
		String appshortname,xmlstr = "" ;
		int r=-1 ;

		xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, internalCfgStoreName ) ;
		// xmlstr = getConfigResource( internalCfgStoreName); // "create-db-sql-xml");
		
		String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR) ;
		h2Dir = DFutils.createPath(h2Dir, "storage/") ;
		
		connect( databaseName,h2Dir) ;
		
		// get the create statements
		appshortname = this.texxProperties.getPersistenceSettings().getAppNameShortStr() ;
		ArrayList<DataBaseCreateCommand> ccs = dbHandler.getCreateFromResource( xmlstr, appshortname, dbname ); 
		
		// executing the statements
		r = dbHandler.createDataTablesByResource(ccs);
		
		if (r!=0){
			throw(new Exception("createDataTablesByResource() failes, error code = "+r+"\n"));
		}
		
		if ((connection==null) || (connection.isClosed())){
			
			h2Dir = h2Dir.substring(0,h2Dir.length()-1);
			String url = 	"jdbc:h2:tcp://localhost/"+h2Dir+"/"+dbname; 
			
			DbLogin login = new DbLogin(user,password) ; 
			connection = dbConnector.getConnection(url, login);
			
		}
		create_iTexxUsers(connection);
		
	}

	
	public void create_iTexxUsers(Connection c) throws Exception {
		
		String str="";
		if (c.isClosed()){
			open(c);
		}
		str = "rg" ;
		DbUser.createDbUser(c, "rg", str) ;
		DbUser.createDbUser(c, "sa", "sa") ;
	}
	
	
	private int checkStructure(Connection c, String dbname) {
											// "randomgraph" "randomwords" "rg-fingerprints"
		ArrayList<String> tnames = new ArrayList<String>();
		String xmlstr,xmlRoot,appshortname;
		boolean rB=false;
		int tableCountDiff = -99 ;
		int expectedTableCount = 9999 ;
		
		Vector<Object> nodeObjs = null;
		XmlStringHandling xMsg = new XmlStringHandling();
		// read definition file 
		
		try {
			if ((c==null) ){
				 // open(c) ;
			}else {
				open();
				c = connection;
			}
		
			jdbMetaData = c.getMetaData() ;
			if (metaData==null){
				dbHandler.updateMetaData(c);
				metaData = dbHandler.getMetadata() ; 
			}
			tnames = metaData.getTableNames(1) ;
			
			xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, internalCfgStoreName ) ;
			// xmlstr = getConfigResource("create-db-sql-xml"); == internalCfgStoreName
			
			xmlRoot = this.texxProperties.getPersistenceSettings().getAppNameShortStr();
			xMsg.setContentRoot(xmlRoot) ;
			
			nodeObjs = xMsg.getNodeList(xmlstr, "/"+xmlRoot+"/database", "table");
			
			appshortname = this.texxProperties.getPersistenceSettings().getAppNameShortStr() ;
			ArrayList<DataBaseCreateCommand> xcc = dbHandler.getCreateFromResource(xmlstr,appshortname, dbname);
																					// "randomwords"
			if (nodeObjs!=null){
				expectedTableCount = nodeObjs.size() ;
				// expectedTableCount = xcc.size() ; // 1 element per table
			}
				
			tableCountDiff =  expectedTableCount - tnames.size();
			if (tableCountDiff != 0){
				// close();
				return tableCountDiff;
			}
			
			for (int i=0;i<nodeObjs.size();i++){
				String tname = xMsg.getNodeInfo(nodeObjs.get(i), "/table", "name") ;
					// ((Node)nodeObjs).
				int p = tnames.indexOf(tname.toUpperCase()) ;
				if (p<0){
					rB=false;
					return -100-tableCountDiff;
				}
			}
			
			rB=true;
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		return tableCountDiff;
	}


	
	public void close() {
		
		try{

			jdbMetaData = null;
			dbCatalog = "";
			disconnect();
			if (server != null){
				server.stop() ;
			}
		
		}catch(Exception e){
			
		}
		
	}

	protected void getJdbMeta(Connection c) throws SQLException{
		jdbMetaData = c.getMetaData();
		dbCatalog = c.getCatalog() ;
		
	}

	public void setDbMetaData(DatabaseMetaData dbMetaData) {
		jdbMetaData = dbMetaData;
	}

	public void updateInfraStructure(Connection c ) throws Exception{

		getJdbMeta(c);
		
		if (dbHandler==null){
			out.printErr(1, "Datbase has not been properly instantiated.");
		}
		dbHandler.setiDb( iciDb ) ;

		// creates a new instance for new MetaData and retrieves the latest info again
		dbHandler.updateMetaData(c) ; 
	
	}
	
	
	public void open(Connection c) throws Exception{
		
		iciDb = Db.open(c);
		updateInfraStructure(c);
	}

	public void open() throws Exception{
		
		if (databaseUrl.length()==0){

			String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR );
			String _db_dir = DFutils.createPath( h2Dir, "storage/") ; // storageDir;

			if (_db_dir.endsWith("/")){
				_db_dir = _db_dir.substring(0,_db_dir.length()-1);
			}

			databaseUrl = 	"jdbc:h2:tcp://localhost/"+_db_dir+"/"+databaseName; // +";AUTO_SERVER=TRUE" ;

		}
			
		iciDb = Db.open(databaseUrl, user, password);
		connection = iciDb.getConnection();

		updateInfraStructure(connection);
	}

	  	
	public void startServer( String dbNamePattern ) { 
		
		removeLock();
		try {
			prepareDatabase( dbNamePattern );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/*
SELECT * FROM INFORMATION_SCHEMA.ROLES ;
SELECT * FROM INFORMATION_SCHEMA.RIGHTS ;
SELECT * FROM INFORMATION_SCHEMA.USERS ;
SELECT * FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES ;
	 */
	public Connection getWireLessDatabaseServer(String user, String password) throws Exception{
		
		String url;
		
		Connection c = null;
		
		 
		try {
			// "jdbc:h2:"+
			url = 	"jdbc:h2:tcp://localhost/~/"+databaseName ;
			
			DbLogin login = new DbLogin(user,password) ; // may contain a pool of users
			connection = dbConnector.getConnection(url, login);

			
			dbHandler = new DataBaseHandler( this ) ;
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		throw(new Exception("wrong db mode in getWireLessDatabaseServer() !!!!!!!!!!!"));
		// return c;
	}
	
	public int createServer(String dbname, String storageDir){
		

		// 
		// start the TCP Server
		String url;
		int r=-1;
		
		try {
			// "jdbc:h2:"+
			url = 	storageDir+"/"+dbname+".h2.db"; // +";AUTO_SERVER=TRUE" ;
			url = url.replace("//", "/") ;

			if (DFutils.fileExists(url)==false){
				dbRecreated = true;
				connect(dbname, storageDir);
				if (connection!=null){
					connection.close();
				}else{
					throw(new Exception("It was not possible to create a new database, or to connect to the new database\n"+
							            "Requested name was <"+dbname+">, "+
							            "requested folder was "+storageDir+"\n\n")) ;
				}
				connection = null ;
				r=1;
			}

			String[] args = new String[]{"-tcpAllowOthers","-tcpPort","8052"};

			server = Server.createTcpServer( args );
			// this starts the console in the browser Server.main(args);
			server.start() ;
			
			r=2; 
			
			int port = server.getPort();
			Service srvc = server.getService();
			boolean allowOthers = srvc.getAllowOthers() ;
			
			if (server.isRunning(true)){
				out.print(2, "H2 server is running on port "+port) ;
				r=0;
			}
			 
			
		} catch (Exception e) {
			r = -7;
			out.printlnErr(1, "Potential Problem met in createServer(): "+ e.getMessage());
		}
		//createTcpServer(args).start();

		// stop the TCP Server
		// server.stop();


		return r;
	}

	
	public String connect( String dbname, String filepath) throws FileNotFoundException{
		return connect( dbname, filepath, 0);
	}
	public String connect( String dbname, String filepath, int serverMode) throws FileNotFoundException{
		
		String dbfile ="";
		int err=0;
		
		
		
		try{
 			
			if ((connection!=null) && (connection.isClosed()==false)){
				dbfile = DFutils.createPath(filepath,dbname+".h2.db") ;
				if (DFutils.fileExists(dbfile)){
					return dbfile ;
				}
			}

			
			// String docoservUrl = connection.
			// "jdbc:h2:tcp://localhost/~/docoserv"
			// :nio
			String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR );
			String _db_dir = DFutils.createPath( h2Dir, "storage/") ; // storageDir;

			if (_db_dir.endsWith("/")){
				_db_dir = _db_dir.substring(0,_db_dir.length()-1);
			}

			String url = 	"jdbc:h2:tcp://localhost/"+_db_dir+"/"+dbname; // +";AUTO_SERVER=TRUE" ;
			url = url.replace("//", "/") ;

			// if (serverMode>=1)
			{
				url = 	"jdbc:h2:tcp://localhost/"+_db_dir+"/"+dbname;
			}
			
						
			databaseUrl = "";
				        /* 
				            ";MODE=MYSQL"
		    				";FILE_LOCK=FS" +
		    				";PAGE_SIZE=1024" +
		    				";CACHE_SIZE=8192";
						    DB_CLOSE_DELAY=10
				        */
		
			Class h2Driver = Class.forName("org.h2.Driver");
	        
			if (connection==null){
				if ((user==null) || (user.length()==0)){
					user="sa"; password="sa";
				}
				
				DbLogin login = new DbLogin(user,password) ; // may contain a pool of users
				connection = dbConnector.getConnection(url, login);

				// connection = DriverManager.getConnection( url, user, password);
			}

			dbfile = DFutils.createPath(filepath,dbname+".h2.db") ;
			
			File fil = new File(dbfile);
			if (fil.exists()){
				if (connection.isClosed()==false){
					databaseUrl = url;
				}
				
			}
			
			 
			// CALL DATABASE_PATH(); 
			 
			
			if (fil.exists()==false){
				throw new FileNotFoundException("\nDatabase file not found (err="+err+")\n"+
											    "expected directory : " + filepath+"\n"+
											    "expected db file   : " + dbname+".h2.db\n");
			}

			out.print(1,"database has been started ...");
			out.print(2,"...its connection url is : "+ databaseUrl) ;
			
		}catch(JdbcSQLException jx){
			System.out.println("Connecting to database <"+dbname+"> failed \n"+jx.getMessage() );
			String lockfile = filepath+"/"+dbname+".lock.db";
			File fil = new File(lockfile);
			fil.deleteOnExit();
			fil.delete();
			
			dbfile = "";
			
		}catch( SQLException sx){
			System.err.println ("Cannot connect to database server");
			dbfile = "";
			sx.printStackTrace();
			
		}catch( ClassNotFoundException e){
			dbfile = "";
			e.printStackTrace();
		}catch( Exception e){
			e.printStackTrace();
		} 
		
		 
		return dbfile;
	}


	public void checkForDbAvailability() { 
		
		try {
			
			if ((connection==null) || (connection.isClosed())){
				
				String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_STOR) ;
				h2Dir = DFutils.createPath(h2Dir, "storage/") ;

				connect( databaseName, h2Dir) ;
			}

			if (iciDb==null){
				open( connection );
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void disconnect(){
		
		try{
			
			if (connection != null){
				
				if (connection.isClosed()==false){
					connection.commit();
					
					out.delay(100);
					
					connection.close(); 
				}
				
				out.delay(100);
				if (connection.isClosed()){
					connection = null;
				}
			}
			
		}catch( SQLException sx){
			sx.printStackTrace();
		}finally {
	         
	    }
		
	}

	public Connection getConnection(){
		
		return connection;
	}
	
	
	public String getStorageDir( ){
		
		String systemroot = texxProperties.getSystemRootDir();
		String prj = texxProperties.getPersistenceSettings().getProjectName() ;
		
		storageDir = DFutils.createPath(systemroot, prj+"/") ;
		storageDir = DFutils.createPath(storageDir, "storage/") ;
		
		return storageDir ; 
	}
	
	private String getDBhomefile( String dbname){
		
		String filename="",filepath, user_home;
			
		getStorageDir();
		filepath = DFutils.createPath(storageDir, dbname);
		
		return filepath;
	}

	public TexxProperties getDocoProperties() {
		return texxProperties;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public Db getIciDb() {
		return iciDb;
	}

	public DatabaseMetaData getDbMetaData() {
		return jdbMetaData;
	}

	public String getDbCatalog() {
		return dbCatalog;
	}

	public DataBaseHandlerIntf getDbHandler() {
		return dbHandler;
	}

	public String getDatabaseFile() {
		return databaseFile;
	}

	public PrintLog getOut() {
		return out;
	}

	@SuppressWarnings("unchecked")
	public void storeDocTable(ArrayList<ArrayList<?>> doctable) throws Exception {
		 
		ArrayList<ArrayList<?>> valuestable;
		ArrayList<?> row,headerRow;
		ArrayList<Integer> indexCols = new ArrayList<Integer>();
		
		String cn, str, valstr,strval;
		Object obj;
		long index,ixval;
		double numval,v ;

		ArrayList<Double> assignatesValues ;
		Contexts iciContext;
		
		
		// 1. we transfer everything to string,
		// 2. max accuracy in data vectors is 7 ;
		// null strings should be corrected to empty string (...and TRIM() it !!! )
		
		try {
			
			open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int valuesRowCount= doctable.get(1).size() ;
		
		headerRow = doctable.get(0) ; 
		valuestable = (ArrayList<ArrayList<?>>) doctable.get(1) ;
		
		if (valuestable.size()==0){
			throw(new Exception("Values table after performing <RGraph> is unexpectedly empty."));
		}
		
		indexCols = getIndexColumns( headerRow , 
									 valuestable.get(0), 
									 new String[]{"id","index","ids","indices"});
		
        int vcix = getFirstValueColumn( headerRow, "001");

		IciqlHandler iciqlHandler  ;
		iciqlHandler = new IciqlHandler(  );
		
		
		for(int i=0;i<valuesRowCount;i++){
			row = valuestable.get(i) ;	
			
			iciContext = new Contexts() ;
			iciqlHandler.setContextsObj(iciContext);
			
			iciqlHandler.clearContext(iciContext);
			
			iciqlHandler.setContextArgument( "entityid", 0L); // enumeration of entity in plain text 
			iciqlHandler.setContextArgument( "entitytype", RawEntityPosition._ENTITY_WORD ); // sentences ...

			iciqlHandler.setContextArgument( "version", ""); 
			
			for (int c=0;c<row.size();c++){
				
				obj = row.get(c);
				cn = obj.getClass().getName().replace("java.lang.", "").toLowerCase() ;
				
				// check value of "frequency" ...TexxDataBase in doctable, and in c.object
				if (c>=vcix){
					numval = (Double)obj;
					 
					
					iciqlHandler.setContextProfileValue( c, vcix, numval);
					continue;
				}
				if (cn.contains("string")){
					strval = (String)obj;
					iciqlHandler.setContextArgument( (String)headerRow.get(c), strval);
				}
				if (cn.contains("double")){
					if (indexCols.indexOf(c)>=0){
						v = (Double)obj;
						index = Math.round(v);
						 
						iciqlHandler.setContextArgument( (String)headerRow.get(c), index);
					}else{
						
						numval = (Double)obj;
						iciqlHandler.setContextArgument( (String)headerRow.get(c), numval);
					}
				}
				if (cn.contains("int")){
					v = (Double)obj;
					int intval = (int) Math.round(v);
					iciqlHandler.setContextArgument( (String)headerRow.get(c), intval);
				}
			}// -> all cells of row
			
			iciContext = iciqlHandler.finalizeIciql();
			// send it to the db...
			
			
			long dbKey = iciDb.insertAndGetKey( iciContext );
			// ....................
			
		} // i->
		

		
		try {
			connection.close() ;
		} catch (Exception e) {  }
		
	}

	 

	private int getFirstValueColumn(ArrayList<?> strs, String endsnip) {
		
		int cix =-1;
		String str;
		
		for (int i=0;i<strs.size();i++){
						
			str = (String)strs.get(i) ;
			if ((str.endsWith(endsnip)) &&  (str.contains("_"))){
				cix = i;
				break;
			}
		}
		
		return cix;
	}

	private ArrayList<Integer> getIndexColumns(ArrayList<?> strs, ArrayList<?> numvalues, String[] indicators) {
		
		ArrayList<Integer> ixcols = new ArrayList<Integer>();
		String str;
		Object obj;
		String  cn="";
		
		
		for (int c=0;c<indicators.length;c++){

			for (int i=0;i<strs.size();i++){
				
				obj = numvalues.get(i);
				cn = obj.getClass().getName().replace("java.lang.", "").toLowerCase() ;
				
				str = (String)strs.get(i) ;
				
				if ((cn.contains("string")==false) && (str.contains( indicators[c] ))){
					ixcols.add(i) ;
				}
				
			}// i->
			str="";
		} // c->
		
		return ixcols;
	}
	
	
	
}











