package org.NooLab.somfluid.app;

import org.NooLab.somfluid.SomFluidMonoResultsIntf;
// import org.NooLab.somfluid.SomFluidStateDescriptionIntf;



/**
 * 
 * needs to be implemented by the client to receive the results
 * internally, the observer pattern is used for decoupling
 * 
 */
public interface SomApplicationEventIntf {

	public void onClassificationPerformed( Object resultObject ) ;
	
	public void onResultsCalculated( SomFluidMonoResultsIntf results );
	
	public void onCalculation( double fractionPerformed );
	
}
