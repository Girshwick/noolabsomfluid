package org.NooLab.somfluid.app.astor.storage.db;

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
import org.math.array.StatisticSample;

import com.iciql.Db;


import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.PathFinder;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.net.GUID;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.resources.ResourceContent;
import org.NooLab.utilities.resources.ResourceLoader;
import org.NooLab.utilities.strings.ArrUtilities;



import org.NooLab.astor.storage.iciql.NodeContent;
import org.NooLab.astor.storage.iciql.NodeFingerprints;
import org.NooLab.astor.storage.iciql.SomNames;

import org.NooLab.itexx.storage.ConnectorClientIntf;
import org.NooLab.itexx.storage.DataBaseCreateCommand;
import org.NooLab.itexx.storage.DataBaseMaintenance;
import org.NooLab.itexx.storage.DatabaseMgmt;
import org.NooLab.itexx.storage.DbConnector;
import org.NooLab.itexx.storage.DbLogin;
import org.NooLab.itexx.storage.DbUser;
import org.NooLab.itexx.storage.MetaData;
import org.NooLab.itexx.storage.docserver.storage.db.iciql.Documents;
import org.NooLab.itexx.storage.docserver.storage.db.iciql.Folders;
import org.NooLab.itexx.storage.docsom.iciql.Randomdocuments;
import org.NooLab.itexx.storage.randomwords.iciql.Contexts;


import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.astor.AstorProperties;
import org.NooLab.somfluid.app.astor.util.FingerPrint;
import org.NooLab.somfluid.app.up.IniProperties;
import org.NooLab.somfluid.env.data.SomTexxDataBase;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.structures.randomgraph.RawEntityPosition;


/**
 * 
 * 
 *
 */
public class AstorDataBase implements ConnectorClientIntf{

	String cfgResourceJarPath = "";
	// in factory, it is set to persistence settings, from where we fetch it here
	// "org/NooLab/texx/resources/sql/" ; // trailing / needed !!   
	
	AstorProperties astorProperties ;
	private SomFluidProperties sfProperties;
	PersistenceSettings ps;
	
	private boolean dbFileExists;
	private boolean isOpen;
	boolean dbRecreated = false;
	
	 
	String databaseName = "";
	String configFile = "";
	String storageDir = "" , h2Dir=""; 
	int _DB_TARGET_LOCATING=0;
	
	String databaseUrl="";

	String accessMode = "tcp"; // "http" = external server via 8082, "file" 
	Server server;
	Connection connection;
	Db iciDb;
	DatabaseMetaData jdbMetaData ;
	String dbCatalog ;
	MetaData metaData ; 
	
	DbConnector dbConnector ;
	DataBaseHandler dbHandler ;

	DataBaseMaintenance dataBaseBasics;
	
	protected String internalCfgStoreName;
	

	String user = "";
	String password = "" ;
	
	Class parent;
	String databaseFile="";
	
	
	StatisticSample sampler ;
	DFutils fileutil = new DFutils();
	PrintLog out = new PrintLog(2,false);
	StringedObjects strObj = new StringedObjects();
	private SomTexxDataBase randomWordsDb;

	private boolean prepareFingerprints=true;
	
	// ========================================================================
	public AstorDataBase( AstorProperties astorProps){
		
		astorProperties = astorProps;
		
		sfProperties = astorProperties.getSomFluidProperties() ; 
		
		ps = sfProperties.getPersistenceSettings();
		
		// sth like "rg-fingerprints";
		// databaseName =  ps.getDatabaseName();
		user = ps.getDbUser() ;
		password = ps.getDbpassword() ;
		
		String cfgJarPath = ps.getConfigSqlResourceJarPath() ;
		if ((cfgResourceJarPath.length()==0) && (cfgJarPath!=null) && (cfgJarPath.length()>0)){
			cfgResourceJarPath = cfgJarPath ;
		}
		
		internalCfgStoreName = ps.getInternalSqlCfgStoreName(); 
		if (internalCfgStoreName.length()==0){
			internalCfgStoreName = "create-db-sql-xml" ;
		}
		
		// String configFile = databaseName+".h2.db-config.xml";
		
		
		getStorageDir();
		
		dbConnector = new DbConnector( (ConnectorClientIntf)this);

		sampler = new StatisticSample(172838);
		
		dataBaseBasics = new DataBaseMaintenance( (ConnectorClientIntf)this );
		
		out = dataBaseBasics.getOut() ;
		out.setPrefix("[ASTOR-DB]");
	}

