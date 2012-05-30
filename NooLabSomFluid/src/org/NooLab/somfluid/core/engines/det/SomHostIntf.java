package org.NooLab.somfluid.core.engines.det;

 

import org.NooLab.somfluid.*;
 
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.post.OutResults;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
 



public interface SomHostIntf {


	public void onTargetedModelingCompleted( ModelProperties results );

	// ----------------------------------------------------

	public SomFluidTask getSfTask();
	
	public SomFluidFactory getSfFactory() ;

	public SomFluid getSomFluid();

	public SomFluidProperties getSfProperties();
	
	public ModelProperties getSomResults() ;
	
	public SomDataObject getSomDataObj() ;
	 
	public SomProcessIntf getSomProcess();
	
	// ----------------------------------------------------
	
	public ModelProperties getResults() ;
  
	public String getOutResultsAsXml(boolean asHtmlTable);
}