package org.NooLab.somfluid.app;

import org.NooLab.somfluid.SomFluidMonoResultsIntf;
// import org.NooLab.somfluid.SomFluidStateDescriptionIntf;
import org.NooLab.somfluid.SomFluidTask;



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
	
	void onProcessStarted( SomFluidTask sfTask, int applicationId, String pid);

	public void onStatusMessage(SomFluidTask sfTask, int applicationId, int errcode, String msg);
	
}
