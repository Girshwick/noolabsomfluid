package org.NooLab.somfluid.core.categories.intensionality;

import java.util.ArrayList;



public interface IntensionalitySurfaceIntf {

	public ProfileVectorIntf getProfileVector();

	public void prepareWeightVector() ;
	
	public ArrayList<Double> getWeightsVector();

	public void initializeWeightsVector( double defaultValue);

	public void clear( int mode );
	
}
