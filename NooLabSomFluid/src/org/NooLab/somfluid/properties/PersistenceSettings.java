package org.NooLab.somfluid.properties;

import java.io.Serializable;



public class PersistenceSettings implements Serializable{

	private static final long serialVersionUID = -2361104769448054255L;

	String pathToSomFluidSystemRootDir = "";

	private String projectName;
	
	// ========================================================================
	public PersistenceSettings(){
		
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
	}


	public void setProjectName(String string) {
		 
		projectName = string;
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


	public void autoSaveSomFluidModels(boolean b) {
		// TODO Auto-generated method stub
		
	}


	public void autoPackagingOfCompleteModels(boolean b) {
		// TODO Auto-generated method stub
		
	}


	public void setIncomingDataSupervisionDir(String string) {
		// TODO Auto-generated method stub
		
	}

	
	public void setIncomingDataSupervisionActive(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
