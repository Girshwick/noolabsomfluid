package org.NooLab.somfluid.properties;

import java.io.Serializable;

import org.NooLab.somfluid.storage.FileOrganizer;

import com.jamesmurty.utils.XMLBuilder;



public class PersistenceSettings implements Serializable{

	private static final long serialVersionUID = -2361104769448054255L;

	String pathToSomFluidSystemRootDir = "";

	private String projectName = "";
	
	transient FileOrganizer fileOrganizer ;

	private boolean incomingDataClassifyFirst;

	private String incomingDataSupervisionDir;

	private boolean incomingDataSupervisionActive;

	private boolean autoPackagingOfCompleteModels;

	private boolean autoSaveSomFluidModels;

	private boolean keepPreparedData;

	private boolean exportTransformModelAsEmbeddedObj = true; 
	
	// ========================================================================
	public PersistenceSettings(FileOrganizer fileOrg){
		fileOrganizer = fileOrg;
	}

	// ========================================================================



	
	
	/**
	 * @return the pathToSomFluidSystemRootDir
	 */
	public String getPathToSomFluidSystemRootDir() {
		return pathToSomFluidSystemRootDir;
	}


	/**
	 * @param pathToSomFluidSystemRootDir the pathToSomFluidSystemRootDir to set
	 */
	public void setPathToSomFluidSystemRootDir(String pathToSomFluidSystemRootDir) {
		this.pathToSomFluidSystemRootDir = pathToSomFluidSystemRootDir;
		fileOrganizer.update();
	}


	public void setProjectName(String string) {
		 
		projectName = string;
		fileOrganizer.update();
	}


	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}


	public void setKeepPreparedData(boolean flag ) {
		keepPreparedData = flag ;
	}


	public boolean isKeepPreparedData() {
		return keepPreparedData;
	}

	public void autoSaveSomFluidModels(boolean flag) {
		autoSaveSomFluidModels = flag ;
	}


	public void autoPackagingOfCompleteModels(boolean flag) {
		autoPackagingOfCompleteModels = flag ;
	}


	public void setIncomingDataSupervisionDir(String dir) {
		incomingDataSupervisionDir = dir;
		fileOrganizer.update();
	}

	
	public String getIncomingDataSupervisionDir() {
		return incomingDataSupervisionDir;
	}

	public boolean isIncomingDataSupervisionActive() {
		return incomingDataSupervisionActive;
	}

	public void setIncomingDataSupervisionActive(boolean flag) {
		incomingDataSupervisionActive = flag ;
	}
	
	public void setIncomingDataClassifyFirst(boolean flag) {
		incomingDataClassifyFirst = flag;
	}
	
	
	
	public boolean isIncomingDataClassifyFirst() {
		return incomingDataClassifyFirst;
	}

	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		// 
		return null;
	}

	public boolean isExportTransformModelAsEmbeddedObj() {
		return exportTransformModelAsEmbeddedObj;
	}

	public void setExportTransformModelAsEmbeddedObj(boolean flag) {
		exportTransformModelAsEmbeddedObj = flag;
	}

	public boolean isAutoPackagingOfCompleteModels() {
		return autoPackagingOfCompleteModels;
	}

	public void setAutoPackagingOfCompleteModels(boolean autoPackagingOfCompleteModels) {
		this.autoPackagingOfCompleteModels = autoPackagingOfCompleteModels;
	}

	public boolean isAutoSaveSomFluidModels() {
		return autoSaveSomFluidModels;
	}

	public void setAutoSaveSomFluidModels(boolean autoSaveSomFluidModels) {
		this.autoSaveSomFluidModels = autoSaveSomFluidModels;
	}


	
	
	
	
}
