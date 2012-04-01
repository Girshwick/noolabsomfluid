package org.NooLab.somscreen;

import java.util.ArrayList;

import org.NooLab.somfluid.properties.*;
import org.NooLab.somfluid.data.*;
import org.NooLab.somfluid.core.*;
import org.NooLab.somfluid.core.engines.det.*;
import org.NooLab.somfluid.core.engines.det.results.*;
import org.NooLab.somfluid.components.*;


/**
 * 
 * this class determines the quality of a model,
 * dependent on the type of the model, the modeling mode, the risk settings
 * 
 */
public class SomQuality implements QualityDescriptionIntf{

	// DSom dSom;
	SomProcessIntf somProcess;
	
	VirtualLattice somLattice;
	SomDataObject somData;

	ModelingSettings modelingSettings;
	ClassificationSettings classificationSettings;

	ArrayList<Double> usagevector ;
	
	ModelProperties modelProperties ;
	ValidationSet tsetResults;
	ValidationSet vsetResults;
	
	int useCount, accessibleVarCount;
	
	int metricSize = 1 ;
	SomQualityData somQualityData;
	/*
	int targetMode ;
	double score = -1.0;
	
	// result parameters from classification
	int tp, fn , fp ,tn,ccases, samplesize;
	double rocAuC, tpSingularity , ecrRelTP;
	
	double ecr ;
	*/
	// ========================================================================
	public SomQuality( SomProcessIntf somprocess, ArrayList<Double> usevector ) {
		
		// dSom = dsom;
		somProcess = somprocess;
		 
		usagevector = new ArrayList<Double>(usevector);

		init();
	}
	
	public SomQuality(SomQuality sq) {
		 
		// dSom = sq.dSom;
		somProcess = sq.somProcess ;
		init();
	}
	
	private void init(){
		
		somData    = somProcess.getSomDataObject() ;
		somLattice = somProcess.getSomLattice() ;
		
		
		modelingSettings = somProcess.getSfProperties().getModelingSettings() ;
		classificationSettings = modelingSettings.getClassifySettings() ;
		
		somQualityData = new SomQualityData();
		
		somQualityData.targetMode =  classificationSettings.getTargetMode() ;
		// targetMode:  ClassificationSettings._TARGETMODE_SINGLE  _TARGETMODE_REGR
		
		 
		
		acquireResultValues();
	}
	// ========================================================================
	
	

	private void acquireResultValues(){
		
		
		modelProperties = somLattice.getModelProperties();
		
		if ( (somQualityData.targetMode == ClassificationSettings._TARGETMODE_SINGLE) || 
			 ((somQualityData.targetMode == ClassificationSettings._TARGETMODE_MULTI))){
			
			calculateClassificationScore(metricSize);
		}
		
		if (somQualityData.targetMode == ClassificationSettings._TARGETMODE_REGR){
			
		}
		
	}
	
	private void calculateClassificationScore( int metricSize){               
		
		Variable variable;
		double v, vmax,sizeCost=1.0;
		boolean isBlack;
		String vstr;
		
		tsetResults = somLattice.getModelProperties().getTrainingSample() ;
		vsetResults = somLattice.getModelProperties().getValidationSample() ;
		
		useCount=0;
		accessibleVarCount=0;
		// based on ECR we calculate the cost score
		// counting non-black, non-id, non-tv variables
		for (int i=0;i<usagevector.size();i++){
			variable = somData.getVariables().getItem(i);
			vstr = variable.getLabel() ;
			isBlack = somData.getVariables().getBlacklistLabels().indexOf(vstr)>=0;
			
			if ((isBlack) || (variable.isID()) || (variable.isIndexcandidate()) || (variable.isTV()) || (variable.isTVcandidate() )){
				continue;
			}
			accessibleVarCount++;
			if (usagevector.get(i)>0){
				useCount++;
			}
		}
		
		double useRatio = (double)useCount / (double)accessibleVarCount ;
		if ((useCount>10) || 
			((useCount>=4) && (useRatio>0.6)) ){
			
		sizeCost = 0.4 * Math.sqrt( Math.min( 1.0, useRatio) );
		if (useCount>20){
			sizeCost = sizeCost + 0.6 * Math.sqrt( Math.min( 1.0, (double)(useCount-21) / (double)(accessibleVarCount-20) ));
		}
		}else{
			sizeCost = 0.1 ;
		}
		
		
		somQualityData.tp = tsetResults.getTruePositives();
		somQualityData.tn = tsetResults.getTrueNegatives() ;
		somQualityData.fn = tsetResults.getFalseNegatives() ;
		somQualityData.fp = tsetResults.getFalsePositives() ;
		somQualityData.ccases = tsetResults.getCasesCount() ;
		somQualityData.samplesize = tsetResults.getSampleSize(); 
		somQualityData.ecr = somLattice.getModelProperties().getEcr();
		
		// example: ecr = 0.01 -> fp very expensive -> tendency for models without fp 
		// ==> strong cleaning, low risk, but also low coverage TODO: coverage at ecr-point
		v = ((1.0-somQualityData.ecr) * (double)somQualityData.fp) + ((somQualityData.ecr) * (double)somQualityData.fn)  ;
		
		vmax = (somQualityData.ecr * (double)somQualityData.ccases) + ((1.0-somQualityData.ecr) * ((double)somQualityData.samplesize/2.0)) ;
		somQualityData.score =  v/vmax ;
		
		
		somQualityData.rocAuC = tsetResults.getRoc().getAuC() ;
		somQualityData.tpSingularity = tsetResults.getTpSingularity();
			
		somQualityData.ecrRelTP = tsetResults.getEcrRelTP() ;
		

		double costsum = (  0.1 * useRatio + 
							4.0 * somQualityData.score + 
				            2.7 * (1.0-somQualityData.tpSingularity) + 
				            1.0 * (1.0-(double)somQualityData.ecrRelTP) + 
				            0.5 * (1.0-somQualityData.rocAuC)+
				            0.2 * (double)somQualityData.fp/((double)(somQualityData.samplesize-somQualityData.ccases))) ;
		somQualityData.score = costsum/8.5;
		
		// somQualityData.score = somQualityData.score* sizeCost;
		
		somQualityData.score = somQualityData.score*100.0;
		somQualityData.score = Math.round( 10000.0*somQualityData.score)/10000.0;
		
	}

	/**
	 * @return the somQualityData
	 */
	public SomQualityData getSomQualityData() {
		return somQualityData;
	}

	/**
	 * @return the modelProperties
	 */
	public ModelProperties getModelProperties() {
		return modelProperties;
	}

	/**
	 * @return the useCount
	 */
	public int getUseCount() {
		return useCount;
	}

	/**
	 * @return the accessibleVarCount
	 */
	public int getAccessibleVarCount() {
		return accessibleVarCount;
	}

	/**
	 * @return the metricSize
	 */
	public int getMetricSize() {
		return metricSize;
	}
	
}










