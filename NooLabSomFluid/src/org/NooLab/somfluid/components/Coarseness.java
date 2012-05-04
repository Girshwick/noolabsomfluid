package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * the influence of the potential resolution as the maximum number of nodes onto the quality of model
 *  
 * the max number of clusters changes : up 3x, at least 100, down 3
 * 
 * 1. metrics remains constant,
 * 2. metrics get optimized in a small number of steps (stopping if more than 20 models did not improve)   
 * 
 * 
 *
 */
public class Coarseness {

	SomHostIntf somHost;
	ModelOptimizer modelOptimizer ;
	
	SomDataObject somData ;
	EvoMetrices evoMetrices; 
	
	ArrayList<String> baseVariableSelection ;
	EvoMetrices evometrices;
	
	
	
	private PrintLog out;

	// ========================================================================
	public Coarseness(ModelOptimizer moptiParent) {
		
		modelOptimizer = moptiParent;
		somHost = (SomHostIntf)modelOptimizer ;
		somData = somHost.getSomDataObj() ;
		
		evometrices = new EvoMetrices(modelOptimizer, 0);
		
		out = modelOptimizer.out ;
	}
	// ========================================================================

	
	public void setBaseVariableSelection( ArrayList<String> varSelection){
		baseVariableSelection = new ArrayList<String>(varSelection);
	}
	
	
	public void check(){
		
	}


	public void evaluate() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
