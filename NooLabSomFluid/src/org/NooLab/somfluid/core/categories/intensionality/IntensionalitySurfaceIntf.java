package org.NooLab.somfluid.core.categories.intensionality;

import java.util.ArrayList;



public interface IntensionalitySurfaceIntf {

	public ProfileVectorIntf getProfileVector();

	public void prepareWeightVector() ;
	
	public ArrayList<Double> getWeightsVector();

	public void initializeWeightsVector( double defaultValue);

	public void setUsageIndicationVector(ArrayList<Double> usevector);	

	public ArrayList<Double> getUsageIndicationVector();	

	/** usageIndicator>=1 : return only the profile values of those variables that are used */
	public ArrayList<Double> getValues(int usageIndicator);

	/** usageIndicator>=1 : return only the labels of those variables that are used */
	public ArrayList<String> getVariablesStr( int usageIndicator);

	public void clear( int mode );

	/**
	 * describes whether the node is a qualified container of targets, i.e. whether the node satisfies the ecr condition
	 */
	public void setTargetStatus(boolean istarget);
	
	public boolean isQualifiedTarget() ;

	public void setTargetVariableIndex(int indexVal);
	
	public int getTargetVariableIndex();
	
}
