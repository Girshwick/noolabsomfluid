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

	ProfileVector profileVector;

	ArrayList<Double> useWeights = new ArrayList<Double>();
	
	
	// ------------------------------------------------------------------------
	public IntensionalitySurface(){
		
		profileVector = new ProfileVector();
		prepareWeightVector( );
	}
	// ------------------------------------------------------------------------

	
	public void prepareWeightVector( ){
		
		for (int i=0;i<profileVector.values.size();i++){
			useWeights.add(1.0) ;
		}
	}
	
	
	
	// ------------------------------------------------------------------------
	
	@Override
	public ProfileVectorIntf getProfileVector(){
		return profileVector;
	}
 
	@Override
	public ArrayList<Double> getWeightsVector() {
		return useWeights;
	}
	
}
