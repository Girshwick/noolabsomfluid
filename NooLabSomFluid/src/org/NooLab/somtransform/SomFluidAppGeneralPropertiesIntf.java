package org.NooLab.somtransform;

import java.util.ArrayList;

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

	 

}
