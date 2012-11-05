package org.NooLab.somfluid.app.astor;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.tasks.SomFluidTask;





public interface SomAstorFrameIntf {
	
	public static final int _ASTOR_SRCMODE_FILE = 0;
	public static final int _ASTOR_SRCMODE_DB   = 1;
	
	
	public SomFluidTask getSfTask();
	
	public SomFluidProperties getSfProperties() ;

	public SomDataObject getSomDataObj() ;
	
}
