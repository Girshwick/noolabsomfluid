package org.NooLab.somfluid;

import org.NooLab.somfluid.core.engines.det.ModelingSettingsIntf;



public interface SomFluidMonoTaskIntf extends ModelingSettingsIntf,
											  ModelingResultsIntf{

	// TODO create constants for those settings !
	
	
	public void setContinuity(int level, int numberOfRuns);

	public void setStartMode(int startingMode);
	
	public int getStartMode();
	
	
}
