package org.NooLab.somfluid;

import java.io.Serializable;


public class SomFluidTask 	implements 
										Serializable,
										SomFluidMonoTaskIntf,
										SomFluidProbTaskIntf{

	private static final long serialVersionUID = -532876083346213662L;

	
	String guidID="";
	
	int startingMode = -1;
	
	boolean isCompleted=false;
	int somType;
	
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

}
