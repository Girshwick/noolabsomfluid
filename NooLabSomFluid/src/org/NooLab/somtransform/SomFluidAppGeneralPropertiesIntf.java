package org.NooLab.somtransform;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.AlgorithmDeclarationsLoader;
import org.NooLab.somfluid.properties.DataUseSettings;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.PersistenceSettings;
import org.NooLab.somfluid.storage.FileOrganizer;




public interface SomFluidAppGeneralPropertiesIntf {

	FileOrganizer getFileOrganizer();

	SomFluidPluginSettings getPluginSettings();

	PersistenceSettings getPersistenceSettings();

	int getSourceType();

	String getDataSrcFilename();

	boolean isExtendingDataSourceEnabled();

	SomFluidProperties getSelfReference();

	ModelingSettings getModelingSettings();

	AlgorithmDeclarationsLoader getAlgoDeclarations();

	boolean addDataSource(int sourceType, String filename);

	void setDataSrcFilename(String dataSrcFilename);

	void setPluginSettings(SomFluidPluginSettings pluginsettings);

	int getMultiProcessingLevel();

	int getShowSomProgressMode();

	SomFluidFactory getSfFactory();

	
	int getSomType();

	/** the extensionality of the nodes can hold 2 lists:</br> 
	 * the first one is the record index as it is provided in the table,
	 * the second is a reference to a cloned object holding some value, of which 
	 * the application itself has to know what to do;</br>
	 * this is especially relevant, where we do not have the full table within 
	 * the SOM, but rather only in a database.</br></br>
	 * 
	 * this call here wraps to the original in SomFluidProperties
	 * @return
	 */
	public Object getCollectibleColumn();

	String getApplicationContext();

	void setApplicationContext(String appcontext);

	boolean isITexxContext();

	 

}
