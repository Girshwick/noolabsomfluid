package org.NooLab.somfluid.components;

import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somscreen.EvoMetrices;







public class VariableContributions {

	SomHostIntf somHost ;
	SomDataObject somData;
	EvoMetrices evometrices ;
	
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	SomFluidProperties sfProperties;
	
	ArrayList<VariableContribution> items = new ArrayList<VariableContribution>();
	
	double baseScore = -1.0; 
	
	
	// ========================================================================
	public VariableContributions(SomHostIntf somhost, EvoMetrices em) {
		 
		somHost = somhost;
		evometrices = em ;
		
		somData = somHost.getSomDataObj() ;
		
		sfProperties = somHost.getSomFluid().getSfProperties() ;
		modelingSettings = sfProperties.getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		evometrices = new EvoMetrices(somhost, 1); 
		
	}
	// ========================================================================

	
	public ArrayList<VariableContribution> getItems() {
		return items;
	}
 	
	public EvoMetrices getEvometrices() {
		return evometrices;
	}
	
	
}
