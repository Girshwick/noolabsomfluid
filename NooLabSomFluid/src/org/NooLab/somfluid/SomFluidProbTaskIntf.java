package org.NooLab.somfluid;


public interface SomFluidProbTaskIntf  extends SomFluidTaskIntf{

	public void setStartMode(int startingMode);
	
	public int getStartMode();

	public String getGuid();
	
	public int getCounter();

	public boolean activatedDataStreamReceptor();
	
	public void activateDataStreamReceptor(boolean flag);
}
