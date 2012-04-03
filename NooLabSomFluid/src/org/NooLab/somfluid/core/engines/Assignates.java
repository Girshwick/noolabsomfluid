package org.NooLab.somfluid.core.engines;

import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;



/**
 * 
 * later used for handling virtual variables as created by SomTransformer
 * 
 */
public class Assignates {

	DSom dSom;
	Variables variables ;
	ModelingSettings modelingSettings;
	
	PrintLog out;
	
	// ========================================================================
	public Assignates( DSom dsom ) {
	
		dSom = dsom ;
		
		variables = dSom.getSomData().getVariables();
		modelingSettings = dSom.getModelingSettings() ;
		
		out = dSom.getOut();
	}
	// ========================================================================	
	
	

}
