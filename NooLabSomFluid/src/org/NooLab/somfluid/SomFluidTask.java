package org.NooLab.somfluid;

import java.io.Serializable;


public class SomFluidTask 	implements 
										Serializable,
										SomFluidMonoTaskIntf,
										SomFluidProbTaskIntf{

	String guidID="";
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
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setStart(int startingMode) {
		// TODO Auto-generated method stub
		
	}

	 
	

	// -----------------------------------

	@Override
	public void setResultsReceptor(SomFluidResultsIntf deliveryTarget) {
		// ?
		
	}

}
