package org.NooLab.somfluid;

import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.somfluid.components.AlgorithmDeclarationsLoader;
import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.core.engines.det.ResultRequests;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
 
import org.NooLab.somfluid.properties.DataUseSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.somfluid.properties.SpriteSettings;
import org.NooLab.somfluid.properties.ValidationSettings;
import org.NooLab.utilities.files.DFutils;


											//  as usual, we offer particular views on this container
public class SomFluidProperties implements 	//  
												DataHandlingPropertiesIntf,
											// 
												LatticePropertiesIntf{

	static SomFluidProperties sfp; 
	
	public final static int _SOMTYPE_MONO = 1; 
	public final static int _SOMTYPE_PROB = 2;
	
	public final static int _SRC_TYPE_FILE = 1;
	public final static int _SRC_TYPE_DB   = 3;
	public final static int _SRC_TYPE_OBJ  = 5;
	public final static int _SRC_TYPE_XML  = 10;

	public static final int _SIM_NONE      = -1;
	public static final int _SIM_SURROGATE = 3;
	public static final int _SIM_PROFILES  = 5;
	
	public static final int _SOMDISPLAY_PROGRESS_NONE  = -1 ;
	public static final int _SOMDISPLAY_PROGRESS_BASIC =  0 ;
	public static final int _SOMDISPLAY_PROGRESS_STEPS =  1 ;
	public static final int _SOMDISPLAY_PROGRESS_PERC  =  2 ;
	
	
	transient SomFluidFactory sfFactory ;
	transient AlgorithmDeclarationsLoader algoDeclarations;
	
	int glueType = 0;
	
	// type of data source for active access:
	// 1=file, 2=db, 3=serialized SomdataObject
	int sourceType = -1;
	String dataSrcFilename = "";
	
	/** <0: don't load, 0: immediate uptake; >0:delayed uptake (in millis) */
	int dataUptakeControl = -1 ;
	
	// that is for _SOMTYPE_MONO only ! 
	ModelingSettings modelingSettings = new ModelingSettings() ;
	DataUseSettings dataUseSettings = new DataUseSettings() ;
	PersistenceSettings persistenceSettings = new PersistenceSettings();
		
	SomFluidPluginSettings pluginSettings = new SomFluidPluginSettings();
	
	// lattice
	int somType = -1; // mandatory 
	int initialNodeCount = -1;
	boolean messagingActive = true;
	boolean multithreadedProcesses=false;
	private int restrictionForSelectionSize = -1;

	boolean initializationOK = false;

	private boolean extendingDataSourceEnabled;

	private ArrayList<String> absoluteFieldExclusions;

	private int absoluteFieldExclusionsMode;

	private int showSomProgressMode;

	private boolean isPluginsAllowed = true;

	private String algorithmsConfigPath;

	
	static SettingsTransporter settingsTransporter;
	
	
	// ========================================================================
	protected SomFluidProperties(){
		
	}
	// ========================================================================
	
	
	public static SomFluidProperties getInstance() {
		
		sfp = new SomFluidProperties(); 
		return sfp;
	}
	/**   */
	public static SomFluidProperties getInstance( String xmlSettingsStack ) {
	 
		sfp = new SomFluidProperties();
		
		if (xmlSettingsStack.length()>0){
			settingsTransporter = new SettingsTransporter( sfp );
			sfp = settingsTransporter.importProperties( xmlSettingsStack );
		}
		
		return sfp;
	}
	
	public void importSettings(){
		
		
	}

	
	public String exportSettings(){
		String xmlSettingsStr = "";
		
		settingsTransporter = new SettingsTransporter( sfp );
		xmlSettingsStr = settingsTransporter.exportProperties(0);
		
		return xmlSettingsStr;
	}

	/**
	 * this creates an XML string with all parameters and their default values
	 * (and probably also with comments describing possible alternative values...)
	 * 
	 * @return the xml string
	 */
	public String getDefaultExport() {
	 
		return null;
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
	
	
	
	public void setFactoryParent(SomFluidFactory factory) {
		sfFactory = factory;
	}
	
	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}
	// ------------------------------------------------------------------------
	

	public AlgorithmDeclarationsLoader getAlgoDeclarations() {
		return algoDeclarations;
	}


	public void setAlgoDeclarations(AlgorithmDeclarationsLoader algoDeclarations) {
		this.algoDeclarations = algoDeclarations;
	}


	public PersistenceSettings getPersistenceSettings() {
		
		return persistenceSettings;
	}

	/**
	 * @param persistenceSettings the persistenceSettings to set
	 */
	public void setPersistenceSettings(PersistenceSettings persistenceSettings) {
		this.persistenceSettings = persistenceSettings;
	}


	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}

	public SpriteSettings getSpriteSettings(){
		return modelingSettings.getSpriteSettings() ;
	}
	
	public SomFluidPluginSettings getPluginSettings() {
		return pluginSettings;
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


	/**
	 * 
	 * @param sourceType 1=full data file, 5=profiles for simulation
	 * @param filename
	 */
	public void setDataSource(int sourceType, String locatorname) {
		// 
		this.sourceType = sourceType;
		dataSrcFilename = locatorname ; 
	}

	/**   */
	public boolean addDataSource(int sourceType, String locatorname) {
		boolean result=false;
		String rootpath =""; // e.g.  "D:/data/projects/"
		String prjname = "", filename="",relPath;
		
		this.sourceType = sourceType;
		locatorname = locatorname.trim();
		
		if ((locatorname.indexOf("/")==0) || (DFutils.fileExists(locatorname)==false)){
			
			rootpath = persistenceSettings.getPathToSomFluidSystemRootDir();
			
			prjname = persistenceSettings.getProjectName() ;
			relPath = prjname+"/data/raw/";
			relPath = relPath.replace("//", "/").trim();
			
			filename = DFutils.createPath(rootpath.trim(), relPath);
			
			// "D:/data/projects/bank2/data/raw/bankn_d2.txt" 
			locatorname = DFutils.createPath( filename,locatorname );
			// e.g.  "D:/data/projects/bank2/data/raw/bankn_d2.txt"
		}
		dataSrcFilename = locatorname ; 
		
		result = DFutils.fileExists(dataSrcFilename) ;
		return result;
	}

	public String getDataSrcFilename() {
		return dataSrcFilename;
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
	public void defineSomBags(int recordsPerNode, int maxNodeCount, int maxRecordCount) {
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

	public void setMultipleWinners(int winnocount) {
		// 
		modelingSettings.setWinningNodesCount( winnocount ) ;
		
	}

	// see also the local class "Growth" in modelingSettings
	public void setActivationOfGrowing(boolean flag) {
		modelingSettings.setActivationOfGrowing(flag);
	}
	public void setGrowthMode(int growthMode, double... params) {
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
		
		modelingSettings.setSomGrowthMode( growthMode ) ;
		
		if (parameters.length>0){
			modelingSettings.setSomGrowthControlParams(parameters) ;	
		}
		
	}
	public int[] getGrowthModes(){
		return null;
	}
	public String[] getGrowthModesAsStr(){
		return null;
	}
	public void removeGrowthMode( int growthMode){
		
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

	public void setInstanceType(int instanceType) {
		glueType = instanceType;
	}
	public void setglueModuleMode(int glueModuleMode) {
		
	}


	public DataFilter getDataFilter(SomFluidFactory factory) {
		 
		return modelingSettings.getDataFilter(factory)  ;
	}


	/**
	 * 
	 * @param factory
	 * @param variableLabel
	 * @param lower     numeric criterion
	 * @param upper
	 * @param bTableRow row in boolean table, will autocorrect to next reasonable value
	 * @param bTableCol col in boolean table, will autocorrect to next reasonable value </br>
	 *                  any value>1 will be interpreted as OR condition within an AND condition
	 *                  negative values indicate XOR 
	 * @param active
	 */
	public void addFilter( SomFluidFactory factory,String variableLabel, 
						   double num, String operator, int bTableRow, int bTableCol, boolean active) {
		
		modelingSettings.getDataFilter(factory).addFilter(variableLabel, num, operator, bTableRow, bTableCol, active) ;
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

	/**
	 *  5=normal, 1=greedy -> large nodes, rather small lattice, 10=allowing sparsely filled nodes
	 * 
	 */
	public void setGrowthSizeAdaptationIntensity(int adaptionintensity) {
		 
	}


	public void setExtendingDataSourceEnabled(boolean flag) {
		extendingDataSourceEnabled = flag;
	}


	public void setShowSomProgress(int displayIntensity) {
		// TODO Auto-generated method stub
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
		if (pathstring.endsWith("/")==false){
			pathstring = pathstring+"/" ; 
		}
		String ps = DFutils.createPath( "", pathstring);
		algorithmsConfigPath = pathstring;
	}


	public String getAlgorithmsConfigPath() {
		return algorithmsConfigPath;
	}




	
	
	
}
