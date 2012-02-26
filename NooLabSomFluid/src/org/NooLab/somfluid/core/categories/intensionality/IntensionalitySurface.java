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

	public ArrayList<Double> getUseWeights() {
		return useWeights;
	}

	public void setUseWeights(ArrayList<Double> useWeights) {
		this.useWeights = useWeights;
	}
	
}
