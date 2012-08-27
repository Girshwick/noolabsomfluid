package org.NooLab.somfluid.app;

import org.NooLab.itexx.storage.TexxDataBaseSettings;
import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.AlgorithmDeclarationsLoader;
import org.NooLab.somfluid.env.data.db.DataBaseAccessDefinition;
import org.NooLab.somfluid.properties.SomFluidSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.net.GUID;

public abstract class SomFluidAppPropertiesAbstract
											implements 
														SomFluidAppGeneralPropertiesIntf{

	
	
	protected transient static SettingsTransporter settingsTransporter;
	protected transient AlgorithmDeclarationsLoader algoDeclarations;
	SomFluidPluginSettings pluginSettings = new SomFluidPluginSettings();

	transient protected SomFluidFactory sfFactory ;
	
	/** concerns persistence of objects, including the models exported for "offline" use */
	protected PersistenceSettings persistenceSettings  ;
	
	/** concerns results */
	protected OutputSettings outputSettings ; 
	protected ModelingSettings modelingSettings = new ModelingSettings() ;
	 
	
	protected SomFluidSettings somfluidSettings ;
	protected TexxDataBaseSettings databaseSettings;
	
	protected int somInstanceType = 0 ; // SOM astor
	protected int somGridType = -1;  // fixed fluid
	
	protected int glueType = 0;
	protected int multiProcessingLevel = 0;
	
	// type of data source for active access:
	// 1=file, 2=db, 3=serialized SomdataObject
	protected int sourceType = -1;
	protected String dataSrcFilename = "";
	
	protected String dbDefinitionResource="";
	protected DataBaseAccessDefinition dbAccessDefinition = new DataBaseAccessDefinition ();
	
	
	protected String supervisedDirectory = "";
	
	
	transient protected FileOrganizer fileOrganizer;
	transient DFutils fileutil = new DFutils();
	private SomFluidProperties sfProperties;
	
	// ========================================================================
	public SomFluidAppPropertiesAbstract(){
		
		FileOrganizer fileorg = new FileOrganizer ();
		
	}
	// ========================================================================

	@Override
	public FileOrganizer getFileOrganizer() {
		 
		return fileOrganizer;
	}
	
	public void setFactoryParent(SomFluidFactory factory) {
		sfFactory = factory;
		sfProperties = sfFactory.getSfProperties();
	
	}

	
	@Override
	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}

	public void setFileOrganizer(FileOrganizer forg) {
		fileOrganizer = forg;
	}

	@Override
	public SomFluidPluginSettings getPluginSettings() {
		return pluginSettings;
	}
	
	@Override
	public void setPluginSettings(SomFluidPluginSettings pluginsettings) {
		this.pluginSettings = pluginsettings;
	}

	@Override
	public PersistenceSettings getPersistenceSettings() {
		return persistenceSettings;
	}

	public void setPersistenceSettings(PersistenceSettings persistencesettings) {
		persistenceSettings = persistencesettings;
	}
	

	@Override
	public int getSourceType() {
	 
		return sourceType;
	}
	 

	public void setSourceType(int sourcetype) {
		sourceType = sourcetype;
	}
	
	
	
	
	
	/**
	 * 
	 * 
	 * @param sourceType 1=full data file, 5=profiles for simulation
	 * @param filename
	 */
	public void setDataSource(int sourcetype, String locatorname) {
		// 
		sourceType = sourcetype;
		dataSrcFilename = locatorname ; 
	}

	@Override
	public String getDataSrcFilename() {
		return dataSrcFilename;
	}
	
	public String getDataSourceFilename() {
		return dataSrcFilename;
	}
	

	@Override
	public boolean addDataSource(int sourceType, String filename) {
		dataSrcFilename = filename;
		return true;
	}

	@Override
	public void setDataSrcFilename(String filename) {
		dataSrcFilename = filename;
	}
	
	public void setDataSourceFile(String filename) {
		dataSrcFilename = filename;
	}
	
	
	@Override
	public boolean isExtendingDataSourceEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	abstract public Object getCollectibleColumn() ;
	
	
	


	/**
	 * it returns a perspective to SomFluidProperties
	 * this requires first to provide the reference to the SomFluidFactory !!
	 */
	@Override
	public SomFluidProperties getSelfReference() {
		return sfProperties;
	}

	@Override
	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}



	public DataBaseAccessDefinition getDbAccessDefinition() {
		return dbAccessDefinition;
	}

	@Override
	public AlgorithmDeclarationsLoader getAlgoDeclarations() {
		return algoDeclarations;
	}
	
	public void setOutputSettings(OutputSettings outputsettings) {
		this.outputSettings = outputsettings;
	}


	/**
	 * 
	 * @param dirname if null or empty, a tmpdir within java temp will be created
	 * @return for provided custom dir success = 0, java temp = 1, if failed = -1
	 */
	public int setSupervisedDirectory( String basedirname, String projectname, String subfolder ) {
		int result = -1;
		
		String dirname=basedirname;
		
		
		if ((subfolder==null) || (subfolder.length()==0)){
			subfolder = "service";
		}
		
		if ((dirname==null) || (dirname.length()==0)){
			String str = GUID.randomvalue().replace("-", "").substring(0,10) ;
			dirname = fileutil.getTempFileJava("~noo-classifiers-"+str) ;
			dirname = fileutil.createpath( dirname, subfolder);
			supervisedDirectory = dirname;
			return 1;
		}
		
		
		dirname = fileutil.createpath( basedirname, projectname);
		dirname = fileutil.createpath( dirname, subfolder+"/");
		
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

	@Override
	public int getMultiProcessingLevel() {
		return multiProcessingLevel;
	}

	@Override
	public int getShowSomProgressMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSomType() {
	
		return 0;
	}

	public int getSomGridType() {
		return somGridType;
	}

	public void setSomGridType(int somGridType) {
		this.somGridType = somGridType;
	}
	
	
}
