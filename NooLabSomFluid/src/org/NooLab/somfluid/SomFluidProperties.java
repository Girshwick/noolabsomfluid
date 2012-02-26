package org.NooLab.somfluid;

import org.NooLab.somfluid.components.DataFilter;
import org.NooLab.somfluid.core.engines.det.ResultRequests;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
 
import org.NooLab.somfluid.properties.DataUseSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.somfluid.properties.SpriteSettings;


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
	public static final int _SIM_PROFILES  = 3;
	
	
	transient SomFluidFactory sfFactory ;
	
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
		
	// lattice
	int somType = -1; // mandatory 
	int initialNodeCount = -1;
	boolean messagingActive = true;
	boolean multithreadedProcesses=false;
	private int restrictionForSelectionSize = -1;

	boolean initializationOK = false;
	
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
	


	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}

	public SpriteSettings getSpriteSettings(){
		return modelingSettings.getSpriteSettings() ;
	}
	
	public int getSomType() {
		return somType;
	}


	public void setSomType(int somType) {
		this.somType = somType;
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
	public void addDataSource(int sourceType, String locatorname) {
		
		this.sourceType = sourceType;
		dataSrcFilename = locatorname ; 
		
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

	public void setValidationActive(boolean flag, double... params) {
 		modelingSettings.setValidationActive(flag) ;
	}


	public void setValidationParameters(int vStale, int repeats, double... params) {
		double[] parameters ;
		
		
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


	
	
	
}