	public AstorDataBase(){

		out.setPrefix("[ASTOR-RG-DB]");
	}

	// ========================================================================
	
	public boolean dbFileExists(){
		return dbFileExists;
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	
	public boolean prepareDatabase( String expected, int keepOpenAfterPrepare, boolean prepareFingerprints ) throws Exception {
		boolean rB=false;
		String appnameShortStr;
		 
		try {
			
			this.prepareFingerprints = prepareFingerprints;
			// remove any lock 
			if (connection==null){
				dataBaseBasics.removeLock(connection , storageDir);
				dataBaseBasics.removeLock(connection , getRelocatedH2Dir(storageDir) );
			}else{
				return true;
			}
			
			if ((databaseName==null) || (databaseName.length()==0)){
				
				
				//             e.g. resourceName = "create-db-sql-xml" ;
				String xmlstr;
				
				xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, internalCfgStoreName ) ;
				
				appnameShortStr = sfProperties.getPersistenceSettings().getAppNameShortStr() ;
				
				dbHandler = new DataBaseHandler( this ) ;
			
				databaseName = dbHandler.getDataBaseNameFromResource( xmlstr,  appnameShortStr, expected ) ;
			} // this would be empty if no match is found, if expected is the full name without wildcards,
			  // it will return the expected name,else the found one
			
			
			int servermodeFlag=0;
			if (accessMode.contentEquals("tcp")){
				// if in server mode
				// String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_PROJECT) ;// _BASEDIR_QUERY_PROJECT
				// h2Dir = DFutils.createPath(h2Dir, "storage/") ;
				if (sfProperties.isITexxContext()==false){
					getRelocatedH2Dir(storageDir);
				}
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
			// String h2Dir = DatabaseMgmt.setH2BaseDir(storageDir, DatabaseMgmt._BASEDIR_QUERY_PROJECT) ;
			// h2Dir = DFutils.createPath(h2Dir, "storage/") ;
			if (sfProperties.isITexxContext()==false){
				getRelocatedH2Dir(storageDir) ;
			}
			databaseFile = connect( databaseName, h2Dir, servermodeFlag ) ;
			// ??? reported url is empty....
			
			
			rB = databaseFile.length()>0;
			
			if (rB){
				
				dbHandler = new DataBaseHandler( this ) ;
		
		// returns falsely false for randomwords		, for astornodes
				if ((checkStructure(connection,databaseName) != 0) || (dbRecreated)){ 
					// connection.close();
					createDatabaseStructure( databaseName ) ;// usually, it should be taken from the XML
					
				}else{
					metaData = dbHandler.getMetadata();
					metaData.retrieveMetaData();
					
					String str = ArrUtilities.arr2Text( metaData.getTableNames(1),";");

					out.printErr(2, "Tables : "+str) ;
				}
				
			}
			
			if ((keepOpenAfterPrepare>=1) && (connection!=null) && (connection.isClosed()==false)){
				connection.close() ;
			}
			
		} catch (FileNotFoundException e) {
			rB=false;
			e.printStackTrace();
		}
		 
		return rB;
	}

	//             e.g. resourceName = "create-db-sql-xml" ;
	
	public String getRelocatedH2Dir(String storageDir){
		
		String _h2Dir;
		try {
			
			if ((h2Dir.length()==0) || (DFutils.folderExists(h2Dir)==false) || 
					(h2Dir.contentEquals(storageDir))){
				_h2Dir = DatabaseMgmt.setH2BaseDir( storageDir, _DB_TARGET_LOCATING);// DatabaseMgmt._BASEDIR_QUERY_PROJECT);
				h2Dir = DFutils.createPath( _h2Dir, "storage/") ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return h2Dir;
	}

	
	private void createDatabaseStructure(String dbname) throws Exception {
		// loading the  "create-db-sql-xml"  
		// from package "org.NooLab.docserver.resources.sql"
		boolean rB;
		String appshortname,xmlstr = "" ;
		int r=-1 ;

		xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, internalCfgStoreName ) ;
		// xmlstr = getConfigResource( internalCfgStoreName); // "create-db-sql-xml");
		
		connect( databaseName, getRelocatedH2Dir(storageDir) ) ;
		
		// get the create statements
		appshortname = this.astorProperties.getPersistenceSettings().getAppNameShortStr() ;
		ArrayList<DataBaseCreateCommand> ccs = dbHandler.getCreateFromResource( xmlstr, appshortname, dbname ); 
		
		// executing the statements
		r = dbHandler.createDataTablesByResource(ccs);
		
		if (r!=0){
			throw(new Exception("createDataTablesByResource() failed, error code = "+r+"\n"));
		}
		
		if ((connection==null) || (connection.isClosed())){
			String _h2Dir = h2Dir;
			if (_h2Dir.endsWith("/")){
				_h2Dir=_h2Dir.substring(0,_h2Dir.length()-1);
			}
			
			String url = 	"jdbc:h2:tcp://localhost/"+_h2Dir+"/"+dbname; 
			
			DbLogin login = new DbLogin(user,password) ; 
			connection = dbConnector.getConnection(url, login);
			
		}
		
		dataBaseBasics.createDbUsers(ccs,connection);
		
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
			
			xmlRoot = this.astorProperties.getPersistenceSettings().getAppNameShortStr();
			xMsg.setContentRoot(xmlRoot) ;
			
			nodeObjs = xMsg.getNodeList(xmlstr, "/"+xmlRoot+"/database", "table");
			
			appshortname = this.astorProperties.getPersistenceSettings().getAppNameShortStr() ;
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

			if (dataBaseBasics!=null){
				dataBaseBasics.disconnect(connection);
			}
			
			jdbMetaData = null;
			dbCatalog = "";
			
			
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
			out.printErr(1, "Daatbase has not been properly instantiated.");
		}
		dbHandler.setiDb( iciDb ) ;

		// creates a new instance for new MetaData and retrieves the latest info again
		dbHandler.updateMetaData(c) ; 
	
	}
	
	
	public void open(Connection c) throws Exception{
		
		try{
			
			if (connection.isClosed()){
				
				connect(this.databaseName, getRelocatedH2Dir(storageDir));
				c=connection;
			}
			iciDb = Db.open(c);
		}catch(Exception e){
			e.printStackTrace() ;
			out.printErr(1, "\n\nretry...");
			
			connection=null;
			prepareDatabase(databaseName,1,prepareFingerprints) ;
			
			iciDb = Db.open(connection);
			getJdbMeta(connection);
		}
		updateInfraStructure(c);
	}

	public void open() throws Exception{
		
		if (databaseUrl.length()==0){

			
			String _h2Dir = getRelocatedH2Dir(storageDir);
			
			if (_h2Dir.endsWith("/")){
				_h2Dir = _h2Dir.substring(0,_h2Dir.length()-1);
			}

			databaseUrl = 	"jdbc:h2:tcp://localhost/"+ _h2Dir+"/"+databaseName; // +";AUTO_SERVER=TRUE" ;

		}
			
		iciDb = Db.open(databaseUrl, user, password);
		connection = iciDb.getConnection();

		updateInfraStructure(connection);
	}

	  	
	public void startServer( String dbNamePattern ) { 
		
		dataBaseBasics.removeLock(connection , storageDir);
		dataBaseBasics.removeLock(connection , getRelocatedH2Dir(storageDir));

		try {
			prepareDatabase( dbNamePattern,1,prepareFingerprints );
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
							            "           Also check the installation of H2, or whether its serive is running at all.\n"+
							            "           Requested name was <"+dbname+">, "+
							            "           requested folder was "+storageDir+"\n\n")) ;
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
			String estr = e.getMessage();
			if (estr.contains("Address already in use:")){
				out.printlnErr(3, "Potential problem met in createServer(): "+ estr);
			}else{
				out.printlnErr(1, "Problem met in createServer(): "+ estr);
			}
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
			
			String _h2Dir = getRelocatedH2Dir(storageDir);
			
			if (_h2Dir.endsWith("/")){
				_h2Dir = _h2Dir.substring(0,_h2Dir.length()-1);
			}

			String url = 	"jdbc:h2:tcp://localhost/"+_h2Dir+"/"+dbname; // +";AUTO_SERVER=TRUE" ;
			
						
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
			}

