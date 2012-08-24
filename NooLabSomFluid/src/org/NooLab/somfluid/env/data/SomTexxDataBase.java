package org.NooLab.somfluid.env.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.NooLab.itexx.storage.ConnectorClientIntf;
import org.NooLab.itexx.storage.DatabaseMgmt;
import org.NooLab.itexx.storage.DbConnector;
import org.NooLab.itexx.storage.DbLogin;
import org.NooLab.itexx.storage.MetaData;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.env.data.db.DataBaseAccessDefinition;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.objects.StringedObjects;

import org.h2.jdbc.JdbcSQLException;
import org.h2.server.Service;
import org.h2.tools.Server;
import com.iciql.Db;

/**
 * 
 * this prepares and organizes physical access to the database,
 * for Astor as well as for the higher-level SOMs
 * 
 * Architecturally, it is rooted (hosted) by SomDataObject 
 *
 */
public class SomTexxDataBase implements ConnectorClientIntf{

	SomDataObject somData;
	
	
	String cfgResourceJarPath = "";
	// in factory, it is set to persistence settings, from where we fetch it here
	// "org/NooLab/texx/resources/sql/" ; // trailing / needed !!   
	
	SomFluidProperties sfProperties ;
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
	DataBaseHandler dbHandler ;
 
	
	String user = "RG";
	String password = "rg" ;
	
	Class parent;
	String databaseFile="";
	
	DFutils fileutil = new DFutils();
	PrintLog out = new PrintLog(2,false);

	
	StringedObjects strObj = new StringedObjects();
	protected String internalCfgStoreName;


	private DataBaseAccessDefinition dbAccess;


	private TexxDataBaseSettingsIntf dbSettings;
 
	
	// ========================================================================
	public SomTexxDataBase(SomDataObject somdataobj, SomFluidProperties props){
		ArrayList<String> fieldlist;
		
		somData = somdataobj;
		sfProperties = props; 
		ps = sfProperties.getPersistenceSettings();
		
		
		dbAccess = sfProperties.getDbAccessDefinition() ;
		user = 	dbAccess.getUser() ;
		password = "rg" ;
		databaseName = dbAccess.getDatabaseName() ; // "randomwords";
		
		dbSettings = sfProperties.getDatabaseSettings() ;
		
		fieldlist = dbSettings.getTableFields() ;
		
		
		getStorageDir();
		
		dbConnector = new DbConnector( (ConnectorClientIntf)this);

		out.setPrefix("[ASTOR-DB]");

	}
	// ========================================================================

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

	// ....................................................
	
	
	

	
	public void updateInfraStructure(Connection c ) throws Exception{
	
		getJdbMeta(c);
		
		if (dbHandler==null){
			out.printErr(1, "Datbase has not been properly instantiated.");
		}
		dbHandler.setiDb( iciDb ) ;
	
		// creates a new instance for new MetaData and retrieves the latest info again
		dbHandler.updateMetaData(c) ; 
	
	}

	

	public boolean prepareDatabase( String expected) throws Exception {
		boolean rB=false;
		String appnameShortStr;
		 
		try {
			// remove any lock 
			if (connection==null){
				// removeLock();
			}else{
				return true;
			}
			
			if ((databaseName==null) || (databaseName.length()==0)){
				// exit;
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
			out.delay(300);
			databaseFile = connect( databaseName, h2Dir, servermodeFlag ) ;
			
			
			
			rB = databaseFile.length()>0;
			
			if (rB){
				
				dbHandler = new DataBaseHandler( this ) ;
		
		// returns falsely false for randomwords		
					metaData = dbHandler.getMetadata();
					metaData.retrieveMetaData();
					 
					String str = ArrUtilities.arr2Text( metaData.getTableNames(1),";");

					out.printErr(2, "Tables : "+str) ;
				
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

	
	public String getStorageDir( ){
		
		String systemroot = sfProperties.getSystemRootDir();
		String prj = sfProperties.getPersistenceSettings().getProjectName() ;
		
		storageDir = DFutils.createPath(systemroot, prj+"/") ;
		storageDir = DFutils.createPath(storageDir, "storage/") ;
		
		return storageDir ; 
	}
	
	
	public Connection getConnection() {
		 
		return connection;
	}

	public Db getIciDb() {
		 
		return iciDb;
	}

	public String getDatabaseFile() {
		 
		return databaseFile;
	}

	public String getDatabaseName() {
		 
		return databaseName;
	}

	public void setDbMetaData(DatabaseMetaData metadata) {
		
		jdbMetaData = metadata ;
	}
	
	protected void getJdbMeta(Connection c) throws SQLException{
		jdbMetaData = c.getMetaData();
		dbCatalog = c.getCatalog() ;
		
	}

	public ArrayList<String> retrieve(String string, ArrayList<String> requestFields, int i) {
		
		ArrayList<String> resultLines = new ArrayList<String>() ;
		String sql, fieldLabelsStr ; 
		
		fieldLabelsStr = somData.arrutil.arr2text( requestFields, ",");
		// defining the SQL statement, that we will issue, like so
		sql = "SELECT "+fieldLabelsStr+ " LIMIT 1000";
		// dealing with the result set, expanding the randomcontext content into List<Double> 
		
		
		return resultLines;
	}
	
 
	
}
