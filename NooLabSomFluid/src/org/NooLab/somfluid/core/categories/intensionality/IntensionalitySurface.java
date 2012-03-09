package org.NooLab.somfluid.core.categories.intensionality;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * 
 * This is about profiles, weights, usevectors = variable selection 

 * 
 */
public class IntensionalitySurface implements 	Serializable ,
												IntensionalitySurfaceIntf{

	private static final long serialVersionUID = 1349099378078978472L;

	/** a compound object that holds several aspects of a profile vector   */
	ProfileVector profileVector = new ProfileVector() ;

	/**  
	 * the weight vector is NOT the use vector, the weight vector describes the weight of a variable IFF used
	 * the usevector  is also part of similarity class, accessible via" similarity.getUsageIndicationVector()" 
	 */
	ArrayList<Double> useWeights = new ArrayList<Double>();

	private ArrayList<Double> usageIndicationVector;
	
	
	private boolean isQualifiedTarget;

	private int targetVariableIndex = -1 ;
	
	
	// ------------------------------------------------------------------------
	public IntensionalitySurface( ){
		 
	}
	// ------------------------------------------------------------------------

	/**   the weight vector is NOT the use vector, the weight vector describes the weight of a variable IFF used  */
	public void prepareWeightVector(){
		int n = profileVector.values.size();
		for (int i=0;i<n;i++){
			useWeights.add(1.0) ;
		}
	}
	
	
	
	// ------------------------------------------------------------------------
	
	@Override
	public void clear(int mode) {
		// 
		
	}

	@Override
	public ProfileVectorIntf getProfileVector(){
		return profileVector;
	}
 
	@Override
	public ArrayList<Double> getWeightsVector() {
		return useWeights;
	}


	@Override
	public void initializeWeightsVector(double defaultValue) {
		prepareWeightVector();
		
	}

	@Override
	public ArrayList<String> getVariablesStr(int usageIndicator) {
		ArrayList<String> usedVariables ;
		
		usedVariables = profileVector.getVariablesStr() ;
		 
		int i=usedVariables.size()-1;
		while (i>=0){
			if (usageIndicationVector.get(i) <= 0.0){
				usedVariables.remove(i) ;
			}
			i--;
		} // i-> all variables
		
		return usedVariables;
	}

	@Override
	public ArrayList<Double> getValues(int usageIndicator) {
		ArrayList<Double> usedVarValues ;
		
		usedVarValues = profileVector.getValues() ;
		
		int i=usedVarValues.size()-1;
		
		while (i>=0){
			if (usageIndicationVector.get(i) <= 0.0){
				usedVarValues.remove(i) ;
			}
			i--;
		} // i-> all variables
		
		return usedVarValues;
	}
	
	@Override
	public void setUsageIndicationVector(ArrayList<Double> usevector) {
		usageIndicationVector = usevector; // it is a copy, not just a reference, as it originates in the similarity part ...
										   // but the active TV is excluded here, since it is used only for application purposes	
	}
 
	@Override
	public ArrayList<Double> getUsageIndicationVector() {
		 
		return usageIndicationVector;
	}
	@Override
	public void setTargetStatus(boolean istarget) {
		isQualifiedTarget = istarget;
		
	}

	public boolean isQualifiedTarget() {
		return isQualifiedTarget;
	}
	public ArrayList<Double> getUseWeights() {
		return useWeights;
	}

	public void setUseWeights(ArrayList<Double> useWeights) {
		this.useWeights = useWeights;
	}

	public boolean getQualifiedTarget() {
		return isQualifiedTarget;
	}

	public void setQualifiedTarget(boolean isQualifiedTarget) {
		this.isQualifiedTarget = isQualifiedTarget;
	}

	@Override
	public void setTargetVariableIndex(int indexVal) {
		targetVariableIndex = indexVal;
	}

	@Override
	public int getTargetVariableIndex() {
		return targetVariableIndex;
	}
	
}