			dbfile = DFutils.createPath(filepath,dbname+".h2.db") ;
			
			File fil = new File(dbfile);
			if (fil.exists()){
				if (connection.isClosed()==false){
					databaseUrl = url;
				}
				databaseUrl = url;
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
			
			System.err.println("Connecting to database <"+dbname+"> failed \n"+jx.getMessage() );
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
				
				connect( databaseName, getRelocatedH2Dir(storageDir)) ;
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

	public Connection getConnection(){
		
		return connection;
	}
	
	
	public String getStorageDir( ){
		/*
		String systemroot = sfProperties.getSystemRootDir();
		String prj = sfProperties.getPersistenceSettings().getProjectName() ;
		
		storageDir = DFutils.createPath(systemroot, prj+"/") ;
		storageDir = DFutils.createPath(storageDir, "storage/") ;
		
		return storageDir ; 
		*/
		String systemroot ,str;
		
		str = IniProperties.fluidSomProjectBasePath ;
		
		systemroot = sfProperties.getSystemRootDir();   // [path]/iTexx  
		
		if ((systemroot.length()==0) || (DFutils.folderExists(systemroot)==false)){
			systemroot = (new PathFinder()).getAppBinPath( this.getClass(), false);
			systemroot = DFutils.getParentDir(systemroot) ;
			sfProperties.setSystemRootDir(systemroot) ;
		}
		
		String appcontext = sfProperties.getApplicationContext() ; 
		if (appcontext.contains("itexx")){  // itexx (as service within iTexx) or "standalone" (via applet)
			storageDir = DFutils.createPath(systemroot, "storage/") ;
			_DB_TARGET_LOCATING = 0;
		}else{
			// the storage is subject of the project
			String prj = sfProperties.getPersistenceSettings().getProjectName() ;
			storageDir = DFutils.createPath(systemroot, prj+"/") ;
			storageDir = DFutils.createPath(storageDir, "storage/") ;
			
			_DB_TARGET_LOCATING = 0; // DatabaseMgmt._BASEDIR_QUERY_PROJECT;
		}
		out.print(2, "AstorSomDataBase::getStorageDir() : \n"+
				     "                   - systemroot : "+systemroot+"  \n"+
				     "                   - storage    : "+storageDir);
		
		return storageDir ;
		
		
	}
	
	private String getDBhomefile( String dbname){
		
		String filename="",filepath, user_home;
			
		getStorageDir();
		filepath = DFutils.createPath(storageDir, dbname);
		
		return filepath;
	}

	public AstorProperties getDocoProperties() {
		return astorProperties;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void setRandomWordsDb(SomTexxDataBase randomWordsDb) {
		// 
		this.randomWordsDb = randomWordsDb;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setInternalCfgStoreName(String fname) {
		internalCfgStoreName = fname;
	}

	public String getCfgResourceJarPath() {
		return cfgResourceJarPath;
	}

	public void setCfgResourceJarPath(String cfgResourceJarPath) {
		this.cfgResourceJarPath = cfgResourceJarPath;
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

	public DataBaseHandler getDbHandler() {
		return dbHandler;
	}

	public String getDatabaseFile() {
		return databaseFile;
	}

	public PrintLog getOut() {
		return out;
	}

	
	public int updateSomLatticRegistration(long somId, String nodecontentTable) {

		int result= -1;
		long dbKey, confirmedId = -1L;
		String fpString="" ;
		
		List<SomNames> sids=null;
		
		try{

			if (iciDb.getConnection().isClosed()){
				if (connection!=null){
					open(connection) ;
				}else{
					open();
				}
			}

			SomNames s = new SomNames();

			try{
				sids = iciDb.from(s).where(s.somid).is(somId).select() ;
			}catch(Exception e){
			}

			result = -3;
			if ((sids!=null) && (sids.size()>0)){
				s = sids.get(0) ;
				confirmedId = s.somid ;
				result=0;
			}
			
			if ((result== -3) || (sids==null) || (sids.size()==0)){
				
				if (nodecontentTable.length()==0){
					nodecontentTable = "nodecontent" ;
				}
				
				fpString = "";
				if (prepareFingerprints){
					// this is quite slow, so avoid it if possible
					fpString = (new FingerPrint(sampler)).createFingerprint( 20, 6,";") ; // will be added as prefix to the fingerprints of the nodes 
				}
				s.somid = somId;
				s.tablename = nodecontentTable;
				s.timestamp = System.currentTimeMillis() ;
				s.fingerprint = fpString ;
				
				dbKey = iciDb.insertAndGetKey( s );
				result= 0;
			} // insert...
			
			
		}catch(Exception e){
			result = -17;
			e.printStackTrace() ;
		}
		
		return result;
	}

	/** it returns the effective numGuid... </br>
	 *  this could differ from the requested one, if for the given somid 
	 *  the runindex already exists */
	public long[] updateLatticNodeRegistration( long somId, long numGuid, int runningIndex ) {
		return updateLatticNodeRegistration( somId, numGuid,runningIndex, -1.0, -1.0, -1.0) ;
	}
	
	/**
	 * this update of the node's registration is being called from /astor/trans/SomAstorNodeContent ;
	 * if the node-enum is known for the given som (identified by id) it will return the stored value 
	 * 
	 * @param somId
	 * @param numGuid
	 * @param runningIndex
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public long[] updateLatticNodeRegistration( long somId, long numGuid, int runningIndex, double x, double  y, double z) {


		long[] resultvalues = new long[]{-1,-1};
		long result = -1L,_max =10L;
		long dbKey, confirmedId = -1L;
		String fpString="" ;
		
		List<NodeFingerprints> nods=null;
		List<Object> indexes;
		
		try{
				
			if (iciDb.getConnection().isClosed()){
				iciDb = Db.open(databaseUrl, user, password);
				 
			}
			NodeFingerprints nfp = new NodeFingerprints();
	
			
			nods=null;
			
			if ((result == -3) || (nods==null) || (nods.size()==0)){
				try{
					nods = iciDb.from(nfp).where(nfp.somid).is(somId)
				                      	  .and(nfp.runindex).is(runningIndex).select() ;
					result = 0;
					if ((nods!=null) && (nods.size()>0)){
						nfp = nods.get(0) ;
						if ((nfp.numguid>=0) && (nfp.somid==somId) && (nfp.fingerprint.length()>10 )){
							dbKey = nfp.id;
							resultvalues[0] = dbKey;
							resultvalues[1] = nfp.numguid ; 
							return resultvalues;
						}
					}
				}catch(Exception e){
					result = -3;
				}
			}
			
			try{
				
				indexes = iciDb.from(nfp).select(nfp.id) ;
				if (indexes.size()==0){
					_max=1L;
				}else{
					int n = indexes.size();
					long lastvalue = (Long)indexes.get(n-1); 
					_max = lastvalue +1L;	
				}
				
				// nods = iciDb.from(nfp).where(nfp.somid).is(somId).and(nfp.numguid).is(numGuid).select() ;
				
				result = 0;
				
			}catch(Exception e){
				result = -3;
			}
			if ((result== -3) || (nods==null) || (nods.size()==0)){
				
				fpString = (new FingerPrint(sampler)).createFingerprint( 50, 6,";") ; // will be added as prefix to the fingerprints of the nodes 
				
				
				// iciDb.from(nfp).increment(nfp.id) ; -->> increments by 16 or even 24... so we do it by our own
				// yet we have to guarantee uniqueness of numguid
				
				
				try{
					long _numGuid = numGuid;
					while (_numGuid == numGuid){
						indexes = iciDb.from(nfp).where(nfp.numguid).is(numGuid).select(nfp.numguid) ;
						_numGuid = numGuid;
						if (indexes.size()>0){
							if (indexes.indexOf(numGuid)>=0){
								numGuid = SerialGuid.numericalValue() ;
							}else{
								break;
							}
						}else{
							break;
						}
					} // ->
					
				}catch(Exception e){
				
				}
				
				
				
				nfp.id = _max;
				nfp.numguid = numGuid;
				nfp.somid = somId;
				nfp.runindex = runningIndex ;
				nfp.locationx = x;
				nfp.locationy = y;
				nfp.locationz = z;
				nfp.fingerprint = fpString ;
				
				dbKey = iciDb.insertAndGetKey( nfp );
				
				resultvalues[0] = dbKey; // usually _max;
				resultvalues[1] = nfp.numguid ; 
			} // insert...
			
			
			// connection.close() ;
			// iciDb.close() ;
			
		}catch(Exception e){
			result = -17;
			// e.printStackTrace() ;
			String estr = "Problem in <updateLatticNodeRegistration()> for node index <"+runningIndex+"> in som id <"+somId+">...\n" + 
			              e.getMessage() ;
			out.printErr(2, estr) ;
		}
		
		return resultvalues;
		
	}

	public Randomdocuments getRandomDocEntryByRecId(Long rec) {
		
		Randomdocuments rdoc = new Randomdocuments();
		
		List<Randomdocuments> rds;
		Db iciDb;

		
		try{
			
			// is of type SomTexxDataBase, which coves both L1=contexts and L2=randomdocuments
			if (randomWordsDb==null){
				throw(new Exception("Database <randomwords> is not available, nodecontent for database <astornodes> can't be created.")) ;
			}
			iciDb = randomWordsDb.getIciDb() ;
			
			if (iciDb.getConnection().isClosed()){      

				iciDb = dataBaseBasics.iciOpenTolerant( databaseUrl, "sa","sa") ;
			}	
			// SELECT * FROM RANDOMDOCUMENTS where DOCID = 495;
			
			rds = iciDb.from(rdoc).where(rdoc.id).is((long)rec).select() ; // here we ask a table of structure astordocs
			
			if ((rds!=null) && (rds.size()>0)){
				rdoc = rds.get(0);
			}else{
				rdoc.clear();
			}

			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return rdoc;
	}
	
	public Contexts getContextsEntryByRecId(Long rec) {
		
		Contexts c = new Contexts();
		List<Contexts> cs;
		Db iciDb;
		
		try{
			
			if (randomWordsDb==null){
				throw(new Exception("Database <randomwords> is not available, nodecontent for database <astornodes> can't be created.")) ;
			}
			iciDb = randomWordsDb.getIciDb() ;
			
			if (iciDb.getConnection().isClosed()){      
				//<<<  wrong database here !!!! points to astornodes, but we need randomwords
				// 
				 
				// randomwords should provide a user "astor" !!! user, password);
				iciDb = dataBaseBasics.iciOpenTolerant( databaseUrl, "sa","sa") ;
				// a wrapper for:  iciDb = Db.open(databaseUrl, "sa","sa") ;
				// it retries to open 5 times
			}	
			// SELECT * FROM CONTEXTS where CONTEXTID = 34;
			
			cs = iciDb.from(c).where(c.contextid).is((long)rec).select() ;
			
			if ((cs!=null) && (cs.size()>0)){
				c = cs.get(0);
			}else{
				c.clear();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return c;
	}

	public long insertUpdateNodeContent( long somID, long nodeNumGuid, long docid, long contextId, long fpindex) {
		 
		long dbKey=-1L, result=-1L;
		long _max = 0L;
		
		
		NodeContent nc = new NodeContent();
		List<NodeContent> ncs;
		
		try{
			
			if (iciDb.getConnection().isClosed()){
				// iciDb = Db.open(databaseUrl, user, password);
				iciDb = dataBaseBasics.iciOpenTolerant( databaseUrl, user, password);
			}	
			
			// is it already contained?
			ncs = iciDb.from(nc).where(nc.somid).is(somID )
			                    .and(nc.nodeid).is(nodeNumGuid)
			                    .and(nc.docid).is(docid)
			                    .and(nc.contextid).is(contextId)
			                    .select() ;
			
			
			if ((ncs!=null) && (ncs.size()>0)){
				nc = ncs.get(0);
				
				if (nc.docid>=0){
					// update
				}
			}else{
				// not known....
				// our own increment of id :
				
				try{
					List<Object> indexes ;
					
					
					indexes = iciDb.from(nc).select(nc.id) ;
					if (indexes.size()==0){
						_max=1L;
					}else{
						int n = indexes.size();
						long lastvalue = (Long)indexes.get(n-1); 
						_max = lastvalue +1L;	
					}
					
					result = 0;
					
				}catch(Exception e){
					result = -3;
				}
				
				
				nc.id = _max;
				// now the data
				nc.somid = somID  ;
                nc.nodeid = nodeNumGuid  ;
                nc.docid = docid ;
                nc.contextid = contextId ;
				
                dbKey = iciDb.insertAndGetKey(nc);
                // iciDb.update(nc);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		 
		return dbKey;
	}
	
	
}











