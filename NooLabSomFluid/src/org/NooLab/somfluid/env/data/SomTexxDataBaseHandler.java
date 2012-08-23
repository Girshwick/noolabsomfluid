package org.NooLab.somfluid.env.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import org.NooLab.itexx.storage.DbConnector;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.properties.PersistenceSettings;
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
public class SomTexxDataBaseHandler {

	SomDataObject somData;
	
	
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
 
	
	String user = "";
	String password = "" ;
	
	Class parent;
	String databaseFile="";
	
	DFutils fileutil = new DFutils();
	PrintLog out = new PrintLog(2,false);

	
	StringedObjects strObj = new StringedObjects();
	protected String internalCfgStoreName;
	
	// ========================================================================
	public SomTexxDataBaseHandler(SomDataObject somdataobj){
		somData = somdataobj;
	}
	// ========================================================================
	
}
