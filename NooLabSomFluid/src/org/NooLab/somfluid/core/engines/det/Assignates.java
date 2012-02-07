package org.NooLab.somfluid.core.engines.det;

import org.NooLab.somfluid.data.ModelingSettings;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.utilities.logging.PrintLog;




public class Assignates {

	DSom dSom;
	Variables variables ;
	ModelingSettings modset;
	
	PrintLog out;
	
	// ========================================================================
	public Assignates( DSom dsom ) {
	
		dSom = dsom ;
		
		variables = dSom.somData.getVariables();
		modset = dSom.modelingSettings ;
		
		out = dSom.out ;
	}
	// ========================================================================	
	
	

}
