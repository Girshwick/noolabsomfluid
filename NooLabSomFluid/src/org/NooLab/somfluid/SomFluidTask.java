package org.NooLab.somfluid;

import java.io.Serializable;


public class SomFluidTask 	implements 
										Serializable,
										SomFluidMonoTaskIntf,
										SomFluidProbTaskIntf{

	private static final long serialVersionUID = -532876083346213662L;


	public static final int _SOM_WORKMODE_FILE   = 1;
	public static final int _SOM_WORKMODE_PIKETT = 2; // either by dir source or glue

	public int workingMode = _SOM_WORKMODE_FILE;
	
	
	String guidID="";
	
	int startingMode = -1;
	
	int callerStatus = 0;
	
	boolean isCompleted=false;
	int somType;


	public boolean isStandbyActive =false;


	private int spelaLevel;

	private int numberOfRuns;

	private int derivatesDepth;


	
	
	// ========================================================================
	protected SomFluidTask(String guidId, int somType){
		
		guidID = guidId;
		this.somType = somType;
		
	}
	// ========================================================================


	
	public boolean isCompleted() {
		// 
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}


	public String getGuidID() {
		return guidID;
	}


	@Override
	public void setContinuity(int level, int numberOfRuns) {
		//  
		spelaLevel = level;
		this.numberOfRuns = numberOfRuns ;
	}

 

	@Override
	public void setContinuity(int level, int derivatesDepth, int numberOfRuns) {

		spelaLevel = level;
		this.numberOfRuns = numberOfRuns ;
		this.derivatesDepth = derivatesDepth;
	}



	@Override
	public void setStartMode(int startingmode) {
		
		startingMode = startingmode;
	}



	@Override
	public int getStartMode() {
		
		return startingMode;
	}
	

	// -----------------------------------
 


	@Override
	public void setResultsReceptor(SomFluidResultsIntf deliveryTarget) {
		// ?
		
	}



	public int getCallerStatus() {
		return callerStatus;
	}



	public void setCallerStatus(int callerStatus) {
		this.callerStatus = callerStatus;
	}



	/**
	 * @return the workingMode
	 */
	public int getWorkingMode() {
		return workingMode;
	}



	/**
	 * @param workingMode the workingMode to set
	 */
	public void setWorkingMode(int workingMode) {
		this.workingMode = workingMode;
	}



	/**
	 * @return the startingMode
	 */
	public int getStartingMode() {
		return startingMode;
	}



	/**
	 * @param startingMode the startingMode to set
	 */
	public void setStartingMode(int startingMode) {
		this.startingMode = startingMode;
	}



	/**
	 * @return the somType
	 */
	public int getSomType() {
		return somType;
	}



	/**
	 * @param somType the somType to set
	 */
	public void setSomType(int somType) {
		this.somType = somType;
	}



	/**
	 * @return the isStandbyActive
	 */
	public boolean isStandbyActive() {
		return isStandbyActive;
	}



	/**
	 * @param isStandbyActive the isStandbyActive to set
	 */
	public void setStandbyActive(boolean isStandbyActive) {
		this.isStandbyActive = isStandbyActive;
	}



	/**
	 * @return the spelaLevel
	 */
	public int getSpelaLevel() {
		return spelaLevel;
	}



	/**
	 * @param spelaLevel the spelaLevel to set
	 */
	public void setSpelaLevel(int spelaLevel) {
		this.spelaLevel = spelaLevel;
	}



	/**
	 * @return the numberOfRuns
	 */
	public int getNumberOfRuns() {
		return numberOfRuns;
	}



	/**
	 * @param numberOfRuns the numberOfRuns to set
	 */
	public void setNumberOfRuns(int numberOfRuns) {
		this.numberOfRuns = numberOfRuns;
	}



	/**
	 * @param guidID the guidID to set
	 */
	public void setGuidID(String guidID) {
		this.guidID = guidID;
	}



	/**
	 * @return the derivatesDepth
	 */
	public int getDerivatesDepth() {
		return derivatesDepth;
	}



	/**
	 * @param derivatesDepth the derivatesDepth to set
	 */
	public void setDerivatesDepth(int derivatesDepth) {
		this.derivatesDepth = derivatesDepth;
	}

}
