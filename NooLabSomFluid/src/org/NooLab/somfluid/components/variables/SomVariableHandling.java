package org.NooLab.somfluid.components.variables;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.ClassificationSettings;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.DSomCore;
import org.NooLab.somfluid.data.DataSampler;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.lattice.VirtualLattice;
import org.NooLab.somfluid.properties.ModelingSettings;
import org.NooLab.somtransform.algo.AdaptiveDiscretization;
import org.NooLab.utilities.logging.PrintLog;

public class SomVariableHandling {

	
	 
	SomDataObject somData;
	
	AdaptiveDiscretization adaptiveGroups ;
	
	VirtualLattice somLattice ;
	
	ModelingSettings modelingSettings;
	ClassificationSettings classifySettings;
	
	DataSampler dataSampler;
	
	double[][] tgSettings ;
	
	
	PrintLog out = new PrintLog(2,true);
	
	// ========================================================================
	 
	
	public SomVariableHandling( SomDataObject sdo, ModelingSettings modset){
		
		somData = sdo;
		modelingSettings = modset ;
		classifySettings = modelingSettings.getClassifySettings() ;
	}
	
	// ========================================================================

	public void determineSampleSizes( int nodecount){
		int maxSampleSize;
		int tvIndex  ;
		DataTable nData, rData ;
		ArrayList<Double> nDataValues ;
		ArrayList<Integer> sampleRecordIndexes;
		
		
		maxSampleSize = Math.max( nodecount  * 15 , 101) ;
		// tvIndex = dSom.getSomLattice().getSimilarityConcepts().getIndexTargetVariable();
		
	}
	
	public void getEmpiricTargetGroups(boolean enforceRecalc) {

		int maxSampleSize, tvIndex=-1;
		String tvLabel="" ;
		double min,max ;
		
		DataTable nData, rData ;
		ArrayList<Double> nDataValues , rDataValues    ;
		ArrayList<Integer> sampleRecordIndexes;
		ArrayList<Double> tvNominalCore, intervalBoundaries;
		
		// ......................................
		
		tvLabel = modelingSettings.getActiveTvLabel() ;
		
		tvIndex = somData.getActiveVariables().getIndexByLabel(tvLabel) ;
		 
		dataSampler = new  DataSampler();
		maxSampleSize = this.somData.getDataTable().getRowcount()-1 ;
		
		double sp = 1.0 ;
		sampleRecordIndexes = dataSampler.createEffectiveRecordList( 1, maxSampleSize, sp);
		
		nData = somData.getNormalizedSomData() ;
		rData = somData.getData() ;
		
		nDataValues = nData.getColumn(tvIndex).getCellValues();
		rDataValues = rData.getColumn(tvIndex).getCellValues();
		 
		
		int tm = modelingSettings.getClassifySettings().getTargetMode();
		
		if (tm == ClassificationSettings._TARGETMODE_MULTI){
		
			double[][] tgDef = classifySettings.getTGdefinition();
			if ( (tgDef==null) || (tgDef.length <= 2) || (enforceRecalc)){ 
				
				// we use the frequency technique, and restricted sampling, max 5000 records, 2000 of them sampled randomly
				adaptiveGroups = new AdaptiveDiscretization( somData, modelingSettings, sampleRecordIndexes );
				adaptiveGroups.hisPolyMinMax( nDataValues, tvIndex) ;
				
				 
				
				tvNominalCore = adaptiveGroups.getNominalSupportValues() ;
				if ((tvNominalCore!=null) && (tvNominalCore.size()>0)){
					// these are the support points, now we have to infer the interval boundaries...
					intervalBoundaries = createEmbeddingIntervalBorders( tvNominalCore ) ;
					
					if ((intervalBoundaries!=null) && (intervalBoundaries.size()>0)){
						
						//intervalBoundaries
						tgDef = new double[intervalBoundaries.size()-1][2] ; 
						
						for (int i=1;i<intervalBoundaries.size();i++){
							min = intervalBoundaries.get(i-1) ;
							max = intervalBoundaries.get(i);
							tgDef[i-1][0] = min;
							tgDef[i-1][1] = max;
						}
						
						tgSettings = tgDef ;
						modelingSettings.getClassifySettings().setTargetGroupDefinition(tgDef);
					}
					
				}
				 
			} // only a few TV groups?
		} // are we in target mode multi
		
		 
		
	}



	private ArrayList<Double> createEmbeddingIntervalBorders( ArrayList<Double> corevalues) {
		
		ArrayList<Double> boundaries = new ArrayList<Double>();
		int s=0;
		double v;
		
		
		
		try{
			
			Collections.sort(corevalues);
			
			if ((corevalues.get(0)<=0.000001) && (corevalues.get(0)>=0)){
				boundaries.add(0.0);
				s=1;
			}else{
				v = (corevalues.get(0)/2.0);
				boundaries.add(v);
				s=1;
			}

			if (corevalues.get(corevalues.size()-1)>=1.0){
				boundaries.add(1.0000000001);
				s=1;
			}else{
				v = ((corevalues.get(corevalues.size()-1)+1.0)/2.0);
				boundaries.add(v);
				s=1;
			}

			
			for (int i=1;i<corevalues.size();i++){
				v = (corevalues.get(i-1)+corevalues.get(i))/2.0 ;
				
				boundaries.add(v);
			}
			
			Collections.sort(boundaries);
			
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		return boundaries;
	}

	/**
	 * there are different flavors of that, actually, it also can perform "adaptive binning" into a number of groups,
	 * perhaps based on mono-variate clustering (in turn based on the spatial distribution of distances)
	 */
 

	public double[][] getTargetGroups() {
		 
		return tgSettings;
	}
	
	
	
	
	
	
}









