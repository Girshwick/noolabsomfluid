package org.NooLab.somfluid.components.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somfluid.properties.OptimizerSettings;
import org.NooLab.somscreen.EvoMetrices;
import org.NooLab.somscreen.SomQualityData;




public class VariableContributions implements Serializable{
 
	private static final long serialVersionUID = -5368622125489520247L;

 
	transient SomHostIntf somHost ;
	SomDataObject somData;
	private EvoMetrices evometrices ;
	
	ModelingSettings modelingSettings;
	OptimizerSettings optimizerSettings ;
	SomFluidProperties sfProperties;
	
	ArrayList<VariableContribution> items = new ArrayList<VariableContribution>();
	
	private double baseScore = -1.0;
	SomQualityData bestSqData ;

	private ArrayList<String> baseMetric; 

	private String resultAsStringTable ="" ;

	// ========================================================================
	public VariableContributions(SomHostIntf somhost, EvoMetrices em) {
		 
		somHost = somhost;
		setEvometrices(em) ;
		
		somData = somHost.getSomDataObj() ;
		
		sfProperties = somHost.getSomFluid().getSfProperties() ;
		modelingSettings = sfProperties.getModelingSettings() ;
		optimizerSettings = modelingSettings.getOptimizerSettings() ;
		
		setEvometrices(new EvoMetrices(somhost, 1)); 
		
	}
	// ========================================================================

	
	public int size() {
		return items.size();
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


	public ArrayList<VariableContribution> getItems() {
		return items;
	}
 	
	public EvoMetrices getEvometrices() {
		return evometrices;
	}


	public void setEvometrices(EvoMetrices evometrices) {
		this.evometrices = new EvoMetrices(evometrices,false);
	}


	public VariableContribution getItem(int index) {
		VariableContribution vc=null;
		if ((index>=0) && (index<items.size())){
			vc = items.get(index);
		}
		return vc;
	}

	public String getResultStringTable( ) {
		return resultAsStringTable;
	}
	public void setResultStringTable( String tablestr) {
		resultAsStringTable = tablestr ;
	}

	public void setBaseScore(double baseScore) {
		this.baseScore = baseScore;
	}


	public double getBaseScore() {
		return baseScore;
	}


	public SomDataObject getSomData() {
		return somData;
	}


	public void setSomData(SomDataObject somData) {
		this.somData = somData;
	}


	public ModelingSettings getModelingSettings() {
		return modelingSettings;
	}


	public void setModelingSettings(ModelingSettings modelingSettings) {
		this.modelingSettings = modelingSettings;
	}


	public OptimizerSettings getOptimizerSettings() {
		return optimizerSettings;
	}


	public void setOptimizerSettings(OptimizerSettings optimizerSettings) {
		this.optimizerSettings = optimizerSettings;
	}


	public SomFluidProperties getSfProperties() {
		return sfProperties;
	}


	public void setSfProperties(SomFluidProperties sfProperties) {
		this.sfProperties = sfProperties;
	}


	public void setItems(ArrayList<VariableContribution> items) {
		this.items = items;
	}


	@SuppressWarnings("rawtypes")
	class VcComparator implements Comparator,Serializable{
 
		private static final long serialVersionUID = -6517932582765384283L;
		
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


		public int getDirection() {
			return direction;
		}


		public void setDirection(int direction) {
			this.direction = direction;
		}
		
	}


	public void setBaseMetric(ArrayList<String> varSelection) {
		baseMetric = varSelection;
		
	}


	public ArrayList<String> getBaseMetric() {
		return baseMetric;
	}


	public SomQualityData getBestSqData() {
		return bestSqData;
	}

	public void setBestSqData(SomQualityData sqData) {
	
		bestSqData = new SomQualityData( sqData ) ;
	}


	 

}
