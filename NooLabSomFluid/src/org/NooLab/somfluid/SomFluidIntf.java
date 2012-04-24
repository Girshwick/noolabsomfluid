package org.NooLab.somfluid;


import org.NooLab.somfluid.core.engines.det.*;



public interface SomFluidIntf {
	
	
	/** initializes the virtual lattice according to the properties and starts the internal awareness process,  */
	void start();
 
	//-------------------------------------------------------------------------
	
	public void setUserbreak(boolean flag);

	public boolean getUserbreak() ;
	 
	//-------------------------------------------------------------------------
	
	// returning callback from task processes that implement the interface "somHost", e.g. ModelOptimizer, SimpleSingleModel, ...
	
	public void onTaskCompleted( SomFluidTask sfTask );
	
}