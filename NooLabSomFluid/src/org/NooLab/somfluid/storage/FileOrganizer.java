package org.NooLab.somfluid.storage;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.clapp.SomAppProperties;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.strings.StringsUtil;



public class FileOrganizer {

	public static final int _DATAOBJECT  = 1;
	public static final int _TABLEOBJECT = 2;
	public static final int _TRANSFORMER = 5;
	public static final int _SOMMODELER  = 8;
	public static final int _SPRITE      = 11;
	
	
	public static final String _PACKAGE_PREFIX = "somfluid-";
	public static final String _TMPDIR_PREFIX  = "~noo-sf-tmp-";
	
	
	// ....................................................
	
	SomFluidAppGeneralPropertiesIntf sfProperties;
	PersistenceSettings persistenceSettings;
	
	String rootDir="", projectDirName="", projectBaseDir="";
	
	
	PrintLog out = new PrintLog(2,true) ;
	StringsUtil strgutil = new StringsUtil();
	DFutils fileutil = new DFutils();
	
	// ========================================================================
	public FileOrganizer( ){
		
		 
		/*
		rootDir = persistenceSettings.getPathToSomFluidSystemRootDir(); // "D:/data/projects/"
		projectDirName = persistenceSettings.getProjectName(); // "bank2", defines sub dir 
		
		projectBaseDir = DFutils.createPath( rootDir, projectDirName+"/") ;
		*/
	}
	// ========================================================================	

	public void setPropertiesBase(SomFluidAppGeneralPropertiesIntf properties){
		
		sfProperties = properties;
		if (sfProperties.getPersistenceSettings()!=null){
			persistenceSettings = sfProperties.getPersistenceSettings();
		}
		update();
		
	}
	
	public void setPropertiesBase(SomAppProperties properties) {
		
		sfProperties = properties.getPropertiesConnection();
		persistenceSettings = properties.getPersistenceSettings();
		update();
		
	}

	public void update(){
		
		rootDir = persistenceSettings.getPathToSomFluidSystemRootDir(); // "D:/data/projects/"
		projectDirName = persistenceSettings.getProjectName(); // "bank2", defines sub dir 
		
		if (projectDirName.length()>0){
			projectBaseDir = DFutils.createPath( rootDir, projectDirName+"/") ;
		}
	}
	
	
	
	public void careForArchive(int moduleID, String filepath) {
		// if the file already exists
		
	}

	private void ensureDirectoryStructure(){
		/*
		      [root]
		              [project 1 base]
		              [project 2 base]
		                     ...
		              [project n base]
		                                 [_archive]
		                                 [data]
		                                          [raw]
		                                          [prepared]
		                                 [export]
		                                          [~version-1.123]   packages for model application, it is without data !! 
		                                                             the original som or transformer can NOT be reconstructed from this
		                                                              
		                                 [model]
		                                          [out]              diagnostic files, result files as defined in OutSettings
		                                              
		                                          [som]              the persistent Som 
		                                             .sfs 
		                                          [sprite]
		                                             .sfe 
		                                          [transform]
		                                             .sft
		                                 [tmp]
		  
		 */
		
	}
	
	public String getFileExtension(int moduleID) {
		
		String ext = ".sf";
		
		if (moduleID == _TRANSFORMER){
			ext = ".sft";
		}
		if (moduleID == _SOMMODELER){  
			ext = ".sfs";
		}
		if (moduleID == _SPRITE){
			ext = ".sff";
		}
		if (moduleID == _TABLEOBJECT){
			ext = ".sdt";
		}
		if (moduleID == _DATAOBJECT){
			ext = ".sdo";
		}
		
		return ext;
	}
	
	
	/**
	 * 
	 * @param srctype  0=raw, 1=prepared
	 * @return
	 */
	public String getDataDir(int srctype, int subVersion) {
		String dir="";
		
		if (srctype>=1){
			dir = DFutils.createPath( projectBaseDir, "data/prepared/"+subVersion+"/") ;
		}else{
			dir = DFutils.createPath( projectBaseDir, "data/raw/") ;
		}
		
		return dir;
	}

	public String getObjectStoreDir() {

		String dir="";
		
		dir = DFutils.createPath( projectBaseDir, "model/obj/") ;
		
		return dir;
	}

	public String getTransformerDir(){
		
		return getTransformerDir("");
	}
	
	public String getTransformerDir(String subVersion){
		String dir="";
		
		dir = DFutils.createPath( projectBaseDir, "model/transform/"+subVersion+"/") ;
		
		return dir;
	}
	
	
	public String getSomStoreDir() {
		return getSomStoreDir("auto") ;
	}
	
	public String getSomStoreDir(String context) {
		String dir="";
		
		dir = DFutils.createPath( projectBaseDir, "model/som/"+context+"/" ) ;
		
		return dir;
	}

	public String getModelerDir(){
		
		return getModelerDir("");
	}
	
	public String getModelerDir(String subVersion){
		String dir="";
		
		dir = DFutils.createPath( projectBaseDir, "model/som/"+subVersion+"/") ;
		
		return dir;
	}
	
	
	public String getSpriteDir(){
		
		return getSpriteDir("");
	}
	
	public String getSpriteDir(String subVersion){
		String dir="";
		
		dir = DFutils.createPath( projectBaseDir, "model/sprite/"+subVersion+"/") ;
		
		return dir;
	}
	
	// ........................................................................
	
	public String getRootDir() {
		return rootDir;
	}

	public String getProjectDirName() {
		return projectDirName;
	}

	public String getProjectBaseDir() {
		return projectBaseDir;
	}

	public StringsUtil getStrgutil() {
		return strgutil;
	}

	public DFutils getFileutil() {
		return fileutil;
	}
 
	public SomFluidProperties getSfProperties() {
		return sfProperties.getSelfReference();
	}

	public PersistenceSettings getPersistenceSettings() {
		return persistenceSettings;
	}

	public PrintLog getOut() {
		return out;
	}
	
	
	
}
