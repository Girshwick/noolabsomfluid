package org.NooLab.somfluid;

import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.env.communication.GlueConnection;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.SettingsTransporter;


											//  as usual, we offer particular views on this container
public class SomFluidProperties implements 	//  
												DataHandlingPropertiesIntf,
											// 
												LatticePropertiesIntf{

	static SomFluidProperties sfp; 
	
	public final static int _SOMTYPE_MONO = 1; 
	public final static int _SOMTYPE_PROB = 2;
	
	transient SomFluidFactory sfFactory ;
	
	transient GlueConnection glueConnection=null;
	int glueType = 0;
	
	// type of data source for active access:
	// 1=file, 2=db, 3=serialized SomdataObject
	int sourceType = -1;
	String dataSrcFilename = "";
	
	/** <0: don't load, 0: immediate uptake; >0:delayed uptake (in millis) */
	int dataUptakeControl = -1 ;
	
	// that is for _SOMTYPE_MONO only ! 
	ModelingSettings modelingSettings = new ModelingSettings() ;
	
		
	// lattice
	int somType = -1; // mandatory 
	int initialNodeCount = -1;
	boolean messagingActive = true;
	boolean multithreadedProcesses=false;
	private int restrictionForSelectionSize = -1;
	
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
		
		settingsTransporter = new SettingsTransporter( sfp );
		
		return sfp;
	}
	
	public void importSettings(){
		
		
	}

	
	public String exportSettings(){
		String xmlSettingsStr = "";
		
		settingsTransporter = new SettingsTransporter( sfp );
		settingsTransporter.export();
		
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


	public void setFactoryParent(SomFluidFactory factory) {
		sfFactory = factory;
	}
	
	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}
	// ------------------------------------------------------------------------
	

	public GlueConnection getGlueConnection( int gluetype) {
		glueType = gluetype;	 
		
		if (glueConnection==null){
			glueConnection = new GlueConnection(this,glueType) ;
		}
		return glueConnection;
	}

	public ModelingSettings getModelingSettings() {
		return modelingSettings;
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


	public void setRestrictionForSelectionSize(int sizevalue) {
		//  
		restrictionForSelectionSize = sizevalue ;
		modelingSettings.setRestrictionForSelectionSize( sizevalue ) ;
	}


	public int getRestrictionForSelectionSize() {
		return restrictionForSelectionSize;
	}


	public void defineSomBags(int recordsPerNode, int maxNodeCount, int maxRecordCount) {
		modelingSettings.getSomBagSettings().setSombagRecordsPerNode(recordsPerNode) ;
		modelingSettings.getSomBagSettings().setSombagMaxNodeCount(maxNodeCount) ;
		modelingSettings.getSomBagSettings().setSombagMaxRecordCount(maxRecordCount) ;
		
	}
	public void applySomBags(boolean flag) {
		modelingSettings.getSomBagSettings().setApplySomBags( flag );
	}


	public void setMultipleWinners(int winnocount) {
		// 
		modelingSettings.setWinningNodesCount( winnocount ) ;
		
	}



	
	
	
}
