package org.NooLab.somfluid;

import java.io.IOException;

import org.NooLab.somfluid.structures.VariableSettingsHandlerIntf;




public interface SomFluidPropertiesHandlerIntf {

	// default=0==no; 1= by PCA, 2= by canonical KNN, 3= combined by PCA & KNN (not avail. yet)
	public final int _INIT_VARSELECTION_METHOD_NONE = 0 ;	
	public final int _INIT_VARSELECTION_METHOD_PCA  = 1 ;
	public final int _INIT_VARSELECTION_METHOD_cKNN = 2 ;
	public final int _INIT_VARSELECTION_METHOD_PKN  = 3 ;

	
	
	
	public void initializeDefaults( );

	public void setAlgorithmsConfigPath( String pathToCatalogFolder ) throws Exception;

	public void setInstance(String description, int ...nodecount );

	/**
	 * abc
	 * @param type 0 = typology file in simple format based on [sections], 1 = textual description (background information)
	 * @return
	 */
	public String checkForVariableDescriptionFile( int type);
	
	public String checkForVariableDescriptionFile(int type, String filepath);
	
	
	public void initializeDefaultsFromFile( String inifilename);

	public void setDataSourcing(String srcDescription, int activateOnlineMode);

	public void setDataSourceName(String dataSource);

	public void setSupervisedDirectory(String folder) throws Exception;

	public String getSupervisedDirectory();

	public String getResultBaseFolder();

	public void setDataSimulationByPrototypes(int numberOfRecords);

	public VariableSettingsHandlerIntf getVariableSettingsHandler();

	public void setSomTargetMode(int targetmodeSingle);

	public void setMethodforInitialVariableSelection(int mode);

	public void preferSmallerModels(boolean flag, int preferredMinimumSize);

	public void setOptimizerStoppingCriteria(int absoluteStepCount, double ...stoppingConstraints);

	public void setBagging( int maxNodeCount, int recordsPerNode, int ...maxRecordCount );

	public void activateGrowingOfSom(boolean flag, int maxNodeSize, double avgQuantile);

	public void setBooleanAdaptiveSampling(boolean b);

	public String setResultsPersistence(int switchOnOff, String pathIntermit);

	public void setMaxNumberOfPrototypes(int n);

	public void setSingleTargetDefinition(String level, double criterionLowerLimit, double criterionUpperLimit, String label);
 
	public void exportVariableSettings( VariableSettingsHandlerIntf variableSettings, String xfilename);

	public boolean loadVariableSettingsFromFile(String  filename) throws IOException;

	public String getStartupTraceInfo();

	public void publishApplicationPackage(boolean flag, String basepath);

	public void setGridType(int somGridtypeFixed);

	public void setDatabaseDefinitionResource(String alias, String dbname, int structureCode);  
	public void setDatabaseDefinitionResource(String dbDefinitionResource, String alias, String dbname, int structureCode);

	
	

}
