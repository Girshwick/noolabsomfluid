package org.NooLab.somfluid.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

	
	public int size() {
		return items.size();
	}


	public ArrayList<VariableContribution> getItems() {
		return items;
	}
 	
	public EvoMetrices getEvometrices() {
		return evometrices;
	}


	public VariableContribution getItem(int index) {
		VariableContribution vc=null;
		if ((index>=0) && (index<items.size())){
			vc = items.get(index);
		}
		return vc;
	}


	@SuppressWarnings("unchecked")
	public void sort(int direction) {
		 
		if (direction<-1){
			direction=-1;
		}else{
			if (direction> 1){
				direction= 1;
			}else{
				if (direction!=0)direction = 0;
			}
		}
		
		Collections.sort(items, new VcComparator(direction));
		
	}
	

	@SuppressWarnings("rawtypes")
	class VcComparator implements Comparator{

		int direction=0;
		
		public VcComparator(int dir){
			direction = dir;
		}

		
		@Override
		public int compare(Object obj1, Object obj2) {
			
			int result = 0;
			VariableContribution vc2,vc1;
			double v1,v2 ;
			
			vc1 = (VariableContribution)obj1;
			vc2 = (VariableContribution)obj2;
			
			v1 = vc1.getScoreDelta() ;
			v2 = vc2.getScoreDelta() ;
			
			if (direction>=0){
				if (v1>v2){
					result = -1;
				}else{
					if (v1<v2){
						result = 1 ;
					}
				}
			}else{
				if (v1>v2){
					result = 1;
				}else{
					if (v1<v2){
						result = -1 ;
					}
				}
				
			}
			
			return result;
		}
		
	}

}
