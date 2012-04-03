package org.NooLab.somfluid.components;

import java.io.Serializable;

import org.NooLab.somscreen.EvoBasics;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.SomQuality;


/**
 * 
 * describes the model on the level of the variables, given a particular "best" model
 * 
 * - removing any of them singularly,
 * - adding any of them singularly  
 * - correlations in data vs across the map
 * 
 * 
 */
public class SomModelDescription implements Serializable{

	ModelOptimizer moptiParent ;
	
	SomDataObject somData ; 
	
	EvoBasics  evoBasics;
	EvoMetrices evoMetrices ;
	SomQuality somQuality ;

	
	// ========================================================================
	public SomModelDescription( ModelOptimizer mopti ) {

		moptiParent = mopti ;
		
		evoBasics = mopti.evoBasics;
		evoMetrices = mopti.evoMetrices;
		somQuality = mopti.somQuality;
		
		somData = mopti.getSomDataObj() ;
	}
	// ========================================================================
	
	
	
	public void calculate() {
		
		
	}

}
