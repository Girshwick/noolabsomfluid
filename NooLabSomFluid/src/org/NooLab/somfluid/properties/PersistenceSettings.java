package org.NooLab.somfluid.properties;

import java.io.Serializable;

import org.NooLab.somfluid.storage.FileOrganizer;

import com.jamesmurty.utils.XMLBuilder;



public class PersistenceSettings implements Serializable{

	private static final long serialVersionUID = -2361104769448054255L;

	String pathToSomFluidSystemRootDir = "";

	private String projectName = "";
	
	transient FileOrganizer fileOrganizer ; 
	
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


	public void setKeepPreparedData(boolean b) {
		// TODO Auto-generated method stub
		
	}


	public void autoSaveSomFluidModels(boolean flag) {
		// TODO Auto-generated method stub
		
	}


	public void autoPackagingOfCompleteModels(boolean flag) {
		// TODO Auto-generated method stub
		
	}


	public void setIncomingDataSupervisionDir(String dir) {
		// TODO Auto-generated method stub
		
		fileOrganizer.update();
	}

	
	public void setIncomingDataSupervisionActive(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
}
