package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;
import java.util.ArrayList;



public class ValidationSet  implements Serializable{

	private static final long serialVersionUID = -5194957520207952879L;
	 
	/** describes the limit for FP as a % of all FP. For the respective sum the corresponding TP value will be determined;</br>
	 * the resulting values are stored in "ecrScFpCompliance" within the train\val-set in ModelProperties
	 */
	double smallCostFpLimit = 2.5;
	
	  
	/** describes the limit for the observations as a % of all observations (=quantil). </br>
	 * For the respective sum the corresponding TP value will be determined;</br>
	 * the resulting values are stored in "ecrScQuCompliance" within the train\val-set in ModelProperties
	 */
	double smallCostObsQuantilLimit = 5.0; 
 
	
	int casesCount;
	int truePositives;
	int trueNegatives;

	int falsePositives;
	int falseNegatives;
	
	int sampleSize ;
	
	RoC roc = new RoC();

	int observationCount;

	// some measures that describe the SOM as far as tohse nodes are concerned which 
	// satisfy the ecr criterion, it is a particular point like the singularity TP @ FP=0
	int ecrTP ;
	int ecrFP  ;
	double ecrRelSize ;
	double ecrRelTP ;
	double ecrRelRisk ;
	
	// related to the ecr measures: the singularity
	double tpSingularity , tpFpX;
	
	public ArrayList<Integer> ecrNodes;   // 
	
	/**    */ 
	EcrCompliance  ecrCompliance;

	/**  data filtered by the value for smallCostFpLimit (a value for FP in the RoC) : 
	 *   only data beow this threshold are considered */ 
	EcrCompliance  ecrScFpCompliance;
	
	/**  data filtered by the value for smallCostObsQuantilLimit (a value for the quantil in the RoC): 
	 *   only data beow this threshold are considered */  
	EcrCompliance  ecrScQuCompliance;
	
	// ------------------------------------------------------------------------
	public ValidationSet(){
		
		ecrCompliance     =  new EcrCompliance();
		ecrScFpCompliance =  new EcrCompliance();
		ecrScQuCompliance =  new EcrCompliance();
	}

	public ValidationSet( ValidationSet vset){

		casesCount = vset.casesCount  ;
		truePositives = vset.truePositives  ;
		trueNegatives = vset.trueNegatives  ;

		falsePositives = vset.falsePositives  ;
		falseNegatives = vset.falseNegatives  ;
		
		sampleSize = vset.sampleSize  ;
		
		roc = new RoC(vset.roc);

		observationCount = vset.observationCount  ;

		ecrTP = vset.ecrTP  ;
		ecrFP = vset.ecrFP  ;
		ecrRelSize = vset.ecrRelSize  ;
		ecrRelTP = vset.ecrRelTP  ;
		ecrRelRisk = vset.ecrRelRisk  ;
		
		tpSingularity = vset.tpSingularity  ;
		
		tpFpX = vset.tpFpX  ;
		
		ecrCompliance = new EcrCompliance(vset.ecrCompliance) ;
		ecrScFpCompliance = new EcrCompliance(vset.ecrScFpCompliance) ;
		ecrScQuCompliance = new EcrCompliance(vset.ecrScQuCompliance) ;
		
	}
	// ------------------------------------------------------------------------
	

	public int getCasesCount() {
		return casesCount;
	}

	public void setCasesCount(int casesCount) {
		this.casesCount = casesCount;
	}

	public int getSampleSize() {
		return sampleSize;
	}
	/**
	 * @param sampleSize the sampleSize to set
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}


	public int getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int truePositives) {
		this.truePositives = truePositives;
	}

	public int getTrueNegatives() {
		return trueNegatives;
	}

	public void setTrueNegatives(int trueNegatives) {
		this.trueNegatives = trueNegatives;
	}

	public int getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(int falsePositives) {
		this.falsePositives = falsePositives;
	}

	public int getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(int falseNegatives) {
		this.falseNegatives = falseNegatives;
	}


	public RoC getRoc() {
		return roc;
	}


	public void setRoc(RoC roc) {
		this.roc = roc;
	}


	public int getObservationCount() {
		return observationCount;
	}


	public void setObservationCount(int observationCount) {
		this.observationCount = observationCount;
	}


	public ArrayList<Integer> getEcrNodes() {
		return ecrNodes;
	}


	public void setEcrNodes(ArrayList<Integer> ecrNodes) {
		this.ecrNodes = ecrNodes;
	}


	public int getEcrTP() {
		return ecrTP;
	}


	public void setEcrTP(int ecrTP) {
		this.ecrTP = ecrTP;
	}


	public int getEcrFP() {
		return ecrFP;
	}


	public void setEcrFP(int ecrFP) {
		this.ecrFP = ecrFP;
	}


	public double getEcrRelSize() {
		return ecrRelSize;
	}


	public void setEcrRelSize(double ecrRelSize) {
		this.ecrRelSize = ecrRelSize;
	}


	public double getEcrRelTP() {
		return ecrRelTP;
	}


	public void setEcrRelTP(double ecrRelTP) {
		this.ecrRelTP = ecrRelTP;
	}


	public double getEcrRelRisk() {
		return ecrRelRisk;
	}


	public void setEcrRelRisk(double ecrRelRisk) {
		this.ecrRelRisk = ecrRelRisk;
	}


	public double getTpSingularity() {
		return tpSingularity;
	}


	public void setTpSingularity(double tpSingularity) {
		this.tpSingularity = tpSingularity;
	}

	/**
	 * @return the tpFpX
	 */
	public double getTpFpX() {
		return tpFpX;
	}

	/**
	 * @param tpFpX the tpFpX to set
	 */
	public void setTpFpX(double tpFpX) {
		this.tpFpX = tpFpX;
	}

	/**
	 * @return the smallCostFpLimit
	 */
	public double getSmallCostFpLimit() {
		return smallCostFpLimit;
	}

	/**
	 * @param smallCostFpLimit the smallCostFpLimit to set
	 */
	public void setSmallCostFpLimit(double smallCostFpLimit) {
		this.smallCostFpLimit = smallCostFpLimit;
	}

	/**
	 * @return the smallCostObsQuantilLimit
	 */
	public double getSmallCostObsQuantilLimit() {
		return smallCostObsQuantilLimit;
	}

	/**
	 * @param smallCostObsQuantilLimit the smallCostObsQuantilLimit to set
	 */
	public void setSmallCostObsQuantilLimit(double smallCostObsQuantilLimit) {
		this.smallCostObsQuantilLimit = smallCostObsQuantilLimit;
	}

	/**
	 * @return the ecrCompliance
	 */
	public EcrCompliance getEcrCompliance() {
		return ecrCompliance;
	}

	/**
	 * @return the ecrScFpCompliance
	 */
	public EcrCompliance getEcrScFpCompliance() {
		return ecrScFpCompliance;
	}

	/**
	 * @return the ecrScQuCompliance
	 */
	public EcrCompliance getEcrScQuCompliance() {
		return ecrScQuCompliance;
	}

	public void setEcrCompliance(EcrCompliance ecrCompliance) {
		this.ecrCompliance = ecrCompliance;
	}

	public void setEcrScFpCompliance(EcrCompliance ecrScFpCompliance) {
		this.ecrScFpCompliance = ecrScFpCompliance;
	}

	public void setEcrScQuCompliance(EcrCompliance ecrScQuCompliance) {
		this.ecrScQuCompliance = ecrScQuCompliance;
	}

}
