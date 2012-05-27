package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppModelLoader;
import org.NooLab.somfluid.app.SomAppResultAnalyses;
import org.NooLab.somfluid.app.SomApplicationEventIntf;
import org.NooLab.somfluid.components.SomDataObject;
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
