package org.NooLab.somfluid.app;

import org.NooLab.somfluid.SomApplicationResults;
import org.NooLab.somfluid.core.engines.det.DSom;





public interface SomApplicationBasicsIntf {

	public void setDSomInstance( DSom dsom) ;
	
	public SomApplicationResults getResultObject( String guidStr );

	public void waitForResults(boolean flag);
	
	
}
