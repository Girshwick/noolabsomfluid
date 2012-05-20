package org.NooLab.somfluid;
 


public interface SomFluidMonoTaskIntf extends SomFluidTaskIntf{

	// TODO create constants for those settings !
	
	
	public void setContinuity(int level, int numberOfRuns);

	public void setContinuity(int level, int derivatesDepth, int numberOfRuns);

	public void setStartMode(int startingMode);
	
	public int getStartMode();

	public String getGuid();
	
	
}
