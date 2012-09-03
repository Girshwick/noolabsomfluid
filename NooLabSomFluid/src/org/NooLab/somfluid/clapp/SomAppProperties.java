package org.NooLab.somfluid.clapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.up.IniProperties;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.FileOrganizer;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.net.GUID;



/**
 * 
 * 
 * 
 *
 */
public class SomAppProperties 
								extends 
											SomFluidAppPropertiesAbstract 
								implements 	
											
											SomAppPropertiesIntf,
											DataHandlingPropertiesIntf,
											Serializable{

	private static final long serialVersionUID = 8129200314108478416L;


	public static final int _WORKINGMODE_PROJECT     = 1 ;
	public static final int _WORKINGMODE_SERVICEFLOW = 4 ;
	public static final int _WORKINGMODE_SERVICEFILE = 5 ;


	public static final int _MODELSELECT_LATEST      = 1;
	public static final int _MODELSELECT_FIRSTFOUND  = 2;
	public static final int _MODELSELECT_BEST        = 4;
	public static final int _MODELSELECT_ROBUST      = 8;
	public static final int _MODELSELECT_VERSION     = 16 ;
	public static final int _MODELSELECT_STRUCPREF   = 100;


	public static final int _MODELSELECT_TARGETLABEL = 65;
	 
	
	static SomAppProperties sclappProperties ;

	transient SomFluidFactory sfFactory ;
	
	transient SomFluidProperties sfProperties ; // better as a restricted interface, we do not need all settings for the classification app
	
	
	
	
	
	private String algorithmsConfigPath;
	private int glueType;
	


	private boolean pluginsAllowed;
	
	int workingMode = _WORKINGMODE_PROJECT;

	
	

	
	String baseModelFolder = "";
	String modelName = "";
	String activeModel ="";
	String modelPackageName ="";
	
	int modelSelectionMode = _MODELSELECT_LATEST;
	String preferredModelVersion = "";
	
	ArrayList<String> activeModels = new ArrayList<String> ();
	
	ArrayList<String> supervisionFilenameFilters = new ArrayList<String> ();


	String indexVariableLabel = "" ;
	int indexVariableLabelIndex = -1 ;

	ArrayList<String>  indexVariableLabels = new ArrayList<String>();
	ArrayList<Integer> indexVariableLabelIndexes = new ArrayList<Integer>();


	private double ecrRisk = -1 ;
	private int requiredSupport = -1;
	
	 
	// ========================================================================
	public SomAppProperties(String sourceForProperties){
			super();
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
		
		// setInstanceType(glueinstanceType) ; 
		setFileOrganizer(fileOrganizer) ;
		setPersistenceSettings(persistenceSettings) ;
		setOutputSettings(outputSettings) ;
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

	public SomFluidProperties getSelfReference() {
		return sfProperties;
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

	public void setDataSourceFile(String projectBasePath, String lastProjectName, String lastDataSet) {
		
		if ((fileutil.fileexists(lastDataSet)) && (lastDataSet.indexOf("/")>0)){
			dataSrcFilename = lastDataSet;
		}else{
			dataSrcFilename = fileutil.createpath(fileutil.createpath( projectBasePath, lastProjectName), "data/"+lastDataSet) ;
		}
		IniProperties.dataSource = dataSrcFilename ;
		IniProperties.saveIniProperties();
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

	public void setBaseModelFolder(String selectProjectHome, String lastProjectName, String modelfolder) {
	
		String path = "";
		
		path = fileutil.createpath( selectProjectHome, lastProjectName);
		path = fileutil.createpath( path, modelfolder);
		
		setBaseModelFolder(path) ;
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

	public int getModelSelectionMode() {
		return modelSelectionMode;
	}

	public void setModelSelectionMode(int selectMode) {
		if (selectMode>=100){
			if (modelSelectionMode<100){
				modelSelectionMode = modelSelectionMode + 100;
			}
		}else{
			if (modelSelectionMode<100){
				modelSelectionMode = selectMode;
			}else{
				modelSelectionMode = 100 + selectMode;
			}
		}
		
	}
	public void setModelSelectionMode(int selectMode, String version) {
		setModelSelectionMode(selectMode) ;
		preferredModelVersion = version ;
	}

	public void resetModelSelectionMode() {
		modelSelectionMode = _MODELSELECT_LATEST;
	}

	
	public String getPreferredModelVersion() {
		return preferredModelVersion;
	}




	
	public void setPreferredModelVersion(String modelversion) {
		preferredModelVersion = modelversion;
	}
	// ====================================================================================================
	@Override
	public int getDataUptakeControl() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setDataUptakeControl(int ctrlValue) {
		// TODO Auto-generated method stub
		
	}
	public void setIndexVariable(String vLabelStr) {
		indexVariableLabel = vLabelStr ;
		
	}

	public void setIndexVariableColumnIndex(int colIndex) {
		indexVariableLabelIndex = colIndex ;
		
	}

	public String getIndexVariableLabel() {
		return indexVariableLabel;
	}

	public void setIndexVariableLabel(String indexVariableLabel) {
		this.indexVariableLabel = indexVariableLabel;
		indexVariableLabels = new ArrayList<String>();
		indexVariableLabels.add(indexVariableLabel);
	}

	public void setIndexVariables(String[] labels) {
		indexVariableLabels = new ArrayList<String>(Arrays.asList(labels)) ;
		if (indexVariableLabels.size()>0){
			indexVariableLabel = indexVariableLabels.get(0) ;
		}
	}

	public ArrayList<String> getIndexVariableLabels() {
		return indexVariableLabels;
	}

	public void setIndexVariableLabels(ArrayList<String> variableLabels) {
		indexVariableLabels = variableLabels;
		
	}

	public int getIndexVariableLabelIndex() {
		return indexVariableLabelIndex;
	}

	public void setIndexVariableLabelIndex(int indexVariableLabelIndex) {
		this.indexVariableLabelIndex = indexVariableLabelIndex;
		indexVariableLabelIndexes = new ArrayList<Integer>();
		indexVariableLabelIndexes.add(indexVariableLabelIndex) ;
	}

	public void setIndexVariableColumnIndexes(int[] indexValues) {

		indexVariableLabelIndexes = new ArrayList<Integer>();
		for (int i=0;i<indexValues.length;i++){
			indexVariableLabelIndexes.add( indexValues[i] );
		}
		if (indexVariableLabelIndexes.size()>0){
			indexVariableLabelIndex = indexVariableLabelIndexes.get(0) ;
		}
	}

	public ArrayList<Integer> getIndexVariableLabelIndexes() {
		return indexVariableLabelIndexes;
	}

	public void setIndexVariableLabelIndexes(ArrayList<Integer> indexVariableLabelIndexes) {
		this.indexVariableLabelIndexes = indexVariableLabelIndexes;
	}

	public double getRiskAttitudeByECR() {
		return ecrRisk ;
	}

	public double getEcrRisk() {
		return ecrRisk;
	}
	
	/**
	 * if not set, the models ECR will be taken
	 * @param ecrValue
	 */
	public void setRiskLevelByECR(double ecrValue) {
		ecrRisk = ecrValue;
	}
	public void setEcrRisk(double ecrValue) {
		ecrRisk = ecrValue;
	}

	public int getRiskConfidenceByClusterSupport() {
		return requiredSupport ;
	}

	public int getRequiredSupport() {
		return requiredSupport;
	}

	public void setRequiredSupport(int groupsupport) {
		requiredSupport = groupsupport;
	}
	public void setRequiredConfidenceBySupport(int groupsupport) {
		requiredSupport = groupsupport;
	}

	public void setModelSelectionCriteria(int semanticSelectCriterion, String targetlabel) {
		
		// use a simple obj structure 
		// semanticSelectionCriterion = semanticSelectCriterion;
		// semanticSelectionCriterion
		
	}

	@Override
	public Object getCollectibleColumn() {
		//  
		return null;
	}

	 
 
}
