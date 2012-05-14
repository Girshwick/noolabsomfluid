package org.NooLab.somfluid.core.engines.det;

 

import org.NooLab.somfluid.*;
 
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.post.OutResults;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
 



public interface SomHostIntf {

	
	public SomFluid getSomFluid();
	
	public void onTargetedModelingCompleted( ModelProperties results );

	public ModelProperties getSomResults() ;
	
	public SomFluidTask getSfTask();
	
	public SomFluidFactory getSfFactory() ;

	public SomDataObject getSomDataObj() ;
	 
	public SomFluidProperties getSfProperties();
	
	public SomProcessIntf getSomProcess();
	
	public ModelProperties getResults() ;
  
	public String getOutResultsAsXml(boolean asHtmlTable);
}