package org.NooLab.somscreen;

import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * This class determines the relevance weights of variables by means of
 * an evolutionary screening;
 * The weight of a variable reprsents the probability that this variable
 * improves the quality of a model. 
 * 
 *
 */
public class SomScreening {

	public static final int _SEL_TOP = 1;
	public static final int _SEL_DIVERSE = 2;

	DSom dSom;
	ModelingSettings modelingSettings;
	
	int totalSelectionSize;
	int[] selectionMode;
	
	PrintLog out;
	
	
	// ========================================================================
	public SomScreening(DSom dsom, ModelingSettings modset) {
	
		dSom = dsom;
		modelingSettings = modset ;
		
		out = dSom.getOut() ;
	}
	// ========================================================================
	
	
	public void startScreening() {
		
	}

	public void setModelResultSelection(int[] selectionmode) {
		// TODO Auto-generated method stub
		selectionMode = selectionmode ;
	}


	public void setModelResultSelectionSize(int selsize) {
		
		totalSelectionSize = selsize ;
	}

}
