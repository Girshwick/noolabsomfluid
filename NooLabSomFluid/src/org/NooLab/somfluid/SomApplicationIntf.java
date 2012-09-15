package org.NooLab.somfluid;

import org.NooLab.somfluid.clapp.SomAppModelLoader;
import org.NooLab.somfluid.clapp.SomAppResultAnalyses;
import org.NooLab.somfluid.clapp.SomApplicationEventIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.tasks.SomFluidTask;
import org.NooLab.utilities.callback.ProcessFeedBackIntf;




public interface SomApplicationIntf extends ProcessFeedBackIntf{

	

	public SomDataObject loadSource() throws Exception;

	public boolean loadModel() throws Exception ;

	/** creates a process and returns a task ID */
	public String perform() ;
	
	public boolean checkApplicability();

	// ----------------------------------------------------
	
	public SomAppModelLoader getSomAppModelLoader() ;
	
	public SomFluidTask getSomFluidTask() ;	

	public SomDataObject getSomData() ;
	
	public void setSomData(SomDataObject somdata);

	public SomAppResultAnalyses getResultAnalyses() ;
	
	// ----------------------------------------------------

	public SomApplicationEventIntf getMessagePort();

	public String getMessageProcessGuid();
	

}
