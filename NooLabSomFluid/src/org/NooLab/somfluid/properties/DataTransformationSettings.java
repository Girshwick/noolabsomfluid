package org.NooLab.somfluid.properties;

import java.io.Serializable;



public class DataTransformationSettings implements Serializable{

	private static final long serialVersionUID = -5270246047837401560L;

	
	int maxNveGroupCount = 32 ;


	// ========================================================================
	public DataTransformationSettings(ModelingSettings modelingSettings){
		
	}
	// ========================================================================

	public int getMaxNveGroupCount() {
		return maxNveGroupCount;
	}


	public void setMaxNveGroupCount(int maxNveGroupCount) {
		this.maxNveGroupCount = maxNveGroupCount;
	}
	
	
}
