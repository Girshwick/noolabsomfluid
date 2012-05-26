package org.NooLab.somfluid;

import org.NooLab.somfluid.app.SomAppModelLoader;
import org.NooLab.somfluid.components.SomDataObject;




public interface SomApplicationIntf {

	
	
	public boolean checkApplicability();
	
	public boolean loadModel() throws Exception ;
	
	public SomAppModelLoader getSomAppModelLoader() ;
	
	public SomDataObject getSomData() ;
	
	/** creates a process and returns a task ID */
	public String perform() ;
	

}
