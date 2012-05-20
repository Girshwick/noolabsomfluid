package org.NooLab.somfluid.app;

import java.io.Serializable;
import java.util.ArrayList;


import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.net.GUID;



/**
 * 
 * 
 * 
 *
 */
public class SomAppProperties implements 	SomAppPropertiesIntf,
											Serializable{

	private static final long serialVersionUID = 8129200314108478416L;


	public static final int _WORKINGMODE_PROJECT     = 1 ;
	public static final int _WORKINGMODE_SERVICEFLOW = 4 ;
	public static final int _WORKINGMODE_SERVICEFILE = 5 ;

	static SomAppProperties sclappProperties ;

	transient SomFluidFactory sfFactory ;
	
	transient SomFluidProperties sfProperties ; // better as a restricted interface, we do not need all settings for the classification app
	
	SomFluidPluginSettings pluginSettings = new SomFluidPluginSettings();
	
	
	private String algorithmsConfigPath;


	private PersistenceSettings persistenceSettings;


	private int glueType;


	private FileOrganizer fileOrganizer;


	private boolean pluginsAllowed;
	
	int workingMode = _WORKINGMODE_PROJECT;

	
	String dataSourceFilename = "";

	String supervisedDirectory = "";
	String baseModelFolder = "";
	String modelName = "";
	String activeModel;
	String modelPackageName;
	
	
	ArrayList<String> activeModels = new ArrayList<String> ();
	
	ArrayList<String> supervisionFilenameFilters = new ArrayList<String> ();
	
	transient DFutils fileutil = new DFutils();
	
	// ========================================================================
	public SomAppProperties(String sourceForProperties){
		
	}
	// ========================================================================	
	
	public static SomAppProperties getInstance(String sourceForProperties) {
		
		sclappProperties = new SomAppProperties(sourceForProperties) ;
		
		return sclappProperties;
	}

	public void connectGeneralProperties() {
		
		sfProperties = new SomFluidProperties(this);
		
		sfProperties.setPluginSettings( pluginSettings ) ;
		
		persistenceSettings = sfProperties.getPersistenceSettings();
		
	}

	public SomFluidProperties getPropertiesConnection() {
		 
		return sfProperties;
	}

	public void setInstanceType(int glueinstanceType) {
		glueType = glueinstanceType;
		
		
		fileOrganizer = new FileOrganizer() ;  
		persistenceSettings = new PersistenceSettings(fileOrganizer);
		OutputSettings outputSettings = new OutputSettings(persistenceSettings);
		
		fileOrganizer.setPropertiesBase(this);
		
		sfProperties.setInstanceType(glueinstanceType) ;
		sfProperties.setFileOrganizer(fileOrganizer) ;
		sfProperties.setPersistenceSettings(persistenceSettings) ;
		sfProperties.setOutputSettings(outputSettings) ;
	}

	public void setFactoryParent(SomFluidFactory factory) {
		sfFactory = factory;
	}

	public PersistenceSettings getPersistenceSettings() {
		 
		return persistenceSettings ;
	}

	public SomFluidPluginSettings getPluginSettings() {
		 
		return pluginSettings;
	}

	public void setAlgorithmsConfigPath(String pathstring) {
		// 
		
		String ps ;
		ps = DFutils.createPath( pathstring,"/");

		if (pathstring.endsWith("/")==false){
			pathstring = pathstring+"/" ; 
		}

		algorithmsConfigPath = pathstring;
		sfProperties.setAlgorithmsConfigPath( algorithmsConfigPath );
	}


	public String getAlgorithmsConfigPath() {
		return algorithmsConfigPath;
	}

	public void setPluginsAllowed(boolean flag) {
		// TODO Auto-generated method stub
		sfProperties.setPluginsAllowed(flag);
		pluginsAllowed = flag;
	}

	public boolean isPluginsAllowed() {
		return pluginsAllowed;
	}

	public int getGlueType() {
		return glueType;
	}

	public void setGlueType(int glueType) {
		this.glueType = glueType;
	}

	public void setWorkingMode(int mode) {
		workingMode = mode;
	}

	public int getWorkingMode() {
		return workingMode;
	}

	public void setDataSourceFile(String filename) {
		dataSourceFilename = filename;
	}

	public String getDataSourceFilename() {
		return dataSourceFilename;
	}

	public void setDataSourceFilename(String dataSourceFilename) {
		this.dataSourceFilename = dataSourceFilename;
	}

	/**
	 * 
	 * @param dirname if null or empty, a tmpdir within java temp will be created
	 * @return for provided custom dir success = 0, java temp = 1, if failed = -1
	 */
	public int setSupervisedDirectory( String dirname ) {
		int result = -1;
		
		if ((dirname==null) || (dirname.length()==0)){
			String str = GUID.randomvalue().replace("-", "").substring(0,10) ;
			dirname = fileutil.getTempFileJava("~noo-classifiers-"+str) ;
			supervisedDirectory = dirname;
			return 1;
		}
		if (fileutil.fileexists(dirname)==false){
			fileutil.createDir(dirname) ;
			if (fileutil.fileexists(dirname)){
				result = 0;
			}
		}else{
			result = 0;
		}
		
		supervisedDirectory = dirname;
		
		return result;
	}

	public String getSupervisedDirectory() {
		return supervisedDirectory;
	}

	public void addSupervisedFilename(String namefilter) {
		supervisionFilenameFilters.add( namefilter );
	}

	public ArrayList<String> getSupervisionFilenameFilters() {
		return supervisionFilenameFilters;
	}

	public void setSupervisionFilenameFilters(ArrayList<String> filenameFilterList) {
		this.supervisionFilenameFilters = filenameFilterList;
	}

	public void clearSupervisionFilenameFilter(String namefilterForRemoval) {
		
	}
	public void clearSupervisionFilenameFilter() {
		supervisionFilenameFilters.clear();
	}

	public void setBaseModelFolder(String baseFolder) {
		
		baseModelFolder = baseFolder.trim();
		
		if (baseModelFolder.endsWith("/")==false ){
			baseModelFolder = baseModelFolder+"/" ;
		}
		fileutil.createDir(baseFolder);
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getBaseModelFolder() {
		return baseModelFolder;
	}

	public void setActiveModel(String modelname) {
		
		if (modelname.trim().length()==0){
			return;
		}
		activeModel = modelname.trim();
		
		activeModels.clear();
		activeModels.add(activeModel);
	}
	public void addActiveModel(String modelname) {
		if (modelname.trim().length()==0){
			return;
		}
		if (activeModels.indexOf(modelname.trim())>=0){
			activeModel = modelname.trim();
			return;
		}
		activeModels.add(activeModel);
	}

	public ArrayList<String> getActiveModels() {
		return activeModels;
	}

	public void setActiveModels(ArrayList<String> activeModels) {
		this.activeModels = activeModels;
	}

	public String getActiveModel() {
		return activeModel;
	}

	public void setModelPackageName(String packageName) {
		modelPackageName = packageName;
	}

	public String getModelPackageName() {
		return modelPackageName;
	}
	
}
