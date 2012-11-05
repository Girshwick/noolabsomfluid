package org.NooLab.somfluid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.itexx.storage.DataStreamProviderIntf;
import org.NooLab.itexx.storage.TexxDataBaseSettings;
import org.NooLab.itexx.storage.TexxDataBaseSettingsIntf;
import org.NooLab.somfluid.clapp.SomAppProperties;
import org.NooLab.somfluid.clapp.SomFluidAppPropertiesAbstract;
import org.NooLab.somfluid.components.AlgorithmDeclarationsLoader;
import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.core.engines.det.ResultRequests;

import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;

 
import org.NooLab.somfluid.properties.SomFluidSettings;
import org.NooLab.somfluid.properties.DataUseSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.somfluid.storage.ContainerStorageDevice;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.structures.InstanceProcessControlIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.objects.StringedObjects;
import org.NooLab.utilities.resources.ResourceContent;
import org.NooLab.utilities.resources.ResourceLoader;


					
public class SomFluidProperties 
									extends
												SomFluidAppPropertiesAbstract
									implements 	// 
												SomFluidAppGeneralPropertiesIntf,
												DataHandlingPropertiesIntf,
											// 
												LatticePropertiesIntf,
												Serializable{

	
	private static final long serialVersionUID = 62391334397417444L;

	
	transient static SomFluidProperties sfp; 
	
	public final static int _SRC_TYPE_DB   = DataStreamProviderIntf._DSP_SOURCE_DB ;  // = 1
	public final static int _SRC_TYPE_TCP  = DataStreamProviderIntf._DSP_SOURCE_TCP;  // = 2
	public final static int _SRC_TYPE_FILE = DataStreamProviderIntf._DSP_SOURCE_FILE ;// = 3
	
	public final static int _SRC_TYPE_OBJ  = 5;
	public final static int _SRC_TYPE_XSTREAM = 12;
	public final static int _SRC_TYPE_ONLINE  = 100;

	
	public static final int _SIM_NONE      = -1;
	public static final int _SIM_SURROGATE = 3;
	public static final int _SIM_PROFILES  = 5;
	
	public static final int _SOMDISPLAY_PROGRESS_NONE  = -1 ;
	public static final int _SOMDISPLAY_PROGRESS_BASIC =  0 ;
	public static final int _SOMDISPLAY_PROGRESS_STEPS =  1 ;
	public static final int _SOMDISPLAY_PROGRESS_PERC  =  2 ;


	public static final String _STORAGE_OBJ = "SomFluid.properties";
	public static final String _STORAGE_XML = "SomFluid-properties.xml";

 
		
	/** <0: don't load, 0: immediate uptake; >0:delayed uptake (in millis) */
	int dataUptakeControl = -1 ;
	
	// that is for _SOMTYPE_MONO only ! 
	
	DataUseSettings dataUseSettings = new DataUseSettings() ;
	transient VariableSettingsHandlerIntf variableSettings; 
	// TODO all getters and setters should be contained in interface... 
 
	
	// 
	
	// lattice
	int somType = -1; // mandatory 
	int initialNodeCount = -1;
	boolean messagingActive = true;
	boolean multithreadedProcesses=false;
	private int restrictionForSelectionSize = -1;

	boolean initializationOK = false;

	private boolean extendingDataSourceEnabled;

	ArrayList<String> absoluteFieldExclusions = new ArrayList<String>() ; 
	ArrayList<String> treatmentDesignVariables = new ArrayList<String>() ;
	ArrayList<String> groupDesignVariables = new ArrayList<String>() ;

	private int absoluteFieldExclusionsMode;
	
	private int collectibleColumn;
	
	
	private int showSomProgressMode;

	
	
	private boolean isPluginsAllowed = true;

	private String algorithmsConfigPath="";

	

	private String systemRootDir="";

	transient private String currentSettingsXml="";


	private boolean isAssignatesHomogeneous=true;


	private String onCompletion="";


	private int streamingActive;

	private int insertIdExtraColumn;
	private int astorInitializationRecordCount = 5000 ;
	private int astorInitializationDocCount = 10 ;

 
	// ========================================================================
	public SomFluidProperties(){
		super();
		
		somfluidSettings = new SomFluidSettings(this);
	 
		databaseSettings = new TexxDataBaseSettings( dbAccessDefinition );
		
	}
	// ========================================================================
	
	
	public SomFluidProperties(SomAppProperties appProperties) {
	  
		sfp = this;
		
	}


	public SomFluidProperties(SomFluidProperties sfPropsIn) {
		// 
		StringedObjects sob = new StringedObjects();
		sfp = (SomFluidProperties) sob.decode( sob.encode(sfPropsIn)) ;
		
		sfp.persistenceSettings = (PersistenceSettings) sob.decode( sob.encode(sfPropsIn.persistenceSettings)) ;
		sfp.databaseSettings = (TexxDataBaseSettings) sob.decode( sob.encode(sfPropsIn.databaseSettings)) ;
		sfp.modelingSettings = (ModelingSettings) sob.decode( sob.encode(sfPropsIn.modelingSettings)) ;
		
		applicationContext = sfPropsIn.applicationContext ;
		sfp.applicationContext = sfPropsIn.applicationContext ;
		
		this.systemRootDir = sfPropsIn.systemRootDir;
		sfp.systemRootDir = sfPropsIn.systemRootDir;
		
		persistenceSettings = sfp.persistenceSettings;
		databaseSettings = sfp.databaseSettings ;
		modelingSettings = sfp.modelingSettings; 
		
		getInstance("");
	}


	public static SomFluidProperties getInstance() {
		 
		return getInstance("");
	}
	/**   */
	public static SomFluidProperties getInstance( String xmlSettingsStack ) {
	 
		if (sfp==null){
			sfp = new SomFluidProperties();
		}
		
		if (xmlSettingsStack.length()>0){
			settingsTransporter = new SettingsTransporter( sfp );
			sfp = settingsTransporter.importProperties( xmlSettingsStack );
		}
		 
		return sfp;
	}
	
	@Override
	public SomFluidProperties getSelfReference() {
		return this;
	}
 
	
	public static SomFluidProperties getSfp() {
		return sfp;
	}


	public void importSettings(){
		
	}

	
	public String exportSettings(){
		String xmlSettingsStr = "";
		
		settingsTransporter = new SettingsTransporter( sfp );
		// target directory is given by sfp itself...
		xmlSettingsStr = settingsTransporter.exportProperties(0); // 0
		
		currentSettingsXml = xmlSettingsStr;
		
		return xmlSettingsStr;
	}

	public void exportXml(){
		 
		save(1) ;
		
	}
	
	public void exportXml(String filename) throws Exception{
		// saving properties to XML
		// note that xml export is also called for included settings, which will be saved in dedicated files
		
		String propsFileName, dir, xstr ;
		DFutils fileutil = new DFutils();
		
		save(1) ;
		
		// rename the file
		xstr = exportSettings();
		dir = fileutil.getUserDir();
		propsFileName = DFutils.createPath( dir, SomFluidProperties._STORAGE_XML ) ;
		fileutil.writeFileSimple(propsFileName,xstr);
		
		// new filename
		fileutil.renameFile( propsFileName, filename) ;
		
		
	}
	
	public String getExportedXml(){
		
		if (currentSettingsXml.length() <= 10){
			
		}
		return currentSettingsXml;
	}
	
	public void save() {
		save(0); 
	}
	
	
	public void save(int target) {
		
		String dir, prjdir="", prjname,propertiesFileName;
		
		if (fileOrganizer==null){
			fileOrganizer = new FileOrganizer ();
			fileOrganizer.setPropertiesBase(this);
			getPersistenceSettings().setFileOrganizer(fileOrganizer);
		}
		
		DFutils fileutil = new DFutils();
		
		String pathitx = "" ;
		if (applicationContext.contentEquals(InstanceProcessControlIntf._APP_CONTEXT_ITEXX) ){
			pathitx ="" ;
		}
		
		
		
		prjname = this.getPersistenceSettings().getProjectName() ;
		prjdir = fileOrganizer.getProjectDirName() ;
		
		if (DFutils.folderExists(prjdir)==false){
			prjdir="";
			if (prjname.length()>0){
				dir = systemRootDir;
				if (applicationContext.contentEquals(InstanceProcessControlIntf._APP_CONTEXT_ITEXX) ){
					dir = DFutils.createPath( dir , "app/");
				}
				dir = DFutils.createPath( dir, "Astor/");
				dir = DFutils.createPath( dir, prjname+"/");
			}else{
				dir = DFutils.createPath( systemRootDir, "shared/");
			}
		}else{
			dir = fileOrganizer.getObjectStoreDir(prjdir); // we could override the projectbasedir ...
		}
		 
			
		if (target>=1){
			
			// dir = fileutil.getUserDir();
			// dir = fileOrganizer.getObjectStoreDir("");
			propertiesFileName = DFutils.createPath( dir, SomFluidProperties._STORAGE_XML ) ;
			String xstr = exportSettings();
			fileutil.writeFileSimple(propertiesFileName,xstr);
			
		}else{
			
			propertiesFileName = DFutils.createPath( dir, SomFluidProperties._STORAGE_OBJ ) ;	
			// now loading the desired properties into a new object;
			ContainerStorageDevice storageDevice ;
			storageDevice = new ContainerStorageDevice();
			
			storageDevice.storeObject( this,propertiesFileName) ;

			DFutils.reduceFileFolderList( dir,1,".properties",20) ; 
		}
	}


	/**
	 * this creates an XML string with all parameters and their default values
	 * (and probably also with comments describing possible alternative values...)
	 * 
	 * @return the xml string
	 */
	public String getDefaultExport() {
	 
		return "";
	}


	public boolean initializationOK() {
		return initializationOK;
	}
	public boolean getInitializationOK() {
		return initializationOK;
	}
	public boolean isInitializationOK() {
		return initializationOK;
	}
	public void setInitializationOK(boolean initializationOK) {
		this.initializationOK = initializationOK;
	}
	
	
	

	// ------------------------------------------------------------------------
	

	public AlgorithmDeclarationsLoader getAlgoDeclarations() {
		return algoDeclarations;
	}


	public void setAlgoDeclarations(AlgorithmDeclarationsLoader algoDeclarations) {
		this.algoDeclarations = algoDeclarations;
	}


	public DataUseSettings getDataUseSettings() {
		return dataUseSettings;
	}


	public PersistenceSettings getPersistenceSettings() {
		
		if (persistenceSettings==null){
			if (fileOrganizer == null){
				fileOrganizer = new FileOrganizer();
				fileOrganizer.setPropertiesBase(this);
			}
			persistenceSettings = new PersistenceSettings( fileOrganizer );
		}
		return persistenceSettings;
	}


	/**
	 * @param persistenceSettings the persistenceSettings to set
	 */
	public void setPersistenceSettings(PersistenceSettings persistenceSettings) {
		this.persistenceSettings = persistenceSettings;
	}


	public SpriteSettings getSpriteSettings(){
		return modelingSettings.getSpriteSettings() ;
	}
  

	public void importTransformationParameterDefaults(String filename) {
		// TODO 
	}


	public int getSomType() {
		return somType;
	}


	public void setSomType(int somType) {
		this.somType = somType;
		modelingSettings.setSomType(somType);
	}


	public void setAbsoluteFieldExclusions(String[] varStrings, int mode) {
	
		absoluteFieldExclusions = new ArrayList<String>( Arrays.asList(varStrings));
		absoluteFieldExclusionsMode = mode;
	}
	
	/**
	 * @return the absoluteFieldExclusions
	 */
	public ArrayList<String> getAbsoluteFieldExclusions() {
		return absoluteFieldExclusions;
	}


	/**
	 * @param absoluteFieldExclusions the absoluteFieldExclusions to set
	 */
	public void setAbsoluteFieldExclusions(ArrayList<String> excludedfields) {
		
		absoluteFieldExclusions = new ArrayList<String>();
		if ((excludedfields!=null) && (excludedfields.size()>0)){
			absoluteFieldExclusions.addAll(excludedfields);
		}
		absoluteFieldExclusionsMode = -1;
	}


	/**
	 * @return the absoluteFieldExclusionsMode
	 */
	public int getAbsoluteFieldExclusionsMode() {
		return absoluteFieldExclusionsMode;
	}


	/**
	 * @param absoluteFieldExclusionsMode the absoluteFieldExclusionsMode to set
	 */
	public void setAbsoluteFieldExclusionsMode(int absoluteFieldExclusionsMode) {
		this.absoluteFieldExclusionsMode = absoluteFieldExclusionsMode;
	}


	public int getInitialNodeCount() {
		 
		return initialNodeCount;
	}


	public void setInitialNodeCount(int nodeCount) {
		this.initialNodeCount = nodeCount;
	}




	/**   */
	public boolean addDataSource(int sourcetype, String locatorname) {
		boolean result=false;
		String dir="",rootpath =""; // e.g.  "D:/data/projects/"
		String prjname = "", filename="",relPath;
		
		sourceType = sourcetype;
		locatorname = locatorname.trim();
		
		if ((locatorname.indexOf("/")==0) || (DFutils.fileExists(locatorname)==false)){
			
			rootpath = persistenceSettings.getPathToSomFluidSystemRootDir();
			
			prjname = persistenceSettings.getProjectName() ;
			relPath = prjname+"/data/raw/";
			relPath = relPath.replace("//", "/").trim();
			
			
			
			if (this.isITexxContext()){ // 
				
				dir = DFutils.createPath(rootpath.trim(),""); 
			}else{
				dir = rootpath.trim();	
			}
			filename = DFutils.createPath(dir, relPath);
			
			// "D:/data/projects/bank2/data/raw/bankn_d2.txt" 
			locatorname = DFutils.createPath( filename,locatorname );
			// e.g.  "D:/data/projects/bank2/data/raw/bankn_d2.txt"
		}
		dataSrcFilename = locatorname ; 
		
		result = DFutils.fileExists(dataSrcFilename) ;
		return result;
	}

	 

	public void setDataSrcFilename(String dataSrcFilename) {
		this.dataSrcFilename = dataSrcFilename;
	}

 

	public int getDataUptakeControl() {
		return dataUptakeControl;
	}


	/** 
	 * <0: don't load, 0: immediate uptake; >0:delayed uptake (in millis) 
	 */
	public void setDataUptakeControl(int ctrlValue) {
		 
		dataUptakeControl = ctrlValue;
	}

	/**
	 * this will allow SomFluid to choose a proper size and a proper number of particles (resolution),
	 * dependent on the data (number of records, number of variables;
	 * Changes will occur in both directions
	 * 
	 * @param 0=false, >=1 yes 
	 */
	public void setAutoAdaptResolutionAllowed(int flag) {
		//  
		
	}

	

	public void setCollectibleColumn(int columnIndex) {
		collectibleColumn = columnIndex;
	}

	
	@Override // from abstract super class
	public int getCollectibleColumn() {
		 
		// TODO for Astor: provide the secondary index == ContextId, which
		//      is from the database.
		// such, the SOM does not need to refer to a locally available persistent 
		// table, just to a persistent index that is somehow available
		
		// default = 0
		return collectibleColumn ;
	}


	public void setMessagingActive(boolean flag) {
		 
		messagingActive = flag;
	}


	public boolean getMessagingActive() {
		 
		return messagingActive;
	}


	public void activateMultithreadedProcesses( boolean flag) {
		// 
		multithreadedProcesses = flag;
	}
	public boolean isMultithreadedProcesses() {
		return multithreadedProcesses;
	}
	public boolean getMultithreadedProcessing() {
		return multithreadedProcesses;
	}
	public void setMultithreadedProcesses(boolean flag) {
		this.multithreadedProcesses = flag;
	}

	/** determines the maximal number of nodes that will be selected during som-working */
	public void setRestrictionForSelectionSize(int sizevalue) {
		//  
		restrictionForSelectionSize = sizevalue ;
		modelingSettings.setRestrictionForSelectionSize( sizevalue ) ;
	}


	public int getRestrictionForSelectionSize() {
		return restrictionForSelectionSize;
	}

	/**
	 * if node grow beyond that number of records, they will be split; 
	 * besides splitting, this could also provoke bagging or growing, if allowed
	 * 
	 * @param count
	 */
	public void setAbsoluteRecordLimit(int count) {
		modelingSettings.setAbsoluteRecordLimit(count);
	}
	
	/**
	 * 
	 * 
	 * @param recordsPerNode minimal average number of records in nodes of a SOM that must be achieved before
	 *                       bagging will be applied 
	 * @param maxNodeCount   maximum number of nodes allowed for a SOM; together with records per node, this triggers bagging
	 *                       if there are enough data 
	 * @param maxRecordCount whenever a SOM takes at least this number of records, bagging will be applied (if it has been activated)
	 */
	public void defineSomBags( int maxNodeCount, int recordsPerNode, int maxRecordCount) {
		
		modelingSettings.getSomBagSettings().setSombagRecordsPerNode(recordsPerNode) ;
		modelingSettings.getSomBagSettings().setSombagMaxNodeCount(maxNodeCount) ;
		modelingSettings.getSomBagSettings().setSombagMaxRecordCount(maxRecordCount) ;
		
	}
	public void applySomBags(boolean flag) {
		modelingSettings.getSomBagSettings().setApplySomBags( flag );
	}
	public void setAutoSomBags(boolean flag) {
		modelingSettings.getSomBagSettings().setAutoSomBags( flag );
	}
	
	/** set to 1 if there is only 1 Winner in the search for the BMU */
	public void setWinnersCountMultiple(int winnocount) {
		// 
		modelingSettings.setWinningNodesCount( winnocount ) ;
		
	}

	// see also the local class "Growth" in modelingSettings
	public void setActivationOfGrowing(boolean flag) {
		modelingSettings.setActivationOfGrowing(flag);
	}
	
	public void setGrowthMode(int[] growthModes, double... params) {
		double[] parameters ;
		
		
		ResultRequests rrq = new ResultRequests();
		
		if ((params!=null) && (params.length>0)){
			
			parameters = new double[params.length] ;
			int z=0;
			for ( double pv : params ){
				parameters[z] = pv ;
				z++;
			}
		}else{
			parameters = new double[0] ; // just preventing a null
		}
		
		modelingSettings.setSomGrowthMode( growthModes ) ;
		
		if (parameters.length>0){
			modelingSettings.setSomGrowthControlParams(parameters) ;	
		}
		
	}
	public void setGrowthMode(int gmode) {
		if (modelingSettings.isGrowthActive()==false){
			
		}
		modelingSettings.setSomGrowthMode( gmode ) ;
	}


	public void addGrowthMode(int gmode) {
		modelingSettings.setSomGrowthMode( gmode ) ;
		
	}


	public int[] getGrowthModes(){
		return null;
	}
	public String[] getGrowthModesAsStr(){
		return null;
	}
	public void removeGrowthMode( int growthMode){
		
	}
	
	/**
	 *  1  = greedy -> large nodes, rather small lattice, weak growth
	 *  5  = normal, 
	 *  10 = allowing sparsely filled nodes, strong growth
	 * 
	 */
	public void setGrowthSizeAdaptationIntensity(int adaptionintensity) {
		 
	}


	public void setValidationActive(boolean flag, double... params) {
 		modelingSettings.setValidationActive(flag) ;
	}

	public void setValidationStyle( int vStyle) {
		modelingSettings.getValidationSettings().setValidationStyle(vStyle);
	}
	
	public void setValidationSampleSizeAutoAdjust( boolean flag){
		modelingSettings.getValidationSettings().setSampleSizeAutoAdjust(flag) ;
	}
	
	/**
	 * p1:n repeats for out-of-sample validation
	 * p2+=sample sizes to keep aside for validation 
	 *     1. for within-process (during evolutionary optimization)
	 *     2. for cross-validation (third sample = radical out-of-sample validation
	 *                
	 * @param repeats
	 * @param params...
	 */
	public void setValidationParameters( int repeats, double... params) {
		setValidationParameters( ValidationSettings._VALIDATE_SINGLE_SAMPLE_PROB, repeats, params);
	}
	
	/**
	 * parameters for validation: p1=style, p2:n repeats p3+=sample sizes to keep for validation
	 * 
	 * @param vStyle   
	 * @param repeats  
	 * @param params   
	 */
	public void setValidationParameters(int vStyle, int repeats, double[] params) {
		double[] parameters ;
		
		modelingSettings.getValidationSettings().setValidationStyle(vStyle);
		modelingSettings.getValidationSettings().setRepeats(repeats);
		
		if ((params!=null) && (params.length>0)){
			
			parameters = new double[params.length] ;
			int z=0;
			for ( double pv : params ){
				parameters[z] = pv ;
				z++;
			}
		}else{
			parameters = new double[0] ; // just preventing a null
		}
		
		modelingSettings.setValidationParameters(parameters);
	}

	public void setGlueInstanceType(int glueinstanceType) {
		glueType = glueinstanceType;
	}
	
	public void setInstanceType(int somInstanceType) {
		 
		if (fileOrganizer==null){
			fileOrganizer = new FileOrganizer() ;  
			persistenceSettings = new PersistenceSettings(fileOrganizer);
		}
		outputSettings = new OutputSettings(persistenceSettings);
		
		fileOrganizer.setPropertiesBase(this);
		 
		this.somInstanceType = somInstanceType;
	}
	
	public void setglueModuleMode(int glueModuleMode) {
		
	}


	public DataFilter getDataFilter(SomFluidFactory factory) {
		 
		return modelingSettings.getDataFilter()  ;
	}


	/**
	 * 
	 * @param variableLabel
	 * @param lower     numeric criterion
	 * @param upper
	 * @param bTableRow row in boolean table, will auto-correct to next reasonable value
	 * @param bTableCol col in boolean table, will auto-correct to next reasonable value </br>
	 *                  any value>1 will be interpreted as OR condition within an AND condition
	 *                  negative values indicate XOR 
	 * @param active
	 */
	public void addFilter( String variableLabel, 
						   double num, String operator, int bTableRow, int bTableCol, boolean active) {
		
		modelingSettings.getDataFilter().addFilter(variableLabel, num, operator, bTableRow, bTableCol, active) ;
	}


	public void setMaxL2LoopCount(int loopcount) {
		
		modelingSettings.setMaxL2LoopCount( loopcount);
		
		
		modelingSettings.setSpriteAssignateDerivation( (loopcount>1) ) ;
		modelingSettings.setEvolutionaryAssignateSelection( (loopcount>1) );
	}


	public void setSimulationSize(int simulationsize) {
		
		dataUseSettings.setSimulationSize( simulationsize );
	}

	public void setSimulationMode(int simulationmode, double... params) {
		dataUseSettings.setSimulationMode(simulationmode, params);
		
	}
	
	/**
	 *
	 *  mode : </br>
	 *   - 0 = 0/0-model, plain normal noise</br>
	 *   - 1 = 0-model, no consideration of correlation structure, but according to empiric distribution</br>
	 *   - 2 = consideration of correlation structure, only top-2 related variable</br>
	 *   - 3 = consideration of correlation structure, all variables in use vector,</br></br>
	 *   
	 *  modes 2,3 use cholesky decomposition </br></br>
	 *  
	 * @param addedPortion amount of records to add as a fraction of the original sample
	 * @param noiseIntensity amount of noise as fraction of stdev per variable
	 * @param mode of simulation
	 */
	public void addSurrogatedSimulationData(double addedPortion, double noiseIntensity, int mode) {
		
	}

	/**
	 * global on/off, initial modeling on/off, optimizing on/off;
	 *  
	 * @param global
	 * @param firstModel
	 * @param optimization
	 */
	public void surrogateAppMode(int global, int firstModel, int optimization) {
		
		
	}

	public void setExtendingDataSourceEnabled(boolean flag) {
		extendingDataSourceEnabled = flag;
	}


	public boolean isExtendingDataSourceEnabled() {
		return extendingDataSourceEnabled;
	}


	public void setShowSomProgress(int displayIntensity) {
		//
		showSomProgressMode = displayIntensity ;
	}
 
	public int getShowSomProgressMode() {
		return showSomProgressMode;
	}
 
	public void setShowSomProgressMode(int showSomProgressMode) {
		this.showSomProgressMode = showSomProgressMode;
	}


	public boolean isPluginsAllowed() {
		return isPluginsAllowed;
	}


	public void setPluginsAllowed(boolean isPluginsAllowed) {
		this.isPluginsAllowed = isPluginsAllowed;
	}

	/**
	 * refers to "builtinscatalog.xml" which is necessary for global indexing and activation of built-in algorithms;
	 * if it does not exist (path or file) only the most basic algorithms are available
	 * 
	 * @param pathstring
	 */
	public void setAlgorithmsConfigPath(String pathstring) {
		// 
		
		String ps ;
		ps = DFutils.createPath( pathstring,"/");
		if (pathstring==null){
			return;
		}
		if (pathstring.endsWith("/")==false){
			pathstring = pathstring+"/" ; 
		}

		algorithmsConfigPath = pathstring;
	}


	public String getAlgorithmsConfigPath() {
		return algorithmsConfigPath;
	}


	public OutputSettings getOutputSettings() {
		//
		return outputSettings;
	}


	public int getGlueType() {
		return glueType;
	}


	public void setGlueType(int glueType) {
		this.glueType = glueType;
	}


	public SomFluidSettings getSomFluidSettings() {
		
		return somfluidSettings;
	}


	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}


	public void setDataUseSettings(DataUseSettings dataUseSettings) {
		this.dataUseSettings = dataUseSettings;
	}
 



	public void setPathToSomFluidSystemRootDir(String dir) {
		// 
		systemRootDir = dir;
		persistenceSettings.setPathToSomFluidSystemRootDir(dir);
	}


	public String getSystemRootDir() {
		return systemRootDir;
	}

	public void setSystemRootDir(String systemRootDir) {
		this.systemRootDir = systemRootDir;
	}
 
	
	public int getMultiProcessingLevel() {
		return multiProcessingLevel;
	}
	public void setMultiProcessingLevel(int mppLevel) {
		multiProcessingLevel = mppLevel;
	}


	public void setVariableSettings(VariableSettingsHandlerIntf variablesettings) {
		variableSettings = variablesettings;
		
	}


	public VariableSettingsHandlerIntf getVariableSettings() {
		return variableSettings;
	}


	public ArrayList<String> getTreatmentDesignVariables() {
		return treatmentDesignVariables;
	}
	public void setTreatmentDesignVariables(ArrayList<String> list) {
		if (list!=null){
			treatmentDesignVariables = new ArrayList<String>(list);
		}
	}


	public ArrayList<String> getGroupDesignVariables() {
		 
		return groupDesignVariables;
	}
	public void setGroupDesignVariables(ArrayList<String> list) {
		if (list!=null){
			groupDesignVariables = new ArrayList<String>(list);
		}
	}
 

	public int getSomInstanceType() {
		return somInstanceType;
	}


	public void setSomInstanceType(int somInstanceType) {
		this.somInstanceType = somInstanceType;
	}


	public int getSomGridType() {
		return somGridType;
	}


	/**
	 * this allows to spread references instead of copies of vectors, which saves a lot of memory
	 * ...especially important for SomFluid
	 */
	@Override
	public boolean isAssignatesHomogeneous() {
		 
		return isAssignatesHomogeneous;
	}


	// ....................................................
	// the next 4 methods are very helpful for SomFluid
	public void addFieldExclusionByIndex(int fieldIndex) {
		// TODO Auto-generated method stub
		
	}


	public void setFieldExclusionByIndexFrom(int fromIndex) {
		 
		
	}


	public void setFieldExclusionByBetween(int fromIndex, int toIndex) {
		 
		
	}

	public void setFieldExclusionByIndexFrom(String fieldLabel, int offset) {
		 
		
	}

	
	public void setFieldExclusionByIndexUntil( String fieldLabel ) {
		// we first have to determine the index of the field, which we 
		// most likely don't know yet right at the beginning
		
	}


	public void setFieldExclusionByIndexUntil(int fIndex) {
		 
		
	}

	// ....................................................

  
	public TexxDataBaseSettingsIntf getDatabaseSettings() {
		return databaseSettings;
	}


	public void setDatabaseSettings(TexxDataBaseSettings dbSettings) {
		this.databaseSettings = dbSettings;
	}


	public void setDatabaseDefinitionResource(String dbdefResource) {
		 
		dbDefinitionResource = dbdefResource;
	}


	public void getDatabaseDefinitionInfo( String dbname, int structureCode) throws Exception{

		dbDefinitionResource ="itexx-db-definition-xml" ;
		String cfgResourceJarPath="org/NooLab/somfluid/resources/sql/";
		String xmlstr ="" ; 
		
		DFutils fileutil = new DFutils();
		
		xmlstr = ResourceContent.getConfigResource( this.getClass(), cfgResourceJarPath, dbDefinitionResource ) ; 
		
		if (xmlstr.length()>15){ 
			dbAccessDefinition.getDatabaseDefinitionInfo( xmlstr, dbname, structureCode) ;
		}
		
	}
	
	private String getConfigResource( String resourcePath ) throws Exception{
		
		
		
		String xmlstr = "" ;
		boolean rB;
		
		ResourceLoader rsrcLoader = new ResourceLoader();   
		rB = rsrcLoader.loadTextResource( this.getClass(), resourcePath  ) ;
		if (rB){  
			xmlstr = rsrcLoader.getTextResource();
			
		}else{
			throw(new Exception("unable to load resources ("+resourcePath+")")) ;
		}
		
		return xmlstr ;
	}


	public void setOnCompletion(String onCompletion) {
		
		this.onCompletion = onCompletion;
	}


	public String getOnCompletion() {
		return onCompletion;
	}


	public void setStreamingActive(int flag) {
		// 
		streamingActive = flag;
	}

	public int getStreamingActive() {
		return streamingActive ;
	}


	public void setInsertIdExtraColumn(int flag) {
		insertIdExtraColumn = flag;
	}

	public int getInsertIdExtraColumn() {
		return insertIdExtraColumn;
	}


	/**
	 * this describes the number of contexts, that are initially loaded from randomwords
	 * 
	 * @return
	 */
	public int getAstorInitializationRecordCount() {
		// 
		return astorInitializationRecordCount;
	}

	public void setAstorInitializationRecordCount( int count) {
		astorInitializationRecordCount = count;
	}


	public int getAstorInitializationDocCount() {
		return astorInitializationDocCount;
	}
	public void setAstorInitializationDocCount(int count) {
		// 
		astorInitializationDocCount = count;
	}



	
	
	
}
